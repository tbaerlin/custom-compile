/*
 * IllegalRequestException.java
 *
 * Created on 01.12.2006 10:20:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import org.apache.lucene.queryParser.ParseException;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsQueryException extends MergerException {
    public NewsQueryException(ParseException pe) {
        super(pe.getMessage(), pe);
    }

    public String getCode() {
        return "news.query.invalid";
    }
}