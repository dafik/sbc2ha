package com.dfi.sbc2ha.sensor.analog;


import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.helper.deserializer.DurationStyle;
import com.dfi.sbc2ha.sensor.ScalarSensor;
import com.dfi.sbc2ha.sensor.TempSensor;
import com.diozero.api.AnalogInputDevice;
import com.diozero.api.AnalogInputEvent;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import com.diozero.internal.spi.AnalogInputDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static com.dfi.sbc2ha.state.sensor.ScalarState.CHANGED;

@Slf4j
public class AnalogSensor extends ScalarSensor {
    public static final Duration UPDATE_INTERVAL = DurationStyle.detectAndParse("60s");
    protected final AnalogInputDevice delegate;
    private final Duration updateInterval;

    public AnalogSensor(AnalogInputDevice delegate, String name) {
        this(delegate, name, UPDATE_INTERVAL);
    }

    public AnalogSensor(AnalogInputDevice delegate, String name, Duration updateInterval) {
        this(delegate, name, updateInterval, new ArrayList<>());
    }
    public AnalogSensor(AnalogInputDevice delegate, String name, Duration updateInterval, List<Map<ValueFilterType, Number>> filters) {
        super(name);
        this.delegate = delegate;
        this.updateInterval = updateInterval;

        this.filters.addAll(filters);
        listeners.put(CHANGED, new LinkedHashSet<>());
    }
    @Override
    public float getRawValue() {
        return delegate.getScaledValue();
    }


    @Override
    protected float calculate(float value) {
        log.debug("{} - Voltage {}V", name, value);
        return value;
    }

    private void onDelegateChange(AnalogInputEvent analogInputEvent) {
        handleChanged(getValue(analogInputEvent.getScaledValue()));
    }

    @Override
    protected void setDelegateListener() {
        delegate.addListener(this::onDelegateChange, 0.001f, (int) updateInterval.toMillis());
    }

    @Override
    protected void removeDelegateListener() {
        delegate.removeListener(this::onDelegateChange);
    }

    public float getRange() {
        return delegate.getRange();

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
