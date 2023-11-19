/*
 * EmbeddedCalculator.java
 *
 * Created on 19.10.2005 08:02:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import static de.marketmaker.istar.ratios.backend.EmbeddedCalculatorMath.percentValueRatio;
import static de.marketmaker.istar.ratios.backend.EmbeddedCalculatorMath.priceValueRatio;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EmbeddedCalculator implements Calculator {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private StaticDataProvider staticDataProvider;

    private PriceProvider priceProvider;

    private RatiosInstrumentRepository instrumentRepository;

    private final RatiosEncoder encoder = new RatiosEncoder();

    private boolean trace = false;

    private long lastCalcMillis;

    public void setPriceProvider(PriceProvider priceProvider) {
        this.priceProvider = priceProvider;
    }

    public void setInstrumentRepository(RatiosInstrumentRepository instrumentRepository) {
        this.instrumentRepository = instrumentRepository;
    }

    public void setStaticDataProvider(StaticDataProvider staticDataProvider) {
        this.staticDataProvider = staticDataProvider;
    }

    public String toString() {
        return "EmbeddedCalculator";
    }

    @Override
    public void calc(List<CalcData> toCalc, ComputedRatiosHandler handler) {
        this.lastCalcMillis = System.currentTimeMillis();
        for (final CalcData calcData : toCalc) {
            final Instrument instrument = calcData.getQuote().getInstrument();

            this.encoder.reset(instrument.getInstrumentType(), instrument.getId(),
                calcData.getQuote().getId());

            this.trace = calcData.isTrace();
            if (this.trace) {
                this.logger.info("TRACE " + calcData);
            }

            addTimestamp(calcData);

            switch (instrument.getInstrumentType()) {
                case CER:
                    delegate(calcData, this::calcStandard);
                    delegate(calcData, this::calcCertificates);
                    break;
                case WNT:
                case FND:
                    delegate(calcData, this::calcStandard);
                    break;
                case BND:
                    delegate(calcData, this::calcStandard);
                    delegate(calcData, this::calcBonds);
                    break;
                case STK:
                    delegate(calcData, this::calcStandard);
                    delegate(calcData, this::calcStocks);
                    break;
                case GNS:
                    delegate(calcData, this::calcStandard);
                    delegate(calcData, this::calcGNS);
                    break;
                case OPT:
                case FUT:
                    delegate(calcData, this::calcFutOpt);
                    break;
                case MER:
                    delegate(calcData, this::calcMER);
                    break;
                case ZNS:
                    delegate(calcData, this::calcZNS);
                    break;
                default:
                    // ignored
            }

            final byte[] encoded = this.encoder.getData();
            if (this.trace) {
                this.logger.info("TRACE <calc> {}", RatiosDecoder.decode(encoded));
            }
            handler.handle(new ComputedRatios(calcData.getQuote().getId(), encoded));
        }
    }

    private void delegate(CalcData calcData, Consumer<CalcData> consumer) {
        try {
            consumer.accept(calcData);
        } catch (Throwable t) {
            this.logger.warn("<delegate> failed for {} {}",
                calcData.getInstrumentType(),
                calcData.getQuote()
            );
            throw t;
        }
    }

    public long unusedSinceMillis() {
        return System.currentTimeMillis() - this.lastCalcMillis;
    }

    private void calcFutOpt(CalcData calcData) {
        this.encoder.add(RatioFieldDescription.totalVolume, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Umsatz_gesamt.id())));
    }

    private void calcCertificates(CalcData calcData) {
        final SnapWrapper sw = SnapWrapper.create(calcData.getSnap(), calcData.getQuote());
        final SnapWrapper swu = SnapWrapper.create(calcData.getReferenceSnap(), null);
        final StaticData staticData = this.staticDataProvider.get(calcData.getQuote().getInstrument().getId());
        final double currencyConvertFactor = getCurrencyConvertFactorForStrike(staticData);

        this.encoder.add(RatioFieldDescription.discountPrice, checkAssert(calcData, Assertions.ASK, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.discount(sw, swu, staticData)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.discountPriceRelative, checkAssert(calcData, Assertions.ASK, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.discountRelative(sw, swu, staticData)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.discountPriceRelativePerYear, checkAssert(calcData, Assertions.ASK, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING, Assertions.EXPIRES)
                ? EmbeddedCalculatorMath.discountRelativePerYear(sw, swu, staticData)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.unchangedEarning, checkAssert(calcData, Assertions.ASK, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.unchangedEarning(sw, swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.unchangedEarningRelative, checkAssert(calcData, Assertions.ASK, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.unchangedEarningRelative(sw, swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.unchangedEarningRelativePerYear, checkAssert(calcData, Assertions.ASK, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING, Assertions.EXPIRES)
                ? EmbeddedCalculatorMath.unchangedEarningRelativePerYear(sw, swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.capToUnderlying, checkAssert(calcData, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.capToUnderlying(swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.capToUnderlyingRelative, checkAssert(calcData, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.capToUnderlyingRelative(swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.underlyingToCap, checkAssert(calcData, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.underlyingToCap(swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.capLevel, checkAssert(calcData, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.capLevel(swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.underlyingToCapRelative, checkAssert(calcData, Assertions.CAP, Assertions.SUBSCRIPTIONRATIO, Assertions.DISCOUNT, Assertions.PRICE_UNDERLYING)
                ? EmbeddedCalculatorMath.underlyingToCapRelative(swu, staticData, currencyConvertFactor)
                : Long.MIN_VALUE);
    }

    private void calcZNS(CalcData calcData) {
        final SnapRecord snap = calcData.getSnap();
        final StaticData staticData = this.staticDataProvider.get(calcData.getQuote().getInstrument().getId());
        final SnapWrapper snapWrapper = SnapWrapper.create(snap, calcData.getQuote());
        if (this.trace) {
            this.logger.info("TRACE staticData:" + ToStringBuilder.reflectionToString(staticData));
        }
        this.encoder.add(RatioFieldDescription.lastPrice, snapWrapper.getLastPrice());
        // from chicago, configured in chicago-fields-zns
        this.encoder.add(RatioFieldDescription.standard, SnapRecordUtils.getString(snap.getField(VwdFieldDescription.ADF_Zusatzinformation.id())));
        this.encoder.add(RatioFieldDescription.rating, SnapRecordUtils.getString(snap.getField(VwdFieldDescription.ADF_Rating_LT.id())));
        this.encoder.add(RatioFieldDescription.volatility,
                percentValueRatio(SnapRecordUtils.getLong(snap.getField(VwdFieldDescription.ADF_Volatility.id()))));
    }

    private void calcGNS(CalcData calcData) {
        final SnapWrapper sw = SnapWrapper.create(calcData.getSnap(), calcData.getQuote());
        final StaticData staticData = this.staticDataProvider.get(calcData.getQuote().getInstrument().getId());
        if (this.trace) {
            this.logger.info("TRACE " + ToStringBuilder.reflectionToString(staticData));
        }

        final long dividendYield = calcDividendYield(calcData, sw, staticData);
        this.encoder.add(RatioFieldDescription.wmDividendYield, dividendYield);

        final long marketCapitalization = EmbeddedCalculatorMath.getMarketCapitalization(sw, staticData);
        this.encoder.add(RatioFieldDescription.marketCapitalization, marketCapitalization);
        this.encoder.add(RatioFieldDescription.marketCapitalizationPreviousDay, EmbeddedCalculatorMath.getMarketCapitalizationPreviousDay(sw, staticData));
        this.encoder.add(RatioFieldDescription.marketCapitalizationEUR, convert(marketCapitalization, sw.getCurrency(), "EUR"));
        this.encoder.add(RatioFieldDescription.marketCapitalizationUSD, convert(marketCapitalization, sw.getCurrency(), "USD"));

        addTurnoverDay(sw);
    }

    private void calcStocks(CalcData calcData) {
        final SnapWrapper sw = SnapWrapper.create(calcData.getSnap(), calcData.getQuote());
        final StaticData staticData = this.staticDataProvider.get(calcData.getQuote().getInstrument().getId());
        if (this.trace) {
            this.logger.info("TRACE " + ToStringBuilder.reflectionToString(staticData));
        }

        // market capitalization is in quote currency, NOT scaled for decimal
        final long marketCapitalization = EmbeddedCalculatorMath.getMarketCapitalization(sw, staticData);
        this.encoder.add(RatioFieldDescription.marketCapitalization, marketCapitalization);
        this.encoder.add(RatioFieldDescription.marketCapitalizationPreviousDay, EmbeddedCalculatorMath.getMarketCapitalizationPreviousDay(sw, staticData));
        this.encoder.add(RatioFieldDescription.marketCapitalizationEUR, convert(marketCapitalization, sw.getCurrency(), "EUR"));
        this.encoder.add(RatioFieldDescription.marketCapitalizationUSD, convert(marketCapitalization, sw.getCurrency(), "USD"));

        addTurnoverDay(sw);

        final long dividendYield = calcDividendYield(calcData, sw, staticData);
        this.encoder.add(RatioFieldDescription.wmDividendYield, dividendYield);

        if (staticData != null) {
            final long lastPrice = sw.getLastPrice();
            final long lastPriceInFactsetCurrency = convert(lastPrice, sw.getCurrency(), staticData.getFactsetCurrency());
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceCashflowRatio1Y,
                    priceValueRatio(lastPriceInFactsetCurrency, staticData.getFactsetCashflow1Y()));
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceCashflowRatio2Y,
                    priceValueRatio(lastPriceInFactsetCurrency, staticData.getFactsetCashflow2Y()));
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceBookvalueRatio1Y,
                    priceValueRatio(lastPriceInFactsetCurrency, staticData.getFactsetBookvalue1Y()));
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceBookvalueRatio2Y,
                    priceValueRatio(lastPriceInFactsetCurrency, staticData.getFactsetBookvalue2Y()));
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceEarningRatio1Y,
                    priceValueRatio(lastPriceInFactsetCurrency, staticData.getFactsetEarning1Y()));
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceEarningRatio2Y,
                    priceValueRatio(lastPriceInFactsetCurrency, staticData.getFactsetEarning2Y()));

            // marketCapitalization is not scaled, just a plain long, sales is scaled and in Mio
            final long marketCapitalizationInFactsetCurrency = convert(
                    marketCapitalization * Constants.SCALE_FOR_DECIMAL, sw.getCurrency(), staticData.getFactsetCurrency());
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceSalesRatio1Y,
                    priceValueRatio(marketCapitalizationInFactsetCurrency, staticData.getFactsetSales1Y() * 1_000_000));
            this.encoder.add(RatioFieldDescription.factsetCurrentPriceSalesRatio2Y,
                    priceValueRatio(marketCapitalizationInFactsetCurrency, staticData.getFactsetSales2Y() * 1_000_000));

            final long lastPriceInTRCurrency = convert(lastPrice, sw.getCurrency(), staticData.getTRCurrency());
            this.encoder.add(RatioFieldDescription.trCurrentPriceCashflowRatio1Y,
                    priceValueRatio(lastPriceInTRCurrency, staticData.getTRCashflow1Y()));
            this.encoder.add(RatioFieldDescription.trCurrentPriceCashflowRatio2Y,
                    priceValueRatio(lastPriceInTRCurrency, staticData.getTRCashflow2Y()));
            this.encoder.add(RatioFieldDescription.trCurrentPriceBookvalueRatio1Y,
                    priceValueRatio(lastPriceInTRCurrency, staticData.getTRBookvalue1Y()));
            this.encoder.add(RatioFieldDescription.trCurrentPriceBookvalueRatio2Y,
                    priceValueRatio(lastPriceInTRCurrency, staticData.getTRBookvalue2Y()));
            this.encoder.add(RatioFieldDescription.trCurrentPriceEarningRatio1Y,
                    priceValueRatio(lastPriceInTRCurrency, staticData.getTREarning1Y()));
            this.encoder.add(RatioFieldDescription.trCurrentPriceEarningRatio2Y,
                    priceValueRatio(lastPriceInTRCurrency, staticData.getTREarning2Y()));

            // marketCapitalization is not scaled, just a plain long, sales is scaled and in Mio
            final long marketCapitalizationInTRCurrency = convert(
                    marketCapitalization * Constants.SCALE_FOR_DECIMAL, sw.getCurrency(), staticData.getTRCurrency());
            this.encoder.add(RatioFieldDescription.trCurrentPriceSalesRatio1Y,
                    priceValueRatio(marketCapitalizationInTRCurrency, staticData.getTRSales1Y() * 1_000_000));
            this.encoder.add(RatioFieldDescription.trCurrentPriceSalesRatio2Y,
                    priceValueRatio(marketCapitalizationInTRCurrency, staticData.getTRSales2Y() * 1_000_000));
        }
    }

    /**
     * Convert price (which is given in srcCurrency) into a price value in dstCurrency
     * @return converted price or Long.MIN_VALUE if conversion is not possible
     */
    private long convert(long price, String srcCurrency, String dstCurrency) {
        if (price == Long.MIN_VALUE || price == 0 || Objects.equals(srcCurrency, dstCurrency)) {
            return price;
        }
        final double f1 = getCurrencyConvertFactor(srcCurrency); // 1(srcCurr) / f1 = 1€
        final double f2 = getCurrencyConvertFactor(dstCurrency); // 1(dstCurr) / f2 = 1€

        if (Double.isNaN(f1) || Double.isNaN(f2)) {
            if (this.trace) {
                this.logger.info("TRACE <convert> f1=" + f1 + ", f2=" + f2);
            }
            return Long.MIN_VALUE;
        }

        // Ex.: convert(1, USD, GBP)
        // 1 USD / 1.3(USD/EUR) * 0.85(GBP/EUR) = 0.65 GBP

        final long result = (long) (price / f1 * f2);
        if (this.trace) {
            this.logger.info("TRACE <convert> price=" + price
                    + ", f1=" + f1 + ", f2=" + f2 + ", result=" + result);
        }
        return result;
    }

    private long calcDividendYield(CalcData calcData, SnapWrapper sw, StaticData staticData) {
        if (!checkAssert(calcData, Assertions.WM_DIVIDEND)) {
            if (this.trace) {
                this.logger.info("TRACE <calcDividendYield> assertion failed");
            }
            return Long.MIN_VALUE;
        }
        final double f1 = getCurrencyConvertFactor(calcData.getQuote().getCurrency().getSymbolIso());
        final double f2 = getCurrencyConvertFactor(staticData.getWmDividendCurrency());
        if (Double.isNaN(f1) || Double.isNaN(f2)) {
            if (this.trace) {
                this.logger.info("TRACE <calcDividendYield> f1=" + f1 + ", f2=" + f2);
            }
            return Long.MIN_VALUE;
        }
        final double price = ((Number) sw.getLastPrice()).doubleValue();
        final double zaehler = getDividend(staticData) / f2;
        final double nenner = price / f1;
        final long result = (long) ((zaehler / nenner) * Constants.SCALE_FOR_DECIMAL);
        if (this.trace) {
            this.logger.info("TRACE <calcDividendYield> price=" + price
                    + ", zaehler=" + zaehler + ", nenner=" + nenner + ", result=" + result);
        }
        return result;
    }

    private long getDividend(StaticData staticData) {
        if (staticData.getWmDividendLastYear() > 0) {
            return staticData.getWmDividendLastYear();
        }
        return staticData.getWmDividend();
    }

    private void calcBonds(CalcData calcData) {
        final StaticData staticData = this.staticDataProvider.get(calcData.getQuote().getInstrument().getId());
        SnapRecord snapRecord = calcData.getSnap();
        final SnapWrapper sw = SnapWrapper.create(snapRecord, calcData.getQuote());

        addTurnoverDay(sw);

        SnapField rendite = snapRecord.getField(VwdFieldDescription.ADF_Rendite_ISMA.id());
        if (!isDefinedAndNotZero(rendite)) {
            rendite = snapRecord.getField(VwdFieldDescription.ADF_Rendite_ISMA_Vortag.id());
            if (!isDefinedAndNotZero(rendite)) {
                rendite = snapRecord.getField(VwdFieldDescription.ADF_Rendite.id());
                if (!isDefinedAndNotZero(rendite)) {
                    rendite = snapRecord.getField(VwdFieldDescription.ADF_Rendite_Vortag.id());
                }
            }
        }

        if (isDefinedAndNotZero(rendite)) {
            this.encoder.add(RatioFieldDescription.yieldRelative_mdps, SnapRecordUtils.getLong(rendite) / 100);
        }
        else {
            this.encoder.add(RatioFieldDescription.yieldRelative_mdps, Long.MIN_VALUE);
        }

        final SnapField bpv = snapRecord.getField(VwdFieldDescription.ADF_BasisPointValue.id());
        if (isDefinedAndNotZero(bpv)) {
            this.encoder.add(RatioFieldDescription.mdpsBasePointValue, SnapRecordUtils.getLong(bpv) / 100);
        }
        else {
            this.encoder.add(RatioFieldDescription.mdpsBasePointValue, Long.MIN_VALUE);
        }

        addField(calcData, VwdFieldDescription.ADF_Duration, RatioFieldDescription.duration_mdps);
        addField(calcData, VwdFieldDescription.ADF_Convexity, RatioFieldDescription.mdpsConvexity);
        addField(calcData, VwdFieldDescription.ADF_ModifiedDuration, RatioFieldDescription.mdpsModifiedDuration);
        addField(calcData, VwdFieldDescription.ADF_Accrued_Interest, RatioFieldDescription.mdpsBrokenPeriodInterest);
        addField(calcData, VwdFieldDescription.ADF_Interest_Rate_Elasticity, RatioFieldDescription.mdpsInterestRateElasticity);

        if (staticData != null) {
            final long price = EmbeddedCalculatorMath.lastPrice(sw);
            final long redemption = staticData.getRedemptionPrice();
            final long pari = price > 0 && redemption > 0 ? price - redemption : Long.MIN_VALUE;
            this.encoder.add(RatioFieldDescription.pari, pari);
        }
        else {
            this.encoder.add(RatioFieldDescription.pari, Long.MIN_VALUE);
        }
    }

    private void addTurnoverDay(SnapWrapper sw) {
        final long turnoverDay = getLastPrice(sw, VwdFieldDescription.ADF_Umsatz_gesamt_in_Whrg.id(),
                VwdFieldDescription.ADF_Umsatz_gesamt_in_Whrg_Vortag.id());
        this.encoder.add(RatioFieldDescription.turnoverDay, turnoverDay);
    }

    private boolean isDefinedAndNotZero(SnapField rendite) {
        return rendite.isDefined() && SnapRecordUtils.getLong(rendite) > 0;
    }

    private void addField(CalcData calcData, VwdFieldDescription.Field feedField,
            RatioFieldDescription.Field ratioField) {
        final SnapField value = calcData.getSnap().getField(feedField.id());
        this.encoder.add(ratioField, SnapRecordUtils.getLong(value));
    }

    private double getCurrencyConvertFactorForStrike(StaticData staticData) {
        if (staticData == null) {
            return 1D;
        }
        return getCurrencyConvertFactor(staticData.getCurrencystrike());
    }

    protected double getCurrencyConvertFactor(String currency) {
        if ("EUR".equals(currency)) {
            return 1d;
        }

        String baseCurrency = CurrencyDp2.getBaseCurrencyIso(currency);
        final Long quoteid = this.instrumentRepository.getQuoteid(baseCurrency);
        if (quoteid != null) {
            final SnapRecord srC = this.priceProvider.getSnapRecord(quoteid);
            final SnapWrapper swC = SnapWrapper.create(srC, null);
            final Number priceEscaped = swC.getPriceEscaped();
            if (priceEscaped.longValue() != Long.MIN_VALUE) {
                if (CurrencyDp2.isCent(currency)) {
                    return priceEscaped.doubleValue() / (Constants.SCALE_FOR_DECIMAL / 100L);
                }
                else {
                    return priceEscaped.doubleValue() / Constants.SCALE_FOR_DECIMAL;
                }
            }
        }
        return Double.NaN;
    }

    private void calcStandard(CalcData calcData) {
        final SnapWrapper sw = SnapWrapper.create(calcData.getSnap(), calcData.getQuote());
        final SnapWrapper swu = SnapWrapper.create(calcData.getReferenceSnap(), null);

        this.encoder.add(RatioFieldDescription.spread, checkAssert(calcData, Assertions.NO_KNOCKOUT)
                ? EmbeddedCalculatorMath.spread(calcData)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.spreadRelative, checkAssert(calcData, Assertions.NO_KNOCKOUT)
                ? EmbeddedCalculatorMath.spreadRelative(calcData)
                : Long.MIN_VALUE);

        this.encoder.add(RatioFieldDescription.changeNet, sw.getDifference());
        this.encoder.add(RatioFieldDescription.changePercent, sw.getDifferenceRelative());
        this.encoder.add(RatioFieldDescription.performance1d, sw.getDifferenceRelative());
        this.encoder.add(RatioFieldDescription.bid, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Geld.id())));
        this.encoder.add(RatioFieldDescription.ask, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Brief.id())));
        this.encoder.add(RatioFieldDescription.bidVolume, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Geld_Umsatz.id())));
        this.encoder.add(RatioFieldDescription.askVolume, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Brief_Umsatz.id())));
        this.encoder.add(RatioFieldDescription.lastPrice, EmbeddedCalculatorMath.lastPrice(sw));
        this.encoder.add(RatioFieldDescription.previousClose, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Schluss_Vortag.id())));
        this.encoder.add(RatioFieldDescription.bidAskDate, sw.getBidDate());
        this.encoder.add(RatioFieldDescription.bidAskTime, sw.getBidTime());
        this.encoder.add(RatioFieldDescription.tradeVolume, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Bezahlt_Umsatz.id())));
        this.encoder.add(RatioFieldDescription.lastDate, EmbeddedCalculatorMath.lastDate(sw));
        this.encoder.add(RatioFieldDescription.lastTime, sw.getPriceTime());
        this.encoder.add(RatioFieldDescription.open, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Anfang.id())));
        this.encoder.add(RatioFieldDescription.highYear, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Jahreshoch.id())));
        this.encoder.add(RatioFieldDescription.lowYear, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Jahrestief.id())));
        this.encoder.add(RatioFieldDescription.totalVolume, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Umsatz_gesamt.id())));
        this.encoder.add(RatioFieldDescription.previousDate, SnapRecordUtils.getInt(calcData.getSnap().getField(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id())));
        if (!sw.isKagFonds()) {
            this.encoder.add(RatioFieldDescription.high, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Tageshoch.id())));
            this.encoder.add(RatioFieldDescription.low, SnapRecordUtils.getLong(calcData.getSnap().getField(VwdFieldDescription.ADF_Tagestief.id())));
        }

        if (swu != SnapWrapper.NULL && (calcData.getInstrumentType() == InstrumentTypeEnum.WNT || calcData.getInstrumentType() == InstrumentTypeEnum.CER)) {
            this.encoder.add(RatioFieldDescription.underlyingLastPrice, swu.getLastPrice());
            this.encoder.add(RatioFieldDescription.underlyingPreviousClose, swu.getPreviousClose());
            this.encoder.add(RatioFieldDescription.underlyingTradeVolume, SnapRecordUtils.getLong(calcData.getReferenceSnap().getField(VwdFieldDescription.ADF_Bezahlt_Umsatz.id())));
            this.encoder.add(RatioFieldDescription.underlyingLastDate, swu.getPriceDate());
            this.encoder.add(RatioFieldDescription.underlyingLastTime, swu.getPriceTime());
            this.encoder.add(RatioFieldDescription.underlyingTotalVolume, SnapRecordUtils.getLong(calcData.getReferenceSnap().getField(VwdFieldDescription.ADF_Umsatz_gesamt.id())));
            this.encoder.add(RatioFieldDescription.underlyingPreviousDate, SnapRecordUtils.getInt(calcData.getReferenceSnap().getField(VwdFieldDescription.ADF_Schluss_Vortagesdatum.id())));
        }

        this.encoder.add(RatioFieldDescription.issuePrice, EmbeddedCalculatorMath.issuePrice(sw));
        if (sw.isKagFonds()) {
            this.encoder.add(RatioFieldDescription.interimProfit, getLastPrice(sw, VwdFieldDescription.ADF_Zwischengewinn.id(), VwdFieldDescription.ADF_Zwischengewinn_Vortag.id()));
            this.encoder.add(RatioFieldDescription.stockProfit, getLastPrice(sw, VwdFieldDescription.ADF_Aktiengewinn.id(), VwdFieldDescription.ADF_Aktiengewinn_Vortag.id()));
            this.encoder.add(RatioFieldDescription.estateProfit, getLastPrice(sw, VwdFieldDescription.ADF_Immobiliengewinn.id(), VwdFieldDescription.ADF_Immobiliengewinn_VT.id()));
            this.encoder.add(RatioFieldDescription.interimProfitDate, getLastDate(sw, VwdFieldDescription.ADF_Zwischengewinn.id(), VwdFieldDescription.ADF_Schluss_Datum.id(), VwdFieldDescription.ADF_Schluss_Vortagesdatum.id()));
            this.encoder.add(RatioFieldDescription.stockProfitDate, getLastDate(sw, VwdFieldDescription.ADF_Aktiengewinn.id(), VwdFieldDescription.ADF_Schluss_Datum.id(), VwdFieldDescription.ADF_Schluss_Vortagesdatum.id()));
        }
    }

    private void calcMER(CalcData calcData) {
        final SnapRecord snap = calcData.getSnap();
        final SnapWrapper sw = SnapWrapper.create(snap, calcData.getQuote());
        // Some experts hijacked ADF_Auflagedatum (issue date) to transmit the expiration date of
        // LME quotes. Additionally, ADF_Auflagedatum should be an instrument specific static datum,
        // but it may happen that its data differs between different LME quotes of the same
        // instrument.
        this.encoder.add(RatioFieldDescription.lmeExpirationDate, sw.getIssueDate());
        this.encoder.add(RatioFieldDescription.isLMEComposite, calcData.getQuote().getContentFlags().isLMEComposite());
        this.encoder.add(RatioFieldDescription.merInstrumentenTyp, SnapRecordUtils.getString(snap.getField(VwdFieldDescription.ADF_Instrumententyp.id())));
        this.encoder.add(RatioFieldDescription.merHandelsmonat, SnapRecordUtils.getString(snap.getField(VwdFieldDescription.ADF_Handelsmonat.id())));
    }

    private void addTimestamp(CalcData calcData) {
        final int time = SnapRecordUtils.getTime(calcData.getSnap());
        final int date = SnapRecordUtils.getDate(calcData.getSnap());
        final long ts = date * 100000L + time;
        this.encoder.add(RatioFieldDescription.mostRecentUpdateTimestamp, ts);
    }

    private int getLastDate(SnapWrapper sw, int checkFieldId, int currentDayId, int previousDayId) {
        final long currentCheckField = SnapRecordUtils.getLong(sw.getSnapRecord().getField(checkFieldId));
        return currentCheckField > 0
                ? SnapRecordUtils.getInt(sw.getSnapRecord().getField(currentDayId))
                : SnapRecordUtils.getInt(sw.getSnapRecord().getField(previousDayId));
    }

    private long getLastPrice(SnapWrapper sw, int currentDayId, int previousDayId) {
        final long current = SnapRecordUtils.getLong(sw.getSnapRecord().getField(currentDayId));
        return current > 0
                ? current
                : SnapRecordUtils.getLong(sw.getSnapRecord().getField(previousDayId));
    }

    private boolean checkAssert(CalcData calcData, Assertions... assertions) {
        final StaticData staticData = this.staticDataProvider.get(calcData.getQuote().getInstrument().getId());
        for (final Assertions a : assertions) {
            switch (a) {
                case BID:
                    if (!assertPriceEscaped(calcData, VwdFieldDescription.ADF_Geld.id())) {
                        return false;
                    }
                    break;
                case ASK:
                    if (!assertPriceEscaped(calcData, VwdFieldDescription.ADF_Brief.id())) {
                        return false;
                    }
                    break;
                case CAP:
                    if (staticData == null || staticData.getCap() <= 0) {
                        return false;
                    }
                    break;
                case SUBSCRIPTIONRATIO:
                    if (staticData == null || staticData.getSubscriptionratio() <= 0) {
                        return false;
                    }
                    break;
                case DISCOUNT:
                    if (!"CERT_DISCOUNT".equals(staticData.getProducttype())) {
                        return false;
                    }
                    break;
                case NO_KNOCKOUT:
                    if (assertPrice(calcData, VwdFieldDescription.ADF_Geld.id())
                            && assertPrice(calcData, VwdFieldDescription.ADF_Brief.id())) {
                        final double bid = SnapRecordUtils.getPrice(calcData.getSnap().getField(VwdFieldDescription.ADF_Geld.id()));
                        final double ask = SnapRecordUtils.getPrice(calcData.getSnap().getField(VwdFieldDescription.ADF_Brief.id()));
                        if (bid * 20 < ask) {
                            return false;
                        }
                    }
                    break;
                case PRICE_UNDERLYING:
                    final SnapWrapper swUnderlying = SnapWrapper.create(calcData.getReferenceSnap(), null);
                    if (swUnderlying == null || swUnderlying.getLastPrice() <= 0) {
                        return false;
                    }
                    break;
                case EXPIRES:
                    if (staticData.getExpires() <= 0 || DateUtil.getDaysToToday(staticData.getExpires()) <= 0) {
                        return false;
                    }
                    break;
                case WM_DIVIDEND:
                    if (staticData == null || getDividend(staticData) < 0
                            || staticData.getWmDividendCurrency() == null
                            || calcData.getQuote() == null
                            || calcData.getQuote().getCurrency() == null
                            || calcData.getQuote().getCurrency().getSymbolIso() == null) {
                        return false;
                    }
                    break;
            }
        }
        return true;
    }

    private boolean assertPrice(CalcData calcData, int fieldid) {
        final SnapField field = calcData.getSnap().getField(fieldid);
        final double price = SnapRecordUtils.getPrice(field);
        return !(Double.isNaN(price) || price == 0d);
    }

    private boolean assertPriceEscaped(CalcData calcData, int fieldid) {
        final SnapRecord record = calcData.getSnap();
        final SnapWrapper sw = SnapWrapper.create(record, calcData.getQuote());
        final SnapField field = record.getField(fieldid);
        final double price = SnapRecordUtils.getPrice(field);
        return !(Double.isNaN(price) || price == 0d) || sw.getLastPrice() > 0;
    }

    private enum Assertions {
        BID,
        ASK,
        CAP,
        SUBSCRIPTIONRATIO,
        DISCOUNT,
        NO_KNOCKOUT,
        PRICE_UNDERLYING,
        EXPIRES,
        WM_DIVIDEND
    }
}
