package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class SingleSourceConfig extends ScalarSensorConfig {
    /**
     * The sensor to read values.
     * (Required, ID):
     */
    private String sensor;

    public SingleSourceConfig(String sensor) {
        super();
        this.sensor = sensor;
    }

    public SingleSourceConfig() {
        super();
    }

    public String getSensor() {
        return sensor;
    }
}
