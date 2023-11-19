/*
 * IqsMessageProcessor.java
 *
 * Created on 24.09.13 07:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps.iqs;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.jcip.annotations.NotThreadSafe;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedDataRepository;
import de.marketmaker.istar.feed.FeedMarket;
import de.marketmaker.istar.feed.mdps.MdpsTypeMappings;
import de.marketmaker.istar.mdps.util.EntitledFieldGroup;
import de.marketmaker.istar.mdps.util.FieldFormatter;
import de.marketmaker.istar.mdps.util.OrderedEntitlementProvider;

import static de.marketmaker.istar.domain.data.SnapRecord.CP_1252;
import static de.marketmaker.istar.mdps.iqs.Constants.*;

/**
 * Processes incoming iqs messages. Expected to be used by a single thread (e.g., a SelectorThread).
 * Each message will be processed completely with the exception of symbol lookup requests that
 * require http-calls and may therefore spend a significant amount of time.
 * @author oflege
 */
@NotThreadSafe
public class IqsMessageProcessor {

    private static final ByteString VERSION = new ByteString("1.0");

    private static final byte[] ACCEPTED_TEXT = "Logon request processed successfully.".getBytes(CP_1252);

    private static final byte[] NOT_PERMISSIONED = "Invalid UserID/Password.".getBytes(CP_1252);

    private static final byte[] ALREADY_LOGGED_ON = "Already logged on.".getBytes(CP_1252);

    private static final byte[] NOT_LOGGED_ON_REJECTED = "Not logged on. Request rejected.".getBytes(CP_1252);

    private static final byte[] OBJECT_NOT_FOUND = "Object not found.".getBytes(CP_1252);

    private static final byte[] OBJECT_NOT_REQUESTED = "Object not requested.".getBytes(CP_1252);

    private static final byte[] OBJECT_RELEASED = "Object released.".getBytes(CP_1252);

    private static final byte[] INVALID_SID = "Invalid Service ID.".getBytes(CP_1252);

    private static final byte[] INVALID_UPDATE_MODE = "Invalid update mode.".getBytes(CP_1252);

    private static final byte[] REQUEST_ID_TOO_LONG = "RequestID length exceeds limit set.".getBytes(CP_1252);

    private static final byte[] USER_DATA_TOO_LONG = "UserData length exceeds limit set.".getBytes(CP_1252);

    private static final byte[] CPSINTRA = "cpsintra".getBytes(CP_1252);

    private static boolean withUnentitledFields(byte[] userData) {
        return Arrays.equals(CPSINTRA, userData);
    }

    // microseconds, duh...
    static final DateTimeFormatter DTF
            = DateTimeFormat.forPattern("yyyy-MM-dd_hh:mm:ss SSS000").withZoneUTC();

    static final ByteString LOOKUP_SELECTORS = new ByteString("LookupSelectors");

    private static final ByteString DELAY_PREFIX = new ByteString("/D");

    private static final ByteString EOD_PREFIX = new ByteString("/E");

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final IqsMessageBuilder builder = new IqsMessageBuilder();

    private final EntitledFieldsBuilder entitledFieldsBuilder;

    private final FeedDataRepository repository;

    private final OrderedEntitlementProvider entitlementProvider;

    private IqsSymbolLookupClient lookupClient;

    private final Subscriptions subscriptions = new Subscriptions();

    private final int quality;

    private final String qualityStr;

    private final byte serviceId;

    private final RecapResponse.Factory recapResponseFactory;

    public IqsMessageProcessor(OrderedEntitlementProvider entitlementProvider,
            FeedDataRepository repository, FieldFormatter formatter, int serviceId, int quality) {
        if (serviceId != 1 && serviceId != 3) {
            throw new IllegalArgumentException("serviceId " + serviceId + " not in [13]");
        }
        this.serviceId = (byte) ('0' + serviceId);

        if (quality < 1 || quality > 3) {
            throw new IllegalArgumentException("quality " + quality + " not in [123]");
        }
        this.quality = quality;
        this.qualityStr = Character.toString((char) ('0' + quality));

        this.entitlementProvider = entitlementProvider;
        this.entitledFieldsBuilder = new EntitledFieldsBuilder(entitlementProvider, formatter, quality);
        this.repository = repository;

        this.recapResponseFactory = new RecapResponse.Factory(quality);
    }

