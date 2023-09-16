package com.dfi.sbc2ha.components.sensor.temperature;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.components.sensor.TempSensor;
import com.diozero.api.I2CDevice;
import com.diozero.devices.LM75;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public class Lm75TempSensor extends TempSensor {


    public Lm75TempSensor(LM75 delegate, String name) {
        super(delegate, name);
    }

    public Lm75TempSensor(LM75 delegate, String name, Duration updateInterval) {
        super(delegate, name, updateInterval);

    }

    public Lm75TempSensor(LM75 delegate, String name, Duration updateInterval, List<Map<ValueFilterType, Number>> filters) {
        super(delegate, name, updateInterval, filters);
    }


    public static class Builder extends TempSensor.Builder<Builder> {
        private final I2CDevice bus;
        private LM75 delegate;

        public Builder(I2CDevice bus) {
            this.bus = bus;
        }

        public static Builder builder(I2CDevice bus) {
            return new Builder(bus);
        }

        @Override
        protected Builder self() {
            return this;
        }


        public Lm75TempSensor build() {
            if (delegate == null) {
                setupDelegate();
            }
            return new Lm75TempSensor(delegate, name, updateInterval, filters);
        }

        private Builder setupDelegate() {
            delegate = new LM75(bus);
            return this;
        }

    }
}
