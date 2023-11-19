/*
 * ParserErrorHandler.java
 *
 * Created on 30.11.2006 13:06:49
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.nio.ByteBuffer;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ParserErrorHandler {
    /**
     * called when parsing fr raised t; the state of all fields parsed prior to the one
     * that caused the error is available in pr.
     */
    void handle(FeedRecord fr, ParsedRecord pr, Throwable t);

    /**
     * called when parsing the message in buffer raised t
     * @param buffer contains a complete feed message
     * @param t raised when buffer was processed
     */
    void handle(ByteBuffer buffer, Throwable t);
}
