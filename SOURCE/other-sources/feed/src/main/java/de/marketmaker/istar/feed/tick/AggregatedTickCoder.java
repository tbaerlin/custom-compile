/*
 * AggregatedTickCoder.java
 *
 * Created on 03.03.2005 15:57:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @version $Id$
 */
class AggregatedTickCoder {
    // FIRST BYTE FOR EACH TICK:

    // Bits 13-16
    final static int OPEN_ABSOLUTE = 0x8000;

    final static int HIGH_EQUALS_OPEN = 0x4000;

    final static int LOW_EQUALS_OPEN = 0x2000;

    final static int CLOSE_EQUALS_OPEN = 0x1000;

    // Bit 12 -- ohlc encoded as plain long values
    final static int OHLC_RAW = 0x0800;

    // Bit 11
    final static int OPEN_UNCHANGED = 0x0400;

    // Bits 9, 10
    final static int OHLC_ZERO_NULLS = 0x0000;

    final static int OHLC_ONE_NULL = 0x0100;

    final static int OHLC_TWO_NULLS = 0x0200;

    final static int OHLC_THREE_NULLS = 0x0300;

    final static int OHLC_NULLS_MASK = 0x0300;


    // SECOND BYTE FOR EACH TICK:

    // Bits 7,8
    final static int TIME_ABSOLUTE = 0x80;

    final static int TIME_MSB = 0x40;

    // Bits 5,6
    final static int NUMBER_TYPE_BYTE = 0x00;

    final static int NUMBER_TYPE_SHORT = 0x10;

    final static int NUMBER_TYPE_INT = 0x20;

    final static int NUMBER_TYPE_LONG = 0x30;

    final static int NUMBER_TYPE_MASK = 0x30;


    // Bits 3,4
    final static int VOLUME_TYPE_BYTE = 0x00;

    final static int VOLUME_TYPE_SHORT = 0x04;

    final static int VOLUME_TYPE_INT = 0x08;

    final static int VOLUME_TYPE_LONG = 0x0C;

    final static int VOLUME_TYPE_MASK = 0x0C;

    // Bits 1,2
    final static int OHLC_TYPE_BYTE = 0x00;

    final static int OHLC_TYPE_SHORT = 0x01;

    final static int OHLC_TYPE_INT = 0x02;

    final static int OHLC_TYPE_LONG = 0x03;

    final static int OHLC_TYPE_MASK = 0x03;
}
