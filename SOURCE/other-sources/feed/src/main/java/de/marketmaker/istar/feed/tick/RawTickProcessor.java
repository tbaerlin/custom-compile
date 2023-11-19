/*
 * RawTickProcessor.java
 *
 * Created on 10.04.13 10:35
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

/**
 * An item that processes RawTick objects.
 * @author oflege
 */
public interface RawTickProcessor<T> {
    /**
     * Process the given tick, a Processer must not store the tick, as it might be changed
     * afterwards.
     * @param rt to be processed
     * @return false if processing should be aborted, true if processing should continue with
     * the next tick.
     */
    boolean process(RawTick rt);

    T getResult();
}
