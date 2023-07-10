package com.dfi.sbc2ha.sensor.temperature;

import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.sensor.Sensor;
import com.dfi.sbc2ha.sensor.scheduled.ScheduledSensor;
import com.diozero.api.RuntimeIOException;
import com.diozero.devices.ThermometerInterface;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

import static com.dfi.sbc2ha.sensor.temperature.TempState.CHANGED;

public abstract class TempSensor extends ScheduledSensor<ThermometerInterface, TempEvent, TempState> implements ThermometerInterface {

    protected final ThermometerInterface delegate;
    private final List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

    protected float lastRawValue;
    protected float lastValue;

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
        Logger.trace("Refreshing temp sensor {}", name);
        if (stopScheduler.get()) {
            throw new RuntimeException("Stopping scheduler due to close request, device key=" + getName());
        }

        float temperature = getTemperature();
        if (temperature != lastValue) {
            lastValue = temperature;
            handleChanged(temperature);
        }
        Logger.trace("Refreshed temp sensor {} : {}", name, temperature);

    }

    public void handleChanged(float temperature) {
        TempEvent event = new TempEvent(CHANGED, temperature);
        listeners.get(CHANGED).forEach(consumer -> consumer.accept(event));
        handleAny(event);
    }

    public void whenChanged(Consumer<TempEvent> consumer) {
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


    @Override
    public float getTemperature() throws RuntimeIOException {
        float rawValue = getRawTemperature();
        if (lastValue != 0 && rawValue == lastRawValue) {
            return lastValue;
        }
        lastRawValue = rawValue;
        return applyFilters(rawValue);

    }

    public float getRawTemperature() {
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