    public void setLookupClient(IqsSymbolLookupClient lookupClient) {
        this.lookupClient = lookupClient;
    }

    public void process(IqsClients.Client conn, byte[] rawMessage, IqsRequest msg) {
        if (msg == null) {
            rejectMessage(conn, REJECT_ERROR_TYPE_INVALID_FORMAT, msg, rawMessage);
            return;
        }

        final int messageType = msg.getMessageType();
        if (messageType == Integer.MIN_VALUE) {
            rejectMessage(conn, REJECT_ERROR_TYPE_MESSAGE_TYPE_MISSING, msg, rawMessage);
            return;
        }

        switch (messageType) {
            case MSG_LOGON_REQUEST:
                handleLogon(conn, msg);
                break;
            case MSG_DATA_REQUEST:
                handleData(conn, msg);
                break;
            case MSG_RELEASE_REQUEST:
                handleRelease(conn, msg);
                break;
            case MSG_HEARTBEAT_REQUEST:
                handleHeartbeatRequest(conn, msg);
                break;
            case MSG_HEARTBEAT_RESPONSE:
                handleHeartbeatResponse(conn);
                break;
/*
the reference implementation does not handle these:
            case MSG_EXECUTE_REQUEST:
            case MSG_LOGOFF_REQUEST:
            case MSG_CHANGE_REQUEST:
*/
            default:
                rejectMessage(conn, REJECT_ERROR_TYPE_INVALID_MESSAGE_TYPE, msg, rawMessage);
        }
    }

    private void handleHeartbeatResponse(IqsClients.Client conn) {
        // this can be ignored, we just log it...
        this.logger.info("<handleHeartbeatResponse> from " + conn);
    }

    private void rejectMessage(IqsClients.Client conn, byte status, IqsRequest msg, byte[] raw) {
        this.builder.prepare(MSG_REJECT);
        if (msg != null) {
            this.builder.header(FID_REQUEST_ID, msg);
        }
        this.builder.header(FID_REJECT_ERROR_TYPE, status);
        this.builder.header(FID_TEXT).hex(raw);
        conn.appendMessage(this.builder.build());
    }

    private void handleLogon(IqsClients.Client conn, IqsRequest msg) {
        if (conn.isLoggedIn()) {
            sendLogonResponse(conn, LOGON_STATUS_ALREADY_LOGGED_ON, msg, ALREADY_LOGGED_ON);
            return;
        }
        if (!checkRequiredFields(conn, msg, FID_VERSION, FID_USERID, FID_PASSWORD)) {
            return;
        }

        final ByteString version = msg.getString(FID_VERSION);
        if (!VERSION.equals(version)) {
            sendLogonResponse(conn, LOGON_STATUS_UNSUPPORTED_VERSION, msg,
                    format("Unsupported MarketTalk Version %s.", version));
        }
        else if (!conn.checkAuthentication(msg.getString(FID_USERID), msg.getString(FID_PASSWORD))) {
            sendLogonResponse(conn, LOGON_STATUS_NOT_PERMISSIONED, msg, NOT_PERMISSIONED);
        }
        else {
            sendLogonResponse(conn, LOGON_STATUS_ACCEPTED, msg, ACCEPTED_TEXT);
        }
    }

    private void sendLogonResponse(IqsClients.Client conn, byte status, IqsRequest msg,
            byte[] text) {
        final ByteBuffer bb = this.builder.prepare(MSG_LOGON_RESPONSE, msg)
                .header(FID_LOGON_STATUS, status)
                .header(FID_TEXT, text)
                .serviceTable(getMarkets(), this.serviceId, this.qualityStr)
                .build();
        conn.appendMessage(bb);
    }

