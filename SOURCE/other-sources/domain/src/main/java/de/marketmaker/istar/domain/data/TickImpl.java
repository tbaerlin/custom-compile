/*
 * TickImpl.java
 *
 * Created on 08.06.2007 14:19:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;
import java.util.BitSet;
import java.util.List;

import net.jcip.annotations.Immutable;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class TickImpl {
    public enum Type {
        TRADE(TickType.TRADE),
        BID(TickType.BID),
        ASK(TickType.ASK),
        BID_ASK,
        BID_ASK_TRADE,
        YIELD,
        SETTLEMENT,
        SYNTHETIC_TRADE(TickType.SYNTHETIC_TRADE),
        ADDITIONAL_FIELDS;

        private final TickType tickTypeChicago;

        Type() {
            this(null);
        }

        Type(TickType tickTypeChicago) {
            this.tickTypeChicago = tickTypeChicago;
        }

        public static TickImpl.Type valueOf(TickType type) {
            switch (type) {
                case TRADE:
                    return TickImpl.Type.TRADE;
                case BID:
                    return TickImpl.Type.BID;
                case ASK:
                    return TickImpl.Type.ASK;
                case SYNTHETIC_TRADE:
                    return TickImpl.Type.SYNTHETIC_TRADE;
            }
            throw new IllegalArgumentException("unknown type: " + type);
        }

        public TickType getTickTypeChicago() {
            return this.tickTypeChicago;
        }
    }

    private final DateTime dateTime;

    private final BigDecimal price;

    private final long volume;

    private final String supplement;

    private final String tradeIdentifier;

    private final Type type;

    private final List<SnapField> fields;

    public TickImpl(DateTime dateTime, List<SnapField> fields) {
        this(dateTime, null, Long.MIN_VALUE, null, null, Type.ADDITIONAL_FIELDS, fields);
    }

    public TickImpl(DateTime dateTime, BigDecimal price, long volume, String supplement,
            String tradeIdentifier, Type type) {
        this(dateTime, price, volume, supplement, tradeIdentifier, type, null);
    }

    public TickImpl(DateTime dateTime, BigDecimal price, long volume, String supplement,
            String tradeIdentifier, Type type, List<SnapField> fields) {
        this.dateTime = dateTime;
        this.price = price;
        this.volume = volume;
        this.supplement = supplement;
        this.tradeIdentifier = tradeIdentifier;
        this.type = type;
        this.fields = fields;
    }

    public DateTime getDateTime() {
        return dateTime;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Long getVolume() {
        if (volume == Long.MIN_VALUE) {
            return null;
        }
        return volume;
    }

    public String getSupplement() {
        return supplement;
    }

    public String getTradeIdentifier() {
        return tradeIdentifier;
    }

    public Type getType() {
        return type;
    }

    public boolean hasVolume() {
        return this.volume != Long.MIN_VALUE;
    }

    public long volume() {
        return this.volume;
    }

    public boolean hasFields() {
        return this.fields != null;
    }

    public List<SnapField> getFields() {
        return this.fields;
    }

    public static boolean hasTickType(Type tickType, TickList.FieldPermissions permissions,
            TickEvent data) {
        switch (tickType) {
            case TRADE:
                return data.isTrade();
            case BID:
                return data.isBid();
            case ASK:
                return data.isAsk();
            case BID_ASK:
                return (data.isAsk() && (permissions == null || permissions.ask))
                        || (data.isBid() && (permissions == null || permissions.bid));
            case BID_ASK_TRADE:
                return (data.isAsk() && (permissions == null || permissions.ask))
                        || (data.isBid() && (permissions == null || permissions.bid))
                        || (data.isTrade() && (permissions == null || permissions.trade));
            case SYNTHETIC_TRADE:
                return data.isAsk() || data.isBid();
            default:
                return false;
        }
    }

    public static boolean hasTickType(Type tickType, TickList.FieldPermissions permissions,
            TickEvent data, BitSet additionalFieldIds) {
        final boolean hasTickType = hasTickType(tickType, permissions, data);
        if (hasTickType || additionalFieldIds == null) {
            return hasTickType;
        }

        final List<SnapField> fields = data.getAdditionalFields();
        if (fields != null) {
            for (SnapField field : fields) {
                if (additionalFieldIds.get(field.getId())) {
                    return true;
                }
            }
        }
        return false;
    }
}
