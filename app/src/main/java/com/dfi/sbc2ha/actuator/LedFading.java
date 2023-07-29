package com.dfi.sbc2ha.actuator;


import com.dfi.sbc2ha.EasingType;
import com.dfi.sbc2ha.EasingVariant;
import com.dfi.sbc2ha.event.actuator.LedFadingEvent;
import com.dfi.sbc2ha.helper.ha.command.LightCommand;
import com.dfi.sbc2ha.state.actuator.ActuatorState;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.PwmFadingLed;
import com.diozero.internal.spi.PwmOutputDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashSet;

import static com.dfi.sbc2ha.state.actuator.ActuatorState.OFF;
import static com.dfi.sbc2ha.state.actuator.ActuatorState.ON;

@Slf4j
@Setter
public class LedFading extends Actuator {

    protected final PwmFadingLed delegate;
    private float limitBrightness;
    private LedFadingEvent lastEvent;
    private String lastEasing;
    private ActuatorState state;

    public LedFading(PwmFadingLed delegate, String name, int id, LedFadingEvent initialValue) {
        super(name, id);
        this.delegate = delegate;

        limitBrightness = initialValue == null ? 1 : initialValue.getBrightnessRaw() == 0 ? 1 : initialValue.getBrightnessRaw();
        this.lastEvent = initialValue;
        setState(limitBrightness > 0);

        listeners.put(ON, new LinkedHashSet<>());
        listeners.put(OFF, new LinkedHashSet<>());

        delegate.whenChange(this::handleChange);
    }

    private static EasingType getEasingType(String effect) {
        String[] parts = effect.split("-");
        return EasingType.valueOf(parts[0].toUpperCase());
    }

    private static EasingVariant getEasingVariant(String effect) {
        String[] parts = effect.split("-");
        return EasingVariant.valueOf(parts[1].toUpperCase());
    }


    private void setState(boolean state) {
        this.state = state ? ON : OFF;
    }

    private void handleChange(PwmFadingLed.PwmEvent event) {
        LedFadingEvent evt = new LedFadingEvent(event.getValue() > 0 ? ON : OFF, event.getValue(), event.getStep(), event.getEasingType(), event.getEasingVariant());
        lastEvent = evt;
        listeners.get(state).forEach(listener -> listener.accept(evt));
        handleAny(evt);
    }

    public void turnOn(LightCommand cmd) {

        log.info("turnOn  id:{},{} state: {}", id, name, state);
        setState(true);
        if (cmd.getTransition() > 0) {
            log.debug("turnOn {} bright: {} trans:{}, effect:{}", name, limitBrightness, cmd.getTransition(), cmd.getEffect());
            delegate.effectBackgroundDirection(getEasingType(cmd.getEffect()), getEasingVariant(cmd.getEffect()),
                    0, cmd.getTransition(), limitBrightness, true);
        } else if (cmd.getEffect() != null && cmd.getTransition() == 0) {
            log.debug("turnOn {} bright: {} trans:{}, effect:{}", name, limitBrightness, 3, cmd.getEffect());
            delegate.effectBackgroundDirection(getEasingType(cmd.getEffect()), getEasingVariant(cmd.getEffect()),
                    0, 3, limitBrightness, true
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
                cmd.setEffect(EasingType.LINEAR + "-" + EasingVariant.OUT);
            }
            log.debug("turn Off {} bright: {} trans:{}, effect:{}", name, limitBrightness, cmd.getTransition(), cmd.getEffect());
            delegate.effectBackgroundDirection(getEasingType(cmd.getEffect()), getEasingVariant(cmd.getEffect()),
                    lastEvent.getStep(), cmd.getTransition(), limitBrightness, false
            );
        } else if (lastEvent.getEasingType() != null) {
            EasingVariant variant = changeVariamt();
            log.debug("turn Off {} bright: {} trans:{}, effect:{}", name, limitBrightness,lastEvent.getStep(), cmd.getEffect());
            delegate.effectBackgroundDirection(lastEvent.getEasingType(), variant, lastEvent.getStep(),
                    lastEvent.getStep(), limitBrightness, false
            );
            lastEasing = null;
        } else {
            log.debug("turn Off {} bright: {}", name, limitBrightness);
            delegate.off();
        }
    }

    private EasingVariant changeVariamt() {
        EasingVariant variant;
        EasingVariant lastVariant = lastEvent.getEasingVariant();
        if (lastVariant == EasingVariant.IN) {
            variant = EasingVariant.OUT;
        } else if (lastVariant == EasingVariant.OUT) {
            variant = EasingVariant.IN;
        } else {
            variant = EasingVariant.INOUT;
        }
        return variant;
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
    public void close() throws RuntimeIOException {
        delegate.close();
        super.close();
    }

    @Setter
    public static class Builder extends Actuator.Builder<Builder, PwmFadingLed> {


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

        public LedFading build() {
            if (delegate == null) {
                setupDelegate();
            }
            LedFading led = new LedFading(delegate, name, id, (LedFadingEvent) initialState);
            led.setMomentaryTurnOff(momentaryTurnOff);
            led.setMomentaryTurnOn(momentaryTurnOn);
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
            delegate = new PwmFadingLed(pwmDeviceFactory, pinInfo.getDeviceNumber(), pwmFrequency, ((LedFadingEvent) initialState).getBrightnessRaw());

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

    }
}
