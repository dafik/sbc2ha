package com.dfi.sbc2ha.actuator;


import com.dfi.sbc2ha.Easing;
import com.dfi.sbc2ha.helper.ha.command.LightCommand;
import com.diozero.api.PinInfo;
import com.diozero.api.function.FloatConsumer;
import com.diozero.devices.PwmLed;
import com.diozero.internal.spi.NativeDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.Setter;
import org.tinylog.Logger;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.actuator.ActuatorState.OFF;
import static com.dfi.sbc2ha.actuator.ActuatorState.ON;

@Setter
public class Led extends Actuator<PwmLed, LedEvent, ActuatorState> implements FloatConsumer {

    private float lastBrightness = 0;
    private float limitBrightness;
    private String lastEasing;

    private ActuatorState state;

    public Led(PwmLed delegate, String name, float initialValue) {
        super(delegate, name);

        limitBrightness = initialValue == 0 ? 1 : initialValue;
        setState(initialValue > 0);

        listeners.put(ON, new LinkedHashSet<>());
        listeners.put(OFF, new LinkedHashSet<>());

        delegate.whenChange(this);
    }

    private void setState(boolean state) {
        this.state = state ? ON : OFF;
    }

    private void handleTurnOn(long e) {
        LedEvent evt = new LedEvent();
        evt.setEventTime(e);
        evt.setState(state);
        listeners.get(ON).forEach(listener -> listener.accept(evt));

        handleChangeBrightness(e, lastBrightness);
/*
        LedEvent change = LedEvent.CHANGE;
        change.setValue(lastBrightness);
        handleAny(e, change);
        //handleBrightness(e,change);*/
    }

    private void handleTurnOff(long e) {
        LedEvent evt = new LedEvent();
        evt.setEventTime(e);
        evt.setState(state);
        listeners.get(OFF).forEach(listener -> listener.accept(evt));

        handleChangeBrightness(e, lastBrightness);

        /*LedEvent change = LedEvent.CHANGE;
        change.setValue(lastBrightness);
        handleAny(e, change);*/
    }

    private void handleChangeBrightness(long e, float value) {
        LedEvent evt = new LedEvent(e, value, state);
        listeners.get(state).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }


    public void whenTurnOn(Consumer<LedEvent> consumer) {
        addListener(consumer, ON);
    }

    public void whenTurnOff(Consumer<LedEvent> consumer) {
        addListener(consumer, OFF);
    }

    public void turnOn(LightCommand cmd) {
        Logger.debug("turn On {} state: {}", name, state);
        setState(true);
        if (cmd.getTransition() > 0) {
            Logger.debug("turn On {} bright: {}", name, limitBrightness, cmd.getTransition(), cmd.getEffect());
            delegate.effectBackgroundDirection(cmd.getEffect(), cmd.getTransition(),
                    limitBrightness, false
            );
        } else if (cmd.getEffect() != null && cmd.getTransition() == 0) {
            Logger.debug("turn On {} bright: {}", name, limitBrightness, 5, cmd.getEffect());
            delegate.effectBackgroundDirection(cmd.getEffect(), 5,
                    limitBrightness, false
            );
            lastEasing = cmd.getEffect().name();
        } else {
            Logger.debug("turn On {} bright: {}", name, limitBrightness);
            lastEasing = null;
            delegate.setValue(limitBrightness);
        }

    }

    public void turnOff(LightCommand cmd) {
        Logger.debug("turn Off {} state: {}", name, state);
        setState(false);
        if (cmd.getTransition() > 0) {
            if (cmd.getEffect() == null) {
                cmd.setEffect(Easing.linerIn);
            }
            if (lastEasing == null) {
                lastEasing = cmd.getEffect().name();
            }
            Easing easing = Easing.valueOf(lastEasing);
            Logger.debug("turn Off {} bright: {}", name, limitBrightness, cmd.getTransition(), cmd.getEffect());
            delegate.effectBackgroundDirection(easing, cmd.getTransition(),
                    limitBrightness, true
            );
        } else if (lastEasing != null) {
            Easing easing = Easing.valueOf(lastEasing.replace("In", "Out"));
            Logger.debug("turn Off {} bright: {}", name, limitBrightness, 5, cmd.getEffect());
            delegate.effectBackgroundDirection(easing, 5,
                    limitBrightness, true
            );
            lastEasing = null;
        } else {
            Logger.debug("turn Off {} bright: {}", name, limitBrightness);
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
        Logger.debug("set brightness {}% {}", (int) (value * 100), name);
        setState(value > 0);
        limitBrightness = value;
        delegate.setValue(value);
    }

    @Override
    public void accept(float value) {
        lastBrightness = value;
        handleChangeBrightness(System.currentTimeMillis(), value);
    }


    @Setter
    public static class Builder extends Actuator.Builder<Builder, PwmLed> {

        private static final int DEFAULT_FREQUENCY = 100;

        private NativeDeviceFactoryInterface nativeDeviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();
        private int pwmFrequency = DEFAULT_FREQUENCY;

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
            return new Led(delegate, name, initialValue.floatValue());
        }

        public Builder setPwmFrequency(int pwmFrequency) {
            this.pwmFrequency = pwmFrequency;
            return self();
        }

        public Builder setDeviceFactory(NativeDeviceFactoryInterface deviceFactory) {
            nativeDeviceFactory = deviceFactory;
            return self();
        }

        protected Builder setupDelegate() {
            if (pinInfo == null) {
                pinInfo = nativeDeviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(gpio);
            }
            PwmLed pwmLed = new PwmLed(nativeDeviceFactory, pinInfo.getDeviceNumber(), pwmFrequency, initialValue.floatValue());


            delegate = pwmLed;
            return self();
        }
    }
}
