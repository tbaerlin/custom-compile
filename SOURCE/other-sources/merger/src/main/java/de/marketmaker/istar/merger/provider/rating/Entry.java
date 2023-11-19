/*
 * Rating.java
 *
 * Created on 04.05.12 15:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.rating;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author zzhao
 */
public class Entry {

    public static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private static final Map<String, Action> actionCache = new HashMap<>(20);

    private static final Map<String, RegulatoryId> regulatoryIdCache = new HashMap<>(5);

    public static Action getAction(String val) {
        if (!actionCache.containsKey(val)) {
            actionCache.put(val, new Action(val));
        }

        return actionCache.get(val);
    }

    public static RegulatoryId getRegulatoryId(String val) {
        if (!regulatoryIdCache.containsKey(val)) {
            regulatoryIdCache.put(val, new RegulatoryId(val));
        }

        return regulatoryIdCache.get(val);
    }

    public static LocalDate getDate(String val) {
        return DTF.parseLocalDate(val);
    }

    public static abstract class HasValue implements Serializable {
        private final String value;

        protected HasValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return this.value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HasValue hasValue = (HasValue) o;

            if (!value.equals(hasValue.value)) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }
    }

    public static class RegulatoryId extends HasValue implements Comparable<RegulatoryId> {

        protected RegulatoryId(String value) {
            super(value);
        }

        @Override
        public int compareTo(RegulatoryId o) {
            return getValue().compareTo(o.getValue());
        }
    }

    public static class Action extends HasValue implements Comparable<Action> {
        private Action(String value) {
            super(value);
        }

        @Override
        public int compareTo(Action o) {
            return getValue().compareTo(o.getValue());
        }
    }
}
