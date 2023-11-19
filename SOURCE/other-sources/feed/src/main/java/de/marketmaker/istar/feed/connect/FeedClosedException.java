/*
 * FeedClosedException.java
 *
 * Created on 13.12.2004 10:56:10
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;

/**
 * exception send by {@link de.marketmaker.istar.feed.connect.PortReader} in case of lost socket.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id: FeedClosedException.java,v 1.1 2004/12/13 18:03:41 tkiesgen Exp $
 */
public class FeedClosedException extends IOException {
    public FeedClosedException(String s) {
        super(s);
    }
}

