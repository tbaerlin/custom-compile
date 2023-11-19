/*
 * RatiosEncoder.java
 *
 * Created on 19.10.2005 13:49:55
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.ratios.frontend.RatioEnumSet;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteBufferUtils;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosEncoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RatiosEncoder.class);

    // public for testing, size of the header: type (int) + iid (long) + qid (long)
    public final static int DATA_OFFSET = Integer.BYTES + 2 * Long.BYTES;

    private final ByteBuffer buffer = ByteBuffer.allocate(4096);

    // only warn once
    private final Map<InstrumentTypeEnum, Set<Short>> warningMap = new EnumMap<>(InstrumentTypeEnum.class);

    private InstrumentTypeEnum type;

    public void reset(InstrumentTypeEnum type, long iid, long qid) {
        this.type = type;
        this.buffer.clear();
        this.buffer.putInt(type.getId());
        this.buffer.putLong(iid);
        this.buffer.putLong(qid);
    }

    public void add(RatioFieldDescription.Field field, long value) {
        add(field.id(), value);
    }

    public void add(RatioFieldDescription.Field field, BitSet value) {
        add(field.id(), value);
    }

    public void add(int fieldId, long value) {
        putFieldId((short) fieldId);
        this.buffer.putLong(value);
    }

    public void add(int fieldId, BitSet value) {
        putFieldId((short) fieldId);
        RatioEnumSet.writeTo(value, this.buffer);
    }

    public void add(RatioFieldDescription.Field f, int value) {
        add(f.id(), value);
    }

    public void add(int fieldId, int value) {
        putFieldId((short) fieldId);
        this.buffer.putInt(value);
    }

    public void add(RatioFieldDescription.Field f, boolean value) {
        add(f.id(), value);
    }

    public void add(int fieldId, boolean value) {
        putFieldId((short) fieldId);
        this.buffer.put((byte) (value ? 1 : 0));
    }

    public void add(RatioFieldDescription.Field f, String value) {
        add(f.id(), value);
    }

    public void add(int fieldId, String value) {
        putFieldId((short) fieldId);
        ByteBufferUtils.putStringShortEncodedLength(this.buffer, value);
    }

    public void add(int fieldId, int localeIndex, String value) {
        putFieldId((short) fieldId);
        this.buffer.put((byte) localeIndex);
        ByteBufferUtils.putStringShortEncodedLength(this.buffer, value);
    }

    public boolean hasData() {
        return this.buffer.position() > DATA_OFFSET;
    }

    public byte[] getData() {
        final byte[] data = new byte[this.buffer.position()];
        this.buffer.flip();
        this.buffer.get(data);
        return data;
    }

    private void putFieldId(short fieldId) {
        this.buffer.putShort(fieldId);
        checkForStaticField(fieldId);
    }

    protected void checkForStaticField(short fieldId) {
        final RatioFieldDescription.Field field = RatioFieldDescription.getFieldById(fieldId);
        if (null != field && field.isStatic()) {
            if (!this.warningMap.containsKey(this.type)) {
                this.warningMap.put(this.type, new HashSet<>());
            }
            final Set<Short> set = this.warningMap.get(this.type);
            if (!set.contains(fieldId)) {
                set.add(fieldId);
                LOGGER.warn("<putFieldId> set static field {} type {} in non-static context", field, this.type);
            }
        }
    }

}

class StaticRatiosEncoder extends RatiosEncoder {
    @Override
    protected void checkForStaticField(short fieldId) {
        // empty, static fields are ok
    }
}
