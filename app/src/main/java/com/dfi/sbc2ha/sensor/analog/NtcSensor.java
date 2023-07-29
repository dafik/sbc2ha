package com.dfi.sbc2ha.sensor.analog;


import com.dfi.sbc2ha.config.sbc2ha.definition.filters.ValueFilterType;
import com.dfi.sbc2ha.config.sbc2ha.definition.sensor.analog.NtcConfig;
import com.dfi.sbc2ha.event.StateEvent;
import com.dfi.sbc2ha.event.sensor.ScalarEvent;
import com.dfi.sbc2ha.helper.UnitConverter;
import com.dfi.sbc2ha.sensor.ScalarSensor;
import com.dfi.sbc2ha.sensor.Sensor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.dfi.sbc2ha.sensor.analog.NtcSensor.Builder.ZERO_POINT;
import static com.dfi.sbc2ha.state.sensor.ScalarState.CHANGED;

@Slf4j
public class NtcSensor extends ScalarSensor {
    protected final ResistanceSensor delegate;
    private final NtcCalibration calibration;


    public NtcSensor(ResistanceSensor delegate, String name, NtcCalibration calibration, List<Map<ValueFilterType, Number>> filters) {
        super(name);
        this.delegate = delegate;
        this.calibration = calibration;
        this.filters.addAll(filters);
        listeners.put(CHANGED, new LinkedHashSet<>());
    }

    public float getRawValue() {
        return delegate.getRawValue();
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

        double lr = Math.log(value);
        double v = calibration.a + calibration.b * lr + calibration.c * lr * lr * lr;
        float temp = (float) (1d / v - 273.15d);

        log.debug("{} - Temperature {}°C", name, temp);
        return temp;
    }

    @Override
    protected void closeDelegate() {
        delegate.close();
    }

    public static class Builder extends Sensor.Builder<Builder> {
        public static final double ZERO_POINT = 273.15d;
        private final NtcCalibration calibration;

        protected final ResistanceSensor delegate;
        private List<Map<ValueFilterType, Number>> filters = new ArrayList<>();

        public Builder(ResistanceSensor source, NtcCalibration calibration) {
            this.delegate = source;
            this.calibration = calibration;
        }

        public static Builder builder(ResistanceSensor source, NtcConfig.BconstantCalibration calibration) {
            return new Builder(source, calcB(calibration));
        }

        public static Builder builder(ResistanceSensor source, NtcConfig.ValueCalibration calibration) {
            return new Builder(source, calcSteinhartHart(calibration));
        }

        public static NtcCalibration calcSteinhartHart(NtcConfig.ValueCalibration calibration) {
            double r1 = UnitConverter.resistance(calibration.getLow().getResistance());
            double t1 = UnitConverter.temperature(calibration.getLow().getTemperature()) + ZERO_POINT;
            double r2 = UnitConverter.resistance(calibration.getMid().getResistance());
            double t2 = UnitConverter.temperature(calibration.getMid().getTemperature()) + ZERO_POINT;
            double r3 = UnitConverter.resistance(calibration.getHigh().getResistance());
            double t3 = UnitConverter.temperature(calibration.getHigh().getTemperature()) + ZERO_POINT;
            double l1 = Math.log(r1);
            double l2 = Math.log(r2);
            double l3 = Math.log(r3);
            double y1 = 1 / t1;
            double y2 = 1 / t2;
            double y3 = 1 / t3;
            double g2 = (y2 - y1) / (l2 - l1);
            double g3 = (y3 - y1) / (l3 - l1);
            double c = (g3 - g2) / (l3 - l2) * 1 / (l1 + l2 + l3);
            double b = g2 - c * (l1 * l1 + l1 * l2 + l2 * l2);
            double a = y1 - (b + l1 * l1 * c) * l1;
            return new NtcCalibration(a, b, c);
        }

        public static NtcCalibration calcB(NtcConfig.BconstantCalibration calibration) {
            double referenceTemperature = UnitConverter.temperature(calibration.getReferenceTemperature());
            double referenceResistance = UnitConverter.resistance(calibration.getReferenceResistance());
            double beta = calibration.getBConstant();
            double t0 = referenceTemperature + ZERO_POINT;
            double a = (1 / t0) - (1 / beta) * Math.log(referenceResistance);
            double b = 1 / beta;
            double c = 0;
            return new NtcCalibration(a, b, c);
        }

        public NtcSensor build() {
            return new NtcSensor(delegate, name, calibration, filters);
        }

        @Override
        protected Builder self() {
            return this;
        }


        public Builder setFilters(List<Map<ValueFilterType, Number>> filters) {
            this.filters = filters;
            return self();
        }

    }

    @Data
    public static class NtcCalibration {
        final double a;
        final double b;
        final double c;
    }

    @Data
    private static class ValueCalibration {
        double r1;
        double t1;
        double r2;
        double t2;
        double r3;
        double t3;

        public static ValueCalibration valueOf(List<Map<String, String>> calibration) {
            if (calibration.size() != 3) {
                throw new IllegalArgumentException("expected 3 temperature point but found " + calibration.size());
            }
            ValueCalibration c = new ValueCalibration();
            AtomicInteger i = new AtomicInteger();
            calibration.forEach(m -> {
                Map.Entry<String, String> record = m.entrySet().stream().findFirst().orElseThrow();

                double resistance = UnitConverter.resistance(record.getKey());
                double temperature = UnitConverter.temperature(record.getValue());
                switch (i.get()) {
                    case 0:
                        c.r1 = resistance;
                        c.t1 = temperature + ZERO_POINT;
                        break;
                    case 1:
                        c.r2 = resistance;
                        c.t2 = temperature + ZERO_POINT;
                        break;
                    default:
                        c.r3 = resistance;
                        c.t3 = temperature + ZERO_POINT;
                        break;
                }
                i.getAndIncrement();
            });
            return c;
        }
    }
}
