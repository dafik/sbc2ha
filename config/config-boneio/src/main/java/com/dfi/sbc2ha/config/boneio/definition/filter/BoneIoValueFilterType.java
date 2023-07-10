package com.dfi.sbc2ha.config.boneio.definition.filter;

public enum BoneIoValueFilterType {
    /**
     * Adds a constant value to each sensor value.
     * - offset: 5
     */
    OFFSET,
    /**
     * Round sensor value to X digits after .
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - round: 2
     */

    ROUND,
    /**
     * Multply sensor value by constant value.
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - multiply: 1.5
     */
    MULTIPLY,
    /**
     * Filter out value if it is equal to constant value
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - filter_out: 17
     */
    FILTER_OUT,
    /**
     * Filter out value if it is greater than constant value
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - filter_out_greater: 17
     */
    FILTER_OUT_GREATER,
    /**
     * Filter out value if it is lower than constant value
     * <p>
     * - platform: dallas
     * ...
     * filters:
     * - filter_out_lower: 17
     * <p>
     * <p>
     * }
     */
    FILTER_OUT_LOWER;
}

