/*
 * Constants.java
 *
 * Created on 19.09.13 09:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.util.Arrays;

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;

/**
 * @author oflege
 */
class Constants {

    static final byte[] NULL_BYTES = new byte[0];

    static final byte[][] OBJECT_PREFIXES = new byte[][]{
            NULL_BYTES,
            NULL_BYTES,
            "/D".getBytes(CP_1252),
            "/E".getBytes(CP_1252)
    };

    static final byte STX = 0x02;  // Start of message

    static final byte ETX = 0x03;  // End of message

    static final byte FS = 0x1C;  // Field separator

    static final byte RS = 0x1E;  // FID Start

    static final byte GS = 0x1D;  // Body separator

    static final byte SI = 0x0F;  // Start of envelope

    static final byte SO = 0x0E;  // End of envelope

    // - request Message Types ------------------------------------------

    static final int MSG_LOGON_REQUEST = 31;

    static final int MSG_LOGOFF_REQUEST = 32;

    static final int MSG_DATA_REQUEST = 33;

    static final int MSG_RELEASE_REQUEST = 34;

    static final int MSG_HEARTBEAT_REQUEST = 35;

    static final int MSG_CHANGE_REQUEST = 37;

    static final int MSG_EXECUTE_REQUEST = 38;

    private static final String[] REQUEST_TYPES = new String[]{
            "LOGON", "LOGOFF", "DATA", "RELEASE", "HEARTBEAT", "CHANGE", "EXECUTE"
    };

    // - response Message Types ------------------------------------------

    static final int MSG_LOGON_RESPONSE = 1;

    static final int MSG_LOGOFF_RESPONSE = 2;

    static final int MSG_DATA_RESPONSE = 3;

    static final int MSG_DATA_STATUS_RESPONSE = 4;

    static final int MSG_RELEASE_RESPONSE = 5;

    static final int MSG_HEARTBEAT_RESPONSE = 6;

    private static final String[] RESPONSE_TYPES = new String[]{
            "LOGON", "LOGOFF", "DATA", "DATA_STATUS", "RELEASE", "HEARTBEAT"
    };

    // - other Message Types ------------------------------------------

    static final int MSG_SERVICE_STATUS = 61;

    static final int MSG_REJECT = 62;

    static final int MSG_BROADCAST = 63;

    // - Field IDs ------------------------------------------

    static final int FID_MESSAGE_TYPE = 1;

    static final int FID_REQUEST_ID = 2;

    static final int FID_USERID = 3;

    static final int FID_PASSWORD = 4;

    static final int FID_SECURITYTYPE = 5;

    static final int FID_DATALEN = 6;

    static final int FID_USERDATA = 7;

    static final int FID_OBJECTNAME = 8;

    static final int FID_GROUP = 9;

    static final int FID_PERMISSION = 10;

    static final int FID_SERVICEID = 11;

    static final int FID_QUERY = 12;

    static final int FID_STARTDATE = 13;

    static final int FID_ENDDATE = 14;

    static final int FID_STARTTIME = 15;

    static final int FID_ENDTIME = 16;

    static final int FID_EXCHANGE_STARTTIME = 17;

    static final int FID_EXCHANGE_ENDTIME = 18;

    static final int FID_MAX_RESULT = 19;

    static final int FID_NEWS_STORY_ID = 20;

    static final int FID_PAGE_X = 21;

    static final int FID_PAGE_Y = 22;

    static final int FID_NEWNAME = 23;

    static final int FID_FIELDLIST = 24;

    static final int FID_RESOLUTION = 25;

    static final int FID_SERVICE_NAME = 26;

    static final int FID_SERVICE_TYPE = 27;

    static final int FID_SERVICE_TABLE = 28;

    static final int FID_EXPIRY = 29;

    static final int FID_LOCATION = 30;

    static final int FID_TEXT = 31;

    static final int FID_VERSION = 32;

    static final int FID_HB_REQUEST_TIMESTAMP = 33;

    static final int FID_HB_RESPONSE_TIMESTAMP = 34;

    static final int FID_TIME_RECEIVED = 35;

    static final int FID_DATE_RECEIVED = 36;

    static final int FID_SOURCE = 41;

    static final int FID_EXCHANGE = 47;

    static final int FID_SECTYPE_LIST = 48;

    static final int FID_SELECTOR_LIST = 49;

    static final int FID_EXECUTE_COMMAND = 50;

    static final int FID_ENCODING_TYPE = 71;

    static final int FID_DATA_TYPE = 72;

    static final int FID_DATA_REQUEST_TYPE = 73;

    static final int FID_DATA_RESPONSE_TYPE = 74;

