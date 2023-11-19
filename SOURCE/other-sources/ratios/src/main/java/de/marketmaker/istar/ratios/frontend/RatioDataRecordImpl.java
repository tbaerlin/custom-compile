/*
 * RatioDataRecordImpl.java
 *
 * Created on 01.08.2006 15:08:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.BitSet;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;
import de.marketmaker.istar.instrument.InstrumentUtil;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatioDataRecordImpl implements RatioDataRecord {
    private final InstrumentRatios instrumentData;

    private final QuoteRatios quoteData;

    private final Map<Field, RatioFieldDescription.Field> fields;

    private final List<Locale> locales;

    public RatioDataRecordImpl(RatioDataResult rdr, List<Locale> locales) {
        this(rdr.getInstrumentRatios(), rdr.getQuoteData(), Collections.emptyMap(), locales);
    }

    public RatioDataRecordImpl(InstrumentRatios instrumentData, QuoteRatios quoteData,
            Map<Field, RatioFieldDescription.Field> fields, List<Locale> locales) {
        this.instrumentData = instrumentData;
        this.quoteData = quoteData;
        this.fields = fields;
        this.locales = locales;
    }

    public List<Integer> getInstrumentFieldIdsSortedByName() {
        return RatioFieldDescription.getInstrumentFieldIdsSortedByName();
    }

    public List<Integer> getQuoteRatiosFieldIdsSortedByName() {
        return RatioFieldDescription.getQuoteRatiosFieldIdsSortedByName();
    }

    private String getInstrumentStringField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return getString(this.instrumentData, f);
    }

    private String getString(Selectable instrumentData, RatioFieldDescription.Field f
    ) {
        if (!f.isLocalized()) {
            return instrumentData.getString(f.id());
        }

        final int localeIndex = RatioFieldDescription.getLocaleIndex(f, this.locales);
        return instrumentData.getString(f.id(), localeIndex);
    }

    private Long getInstrumentNumberField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.instrumentData.getLong(f.id());
    }

    private Boolean getInstrumentBooleanField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.instrumentData.getBoolean(f.id());
    }

    public String getInstrumentStringField(String fieldName) {
        return getInstrumentStringField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private BigDecimal getInstrumentPriceField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        final Long value = this.instrumentData.getLong(f.id());
        return value != null ? PriceCoder.decode(value) : null;
    }

    public BigDecimal getInstrumentPriceField(String fieldName) {
        return getInstrumentPriceField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private Integer getInstrumentIntField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.instrumentData.getInt(f.id());
    }

    private Long getInstrumentLongField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.instrumentData.getLong(f.id());
    }

    private BitSet getInstrumentBitSetField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.instrumentData.getBitSet(f.id());
    }

    public Integer getInstrumentNumberField(String fieldName) {
        return getInstrumentIntField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private DateTime getQuoteDateField(RatioFieldDescription.Field f) {
        final Integer yyyymmdd = getQuoteNumberField(f);
        if (yyyymmdd == null) {
            return null;
        }

        return yyyymmdd == 20991230 || yyyymmdd == 0 ? null : DateUtil.yyyymmddToDateTime(yyyymmdd);
    }

    private DateTime getQuoteTimestampField(RatioFieldDescription.Field f) {
        final Long value = getQuoteLongField(f);
        if (value == null) {
            return null;
        }

        final int yyyymmdd = (int) (value / 100000L);
        final int secondsInDay = (int) (value % 100000L);

        return DateUtil.toDateTime(yyyymmdd, secondsInDay);
    }

    private DateTime getInstrumentDateField(RatioFieldDescription.Field f) {
        final Integer yyyymmdd = getInstrumentIntField(f);
        if (yyyymmdd == null) {
            return null;
        }

        return yyyymmdd == 20991230 || yyyymmdd == 0 ? null : DateUtil.yyyymmddToDateTime(yyyymmdd);
    }

    public DateTime getInstrumentDateField(String fieldName) {
        return getInstrumentDateField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private String getQuoteStringField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return getString(this.quoteData, f);
    }

    public String getQuoteStringField(String fieldName) {
        return getQuoteStringField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private BigDecimal getQuotePriceField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        final Long value = this.quoteData.getLong(f.id());
        return value != null ? PriceCoder.decode(value) : null;
    }

    public BigDecimal getQuotePriceField(String fieldName) {
        return getQuotePriceField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private Integer getQuoteNumberField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.quoteData.getInt(f.id());
    }

    private Long getQuoteLongField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.quoteData.getLong(f.id());
    }

    public Long getQuoteLongField(String fieldName) {
        return getQuoteLongField(RatioFieldDescription.getFieldByName(fieldName));
    }

    private Boolean getQuoteBooleanField(RatioFieldDescription.Field f) {
        if (f == null) {
            return null;
        }
        return this.quoteData.getBoolean(f.id());
    }

    public Long getQuoteBooleanField(String fieldName) {
        return getQuoteLongField(RatioFieldDescription.getFieldByName(fieldName));
    }

    public long getInstrumentId() {
        return this.instrumentData.getId();
    }

    public long getQuoteId() {
        return this.quoteData.getId();
    }

    public String getIsin() {
        return getInstrumentStringField(RatioFieldDescription.isin);
    }

    public String getWkn() {
        return getInstrumentStringField(RatioFieldDescription.wkn);
    }

    public String getName() {
        return getInstrumentStringField(this.fields.get(Field.name));
    }

    public String getSector() {
        return getInstrumentStringField(this.fields.get(Field.sector));
    }

    @Deprecated
    public String getSectorFww() {
        return getInstrumentStringField(RatioFieldDescription.fwwSector);
    }

    @Deprecated
    public String getFundTypeFww() {
        return getInstrumentStringField(RatioFieldDescription.fwwFundType);
    }

    public String getSymbolVwdfeedMarket() {
        final String vwdcode = getQuoteStringField(RatioFieldDescription.vwdCode);
        if (vwdcode == null) {
            // TODO: fix in backend
            return Long.toString(getQuoteId());
        }
        final VendorkeyVwd vkey = VendorkeyVwd.getInstance(new ByteString("1." + vwdcode));
        return vkey.getMarketName().toString();
    }

    public String getCurrencySymbolIso() {
        return getQuoteStringField(RatioFieldDescription.currency);
    }

    public Price getPrice() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.lastPrice);
        final Long volume = getQuoteLongField(RatioFieldDescription.tradeVolume);
        final Integer date = getQuoteNumberField(RatioFieldDescription.lastDate);
        final Integer tmpTime = getQuoteNumberField(RatioFieldDescription.lastTime);
        final Integer time = InstrumentUtil.isVwdFundVwdcode(getQuoteStringField(RatioFieldDescription.vwdCode)) || tmpTime == null
                ? 0
                : tmpTime;

        //TODO: adapt price quality
        return new PriceImpl(price != null ? price : null,
                volume,
                null,
                date != null && date > 0 ? DateUtil.toDateTime(date, time) : null,
                PriceQuality.DELAYED);
    }

    public Price getUnderlyingPrice() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.underlyingLastPrice);
        final Long volume = getQuoteLongField(RatioFieldDescription.underlyingTradeVolume);
        final Integer date = getQuoteNumberField(RatioFieldDescription.underlyingLastDate);
        final Integer time = getQuoteNumberField(RatioFieldDescription.underlyingLastTime);

        //TODO: adapt price quality
        return new PriceImpl(price != null ? price : null,
                volume,
                null,
                date != null && date > 0 ? DateUtil.toDateTime(date, time) : null,
                PriceQuality.DELAYED);
    }

    public Price getBid() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.bid);
        final Long volume = getQuoteLongField(RatioFieldDescription.bidVolume);
        final Integer date = getQuoteNumberField(RatioFieldDescription.bidAskDate);
        final Integer time = getQuoteNumberField(RatioFieldDescription.bidAskTime);

        //TODO: adapt price quality
        return new PriceImpl(price != null ? price : null,
                volume,
                null,
                (date == null || time == null) ? null : DateUtil.toDateTime(Math.max(date, DateUtil.dateToYyyyMmDd()), Math.max(time, 0)),
                PriceQuality.DELAYED);
    }

    public Price getAsk() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.ask);
        final Long volume = getQuoteLongField(RatioFieldDescription.askVolume);
        final Integer date = getQuoteNumberField(RatioFieldDescription.bidAskDate);
        final Integer time = getQuoteNumberField(RatioFieldDescription.bidAskTime);

        //TODO: adapt price quality
        return new PriceImpl(price != null ? price : null,
                volume,
                null,
                (date == null || time == null) ? null : DateUtil.toDateTime(Math.max(date, DateUtil.dateToYyyyMmDd()), Math.max(time, 0)),
                PriceQuality.DELAYED);
    }

    public BigDecimal getChangeNet() {
        return getQuotePriceField(RatioFieldDescription.changeNet);
    }

    public BigDecimal getChangePercent() {
        return getQuotePriceField(RatioFieldDescription.changePercent);
    }

    public BigDecimal getChangeNet1Year() {
        //TODO: implement correctly
        final BigDecimal changePercent = getQuotePriceField(RatioFieldDescription.performance1y);
        final Price lastPrice = getPrice();
        if (changePercent == null || lastPrice.getValue() == null) {
            return null;
        }
        if (changePercent.add(BigDecimal.ONE).compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return lastPrice.getValue().subtract(lastPrice.getValue().divide(changePercent.add(BigDecimal.ONE), RoundingMode.HALF_UP));
    }

    public BigDecimal getChangePercent1Month() {
        return getQuotePriceField(RatioFieldDescription.performance1m);
    }

    public BigDecimal getChangePercent3Months() {
        return getQuotePriceField(RatioFieldDescription.performance3m);
    }

    public BigDecimal getChangePercent6Months() {
        return getQuotePriceField(RatioFieldDescription.performance6m);
    }

    public BigDecimal getChangePercent1Year() {
        return getQuotePriceField(RatioFieldDescription.performance1y);
    }

    public BigDecimal getChangePercent3Years() {
        return getQuotePriceField(RatioFieldDescription.performance3y);
    }

    public BigDecimal getChangePercent5Years() {
        return getQuotePriceField(RatioFieldDescription.performance5y);
    }

    public BigDecimal getChangePercent10Years() {
        return null;
    }

    public Long getTotalVolume() {
        return getQuoteLongField(RatioFieldDescription.totalVolume);
    }

    public BigDecimal getYield() {
        if (this.quoteData instanceof QuoteRatiosBND) {
            return getQuotePriceField(RatioFieldDescription.yieldRelative_mdps);
        }
        if (this.quoteData instanceof QuoteRatiosCER) {
            return getQuotePriceField(this.fields.get(Field.yield));
        }
        return null;
    }

    public BigDecimal getInterest() {
        return getInstrumentPriceField(RatioFieldDescription.interest);
    }

    public String getInterestPeriod() {
        return getInstrumentStringField(this.fields.get(Field.interestPeriod));
    }

    public String getUnderlyingName() {
        return getInstrumentStringField(RatioFieldDescription.underlyingName);
    }

    public String getUnderlyingType() {
        return getInstrumentStringField(RatioFieldDescription.underlyingType);
    }

    public String getIssuername() {
        return getInstrumentStringField(this.fields.get(Field.issuername));
    }

    public DateTime getExpires() {
        return getInstrumentDateField(RatioFieldDescription.expires);
    }

    public BigDecimal getIssueSurcharge() {
        return getInstrumentPriceField(this.fields.get(Field.issueSurcharge));
    }

    public String getInvestmentFocus() {
        return getInstrumentStringField(this.fields.get(Field.investmentFocus));
    }

    public BigDecimal getManagementFee() {
        return getInstrumentPriceField(this.fields.get(Field.managementFee));
    }

    public BigDecimal getAccountFee() {
        return getInstrumentPriceField(this.fields.get(Field.accountFee));
    }

    public BigDecimal getTer() {
        return getInstrumentPriceField(this.fields.get(Field.ter));
    }

    public BigDecimal getBVIPerformanceCurrentYear() {
        return getQuotePriceField(RatioFieldDescription.bviperformancecurrentyear);
    }

    public BigDecimal getBVIPerformance1Week() {
        return getQuotePriceField(RatioFieldDescription.bviperformance1w);
    }

    public BigDecimal getBVIPerformance1Month() {
        return getQuotePriceField(RatioFieldDescription.bviperformance1m);
    }

    public BigDecimal getBVIPerformance3Months() {
        return getQuotePriceField(RatioFieldDescription.bviperformance3m);
    }

    public BigDecimal getBVIPerformance6Months() {
        return getQuotePriceField(RatioFieldDescription.bviperformance6m);
    }

    public BigDecimal getBVIPerformance1Year() {
        return getQuotePriceField(RatioFieldDescription.bviperformance1y);
    }

    public BigDecimal getBVIPerformance3Years() {
        return getQuotePriceField(RatioFieldDescription.bviperformance3y);
    }

    public BigDecimal getBVIPerformance5Years() {
        return getQuotePriceField(RatioFieldDescription.bviperformance5y);
    }

    public BigDecimal getBVIPerformance10Years() {
        return getQuotePriceField(RatioFieldDescription.bviperformance10y);
    }

    public BigDecimal getNegativeMonthsPercent1Month() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent1m);
    }

    public BigDecimal getNegativeMonthsPercent3Months() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent3m);
    }

    public BigDecimal getNegativeMonthsPercent6Months() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent6m);
    }

    public BigDecimal getNegativeMonthsPercent1Year() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent1y);
    }

    public BigDecimal getNegativeMonthsPercent3Years() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent3y);
    }

    public BigDecimal getNegativeMonthsPercent5Years() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent5y);
    }

    public BigDecimal getNegativeMonthsPercent10Years() {
        return getQuotePriceField(RatioFieldDescription.negativeMonthsPercent10y);
    }

    public BigDecimal getMaximumLoss3y() {
        return getQuotePriceField(RatioFieldDescription.maximumLoss3y);
    }

    public Integer getMaximumLossMonths3y() {
        final BigDecimal field = getQuotePriceField(RatioFieldDescription.maximumLossMonths3y);
        return field == null ? null : field.intValue();
    }

    public BigDecimal getFeriBenchmarkPerformance1y() {
        // HACK, use non-Feri fields, TODO: remove after OK by mmf development (last check 2018-02-14), used in FndRatioData
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance1y));
    }

    public BigDecimal getFeriBenchmarkPerformance3y() {
        // HACK, use non-Feri fields, TODO: remove after OK by mmf development (last check 2018-02-14), used in FndRatioData
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance3y));
    }

    @Override
    public BigDecimal getBenchmarkOutperformance1m() {
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance1m));
    }

    @Override
    public BigDecimal getBenchmarkOutperformance6m() {
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance6m));
    }

    public BigDecimal getBenchmarkOutperformance1y() {
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance1y));
    }

    public BigDecimal getBenchmarkOutperformance3y() {
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance3y));
    }

    @Override
    public BigDecimal getBenchmarkOutperformance5y() {
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformance5y));
    }

    @Override
    public BigDecimal getBenchmarkOutperformanceCurrentYear() {
        return getQuotePriceField(this.fields.get(Field.benchmarkOutperformanceCurrentYear));
    }

    public BigDecimal getProbabilityOfOutperformance3y() {
        return getQuotePriceField(this.fields.get(Field.probabilityOfOutperformance3y));
    }

    public BigDecimal getFeriProbOfOutperf3y() {
        // HACK, use non-Feri fields, TODO: remove after OK by mmf development
        return getQuotePriceField(this.fields.get(Field.probabilityOfOutperformance3y));
    }

    public BigDecimal getAlpha1m() {
        return getQuotePriceField(RatioFieldDescription.alpha1m);
    }

    public BigDecimal getFeriAlpha3y() {
        // HACK, use non-Feri fields, TODO: remove after OK by mmf development
        return getQuotePriceField(RatioFieldDescription.alpha3y);
    }

    public BigDecimal getFeriBeta3y() {
        // HACK, use non-Feri fields, TODO: remove after OK by mmf development
        return getQuotePriceField(RatioFieldDescription.beta3y);
    }

    public Price getStockProfit() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.stockProfit);
        final Integer date = getQuoteNumberField(RatioFieldDescription.stockProfitDate);
        return new PriceImpl(price != null ? price : null, null, null,
                date != null ? DateUtil.toDateTime(Math.max(date, DateUtil.dateToYyyyMmDd()), 0) : null, PriceQuality.DELAYED);
    }

    public Price getEstateProfit() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.estateProfit);
        return new PriceImpl(price != null ? price : null, null, null, null, PriceQuality.DELAYED);
    }

    public Price getInterimProfit() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.interimProfit);
        final Integer date = getQuoteNumberField(RatioFieldDescription.interimProfitDate);
        return new PriceImpl(price != null ? price : null, null, null,
                date != null ? DateUtil.toDateTime(Math.max(date, DateUtil.dateToYyyyMmDd()), 0) : null, PriceQuality.DELAYED);
    }

    public Price getHigh1y() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.high1y);
        return new PriceImpl(price != null ? price : null, null, null, null, PriceQuality.DELAYED);
    }

    public Price getLow1y() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.low1y);
        return new PriceImpl(price != null ? price : null, null, null, null, PriceQuality.DELAYED);
    }

    public Price getHighDay() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.high);
        final Integer date = getQuoteNumberField(RatioFieldDescription.lastDate);

        //TODO: adapt price quality
        return new PriceImpl(price != null ? price : null, null, null,
                date != null ? DateUtil.toDateTime(Math.max(date, DateUtil.dateToYyyyMmDd()), 0) : null, PriceQuality.DELAYED);
    }

    public Price getLowDay() {
        final BigDecimal price = getQuotePriceField(RatioFieldDescription.low);
        final Integer date = getQuoteNumberField(RatioFieldDescription.lastDate);

        //TODO: adapt price quality
        return new PriceImpl(price != null ? price : null, null, null,
                date != null ? DateUtil.toDateTime(Math.max(date, DateUtil.dateToYyyyMmDd()), 0) : null, PriceQuality.DELAYED);
    }

    public String getSubtypeGatrixx() {
        return getInstrumentStringField(RatioFieldDescription.gatrixxType);
    }

    public String getGuaranteeTypeGatrixx() {
        return getInstrumentStringField(RatioFieldDescription.gatrixxGuaranteeType);
    }

    public String getLeverageTypeGatrixx() {
        return getInstrumentStringField(RatioFieldDescription.gatrixxLeverageType);
    }

    public BigDecimal getStrikePriceGatrixx() {
        return getInstrumentPriceField(RatioFieldDescription.gatrixxStrikePrice);
    }

    public BigDecimal getCouponGatrixx() {
        return getInstrumentPriceField(RatioFieldDescription.gatrixxCoupon);
    }

    public BigDecimal getCapGatrixx() {
        return getInstrumentPriceField(RatioFieldDescription.gatrixxCap);
    }

    public BigDecimal getKnockinGatrixx() {
        return getInstrumentPriceField(RatioFieldDescription.gatrixxKnockin);
    }

    public BigDecimal getBonuslevelGatrixx() {
        return getInstrumentPriceField(RatioFieldDescription.gatrixxBonuslevel);
    }

    public BigDecimal getBonusLevel() {
        return getInstrumentPriceField(this.fields.get(RatioDataRecord.Field.bonusLevel));
    }

    public BigDecimal getBarrierGatrixx() {
        return getInstrumentPriceField(RatioFieldDescription.gatrixxBarrier);
    }

    public BigDecimal getDiscountPercent() {
        return getQuotePriceField(this.fields.get(RatioDataRecord.Field.discountRelative));
    }

    public Boolean isEtf() {
        return getInstrumentBooleanField(this.fields.get(RatioDataRecord.Field.etf));
    }

    public BigDecimal getYieldPerAnno() {
        return getQuotePriceField(this.fields.get(RatioDataRecord.Field.yieldRelativePerYear));
    }

    public BigDecimal getMaximumYieldPercent() {
        return getQuotePriceField(this.fields.get(Field.maximumYieldRelative));
    }

    public BigDecimal getCapLevel() {
        return getQuotePriceField(RatioFieldDescription.capLevel);
    }

    public BigDecimal getVolatilityCurrentYear() {
        return getQuotePriceField(RatioFieldDescription.volatilityCurrentYear);
    }

    public BigDecimal getVolatility1m() {
        return getQuotePriceField(RatioFieldDescription.volatility1m);
    }

    public BigDecimal getVolatility3m() {
        return getQuotePriceField(RatioFieldDescription.volatility3m);
    }

    public BigDecimal getVolatility6m() {
        return getQuotePriceField(RatioFieldDescription.volatility6m);
    }

    public BigDecimal getVolatility1y() {
        return getQuotePriceField(RatioFieldDescription.volatility1y);
    }

    public BigDecimal getVolatility3y() {
        return getQuotePriceField(RatioFieldDescription.volatility3y);
    }

    public BigDecimal getVolatility5y() {
        return getQuotePriceField(RatioFieldDescription.volatility5y);
    }

    @Override
    public BigDecimal getSharpeRatio1w() {
        return getQuotePriceField(RatioFieldDescription.sharperatio1w);
    }

    @Override
    public BigDecimal getSharpeRatio1m() {
        return getQuotePriceField(RatioFieldDescription.sharperatio1m);
    }

    @Override
    public BigDecimal getSharpeRatio3m() {
        return getQuotePriceField(RatioFieldDescription.sharperatio3m);
    }

    @Override
    public BigDecimal getSharpeRatio6m() {
        return getQuotePriceField(RatioFieldDescription.sharperatio6m);
    }

    public BigDecimal getSharpeRatio1y() {
        return getQuotePriceField(RatioFieldDescription.sharperatio1y);
    }

    public BigDecimal getSharpeRatio3y() {
        return getQuotePriceField(RatioFieldDescription.sharperatio3y);
    }

    public BigDecimal getSharpeRatio5y() {
        return getQuotePriceField(RatioFieldDescription.sharperatio5y);
    }

    @Override
    public BigDecimal getSharpeRatio10y() {
        return getQuotePriceField(RatioFieldDescription.sharperatio10y);
    }

    public BigDecimal getTreynor1y() {
        return getQuotePriceField(RatioFieldDescription.treynor1y);
    }

    public BigDecimal getTreynor3y() {
        return getQuotePriceField(RatioFieldDescription.treynor3y);
    }

    public BigDecimal getTreynor5y() {
        return getQuotePriceField(RatioFieldDescription.treynor5y);
    }

    public BigDecimal getInformationRatio1y() {
        return getQuotePriceField(RatioFieldDescription.informationRatio1y);
    }

    public BigDecimal getInformationRatio3y() {
        return getQuotePriceField(RatioFieldDescription.informationRatio3y);
    }

    public BigDecimal getInformationRatio5y() {
        return getQuotePriceField(RatioFieldDescription.informationRatio5y);
    }

    public BigDecimal getSterlingRatio1y() {
        return getQuotePriceField(RatioFieldDescription.sterlingRatio1y);
    }

    public BigDecimal getSterlingRatio3y() {
        return getQuotePriceField(RatioFieldDescription.sterlingRatio3y);
    }

    public BigDecimal getSterlingRatio5y() {
        return getQuotePriceField(RatioFieldDescription.sterlingRatio5y);
    }

    public BigDecimal getTrackingError1y() {
        return getQuotePriceField(RatioFieldDescription.trackingError1y);
    }

    public BigDecimal getTrackingError3y() {
        return getQuotePriceField(RatioFieldDescription.trackingError3y);
    }

    public BigDecimal getTrackingError5y() {
        return getQuotePriceField(RatioFieldDescription.trackingError5y);
    }

    public BigDecimal getAlpha1y() {
        return getQuotePriceField(RatioFieldDescription.alpha1y);
    }

    public BigDecimal getAlpha3y() {
        return getQuotePriceField(RatioFieldDescription.alpha3y);
    }

    public BigDecimal getAlpha5y() {
        return getQuotePriceField(RatioFieldDescription.alpha5y);
    }

    @Override
    public BigDecimal getAlpha1w() {
        return getQuotePriceField(RatioFieldDescription.alpha1w);
    }

    @Override
    public BigDecimal getAlpha3m() {
        return getQuotePriceField(RatioFieldDescription.alpha3m);
    }

    @Override
    public BigDecimal getAlpha6m() {
        return getQuotePriceField(RatioFieldDescription.alpha6m);
    }

    @Override
    public BigDecimal getAlpha10y() {
        return getQuotePriceField(RatioFieldDescription.alpha10y);
    }

    public Boolean isQuanto() {
        return getInstrumentBooleanField(this.fields.get(Field.quanto));
    }

    @Override
    public Boolean isEndless() {
        return getInstrumentBooleanField(this.fields.get(Field.isEndless));
    }

    public BigDecimal getPerformance1m() {
        return getQuotePriceField(RatioFieldDescription.performance1m);
    }

    public BigDecimal getPerformance3m() {
        return getQuotePriceField(RatioFieldDescription.performance3m);
    }

    public BigDecimal getPerformance6m() {
        return getQuotePriceField(RatioFieldDescription.performance6m);
    }

    public BigDecimal getPerformance1y() {
        return getQuotePriceField(RatioFieldDescription.performance1y);
    }

    public BigDecimal getPerformance3y() {
        return getQuotePriceField(RatioFieldDescription.performance3y);
    }

    public DateTime getIssueDate() {
        return getInstrumentDateField(this.fields.get(Field.issueDate));
    }

    public BigDecimal getDiscount() {
        return getQuotePriceField(this.fields.get(Field.discount));
    }

    public BigDecimal getMaximumYield() {
        return getQuotePriceField(this.fields.get(Field.maximumYield));
    }

    public BigDecimal getYieldPercent() {
        return getQuotePriceField(this.fields.get(Field.yieldRelative));
    }

    public BigDecimal getSpread() {
        return getQuotePriceField(RatioFieldDescription.spread);
    }

    public BigDecimal getSpreadPercent() {
        return getQuotePriceField(RatioFieldDescription.spreadRelative);
    }

    public BigDecimal getUnderlyingToCap() {
        return getQuotePriceField(RatioFieldDescription.underlyingToCap);
    }

    public BigDecimal getUnderlyingToCapPercent() {
        return getQuotePriceField(RatioFieldDescription.underlyingToCapRelative);
    }

    public BigDecimal getCapToUnderlying() {
        return getQuotePriceField(RatioFieldDescription.capToUnderlying);
    }

    public BigDecimal getCapToUnderlyingPercent() {
        return getQuotePriceField(RatioFieldDescription.capToUnderlyingRelative);
    }

    public BigDecimal getRefundMaximum() {
        return getInstrumentPriceField(this.fields.get(Field.refundMaximum));
    }

    public BigDecimal getStartvalue() {
        return getInstrumentPriceField(this.fields.get(Field.startvalue));
    }

    public BigDecimal getStopvalue() {
        return getInstrumentPriceField(this.fields.get(Field.stopvalue));
    }

    public BigDecimal getMaximumYieldPerAnno() {
        return getQuotePriceField(this.fields.get(Field.maximumYieldRelativePerYear));
    }

    public BigDecimal getOutperformanceValue() {
        return getQuotePriceField(this.fields.get(Field.outperformanceValue));
    }

    public BigDecimal getUnchangedYield() {
        return getQuotePriceField(this.fields.get(Field.unchangedYield));
    }

    public BigDecimal getUnchangedYieldPercent() {
        return getQuotePriceField(this.fields.get(Field.unchangedYieldRelative));
    }

    public BigDecimal getUnchangedYieldPerAnno() {
        return getQuotePriceField(this.fields.get(Field.unchangedYieldRelativePerYear));
    }

    public String getFundtype() {
        return getInstrumentStringField(this.fields.get(Field.fundtype));
    }

    public BigDecimal getFundVolume() {
        return getInstrumentPriceField(this.fields.get(Field.fundVolume));
    }

    public String getCountry() {
        return getInstrumentStringField(this.fields.get(Field.country));
    }

    public String getDistributionStrategy() {
        return getInstrumentStringField(this.fields.get(Field.distributionStrategy));
    }

    public String getRatingFeri() {
        return getInstrumentStringField(this.fields.get(Field.ratingFeri));
    }

    public Long getRatingMorningstar() {
        return getInstrumentLongField(this.fields.get(Field.ratingMorningstar));
    }

    public String getMarketAdmission() {
        RatioFieldDescription.Field f = this.fields.get(Field.marketAdmission);
        return RatioEnumSetFactory.fromBits(f.id(), getInstrumentBitSetField(f));
    }

    public String getRegion() {
        return getInstrumentStringField(this.fields.get(Field.region));
    }

    public String getFundtypeBviCoarse() {
        return getInstrumentStringField(this.fields.get(Field.fundtypeBviCoarse));
    }

    public BigDecimal getCorrelation1w() {
        return getQuotePriceField(RatioFieldDescription.correlation1w);

    }

    public BigDecimal getCorrelation1m() {
        return getQuotePriceField(RatioFieldDescription.correlation1m);
    }

    public BigDecimal getCorrelation3m() {
        return getQuotePriceField(RatioFieldDescription.correlation3m);

    }

    public BigDecimal getCorrelation6m() {
        return getQuotePriceField(RatioFieldDescription.correlation6m);

    }

    public BigDecimal getCorrelation1y() {
        return getQuotePriceField(RatioFieldDescription.correlation1y);

    }

    public BigDecimal getCorrelation3y() {
        return getQuotePriceField(RatioFieldDescription.correlation3y);

    }

    public BigDecimal getCorrelation5y() {
        return getQuotePriceField(RatioFieldDescription.correlation5y);

    }

    public BigDecimal getCorrelation10y() {
        return getQuotePriceField(RatioFieldDescription.correlation10y);

    }

    public BigDecimal getCorrelationCurrentYear() {
        return getQuotePriceField(RatioFieldDescription.correlationCurrentYear);
    }

    public BigDecimal getAverageVolume1w() {
        return getQuotePriceField(RatioFieldDescription.averageVolume1w);

    }

    public BigDecimal getAverageVolume1m() {
        return getQuotePriceField(RatioFieldDescription.averageVolume1m);

    }

    public BigDecimal getAverageVolume3m() {
        return getQuotePriceField(RatioFieldDescription.averageVolume3m);

    }

    public BigDecimal getAverageVolume6m() {
        return getQuotePriceField(RatioFieldDescription.averageVolume6m);

    }

    public BigDecimal getAverageVolume1y() {
        return getQuotePriceField(RatioFieldDescription.averageVolume1y);

    }

    public BigDecimal getAverageVolume3y() {
        return getQuotePriceField(RatioFieldDescription.averageVolume3y);

    }

    public BigDecimal getAverageVolume5y() {
        return getQuotePriceField(RatioFieldDescription.averageVolume5y);

    }

    public BigDecimal getAverageVolume10y() {
        return getQuotePriceField(RatioFieldDescription.averageVolume10y);

    }

    @Override
    public BigDecimal getAveragePrice1w() {
        return getQuotePriceField(RatioFieldDescription.averagePrice1w);
    }

    @Override
    public BigDecimal getAveragePrice1m() {
        return getQuotePriceField(RatioFieldDescription.averagePrice1m);
    }

    @Override
    public BigDecimal getAveragePrice3m() {
        return getQuotePriceField(RatioFieldDescription.averagePrice3m);
    }

    @Override
    public BigDecimal getAveragePrice6m() {
        return getQuotePriceField(RatioFieldDescription.averagePrice6m);
    }

    @Override
    public BigDecimal getAveragePrice1y() {
        return getQuotePriceField(RatioFieldDescription.averagePrice1y);
    }

    @Override
    public BigDecimal getAveragePrice3y() {
        return getQuotePriceField(RatioFieldDescription.averagePrice3y);
    }

    @Override
    public BigDecimal getAveragePrice5y() {
        return getQuotePriceField(RatioFieldDescription.averagePrice5y);
    }

    @Override
    public BigDecimal getAveragePrice10y() {
        return getQuotePriceField(RatioFieldDescription.averagePrice10y);
    }


    public BigDecimal getPerformanceToBenchmark1d() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark1d);
    }

    public BigDecimal getPerformanceToBenchmark1w() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark1w);
    }

    public BigDecimal getPerformanceToBenchmark1m() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark1m);

    }

    public BigDecimal getPerformanceToBenchmark3m() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark3m);

    }

    public BigDecimal getPerformanceToBenchmark6m() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark6m);

    }

    public BigDecimal getPerformanceToBenchmark1y() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark1y);

    }

    public BigDecimal getPerformanceToBenchmark3y() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark3y);

    }

    public BigDecimal getPerformanceToBenchmark5y() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark5y);

    }

    public BigDecimal getPerformanceToBenchmark10y() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmark10y);

    }

    public BigDecimal getPerformanceToBenchmarkCurrentYear() {
        return getQuotePriceField(RatioFieldDescription.performanceToBenchmarkCurrentYear);
    }

    public BigDecimal getPerformance1w() {
        return getQuotePriceField(RatioFieldDescription.performance1w);
    }

    public BigDecimal getPerformance5y() {
        return getQuotePriceField(RatioFieldDescription.performance5y);
    }

    public BigDecimal getPerformance10y() {
        return getQuotePriceField(RatioFieldDescription.performance10y);
    }

    public BigDecimal getPerformanceCurrentYear() {
        return getQuotePriceField(RatioFieldDescription.performanceCurrentYear);
    }

    public BigDecimal getPerformanceAlltime() {
        return getQuotePriceField(this.fields.get(Field.performanceAlltime));
    }

    public BigDecimal getChangePercentHigh1y() {
        return getQuotePriceField(RatioFieldDescription.changePercentHigh52Weeks);
    }

    public BigDecimal getChangePercentLow1y() {
        return getQuotePriceField(RatioFieldDescription.changePercentLow52Weeks);
    }

    public BigDecimal getChangePercentHighAlltime() {
        return getQuotePriceField(RatioFieldDescription.changePercentAlltimeHigh);
    }

    public BigDecimal getVolatility1w() {
        return getQuotePriceField(RatioFieldDescription.volatility1w);
    }

    public BigDecimal getVolatility10y() {
        return getQuotePriceField(RatioFieldDescription.volatility10y);
    }

    public Long getMarketCapitalization() {
        return getQuoteLongField(RatioFieldDescription.marketCapitalization);

    }

    public Long getMarketCapitalizationUSD() {
        return getQuoteLongField(RatioFieldDescription.marketCapitalizationUSD);

    }

    public Long getMarketCapitalizationEUR() {
        return getQuoteLongField(RatioFieldDescription.marketCapitalizationEUR);

    }

    public BigDecimal getBeta1m() {
        return getQuotePriceField(RatioFieldDescription.beta1m);

    }

    public BigDecimal getBeta1y() {
        return getQuotePriceField(RatioFieldDescription.beta1y);
    }

    public BigDecimal getBeta3y() {
        return getQuotePriceField(RatioFieldDescription.beta3y);
    }

    public BigDecimal getPriceSalesRatio1y() {
        return getQuotePriceField(this.fields.get(Field.priceSalesRatio1y));
    }

    public BigDecimal getPriceSalesRatio2y() {
        return getQuotePriceField(this.fields.get(Field.priceSalesRatio2y));
    }

    public BigDecimal getPriceSalesRatio3y() {
        return getQuotePriceField(this.fields.get(Field.priceSalesRatio3y));
    }

    public BigDecimal getPriceSalesRatio4y() {
        return getQuotePriceField(this.fields.get(Field.priceSalesRatio4y));
    }

    public BigDecimal getPriceEarningRatio1y() {
        return getInstrumentPriceField(this.fields.get(Field.priceEarningRatio1y));
    }

    public BigDecimal getPriceEarningRatio2y() {
        return getInstrumentPriceField(this.fields.get(Field.priceEarningRatio2y));
    }

    public BigDecimal getPriceEarningRatio3y() {
        return getInstrumentPriceField(this.fields.get(Field.priceEarningRatio3y));
    }

    public BigDecimal getPriceEarningRatio4y() {
        return getInstrumentPriceField(this.fields.get(Field.priceEarningRatio4y));
    }

    public BigDecimal getPriceBookvalueRatio1y() {
        return getQuotePriceField(this.fields.get(Field.priceBookvalueRatio1y));
    }

    public BigDecimal getPriceBookvalueRatio2y() {
        return getQuotePriceField(this.fields.get(Field.priceBookvalueRatio2y));
    }

    public BigDecimal getPriceCashflowRatio1y() {
        return getQuotePriceField(this.fields.get(Field.priceCashflowRatio1y));
    }

    public BigDecimal getPriceCashflowRatio2y() {
        return getQuotePriceField(this.fields.get(Field.priceCashflowRatio2y));
    }

    public BigDecimal getPriceCashflowRatio3y() {
        return getQuotePriceField(this.fields.get(Field.priceCashflowRatio3y));
    }

    public BigDecimal getPriceCashflowRatio4y() {
        return getQuotePriceField(this.fields.get(Field.priceCashflowRatio4y));
    }

    public BigDecimal getDividend1y() {
        return getInstrumentPriceField(this.fields.get(Field.dividend1y));
    }

    public BigDecimal getDividend2y() {
        return getInstrumentPriceField(this.fields.get(Field.dividend2y));
    }

    public BigDecimal getDividendYield1y() {
        return getInstrumentPriceField(this.fields.get(Field.dividendYield1y));
    }

    public BigDecimal getDividendYield2y() {
        return getInstrumentPriceField(this.fields.get(Field.dividendYield2y));
    }

    public BigDecimal getDividendYield3y() {
        return getInstrumentPriceField(this.fields.get(Field.dividendYield3y));
    }

    public BigDecimal getDividendYield4y() {
        return getInstrumentPriceField(this.fields.get(Field.dividendYield4y));
    }

    public BigDecimal getSales1y() {
        return getInstrumentPriceField(this.fields.get(Field.sales1y));
    }

    public BigDecimal getSales2y() {
        return getInstrumentPriceField(this.fields.get(Field.sales2y));
    }

    public BigDecimal getSales3y() {
        return getInstrumentPriceField(this.fields.get(Field.sales3y));
    }

    public BigDecimal getSales4y() {
        return getInstrumentPriceField(this.fields.get(Field.sales4y));
    }

    public BigDecimal getProfit1y() {
        return getInstrumentPriceField(this.fields.get(Field.profit1y));
    }

    public BigDecimal getProfit2y() {
        return getInstrumentPriceField(this.fields.get(Field.profit2y));
    }

    public BigDecimal getProfit3y() {
        return getInstrumentPriceField(this.fields.get(Field.profit3y));
    }

    public BigDecimal getProfit4y() {
        return getInstrumentPriceField(this.fields.get(Field.profit4y));
    }

    public BigDecimal getEBIT1y() {
        return getInstrumentPriceField(this.fields.get(Field.ebit1y));
    }

    public BigDecimal getEBIT2y() {
        return getInstrumentPriceField(this.fields.get(Field.ebit2y));
    }

    public BigDecimal getEBIT3y() {
        return getInstrumentPriceField(this.fields.get(Field.ebit3y));
    }

    public BigDecimal getEBIT4y() {
        return getInstrumentPriceField(this.fields.get(Field.ebit4y));
    }

    public BigDecimal getEBITDA1y() {
        return getInstrumentPriceField(this.fields.get(Field.ebitda1y));
    }

    public BigDecimal getEBITDA2y() {
        return getInstrumentPriceField(this.fields.get(Field.ebitda2y));
    }

    public BigDecimal getEBITDA3y() {
        return getInstrumentPriceField(this.fields.get(Field.ebitda3y));
    }

    public BigDecimal getEBITDA4y() {
        return getInstrumentPriceField(this.fields.get(Field.ebitda4y));
    }

    public Long getScreenerInterest() {
        return getInstrumentLongField(RatioFieldDescription.screenerInterest);
    }

    public String getWarrantType() {
        return getInstrumentStringField(this.fields.get(Field.warrantType));
    }

    public Boolean isAmerican() {
        return getInstrumentBooleanField(this.fields.get(Field.isAmerican));
    }

    public BigDecimal getStrike() {
        return getInstrumentPriceField(this.fields.get(Field.strike));
    }

    public DateTime getExpirationDate() {
        return getInstrumentDateField(this.fields.get(Field.expirationDate));
    }

    public DateTime getTradingMonth() {
        return getInstrumentDateField(this.fields.get(Field.tradingMonth));
    }

    public BigDecimal getOmega() {
        return getQuotePriceField(this.fields.get(Field.omega));
    }

    public BigDecimal getImpliedVolatility() {
        return getQuotePriceField(this.fields.get(Field.impliedVolatility));
    }

    public BigDecimal getDelta() {
        return getQuotePriceField(this.fields.get(Field.delta));
    }

    public BigDecimal getIntrinsicValue() {
        return getQuotePriceField(this.fields.get(Field.intrinsicValue));
    }

    public BigDecimal getExtrinsicValue() {
        return getQuotePriceField(this.fields.get(Field.extrinsicValue));
    }

    public BigDecimal getOptionPrice() {
        return getQuotePriceField(this.fields.get(Field.optionPrice));
    }

    @Override
    public BigDecimal getOptionPricePerYear() {
        return getQuotePriceField(this.fields.get(Field.optionPricePerYear));
    }

    public BigDecimal getFairValue() {
        return getQuotePriceField(this.fields.get(Field.fairValue));
    }

    public BigDecimal getBreakeven() {
        return getQuotePriceField(this.fields.get(Field.breakeven));
    }

    public BigDecimal getTheta() {
        return getQuotePriceField(this.fields.get(Field.theta));
    }

    public BigDecimal getRho() {
        return getQuotePriceField(this.fields.get(Field.rho));
    }

    public BigDecimal getGamma() {
        return getQuotePriceField(this.fields.get(Field.gamma));
    }

    public BigDecimal getLeverage() {
        return getQuotePriceField(this.fields.get(Field.leverage));
    }

    public BigDecimal getVega() {
        return getQuotePriceField(this.fields.get(Field.vega));
    }

    public String getUnderlyingIsin() {
        return getInstrumentStringField(this.fields.get(Field.underlyingIsin));
    }

    public String getCertificateType() {
        return getInstrumentStringField(this.fields.get(Field.certificateType));
    }

    public String getCertificateSubtype() {
        return getInstrumentStringField(this.fields.get(Field.certificateSubtype));
    }

    public String getMultiassetName() {
        return getInstrumentStringField(this.fields.get(Field.multiassetName));
    }

    public String getCertificateTypeDZBANK() {
        return getInstrumentStringField(this.fields.get(Field.certificateTypeDZBANK));
    }

    public BigDecimal getCap() {
        return getInstrumentPriceField(this.fields.get(Field.cap));
    }

    public BigDecimal getGapCap() {
        return getQuotePriceField(this.fields.get(Field.gapCap));
    }

    public Boolean getIsknockout() {
        return getInstrumentBooleanField(this.fields.get(Field.isknockout));
    }

    public BigDecimal getGapCapRelative() {
        return getQuotePriceField(this.fields.get(Field.gapCapRelative));
    }

    public BigDecimal getProtectLevel() {
        return getInstrumentPriceField(this.fields.get(Field.protectLevel));
    }

    public BigDecimal getGapBonusLevel() {
        return getQuotePriceField(this.fields.get(Field.gapBonusLevel));
    }

    public BigDecimal getGapBonusLevelRelative() {
        return getQuotePriceField(this.fields.get(Field.gapBonusLevelRelative));
    }

    public BigDecimal getGapBonusBufferRelative() {
        return getQuotePriceField(this.fields.get(Field.gapBonusBufferRelative));
    }

    public BigDecimal getGapBarrier() {
        return getQuotePriceField(this.fields.get(Field.gapBarrier));
    }

    public BigDecimal getGapBarrierRelative() {
        return getQuotePriceField(this.fields.get(Field.gapBarrierRelative));
    }

    public BigDecimal getGapLowerBarrier() {
        return getQuotePriceField(this.fields.get(Field.gapLowerBarrier));
    }

    public BigDecimal getGapUpperBarrier() {
        return getQuotePriceField(this.fields.get(Field.gapUpperBarrier));
    }

    public BigDecimal getGapLowerBarrierRelative() {
        return getQuotePriceField(this.fields.get(Field.gapLowerBarrierRelative));
    }

    public BigDecimal getGapUpperBarrierRelative() {
        return getQuotePriceField(this.fields.get(Field.gapUpperBarrierRelative));
    }

    public DateTime getKnockinDate() {
        return getInstrumentDateField(this.fields.get(Field.knockindate));
    }

    public DateTime getDateBarrierReached() {
        return getQuoteTimestampField(this.fields.get(Field.dateBarrierReached));
    }

    public BigDecimal getAgio() {
        return getQuotePriceField(this.fields.get(Field.agio));
    }

    public BigDecimal getAgioRelative() {
        return getQuotePriceField(this.fields.get(Field.agioRelative));
    }

    public BigDecimal getAgioRelativePerYear() {
        return getQuotePriceField(this.fields.get(Field.agioRelativePerYear));
    }

    public BigDecimal getCapToUnderlyingRelative() {
        return getQuotePriceField(this.fields.get(Field.capToUnderlyingRelative));
    }

    public BigDecimal getUnderlyingToCapRelative() {
        return getQuotePriceField(this.fields.get(Field.underlyingToCapRelative));
    }

    public BigDecimal getGapStrike() {
        return getQuotePriceField(this.fields.get(Field.gapStrike));
    }

    public BigDecimal getGapStrikeRelative() {
        return getQuotePriceField(this.fields.get(Field.gapStrikeRelative));
    }

    public BigDecimal getParticipationLevel() {
        return getInstrumentPriceField(this.fields.get(Field.participationLevel));
    }

    public BigDecimal getParticipationFactor() {
        return getInstrumentPriceField(this.fields.get(Field.participationFactor));
    }

    public BigDecimal getStoploss() {
        return getInstrumentPriceField(this.fields.get(Field.stoploss));
    }

    public Long getRiskclassAttrax() {
        return getInstrumentLongField(RatioFieldDescription.attraxRiskclass);
    }

    public BigDecimal getCoupon() {
        return getInstrumentPriceField(this.fields.get(Field.coupon));
    }

    public BigDecimal getNominalInterest() {
        return getInstrumentPriceField(this.fields.get(Field.nominalInterest));
    }

    public String getCouponType() {
        return getInstrumentStringField(this.fields.get(Field.couponType));
    }

    public String getBondType() {
        return getInstrumentStringField(this.fields.get(Field.bondType));
    }

    @Override
    public String getBondRank() {
        return getInstrumentStringField(this.fields.get(Field.bondRank));
    }

    public String getOptionType() {
        return getInstrumentStringField(this.fields.get(Field.optionType));
    }

    public String getOptionCategory() {
        return getInstrumentStringField(this.fields.get(Field.optionCategory));
    }

    public String getExerciseType() {
        return getInstrumentStringField(this.fields.get(Field.exerciseType));
    }

    public String getType() {
        return getInstrumentStringField(this.fields.get(Field.type));
    }

    public String getTypeKey() {
        return getInstrumentStringField(this.fields.get(Field.typeKey));
    }

    public String getSubtype() {
        return getInstrumentStringField(this.fields.get(Field.subtype));
    }

    public String getSubtypeKey() {
        return getInstrumentStringField(this.fields.get(Field.subtypeKey));
    }

    public BigDecimal getThetaRelative() {
        return getQuotePriceField(this.fields.get(Field.thetaRelative));
    }

    public BigDecimal getTheta1w() {
        return getQuotePriceField(this.fields.get(Field.theta1w));
    }

    public BigDecimal getTheta1wRelative() {
        return getQuotePriceField(this.fields.get(Field.theta1wRelative));
    }

    public BigDecimal getTheta1m() {
        return getQuotePriceField(this.fields.get(Field.theta1m));
    }

    public BigDecimal getTheta1mRelative() {
        return getQuotePriceField(this.fields.get(Field.theta1mRelative));
    }

    @Override
    public BigDecimal getDividend() {
        return getInstrumentPriceField(this.fields.get(Field.dividend));
    }

    @Override
    public BigDecimal getDividendYield() {
        return getQuotePriceField(this.fields.get(Field.dividendYield));
    }

    @Override
    public String getDividendCurrency() {
        return getInstrumentStringField(this.fields.get(Field.dividendCurrency));
    }

    @Override
    public DateTime getExternalReferenceTimestamp() {
        return getQuoteTimestampField(RatioFieldDescription.externalReferenceTimestamp);
    }

    @Override
    public String getIssueCurrency() {
        return getInstrumentStringField(this.fields.get(Field.issueCurrency));
    }

    @Override
    public BigDecimal getTurnoverDay() {
        return getQuotePriceField(this.fields.get(Field.turnoverDay));
    }

    public String getCurrency() {
        return getInstrumentStringField(this.fields.get(Field.currency));
    }

    public Long getWgzListid() {
        return getInstrumentNumberField(this.fields.get(Field.wgzListid));
    }

    public String getRatingFitchLongTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingFitchLongTerm));
    }

    @Override
    public DateTime getRatingFitchLongTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingFitchLongTermDate));
    }

    @Override
    public String getRatingFitchLongTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingFitchLongTermAction));
    }

    public String getRatingFitchShortTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingFitchShortTerm));
    }

    @Override
    public DateTime getRatingFitchShortTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingFitchShortTermDate));
    }

    @Override
    public String getRatingFitchShortTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingFitchShortTermAction));
    }

    @Override
    public String getRatingMoodysLongTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingMoodysLongTerm));
    }

    @Override
    public DateTime getRatingMoodysLongTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingMoodysLongTermDate));
    }

    @Override
    public String getRatingMoodysLongTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingMoodysLongTermAction));
    }

    @Override
    public String getRatingMoodysLongTermSource() {
        return getInstrumentStringField(this.fields.get(Field.ratingMoodysLongTermSource));
    }

    @Override
    public String getRatingMoodysShortTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingMoodysShortTerm));
    }

    @Override
    public DateTime getRatingMoodysShortTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingMoodysShortTermDate));
    }

    @Override
    public String getRatingMoodysShortTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingMoodysShortTermAction));
    }

    @Override
    public String getRatingMoodysShortTermSource() {
        return getInstrumentStringField(this.fields.get(Field.ratingMoodysShortTermSource));
    }

    public BigDecimal getBasePointValue() {
        return getQuotePriceField(this.fields.get(Field.basePointValue));
    }

    public BigDecimal getDuration() {
        return getQuotePriceField(this.fields.get(Field.duration));
    }

    public BigDecimal getConvexity() {
        return getQuotePriceField(this.fields.get(Field.convexity));
    }

    public BigDecimal getModifiedDuration() {
        return getQuotePriceField(this.fields.get(Field.modifiedDuration));
    }

    public BigDecimal getBrokenPeriodInterest() {
        return getQuotePriceField(this.fields.get(Field.brokenPeriodInterest));
    }

    public BigDecimal getInterestRateElasticity() {
        return getQuotePriceField(this.fields.get(Field.interestRateElasticity));
    }

    public String getProductnameIssuer() {
        return getInstrumentStringField(RatioFieldDescription.issuerProductname);
    }

    public String getIssuerCategory() {
        return getInstrumentStringField(RatioFieldDescription.issuerCategory);
    }

    public BigDecimal getRecommendation() {
        return getInstrumentPriceField(this.fields.get(Field.recommendation));
    }

    public Long getFiscalYear() {
        return getInstrumentNumberField(this.fields.get(Field.fiscalYear));
    }

    public DateTime getEdgRatingDate() {
        return getInstrumentDateField(this.fields.get(Field.edgRatingDate));
    }

    public Long getEdgScore1() {
        return getInstrumentNumberField(this.fields.get(Field.edgScore1));
    }

    public Long getEdgScore2() {
        return getInstrumentNumberField(this.fields.get(Field.edgScore2));
    }

    public Long getEdgScore3() {
        return getInstrumentNumberField(this.fields.get(Field.edgScore3));
    }

    public Long getEdgScore4() {
        return getInstrumentNumberField(this.fields.get(Field.edgScore4));
    }

    public Long getEdgScore5() {
        return getInstrumentNumberField(this.fields.get(Field.edgScore5));
    }

    public Long getEdgTopClass() {
        return getInstrumentNumberField(this.fields.get(Field.edgTopClass));
    }

    public Long getEdgTopScore() {
        return getInstrumentNumberField(this.fields.get(Field.edgTopScore));
    }

    public String getBenchmarkName() {
        return getInstrumentStringField(this.fields.get(Field.benchmarkName));
    }

    public DateTime getExpirationDateForMatrix() {
        // HACK
        final DateTime stdField = getInstrumentDateField(RatioFieldDescription.expires);
        if (stdField != null) {
            return stdField;
        }
        return getInstrumentDateField(RatioFieldDescription.smfExpires);
    }

    public String getWarrantTypeForMatrix() {
        // HACK
        final String stdField = getInstrumentStringField(RatioFieldDescription.osType);
        if (stdField != null) {
            return stdField;
        }
        return getInstrumentStringField(RatioFieldDescription.smfLeverageType);
    }

    public BigDecimal getStrikeForMatrix() {
        // HACK
        final BigDecimal stdField = getInstrumentPriceField(RatioFieldDescription.strikePrice);
        if (stdField != null) {
            return stdField;
        }
        return getInstrumentPriceField(RatioFieldDescription.smfStrike);
    }

    public BigDecimal getOngoingCharge() {
        return getInstrumentPriceField(this.fields.get(Field.ongoingCharge));
    }

    public DateTime getOngoingChargeDate() {
        return getInstrumentDateField(this.fields.get(Field.ongoingChargeDate));
    }

    @Override
    public BigDecimal getTickSize() {
        return getInstrumentPriceField(this.fields.get(Field.tickSize));
    }

    @Override
    public BigDecimal getTickValue() {
        return getInstrumentPriceField(this.fields.get(Field.tickValue));
    }

    @Override
    public String getTickCurrency() {
        return getInstrumentStringField(this.fields.get(Field.tickCurrency));
    }

    @Override
    public BigDecimal getContractValue() {
        return getInstrumentPriceField(this.fields.get(Field.contractValue));
    }

    @Override
    public BigDecimal getContractValueCalculated() {
        return getInstrumentPriceField(this.fields.get(Field.contractValueCalculated));
    }

    @Override
    public String getContractCurrency() {
        return getInstrumentStringField(this.fields.get(Field.contractCurrency));
    }

    @Override
    public BigDecimal getContractSize() {
        return getInstrumentPriceField(this.fields.get(Field.contractSize));
    }

    @Override
    public Long getGenerationNumber() {
        return getInstrumentLongField(this.fields.get(Field.generationNumber));
    }

    @Override
    public Long getVersionNumber() {
        return getInstrumentLongField(this.fields.get(Field.versionNumber));
    }

    @Override
    public String getRatingSnPLongTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLongTerm));
    }

    @Override
    public DateTime getRatingSnPLongTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingSnPLongTermDate));
    }

    @Override
    public String getRatingSnPLongTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLongTermAction));
    }

    @Override
    public String getRatingSnPLongTermRegulatoryId() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLongTermRegulatoryId));
    }

    @Override
    public String getRatingSnPLongTermQualifier() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLongTermQualifier));
    }

    @Override
    public String getRatingSnPShortTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPShortTerm));
    }

    @Override
    public DateTime getRatingSnPShortTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingSnPShortTermDate));
    }

    @Override
    public String getRatingSnPShortTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPShortTermAction));
    }

    @Override
    public String getZnsCategory() {
        return getInstrumentStringField(this.fields.get(Field.znsCategory));
    }

    @Override
    public String getMaturity() {
        return getInstrumentStringField(this.fields.get(Field.maturity));
    }

    @Override
    public String getDebtRanking() {
        return getInstrumentStringField(this.fields.get(Field.debtRanking));
    }

    @Override
    public String getIssuerType() {
        return getInstrumentStringField(this.fields.get(Field.issuerType));
    }

    @Override
    public String getRestructuringRule() {
        return getInstrumentStringField(this.fields.get(Field.restructuringRule));
    }

    @Override
    public String getSource() {
        return getInstrumentStringField(this.fields.get(Field.source));
    }

    @Override
    public Long getSrriValue() {
        return getInstrumentLongField(this.fields.get(Field.srriValue));
    }

    @Override
    public DateTime getSrriValueDate() {
        return getInstrumentDateField(this.fields.get(Field.srriValueDate));
    }

    @Override
    public Long getDiamondRating() {
        return getInstrumentLongField(this.fields.get(Field.diamondRating));
    }

    @Override
    public DateTime getDiamondRatingDate() {
        return getInstrumentDateField(this.fields.get(Field.diamondRatingDate));
    }

    @Override
    public Long getFidaRating() {
        return getInstrumentLongField(this.fields.get(Field.fidaRating));
    }

    @Override
    public String getSedexIssuerName() {
        return getInstrumentStringField(this.fields.get(Field.sedexIssuerName));
    }

    @Override
    public Long getSedexStrike() {
        return getInstrumentLongField(this.fields.get(Field.sedexStrike));
    }

    @Override
    public DateTime getSedexIssueDate() {
        return getInstrumentDateField(this.fields.get(Field.sedexIssueDate));
    }

    @Override
    public DateTime getSedexExpires() {
        return getInstrumentDateField(this.fields.get(Field.sedexExpires));
    }

    @Override
    public BigDecimal getSmallestTransferableUnit() {
        return getInstrumentPriceField(this.fields.get(Field.smallestTransferableUnit));
    }

    @Override
    public Long getFwwRiskclass() {
        return getInstrumentNumberField(this.fields.get(Field.fwwRiskclass));
    }

    @Override
    public String getLmeMetalCode() {
        return getInstrumentStringField(this.fields.get(Field.lmeMetalCode));
    }

    @Override
    public DateTime getLmeExpirationDate() {
        return getQuoteDateField(this.fields.get(Field.lmeExpirationDate));
    }

    @Override
    public String getWmInvestmentAssetPoolClass() {
        return getInstrumentStringField(this.fields.get(Field.wmInvestmentAssetPoolClass));
    }

    @Override
    public Boolean isSpecialDismissal() {
        return getInstrumentBooleanField(this.fields.get(Field.isSpecialDismissal));
    }

    @Override
    public String getGicsSector() {
        return getInstrumentStringField(this.fields.get(Field.gicsSector));
    }

    @Override
    public String getGicsIndustryGroup() {
        return getInstrumentStringField(this.fields.get(Field.gicsIndustryGroup));
    }

    @Override
    public String getGicsIndustry() {
        return getInstrumentStringField(this.fields.get(Field.gicsIndustry));
    }

    @Override
    public String getGicsSubIndustry() {
        return getInstrumentStringField(this.fields.get(Field.gicsSubIndustry));
    }

    @Override
    public Boolean isLmeComposite() {
        return getQuoteBooleanField(this.fields.get(Field.lmeComposite));
    }

    @Override
    public String getRatingSnPLocalLongTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLocalLongTerm));
    }

    @Override
    public DateTime getRatingSnPLocalLongTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingSnPLocalLongTermDate));
    }

    @Override
    public String getRatingSnPLocalShortTerm() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLocalShortTerm));
    }

    @Override
    public DateTime getRatingSnPLocalShortTermDate() {
        return getInstrumentDateField(this.fields.get(Field.ratingSnPLocalShortTermDate));
    }

    @Override
    public String getGicsSectorKey() {
        return getInstrumentStringField(this.fields.get(Field.gicsSectorKey));
    }

    @Override
    public String getGicsIndustryGroupKey() {
        return getInstrumentStringField(this.fields.get(Field.gicsIndustryGroupKey));
    }

    @Override
    public String getGicsIndustryKey() {
        return getInstrumentStringField(this.fields.get(Field.gicsIndustryKey));
    }

    @Override
    public String getGicsSubIndustryKey() {
        return getInstrumentStringField(this.fields.get(Field.gicsSubIndustryKey));
    }

    @Override
    public String getMerHandelsmonat() {
        return getQuoteStringField(this.fields.get(Field.merHandelsmonat));
    }

    @Override
    public String getMerInstrumentenTyp() {
        return getQuoteStringField(this.fields.get(Field.merInstrumentenTyp));
    }

    @Override
    public String getLei() {
        return getInstrumentStringField(this.fields.get(Field.lei));
    }

    @Override
    public BigDecimal getVolatility() {
        return getQuotePriceField(this.fields.get(Field.volatility));
    }

    @Override
    public String getRating() {
        return getQuoteStringField(this.fields.get(Field.rating));
    }

    @Override
    public String getStandard() {
        return getQuoteStringField(this.fields.get(Field.standard));
    }

    @Override
    public String getFidaPermissionType() {
        return getInstrumentStringField(this.fields.get(Field.fidaPermissionType));
    }

    @Override
    public Boolean isFlex() {
        return getInstrumentBooleanField(this.fields.get(Field.isFlex));
    }

    @Override
    public BigDecimal getMinimumInvestmentAmount() {
        return getInstrumentPriceField(this.fields.get(Field.minimumInvestmentAmount));
    }

    @Override
    public String getRatingSnPLongTermSource() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLongTermSource));
    }

    @Override
    public String getRatingSnPShortTermSource() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPShortTermSource));
    }

    @Override
    public String getRatingSnPLocalLongTermSource() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLocalLongTermSource));
    }

    @Override
    public String getRatingSnPLocalShortTermSource() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLocalShortTermSource));
    }

    @Override
    public String getRatingSnPLocalLongTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLocalLongTermAction));
    }

    @Override
    public String getRatingSnPLocalShortTermAction() {
        return getInstrumentStringField(this.fields.get(Field.ratingSnPLocalShortTermAction));
    }

}
