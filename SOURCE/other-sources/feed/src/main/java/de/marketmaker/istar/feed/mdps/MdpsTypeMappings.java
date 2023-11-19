/*
 * MdpsKeyMappings.java
 *
 * Created on 02.07.2008 12:01:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import de.marketmaker.istar.common.util.ByteString;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsTypeMappings {
    public static final boolean WITH_OLD_TYPE_MAPPING = Boolean.getBoolean("istar.withOldTypeMappings");

    static final String[] VWD2MDPS_TYPE = new String[]{
            null, //  0
            "E",  //  1
            "O",  //  2
            "F",  //  3
            "FO", //  4
            "B",  //  5
            "I",  //  6
            "ST", //  7
            "W",  //  8
            "FN", //  9
            "X",  // 10
            null, //
            "MD", // 12
            null, //
            "EB", // 14
            null, //
            "RT", // 16
            "C",  // 17
            "EF"  // 18
    };

    static final Map<String, String> MAPPINGS;

    static final Map<String, String> REVERSE_MAPPINGS;

    static {
        final Map<String, String> tmp = new HashMap<>();
        for (int i = 0; i < VWD2MDPS_TYPE.length; i++) {
            if (VWD2MDPS_TYPE[i] != null) {
                tmp.put(VWD2MDPS_TYPE[i], Integer.toString(i));
            }
        }
        tmp.put("S", "99"); // S is not really expected to occur...
        MAPPINGS = Collections.unmodifiableMap(tmp);

        final Map<String, String> tmpR = new HashMap<>();
        for (Map.Entry<String, String> e : MAPPINGS.entrySet()) {
            tmpR.put(e.getValue(), e.getKey());
        }
        REVERSE_MAPPINGS = Collections.unmodifiableMap(tmpR);
    }

    public static final ByteString[] MDPS_TYPES = new ByteString[]{
            new ByteString("OT")   // 0x00
            , new ByteString("E")  // 0x01
            , new ByteString("S")  // 0x02
            , new ByteString("O")  // 0x03
            , new ByteString("F")  // 0x04
            , new ByteString("FO") // 0x05
            , new ByteString("B")  // 0x06
            , new ByteString("I")  // 0x07
            , new ByteString("ST") // 0x08
            , new ByteString("W")  // 0x09
            , new ByteString("FN") // 0x0a
            , new ByteString("X")  // 0x0b
            , new ByteString("MD") // 0x0c
            , new ByteString("EB") // 0x0d
            , new ByteString("RT") // 0x0e
            , new ByteString("C")  // 0x0f
            , new ByteString("EF") // 0x10
    };

    public static final ByteString[] MDPS_TYPE_SUFFIXES = new ByteString[MDPS_TYPES.length];

    static {
        byte[] comma = {','};
        for (int i = 0; i < MDPS_TYPES.length; i++) {
            MDPS_TYPE_SUFFIXES[i] = MDPS_TYPES[i].prepend(comma);
        }
    }

    private static final int[] VWD_TYPE_BY_MDPS_TYPE_INDEX = new int[]{
            0, 1, 99, 2, 3, 4, 5, 6, 7, 8, 9, 10, 12, 14, 16, 17, 18
    };

    public static final int UNKNOWN = 0;

    /**
     * To be called with result of {@link #getMappingForMdpsType(int, int)}, shifted one byte to the right.
     */
    public static ByteString getMdpsKeyTypeById(int id) {
        return MDPS_TYPES[id];
    }

    public static ByteString getMdpsKeyTypeSuffixById(int id) {
        return MDPS_TYPE_SUFFIXES[id];
    }

    public static int getMappingForMdpsTypeId(int id) {
        return (id << 8) + VWD_TYPE_BY_MDPS_TYPE_INDEX[id];
    }

    public static String getMdpsKeyTypeByVwdType(int type) {
        try {
            return VWD2MDPS_TYPE[type];
        } catch (ArrayIndexOutOfBoundsException e) {
            if (type == 99) {
                return "S";
            }
            return null;
        }
    }

    public static String toNumericType(String type) {
        return MAPPINGS.get(type);
    }

    public static int getMappingForMdpsKey(ByteString bs) {
        final int length = bs.length();
        int b1 = bs.byteAt(length - 2);
        return getMappingForMdpsType(b1 == ',' ? 0 : b1, bs.byteAt(length - 1));
    }

    public static String fromNumericType(String type) {
        return REVERSE_MAPPINGS.get(type);
    }

    public static int getMappingForVwdKey(ByteString bs) {
        if (bs.byteAt(1) == '.') {
            return getMappingForVwdType(0, bs.byteAt(0));
        }
        else {
            return getMappingForVwdType(bs.byteAt(0), bs.byteAt(1));
        }
    }

    public static int getMappingForMdpsType(int b1, int b2) {
        // optimized for performance
        // result is a short int, the upper byte contains the index of the mdps type in MDPS_TYPES,
        // the lower byte is the numeric vwd sec type (i.e., the index of the type in VWD2MDPS_TYPE)
        switch (b2) {
            case 'B':
                return (b1 == 0) ? (0x0600 + 5) : (b1 == 'E') ? (0x0D00 + 14) : UNKNOWN;
            case 'C':
                return (b1 == 0) ? (0x0F00 + 17) : UNKNOWN;
            case 'D':
                return (b1 == 'M') ? (0x0C00 + 12) : UNKNOWN;
            case 'E':
                return (b1 == 0) ? (0x0100 + 1) : UNKNOWN;
            case 'F':
                return (b1 == 0) ? (0x0400 + 3) : (b1 == 'E') ? (0x1000 + 18) : UNKNOWN;
            case 'I':
                return (b1 == 0) ? (0x0700 + 6) : UNKNOWN;
            case 'N':
                return (b1 == 'F') ? (0x0A00 + 9) : UNKNOWN;
            case 'O':
                return (b1 == 0) ? (0x0300 + 2) : (b1 == 'F') ? (0x0500 + 4) : UNKNOWN;
            case 'S':
                return (b1 == 0) ? (0x0200 + 99) : UNKNOWN;
            case 'T':
                return (b1 == 'S') ? (0x0800 + 7) : (b1 == 'R') ? (0x0E00 + 16) : UNKNOWN;
            case 'W':
                return (b1 == 0) ? (0x0900 + 8) : UNKNOWN;
            case 'X':
                return (b1 == 0) ? (0x0B00 + 10) : UNKNOWN;
            default:
                return UNKNOWN;
        }
    }

    public static int getMappingForVwdType(int b1, int b2) {
        // optimized for performance
        // result is a short int, the upper byte contains the index of the mdps type in MDPS_TYPES,
        // the lower byte is the numeric vwd sec type (i.e., the index of the type in VWD2MDPS_TYPE)
        switch (b2) {
            case '0':
                return (b1 == '1') ? (0x0B00 + 10) : UNKNOWN;
            case '1':
                return (b1 == 0) ? (0x0100 + 1) : UNKNOWN;
            case '2':
                return (b1 == 0) ? (0x0300 + 2) : (b1 == '1') ? (0x0C00 + 12) : UNKNOWN;
            case '3':
                return (b1 == 0) ? (0x0400 + 3) : UNKNOWN;
            case '4':
                return (b1 == 0) ? (0x0500 + 4) : (b1 == '1') ? (0x0D00 + 14) : UNKNOWN;
            case '5':
                return (b1 == 0) ? (0x0600 + 5) : UNKNOWN;
            case '6':
                return (b1 == 0) ? (0x0700 + 6) : (b1 == '1') ? (0x0E00 + 16) : UNKNOWN;
            case '7':
                return (b1 == 0) ? (0x0800 + 7) : (b1 == '1') ? (0x0F00 + 17) : UNKNOWN;
            case '8':
                return (b1 == 0) ? (0x0900 + 8) : (b1 == '1') ? (0x1000 + 18) : UNKNOWN;
            case '9':
                return (b1 == 0) ? (0x0A00 + 9) : (b1 == '9') ? (0x0200 + 99) : UNKNOWN;
            default:
                return UNKNOWN;
        }
    }

    public static int getMappingForVwdType(int type) {
        switch (type) {
            case 1:
                return 0x0100 + 1;
            case 2:
                return 0x0300 + 2;
            case 3:
                return 0x0400 + 3;
            case 4:
                return 0x0500 + 4;
            case 5:
                return 0x0600 + 5;
            case 6:
                return 0x0700 + 6;
            case 7:
                return 0x0800 + 7;
            case 8:
                return 0x0900 + 8;
            case 9:
                return 0x0A00 + 9;
            case 10:
                return 0x0B00 + 10;
            // no 11
            case 12:
                return 0x0C00 + 12;
            // no 13
            case 14:
                return 0x0D00 + 14;
            // no 15
            case 16:
                return 0x0E00 + 16;
            case 17:
                return 0x0F00 + 17;
            case 18:
                return 0x1000 + 18;
            default:
                return (type == 99) ? (0x0200 + 99) : type;
        }
    }
}
