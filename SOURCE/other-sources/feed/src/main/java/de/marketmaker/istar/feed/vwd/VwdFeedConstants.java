/*
 * VwdFeedConstants.java
 *
 * Created on 31.07.2003 07:42:03
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

/**
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class VwdFeedConstants {

    public static final byte MESSAGE_TYPE_CORRECTION = (byte) '#';

    public static final byte MESSAGE_TYPE_DYNAMIC_UPDATE = (byte) 'A';

    public static final byte MESSAGE_TYPE_DYNAMIC_RECAP = (byte) 'B';

    public static final byte MESSAGE_TYPE_STATIC_UPDATE = (byte) 'C';

    public static final byte MESSAGE_TYPE_STATIC_RECAP = (byte) 'D';

    public static final byte MESSAGE_TYPE_FIELDMAP = (byte) 'E';

    public static final byte MESSAGE_TYPE_DELETE_FIELDS = (byte) 'F';

    public static final byte MESSAGE_TYPE_ENTITLEMENTS = (byte) 'H';

    public static final byte MESSAGE_TYPE_NEWS = (byte) 'I';

    public static final byte MESSAGE_TYPE_MIDNIGHT = (byte) 'J';

    public static final byte MESSAGE_TYPE_PAGE = (byte) 'K';

    public static final byte MESSAGE_TYPE_DYNAMIC_DELETE = (byte) 'M';

    public static final byte MESSAGE_TYPE_STATIC_DELETE = (byte) 'N';

    public static final byte MESSAGE_TYPE_EXCHANGELIST = (byte) 'O';

    public static final byte MESSAGE_TYPE_RATIOS = (byte) 'R';

    public static final byte MESSAGE_TYPE_MIDNIGHT2 = (byte) 'X';

    public static final byte MESSAGE_TYPE_BLOB = (byte) 'Z';

    public static final byte MESSAGE_TYPE_UNKNOWN = (byte) 0;

    private static final byte[] ALL = new byte[Byte.MAX_VALUE];

    static {
        for (int i = 0; i < ALL.length; i++) {
            ALL[i] = (byte) i;
        }
    }

    private VwdFeedConstants() {
    }

    public static boolean isDelete(int msgtype) {
        return msgtype == MESSAGE_TYPE_DYNAMIC_DELETE
                || msgtype == MESSAGE_TYPE_STATIC_DELETE;
    }

    public static boolean isRecap(int msgtype) {
        return msgtype == MESSAGE_TYPE_DYNAMIC_RECAP
                || msgtype == MESSAGE_TYPE_STATIC_RECAP;
    }

    public static byte[] getXfeedDynamicAndStatic() {
        return new byte[]{
                VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE
                , VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_RECAP
                , VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE
                , VwdFeedConstants.MESSAGE_TYPE_STATIC_DELETE
                , VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE
                , VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP
        };
    }

    public static byte[] getXfeedDynamicAndStaticAndRatios() {
        return new byte[]{
                VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE
                , VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_RECAP
                , VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE
                , VwdFeedConstants.MESSAGE_TYPE_STATIC_DELETE
                , VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE
                , VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP
                , VwdFeedConstants.MESSAGE_TYPE_RATIOS
                , VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS
        };
    }

    public static byte[] getAll() {
        return ALL;
    }

    public static void main(String[] args) {
        for (char c = 0x20; c < 0x80; ) {
            System.out.print(c);
            System.out.print(' ');
            c++;
            if (c % 16 == 0) {
                System.out.println();
            }
        }
    }
}
