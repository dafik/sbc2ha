package com.dfi.sbc2ha.sensor.temperature;

import com.dfi.sbc2ha.helper.BoneIoBBB;
import com.diozero.devices.LM75;

import java.time.Duration;
import java.util.Map;

public class Lm75TempSensor extends TempSensor {


    public Lm75TempSensor(LM75 delegate, String name) {
        super(delegate, name);
    }

    public Lm75TempSensor(LM75 delegate, String name, Duration updateInterval) {
        super(delegate, name, updateInterval);

    }

    public Lm75TempSensor(LM75 delegate, String name, Duration updateInterval, Map<String, String> filters) {
        super(delegate, name, updateInterval, filters);
    }


    public static class Builder extends TempSensor.Builder<Builder> {

        private LM75 delegate;
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

        public Lm75TempSensor build() {
            if (delegate == null) {
                setupDelegate();
            }
            return new Lm75TempSensor(delegate, name, updateInterval, filters);
        }

        private Builder setupDelegate() {
            delegate = new LM75(BoneIoBBB.I2C_CONTROLLER, (byte) address);
            return this;
        }

    }
}
