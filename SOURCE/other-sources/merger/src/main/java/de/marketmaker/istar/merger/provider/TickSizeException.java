/*
 * TickSizeException.java
 *
 * Created on 21.10.14 11:05
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author oflege
 */
public class TickSizeException extends MergerException {
    public TickSizeException(int threshold, int actual) {
        super("Tick size limit (" + threshold + ") exceeded: " + actual + ", consider splitting the request");
    }

    @Override
    public String getCode() {
        return "max.ticksize.exceeded";
    }
}