    private void handleData(IqsClients.Client conn, IqsRequest msg) {
        if (!conn.isLoggedIn()) {
            sendDataStatusResponse(conn, DATA_STATUS_NOT_LOGGED_ON, msg, NOT_LOGGED_ON_REJECTED);
            return;
        }

        if (!checkRequiredFields(conn, msg, FID_SERVICEID)) {
            return;
        }
        final int serviceId = msg.getByte(FID_SERVICEID);
        if (serviceId == SERVICE_ID_PRICE && serviceId == this.serviceId) {
            handlePriceData(conn, msg);
        }
        else if (serviceId == SERVICE_ID_EXCHANGE_SUBSCRIBE && serviceId == this.serviceId) {
            handleLookupSelectors(conn, msg);
        }
        else {
            sendDataStatusResponse(conn, DATA_STATUS_INVALID_REQUEST, msg, INVALID_SID);
        }
    }

    private void handleLookupSelectors(IqsClients.Client conn, IqsRequest msg) {
        if (!checkRequiredFields(conn, msg, FID_REQUEST_ID, FID_DATA_REQUEST_TYPE)) {
            return;
        }
        final byte requestType = msg.getByte(FID_DATA_REQUEST_TYPE);
        if (requestType != DATA_REQUEST_TYPE_EXECUTE) {
            sendDataStatusResponse(conn, DATA_STATUS_INVALID_REQUEST, msg,
                    format("Invalid data request type '%s'.", Character.toString((char) requestType)));
            return;
        }
        if (!checkRequiredFields(conn, msg, FID_EXECUTE_COMMAND, FID_EXCHANGE, FID_SECTYPE_LIST)) {
            return;
        }
        final ByteString cmd = msg.getString(FID_EXECUTE_COMMAND);
        if (!LOOKUP_SELECTORS.equals(cmd)) {
            sendDataStatusResponse(conn, DATA_STATUS_INVALID_REQUEST, msg,
                    format("Unknown execute request '%s'.", cmd));
            return;
        }
        final ByteString exchange = msg.getString(FID_EXCHANGE);
        if (repository.getMarket(exchange) == null) {
            sendDataStatusResponse(conn, DATA_STATUS_NOT_FOUND, msg,
                    format("Unknown exchange '%s'.", exchange));
            return;
        }

        final ByteString sectypes = msg.getString(FID_SECTYPE_LIST);
        final byte[] selectors = findSelectorsFor(exchange, sectypes);

        final ByteBuffer bb = this.builder.prepare(MSG_DATA_RESPONSE, msg, FID_USERDATA)
                .header(FID_SERVICEID, SERVICE_ID_EXCHANGE_SUBSCRIBE)
                .header(FID_EXCHANGE, exchange)
                .header(FID_SELECTOR_LIST, selectors)
                .build();
        conn.appendMessage(bb);
    }

    private byte[] findSelectorsFor(ByteString exchange, ByteString sectypes) {
        final int[] types = parse(sectypes);
        int[] selectors = this.entitlementProvider.getSelectors(exchange, types);
        if (selectors.length == 0) {
            return Constants.NULL_BYTES;
        }
        ByteBuffer bb = ByteBuffer.allocate(5 * selectors.length);
        for (int i = 0; i < selectors.length; i++) {
            if (i != 0) {
                bb.put((byte) ',');
            }
            FieldFormatter.appendUnsignedShort(bb, selectors[i]);
        }
        return Arrays.copyOf(bb.array(), bb.position());
    }

    private int[] parse(ByteString sectypes) {
        final String[] typeStrs = sectypes.toString().split(",");
        final int[] result = new int[typeStrs.length];
        for (int i = 0; i < typeStrs.length; i++) {
            result[i] = parseSectype(typeStrs[i]);
        }
        return result;
    }

