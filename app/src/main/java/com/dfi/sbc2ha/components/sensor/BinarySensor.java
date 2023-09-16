package com.dfi.sbc2ha.components.sensor;

import com.diozero.api.DebouncedDigitalInputDevice;
import com.diozero.api.DigitalInputDevice;
import com.diozero.api.GpioPullUpDown;
import com.diozero.api.PinInfo;
import com.diozero.internal.spi.GpioDeviceFactoryInterface;
import com.diozero.sbc.DeviceFactoryHelper;

public abstract class BinarySensor extends Sensor {
    protected final boolean inverted;
    protected final DigitalInputDevice delegate;

    public BinarySensor(DigitalInputDevice delegate, String name, boolean inverted) {
        super(name);
        this.delegate = delegate;
        this.inverted = inverted;
    }

    protected void removeDelegateListener() {
        delegate.whenActivated(null);
        delegate.whenDeactivated(null);
    }

    @Override
    protected void closeDelegate() {
        delegate.close();
    }

    public abstract static class Builder<T extends Builder<T>> extends Sensor.Builder<T> {
        protected Integer gpio;
        protected PinInfo pinInfo;
        protected DigitalInputDevice delegate;
        protected boolean inverted = false;
        private GpioPullUpDown pud = GpioPullUpDown.NONE;
        private GpioDeviceFactoryInterface deviceFactory;

        private int debounceTimeMillis = 0;

        public Builder(int gpio) {

            this.gpio = gpio;
        }

        public Builder(PinInfo pinInfo) {

            this.pinInfo = pinInfo;
        }

        public T setPullUpDown(GpioPullUpDown pud) {
            this.pud = pud;
            return self();
        }

        protected T setupDelegate() {
            if (deviceFactory == null) {
                deviceFactory = DeviceFactoryHelper.getNativeDeviceFactory();
            }
            if (pinInfo == null) {
                pinInfo = deviceFactory.getBoardPinInfo().getByGpioNumberOrThrow(gpio);
            }


            if (debounceTimeMillis > 0) {
                var builder = DebouncedDigitalInputDevice.Builder.builder(pinInfo, debounceTimeMillis);

                builder
                        //.setPullUpDown(pud)
                        .setDeviceFactory(deviceFactory);

                delegate = builder.build();
            } else {
                var builder = DigitalInputDevice.Builder.builder(pinInfo);

                builder
                        //.setPullUpDown(pud)
                        .setDeviceFactory(deviceFactory);

                delegate = builder.build();
            }


            return self();
        }

        public T setGpio(Integer gpio) {
            this.gpio = gpio;
            return self();
        }

        public T setPinInfo(PinInfo pinInfo) {
            this.pinInfo = pinInfo;
            return self();

        }

        public T setPud(GpioPullUpDown pud) {
            this.pud = pud;
            return self();
        }

        public T setDeviceFactory(GpioDeviceFactoryInterface deviceFactory) {
            this.deviceFactory = deviceFactory;
            return self();
        }

        public T setDelegate(DigitalInputDevice delegate) {
            this.delegate = delegate;
            return self();
        }

        public T setInverted(boolean inverted) {
            this.inverted = inverted;
            return self();
        }

        public T setDebounceTimeMillis(long debounceTimeMillis) {
            this.debounceTimeMillis = (int) debounceTimeMillis;
            return self();
        }
    }
}
