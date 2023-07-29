package com.dfi.sbc2ha.actuator;


import com.dfi.sbc2ha.EasingOld;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.actuator.LedEvent;
import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.dfi.sbc2ha.helper.ha.command.LightCommand;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.api.function.FloatConsumer;
import com.diozero.devices.PwmLed;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.state.actuator.ActuatorState.OFF;
import static com.dfi.sbc2ha.state.actuator.ActuatorState.ON;

@Slf4j
@Setter
public class Led extends Actuator implements FloatConsumer {

    protected final PwmLed delegate;
    private float lastBrightness = 0;
    private float limitBrightness;
    private String lastEasing;

    private ActuatorState state;
    private int percentageDefaultBrightness;

    public Led(PwmLed delegate, String name, int id, float initialValue) {
        super(name, id);
        this.delegate = delegate;

        limitBrightness = initialValue == 0 ? 1 : initialValue;
        setState(initialValue > 0);

        listeners.put(ON, new LinkedHashSet<>());
        listeners.put(OFF, new LinkedHashSet<>());

        delegate.whenChange(this);
    }


    private void setPercentageDefaultBrightness(int percentageDefaultBrightness) {
        this.percentageDefaultBrightness = percentageDefaultBrightness;
    }

    private void setState(boolean state) {
        this.state = state ? ON : OFF;
    }

    private void handleTurnOn(long e) {
        LedEvent evt = new LedEvent(state);
        listeners.get(ON).forEach(listener -> listener.accept(evt));

        handleChangeBrightness(e, lastBrightness);

    }

    private void handleTurnOff(long e) {
        LedEvent evt = new LedEvent(state);
        listeners.get(OFF).forEach(listener -> listener.accept(evt));

        handleChangeBrightness(e, lastBrightness);

    }

    private void handleChangeBrightness(long e, float value) {
        LedEvent evt = new LedEvent(state, value);

        listeners.get(state).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }


    public void whenTurnOn(Consumer<StateEvent> consumer) {
        addListener(consumer, ON);
    }

    public void whenTurnOff(Consumer<StateEvent> consumer) {
        addListener(consumer, OFF);
    }

    public void turnOn(LightCommand cmd) {

        log.info("turnOn  id:{},{} state: {}", id, name, state);
        setState(true);
        if (cmd.getTransition() > 0) {
            log.debug("turnOn {} bright: {} trans:{}, effect:{}", name, limitBrightness, cmd.getTransition(), cmd.getEffect());
            delegate.effectBackgroundDirection(EasingOld.valueOf(cmd.getEffect().toUpperCase()), cmd.getTransition(),
                    limitBrightness, false
            );
        } else if (cmd.getEffect() != null && cmd.getTransition() == 0) {
            log.debug("turnOn {} bright: {} trans:{}, effect:{}", name, limitBrightness, 5, cmd.getEffect());
            delegate.effectBackgroundDirection(EasingOld.valueOf(cmd.getEffect().toUpperCase()), 5,
                    limitBrightness, false
            );
            lastEasing = cmd.getEffect();
        } else {
            log.debug("turnOn {} bright: {}", name, limitBrightness);
            lastEasing = null;
            delegate.setValue(limitBrightness);
        }

    }

    public void turnOff(LightCommand cmd) {
        log.info("turnOff id:{},{} state: {}", id, name, state);
        setState(false);
        if (cmd.getTransition() > 0) {
            if (cmd.getEffect() == null) {
                cmd.setEffect(EasingOld.linerIn.name());
            }
            if (lastEasing == null) {
                lastEasing = cmd.getEffect();
            }
            EasingOld easingOld = EasingOld.valueOf(lastEasing);
            log.debug("turn Off {} bright: {} trans:{}, effect:{}", name, limitBrightness, cmd.getTransition(), cmd.getEffect());
            delegate.effectBackgroundDirection(easingOld, cmd.getTransition(),
                    limitBrightness, true
            );
        } else if (lastEasing != null) {
            EasingOld easingOld = EasingOld.valueOf(lastEasing.replace("In", "Out"));
            log.debug("turn Off {} bright: {} trans:{}, effect:{}", name, limitBrightness, 5, cmd.getEffect());
            delegate.effectBackgroundDirection(easingOld, 5,
                    limitBrightness, true
            );
            lastEasing = null;
        } else {
            log.debug("turn Off {} bright: {}", name, limitBrightness);
            delegate.off();
        }
    }

    public void toggle(LightCommand lightCommand) {
        if (state == ON) {
            turnOff(lightCommand);
        } else {
            turnOn(lightCommand);
        }
    }

    public void setBrightness(float value) {
        log.debug("set brightness {}% {}", (int) (value * 100), name);
        setState(value > 0);
        limitBrightness = value;
        delegate.setValue(value);
    }

    @Override
    public void accept(float value) {
        lastBrightness = value;
        handleChangeBrightness(System.currentTimeMillis(), value);
    }

    @Override
    public void close() throws RuntimeIOException {
        delegate.close();
        super.close();
    }

    @Setter
    public static class Builder extends Actuator.Builder<Builder, PwmLed> {


        private static final int DEFAULT_FREQUENCY = 100;
        private int pwmFrequency = DEFAULT_FREQUENCY;
        private PwmOutputDeviceFactoryInterface pwmDeviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();

        private int percentageDefaultBrightness;

        public Builder(int gpio) {
            super(gpio);
        }

        public Builder(PinInfo pinInfo) {
            super(pinInfo);
        }

        public static Builder builder(int gpio) {
            return new Builder(gpio);
        }

        public static Builder builder(PinInfo pinInfo) {
            return new Builder(pinInfo);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Led build() {
            if (delegate == null) {
                setupDelegate();
            }
            Led led = new Led(delegate, name, id, initialState == null ? 0 : ((ScalarEvent) initialState).getValue());
            led.setMomentaryTurnOff(momentaryTurnOff);
            led.setMomentaryTurnOn(momentaryTurnOn);
            led.setPercentageDefaultBrightness(percentageDefaultBrightness);
            return led;
        }

        public Builder setPwmFrequency(int pwmFrequency) {
            this.pwmFrequency = pwmFrequency;
            return self();
        }

        protected Builder setupDelegate() {
            if (pinInfo == null) {
                pinInfo = pwmDeviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(gpio);
            }
            delegate = new PwmLed(pwmDeviceFactory, pinInfo.getDeviceNumber(), pwmFrequency, getInitialValue());
            return self();
        }

        public Builder setPwmDeviceFactory(PwmOutputDeviceFactoryInterface deviceFactory) {
            pwmDeviceFactory = deviceFactory;
            return self();
        }

        public Builder setPercentageDefaultBrightness(int percentageDefaultBrightness) {
            this.percentageDefaultBrightness = percentageDefaultBrightness;
            return self();
        }

        private float getInitialValue() {
            if (initialState == null) {
                return 0;
            }
            return ((ScalarEvent) initialState).getValue();
        }

    }
}
