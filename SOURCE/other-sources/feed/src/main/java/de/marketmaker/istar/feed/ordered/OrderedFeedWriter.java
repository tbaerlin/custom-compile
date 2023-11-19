/*
 * OrderedFeedWriter.java
 *
 * Created on 23.10.12 09:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.IOException;

import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.connect.BufferWriter;

/**
 * Forwards ordered feed data to a delegate {@link BufferWriter}.
 * @author oflege
 */
public class OrderedFeedWriter extends OrderedFeedBuilder {
    private BufferWriter writer;

    public OrderedFeedWriter() {
    }

    public OrderedFeedWriter(int flags) {
        super(flags);
    }

    public void setWriter(BufferWriter writer) {
        this.writer = writer;
    }

    @Override
    public void process(FeedData data, ParsedRecord pr) {
        doProcess(data, pr);
        try {
            this.writer.write(this.bb);
        } catch (IOException e) {
            this.logger.error("<process> failed", e);
        }
    }
}
