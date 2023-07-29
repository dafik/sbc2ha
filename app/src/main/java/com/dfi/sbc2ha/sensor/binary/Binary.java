package com.dfi.sbc2ha.sensor.binary;

import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.sensor.BinaryEvent;
import com.dfi.sbc2ha.sensor.BinarySensor;
import com.diozero.api.DigitalInputDevice;
import com.diozero.api.PinInfo;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.state.sensor.BinaryState.PRESSED;
import static com.dfi.sbc2ha.state.sensor.BinaryState.RELEASED;

@Setter
public class Binary extends BinarySensor {

    public Binary(DigitalInputDevice delegate, String name, boolean inverted) {
        super(delegate, name, inverted);

        listeners.put(PRESSED, new LinkedHashSet<>());
        listeners.put(RELEASED, new LinkedHashSet<>());
    }

    protected void setDelegateListener() {
        if (inverted) {
            delegate.whenActivated(this::handlePressed);
            delegate.whenDeactivated(this::handleReleased);
        } else {
            delegate.whenActivated(this::handleReleased);
            delegate.whenDeactivated(this::handlePressed);
        }

    }

    private void handlePressed(long e) {
        StateEvent event = new BinaryEvent(PRESSED);
        listeners.get(PRESSED).forEach(listener -> listener.accept(event));
        handleAny(event);
    }

    private void handleReleased(long e) {
        BinaryEvent event = new BinaryEvent(RELEASED);
        listeners.get(RELEASED).forEach(listener -> listener.accept(event));
        handleAny(event);
    }

    public void whenPressed(Consumer<StateEvent> consumer) {
        addListener(consumer, PRESSED);
    }

    public void whenReleased(Consumer<StateEvent> consumer) {
        addListener(consumer, RELEASED);
    }

    public StateEvent getInitialState() {
        if (inverted) {
            return new BinaryEvent(delegate.getValue() ? PRESSED : RELEASED);
        } else {
            return new BinaryEvent(delegate.getValue() ? RELEASED : PRESSED);
        }
    }

    @Setter
    public static class Builder extends BinarySensor.Builder<Builder> {

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

        public Binary build() {
            if (delegate == null) {
                setupDelegate();
            }
            return new Binary(delegate, name, inverted);
        }
    }
}
