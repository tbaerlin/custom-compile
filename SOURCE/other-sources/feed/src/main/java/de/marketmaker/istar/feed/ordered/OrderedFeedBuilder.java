/*
 * OrderedFeedBuilder.java
 *
 * Created on 28.08.12 14:52
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.FeedBuilder;
import de.marketmaker.istar.feed.FeedData;
import de.marketmaker.istar.feed.FeedUpdateFlags;
import de.marketmaker.istar.feed.OrderedFieldBuilder;
import de.marketmaker.istar.feed.ParsedRecord;
import de.marketmaker.istar.feed.TickTypeChecker;
import de.marketmaker.istar.feed.mdps.MdpsFeedUtils;
import de.marketmaker.istar.feed.vwd.VwdFeedConstants;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.decodeDate;
import static de.marketmaker.istar.feed.DateTimeProvider.Timestamp.decodeTime;
import static de.marketmaker.istar.feed.FeedUpdateFlags.*;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.ID_ADF_HANDELSDATUM;

/**
 * Creates feed records that are ordered by order-id instead of vwd-field-id. In addition to
 * that, a number of flags are set based on the content of the record to simplify processing in
 * backend components.
 *
 * Also we set derived values for MMF_BEZAHLT_DATUM and MMF_BOERSENZEIT
 *
 * @author oflege
 */
abstract class OrderedFeedBuilder extends FieldDataBuilder implements FeedBuilder, OrderedFieldBuilder {

    private static final Set<ByteString> MARKETS_WITH_QUELLE = new HashSet<>(
            Arrays.asList(new ByteString("A"), new ByteString("N"), new ByteString("Q"))
    );

    private static final int TEN_PAST_MIDNIGHT = 600;

    private static final int TEN_BEFORE_MIDNIGHT = 86400 - 600;

    private static final int NO_TIME = -1;

    private static final int NO_TICKS_MASK = 0xFFFF
            - FeedUpdateFlags.FLAG_WITH_TRADE
            - FeedUpdateFlags.FLAG_WITH_ASK
            - FeedUpdateFlags.FLAG_WITH_BID
            - FeedUpdateFlags.FLAG_WITH_TICK_FIELD;

    public static final byte[] APPLICABLE_MESSAGE_TYPES = new byte[]{
            VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE,
            VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_RECAP,
            VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_DELETE,
            VwdFeedConstants.MESSAGE_TYPE_STATIC_DELETE,
            VwdFeedConstants.MESSAGE_TYPE_STATIC_UPDATE,
            VwdFeedConstants.MESSAGE_TYPE_STATIC_RECAP,
            VwdFeedConstants.MESSAGE_TYPE_DELETE_FIELDS,
            VwdFeedConstants.MESSAGE_TYPE_RATIOS,
    };

    // use an int as a BitSet for faster detection of trade dates
    @SuppressWarnings("PointlessArithmeticExpression")
    private static final int TRADE_DATES = 0
            + (1 << VwdFieldOrder.ORDER_ADF_DATUM_QUOTIERUNG)
            + (1 << VwdFieldOrder.ORDER_ADF_DATUM)
            + (1 << VwdFieldOrder.ORDER_ADF_HANDELSDATUM)
            + (1 << VwdFieldOrder.ORDER_ADF_BEZAHLT_DATUM);

    private static final int NON_DYNAMIC_FIELD = VwdFieldDescription.FLAG_STATIC + VwdFieldDescription.FLAG_RATIO;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private boolean withTrade;

    private int yesterday;

    private int messageDate;

    private int messageTime;

    private int tickDate = 0;

    private int tickTime = 0;

    private int mmfBezahltDatum = 0;

    private final int fieldFlags;

    private boolean withSourceId = false;

    public OrderedFeedBuilder() {
        this(0);
    }

    public OrderedFeedBuilder(int fieldFlags) {
        super(8192);
        this.fieldFlags = fieldFlags;
    }

    public void setWithSourceId(boolean withSourceId) {
        this.withSourceId = withSourceId;
    }

    @Override
    public byte[] getApplicableMessageTypes() {
        return APPLICABLE_MESSAGE_TYPES;
    }

    /**
     * Puts this update into <code>this.bb</code>. When this method returns, the buffer starts
     * with a short that contains the length of the message and the buffer is limited at the
     * end of the message.
     * @param data contains the data's key, market, etc.
     * @param pr contains parsed fields
     */
    protected final void doProcess(FeedData data, ParsedRecord pr) {
        this.bb.clear();
        this.bb.position(2);  // space for the length

        final ByteString vwdcode = data.getVwdcode();
        this.bb.put((byte) vwdcode.hashCode());
        vwdcode.writeTo(this.bb, ByteString.LENGTH_ENCODING_BYTE);

        this.bb.put(pr.getMessageTypeMdps());
        this.bb.put(pr.getMessageType());
        this.bb.putShort((short) pr.getKeyType());

        final int messageTimestamp = pr.getMessageTimestamp();
        // ack before getFlags(...) below reads messageDate
        ackMessageTimestamp(messageTimestamp);

        final int flagPosition = this.bb.position();
        int flags = getFlags(data, pr);
        this.bb.putShort((short) flags);

        this.bb.putInt(messageTimestamp);

        this.lastOrder = 0;
        this.tickDate = 0;
        this.tickTime = NO_TIME;
        this.mmfBezahltDatum = this.messageDate;

        setFields(data, pr);

        if (flags != 0) {
            final int newFlags = postProcessFlags(flags);
            if (newFlags != flags) {
                this.bb.putShort(flagPosition, (short) newFlags);
            }
        }

        this.bb.flip();
        this.bb.putShort(0, (short) this.bb.remaining());
    }

