/*
 * IqsMessageParser.java
 *
 * Created on 23.09.13 15:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author oflege
 */
public class IqsMessageParser {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private IqsMessageProcessor processor;

    private final IqsRequest.Builder builder = new IqsRequest.Builder();

    private final StreamingMessageParser parser = new StreamingMessageParser(builder);

    private final StringWriter sw = new StringWriter();

    private final StreamingMessageParser dumper
            = new StreamingMessageParser(new IqsMessageDumper(new PrintWriter(sw)));

    public void setProcessor(IqsMessageProcessor processor) {
        this.processor = processor;
    }

    void process(IqsClients.Client conn, ByteBuffer bb) {
        int i = bb.position();
        int n = bb.limit();
        final byte[] array = bb.array();
        while (i < n) {
            if (array[i] != Constants.STX) {
                // ?? invalid garbage, todo: close connection?!
                i++;
                continue;
            }
            final int end = findEnd(array, i + 1, n);
            if (end < 0) {
                if (i == 0 && n == bb.capacity()) {
                    throw new IllegalStateException("too long");
                }
                bb.position(i);
                bb.compact();
                return;
            }
            final byte[] rawMessage = Arrays.copyOfRange(array, i, end + 1);

            this.parser.parse(rawMessage);
            final IqsRequest request = this.builder.build();

            try {
                this.processor.process(conn, rawMessage, request);
            } catch (Exception e) {
                this.sw.getBuffer().setLength(0);
                this.dumper.parse(rawMessage);
                this.logger.warn("<process> failed for " + this.sw.toString(), e);
            }
            i = end + 1;
        }
    }

    static int findEnd(byte[] array, int i, int n) {
        for (int k = i; k < n; k++) {
            if (array[k] == Constants.ETX) {
                return k;
            }
        }
        return -1;
    }
}