    static final int FID_DATA_RESPONSE_FLAG = 75;

    static final int FID_SERVICE_STATUS = 76;

    static final int FID_LOGON_STATUS = 77;

    static final int FID_RELEASE_STATUS = 78;

    static final int FID_DATA_STATUS = 79;

    static final int FID_INFO_TYPE = 80;

    static final int FID_REJECT_ERROR_TYPE = 81;

    static final int FID_LOGON_REQUEST_FLAG = 82;

    static final int FID_LOGOFF_STATUS = 83;

    // - data request type

    static final byte DATA_REQUEST_TYPE_RECAP_AND_UPDATES = (byte) 'U';

    static final byte DATA_REQUEST_TYPE_RECAP = (byte) 'R';

    static final byte DATA_REQUEST_TYPE_UPDATES = (byte) 'B';

    static final byte DATA_REQUEST_TYPE_END_OF_DAY = (byte) 'E';

    static final byte DATA_REQUEST_TYPE_CHANGE = (byte) 'C';

    static final byte DATA_REQUEST_TYPE_EXECUTE = (byte) 'X';

    // - FID_REQUEST_STATUS

    static final byte DATA_STATUS_OK = (byte) 'a';

    static final byte DATA_STATUS_STALE = (byte) 'b';

    static final byte DATA_STATUS_ALREADY_REQUESTED = (byte) 'c';

    static final byte DATA_STATUS_NO_PERMISSION = (byte) 'd';

    static final byte DATA_STATUS_RENAME = (byte) 'e';

    static final byte DATA_STATUS_RESOURCE_EXCEEDED = (byte) 'f';

    static final byte DATA_STATUS_DELETE = (byte) 'g';

    static final byte DATA_STATUS_TIMEOUT = (byte) 'h';

    static final byte DATA_STATUS_NOT_FOUND = (byte) 'i';

    static final byte DATA_STATUS_INVALID_REQUEST = (byte) 'j';

    static final byte DATA_STATUS_SERVICE_OUTAGE = (byte) 'k';

    static final byte DATA_STATUS_INTERNAL_ERROR = (byte) 'l';

    static final byte DATA_STATUS_REQUEST_DELAYED = (byte) 'm';

    static final byte DATA_STATUS_NOT_REQUESTED = (byte) 'n';

    static final byte DATA_STATUS_NOT_IMPLEMENTED = (byte) 'o';

    static final byte DATA_STATUS_QUERY_ACCEPTED = (byte) 'p';

    static final byte DATA_STATUS_QUERY_IN_PROGRESS = (byte) 'q';

    static final byte DATA_STATUS_QUERY_COMPLETED = (byte) 'r';

    static final byte DATA_STATUS_REQUIRED_FIELD_MISSING = (byte) 's';

    static final byte DATA_STATUS_NOT_LOGGED_ON = (byte) 't';

    private static final String[] DATA_STATUS = new String[]{
            "OK", "STALE", "ALREADY_REQUESTED", "NO_PERMISSION", "RENAME", "RESOURCE_EXCEEDED", "DELETE",
            "TIMEOUT", "NOT_FOUND", "INVALID_REQUEST", "SERVICE_OUTAGE", "INTERNAL_ERROR", "REQUEST_DELAYED",
            "NOT_REQUESTED", "NOT_IMPLEMENTED", "QUERY_ACCEPTED", "QUERY_IN_PROGRESS", "QUERY_COMPLETED",
            "REQUIRED_FIELD_MISSING", "NOT_LOGGED_ON"};

    // - VWD_FLD_DATA_RESPONSE_TYPE
    static final byte DATA_RESPONSE_TYPE_UPDATE = (byte) 'U';

    static final byte DATA_RESPONSE_TYPE_RECAP = (byte) 'R';

    static final byte DATA_RESPONSE_TYPE_PARTIAL_RECAP = (byte) 'P';

    // VMT_FID_DATA_RESPONSE_FLAG
    static final byte DATA_RESPONSE_FLAG_RECAP_COMPLETE = (byte) 'C';

    static final byte DATA_RESPONSE_FLAG_RESPONSE_COMPLETE = (byte) 'D';

    static final byte DATA_RESPONSE_FLAG_NONCACHEABLE = (byte) 'N';

    static final byte DATA_RESPONSE_FLAG_EXPIRES = (byte) 'E';

    static final byte DATA_RESPONSE_FLAG_UNIQUE = (byte) 'U';

    // VMT_FID_REJECT_TYPE
    static final byte REJECT_ERROR_TYPE_MESSAGE_TYPE_MISSING = (byte) 'A';

    static final byte REJECT_ERROR_TYPE_INVALID_MESSAGE_TYPE = (byte) 'B';

