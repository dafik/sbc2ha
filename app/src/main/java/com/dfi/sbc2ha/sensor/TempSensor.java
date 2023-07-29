package com.dfi.sbc2ha.sensor;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.diozero.devices.ThermometerInterface;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static com.dfi.sbc2ha.state.sensor.ScalarState.CHANGED;

@Slf4j
public abstract class TempSensor extends ScheduledSensor {

    protected final ThermometerInterface delegate;
    private float lastDelegateValue;


    public TempSensor(ThermometerInterface delegate, String name) {
        this(delegate, name, UPDATE_INTERVAL);
    }

    public TempSensor(ThermometerInterface delegate, String name, Duration updateInterval) {
        this(delegate, name, updateInterval, new ArrayList<>());
    }

    public TempSensor(ThermometerInterface delegate, String name, Duration updateInterval, List<Map<ValueFilterType, Number>> filters) {
        super(name, updateInterval);
        this.delegate = delegate;

        this.filters.addAll(filters);

        listeners.put(CHANGED, new LinkedHashSet<>());
    }

    @Override
    public void run() {
        log.trace("Refreshing temp sensor {}", name);
        if (stopScheduler.get()) {
            throw new RuntimeException("Stopping scheduler due to close request, device key=" + getName());
        }

        float temperature = getValue(getRawValue());
        if (temperature != lastDelegateValue) {
            lastDelegateValue = temperature;
            handleChanged(temperature);
        }
        log.trace("Refreshed temp sensor {} : {}", name, temperature);

    }


    @Override
    protected float calculate(float value) {
        return value;
    }

    public float getRawValue() {
        return delegate.getTemperatureCelsius();
    }


    @Override
    protected void closeDelegate() {
        delegate.close();
    }

    public abstract static class Builder<T extends Builder<T>> extends Sensor.Builder<T> {

        protected Duration updateInterval = UPDATE_INTERVAL;
        protected List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

        public T setUpdateInterval(Duration updateInterval) {
            this.updateInterval = updateInterval;
            return self();
        }

        public T setFilters(List<Map<ValueFilterType, Number>> filters) {
            this.filters = filters;
            return self();
        }
    }
}
