/*
 * SnapWrapper.java
 *
 * Created on 19.10.2005 13:12:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import de.marketmaker.istar.domain.data.NullSnapRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.instrument.ContentFlagsDp2;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapWrapper {
    public final static SnapWrapper NULL = new SnapWrapper() {
        public boolean isPriceBased() {
            return false;
        }

        public boolean isBidAskBased() {
            return false;
        }

        public boolean isBidAskOnlyBased() {
            return false;
        }

        public boolean isKagFonds() {
            return false;
        }

        public long getPrice() {
            return Constants.NOT_DEFINED_LONG;
        }

        public int getPriceTime() {
            return Constants.NOT_DEFINED_INT;
        }

        public int getPriceDate() {
            return Constants.NOT_DEFINED_INT;
        }

        public long getBid() {
            return Constants.NOT_DEFINED_LONG;
        }

        public int getBidTime() {
            return Constants.NOT_DEFINED_INT;
        }

        public int getBidDate() {
            return Constants.NOT_DEFINED_INT;
        }

        public long getAsk() {
            return Constants.NOT_DEFINED_LONG;
        }

        public int getAskTime() {
            return Constants.NOT_DEFINED_INT;
        }

        public int getAskDate() {
            return Constants.NOT_DEFINED_INT;
        }

        public long getPreviousClose() {
            return Constants.NOT_DEFINED_LONG;
        }

        public long getLastPrice() {
            return Constants.NOT_DEFINED_LONG;
        }

        public long getDifferenceRelative() {
            return Constants.NOT_DEFINED_LONG;
        }

        public long getDifference() {
            return Constants.NOT_DEFINED_LONG;
        }

        public long getPriceEscaped() {
            return Constants.NOT_DEFINED_LONG;
        }

        public int getTimeEscaped() {
            return Constants.NOT_DEFINED_INT;
        }

        public SnapRecord getSnapRecord() {
            return NullSnapRecord.INSTANCE;
        }
    };

    private final SnapRecord sr;

    private boolean kagFonds;

    private boolean priceBased = false;

    private boolean bidAskBased = false;

    private String currency;

    private SnapWrapper() {
        this.sr = null;
    }

    public static SnapWrapper create(SnapRecord sr, Quote quote) {
        return (sr != null) ? new SnapWrapper(sr, quote) : SnapWrapper.NULL;
    }

    private SnapWrapper(SnapRecord sr, Quote quote) {
        this.sr = sr;

        final SnapField trade = this.sr.getField(VwdFieldDescription.ADF_Bezahlt.id());
        final SnapField previousClose = this.sr.getField(VwdFieldDescription.ADF_Schluss_Vortag.id());

        this.priceBased = (trade.isDefined() && SnapRecordUtils.getLong(trade) > 0)
                || (previousClose.isDefined() && SnapRecordUtils.getLong(previousClose) > 0);

        final SnapField bid = this.sr.getField(VwdFieldDescription.ADF_Geld.id());
        final SnapField ask = this.sr.getField(VwdFieldDescription.ADF_Brief.id());

        this.bidAskBased = (bid.isDefined() && SnapRecordUtils.getLong(bid) > 0)
                || (ask.isDefined() && SnapRecordUtils.getLong(ask) > 0);

        if (quote != null) {
            this.kagFonds = quote.getInstrument().getInstrumentType() == InstrumentTypeEnum.FND
                    && InstrumentUtil.isVwdFund(quote);
            this.currency = quote.getCurrency().getSymbolIso();
        }
    }

    public SnapRecord getSnapRecord() {
        return this.sr;
    }

    public boolean isKagFonds() {
        return kagFonds;
    }

    public String getCurrency() {
        return currency;
    }

    public boolean isPriceBased() {
        return priceBased;
    }

    public boolean isBidAskBased() {
        return bidAskBased;
    }

    public boolean isBidAskOnlyBased() {
        return this.bidAskBased && !this.priceBased;
    }

    private long getPriceBasedField(final int fieldid) {
        if (!isPriceBased()) {
            return Constants.NOT_DEFINED_LONG;
        }

        return SnapRecordUtils.getLong(this.sr.getField(fieldid));
    }

    private long getBidAskBasedField(final int fieldid) {
        if (!isBidAskBased()) {
            return Constants.NOT_DEFINED_LONG;
        }

        return SnapRecordUtils.getLong(this.sr.getField(fieldid));
    }

    public long getPrice() {
        if (isKagFonds()) {
            return getPrice(VwdFieldDescription.ADF_NAV, VwdFieldDescription.ADF_Ruecknahme);
        }
        return getPriceBasedField(VwdFieldDescription.ADF_Bezahlt.id());
    }

    private long getPrice(final VwdFieldDescription.Field first,
            final VwdFieldDescription.Field second) {
        final long nav = SnapRecordUtils.getLong(this.sr.getField(first.id()));
        if (nav != Constants.NOT_DEFINED_LONG) {
            return nav;
        }
        return getPriceBasedField(second.id());
    }


    public int getPriceTime() {
        return SnapRecordUtils.getTradeTime(this.sr);
    }

    public int getPriceDate() {
        final int mmfBezahltDatum = SnapRecordUtils.getInt(this.sr.getField(VwdFieldDescription.MMF_Bezahlt_Datum.id()));
        if (mmfBezahltDatum > 0) {
            return mmfBezahltDatum;
        }

        if (getPrice() > 0) {
            return SnapRecordUtils.getInt(this.sr.getField(VwdFieldDescription.ADF_DATEOFARR.id()));
        }

        final int schlussVortagesDatum = SnapRecordUtils.getInt(this.sr.getField(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id()));
        if (schlussVortagesDatum > 0) {
            return schlussVortagesDatum;
        }

        return Constants.NOT_DEFINED_INT;
    }

    public long getBid() {
        return getBidAskBasedField(VwdFieldDescription.ADF_Geld.id());
    }

    public int getBidTime() {
        if (getBid() > 0) {
            return SnapRecordUtils.getTime(this.sr);
        }
        return Constants.NOT_DEFINED_INT;
    }

    public int getBidDate() {
        if (getBid() > 0) {
            return SnapRecordUtils.getInt(this.sr.getField(VwdFieldDescription.ADF_DATEOFARR.id()));
        }
        return Constants.NOT_DEFINED_INT;
    }

    public long getAsk() {
        return getBidAskBasedField(VwdFieldDescription.ADF_Brief.id());
    }

    public long getPreviousClose() {
        if (isKagFonds()) {
            return getPrice(VwdFieldDescription.ADF_NAV_Vortag, VwdFieldDescription.ADF_Ruecknahme_Vortag);
        }
        return getPriceBasedField(VwdFieldDescription.ADF_Schluss_Vortag.id());
    }

    public long getLastPrice() {
        final long price = getPrice();
        if (price > 0) {
            return price;
        }

        return getPreviousClose();
    }

    public long getDifferenceRelative() {
        if (isBidAskOnlyBased()) {
            return Constants.NOT_DEFINED_LONG;
        }

        final long diff = getDifference();
        if (diff == Constants.NOT_DEFINED_LONG) {
            return Constants.NOT_DEFINED_LONG;
        }

        final long previousClose = getPreviousClose();

        if (previousClose == Constants.NOT_DEFINED_LONG || previousClose == 0) {
            return Constants.NOT_DEFINED_LONG;
        }

        if (previousClose > 0L) {
            return (diff * Constants.SCALE_FOR_DECIMAL) / previousClose;
        }

        final long prepreviousClose = getPrePreviousClose();

        if (prepreviousClose == Constants.NOT_DEFINED_LONG || prepreviousClose == 0) {
            return Constants.NOT_DEFINED_LONG;
        }

        return (diff * Constants.SCALE_FOR_DECIMAL) / prepreviousClose;
    }

    public long getDifference() {
        if (isBidAskOnlyBased()) {
            return Constants.NOT_DEFINED_LONG;
        }

        final long price = getPrice();
        final long previousClose = getPreviousClose();

        if (previousClose == Constants.NOT_DEFINED_LONG) {
            return Constants.NOT_DEFINED_LONG;
        }

        if (price > 0L) {
            return price - previousClose;
        }

        final long prepreviousClose = getPrePreviousClose();

        if (prepreviousClose == Constants.NOT_DEFINED_LONG) {
            return Constants.NOT_DEFINED_LONG;
        }

        return previousClose - prepreviousClose;
    }

    private long getPrePreviousClose() {
        if (isKagFonds()) {
            return getPrice(VwdFieldDescription.MMF_NAV_Vorvortag, VwdFieldDescription.MMF_Schluss_Vorvortag);
        }
        return getPriceBasedField(VwdFieldDescription.MMF_Schluss_Vorvortag.id());
    }

    public long getPriceEscaped() {
        final long lastprice = getLastPrice();
        if (lastprice != Constants.NOT_DEFINED_LONG) {
            return lastprice;
        }

        return getBidAskPrice();
    }

    private long getBidAskPrice() {
        final long bid = getBid();
        final long ask = getAsk();

        if (bid != Constants.NOT_DEFINED_LONG && ask != Constants.NOT_DEFINED_LONG) {
            if (bid == 0) {
                return ask;
            }
            if (ask == 0) {
                return bid;
            }

            return (bid + ask) / 2;
        }

        if (bid == Constants.NOT_DEFINED_LONG && ask != Constants.NOT_DEFINED_LONG) {
            return ask;
        }

        if (bid != Constants.NOT_DEFINED_LONG) {
            return bid;
        }

        return Constants.NOT_DEFINED_LONG;
    }

    public int getIssueDate() {
        return SnapRecordUtils.getInt(this.sr.getField(VwdFieldDescription.ADF_Auflagedatum.id()));
    }

    public ContentFlags getContentFlags() {
        final String contentFlags = SnapRecordUtils.getString(this.sr.getField(VwdFieldDescription.ADF_ContentFlags.id()));
        return contentFlags==null?ContentFlagsDp2.NO_FLAGS_SET:new ContentFlagsDp2(contentFlags);
    }

    public String toString() {
        return "SnapWrapper[snapRecord=" + this.sr
                + ", isPriceBased=" + this.isPriceBased()
                + ", isBidAskBased=" + this.isBidAskBased()
                + "]";
    }
}
