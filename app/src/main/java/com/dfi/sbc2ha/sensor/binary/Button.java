package com.dfi.sbc2ha.sensor.binary;

import com.dfi.sbc2ha.helper.detector.ClickDetector;
import com.dfi.sbc2ha.helper.detector.ClickDetectorFactory;
import com.diozero.api.DigitalInputDevice;
import com.diozero.api.PinInfo;
import lombok.Setter;
import org.tinylog.Logger;

import java.util.LinkedHashSet;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.sensor.binary.ButtonState.*;

@Setter
public class Button extends BinarySensor<ButtonEvent, ButtonState> {
    public static final int DOUBLE_CLICK_DURATION_MS = 350;
    public static final int LONG_PRESS_DURATION_MS = 700;
    private ClickDetector clickDetector;

    public Button(DigitalInputDevice delegate, String name, ButtonState clickDetection, int doubleClickDurationMs, int longPressDurationMs, boolean inverted) {
        super(delegate, name, inverted);
        clickDetector = ClickDetectorFactory.build(clickDetection, doubleClickDurationMs, longPressDurationMs, this::handleRelease, this::handleCLick, this::handleDoubleClick, this::handleLongPress);

        listeners.put(SINGLE, new LinkedHashSet<>());
        listeners.put(DOUBLE, new LinkedHashSet<>());
        listeners.put(LONG, new LinkedHashSet<>());

        listeners.put(RELEASE, new LinkedHashSet<>());
    }

    protected void setDelegateListener() {
        delegate.whenActivated(e -> {
            Logger.debug("onActivated on {}, time:{}", name, e);
            clickDetector.detect(e, inverted ? Direction.PRESS : Direction.RELEASE);
        });
        delegate.whenDeactivated(e -> {
            Logger.debug("onDeactivated on {}, time:{}", name, e);
            clickDetector.detect(e, inverted ? Direction.RELEASE : Direction.PRESS);
        });
    }

    private void handleLongPress(long e) {
        Logger.info("onLongPress on {}, time:{}", name, e);
        ButtonEvent event = new ButtonEvent(LONG);
        listeners.get(LONG).forEach(listener -> listener.accept(event));
        handleAny(event);
        //handleAny(new ButtonEvent(System.currentTimeMillis(), RELEASE));
    }

    private void handleDoubleClick(long e) {
        Logger.info("onDoubleClik on {}, time:{}", name, e);
        ButtonEvent event = new ButtonEvent( DOUBLE);
        listeners.get(DOUBLE).forEach(listener -> listener.accept(event));
        handleAny(event);
        //handleAny(new ButtonEvent(System.currentTimeMillis(), RELEASE));
    }

    private void handleCLick(long e) {
        Logger.info("onClik on {}, time:{}", name, e);
        ButtonEvent event = new ButtonEvent( SINGLE);
        listeners.get(SINGLE).forEach(listener -> listener.accept(event));
        handleAny(event);
        //handleAny(new ButtonEvent(System.currentTimeMillis(), RELEASE));
    }

    private void handleRelease(long e) {
        Logger.info("onRelease on {}, time:{}", name, e);
        ButtonEvent event = new ButtonEvent( RELEASE);
        listeners.get(RELEASE).forEach(listener -> listener.accept(event));
        handleAny(event);

    }

    public void whenClick(Consumer<ButtonEvent> consumer) {
        addListener(consumer, SINGLE);
    }

    public void whenDoubleClick(Consumer<ButtonEvent> consumer) {
        addListener(consumer, DOUBLE);
    }

    public void whenLongPress(Consumer<ButtonEvent> consumer) {
        addListener(consumer, LONG);
    }


    public static class Builder extends BinarySensor.Builder<Builder> {
        private int doubleClickDurationMs = DOUBLE_CLICK_DURATION_MS;
        private int longPressDurationMs = LONG_PRESS_DURATION_MS;
        private ButtonState clickDetection = LONG;

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

        public Button build() {
            if (delegate == null) {
                setupDelegate();
            }
            return new Button(delegate, name, clickDetection, doubleClickDurationMs, longPressDurationMs, inverted);
        }

        public Builder setDoubleClickDurationMs(int doubleClickDurationMs) {
            this.doubleClickDurationMs = doubleClickDurationMs;
            return this;
        }

        public Builder setLongPressDurationMs(int longPressDurationMs) {
            this.longPressDurationMs = longPressDurationMs;
            return this;
        }

        public Builder setClickDetection(ButtonState clickDetection) {
            this.clickDetection = clickDetection;
            return this;
        }
    }
}
