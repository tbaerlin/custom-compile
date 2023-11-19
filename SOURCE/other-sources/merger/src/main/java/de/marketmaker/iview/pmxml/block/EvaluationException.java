/*
 * UnknownSymbolException.java
 *
 * Created on 23.08.2006 17:41:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.pmxml.block;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EvaluationException extends MergerException {
    public EvaluationException(String message) {
        super(message);
    }

    public EvaluationException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getCode() {
        return "evaluation.failed";
    }
}