    private int parseSectype(String typeStr) {
        if (typeStr.length() < 1 || typeStr.length() > 2) {
            return 0;
        }
        if (Character.isDigit(typeStr.charAt(0))) {
            typeStr = MdpsTypeMappings.fromNumericType(typeStr);
            if (typeStr == null) {
                return 0;
            }
        }
        switch (typeStr.length()) {
            case 1:
                return MdpsTypeMappings.getMappingForMdpsType(0, typeStr.charAt(0)) & 0xFF;
            case 2:
                return MdpsTypeMappings.getMappingForMdpsType(typeStr.charAt(0), typeStr.charAt(1)) & 0xFF;
            default:
                return 0;
        }
    }

    private void handlePriceData(IqsClients.Client conn, IqsRequest msg) {
        if (!checkRequiredFields(conn, msg, FID_REQUEST_ID, FID_DATA_REQUEST_TYPE, FID_OBJECTNAME)) {
            return;
        }
        final byte requestType = msg.getByte(FID_DATA_REQUEST_TYPE);
        if (requestType != DATA_REQUEST_TYPE_RECAP_AND_UPDATES
                && requestType != DATA_REQUEST_TYPE_RECAP) {
            sendDataStatusResponse(conn, DATA_STATUS_INVALID_REQUEST, msg, INVALID_UPDATE_MODE);
            return;
        }

        final ByteString name = msg.getString(FID_OBJECTNAME);
        final IqsFeedData fd = getFeedData(name);
        if (fd == null) {
            lookup(conn, msg, name);
            return;
        }

        final byte[] requestId = msg.getBytes(FID_REQUEST_ID);
        if (requestId != null && requestId.length > Byte.MAX_VALUE) {
            sendDataStatusResponse(conn, DATA_STATUS_INTERNAL_ERROR, msg, REQUEST_ID_TOO_LONG);
            return;
        }
        final byte[] userData = msg.getBytes(FID_USERDATA);
        if (userData != null && userData.length > Byte.MAX_VALUE) {
            sendDataStatusResponse(conn, DATA_STATUS_INTERNAL_ERROR, msg, USER_DATA_TOO_LONG);
            return;
        }

        final EntitledFieldGroup[] fgs = getEntitledFieldGroups(fd);
        if (fgs == null && !withUnentitledFields(userData)) {
            sendDataStatusResponse(conn, DATA_STATUS_NO_PERMISSION, msg,
                    format("Symbol %s is not permissioned.", name));
            return;
        }

        if (requestType == DATA_REQUEST_TYPE_RECAP) {
            sendRecap(conn, fd, requestId, userData);
            return;
        }

        boolean success;
        synchronized (fd) {
            success = subscribe(conn, msg, fd, requestId, userData);
        }
        if (!success) {
            sendDataStatusResponse(conn, DATA_STATUS_ALREADY_REQUESTED, msg,
                    format("Symbol %s already monitored.", name));
        }
    }

    private void lookup(final IqsClients.Client conn, final IqsRequest msg, final ByteString name) {
        final int vwdcodeStart = isKeyWithPrefix(name) ? 2 : 0;
        final int comma = name.indexOf(',');
        if (comma > 0) { // see R-37830
            final FeedData fd = getFeedData(name.substring(vwdcodeStart, comma));
            if (fd != null) {
                sendLookupResult(conn, msg, name, fd.getVwdcode());
                return;
            }
        }

        if (name.indexOf('.') >= 0) {
            sendLookupResult(conn, msg, name, null);
            return;
        }

        if (!this.lookupClient.submit(new Runnable() {
            @Override
            public void run() {
                final ByteString symbol = (vwdcodeStart != 0) ? name.substring(vwdcodeStart) : name;
                sendLookupResult(conn, msg, name, lookupClient.lookup(symbol));
            }
        })) {
            sendLookupResult(conn, msg, name, null);
        }
    }