    protected void setFields(FeedData data, ParsedRecord pr) {
        if (this.fieldFlags == 0) {
            pr.setAllFields(this);
        }
        else {
            pr.setFields(this, this.fieldFlags);
        }
    }

    protected void ackMessageTimestamp(int messageTimestamp) {
        final int tmp = decodeDate(messageTimestamp);
        if (tmp != this.messageDate) {
            this.messageDate = tmp;
            this.yesterday = DateUtil.toYyyyMmDd(DateUtil.yyyyMmDdToLocalDate(tmp).minusDays(1));
        }
        this.messageTime = decodeTime(messageTimestamp);
    }

    private int postProcessFlags(int flags) {
        if (this.withSourceId) {
            return flags;
        }
        if (this.tickTime == NO_TIME) { // not < 0 as mdps times >= 16:00:00 are negative
            return flags & NO_TICKS_MASK; // no time, clear flags...
        }
        if (this.tickDate != 0 && this.tickDate != this.messageDate) {
            if (this.tickDate == this.yesterday && isLateTickForYesterday()) {
                return flags | FeedUpdateFlags.FLAG_YESTERDAY;
            }
            return flags & NO_TICKS_MASK; // not today, clear flags...
        }
        if (isLateTickForYesterday()) {
            return flags | FeedUpdateFlags.FLAG_YESTERDAY;
        }
        return flags;
    }

    private boolean isLateTickForYesterday() {
        final int tt = MdpsFeedUtils.toSecondOfDay(this.tickTime);
        return tt > TEN_BEFORE_MIDNIGHT && this.messageTime < TEN_PAST_MIDNIGHT;
    }

    private int getFlags(FeedData data, ParsedRecord pr) {
        if (this.withSourceId) {
            return pr.getSourceId();
        }

        final int result = computeFlags(data, pr);
        this.withTrade = (result & FLAG_WITH_TRADE) != 0;
        return result;
    }

    private int computeFlags(FeedData data, ParsedRecord pr) {
        int result = 0;

        if (pr.isWithFieldFlags(NON_DYNAMIC_FIELD)) {
            result |= FLAG_WITH_NON_DYNAMIC_FIELD;
        }

        if (pr.isFieldPresent(VwdFieldDescription.ID_ADF_SCHLUSS_VORTAGESDATUM)) {
            result |= FLAG_WITH_CLOSE_DATE_YESTERDAY;
        }

        if (pr.getMessageType() == VwdFeedConstants.MESSAGE_TYPE_DYNAMIC_UPDATE) {
            final TickTypeChecker checker = data.getMarket().getTickTypeChecker();
            result |= checker.getTickFlags(pr);
            if ((result & FLAG_PROFESSIONAL_TRADE) != 0) {
                return result;
            }

            final long hd = (int) pr.getNumericValue(ID_ADF_HANDELSDATUM);
            if (isWithOldHandelsdatum(hd)) {
                return FLAG_WITH_OLD_HANDELSDATUM;
            }

            if (isWithQuelle(data, pr)) {
                result |= FLAG_WITH_QUELLE;
            }
        }
        return result;
    }

    private boolean isWithOldHandelsdatum(long hd) {
        return hd > 0 && hd < this.messageDate;
    }

    private boolean isWithQuelle(FeedData data, ParsedRecord pr) {
        if (!pr.isFieldPresent(VwdFieldDescription.ID_ADF_QUELLE)) {
            return false;
        }
        final ByteString marketName = data.getMarket().getName();
        return (marketName.length() == 1) && MARKETS_WITH_QUELLE.contains(marketName);
    }

    @Override
    public void setTime(int orderId, int value) {
        doSetTime(orderId, value);
        switch (orderId) {
            case VwdFieldOrder.ORDER_ADF_ZEIT:
                this.tickTime = value;
                break;
            case VwdFieldOrder.ORDER_ADF_BOERSENZEIT:
                this.tickTime = value;
                break;
            case VwdFieldOrder.ORDER_ADF_ZEIT_QUOTIERUNG:
                if (this.lastOrder != VwdFieldOrder.ORDER_ADF_BOERSENZEIT) {
                    this.tickTime = value;
                }
                break;
        }
    }

    private void doSetTime(int orderId, int value) {
        putTimeFid(orderId);
        this.bb.putInt(value);
    }

    @Override
    public void setInt(int orderId, int value) {
        doSetInt(orderId, value);

        if (isTradeDate(orderId)) {
            this.tickDate = value;
            if (this.withTrade && orderId != VwdFieldOrder.ORDER_ADF_DATUM_QUOTIERUNG) {
                // any trade date besides ADF_DATUM_QUOTIERUNG is used as MMF_BEZAHLT_DATUM
                this.mmfBezahltDatum = value;
            }
        }
    }

    private boolean isTradeDate(int oid) {
        return (oid < 32) && ((1 << oid) & TRADE_DATES) != 0;
    }

    private void doSetInt(int orderId, int value) {
        putIntFid(orderId);
        this.bb.putInt(value);
    }

    @Override
    public void setPrice(int orderId, long value) {
        putPriceFid(orderId);
        MdpsFeedUtils.putMdpsPrice(this.bb, value);
        // hack to set MMF_BEZAHLT_DATUM/MMF_BOERSENZEIT
        if (this.withTrade && orderId == VwdFieldOrder.ORDER_ADF_BEZAHLT && this.tickTime != NO_TIME) {
            doSetTime(VwdFieldOrder.ORDER_MMF_BOERSENZEIT, this.tickTime);
            doSetInt(VwdFieldOrder.ORDER_MMF_BEZAHLT_DATUM, this.mmfBezahltDatum);
        }
    }

    @Override
    public void setString(int orderId, byte[] value, int start, int length) {
        putStringFid(orderId);
        putStopBitEncoded(length);
        this.bb.put(value, start, length);
    }
}
