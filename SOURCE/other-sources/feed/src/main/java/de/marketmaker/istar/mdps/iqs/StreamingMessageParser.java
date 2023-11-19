/*
 * StreamingMessageParser.java
 *
 * Created on 18.10.13 08:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;

/**
 * Parses iqs messages and invokes callback methods on a {@link Handler} for all message parts.
 * @author oflege
 */
class StreamingMessageParser {

    interface Handler {
        /**
         * an unexpected message byte was found
         */
        void fail();

        /**
         * about to start parsing
         * @param msg to be parsed
         */
        void start(byte[] msg);

        void startEnvelope();

        /**
         * only called for bodies of envelopes, not for the body of the main messages, as the
         * latter may only contain envelopes and therefore {@link #startEnvelope()} is sufficient
         * to indicate that the body of the main message is processed.
         */
        void startBody();

        void end();

        /**
         * called for each header field of the main message, each header field of an envelope and
         * each body field of an envelope
         * @param fid field id
         * @param start position of the start of the field's value in <tt>msg</tt>
         * @param end position after the end of the field's value
         */
        void field(int fid, int start, int end);
    }

    private final Handler h;

    private byte[] msg;

    private int pos = 0;

    StreamingMessageParser(Handler h) {
        this.h = h;
    }

    void parse(ByteBuffer bb) {
        byte[] tmp = new byte[bb.remaining()];
        bb.get(tmp);
        parse(tmp);
    }

    void parse(byte[] msg) {
        this.pos = 0;
        this.msg = msg;

        if (!expect(Constants.STX)) {
            h.fail();
            return;
        }

        try {
            doParse(msg);
        } catch (ArrayIndexOutOfBoundsException e) {
            h.fail();
        }
    }

    private void doParse(byte[] msg) {
        h.start(msg);
        MSG: while (true) {
            switch (current()) {
                case Constants.RS:
                    if (!parseField()) {
                        return;
                    }
                    break;
                case Constants.GS:
                    if (!parseBody()) {
                        return;
                    }
                    break MSG;
                case Constants.ETX:
                    break MSG;
                default:
                    h.fail();
                    return;
            }
        }

        expect(Constants.ETX);
        h.end();
    }

    private boolean parseField() {
        int fid = 0;
        byte c = next();
        while (c >= '0' && c <= '9') {
            fid = fid * 10 + c - '0';
            c = next();
        }
        if (fid == 0) {
            h.fail();
            return false;
        }
        if (c != Constants.FS) {
            h.fail();
            return false;
        }
        c = next();
        int from = position();
        while (c >= 0x20 || (c != Constants.RS && c != Constants.GS && c != Constants.SO && c != Constants.ETX)) {
            c = next();
        }
        h.field(fid, from, position());
        return true;
    }

    private boolean parseBody() {
        skip();
        while (true) {
            switch (current()) {
                case Constants.SI:
                    if (!parseEnvelope()) {
                        return false;
                    }
                    break;
                case Constants.ETX:
                    return true;
                default:
                    return false;
            }
        }
    }

    private boolean parseEnvelope() {
        h.startEnvelope();
        boolean header = true;
        skip();
        while (true) {
            switch (current()) {
                case Constants.RS:
                    if (!parseField()) {
                        return false;
                    }
                    break;
                case Constants.GS:
                    if (header) {
                        skip();
                        h.startBody();
                        header = false;
                    }
                    else {
                        h.fail();
                        return false;
                    }
                    break;
                case Constants.SO:
                    skip();
                    return true;
                default:
                    h.fail();
                    return false;
            }
        }
    }

    private int position() {
        return this.pos;
    }

    private byte next() {
        return msg[++pos];
    }

    private byte current() {
        return msg[pos];
    }

    private void skip() {
        ++pos;
    }

    private boolean expect(byte b) {
        if (current() != b) {
            h.fail();
            return false;
        }
        skip();
        return true;
    }
}
