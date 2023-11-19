/*
 * MessageTypes.java
 *
 * Created on 01.08.2005 14:28:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

/**
 * Message Types in MDPS, updated according to E-Mail from zz, 2008-11-20
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsMessageTypes {
    public static final int HEARTBEAT = 10;

    public static final int UPDATE = 11;

    public static final int DELETE = 12;

    public static final int DELETE_MSG = 13;

    public static final int RETRANS = 14;

    public static final int RETRANS_NEW_SYMBOL = 15;

    public static final int RECAP = 16;

    public static final int DELETE_FIELD = 17;

    public static final int STATIC_UPDATE = 18;

    public static final int STATIC_DELETE = 19;

    public static final int STATIC_DELETE_FIELD = 20;

    public static final int STATIC_RECAP = 21;

    public static final int NEWS_STORY = 22;

    public static final int DJN_NEWS = 22; /* same as MT_NEWS_STORY */

    public static final int ANNOUNCEMENT = 23;

    public static final int FIX_DY = 24;

    public static final int FIX_SEND_DY = 25;

    public static final int FIX_ST = 26;

    public static final int FIX_SEND_ST = 27;

    public static final int DEL_DY = 28;

    public static final int DEL_SEND_DY = 29;

    public static final int DEL_ST = 30;

    public static final int DEL_SEND_ST = 31;

    public static final int STATIC_RECAP_NONWM = 32;

    public static final int STATIC_RECAP_KL1 = 33;

    public static final int STATIC_RECAP_KL2 = 34;

    public static final int STATIC_RECAP_ON_NONWM = 35;  /* OVERNIGHT */

    public static final int STATIC_RECAP_ON_KL1 = 36;  /* OVERNIGHT */

    public static final int STATIC_RECAP_ON_KL2 = 37;  /* OVERNIGHT */

    public static final int STATIC_UPDATE_NONWM = 39;

    public static final int STATIC_UPDATE_KL1 = 40;

    public static final int STATIC_UPDATE_KL2 = 41;

    public static final int NEWS_SOURCE = 42;

    public static final int UPDATE_COMPFX = 43;  /* CFXX */

    public static final int UPDATE_XRATE = 44;  /* CFXX */

    public static final int UPDATE_INDICATOR = 45;  /* IndCalc */

    public static final int LATENCY_TEST = 46;  /* LatencyTest */

    public static final int RECAP_ON = 47;

    public static final int CODI_LOGIN = 48;

    public static final int NEWS_REFERENCE = 49;  /* IQS */

    public static final int NEWS_PAGE_PDLFMT = 50;  /* IPS */

    /* Interface Server related Messages: */
    public static final int IS_UPDATE_REQ = 51;

    public static final int IS_UPDATE_ACK = 52;

    public static final int IS_UPDATE_ERR_RESP = 53;

    public static final int IS_CLT_SUMMARY_DATA_REQ = 54;

    public static final int IS_CLT_SUMMARY_DATA_RESP = 55;

    public static final int IS_CLT_SUMMARY_DATA_ERR_RESP = 56;

    public static final int IS_CLT_UPDATE_REQ = 57;

    public static final int IS_CLT_UPDATE_ACK = 58;

    public static final int IS_CLT_UPDATE_ERR_RESP = 59;

    public static final int IS_CLT_RECAP_DATA_REQ = 60;

    public static final int IS_CLT_RECAP_DATA_RESP = 61;

    public static final int IS_CLT_RECAP_DATA_ERR_RESP = 62;

    /* Static related - added on 05/02/2008 */
    public static final int IS_ST_UPDATE_REQ = 63;

    public static final int IS_ST_UPDATE_ACK = 64;

    public static final int IS_ST_UPDATE_ERR_RESP = 65;

    public static final int IS_CLT_ST_UPDATE_REQ = 66;

    public static final int IS_CLT_ST_UPDATE_ACK = 67;

    public static final int IS_CLT_ST_UPDATE_ERR_RESP = 68;

    /* Strangler related Messages: */
    public static final int STRANGLE_UPDATE = 70;

    public static final int STRANGLE_DELETE = 71;

    /* Special Message Types for Data Injector: */
    public static final int INDICATOR_UPDATE = 72;

    public static final int SPECIAL_STATIC_UPDATE = 73;

    public static final int INTERNAL_UPDATE = 74;

    /* Delayer related Messages: */
    public static final int DELAY_UPDATE = 80;

    public static final int DELAY_DELETE = 81;

    /* Composite future update */
    public static final int UPDATE_COMPFU = 82;

    public static final int PAGE_PDLFMT = 83;  /* IPS */

    public static final int UPDATE_AVS = 85;

    public static final int BLOB = 101;

    // used to create mappings, so update when new max value emerges
    public static final int MAX_MESSAGE_TYPE_VALUE = 101;
}
