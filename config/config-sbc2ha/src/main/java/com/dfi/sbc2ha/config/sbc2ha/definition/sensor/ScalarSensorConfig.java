package com.dfi.sbc2ha.config.sbc2ha.definition.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public abstract class ScalarSensorConfig extends SensorConfig {
    private List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

    public ScalarSensorConfig() {
        super();
    }

}
