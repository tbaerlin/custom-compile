/*
 * TickCoder.java
 *
 * Created on 15.11.2004 11:36:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.tick;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TickCoder {

    // ############ FIRST BYTE
    // for non-standard ticks, Trade/Bid/Ask will be 0 (non-std iff 1st byte & TYPE_MASK == 0)

    public final static int TYPE_TRADE = 0x80;

    public final static int TYPE_BID = 0x40;

    public final static int TYPE_ASK = 0x20;

    final static int TYPE_MASK = 0xE0;

    final static int TIME_UNCHANGED = 0x10;

    final static int TIME_ABSOLUTE = 0x08;

    final static int SUPPLEMENT_PRESENT = 0x04;

    final static int SUPPLEMENT_UNCHANGED = 0x02;

    final static int TIME_MSB = 0x01;

    // ############ BYTE FOR EACH TRADE/BID/ASK. IF VOLUME_UNCHANGED IS FALSE,
    // ############ THERE WILL BE ANOTHER BYTE WITH VOLUME'S TYPE AND NULL ENCODING

    final static int PRICE_PRESENT = 0x80;

    final static int PRICE_ABSOLUTE = 0x40;

    final static int DATA_TYPE_BYTE = 0x00;

    final static int DATA_TYPE_SHORT = 0x10;

    final static int DATA_TYPE_INT = 0x20;

    final static int DATA_TYPE_LONG = 0x30;

    final static int DATA_TYPE_MASK = 0x30;

    final static int ZERO_NULLS = 0x00;

    final static int ONE_NULL = 0x04;

    final static int TWO_NULLS = 0x08;

    final static int THREE_NULLS = 0x0C;

    final static int NULLS_MASK = 0x0C;

    final static int VOLUME_PRESENT = 0x02;

    final static int VOLUME_UNCHANGED = 0x01;

    // ############ SUPPLEMENTARY FIRST BYTE
    // used for non-standard ticks; will also contain trade/bid/ask flags (TYPE_MASK)

    final static int TICK_WITH_CLOSE = 0x10;

    final static int TICK_HAS_YIELD = 0x08;

    final static int TICK_YIELD_TYPE_INT = 0x04;
    
    final static int TICK_WITH_KASSA = 0x02;

    final static int TICK_SUSPEND = 0x01;

    // only used if TICK_SUSPEND is set
    public final static int TICK_SUSPEND_START = 0x02;

    // only used if TICK_SUSPEND is set
    public final static int TICK_SUSPEND_END = 0x04;

    // ############ SUPPLEMENT WITH NOTIERUNGSART

    final static int WITH_NOTIERUNGSART = 0x02;

    final static int WITH_SUPPLEMENT = 0x01;


    private TickCoder() {
    }
}
