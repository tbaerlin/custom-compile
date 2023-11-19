/*
 * MdpsParserErrorHandler.java
 *
 * Created on 30.11.2006 13:33:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.jmx.export.annotation.ManagedOperation;

import de.marketmaker.istar.feed.FeedRecord;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.ParserErrorHandlerImpl;

/**
 * Dumps mdps feed records into files so that they can be parsed and debugged.
 * The file consists of consecutive mdps recors.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class MdpsParserErrorHandler extends ParserErrorHandlerImpl {
    private int preRecordOffset = 0;

    public void setPreRecordOffset(int preRecordOffset) {
        this.preRecordOffset = preRecordOffset;
    }

    public void handle(FeedRecord fr, ParsedRecord pr, Throwable t) {
        if (fr != null) {
            append(fr.getData(), fr.getOffset(), fr.getLength());
        }
        handle(t);
    }

    @ManagedOperation
    public void flush() {
        super.flush(); // do not inline, spring's jmx support needs annotated method in this class!
    }
}
