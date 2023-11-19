/*
 * Alternator.java
 *
 * Created on 04.06.2008 12:19:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Ulrich Maurer
 */
public class Alternator <T> {
    private final T[] values;
    private int index = 0;
    private boolean auto;


    public Alternator(T[] values) {
        this(values, true);
    }

    public Alternator(T[] values, boolean auto) {
        this.values = values;
        this.auto = auto;
    }

    /**
     * Return the current value without incrementing the index. Subsequent calls to getCurrent() return the same value.
     * @return The current value.
     */
    public T getCurrent() {
        return this.values[this.index];
    }

    /**
     * Return the current value and increment the index. Subsequent calls to getNext() return different values.
     * @return The current value.
     */
    public T getNext() {
        final T current = getCurrent();
        shift();
        return current;
    }

    /**
     * Increment the index.
     */
    public void shift() {
        this.index = (this.index + 1) % this.values.length;
    }


    public String toString() {
        final T current = getCurrent();
        if (this.auto) {
            shift();
        }
        if (current == null) {
            return null;
        }
        return current.toString();
    }
}
