/*
 * WMData.java
 *
 * Created on 31.10.2011 15:55:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Collection;

/**
 * @author tkiesgen
 */
public interface WMData {

    public static enum WMFieldType {
        STRING, DECIMAL, DATE, SEQUENCE, INTERVAL;

        public boolean isString() {
            return this == STRING;
        }

        public boolean isDecimal() {
            return this == DECIMAL;
        }

        public boolean isDate() {
            return this == DATE;
        }

        public boolean isSequence() {
            return this == SEQUENCE;
        }

        public boolean isInterval() {
            return this == INTERVAL;
        }
    }

    public interface Field {
        String getName();

        String getKey();

        String getTextinfo();

        WMFieldType getType();

        /**
         * Returns the value that corrsponds to the type returned by {@link #getType()}
         * @return
         */
        Object getValue();
    }

    long getInstrumentid();

    Collection<Field> getFields();

    Field getField(String name);
}
