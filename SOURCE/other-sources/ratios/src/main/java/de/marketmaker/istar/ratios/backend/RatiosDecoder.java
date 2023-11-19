/*
 * RatiosDecoder.java
 *
 * Created on 19.10.2005 14:02:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.ratios.frontend.RatioEnumSet;
import java.nio.ByteBuffer;

import de.marketmaker.istar.common.util.ByteBufferUtils;
import de.marketmaker.istar.common.util.DateFormatter;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioEnumSetFactory;
import java.util.BitSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosDecoder {

    private final static Logger log = LoggerFactory.getLogger(RatiosDecoder.class);

    public static String decode(byte[] data) {
        final ByteBuffer buffer = ByteBuffer.wrap(data);

        final StringBuilder stb = new StringBuilder(200);
        stb.append(InstrumentTypeEnum.valueOf(buffer.getInt()).name());
        stb.append(", ").append(buffer.getLong()).append(".iid");
        stb.append(", ").append(buffer.getLong()).append(".qid");

        while (buffer.hasRemaining()) {
            final int fieldid = buffer.getShort();
            final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(fieldid);

            if (field == null) {
                log.error("<decode> unknown fieldId {}", fieldid);
                stb.append(", ").append("unknown fieldId: ").append(fieldid);
                return stb.toString();
            }

            stb.append(", ").append(field.name()).append("/").append(fieldid).append(":");

            switch (field.type()) {
                case DECIMAL:
                    final long d = buffer.getLong();
                    stb.append(d == Long.MIN_VALUE ? "n/a" : PriceCoder.decodeAsDouble(d));
                    break;
                case NUMBER:
                case TIMESTAMP:
                    final long n = buffer.getLong();
                    stb.append(n == Long.MIN_VALUE ? "n/a" : n);
                    break;
                case ENUMSET:
                    final BitSet bitSet = RatioEnumSet.read(buffer);
                    stb.append(bitSet.isEmpty() ? "n/a" : RatioEnumSetFactory.fromBits(fieldid, bitSet));
                    break;
                case DATE:
                    stb.append(DateFormatter.formatYyyymmdd(buffer.getInt()));
                    break;
                case TIME:
                    stb.append(TimeFormatter.formatHHMMSS(buffer.getInt()));
                    break;
                case STRING:
                    stb.append(ByteBufferUtils.getStringShortEncodedLength(buffer));
                    break;
                case BOOLEAN:
                    stb.append(buffer.get() > 0);
                    break;
            }
        }

        return stb.toString();
    }
}
