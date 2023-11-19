/*
 * LocalDateCache.java
 *
 * Created on 6/23/14 3:35 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import net.jcip.annotations.NotThreadSafe;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Willenbrock
 */
@NotThreadSafe
public class LocalDateCache {
    private final static DateTimeFormatter DTF_DATE = DateTimeFormat.forPattern("dd.MM.yyyy");

    private final Map<String, LocalDate> localDateCache = new HashMap<>();

    public LocalDate getDate(String date) {
        if (date == null || date.startsWith("31.12.9999")) {
            return null;
        }
        if (this.localDateCache.containsKey(date)) {
            return this.localDateCache.get(date);
        }
        final LocalDate result = DTF_DATE.parseDateTime(date).toLocalDate();
        this.localDateCache.put(date, result);
        return result;
    }
}
