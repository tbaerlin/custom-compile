/*
 * Mementos.java
 *
 * Created on 23.01.15 14:57
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

/**
 * Utility class for int and long values that are tracked and for which the difference between
 * the new and the old value needs to be computed on each update
 * @author oflege
 */
public final class Mementos {
    private Mementos() {
    }

    public static class Int {
        private int value;

        public Int(int initialValue) {
            this.value = initialValue;
        }

        public Int() {
        }

        public int diffAndSet(int update) {
            int result = update - this.value;
            this.value = update;
            return result;
        }
    }

    public static class Long {
        private long value;

        public Long(long initialValue) {
            this.value = initialValue;
        }

        public Long() {
        }

        public long diffAndSet(long update) {
            long result = update - this.value;
            this.value = update;
            return result;
        }
    }
}
