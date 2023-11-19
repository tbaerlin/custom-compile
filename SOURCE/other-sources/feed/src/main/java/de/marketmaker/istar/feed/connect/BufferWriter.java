/*
 * PortWriter.java
 *
 * Created on 13.12.2004 10:52:08
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.connect;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Receives a ByteBuffer with the intention to write the contents somewhere.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface BufferWriter {
    /**
     * Writes the contents of the given ByteBuffer to some destination. Clients must not assume
     * that this method will be called by just one thread and therefore have to ensure
     * thread-safety. Clients may choose not to process all remaining bytes in bb, so that the
     * caller should compact the buffer if it has any remaining bytes after this method returns.
     * @param bb contains data to be written
     * @throws java.io.IOException if writing fails
     */
    void write(ByteBuffer bb) throws IOException;
}

