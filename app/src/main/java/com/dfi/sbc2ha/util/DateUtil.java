package com.dfi.sbc2ha.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DateUtil {
    public static LocalDateTime epochMillisToLocalDateTime(long epochMillis) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochMilli(epochMillis),
                ZoneId.systemDefault());
    }

    public static LocalDateTime epochNanosToLocalDateTime(long epochNanos) {
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(0L, epochNanos),
                ZoneId.systemDefault());
    }


    public static Instant epochMillisToInstant(long epochMillis) {
        return Instant.ofEpochMilli(epochMillis);

    }

    public static Instant epochNanosToInstant(long epochNanos) {
        return Instant.ofEpochSecond(0L, epochNanos);
    }

}
