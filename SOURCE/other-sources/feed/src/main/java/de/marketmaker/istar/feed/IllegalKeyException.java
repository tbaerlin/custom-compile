/*
 * IllegalFieldException.java
 *
 * Created on 26.01.2006 18:05:25
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IllegalKeyException extends RuntimeException {

    public IllegalKeyException(ByteString key) {
        super("Illegal key: '" + key + "'");
    }
}
