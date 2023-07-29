package com.dfi.sbc2ha.config.sbc2ha.definition.filters;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import java.util.function.BinaryOperator;

import static java.lang.Float.NaN;

public enum ValueFilterType {
    /**
     * Adds a constant value to each sensor value.
     * - offset: 5
     */
    OFFSET(Float::sum),
    /**
     * Round sensor value to X digits after .
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - round: 2
     */

    ROUND((value, places) -> {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(Double.toString(value));
        bd = bd.setScale(places.intValue(), RoundingMode.HALF_UP);
        return (float) bd.doubleValue();
    }),
    /**
     * Multply sensor value by constant value.
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - multiply: 1.5
     */
    MULTIPLY((v, m) -> v * m),
    /**
     * Filter out value if it is equal to constant value
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - filter_out: 17
     */
    FILTER_OUT((aFloat, aFloat2) -> {
        if (Objects.equals(aFloat, aFloat2)) {
            throw new RuntimeException("filtered");
        }
        return aFloat;
    }),
    /**
     * Filter out value if it is greater than constant value
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - filter_out_greater: 17
     */
    FILTER_OUT_GREATER((aFloat, aFloat2) -> {
        if (aFloat > aFloat2) {
            return NaN;
            //throw new RuntimeException("filtered");
        }
        return aFloat;
    }),
    /**
     * Filter out value if it is lower than constant value
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - filter_out_lower: 17
     * <p>
     * <p>
     * <p>
     * }
     */
    FILTER_OUT_LOWER((aFloat, aFloat2) -> {
        if (aFloat < aFloat2) {
            return NaN;
            //throw new RuntimeException("filtered");
        }
        return aFloat;
    });

    private final BinaryOperator<Float> filter;

    ValueFilterType(BinaryOperator<Float> filter) {
        this.filter = filter;
    }

    public BinaryOperator<Float> getFilter() {
        return filter;
    }
}

