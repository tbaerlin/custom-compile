/*
 * MdpsKeyConverter.java
 *
 * Created on 31.08.2006 09:17:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.nio.ByteBuffer;

import net.jcip.annotations.ThreadSafe;

import de.marketmaker.istar.common.util.ByteString;

import static de.marketmaker.istar.feed.mdps.MdpsMessageConstants.*;

/**
 * Utility to convert mdps keys (/[DE])?(.+),\w{1,2} to a vwdcode, i.e., $2
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ThreadSafe
public class MdpsKeyConverter {

    /**
     * @param mapping result from calling {@link #getMapping(ByteString}
     * @return true iff the mapping resulted in an unknown xfeed type
     */
    public static boolean isUnknownXfeedType(int mapping) {
        return mapping == 0;
    }

    private final boolean processDelayedRecords;

    private final boolean ignoreUnknownTypes;

    public MdpsKeyConverter(boolean processDelayedRecords) {
        this(processDelayedRecords, true);
    }

    public MdpsKeyConverter(boolean processDelayedRecords, boolean ignoreUnknownTypes) {
        this.processDelayedRecords = processDelayedRecords;
        this.ignoreUnknownTypes = ignoreUnknownTypes;
    }

    private boolean isDelayedKey(ByteString mdpsKey) {
        if (mdpsKey.byteAt(0) != MDPS_DELAYED_KEY_PREFIX1) {
            return false;
        }
        final byte b = mdpsKey.byteAt(1);
        return (b == MDPS_DELAYED_KEY_PREFIX2 || b == MDPS_EOD_KEY_PREFIX2);
    }

    private boolean isDelayedKey(ByteBuffer bb) {
        bb.mark();
        if (bb.get() != MDPS_DELAYED_KEY_PREFIX1) {
            bb.reset();
            return false;
        }
        final byte b = bb.get();
        boolean delayed = b == MDPS_DELAYED_KEY_PREFIX2 || b == MDPS_EOD_KEY_PREFIX2;

        // We do not need the prefix if it is delayed
        if (!delayed) {
            bb.reset();
        }
        return delayed;
    }

    private int getSuffixLength(ByteString mdpsKey) {
        return (mdpsKey.byteAt(mdpsKey.length() - 3) == ',') ? 3 : 2;
    }

    private int getSuffixLength(ByteBuffer bb, final int mdpsKeyLength) {
        return (bb.get(bb.position() + mdpsKeyLength - 3) == ',' ? 3 : 2);
    }

    /**
     * @param mdpsKey to be converted
     * @return vendorkey
     */
    public static int getMapping(ByteString mdpsKey) {
        return MdpsTypeMappings.getMappingForMdpsKey(mdpsKey);
    }

    /**
     * @param bb ByteBuffer containing the data to be converted
     * @param mdpsKeyLength Length of the MdpsKey
     * @return vendorkey
     */
    public static int getMapping(ByteBuffer bb, final int mdpsKeyLength) {
        int position = bb.position();
        int b1 = bb.get(position + mdpsKeyLength - 2);
        return MdpsTypeMappings.getMappingForMdpsType(b1 == ',' ? 0 : b1, bb.get(position + mdpsKeyLength - 1));
    }

    /**
     * @param mdpsKey to be converted
     * @return vendorkey
     */
    ByteString convert(ByteString mdpsKey) {
        return convert(mdpsKey, getMapping(mdpsKey));
    }

    /**
     * @param bb ByteBuffer containing the data to be converted
     * @param mdpsKeyLength Length of key
     * @return vendorkey
     */
    ByteString convert(ByteBuffer bb, int mdpsKeyLength) {
        return convert(bb, mdpsKeyLength, getMapping(bb, mdpsKeyLength));
    }

    /**
     * @param mdpsKey to be converted
     * @param mapping as obtained from calling
     * {@link #getMapping(de.marketmaker.istar.common.util.ByteString)}
     * @return vendorkey
     */
    ByteString convert(ByteString mdpsKey, int mapping) {
        final boolean delayedKey = isDelayedKey(mdpsKey);
        if (this.processDelayedRecords != delayedKey) {
            return null;
        }
        if (isUnknownXfeedType(mapping) && this.ignoreUnknownTypes) {
            return null;
        }
        return mdpsKey.substring(delayedKey ? 2 : 0, mdpsKey.length() - getSuffixLength(mdpsKey));
    }

    /**
     * @param bb ByteBuffer containing the data to be converted
     * @param mdpsKeyLength Length of key
     * @param mapping as obtained from calling
     * {@link #getMapping(de.marketmaker.istar.common.util.ByteString)}
     * @return vendorkey
     */
    ByteString convert(ByteBuffer bb, int mdpsKeyLength, int mapping) {
        final boolean delayedKey = isDelayedKey(bb);
        if (this.processDelayedRecords != delayedKey) {
            return null;
        }
        if (isUnknownXfeedType(mapping) && this.ignoreUnknownTypes) {
            return null;
        }

        // When delayed the buffer was already advanced after the prefix
        mdpsKeyLength -= (delayedKey ? 2 : 0);
        int length = mdpsKeyLength - getSuffixLength(bb, mdpsKeyLength);
        ByteString vendorkey = ByteString.readWithLengthFrom(bb, length);
        if (length < mdpsKeyLength) {
            bb.position(bb.position() + (mdpsKeyLength - length));
        }
        return vendorkey;
    }
}