    static final byte REJECT_ERROR_TYPE_MESSAGE_TOO_LONG = (byte) 'C';

    static final byte REJECT_ERROR_TYPE_MESSAGE_TOO_SHORT = (byte) 'D';

    static final byte REJECT_ERROR_TYPE_STX_MISSING = (byte) 'E';

    static final byte REJECT_ERROR_TYPE_ETX_MISSING = (byte) 'F';

    static final byte REJECT_ERROR_TYPE_GS_MISSING = (byte) 'G';

    static final byte REJECT_ERROR_TYPE_RS_MISSING = (byte) 'H';

    static final byte REJECT_ERROR_TYPE_US_MISSING = (byte) 'I';

    static final byte REJECT_ERROR_TYPE_SI_MISSING = (byte) 'J';

    static final byte REJECT_ERROR_TYPE_SO_MISSING = (byte) 'K';

    static final byte REJECT_ERROR_TYPE_INVALID_FORMAT = (byte) 'L';

    static final byte REJECT_ERROR_TYPE_RESOURCE_FAILURE = (byte) 'M';

    static final byte REJECT_ERROR_TYPE_INTERNAL_ERROR = (byte) 'N';

    static final byte REJECT_ERROR_TYPE_NOT_LOGGED_ON = (byte) 'O';

    // VMT_FID_SERVICE_STATUS
    static final byte SERVICE_STATUS_AVAILABLE = (byte) 'A';

    static final byte SERVICE_STATUS_PENDING = (byte) 'P';

    static final byte SERVICE_STATUS_UNAVAILABLE = (byte) 'D';

    static final byte SERVICE_STATUS_NOT_PERMISSIONED = (byte) 'N';

    // VMT_FID_LOGON_STATUS
    static final byte LOGON_STATUS_ACCEPTED = (byte) 'A';

    static final byte LOGON_STATUS_NOT_PERMISSIONED = (byte) 'P';

    static final byte LOGON_STATUS_RESOURCES_EXCEEDED = (byte) 'R';

    static final byte LOGON_STATUS_NOT_ACCEPTED = (byte) 'N';

    static final byte LOGON_STATUS_INVALID_REQUEST = (byte) 'I';

    static final byte LOGON_STATUS_ALREADY_LOGGED_ON = (byte) 'L';

    static final byte LOGON_STATUS_UNSUPPORTED_VERSION = (byte) 'U';

    static final byte LOGON_STATUS_REQUIRED_FIELD_MISSING = (byte) 'F';

    // VMT_FID_RELEASE_RESPONSE_STATUS
    static final byte RELEASE_STATUS_OK = (byte) 'O';

    static final byte RELEASE_STATUS_NOT_REQUESTED = (byte) 'N';

    static final byte RELEASE_STATUS_REQUIRED_FIELD_MISSING = (byte) 'X';

    static final byte RELEASE_STATUS_INVALID_REQUEST = (byte) 'Y';

    static final byte RELEASE_STATUS_ERROR_IN_RELEASE = (byte) 'Z';

    /*
    */

    static final byte DATA_TYPE_FIELD = 'B';


    static final byte SERVICE_ID_PRICE = '1';

    static final String SERVICE_NAME_PRICE = "vwdPrice";

    static final String SERVICE_TYPE_PRICE = "price";

    static final byte SERVICE_ID_EXCHANGE_SUBSCRIBE = '3';

    static final String SERVICE_NAME_EXCHANGE_SUBSCRIBE = "vwdExchangeSubscribe";

    static final String SERVICE_TYPE_EXCHANGE_SUBSCRIBE = "exchangeSubscribe";


    static final String[] FIELDNAMES = new String[84];

