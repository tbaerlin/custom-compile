/*
 * IllegalQuoteFilterException.java
 *
 * Created on 06.07.2010 12:52:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author oflege
 */
public class QuoteFilterException extends MergerException {
    protected QuoteFilterException(String message) {
        super("Invalid filter: " + message);
    }

    @Override
    public String getCode() {
        return "filter.error";
    }
}