    private boolean isKeyWithPrefix(ByteString name) {
        switch (this.quality) {
            case 2:
                return name.startsWith(DELAY_PREFIX);
            case 3:
                return name.startsWith(EOD_PREFIX);
            default:
                return false;
        }
    }

    private void sendLookupResult(IqsClients.Client conn, IqsRequest msg, ByteString name,
            ByteString newName) {
        if (newName == null) {
            sendDataStatusResponse(conn, DATA_STATUS_NOT_FOUND, msg, OBJECT_NOT_FOUND);
        }
        else {
            sendRenameResponse(conn, msg, name, newName);
        }
    }

    private boolean subscribe(IqsClients.Client conn, IqsRequest msg, IqsFeedData fd,
            byte[] requestId,
            byte[] userData) {
        assert Thread.holdsLock(fd);
        this.subscriptions.reset(fd.getSubscriptions());
        if (this.subscriptions.hasSubscriptionFor(conn.getId())) {
            return false;
        }
        final byte[] subs = this.subscriptions.addSubscription(conn.getId(), requestId, userData);
        fd.setSubscriptions(subs);
        sendRecapForSubscription(conn, fd);
        return true;
    }

    private boolean release(IqsFeedData fd, IqsClients.Client conn) {
        synchronized (fd) {
            return this.subscriptions.reset(fd.getSubscriptions()).remove(conn.getId());
        }
    }

    private void sendRecapForSubscription(IqsClients.Client conn, IqsFeedData fd) {
        final ByteBuffer bb = this.entitledFieldsBuilder.formatMessage(fd, false);
        conn.appendMessage(this.recapResponseFactory.onSubscribe(fd, this.subscriptions, bb));
    }

    private void sendRecap(IqsClients.Client conn, IqsFeedData fd, byte[] requestId,
            byte[] userData) {
        final boolean withAllFields = withUnentitledFields(userData);
        synchronized (fd) {
            final ByteBuffer bb = this.entitledFieldsBuilder.formatMessage(fd, withAllFields);
            this.subscriptions.reset(requestId, userData);
            conn.appendMessage(this.recapResponseFactory.onRequest(fd, this.subscriptions, bb));
        }
    }

    private void handleRelease(IqsClients.Client conn, IqsRequest msg) {
        if (!conn.isLoggedIn()) {
            sendReleaseResponse(conn, RELEASE_STATUS_INVALID_REQUEST, msg, NOT_LOGGED_ON_REJECTED);
            return;
        }
        if (!checkRequiredFields(conn, msg, FID_SERVICEID, FID_OBJECTNAME)) {
            return;
        }
        final int serviceId = msg.getByte(FID_SERVICEID);
        if (serviceId == SERVICE_ID_PRICE && serviceId == this.serviceId) {
            handlePriceRelease(conn, msg);
        }
        else {
            sendReleaseResponse(conn, RELEASE_STATUS_INVALID_REQUEST, msg, INVALID_SID);
        }
    }

    private void handlePriceRelease(IqsClients.Client conn, IqsRequest msg) {
        if (!checkRequiredFields(conn, msg, FID_OBJECTNAME)) {
            return;
        }
        final IqsFeedData fd = getFeedData(msg);
        if (fd == null) {
            sendReleaseResponse(conn, RELEASE_STATUS_NOT_REQUESTED, msg, OBJECT_NOT_FOUND);
            return;
        }
        if (release(fd, conn)) {
            sendReleaseResponse(conn, RELEASE_STATUS_OK, msg, OBJECT_RELEASED);
        }
        else {
            sendReleaseResponse(conn, RELEASE_STATUS_NOT_REQUESTED, msg, OBJECT_NOT_REQUESTED);
        }
    }

    private byte[] format(final String format, Object... args) {
        return String.format(format, args).getBytes(CP_1252);
    }

    private List<FeedMarket> getMarkets() {
        return (repository != null) ? repository.getMarkets() : Collections.<FeedMarket>emptyList();
    }

