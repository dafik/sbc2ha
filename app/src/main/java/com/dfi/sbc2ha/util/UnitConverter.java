package com.dfi.sbc2ha.util;

import java.util.HashMap;
import java.util.function.ToDoubleFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnitConverter {
    private static final HashMap<String, Double> METRIC_SUFFIXES = new HashMap<>();

    static {
        METRIC_SUFFIXES.put("E", 1e18);
        METRIC_SUFFIXES.put("P", 1e15);
        METRIC_SUFFIXES.put("T", 1e12);
        METRIC_SUFFIXES.put("G", 1e9);
        METRIC_SUFFIXES.put("M", 1e6);
        METRIC_SUFFIXES.put("k", 1e3);
        METRIC_SUFFIXES.put("da", 10.0);
        METRIC_SUFFIXES.put("d", 1e-1);
        METRIC_SUFFIXES.put("c", 1e-2);
        METRIC_SUFFIXES.put("m", 0.001);
        METRIC_SUFFIXES.put("µ", 1e-6);
        METRIC_SUFFIXES.put("u", 1e-6);
        METRIC_SUFFIXES.put("n", 1e-9);
        METRIC_SUFFIXES.put("p", 1e-12);
        METRIC_SUFFIXES.put("f", 1e-15);
        METRIC_SUFFIXES.put("a", 1e-18);
        METRIC_SUFFIXES.put("", 1.0);
    }

    public static ToDoubleFunction<String> floatWithUnit(String quantity, String regex_suffix, boolean optional_unit) {
        Pattern pattern = Pattern.compile("^([-+]?[0-9]*\\.?[0-9]*)\\s*(\\w*?)" + regex_suffix + "$", Pattern.UNICODE_CASE);

        return (String value) -> {
            if (optional_unit) {
                try {
                    return Double.parseDouble(value);
                } catch (NumberFormatException e) {
                    // Do nothing
                }
            }

            Matcher match = pattern.matcher(value);
            if (!match.find()) {
                throw new IllegalArgumentException("Expected " + quantity + " with unit, got " + value);
            }

            double mantissa = Double.parseDouble(match.group(1));
            String suffix = match.group(2);
            if (!METRIC_SUFFIXES.containsKey(suffix)) {
                throw new IllegalArgumentException("Invalid " + quantity + " suffix " + suffix);
            }

            double multiplier = METRIC_SUFFIXES.get(suffix);
            return mantissa * multiplier;
        };
    }

    public static double resistance(String value) {
        return floatWithUnit("resistance", "(Ω|Ω|ohm|Ohm|OHM)?", false).applyAsDouble(value);
    }

    public static double temperature(String value) {
        return floatWithUnit("temperature", "(°C|° C|°|C)?", false).applyAsDouble(value);
    }

    public static double voltage(String value) {
        return floatWithUnit("voltage", "(v|V|volt|Volts)?",false).applyAsDouble(value);
    }


}

