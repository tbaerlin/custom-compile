/*
 * ValuationPrice.java
 *
 * Created on 27.10.2010 17:34:44
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.math.BigDecimal;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.data.PriceUtil;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * @author oflege
 */
class ValuationPrice {

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    private final Quote quote;

    private final SnapRecord snapRecord;

    private final PriceRecord priceRecord;

    private final int valuationPriceField;

    private final Price valuationPrice;

    private final Price previousValuationPrice;

    private String source;

    private static final VwdFieldDescription.Field[] PRICE_DATE_FIELDS
            = new VwdFieldDescription.Field[]{
            VwdFieldDescription.ADF_Schluss_Vortagesdatum,
            VwdFieldDescription.ADF_Vorheriges_Datum,
            VwdFieldDescription.ADF_Datum,
            VwdFieldDescription.ADF_Handelsdatum
    };

    ValuationPrice(Quote quote, SnapRecord snapRecord, PriceRecord priceRecord,
            int valuationPriceField, LocalDate maxDate) {
        this.quote = quote;
        this.snapRecord = snapRecord;
        this.priceRecord = priceRecord;
        this.valuationPriceField = valuationPriceField;

        this.valuationPrice = ensureDate(getValuationPriceInternal(maxDate));
        this.previousValuationPrice = getPreviousValuationPriceInternal();
    }

    private Price ensureDate(Price p) {
        if (!p.isDefined() || p.getDate() != null) {
            return p;
        }
        final DateTime date = getValuationPriceDate(); // todo: why not p.getDate() ?

        return new PriceImpl(p.getValue(), p.getVolume(), p.getSupplement(),
                date, PriceQuality.END_OF_DAY);  // todo: take PQ from price ???
    }

    Price getValuationPrice() {
        return this.valuationPrice;
    }

    Price getPreviousValuationPrice() {
        return this.previousValuationPrice;
    }

    private DateTime getValuationPriceDate() {
        for (VwdFieldDescription.Field field : PRICE_DATE_FIELDS) {
            final DateTime date = getDateTime(this.snapRecord.getField(field.id()));
            if (date != null) {
                return date;
            }
        }
        return new DateTime();  // todo: is this correct?!
    }

    private DateTime getDateTime(SnapField date) {
        if (!date.isDefined()) {
            return null;
        }

        try {
            return toDateTime(SnapRecordUtils.getInt(date), 0);
        } catch (Exception e) {
            return null;
        }
    }

    private DateTime toDateTime(int date, int time) {
        return (date > 0 && time >= 0) ? DateUtil.toDateTime(date, time) : null;
    }


    private Price getPreviousValuationPriceInternal() {
        if ("settlement".equals(this.source)) {
            return this.priceRecord.getPreviousSettlement();
        }
        if ("close".equals(this.source) || "nav".equals(this.source)) {
            return this.priceRecord.getPreviousClose();
        }
        // todo: any more that we can use?
        return NullPrice.INSTANCE;
    }

    private Price getValuationPriceInternal(LocalDate maxDate) {
        final InstrumentTypeEnum type = this.quote.getInstrument().getInstrumentType();
        if (type == InstrumentTypeEnum.OPT || type == InstrumentTypeEnum.FUT) {
            final Price settlement = this.priceRecord.getSettlement();
            if (settlement.isDefined()) {
                this.source = "settlement";
                return settlement;
            }
        }

        if ("BONDS".equals(this.quote.getSymbolVwdfeedMarket())) {
            final Price yield = this.priceRecord.getYieldPrice();
            this.source = "yield";
            return PriceUtil.multiply(yield, ONE_HUNDRED); // yield is decimal representation of a percent value, BEW needs the percent value
        }

        final Price price = doGetPrice();

        if (price == NullPrice.INSTANCE) {
            return price;
        }

        if (isMarketToPreferBidForOldLast(this.quote) && price.getDate() != null) {
            // for SWX return current bid price if close price is older
            final Price bid = getBid(this.priceRecord);
            final DateTime date = bid.getDate();

            if (date != null) {
                final boolean weekend = date.getDayOfWeek() == DateTimeConstants.SATURDAY
                        || date.getDayOfWeek() == DateTimeConstants.SUNDAY;

                final boolean afterMaxDate = maxDate != null && date.toLocalDate().isAfter(maxDate);

                // problem remains that with the weekend condition a basically valid bid price is discarded -> feed needs a specific bid/ask date field
                if (!weekend && price.getDate().toLocalDate().isBefore(date.toLocalDate()) && !afterMaxDate) {
                    this.source = "bid";
                    return bid;
                }
            }
        }

        return price;
    }

    private boolean isMarketToPreferBidForOldLast(Quote quote) {
        final String market = quote.getSymbolVwdfeedMarket();
        return "CH".equals(market)
                || "SCO_CH".equals(market)
                || "XCOR".equals(market)
                || "BER".equals(market);
    }

    private Price doGetPrice() {
        final Price close = getClose();
        if (isDefined(close)) {
            this.source = this.priceRecord instanceof PriceRecordFund
                    ? "nav"
                    : "close";
            return close;
        }

        final Price open = this.priceRecord.getOpen();
        if (isDefined(open)) {
            this.source = "open";
            return open;
        }

        final Price kassa = this.priceRecord.getKassa();
        if (isDefined(kassa)) {
            this.source = "auction";
            return kassa;
        }

        final Price bid = getBid(this.priceRecord);
        if (isDefined(bid)) {
            this.source = "bid";
            return bid;
        }

        final Price ask = getAsk(this.priceRecord);
        if (isDefined(ask)) {
            this.source = "ask";
            return ask;
        }

        return NullPrice.INSTANCE;
    }

    static Price getBid(PriceRecord priceRecord) {
        final Price bid = priceRecord.getBid();
        if (isDefined(bid)) {
            return bid;
        }
        return priceRecord.getPreviousBid();
    }

    static Price getAsk(PriceRecord priceRecord) {
        final Price ask = priceRecord.getAsk();
        if (isDefined(ask)) {
            return ask;
        }
        return priceRecord.getPreviousAsk();
    }

    private Price getClose() {
        if (this.valuationPriceField >= 0) {
            return getValuationPriceFromMappingField();
        }

        final Price close = this.priceRecord.getLastClose();
        if (isDefined(close)) {
            return close;
        }

        return getPreviousClose();
    }

    Price getPreviousClose() {
        return this.priceRecord.getPreviousClose();
    }

    private Price getValuationPriceFromMappingField() {
        final SnapField sf = this.snapRecord.getField(this.valuationPriceField);
        if (!sf.isDefined()) {
            return NullPrice.INSTANCE;
        }
        return new PriceImpl(sf.getPrice(), 0L, null,
                getValuationPriceDate(), PriceQuality.END_OF_DAY);
    }

    private static boolean isDefined(Price p) {
        return p != null && p.getValue() != null && p.getValue().signum() != 0;
    }

    public String getSource() {
        return this.source;
    }
}
