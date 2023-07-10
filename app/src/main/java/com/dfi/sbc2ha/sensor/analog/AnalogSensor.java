package com.dfi.sbc2ha.sensor.analog;


import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.dfi.sbc2ha.sensor.scheduled.ScheduledSensor;
import com.dfi.sbc2ha.sensor.temperature.TempSensor;
import com.diozero.api.AnalogInputDevice;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import com.diozero.api.RuntimeIOException;
import com.diozero.internal.spi.AnalogInputDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.sensor.analog.AnalogState.CHANGED;

public class AnalogSensor extends ScheduledSensor<AnalogInputDevice, AnalogEvent, AnalogState> {

    public static final Duration UPDATE_INTERVAL = DurationStyle.detectAndParse("60s");

    protected final AnalogInputDevice delegate;
    private final List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

    protected float lastRawValue;
    protected float lastValue;

    public AnalogSensor(AnalogInputDevice delegate, String name) {
        this(delegate, name, UPDATE_INTERVAL);
    }

    public AnalogSensor(AnalogInputDevice delegate, String name, Duration updateInterval) {
        this(delegate, name, updateInterval, new ArrayList<>());
    }

    public AnalogSensor(AnalogInputDevice delegate, String name, Duration updateInterval, List<Map<ValueFilterType, Number>> filters) {
        super(name, updateInterval);
        this.delegate = delegate;

        this.filters.addAll(filters);

        listeners.put(CHANGED, new LinkedHashSet<>());
    }

    @Override
    public void run() {
        Logger.debug("Refreshing analog sensor {}", name);
        if (stopScheduler.get()) {
            throw new RuntimeException("Stopping scheduler due to close request, device key=" + getName());
        }

        float temperature = getValue();
        if (temperature != lastValue) {
            lastValue = temperature;
            handleChanged(temperature);
        }
        Logger.debug("Refreshed analog sensor {} : {}", name, temperature);

    }

    public void handleChanged(float temperature) {
        AnalogEvent e = new AnalogEvent(CHANGED, temperature);
        listeners.get(CHANGED).forEach(consumer -> consumer.accept(e));
        handleAny(e);
    }

    public void whenChanged(Consumer<AnalogEvent> consumer) {
        addListener(consumer, CHANGED);

    }


    private float applyFilters(float rawValue) {
        for (var filter : filters) {
            for (Map.Entry<ValueFilterType, Number> entry : filter.entrySet()) {
                ValueFilterType valueFilterType = entry.getKey();
                Number number = entry.getValue();
                rawValue = valueFilterType.getFilter().apply(rawValue, number.floatValue());
            }
        }
        return rawValue;
    }


    public float getValue() throws RuntimeIOException {
        float rawValue = getRawValue();
        if (lastValue != 0 && rawValue == lastRawValue) {
            return lastValue;
        }
        lastRawValue = rawValue;
        return applyFilters(rawValue);

    }

    public float getRawValue() {
        return delegate.getScaledValue();
    }


    @Override
    protected void closeDelegate() {
        delegate.close();
    }

    public static class Builder extends TempSensor.Builder<Builder> {
        protected Integer gpio;
        protected PinInfo pinInfo;
        protected AnalogInputDevice delegate;
        private GpioPullUpDown pud = GpioPullUpDown.NONE;
        private AnalogInputDeviceFactoryInterface deviceFactory;

        public Builder(int gpio) {

            this.gpio = gpio;
        }

        public Builder(PinInfo pinInfo) {

            this.pinInfo = pinInfo;
        }

        public static Builder builder(int gpio) {
            return new Builder(gpio);
        }

        public static Builder builder(PinInfo pinInfo) {
            return new Builder(pinInfo);
        }

        public AnalogSensor build() {
            if (delegate == null) {
                setupDelegate();
            }
            return new AnalogSensor(delegate, name, updateInterval, filters);
        }

        public Builder setPullUpDown(GpioPullUpDown pud) {
            this.pud = pud;
            return self();
        }

        protected Builder setupDelegate() {
            if (pinInfo == null) {
                pinInfo = deviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(gpio);
            }
            if (deviceFactory == null) {
                deviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();
            }
            AnalogInputDevice.Builder builder = AnalogInputDevice.Builder
                    .builder(pinInfo)
                    //.setPullUpDown(pud)
                    .setGpioDeviceFactoryInterface(deviceFactory);

            delegate = builder.build();
            return self();
        }

        public Builder setGpio(Integer gpio) {
            this.gpio = gpio;
            return self();
        }

        public Builder setPinInfo(PinInfo pinInfo) {
            this.pinInfo = pinInfo;
            return self();

        }

        public Builder setPud(GpioPullUpDown pud) {
            this.pud = pud;
            return self();
        }

        public Builder setDeviceFactory(AnalogInputDeviceFactoryInterface deviceFactory) {
            this.deviceFactory = deviceFactory;
            return self();
        }

        public Builder setDelegate(AnalogInputDevice delegate) {
            this.delegate = delegate;
            return self();
        }

        @Override
        protected Builder self() {
            return this;
        }
    }
}
