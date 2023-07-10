package com.dfi.sbc2ha.sensor.temperature.oneWire;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.sensor.temperature.TempSensor;
import com.diozero.devices.oneWire.OneWireThermSensor;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class W1TempSensor extends TempSensor {


    public W1TempSensor(OneWireThermSensor delegate, String name) {
        super(delegate, name);
    }

    public W1TempSensor(OneWireThermSensor delegate, String name, Duration updateInterval) {
        super(delegate, name, updateInterval);

    }

    public W1TempSensor(OneWireThermSensor delegate, String name, Duration updateInterval, List<Map<ValueFilterType, Number>> filters) {
        super(delegate, name, updateInterval, filters);
    }


    public static class Builder extends TempSensor.Builder<Builder> {

        private OneWireThermSensor delegate;
        private int address;

        public Builder(int address) {
            this.address = address;
        }

        public static Builder builder(int address) {
            return new Builder(address);
        }

        @Override
        protected Builder self() {
            return this;
        }

        public Builder setAddress(int address) {
            this.address = address;
            return this;
        }

        public W1TempSensor build() {
            if (delegate == null) {
                setupDelegate();
            }
            return new W1TempSensor(delegate, name, updateInterval, filters);
        }

        private Builder setupDelegate() {
            //delegate = new LM75(BoneIoBBB.I2C_CONTROLLER, (byte) address);
            return this;
        }

    }
}
