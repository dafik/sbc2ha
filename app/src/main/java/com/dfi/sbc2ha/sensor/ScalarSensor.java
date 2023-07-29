package com.dfi.sbc2ha.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.diozero.api.RuntimeIOException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.state.sensor.ScalarState.CHANGED;

public abstract class ScalarSensor extends Sensor {
    protected final List<Map<ValueFilterType, Number>> filters = new ArrayList<>();
    protected float lastRawValue;
    protected float lastValue;

    public ScalarSensor(String name) {
        super(name);
    }

    public void handleChanged(float value) {
        ScalarEvent e = new ScalarEvent( value);
        listeners.get(CHANGED).forEach(consumer -> consumer.accept(e));
        handleAny(e);
    }

    public void whenChanged(Consumer<StateEvent> consumer) {
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

    public abstract float getRawValue();

    protected abstract float calculate(float value);

    public float getValue() {
        return getValue(getRawValue());
    }


    public float getValue(float rawValue) throws RuntimeIOException {
        float calculatedValue = calculate(rawValue);
        if (lastValue != 0 && calculatedValue == lastRawValue) {
            return lastValue;
        }
        lastRawValue = calculatedValue;
        return applyFilters(calculatedValue);
    }

}
