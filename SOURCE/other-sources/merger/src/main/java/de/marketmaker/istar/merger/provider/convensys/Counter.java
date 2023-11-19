/*
 * Counter.java
 *
 * Created on 07.03.2008 15:59:36
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.istar.merger.provider.convensys;

/**
 * A Counter object is initialized with
 * a start value and incremented each time, its value is requested with getValue().
 * A Counter can be created by a WebMacro using the ContextTool as follows:
 * <pre>#set $counter = $contextTool.getCounter(0)</pre>
 * <p>This creates a new Counter object with an increment of one. If a different increment should be used,
 * a second parameter with the increment can be provided:</p>
 * <pre>#set $counter = $contextTool.getCounter(0, 2)</pre>
 *
 *
 * @author Ulrich Maurer
 */
public class Counter {
    private long value;
    private final long increment;

    /**
     * Initialize the Counter with a start value and specify an increment value.
     * @param start The first time getValue() is called, this value is returned.
     * @param increment Each time getValue() is called, the value is incremented by increment.
     */
    public Counter(long start, long increment) {
        this.value = start;
        this.increment = increment;
    }

    /**
     * Initialize the Counter with a start value. The increment is set to one.
     * @param start The first time getValue() is called, this value is returned.
     */
    public Counter(long start) {
        this(start, 1);
    }

    /**
     * Initialize the Counter with zero. The increment is set to one.
     */
    public Counter() {
        this(0, 1);
    }

    /**
     * Returns the current value and increment it afterwards.
     * @return The current value.
     */
    public long getValue() {
        final long result = this.value;
        this.value += this.increment;
        return result;
    }


    public int getIntValue() {
        return (int)getValue();
    }

    /**
     * Returns the current value without incrementation.
     * @return The current value.
     */
    public long getCurrentValue() {
        return this.value;
    }

    /**
     * Increment the counter by the predefined increment value and return the new counter value.
     * @return The new counter value.
     */
    public long inc() {
        this.value += this.increment;
        return this.value;
    }

    /**
     * Increment the counter by the specified increment value and return the new counter value.
     * @param increment The value by which the counter is incremented.
     * @return The new counter value.
     */
    public long inc(long increment) {
        this.value += increment;
        return this.value;
    }

    /**
     * Set the current value. The next time, getValue() is called, this value is returned.
     * @param value The new current value.
     */
    public void setValue(long value) {
        this.value = value;
    }


    public String toString() {
        return String.valueOf(getValue());
    }


    public boolean lessThan(long value) {
        return this.value < value;
    }
}