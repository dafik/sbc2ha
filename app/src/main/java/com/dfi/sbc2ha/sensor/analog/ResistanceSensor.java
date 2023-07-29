package com.dfi.sbc2ha.sensor.analog;


import com.dfi.sbc2ha.config.sbc2ha.definition.enums.ResistanceDirectionType;
import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.dfi.sbc2ha.sensor.ScalarSensor;
import com.dfi.sbc2ha.sensor.Sensor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static com.dfi.sbc2ha.state.sensor.ScalarState.CHANGED;

@Slf4j
public class ResistanceSensor extends ScalarSensor {

    protected final AnalogSensor delegate;
    private final ResistanceDirectionType direction;
    private final float resistor;
    private final float range;


    public ResistanceSensor(AnalogSensor delegate, String name, ResistanceDirectionType direction, float resistor, float referenceVoltage, List<Map<ValueFilterType, Number>> filters) {
        super(name);
        this.delegate = delegate;

        this.direction = direction;
        this.resistor = resistor;
        if (referenceVoltage == -1) {
            range = delegate.getRange();
        } else {
            range = referenceVoltage;
        }
        this.filters.addAll(filters);
        listeners.put(CHANGED, new LinkedHashSet<>());
    }

    public float getRawValue() {
        return delegate.getRawValue();
    }

    @Override
    protected void closeDelegate() {
        delegate.close();
    }

    @Override
    protected void removeDelegateListener() {
        delegate.removeListener(this::onDelegateChange, CHANGED);
    }

    @Override
    protected void setDelegateListener() {
        delegate.addListener(this::onDelegateChange, CHANGED);
    }

    private void onDelegateChange(StateEvent analogEvent) {

        handleChanged(getValue(((ScalarEvent) analogEvent).getValue()));
    }

    protected float calculate(float value) {
        float res = 0;
        switch (direction) {
            case UPSTREAM:
                if (value == 0.0f) {
                    res = 0;
                } else {
                    res = (range - value) / value;
                }
                break;
            case DOWNSTREAM:
                if (value == range) {
                    res = 0;
                } else {
                    res = value / (range - value);
                }
                break;
        }

        res *= resistor;
        log.debug("{} - Resistance {}Ω", name, res);
        return res;
    }

    public static class Builder extends Sensor.Builder<Builder> {
        private final ResistanceDirectionType direction;
        private final float resistor;
        protected final AnalogSensor delegate;
        private float referenceVoltage = -1;
        private List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

        public Builder(AnalogSensor source, ResistanceDirectionType direction, float resistor) {
            this.delegate = source;
            this.direction = direction;
            this.resistor = resistor;
        }

        public static Builder builder(AnalogSensor source, ResistanceDirectionType direction, float resistor) {
            return new Builder(source, direction, resistor);
        }

        public ResistanceSensor build() {
            return new ResistanceSensor(delegate, name, direction, resistor, referenceVoltage, filters);
        }


        @Override
        protected Builder self() {
            return this;
        }

        public Builder setRange(float referenceVoltage) {

            this.referenceVoltage = referenceVoltage;
            return self();
        }

        public Builder setFilters(List<Map<ValueFilterType, Number>> filters) {
            this.filters = filters;
            return self();
        }
    }
}
