package com.menuonline.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class DateUtils {

    public static LocalDateTime secondsToObject(Long seconds) {
        if (seconds == null)
            return null;
        return LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneOffset.UTC);
    }
}