    private EntitledFieldGroup[] getEntitledFieldGroups(IqsFeedData fd) {
        synchronized (fd) {
            return fd.getFieldGroups(this.entitlementProvider);
        }
    }

    private IqsFeedData getFeedData(IqsRequest msg) {
        return getFeedData(msg.getString(FID_OBJECTNAME));
    }

    private IqsFeedData getFeedData(final ByteString name) {
        return (IqsFeedData) repository.get(isKeyWithPrefix(name) ? name.substring(2) : name);
    }

    private void sendReleaseResponse(IqsClients.Client conn, byte status,
            IqsRequest msg, byte[] text) {
        this.builder.prepare(MSG_RELEASE_RESPONSE, msg, FID_OBJECTNAME)
                .header(FID_SERVICEID, SERVICE_ID_PRICE)
                .header(FID_DATA_REQUEST_TYPE, DATA_REQUEST_TYPE_RECAP_AND_UPDATES)
                .header(FID_RELEASE_STATUS, status)
                .header(FID_TEXT, text)
                .header(FID_USERDATA, msg);
        conn.appendMessage(builder.build());
    }

    private void sendRenameResponse(IqsClients.Client conn, IqsRequest msg,
            ByteString name, ByteString newName) {
        final byte[] text = format("Name Change: Requested Object=%s; Symbol=%s", name, newName);
        final ByteBuffer bb = prepareStatusResponse(DATA_STATUS_RENAME, msg, text)
                .header(FID_NEWNAME, newName)
                .build();
        conn.appendMessage(bb);
    }

    private void sendDataStatusResponse(IqsClients.Client conn, byte status,
            IqsRequest msg, byte[] text) {
        final ByteBuffer bb = prepareStatusResponse(status, msg, text).build();
        conn.appendMessage(bb);
    }

    private IqsMessageBuilder prepareStatusResponse(byte status, IqsRequest msg, byte[] text) {
        return this.builder.prepare(MSG_DATA_STATUS_RESPONSE, msg, FID_USERDATA, FID_DATA_REQUEST_TYPE, FID_OBJECTNAME)
                .header(FID_SERVICEID, this.serviceId)
                .header(FID_DATA_STATUS, status)
                .header(FID_TEXT, text);
    }

    private void handleHeartbeatRequest(IqsClients.Client conn, IqsRequest msg) {
        if (!conn.isLoggedIn()) {
            rejectMessage(conn, REJECT_ERROR_TYPE_NOT_LOGGED_ON, msg, msg.getMessageBytes());
            return;
        }
        final ByteBuffer bb = this.builder.prepare(MSG_HEARTBEAT_RESPONSE, msg, FID_HB_REQUEST_TIMESTAMP)
                .header(FID_HB_RESPONSE_TIMESTAMP, DTF.print(System.currentTimeMillis()))
                .build();
        conn.appendMessage(bb);
    }

    private boolean checkRequiredFields(IqsClients.Client conn, IqsRequest msg, int... fields) {
        for (int field : fields) {
            if (!msg.hasField(field)) {
                byte[] text = format("Required field %d missing.", field);
                switch (msg.getMessageType()) {
                    case MSG_LOGON_REQUEST:
                        sendLogonResponse(conn, LOGON_STATUS_REQUIRED_FIELD_MISSING, msg, text);
                        break;
                    case MSG_DATA_REQUEST:
                        sendDataStatusResponse(conn, DATA_STATUS_REQUIRED_FIELD_MISSING, msg, text);
                        break;
                    case MSG_RELEASE_REQUEST:
                        sendReleaseResponse(conn, RELEASE_STATUS_REQUIRED_FIELD_MISSING, msg, text);
                        break;
                    default:
                        this.logger.error("<checkRequiredFields> invalid message type " + msg.getMessageType());
                        break;
                }
                return false;
            }
        }
        return true;
    }

}
