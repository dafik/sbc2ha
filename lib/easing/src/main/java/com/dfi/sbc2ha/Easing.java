package com.dfi.sbc2ha;

import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public enum Easing {

    linerIn(f -> f,
            f -> f),
    linerOut(f -> f,
            f -> f),
    easeInQuad(f -> f * f,
            f -> (float) Math.sqrt(f)),
    easeOutQuad(f -> 1f - (1f - f) * (1f - f),
            f -> f == 0 ? 0 : (float) (1d - Math.sqrt(1d - f))),
    easeInCubic(f -> f * f * f,
            f -> (float) Math.pow(f, 1d / 3)),
    easeOutCubic(f -> (float) (1d - Math.pow(1d - f, 3)),
            f -> f == 0 ? 0 : (float) (1d - Math.pow(1 - f, 1d / 3))),
    easeInQuart(f -> f * f * f * f,
            f -> (float) Math.pow(f, 1d / 4)),
    easeOutQuart(f -> (float) (1d - Math.pow(1d - f, 4)),
            f -> f == 0 ? 0 : (float) (1d - Math.pow(1d - f, 1d / 4))),
    easeInQuint(f -> f * f * f * f * f,
            f -> (float) Math.pow(f, 1d / 5)),
    easeOutQuint(f -> (float) (1d - Math.pow(1d - f, 5)),
            f -> f == 0 ? 0 : (float) (1d - Math.pow(1d - f, 1d / 5))),
    easeInExpo(f -> f == 0 ? 0 : (float) Math.pow(2, 10 * f - 10),
            null),
    easeOutExpo(f -> f == 1 ? 1 : (float) (1d - Math.pow(2, -10 * f)),
            null),
    easeInCirc(f -> (float) (1d - Math.sqrt(1d - Math.pow(f, 2))),
            null),
    easeOutCirc(f -> (float) Math.sqrt(1d - Math.pow(f - 1, 2)),
            null);

    private final UnaryOperator<Float> operator;
    private final UnaryOperator<Float> revert;

    Easing(UnaryOperator<Float> operator, UnaryOperator<Float> revert) {

        this.operator = operator;
        this.revert = revert;
    }

    public static List<String> names() {
        return Stream.of(Easing.values()).map(Easing::name).collect(Collectors.toList());
    }


    public UnaryOperator<Float> getOperator() {
        return operator;
    }

    public UnaryOperator<Float> getRevert() {
        return revert;
    }
}
