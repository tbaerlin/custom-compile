/*
 * InstrumentAllocationImpl.java
 *
 * Created on 12.08.2006 14:55:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import java.io.Serializable;
import java.math.BigDecimal;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.MasterDataFund;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class InstrumentAllocationImpl implements Serializable, InstrumentAllocation {
    protected static final long serialVersionUID = 1L;

    private final Type type;

    private ShareType shareType;

    private final Long id;

    private final String category;

    private BigDecimal share;

    private BigDecimal longPosition;

    private BigDecimal shortPosition;

    private final String isin;

    private final String elementCode;

    private final DateTime lastUpdate;

    private MasterDataFund.Source source;

    public InstrumentAllocationImpl(Type type, String category) {
        this(type, null, category,null);
    }

    public InstrumentAllocationImpl(Type type, String category, BigDecimal share) {
        this(type, null, category, share);
    }

    public InstrumentAllocationImpl(Type type, Long id, String category, BigDecimal share) {
        this(type, id, category, share, null, null, null, null, null);
    }

    public InstrumentAllocationImpl(Type type, Long id, String category, BigDecimal share, String isin, String elementCode, DateTime lastUpdate) {
        this(type, id, category, share, null, null, isin, elementCode, lastUpdate);
    }

    public InstrumentAllocationImpl(Type type, Long id, String category, BigDecimal share,
            BigDecimal longPosition, BigDecimal shortPosition, String isin, String elementCode, DateTime lastUpdate) {
        this.type = type;
        this.id = id;
        this.category = category;
        this.share = share;
        this.longPosition = longPosition;
        this.shortPosition = shortPosition;
        this.isin = isin;
        this.elementCode = elementCode;
        this.lastUpdate = lastUpdate;
    }

    public InstrumentAllocation withLongPosition(BigDecimal longPosition) {
        this.longPosition = longPosition;
        return this;
    }

    public InstrumentAllocation withShortPosition(BigDecimal shortPosition) {
        this.shortPosition = shortPosition;
        return this;
    }

    public InstrumentAllocation withSharePosition(BigDecimal share, ShareType shareType) {
        this.share = share;
        this.shareType = shareType;
        return this;
    }

    public InstrumentAllocation withSource(MasterDataFund.Source source) {
        this.source = source;
        return this;
    }

    public void finish() {
        if (this.share != null) {
            return;
        }

        final BigDecimal l = this.longPosition != null ? this.longPosition : BigDecimal.ZERO;
        final BigDecimal s = this.shortPosition != null ? this.shortPosition : BigDecimal.ZERO;
        this.share = l.subtract(s);
        this.shareType = ShareType.CONSOLIDATED;
    }

    public Type getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getCategory() {
        return category;
    }

    public BigDecimal getShare() {
        return share;
    }

    public ShareType getShareType() {
        return shareType;
    }

    public BigDecimal getLongPosition() {
        return longPosition;
    }

    public void setLongPosition(BigDecimal longPosition) {
        this.longPosition = longPosition;
    }

    public BigDecimal getShortPosition() {
        return shortPosition;
    }

    @Override
    public String getIsin() {
        return this.isin;
    }

    @Override
    public String getElementCode() {
        return elementCode;
    }

    @Override
    public DateTime getLastUpdate() {
        return this.lastUpdate;
    }

    @Override
    public MasterDataFund.Source getSource() {
        return source;
    }

    public void setShortPosition(BigDecimal shortPosition) {
        this.shortPosition = shortPosition;
    }

    public String toString() {
        return "InstrumentAllocationImpl[type=" + type.name()
                + ", id=" + id
                + ", category=" + category
                + ", share=" + share
                + ", shareType=" + shareType
                + ", longPosition=" + longPosition
                + ", shortPosition=" + shortPosition
                + ", source=" + source
                + "]";
    }
}
