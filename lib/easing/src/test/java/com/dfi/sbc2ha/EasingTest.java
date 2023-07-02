package com.dfi.sbc2ha;

import org.junit.Test;
import org.tinylog.Logger;

import java.util.List;
import java.util.function.UnaryOperator;

import static org.junit.Assert.assertTrue;

public class EasingTest {
    @Test
    public void easingCalculateReverse() {
        List<String> names = Easing.names();

        float THRESHOLD = 0.0300007f;

        for (var easingName : names) {
            Easing easing = Easing.valueOf(easingName);
            UnaryOperator<Float> operator = easing.getOperator();
            UnaryOperator<Float> revertOperator = easing.getRevert();

            for (var f = 0f; f <= 1; f += 0.01) {
                float val1 = operator.apply((float) f);
                if (revertOperator != null) {
                    float val2 = revertOperator.apply(val1);
                    float diff = Math.abs(f - val2);
                    if (diff > 0.002) {
                        Logger.info("easing: {} s={} x={} r={} diff={}", easingName, f, val1, val2, diff);
                    }
                    assertTrue("easing: " + easingName + " failed " + f + " != " + val2 + " diff: " + diff, diff < THRESHOLD);
                }
            }
            Logger.info("easing: {} OK", easingName);
        }
    }
}
