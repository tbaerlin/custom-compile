/*
 * OrderedFileRecordReader.java
 *
 * Created on 20.03.13 11:39
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.io.File;
import java.math.BigDecimal;
import java.util.Map;
import java.util.Scanner;
import java.util.SortedMap;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedOperationParameter;
import org.springframework.jmx.export.annotation.ManagedOperationParameters;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.ThroughputLimiter;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.DateTimeProvider;
import de.marketmaker.istar.feed.DateTimeProviderStatic;
import de.marketmaker.istar.feed.connect.BufferWriter;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

/**
 * Reads records from a file and forwards them to a BufferWriter, usually a feed distributor.
 * Each line in the file defines a record, format is
 * <pre>
 *     M;KEY[;FID(=VALUE)?]*
 * </pre>
 * <dl>
 *     <dt>M</dt>
 *     <dd>message type according to {@link VwdFeedConstants}</dd>
 *     <dt>KEY</dt>
 *     <dd>vendorkey (with type prefix, required for static messages) or vwdcode</dd>
 *     <dt>FID</dt>
 *     <dd>id from the VwdFieldDescription</dd>
 *     <dt>VALUE</dt>
 *     <dd>a value that conforms to the type of FID: a number for int/date, HH:mm:ss(.SSS)? for time,
 *     a value that can be used as BigDecimal string constructor arg for price, and arbitrary
 *     values for strings (not enclosed in quotes);<p>
 *     For message type MESSAGE_TYPE_DELETE_FIELDS, no value is required as it will be ignored anyway
 *         </dd>
 * </dl>
 * @author oflege
 */
@ManagedResource
public class OrderedFileRecordReader {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private BufferWriter writer;

    public void setWriter(BufferWriter writer) {
        this.writer = writer;
    }

    @ManagedOperation(description = "read records from file, add them to feed")
    @ManagedOperationParameters({
            @ManagedOperationParameter(name = "filename", description = "file with records"),
    })
    public synchronized String read(String filename) {
        File f = new File(filename);
        if (!f.canRead()) {
            return "no such file " + f.getAbsolutePath();
        }

        TimeTaker tt = new TimeTaker();
        this.logger.info("<read> " + f.getAbsolutePath() + "...");
        final ThroughputLimiter limiter = new ThroughputLimiter(10000);

        LineParser parser = new LineParser();

        try (Scanner sc = new Scanner(f, "UTF-8")) {
            while (sc.hasNextLine()) {
                final String line = sc.nextLine();
                if (parser.parse(line)) {
                    this.writer.write(parser.bb);
                }
                limiter.ackAction();
            }
        } catch (Exception e) {
            this.logger.warn("<read> failed in line " + parser.lineNo + " for " +f.getAbsolutePath(), e);
            return e.toString();
        }
        this.logger.info("<read> " + f.getAbsolutePath() + " finished, took " + tt);
        return "ok";
    }

    private static class LineParser extends FieldDataBuilder {
        private final Logger logger = LoggerFactory.getLogger(getClass());

        private static final byte[] EMPTY_STRING = new byte[0];

        private final DateTimeProvider dtp = new DateTimeProviderStatic(new DateTime());

        private final SortedMap<VwdFieldDescription.Field, Object> fields
                = new TreeMap<>(VwdFieldOrder.BY_ORDER);

        private String line;

        private int k;

        private int lineNo = 0;

        private LineParser() {
            super(8192);
        }

        boolean parse(String line) {
            lineNo++;
            if (!StringUtils.hasText(line) || line.startsWith("#") || line.length() < 5) {
                return false;
            }
            reset();
            this.bb.position(2);

            this.line = line;
            this.k = 0;

            final char c = next();
            if (c < 'A' || c > 'Z' || !nextIf(';')) return fail("illegal message type '" + c + "'");
            byte msgType = (byte) c;

            final byte[] bytes = parseString();
            final ByteString key = new ByteString(bytes, 0, bytes.length);

            final boolean withTypePrefix = VendorkeyVwd.isKeyWithTypePrefix(key);
            if (msgType == VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP && !withTypePrefix) {
                return fail("static recap required typed vendorkey");
            }

            final VendorkeyVwd vkey = withTypePrefix
                    ? VendorkeyVwd.getInstance(key) : VendorkeyVwd.getInstance(key, 1);
            if (vkey == VendorkeyVwd.ERROR) {
                return fail("invalid vendorkey '" + vkey + "'");
            }

            final ByteString vwdcode = vkey.toVwdcode();
            bb.put((byte) vwdcode.hashCode());
            vwdcode.writeTo(bb, ByteString.LENGTH_ENCODING_BYTE);

            bb.put((byte) 0);
            bb.put(msgType);
            bb.putShort((short) vkey.getType());
            bb.putShort((short) 0); // flags

            bb.putInt(this.dtp.current().feedTimestamp);

            final boolean result = parseFields(msgType);

            bb.flip();
            bb.putShort(0, (short) bb.remaining());
            return result;
        }