    static {
        Arrays.fill(FIELDNAMES, "");

        FIELDNAMES[FID_MESSAGE_TYPE] = "MESSAGE_TYPE";

        FIELDNAMES[FID_REQUEST_ID] = "REQUEST_ID";

        FIELDNAMES[FID_USERID] = "USERID";

        FIELDNAMES[FID_PASSWORD] = "PASSWORD";

        FIELDNAMES[FID_SECURITYTYPE] = "SECURITYTYPE";

        FIELDNAMES[FID_DATALEN] = "DATALEN";

        FIELDNAMES[FID_USERDATA] = "USERDATA";

        FIELDNAMES[FID_OBJECTNAME] = "OBJECTNAME";

        FIELDNAMES[FID_GROUP] = "GROUP";

        FIELDNAMES[FID_PERMISSION] = "PERMISSION";

        FIELDNAMES[FID_SERVICEID] = "SERVICEID";

        FIELDNAMES[FID_QUERY] = "QUERY";

        FIELDNAMES[FID_STARTDATE] = "STARTDATE";

        FIELDNAMES[FID_ENDDATE] = "ENDDATE";

        FIELDNAMES[FID_STARTTIME] = "STARTTIME";

        FIELDNAMES[FID_ENDTIME] = "ENDTIME";

        FIELDNAMES[FID_EXCHANGE_STARTTIME] = "EXCHANGE_STARTTIME";

        FIELDNAMES[FID_EXCHANGE_ENDTIME] = "EXCHANGE_ENDTIME";

        FIELDNAMES[FID_MAX_RESULT] = "MAX_RESULT";

        FIELDNAMES[FID_NEWS_STORY_ID] = "NEWS_STORY_ID";

        FIELDNAMES[FID_PAGE_X] = "PAGE_X";

        FIELDNAMES[FID_PAGE_Y] = "PAGE_Y";

        FIELDNAMES[FID_NEWNAME] = "NEWNAME";

        FIELDNAMES[FID_FIELDLIST] = "FIELDLIST";

        FIELDNAMES[FID_RESOLUTION] = "RESOLUTION";

        FIELDNAMES[FID_SERVICE_NAME] = "SERVICE_NAME";

        FIELDNAMES[FID_SERVICE_TYPE] = "SERVICE_TYPE";

        FIELDNAMES[FID_SERVICE_TABLE] = "SERVICE_TABLE";

        FIELDNAMES[FID_EXPIRY] = "EXPIRY";

        FIELDNAMES[FID_LOCATION] = "LOCATION";

        FIELDNAMES[FID_TEXT] = "TEXT";

        FIELDNAMES[FID_VERSION] = "VERSION";

        FIELDNAMES[FID_HB_REQUEST_TIMESTAMP] = "HB_REQUEST_TIMESTAMP";

        FIELDNAMES[FID_HB_RESPONSE_TIMESTAMP] = "HB_RESPONSE_TIMESTAMP";

        FIELDNAMES[FID_TIME_RECEIVED] = "TIME_RECEIVED";

        FIELDNAMES[FID_DATE_RECEIVED] = "DATE_RECEIVED";

        FIELDNAMES[FID_SOURCE] = "SOURCE";

        FIELDNAMES[FID_EXCHANGE] = "EXCHANGE";

        FIELDNAMES[FID_SECTYPE_LIST] = "SECTYPE_LIST";

        FIELDNAMES[FID_SELECTOR_LIST] = "SELECTOR_LIST";

        FIELDNAMES[FID_EXECUTE_COMMAND] = "EXECUTE_COMMAND";

        FIELDNAMES[FID_ENCODING_TYPE] = "ENCODING_TYPE";

        FIELDNAMES[FID_DATA_TYPE] = "DATA_TYPE";

        FIELDNAMES[FID_DATA_REQUEST_TYPE] = "DATA_REQUEST_TYPE";

        FIELDNAMES[FID_DATA_RESPONSE_TYPE] = "DATA_RESPONSE_TYPE";

        FIELDNAMES[FID_DATA_RESPONSE_FLAG] = "DATA_RESPONSE_FLAG";

        FIELDNAMES[FID_SERVICE_STATUS] = "SERVICE_STATUS";

        FIELDNAMES[FID_LOGON_STATUS] = "LOGON_STATUS";

        FIELDNAMES[FID_RELEASE_STATUS] = "RELEASE_STATUS";

        FIELDNAMES[FID_DATA_STATUS] = "DATA_STATUS";

        FIELDNAMES[FID_INFO_TYPE] = "INFO_TYPE";

        FIELDNAMES[FID_REJECT_ERROR_TYPE] = "REJECT_ERROR_TYPE";

        FIELDNAMES[FID_LOGON_REQUEST_FLAG] = "LOGON_REQUEST_FLAG";

        FIELDNAMES[FID_LOGOFF_STATUS] = "LOGOFF_STATUS";
    }

    static String getFieldname(int fid) {
        return (fid > 0 && fid < FIELDNAMES.length) ? FIELDNAMES[fid] : "";
    }

    static String getRequestType(int value) {
        if (value >= 31 && value <= 38) {
            return REQUEST_TYPES[(value - MSG_LOGON_REQUEST)];
        }
        else if (value >= 1 && value <= 6) {
            return RESPONSE_TYPES[(value - MSG_LOGON_RESPONSE)];
        }
        return null;
    }

    static String getDataStatus(String value) {
        int idx = (value.length() == 1) ? (value.charAt(0) - DATA_STATUS_OK) : -1;
        return (idx > 0 && idx < DATA_STATUS.length) ? DATA_STATUS[idx] : null;
    }
}
