/*
 * PriceRecordVwd.java
 *
 * Created on 10.07.2006 12:46:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.FieldTypeEnum;
import de.marketmaker.istar.domain.data.NullSnapField;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domainimpl.data.NullPrice;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;

import static de.marketmaker.istar.feed.mdps.MdpsPriceUtils.isInvalidAsk;
import static de.marketmaker.istar.feed.snap.SnapRecordUtils.*;
import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceRecordVwd implements Serializable, PriceRecord {
    protected final static Logger logger = LoggerFactory.getLogger(PriceRecordVwd.class);

    protected static final long serialVersionUID = 2L;

    protected final long quoteid;

    protected final SnapRecord sr;

    protected final PriceQuality priceQuality;

    private final boolean pushAllowed;

    private final BigDecimal subscriptionRatio;

    private final boolean isForward;

    protected boolean currentDayDefined;

    protected static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    protected static final MathContext MC = new MathContext(8, RoundingMode.HALF_EVEN);

    // transient fields used as caches for data that is likely to be queried multiple times:

    private transient Price price;

    private transient Price ask;

    private transient Price bid;

    private transient Price pclose;

    private transient int today;

    /**
     * Fields to be examined in this order to find the most recent defined price
     */
    private static final int[] LAST_PRICE_FIELDS = new int[]{
            ADF_Bezahlt.id(),
            ADF_Schluss.id(),
            ADF_Schluss_Vortag.id(),
            MMF_Schluss_Vorvortag.id()
    };

    /**
     * Fields with dates corresponding to the prices in LAST_PRICE_FIELDS
     */
    private static final int[] LAST_DATE_FIELDS = new int[]{
            MMF_Bezahlt_Datum.id(),
            ADF_Schluss_Datum.id(),
            ADF_Schluss_Vortagesdatum.id(),
            MMF_Schluss_Vorvortagesdatum.id()
    };

    private static final int[] CLOSE_BEFORE_DATES = new int[]{
            ADF_Schluss_Vortagesdatum.id(), VwdFieldDescription.MMF_Schluss_Vorvortagesdatum.id()
    };

    private static final int[] CLOSE_BEFORE_PRICES = new int[]{
            ADF_Schluss_Vortag.id(), MMF_Schluss_Vorvortag.id()
    };

    public PriceRecordVwd(long quoteid, SnapRecord sr, PriceQuality priceQuality,
            boolean pushAllowed, BigDecimal subscriptionRatio, boolean isForward) {
        this.quoteid = quoteid;
        this.sr = sr;
        this.priceQuality = priceQuality;
        this.pushAllowed = pushAllowed;
        this.subscriptionRatio = subscriptionRatio;
        this.isForward = isForward;
        initToday();
        this.currentDayDefined = getCurrentDayDefined();
        initCaches();
    }

    protected boolean getCurrentDayDefined() {
        // TODO: check for data w/o Bezahlt before & after midnight
        final boolean currentDayDefined = isDefinedAndNotZero(ADF_Bezahlt) || getInt(ADF_Handelsdatum) == this.today;
        final boolean zeroPriceWithDate = isDefined(ADF_Bezahlt) && isDefinedAndNotZero(ADF_Bezahlt_Datum);
        if (!currentDayDefined && zeroPriceWithDate) {
            logger.warn("Current day is not defined, but price = 0 and date != 0 for " + this.quoteid);
        }

        return currentDayDefined;
    }

    private Object readResolve() {
        initToday();
        initCaches();
        return this;
    }

    private void initToday() {
        if (this.today == 0) {
            this.today = DateUtil.dateToYyyyMmDd();
        }
    }

    protected void initCaches() {
        this.price = getPrice();
        this.bid = doGetBid();
        this.ask = doGetAsk();
        this.pclose = getPreviousClose();
    }

    public PriceQuality getPriceQuality() {
        return priceQuality;
    }

    public boolean isPushAllowed() {
        return this.pushAllowed;
    }

    public boolean isCurrentDayDefined() {
        return currentDayDefined;
    }

    public String toString() {
        return new StringBuilder(200)
                .append(ClassUtils.getShortName(getClass()))
                .append("[price=").append(this.price)
                .append(", bid=").append(this.bid)
                .append(", ask=").append(this.ask)
                .append(", pclose=").append(this.pclose)
                .append(']').toString();
    }

    public DateTime getDate() {
        return DateUtil.max(getSettlement().getDate(), DateUtil.max(DateUtil.max(getPrice().getDate(), getBid().getDate()), getAsk().getDate()));
    }

    public final Price getPrice() {
        // TODO: handle EndOfDay PriceQuality
        // TODO: forwards can have valid prices with value 0
        if (this.price == null) {
            this.price = doGetPrice();
        }
        return this.price;
    }

    protected Price doGetPrice() {
        final SnapField price;
        final SnapField volume;
        final SnapField supplement;
        DateTime priceDate;

        if (this.currentDayDefined) {
            final SnapField close = getField(ADF_Schluss);
            final int closeDate = getInt(ADF_Schluss_Datum);
            final int mmfDate = getInt(MMF_Bezahlt_Datum);
            if (isDefinedAndNotZero(close) && closeDate >= mmfDate) {
                price = close;
                volume = getField(ADF_Schluss_Umsatz);
                supplement = getField(ADF_Schluss_Kurszusatz);
            }
            else {
                price = getField(ADF_Bezahlt);
                volume = getField(ADF_Bezahlt_Umsatz);
                supplement = getField(ADF_Bezahlt_Kurszusatz);
            }
            priceDate = getPriceDate();
        }
        else {
            return getPreviousClose();
        }

        return createPrice(price, volume, supplement, priceDate);
    }

    // TODO: this is a complete mess, we end up with previousCloseDate + TimeOfArrival etc...
    private DateTime getPriceDate() {
        final int handelsdatumDate = getInt(ADF_Handelsdatum);

        int date = getInt(MMF_Bezahlt_Datum);

        int time = 0;
        if (isUndefinedDate(handelsdatumDate) || handelsdatumDate <= date) {
            time = getTradeTime(this.sr);
        }
        else {
            date = handelsdatumDate;
            if (this.currentDayDefined) {
                // ignore MMF_Boersenzeit as Handelsdatum was more recent than MMF_Bezahlt_Datum
                // which probably means that the MMF-fields are outdated (e.g., ESBI.SSB)
                time = getTradeTime(sr, true);
            }
            else {
                time = getTradeTime(this.sr);
            }
        }

        if (isUndefinedDate(date)) {
            date = getInt(ADF_Datum);
            if (date == this.today) {
                time = getTradeTime(this.sr);
            }
            else {
                date = Integer.MIN_VALUE;
            }
        }

        if (isUndefinedDate(date)) {
            date = getInt(ADF_Schluss_Vortagesdatum);
            time = getTradeTime(this.sr);
        }
        if (isUndefinedDate(date)) {
            date = getInt(ADF_Vorheriges_Datum);
            time = getTradeTime(this.sr);
        }
        if (isUndefinedDate(date)) {
            date = getDateOfArrival(this.sr);
            time = getTimeOfArrival(this.sr);
        }

        return toDateTime(date, time);
    }

    private boolean isUndefinedDate(int date) {
        return date == Integer.MIN_VALUE || date == 0;
    }

    protected Price createPrice(SnapField price, SnapField volume, SnapField supplement,
            DateTime date) {
        final BigDecimal value = (price != null && price.isDefined()) ? price.getPrice() : null;
        return createPrice(value, volume, supplement, date);
    }

    private Price createPrice(BigDecimal value, SnapField volume, SnapField supplement,
            DateTime date) {
        if (value == null) {
            return NullPrice.INSTANCE;
        }
        return new PriceImpl(value, (volume != null && volume.isDefined()) ? getLong(volume) : null,
                getString(supplement), date, this.priceQuality);
    }

    public Price getValuationPrice() {
        // ToDo: not correct yet, settlement should only be used for option and future
        final Price settlement = getSettlement();
        if (settlement.getValue() != null) {
            return settlement;
        }

        // also respects fund prices since PriceRecordFundVwd overwrites doGetPrice
        final Price last = getPrice();
        if (last.getValue() != null) {
            return last;
        }

        final Price open = getOpen();
        if (open.getValue() != null) {
            return open;
        }

        final Price kassa = getKassa();
        if (kassa.getValue() != null) {
            return kassa;
        }

//        final Price settlement  = getSettlement();
//        if (settlement.getValue() != null) {
//            return settlement;
//        }

        final Price bid = getBid();
        if (bid.getValue() != null) {
            return bid;
        }

        final Price previousBid = getPreviousBid();
        if (previousBid.getValue() != null) {
            return previousBid;
        }

        final Price ask = getAsk();
        if (ask.getValue() != null) {
            return ask;
        }

        final Price previousAsk = getPreviousAsk();
        if (previousAsk.getValue() != null) {
            return previousAsk;
        }

        final SnapField midprice = getField(ADF_Mittelkurs);
        if (!midprice.isDefined()) {
            return NullPrice.INSTANCE;
        }
        final int date = getInt(ADF_Datum);
        final int time = Math.max(getInt(ADF_Zeit), getInt(ADF_Boersenzeit));

        final DateTime priceDate = (date <= 0)
                ? toDateTime(getDateOfArrival(this.sr), getTimeOfArrival(this.sr))
                : toDateTime(date, time);
        return createPrice(midprice, null, null, priceDate);
    }

    public Price getAsk() {
        return this.ask;
    }

    private Price doGetAsk() {
        Price result = getBidAskPrice(ADF_Brief, ADF_Brief_Umsatz, ADF_Brief_Kurszusatz);
        if (result.isDefined() && isInvalidAsk(result.getValue(), this.bid.getValue(), this.price.getValue())) {
            return NullPrice.INSTANCE;
        }
        return result;
    }

    public Price getBid() {
        return this.bid;
    }

    private Price doGetBid() {
        return getBidAskPrice(ADF_Geld, ADF_Geld_Umsatz, ADF_Geld_Kurszusatz);
    }

    @Override
    public Price getLastAsk() {
        return getLastPrice(ADF_Brief, getAsk());
    }

    @Override
    public Price getLastBid() {
        return getLastPrice(ADF_Geld, getBid());
    }

    private Price getLastPrice(final Field field, Price current) {
        if (!current.isDefined() || BigDecimal.ZERO.compareTo(current.getValue()) != 0) {
            return current;
        }
        SnapField sf = getField(field);
        //noinspection NumberEquality
        if (sf.isDefined() && sf.getPrice() != sf.getLastPrice()) {
            return new PriceImpl(sf.getLastPrice(), null, null, null, this.priceQuality);
        }
        return current;
    }

    public Price getPreviousBid() {
        return getPreviousBidAskPrice(ADF_Geld_Vortag);
    }

    public Price getPreviousAsk() {
        return getPreviousBidAskPrice(ADF_Brief_Vortag);
    }

    public Price getOpen() {
        return getTodayOrPrevious(ADF_Anfang, null, ADF_Anfang_Kurszusatz,
                ADF_Anfang_Zeit, ADF_Anfang_Vortag, ADF_Anfang_Vortag_Kurszusatz);
    }

    public Price getClose() {
        final Price close = getTodayOrPrevious(ADF_Schluss, ADF_Schluss_Umsatz, ADF_Schluss_Kurszusatz,
                ADF_Schluss_Zeit, ADF_Schluss_Vortag, ADF_Schluss_Vortag_Kurszusatz);
        return isValidPrice(close.getValue()) ? close : NullPrice.INSTANCE;
    }

    public Price getPreviousOpen() {
        return getPreviousPrice(ADF_Anfang_Vortag, ADF_Anfang_Vortag_Kurszusatz);
    }

    public Price getPreviousClose() {
        if (this.pclose == null) {
            this.pclose = doGetPreviousClose();
        }
        return this.pclose;
    }

    protected Price doGetPreviousClose() {
        final SnapField price = getField(ADF_Schluss_Vortag);
        if (!isDefinedAndNotZero(price)) {
            return NullPrice.INSTANCE;
        }

        final SnapField supplement = getField(ADF_Schluss_Vortag_Kurszusatz);
        final DateTime dt = getPreviousCloseDate();
        return createPrice(price, null, supplement, dt);
    }

    private DateTime getPreviousCloseDate() {
        final int yyyymmdd = getInt(ADF_Schluss_Vortagesdatum);

        if (yyyymmdd > 0) {
            if (getInt(MMF_Bezahlt_Datum) == yyyymmdd) {
                final int time = getInt(MMF_Boersenzeit);
                return toDateTime(yyyymmdd, time >= 0 ? time : 0);
            }
            return toDateTime(yyyymmdd, 0);
        }

        return getDateTime(ADF_Vorheriges_Datum);
    }

    public BigDecimal getYield() {
        return getYieldPrice().getValue();
    }

    public Price getYieldPrice() {
        final SnapField yield = getIfDefinedOr(ADF_Rendite_ISMA, ADF_Rendite);
        if (isDefinedAndNotZero(yield)) {
            return createPrice(asPercent(yield), null, null, getPrice().getDate());
        }

        return getPreviousYieldPrice();
    }

    public BigDecimal getPreviousYield() {
        return getPreviousYieldPrice().getValue();
    }

    public Price getPreviousYieldPrice() {
        final SnapField prevYield = getIfDefinedOr(ADF_Rendite_ISMA_Vortag, ADF_Rendite_Vortag);
        if (isDefinedAndNotZero(prevYield)) {
            return createPrice(asPercent(prevYield), null, null, getPreviousDate());
        }

        return NullPrice.INSTANCE;
    }

    public BigDecimal getYieldISMA() {
        return asPercent(getIfDefinedAndNotZeroOr(ADF_Rendite_ISMA, ADF_Rendite_ISMA_Vortag));
    }

    public BigDecimal getImpliedVolatility() {
        return asPercent(ADF_Imp_Vola_Mitte);
    }

    @Override
    public int getNominalDelayInSeconds() {
        return this.sr.getNominalDelayInSeconds();
    }

    public BigDecimal getAccruedInterest() {
        return asPercent(ADF_Accrued_Interest);
    }

    public BigDecimal getCloseBefore(LocalDate date) {
        return getCloseBefore(date, CLOSE_BEFORE_DATES, CLOSE_BEFORE_PRICES);
    }

    public Price getSettlement() {
        final SnapField settlement = getField(ADF_Settlement);
        if (isDefinedAndNotZero(settlement)) {
            SnapField dateField = getIfDefinedOr(ADF_Settlement_Datum, ADF_Handelsdatum);
            if (!dateField.isDefined()) {
                dateField = getIfFieldIdValid(MMF_Bezahlt_Datum.id());
            }
            return createPrice(settlement, null, NullSnapField.INSTANCE, getDateTime(dateField));
        }

        final SnapField settlementVortag = getField(ADF_Settlement_Vortag);
        if (isDefinedAndNotZero(settlementVortag)) {
            final SnapField dateField = getIfDefinedOr(ADF_Settlement_Datum_Vortag, ADF_Vorheriges_Datum);
            return createPrice(settlementVortag, null, NullSnapField.INSTANCE, getDateTime(dateField));
        }

        return NullPrice.INSTANCE;
    }

    public Price getPreviousSettlement() {
        final SnapField price = getField(ADF_Settlement_Vortag);
        final SnapField supplement = NullSnapField.INSTANCE;
        final SnapField dateField = getIfDefinedOr(ADF_Settlement_Datum_Vortag, ADF_Vorheriges_Datum);
        final DateTime priceDate = getDateTime(dateField);
        return createPrice(price, null, supplement, priceDate);
    }

    public Price getOpenInterest() {
        final SnapField openInterest = getIfCurrentOr(ADF_Open_Interest, ADF_Open_Interest_Vortag);
        if (!openInterest.isDefined()) {
            return NullPrice.INSTANCE;
        }
        final SnapField dateField = (openInterest.getId() == ADF_Open_Interest.id())
                ? getField(ADF_OI_Date) : getField(ADF_OI_Date_prev);
        final DateTime priceDate = getDateTime(dateField);
        return createPrice(BigDecimal.ZERO, openInterest, NullSnapField.INSTANCE, priceDate);
    }

    protected BigDecimal getCloseBefore(LocalDate date, int[] dates, int[] prices) {
        for (int i = 0; i < dates.length; i++) {
            final SnapField svd = getField(dates[i]);
            if (!isDefinedAndNotZero(svd)) {
                return null;
            }
            if (DateUtil.yyyyMmDdToLocalDate(SnapRecordUtils.getInt(svd)).isBefore(date)) {
                final SnapField sv = getField(prices[i]);
                if (!sv.isDefined()) {
                    return null;
                }
                final BigDecimal result = sv.getPrice();
                return (result.signum() == 1) ? result : null;
            }
        }

        return null;
    }

    public Price getOfficialClose() {
        return getTodayOrPrevious(ADF_Official_Close, null, null, null, ADF_Official_Close_Vortag, null);
    }

    public Price getOfficialBid() {
        return getTodayOrPrevious(ADF_Official_Bid, null, null, null, ADF_Prev_Official_Bid, null);
    }

    public Price getOfficialAsk() {
        return getTodayOrPrevious(ADF_Official_Ask, null, null, null, ADF_Prev_Official_Ask, null);
    }

    public Price getPreviousOfficialBid() {
        return getPreviousPrice(ADF_Prev_Official_Bid, null);
    }

    public Price getPreviousOfficialAsk() {
        return getPreviousPrice(ADF_Prev_Official_Ask, null);
    }

    public Price getUnofficialBid() {
        return getTodayOrPrevious(ADF_Unofficial_Bid, null, null, null, ADF_Prev_Unofficial_Bid, null);
    }

    public Price getUnofficialAsk() {
        return getTodayOrPrevious(ADF_Unofficial_Ask, null, null, null, ADF_Prev_Unofficial_Ask, null);
    }

    public Price getPreviousUnofficialBid() {
        return getPreviousPrice(ADF_Prev_Unofficial_Bid, null);
    }

    public Price getPreviousUnofficialAsk() {
        return getPreviousPrice(ADF_Prev_Unofficial_Ask, null);
    }

    public BigDecimal getInterpolatedClosing() {
        return getPrice(ADF_Interpo_Closing);
    }

    public BigDecimal getProvisionalEvaluation() {
        return getPrice(ADF_Prov_Evaluation);
    }

    public BigDecimal getBidYield() {
        return asPercent(ADF_Rendite_Geld);
    }

    public BigDecimal getAskYield() {
        return asPercent(ADF_Rendite_Brief);
    }

    public BigDecimal getBrokenPeriodInterest() {
        return getPrice(ADF_Accrued_Interest);
    }

    private BigDecimal getPrice(Field field) {
        final SnapField sf = getField(field);
        return sf.isDefined() ? sf.getPrice() : null;
    }

    public BigDecimal getDuration() {
        return getPrice(ADF_Duration);
    }

    public BigDecimal getModifiedDuration() {
        return getPrice(ADF_ModifiedDuration);
    }

    public BigDecimal getConvexity() {
        return getPrice(ADF_Convexity);
    }

    public BigDecimal getInterestRateElasticity() {
        return getPrice(ADF_Interest_Rate_Elasticity);
    }

    public BigDecimal getBasePointValue() {
        return getPrice(ADF_BasisPointValue);
    }

    public Price getKassa() {
        return getTodayOrPrevious(ADF_Kassa, null, ADF_Kassa_Kurszusatz,
                null, ADF_Kassa_Vortag, ADF_Kassa_Vortag_Kurszusatz);
    }

    private Price getBidAskPrice(Field priceField, Field volumeField, Field supplementField) {
        final SnapField price = getField(priceField);

        if (!isValidPrice(price)) {
            return NullPrice.INSTANCE;
        }

        final SnapField volume = getField(volumeField);
        final SnapField supplement = getField(supplementField);

        final DateTime priceDate = (volumeField != null) ? getBidAskDateTime() : null;
        return createPrice(price, volume, supplement, priceDate);
    }

    private Price getPreviousBidAskPrice(Field priceField) {
        final SnapField price = getField(priceField);

        if (!isValidPrice(price)) {
            return NullPrice.INSTANCE;
        }

        final DateTime priceDate = getDateTime(getIfDefinedOr(ADF_Vorheriges_Datum, ADF_Vorheriges_Datum));
        return createPrice(price, NullSnapField.INSTANCE, NullSnapField.INSTANCE, priceDate);
    }

    protected boolean isValidPrice(SnapField price) {
        // TODO: use for other prices, not only for bid/ask/close
        return price.isDefined() && price.getType() == FieldTypeEnum.PRICE && isValidPrice(price.getPrice());
    }

    private boolean isValidPrice(BigDecimal bd) {
        return this.isForward || (bd != null && bd.signum() != 0);
    }

    protected DateTime getBidAskDateTime() {
        return toDateTime(SnapRecordUtils.getInt(getBidAskDate()), getTime(this.sr));
    }

    protected SnapField getBidAskDate() {
        final SnapField field = getIfDefinedAndNotZeroOr(ADF_Datum_Quotierung, ADF_Datum, ADF_Handelsdatum);
        if (field != null) {
            return field;
        }

        // analyse ADF_Datum => if it is defined and contains 0, use ADF_Vorheriges_Datum
        // see R-44875
        final SnapField adfDatum = getField(ADF_Datum);
        if (adfDatum.isDefined() && SnapRecordUtils.getInt(adfDatum) == 0) {
            final SnapField adfVorherigesDatum = getField(ADF_Vorheriges_Datum);
            if (isDefinedAndNotZero(adfVorherigesDatum)) {
                return adfVorherigesDatum;
            }
        }

        return getField(ADF_DATEOFARR);
    }

    public BigDecimal getSpreadNet() {
        final Price bid = getBid();
        final Price ask = getAsk();

        if (bid == NullPrice.INSTANCE || ask == NullPrice.INSTANCE) {
            return null;
        }

        return ask.getValue().subtract(bid.getValue());
    }

    public BigDecimal getSpreadPercent() {
        if (this.bid == null || this.bid == NullPrice.INSTANCE
                || this.ask == null || this.ask == NullPrice.INSTANCE
                || this.bid.getValue().signum() == 0) {
            return null;
        }

        return this.ask.getValue().subtract(this.bid.getValue()).divide(this.bid.getValue(), MC);
    }

    public BigDecimal getSpreadHomogenized() {
        if (this.subscriptionRatio == null) {
            return null;
        }
        final BigDecimal spreadPercent = getSpreadPercent();
        if (spreadPercent == null) {
            return null;
        }
        return spreadPercent.multiply(this.subscriptionRatio);
    }

    public BigDecimal getChangeNet() {
        if (this.currentDayDefined) {
            final SnapField sf = getField(ADF_Veraenderung);
            if (sf != NullSnapField.INSTANCE) {
                return sf.getPrice();
            }
        }
        final SnapField sfp = getField(ADF_Veraenderung_Vortag);
        if (sfp != NullSnapField.INSTANCE) {
            return sfp.getPrice();
        }
        final BigDecimal[] bds = getLastAndPreviousPrice();
        return (bds != null) ? bds[0].subtract(bds[1]) : null;
    }

    public BigDecimal getChangePercent() {
        if (this.currentDayDefined) {
            final SnapField sf = getField(ADF_Prozentuale_Veraenderung);
            if (sf != NullSnapField.INSTANCE) {
                return sf.getPrice().movePointLeft(2);
            }
        }
        final SnapField sfp = getField(ADF_Proz_Veraend_Vortag);
        if (sfp != NullSnapField.INSTANCE) {
            return sfp.getPrice().movePointLeft(2);
        }
        final BigDecimal[] bds = getLastAndPreviousPrice();
        return (bds != null) ? bds[0].subtract(bds[1]).divide(bds[1], MC) : null;
    }

    private int getLastPriceIndex(int from) {
        for (int i = from; i < LAST_PRICE_FIELDS.length; i++) {
            final SnapField sf = getField(LAST_PRICE_FIELDS[i]);
            if (isDefinedAndNotZero(sf)) {
                return i;
            }
        }
        return -1;
    }

    private boolean isOnSameDay(int i, int j) {
        final SnapField sfi = getField(LAST_DATE_FIELDS[i]);
        final SnapField sfj = getField(LAST_DATE_FIELDS[j]);
        return sfi.isDefined() && sfj.isDefined() && sfi.getValue().equals(sfj.getValue());
    }

    private BigDecimal[] getLastAndPreviousPrice() {
        // find index of most recent defined price field
        final int lastIdx = getLastPriceIndex(0);
        if (lastIdx == -1) {
            return null;
        }

        // find index of most recent defined price field after lastIdx on a different day
        int prevIdx = lastIdx;
        do {
            prevIdx = getLastPriceIndex(++prevIdx);
        }
        while (prevIdx != -1 && isOnSameDay(lastIdx, prevIdx));

        if (prevIdx == -1) {
            return null;
        }

        return new BigDecimal[]{
                getField(LAST_PRICE_FIELDS[lastIdx]).getPrice(),
                getField(LAST_PRICE_FIELDS[prevIdx]).getPrice()
        };
    }

    int getInt(Field field) {
        return SnapRecordUtils.getInt(getField(field));
    }

    protected boolean isDefinedAndNotZero(Field field) {
        return isDefinedAndNotZero(getField(field));
    }

    boolean isDefinedAndNotZero(SnapField sf) {
        if (!sf.isDefined()) {
            return false;
        }
        switch (sf.getType()) {
            case PRICE:
                return sf.getPrice().signum() != 0;
            case DATE:
            case TIME:
            case NUMBER:
                return ((Number) sf.getValue()).intValue() != 0;
            default:
                return true;
        }
    }

    public Price getLastClose() {
        final SnapField close = getField(ADF_Schluss);
        if (isDefinedAndNotZero(close)) {
            final SnapField supplement = getField(ADF_Schluss_Kurszusatz);

            final int date = getInt(ADF_Schluss_Datum);
            final int time = getInt(ADF_Schluss_Zeit);
            final DateTime priceDate = toDateTime(date, Math.max(0, time));

            return createPrice(close, getField(ADF_Schluss_Umsatz), supplement, priceDate);
        }

        return getPreviousClose();
    }

    public Price getHighDay() {
        final Price high = getTodayOrPrevious(ADF_Tageshoch, null, ADF_Tageshoch_Kurszusatz,
                ADF_Tageshoch_Zeit, ADF_Tageshoch_Vortag, ADF_Tageshoch_Vortag_Kurszusatz);
        if (high.getValue() != null) {
            return high;
        }
        return getPrice();
    }

    public Price getPreviousHighDay() {
        return getPreviousPrice(ADF_Tageshoch_Vortag, ADF_Tageshoch_Vortag_Kurszusatz);
    }

    protected Price getPreviousPrice(Field priceField, Field supplementField) {
        final SnapField price = getField(priceField);
        final SnapField supplement = getField(supplementField);
        return isValidPrice(price) ? createPrice(price, null, supplement, getPreviousDate()) : NullPrice.INSTANCE;
    }

    private DateTime getPreviousDate() {
        return getDateTime(ADF_Schluss_Vortagesdatum, ADF_Vorheriges_Datum);
    }

    public Price getLowDay() {
        final Price low = getTodayOrPrevious(ADF_Tagestief, null, ADF_Tagestief_Kurszusatz,
                ADF_Tagestief_Zeit, ADF_Tagestief_Vortag,
                ADF_Tagestief_Vortag_Kurszusatz);
        if (low.getValue() != null) {
            return low;
        }
        return getPrice();
    }

    public Price getPreviousLowDay() {
        return getPreviousPrice(ADF_Tagestief_Vortag, ADF_Tagestief_Vortag_Kurszusatz);
    }

    private Price getTodayOrPrevious(Field currentDayPrice,
            Field currentDayVolume, Field currentDaySupplement,
            Field currentDayTime, Field previousDayPrice, Field previousDaySupplement) {
        final SnapField price;
        final SnapField supplement;
        final SnapField volume;
        final int date;
        final int time;

        if (this.currentDayDefined) {
            price = getField(currentDayPrice);
            volume = getField(currentDayVolume);
            supplement = getField(currentDaySupplement);
            final Price currentPrice = getPrice();
            date = currentPrice.getDate() != null
                    ? DateUtil.toYyyyMmDd(currentPrice.getDate()) // do not use this.today because of late roll for american quotes, use date of last price
                    : this.today;
            final SnapField timeField = getField(currentDayTime);
            time = timeField.isDefined() ? SnapRecordUtils.getInt(timeField) : 0;
        }
        else {
            price = getField(previousDayPrice);
            volume = NullSnapField.INSTANCE;
            supplement = getField(previousDaySupplement);
            date = isDefinedAndNotZero(ADF_Schluss_Vortagesdatum)
                    ? getInt(ADF_Schluss_Vortagesdatum)
                    : getInt(ADF_Vorheriges_Datum);
            time = 0;
        }

        final DateTime priceDate = toDateTime(date, time);
        return createPrice(price, volume, supplement, priceDate);
    }

    public Price getHighYear() {
        return getPrice(ADF_Jahreshoch, ADF_Jahreshoch_Datum);
    }

    public Price getLowYear() {
        return getPrice(ADF_Jahrestief, ADF_Jahrestief_Datum);
    }

    public Price getHigh52W() {
        final Price high52W = getPrice(ADF_52_W_Hoch, ADF_52_W_Hoch_Datum);
        if (!isDefined(high52W)) {
            return NullPrice.INSTANCE;
        }
        final Price highDay = getHighDay();
        if (!isDefinedAndNotZero(highDay)) {
            return high52W;
        }
        return highDay.getValue().compareTo(high52W.getValue()) == 1 ? highDay : high52W;
    }

    public Price getLow52W() {
        final Price low52W = getPrice(ADF_52_W_Tief, ADF_52_W_Tief_Datum);
        if (!isDefined(low52W)) {
            return NullPrice.INSTANCE;
        }
        final Price lowDay = getLowDay();
        if (!isDefinedAndNotZero(lowDay)) {
            return low52W;
        }
        return lowDay.getValue().compareTo(low52W.getValue()) == -1 ? lowDay : low52W;
    }

    private boolean isDefined(Price p) {
        return p != null && p != NullPrice.INSTANCE && p.getValue() != null;
    }

    protected boolean isDefined(Field field) {
        return getField(field).isDefined();
    }

    private boolean isDefinedAndNotZero(Price p) {
        return isDefined(p) && p.getValue().signum() != 0;
    }

    public Long getVolumeDay() {
        final SnapField field = getIfCurrentOr(ADF_Umsatz_gesamt, ADF_Umsatz_gesamt_Vortag);
        return field.isDefined() ? getLong(field) : null;
    }

    public Long getPreviousVolumeDay() {
        final SnapField field = getField(ADF_Umsatz_gesamt_Vortag);
        return field.isDefined() ? getLong(field) : null;
    }

    public BigDecimal getTurnoverDay() {
        final SnapField field = getIfCurrentOr(ADF_Umsatz_gesamt_in_Whrg, ADF_Umsatz_gesamt_in_Whrg_Vortag);
        return getPriceFromStringField(field);
    }

    public BigDecimal getInterimProfit() {
        final SnapField field = getIfCurrentOr(ADF_Zwischengewinn, ADF_Zwischengewinn_Vortag);
        return field.isDefined() ? field.getPrice() : null;
    }

    public Price getDistributionFund() {
        return getPrice(ADF_Ausschuettung_KAG, ADF_Ausschuettungsdatum_KAG);
    }

    public BigDecimal getPreviousTurnoverDay() {
        return getPriceFromStringField(getField(ADF_Umsatz_gesamt_in_Whrg_Vortag));
    }

    private BigDecimal getPriceFromStringField(SnapField field) {
        final String value = getString(field);
        if (StringUtils.hasText(value)) {
            try {
                return new BigDecimal(value.trim());
            } catch (NumberFormatException e) {
                // ignore
            }
        }
        return null;
    }

    public BigDecimal getVwap() {
        final BigDecimal vwap = getPrice(ADF_VWAP);
        if (vwap != null && vwap.compareTo(BigDecimal.ZERO) != 0) {
            return vwap;
        }
        return getPrice(ADF_VWAP_Vortag);
    }

    public BigDecimal getMarketCapitalization() {
        final SnapField field = getIfDefinedOr(ADF_Marktkapitalisierung, ADF_Marktkapitalisierung_Vortag);
        return getPriceFromStringField(field);
    }

    protected Price getPrice(final Field priceField, final Field dateField) {
        final SnapField price = getField(priceField);
        if (!price.isDefined()) {
            return NullPrice.INSTANCE;
        }
        return createPrice(price, null, null, getDateTime(dateField));
    }

    protected DateTime getDateTime(VwdFieldDescription.Field date1,
            VwdFieldDescription.Field date2) {
        final SnapField field = getField(date1);
        return field.isDefined() ? getDateTime(date1) : getDateTime(date2);
    }

    protected DateTime getDateTime(VwdFieldDescription.Field date) {
        return getDateTime(getField(date));
    }

    protected DateTime getDateTime(SnapField date) {
        try {
            return toDateTime(SnapRecordUtils.getInt(date), 0);
        } catch (Exception e) {
            logger.warn("<getDateTime> illegal date for " + this.quoteid + ".qid, field " + date);
            return null;
        }
    }

    protected SnapField getField(Field field) {
        return (field != null) ? getField(field.id()) : NullSnapField.INSTANCE;
    }

    protected SnapField getField(int fieldId) {
        return this.sr.getField(fieldId);
    }

    private SnapField getIfCurrentOr(Field f1, Field f2) {
        return this.currentDayDefined ? getField(f1) : getField(f2);
    }

    private SnapField getIfDefinedOr(Field f1, Field f2) {
        final SnapField result = getField(f1);
        return (result.isDefined()) ? result : getField(f2);
    }

    private SnapField getIfDefinedAndNotZeroOr(Field... fields) {
        for (final Field field : fields) {
            final SnapField sf = getField(field);
            if (isDefinedAndNotZero(sf)) {
                return sf;
            }
        }
        return null;
    }

    private SnapField getIfFieldIdValid(int fieldId) {
        return (fieldId > 0) ? getField(fieldId) : NullSnapField.INSTANCE;
    }

    public BigDecimal getDividendYield() {
        return asPercent(ADF_Dividenden_Rendite);
    }

    public BigDecimal getDividendCash() {
        return getPrice(ADF_Dividende_1);
    }

    public DateTime getDividendDate() {
        return getDateTime(ADF_Dividende_Ex_Tag);
    }

    public BigDecimal getTwas() {
//        return getPrice(ADF_TWAS);
        return null;
    }

    public Long getNumberOfTrades() {
        if (this.currentDayDefined) {
            final SnapField field = getField(ADF_Anzahl_Handel);
            return field.isDefined() ? getLong(field) : null;
        }
        return null;
    }

    protected DateTime toDateTime(int date, int time) {
        return (date > 0 && time >= 0) ? DateUtil.toDateTime(date, time) : null;
    }

    private BigDecimal asPercent(Field f) {
        return asPercent(getField(f));
    }

    private BigDecimal asPercent(SnapField field) {
        return field != null && field.isDefined() ? field.getPrice().divide(ONE_HUNDRED, MC) : null;
    }

    public String getLmeSubsystemPrice() {
        return null;
    }

    public String getLmeSubsystemBid() {
        return null;
    }

    public String getLmeSubsystemAsk() {
        return null;
    }

    public BigDecimal getUnderlyingReferencePrice() {
        return getPrice(VwdFieldDescription.ADF_Kurs_Basiswert);
    }

    public SnapRecord getSnapRecord() {
        return this.sr;
    }

    public long getQuoteid() {
        return quoteid;
    }

    @Override
    public BigDecimal getBidAskMidPrice() {
        return calcMidPrice(getLastBid().getValue(), getLastAsk().getValue());
    }

    @Override
    public BigDecimal getPreviousBidAskMidPrice() {
        return calcMidPrice(getPreviousBid().getValue(), getPreviousAsk().getValue());
    }

    private BigDecimal calcMidPrice(BigDecimal bid, BigDecimal ask) {
        if (bid == null || BigDecimal.ZERO.equals(bid)) {
            return ask == null ? BigDecimal.ZERO : ask;
        }
        else if (ask == null || BigDecimal.ZERO.equals(ask)) {
            return bid == null ? BigDecimal.ZERO : bid;
        }
        else {
            return bid.add(ask, MC).divide(BigDecimal.valueOf(2), MC);
        }
    }

    @Override
    public BigDecimal getAskHighDay() {
        return isDefinedAndNotZero(ADF_Brief_Tageshoch) ? getPrice(ADF_Brief_Tageshoch) : null;
    }

    @Override
    public BigDecimal getAskLowDay() {
        return isDefinedAndNotZero(ADF_Brief_Tagestief) ? getPrice(ADF_Brief_Tagestief) : null;
    }

    @Override
    public BigDecimal getBidHighDay() {
        return isDefinedAndNotZero(ADF_Geld_Tageshoch) ? getPrice(ADF_Geld_Tageshoch) : null;
    }

    @Override
    public BigDecimal getBidLowDay() {
        return isDefinedAndNotZero(ADF_Geld_Tagestief) ? getPrice(ADF_Geld_Tagestief) : null;
    }
}