        private boolean fail(String s) {
            this.logger.warn("<fail> line " + lineNo + ": " + s + ", " + line);
            return false;
        }

        private boolean parseFields(byte msgType) {
            this.fields.clear();
            while (nextIf(';')) {
                final int fid = nextNum();
                final VwdFieldDescription.Field field = VwdFieldDescription.getField(fid);
                if (field == null) {
                    return fail("invalid field " + fid);
                }
                boolean withValue = nextIf('=');
                if (!withValue && msgType != VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS) {
                    return fail("no value for field " + fid);
                }
                switch (field.type()) {
                    case DATE:
                    case USHORT:
                    case UINT:
                        this.fields.put(field, withValue ? nextNum() : 0);
                        break;
                    case TIME:
                        this.fields.put(field, withValue ? parseTime() : 0);
                        break;
                    case PRICE:
                        this.fields.put(field, withValue ? parsePrice() : 0L);
                        break;
                    case STRING:
                        this.fields.put(field, withValue ? parseString() : EMPTY_STRING);
                        break;
                }
            }
            if (fields.isEmpty() && !VwdFeedConstants.isDelete(msgType)) {
                return fail("no fields");
            }
            addFieldsToBuffer();
            return true;
        }

        private void addFieldsToBuffer() {
            for (Map.Entry<VwdFieldDescription.Field, Object> entry : fields.entrySet()) {
                final VwdFieldDescription.Field field = entry.getKey();
                final int order = VwdFieldOrder.getOrder(field.id());
                switch (field.type()) {
                    case DATE:
                    case USHORT:
                    case UINT:
                        putIntFid(order);
                        putInt((Integer) entry.getValue());
                        break;
                    case TIME:
                        putTimeFid(order);
                        putInt((Integer) entry.getValue());
                        break;
                    case PRICE:
                        putPriceFid(order);
                        putPrice((Long) entry.getValue());
                        break;
                    case STRING:
                        putStringFid(order);
                        putString((byte[]) entry.getValue());
                        break;
                }
            }
        }

        private byte[] parseString() {
            StringBuilder sb = new StringBuilder();
            while (hasNext() && !peek(';')) {
                char c = next();
                sb.append(c != '\\' ? c : next());
            }
            return sb.toString().getBytes(OrderedSnapRecord.DEFAULT_CHARSET);
        }

        private long parsePrice() {
            final int from = k;
            while (hasNext() && !peek(';')) {
                next();
            }
            return MdpsFeedUtils.encodePrice(new BigDecimal(line.substring(from, k)));
        }

        private int parseTime() {
            int hh = nextNum();
            int mm = consume(':').nextNum();
            int ss = consume(':').nextNum();
            int ms = nextIf('.') ? nextNum() : 0;
            return MdpsFeedUtils.encodeTime(hh * 3600 + mm * 60 + ss, ms);
        }

        private LineParser consume(char c) {
            if (!hasNext()) {
                throw new IllegalStateException("eol");
            }
            if (!peek(c)) {
                throw new IllegalStateException("'" + peek() + "' <> '" + c + "'");
            }
            this.k++;
            return this;
        }

        private int nextNum() {
            final boolean neg = nextIf('-');
            int n = 0;
            while (hasNext() && Character.isDigit(peek())) {
                n = (n * 10) + (next() - '0');
            }
            return neg ? -n : n;
        }

        private char next() {
            return this.line.charAt(this.k++);
        }

        private boolean nextIf(char c) {
            if (hasNext() && peek(c)) {
                next();
                return true;
            }
            return false;
        }

        private boolean peek(char c) {
            return c == peek();
        }

        private char peek() {
            return this.line.charAt(this.k);
        }

        private boolean hasNext() {
            return k < this.line.length();
        }
    }

    public static void main(String[] args) {
        OrderedFileRecordReader r = new OrderedFileRecordReader();
        r.read("/Users/oflege/tmp/invalid_rt.txt");
    }
}
