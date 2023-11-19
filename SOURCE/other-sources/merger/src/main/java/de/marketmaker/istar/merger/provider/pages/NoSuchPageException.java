/*
 * NoSuchPageException.java
 *
 * Created on 25.02.2008 12:53:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import de.marketmaker.istar.merger.MergerException;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NoSuchPageException extends MergerException {
    public NoSuchPageException(String message, Object... arguments) {
        super(message, arguments);
    }

    public String getCode() {
        return "page.notfound";
    }
}
