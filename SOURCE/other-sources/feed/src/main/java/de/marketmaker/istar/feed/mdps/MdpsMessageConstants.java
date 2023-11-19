/*
 * MdpsMessage.java
 *
 * Created on 01.03.2006 16:51:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

/**
 * Various constants for mdps message processing
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public final class MdpsMessageConstants {
    private MdpsMessageConstants() {
    }

    public static final int HEADER_LENGTH = 6;

    public static final int HEADER_MESSAGE_LENGTH_OFFSET = 0;

    public static final int HEADER_MESSAGE_TYPE_OFFSET = 2;

    /* protocol version 1 */
    public static final int HEADER_TARGET_PROCESSID_OFFSET = 3;

    public static final int HEADER_BODY_LENGTH_OFFSET = 4;

    /* protocol version 3 */
    public static final int HEADER_V3_VERSION_OFFSET = 3;

    public static final int HEADER_V3_TARGET_PROCESSID_OFFSET = 4;

    public static final int MDPS_KEY_FID = 1; // mdps doc says 17, BUT feed capture says 1

    public static final int MDPS_KEY_LENGTH_OFFSET = 8;

    public static final int MDPS_KEY_OFFSET = 9;

    public static final byte MDPS_DELAYED_KEY_PREFIX1 = '/';

    public static final byte MDPS_DELAYED_KEY_PREFIX2 = 'D';

    public static final byte MDPS_EOD_KEY_PREFIX1 = '/';

    public static final byte MDPS_EOD_KEY_PREFIX2 = 'E';
}
