package com.dfi.sbc2ha;

import com.dfi.sbc2ha.easing.*;

public class Easing {

    public static float ease(float time, float beginning, float change, float duration, EasingType type, EasingVariant variant) {

        switch (type) {
            default -> {
                return switch (variant) {
                    case INOUT -> Linear.easeInOut(time, beginning, change, duration);
                    case OUT -> Linear.easeOut(time, beginning, change, duration);
                    default -> Linear.easeIn(time, beginning, change, duration);
                };
            }
            case SINE -> {
                return switch (variant) {
                    case INOUT -> Sine.easeInOut(time, beginning, change, duration);
                    case OUT -> Sine.easeOut(time, beginning, change, duration);
                    default -> Sine.easeIn(time, beginning, change, duration);
                };
            }
            case QUAD -> {
                return switch (variant) {
                    case INOUT -> Quad.easeInOut(time, beginning, change, duration);
                    case OUT -> Quad.easeOut(time, beginning, change, duration);
                    default -> Quad.easeIn(time, beginning, change, duration);
                };
            }
            case CIRC -> {
                return switch (variant) {
                    case INOUT -> Circ.easeInOut(time, beginning, change, duration);
                    case OUT -> Circ.easeOut(time, beginning, change, duration);
                    default -> Circ.easeIn(time, beginning, change, duration);
                };
            }
            case CUBIC -> {
                return switch (variant) {
                    case INOUT -> Cubic.easeInOut(time, beginning, change, duration);
                    case OUT -> Cubic.easeOut(time, beginning, change, duration);
                    default -> Cubic.easeIn(time, beginning, change, duration);
                };
            }
            case QUART -> {
                return switch (variant) {
                    case INOUT -> Quart.easeInOut(time, beginning, change, duration);
                    case OUT -> Quart.easeOut(time, beginning, change, duration);
                    default -> Quart.easeIn(time, beginning, change, duration);
                };
            }
            case QUINT -> {
                return switch (variant) {
                    case INOUT -> Quint.easeInOut(time, beginning, change, duration);
                    case OUT -> Quint.easeOut(time, beginning, change, duration);
                    default -> Quint.easeIn(time, beginning, change, duration);
                };
            }
            case EXPO -> {
                return switch (variant) {
                    case INOUT -> Expo.easeInOut(time, beginning, change, duration);
                    case OUT -> Expo.easeOut(time, beginning, change, duration);
                    default -> Expo.easeIn(time, beginning, change, duration);
                };
            }
        }
    }
}
