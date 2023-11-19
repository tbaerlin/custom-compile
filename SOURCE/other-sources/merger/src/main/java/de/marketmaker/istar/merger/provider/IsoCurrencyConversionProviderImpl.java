/*
 * IsoCurrencyConversionProviderImpl.java
 *
 * Created on 08.08.2006 15:50:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import static de.marketmaker.istar.domainimpl.CurrencyDp2.getBaseCurrencyIso;
import static de.marketmaker.istar.domainimpl.CurrencyDp2.isCent;
import static de.marketmaker.istar.merger.Constants.MC;

import de.marketmaker.istar.common.mm.MMKeyType;
import de.marketmaker.istar.common.mm.MMService;
import de.marketmaker.istar.common.mm.MMServiceResponse;
import de.marketmaker.istar.common.mm.MMTalkException;
import de.marketmaker.istar.common.mm.MMTalkTableRequest;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.CurrencyCrossrate;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.PriceImpl;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.util.QuoteRef;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategyFactory;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IsoCurrencyConversionProviderImpl
        implements IsoCurrencyConversionProvider, InitializingBean {

    public static class ConversionResult implements Serializable {
        protected static final long serialVersionUID = 1L;

        private final Price rate;

        private final BigDecimal sourceToTargetFactor;

        private transient BigDecimal factor;

        private transient QuoteRef quoteRef;

        public ConversionResult(Price rate, BigDecimal sourceToTargetFactor, QuoteRef quoteRef) {
            this.rate = rate;
            this.sourceToTargetFactor = sourceToTargetFactor;
            this.factor = this.sourceToTargetFactor.multiply(this.rate.getValue(), MC);
            this.quoteRef = quoteRef;
        }

        public QuoteRef getQuoteRef() {
            return quoteRef;
        }

        private void readObject(java.io.ObjectInputStream in)
                throws IOException, ClassNotFoundException {
            in.defaultReadObject();
            this.factor = this.sourceToTargetFactor.multiply(this.rate.getValue(), MC);
        }

        public Price getRate() {
            return rate;
        }

        public BigDecimal getSourceToTargetFactor() {
            return sourceToTargetFactor;
        }

        /**
         * @return <tt>getRate().getValue() * getSourceToTargetFactor()</tt>
         */
        public BigDecimal getFactor() {
            return factor;
        }

        public BigDecimal convert(BigDecimal source) {
            return source.multiply(this.factor, MC);
        }

        ConversionResult multiplyBy(BigDecimal factor) {
            final Price p = new PriceImpl(rate.getValue().divide(factor, MC),
                    rate.getVolume(), rate.getSupplement(), rate.getDate(), PriceQuality.NONE);
            return new ConversionResult(p, sourceToTargetFactor, this.quoteRef);
        }
    }

    public static final String INDIRECT_ISO_CODE_USD = "USD";

    public static final String INDIRECT_ISO_CODE_EUR = "EUR";

    private static final BigDecimal ONE_HUNDRED = new BigDecimal(100);

    private static final BigDecimal ONE_HUNDREDTH = new BigDecimal("0.01");

    private static final DateTimeFormatter DTF = DateTimeFormat.forPattern("dd.MM.yyyy");

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, IsoCurrencySource> sources;

    private IntradayProvider intradayProvider;

    private InstrumentProvider instrumentProvider;

    private MMService mmService;

    private Resource config = new ClassPathResource("currencyconversions.properties", getClass());

    public void setConfig(Resource config) {
        this.config = config;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(InstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setMmService(MMService mmService) {
        this.mmService = mmService;
    }

    public void afterPropertiesSet() throws Exception {
        this.sources = new IsoCurrencySourceReader().read(this.config);
//        this.logger.info("<initCurrencies> sources: " + sources);
    }

    private ConversionResult getFactor(IsoCurrencySource source, String sourceIso, String targetIso,
            boolean reverse, LocalDate date) {
        final ConversionResult result = getFactorInternal(source, sourceIso, targetIso, reverse, date);
        if (result == null) {
            throw new NoDataException("conversion not possible");
        }
        return result;
    }

    private ConversionResult getFactorInternal(IsoCurrencySource source, String currency,
            String resultCurrency, boolean reverse, LocalDate date) {
        final IsoCurrencyResult result = source.getResult(resultCurrency);

        return getFactorInternalFromCurrencyResult(currency, resultCurrency, reverse, date, result);
    }

    private ConversionResult getFactorInternalFromCurrencyResult(
            String currency, String resultCurrency, boolean reverse, LocalDate date,
            IsoCurrencyResult result) {
        if (result.getType() == IsoCurrencyResult.Type.FACTOR) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getFactor> direct factor (" + currency + "->" + resultCurrency + "): " + result.getFactor());
            }
            final BigDecimal value = result.getFactor(reverse);
            return new ConversionResult(createPrice(value), BigDecimal.ONE, QuoteRef.ref(result.getQuote()));
        }

        if (isToday(date)) {
            return getCurrentFactor(currency, resultCurrency, reverse, result);
        }
        else {
            return getHistoricFactor(currency, reverse, date, result);
        }
    }

    private ConversionResult getHistoricFactor(String currency,
            boolean reverse, LocalDate date, IsoCurrencyResult result) {
        final Object[] mmtalkResult = getHistoricConversion(currency, result, date);

        final BigDecimal factor = HistoricRatiosProviderImpl.getValue(mmtalkResult[0]);
        if (factor == null) {
            throw new NoDataException("no historic data found");
        }

        final BigDecimal value = reverse ? factor : BigDecimal.ONE.divide(factor, MC);

        final BigDecimal sourceToTargetFactor = getSourceToTargetFactor(result);

        final DateTime dt = new DateTime(DateUtil.comDateToDate((Double) mmtalkResult[1]).getTime());
        return new ConversionResult(new PriceImpl(value, Long.MIN_VALUE, null, dt, PriceQuality.NONE),
                sourceToTargetFactor, QuoteRef.ref(result.getQuote()));
    }

    private ConversionResult getCurrentFactor(String currency,
            String resultCurrency, boolean reverse, IsoCurrencyResult result) {
        final Price p = getCurrencyPrice(result);
        if (p == null || p.getValue() == null || p.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            this.logger.debug("<getFactor> problem with query => factor=1");
            return null;
        }

        this.logger.debug("<getFactor> direct key (" + currency + "->" + resultCurrency + "): " + p);
        final BigDecimal value = reverse ? BigDecimal.ONE.divide(p.getValue(), MC) : p.getValue();

        if (result.getVwdfeedSymbol() != null && result.getFactor() != null) {
            BigDecimal factoredPrice = value.divide(result.getFactor(reverse), MC);
            return new ConversionResult(new PriceImpl(factoredPrice, p.getVolume(),
                p.getSupplement(), p.getDate(), PriceQuality.NONE), BigDecimal.ONE,
                QuoteRef.ref(result.getQuote()));
        }

        final BigDecimal sourceToTargetFactor = getSourceToTargetFactor(result);

        return new ConversionResult(
            new PriceImpl(value.divide(sourceToTargetFactor, MC), p.getVolume(),
                p.getSupplement(), p.getDate(), PriceQuality.NONE), sourceToTargetFactor,
            QuoteRef.ref(result.getQuote()));
    }

    private boolean isToday(LocalDate date) {
        return date.isEqual(new LocalDate());
    }

    private BigDecimal getSourceToTargetFactor(IsoCurrencyResult result) {
        if (result.getQuote() == null
                || !(result.getQuote().getInstrument() instanceof CurrencyCrossrate)) {
            throw new NoDataException("no conversion possible");
        }

        final double factor
                = ((CurrencyCrossrate) result.getQuote().getInstrument()).getSourceToTargetFactor();

        return factor == 0d ? BigDecimal.ONE : new BigDecimal(factor);
    }

    private Price getCurrencyPrice(final IsoCurrencyResult icr) {
        try {
            return RequestContextHolder.callWith(getProfile(), getMarketStrategy(),
                    new Callable<Price>() {
                        public Price call() {
                            return doGetCurrencyPrice(icr);
                        }
                    });
        } catch (Exception e) {
            this.logger.warn("<getCurrencyPrice> failed", e);
            return null;
        }
    }

    private MarketStrategy getMarketStrategy() {
        return MarketStrategyFactory.defaultStrategy();
    }

    private Profile getProfile() {
        return ProfileFactory.valueOf(true);
    }

    private Price doGetCurrencyPrice(IsoCurrencyResult icr) {
        final Quote quote = getQuote(icr);
        if (quote == null) {
            return null;
        }
        return this.intradayProvider.getPriceRecords(Collections.singletonList(quote)).get(0).getPrice();
    }

    private Quote getQuote(IsoCurrencyResult icr) {
        Quote quote = icr.getQuote();
        if (quote == null) {
            try {
                quote = this.instrumentProvider.identifyByVwdfeed(icr.getVwdfeedSymbol());
            } catch (UnknownSymbolException e) {
                this.logger.error("<getQuote> unknown symbol '" + icr.getVwdfeedSymbol() + "'");
                return null;
            }
            icr.setQuote(quote);
        }
        return quote;
    }

    public ConversionResult getConversion(String isoSource, String isoTarget) {
        return getConversion(isoSource, isoTarget, new LocalDate());
    }

    public ConversionResult getConversion(String isoSource, String isoTarget, YearMonthDay date) {
        return getConversion(isoSource, isoTarget, date.toLocalDate());
    }

    public ConversionResult getConversion(String isoSource, String isoTarget, LocalDate date) {
        try {
            return doGetConversion(isoSource, isoTarget, date);
        }
        catch (NoDataException e) {
            this.logger.warn("<getConversion> failed for '" + isoSource + "' to '" + isoTarget
                    + "' on " + date);
            throw e;
        }
    }

    private ConversionResult doGetConversion(String isoSource, String isoTarget, LocalDate date) {
        if (isCent(isoSource)) {
            return doGetConversion(getBaseCurrencyIso(isoSource), isoTarget, date).multiplyBy(ONE_HUNDRED);
        }
        if (isCent(isoTarget)) {
            return doGetConversion(isoSource, getBaseCurrencyIso(isoTarget), date).multiplyBy(ONE_HUNDREDTH);
        }
        return getConversionInternal(isoSource, isoTarget, date);
    }

    private ConversionResult getConversionInternal(String isoSource, String isoTarget,
            LocalDate date) {
        final ConversionResult standard = tryStandard(isoSource, isoTarget, date);
        if (standard != null) {
            return standard;
        }

        final ConversionResult indirectUSD
                = tryIndirect(isoSource, isoTarget, date, INDIRECT_ISO_CODE_USD);
        if (indirectUSD != null) {
            return indirectUSD;
        }

        final ConversionResult indirectEUR
                = tryIndirect(isoSource, isoTarget, date, INDIRECT_ISO_CODE_EUR);
        if (indirectEUR != null) {
            return indirectEUR;
        }

        final ConversionResult indirectUSD_EUR
                = tryIndirect(isoSource, isoTarget, date, INDIRECT_ISO_CODE_USD, INDIRECT_ISO_CODE_EUR);
        if (indirectUSD_EUR != null) {
            return indirectUSD_EUR;
        }

        final ConversionResult indirectEUR_USD
                = tryIndirect(isoSource, isoTarget, date, INDIRECT_ISO_CODE_EUR, INDIRECT_ISO_CODE_USD);
        if (indirectEUR_USD != null) {
            return indirectEUR_USD;
        }

        throw new NoDataException("no conversion for '" + isoSource + "' to '" + isoTarget + "'");
    }

    private ConversionResult tryIndirect(String isoSource, String isoTarget, LocalDate date,
        String indirectSource, String indirectTarget) {
        final ConversionResult s2x = tryStandard(isoSource, indirectSource, date);
        final ConversionResult t2y = tryStandard(isoTarget, indirectTarget, date);
        if (s2x != null && t2y != null) {
            final ConversionResult x2y = tryStandard(indirectSource, indirectTarget, date);
            if (x2y == null) {
                return null;
            }
            return new ConversionResult(createPrice(s2x, t2y, x2y), createFactor(s2x, t2y, x2y),
                s2x.getQuoteRef().chain(t2y.getQuoteRef()).chain(x2y.getQuoteRef()));
        }
        return null;
    }

    private PriceImpl createPrice(ConversionResult s2x, ConversionResult t2y,
            ConversionResult x2y) {
        return createPrice(s2x.getRate().getValue()
                .divide(t2y.getRate().getValue(), MC)
                .multiply(x2y.getRate().getValue()));
    }

    private BigDecimal createFactor(ConversionResult s2x, ConversionResult t2y,
            ConversionResult x2y) {
        return s2x.getSourceToTargetFactor()
                .divide(t2y.getSourceToTargetFactor(), MC)
                .multiply(x2y.getSourceToTargetFactor());
    }

    private PriceImpl createPrice(BigDecimal value) {
        return new PriceImpl(value, Long.MIN_VALUE, null, new DateTime(), PriceQuality.NONE);
    }

    private ConversionResult tryStandard(String isoSource, String isoTarget, LocalDate date) {
        if (!StringUtils.hasText(isoSource) || !StringUtils.hasText(isoTarget)) {
            return null;
        }

        if (isoSource.equals(isoTarget) || "XXP".equals(isoSource)) {
            return new ConversionResult(createPrice(BigDecimal.ONE), BigDecimal.ONE, null);
        }

        final IsoCurrencySource source = this.sources.get(isoSource);

        if (source != null && source.hasResult(isoTarget)) {
            // isoSource->isoTarget
            this.logger.debug("<getFactorInternal> direct mapping: " + isoSource + " -> " + isoTarget);
            return getFactor(source, isoSource, isoTarget, false, date);
        }

        final IsoCurrencySource resultAsSource = this.sources.get(isoTarget);

        if (resultAsSource != null && resultAsSource.hasResult(isoSource)) {
            // 1 / isoSource->isoTarget
            this.logger.debug("<getFactorInternal> _reverse_ direct mapping: " + isoTarget + " -> " + isoSource);
            return getFactor(resultAsSource, isoTarget, isoSource, true, date);
        }

        // try to find a quote with cross rate symbol
        return tryCrossRateBidirectional(isoSource, isoTarget, date);
    }

    ConversionResult tryCrossRateBidirectional(String isoSource, String isoTarget, LocalDate date) {
        final ConversionResult ret = tryCrossRate(isoSource, isoTarget, false, date);
        if (ret != null) {
            return ret;
        }

        return tryCrossRate(isoTarget, isoSource, true, date);
    }

    private ConversionResult tryCrossRate(String isoSource, String isoTarget, boolean reverse,
            LocalDate date) {
        final Quote quote = getCrossRateQuote(isoSource, isoTarget);
        if (quote != null) {
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<tryStandard> " + (reverse ? "_reverse_ " : "")
                        + "direct cross rate mapping: " + isoSource + " -> " + isoTarget);
            }

            //XXX cache result?
            final IsoCurrencyResult cr = IsoCurrencyResult.create(isoTarget, quote.getSymbolVwdfeed());
            cr.setQuote(quote);
            return getFactorInternalFromCurrencyResult(isoSource, isoTarget, reverse, date, cr);
        }

        return null;
    }

    private static final Set<String> USD_SPECIALS = new HashSet<>(Arrays.asList("EUR", "NZD", "AUD", "GBP"));

    private Quote getCrossRateQuote(String isoSource, String isoTarget) {
        if (INDIRECT_ISO_CODE_USD.equals(isoTarget) && USD_SPECIALS.contains(isoSource)) {
            return identifyQuote("10." + isoSource + ".FXVWD");
        }
        if (INDIRECT_ISO_CODE_USD.equals(isoSource) && !USD_SPECIALS.contains(isoTarget)) {
            return identifyQuote("10." + isoTarget + ".FXVWD");
        }
        return identifyQuote("10." + isoSource + isoTarget + ".XRATE.SPOT");
    }

    private Quote identifyQuote(String... vwdFeeds) {
        for (final String vwdFeed : vwdFeeds) {
            try {
                final Quote quote = this.instrumentProvider.identifyByVwdfeed(vwdFeed);
                if (quote != null) {
                    return quote;
                }
            } catch (UnknownSymbolException e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<identifyQuote> failed: " + e.getMessage());
                }
            }
        }

        return null;
    }

    private ConversionResult tryIndirect(String isoSource, String isoTarget, LocalDate date,
            String indirectIsoCode) {
        final IsoCurrencySource source = this.sources.get(isoSource);
        final IsoCurrencySource resultAsSource = this.sources.get(isoTarget);
        final IsoCurrencySource usdAsSource = this.sources.get(indirectIsoCode);

        if (source != null && source.hasResult(indirectIsoCode)) {
            final ConversionResult sourceFactor = getFactorInternal(source, isoSource, indirectIsoCode, false, date);

            if (sourceFactor != null) {
                if (usdAsSource != null && usdAsSource.hasResult(isoTarget)) {
                    // isoSource->USD * USD->isoTarget
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<getFactorInternal> " + isoSource + "->" + indirectIsoCode + " * " + indirectIsoCode + "->" + isoTarget + " mapping => get both conversions and return result");
                    }
                    final ConversionResult resultFactor = getFactorInternal(usdAsSource, indirectIsoCode, isoTarget, false, date);
                    if (resultFactor != null) {
                        BigDecimal value = sourceFactor.getRate().getValue().multiply(resultFactor.getRate().getValue());
                        return new ConversionResult(createPrice(value),
                                sourceFactor.getSourceToTargetFactor().multiply(resultFactor.getSourceToTargetFactor()),
                            resultFactor.getQuoteRef().chain(sourceFactor.getQuoteRef()));
                    }
                }

                if (resultAsSource != null && resultAsSource.hasResult(indirectIsoCode)) {
                    // isoSource->USD / isoTarget->USD
                    if (this.logger.isDebugEnabled()) {
                        this.logger.debug("<getFactorInternal> " + isoSource + "->" + indirectIsoCode + " / " + isoTarget + "->" + indirectIsoCode + " mapping => get both conversions and return result");
                    }
                    final ConversionResult resultFactor = getFactor(resultAsSource, isoTarget, indirectIsoCode, false, date);
                    BigDecimal value = sourceFactor.getRate().getValue().divide(resultFactor.getRate().getValue(), MC);
                    return new ConversionResult(createPrice(value),
                            sourceFactor.getSourceToTargetFactor().divide(resultFactor.getSourceToTargetFactor(), MC),
                        resultFactor.getQuoteRef().chain(sourceFactor.getQuoteRef()));
                }
            }
        }

        if (usdAsSource != null && usdAsSource.hasResult(isoSource)) {
            final ConversionResult sourceFactor = getFactor(usdAsSource, indirectIsoCode, isoSource, false, date);

            if (usdAsSource.hasResult(isoTarget)) {
                // USD->isoTarget / USD->isoSource
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getFactorInternal> " + indirectIsoCode + "->" + isoTarget + " / " + indirectIsoCode + "->" + isoSource + " mapping => get both conversions and return result");
                }
                final ConversionResult resultFactor = getFactor(usdAsSource, indirectIsoCode, isoTarget, false, date);
                BigDecimal value = resultFactor.getRate().getValue().divide(sourceFactor.getRate().getValue(), MC);
                return new ConversionResult(createPrice(value),
                        resultFactor.getSourceToTargetFactor().divide(sourceFactor.getSourceToTargetFactor(), MC),
                    resultFactor.getQuoteRef().chain(sourceFactor.getQuoteRef()));
            }

            if (resultAsSource != null && resultAsSource.hasResult(indirectIsoCode)) {
                // 1 / (USD->isoSource * isoTarget->USD)
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<getFactorInternal> 1 / (" + indirectIsoCode + "->" + isoSource + " * " + isoTarget + "->" + indirectIsoCode + ") mapping => get both conversions and return result");
                }
                final ConversionResult resultFactor = getFactor(resultAsSource, isoTarget, indirectIsoCode, false, date);
                BigDecimal value = BigDecimal.ONE.divide(sourceFactor.getRate().getValue().multiply(resultFactor.getRate().getValue()), MC);
                return new ConversionResult(createPrice(value),
                        BigDecimal.ONE.divide(sourceFactor.getSourceToTargetFactor().multiply(resultFactor.getSourceToTargetFactor())),
                    resultFactor.getQuoteRef().chain(sourceFactor.getQuoteRef()));
            }
        }

        return null;
    }

    private Object[] getHistoricConversion(String currency, IsoCurrencyResult icr,
            LocalDate date) {
        final Quote quote = getQuote(icr);
        if (!StringUtils.hasText(quote.getSymbolMmwkn())) {
            throw new NoDataException("no historic data for " + quote.getId() + ".qid");
        }

        try {
            // TODO: change back to not use ZR when bug in pm is fixed (always return 1 for values before DM/USD timeseries)
            final MMTalkTableRequest request = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                    .withKey(quote.getSymbolMmwkn())
                    .withFormulas(Arrays.asList("object", "datum"))
                    .withPreFormula("Währung.Wechselkurszr[\"" + currency + "\"].at[\"" + DTF.print(date) + "\"]");
            final MMServiceResponse response = this.mmService.getMMTalkTable(request);
            final BigDecimal value = HistoricRatiosProviderImpl.getValue(response.getData()[0]);
            if (value != null) {
                return response.getData();
            }

            final String vwdfeed = quote.getSymbolVwdfeed();
            if (vwdfeed.endsWith(".XRATE.SPOT")) {
                // use reverse symbol to emulate wechselkurszr
                final String reverseSymbol = "10." + vwdfeed.substring(6, 9) + vwdfeed.substring(3, 6) + ".XRATE.SPOT";
                final Quote reverseQuote = this.instrumentProvider.identifyByVwdfeed(reverseSymbol);
                final MMTalkTableRequest xrateReq = new MMTalkTableRequest(MMKeyType.SECURITY_WKN)
                        .withKey(reverseQuote.getSymbolMmwkn())
                        .withFormulas(Arrays.asList("object", "datum"))
                        .withPreFormula("close.at[\"" + DTF.print(date) + "\"]");
                final MMServiceResponse xrateRes = this.mmService.getMMTalkTable(xrateReq);
                return xrateRes.getData();
            }
        }
        catch (MMTalkException e) {
            this.logger.warn("<getHistoricConversion> failed", e);
        }
        throw new NoDataException("no historic data found");
    }

    public Map<String, String> getCrossRateSymbols(Collection<String> isocodes) {
        final Map<String, String> result = new HashMap<>();

        for (final String from : isocodes) {
            for (final String to : isocodes) {
                if (!from.equals(to)) {
                    final String symbol = getCrossRateSymbol(from, to);
                    if (symbol != null) {
                        result.put(from + "-" + to, symbol);
                    }
                }
            }
        }
        return result;
    }

    @Override
    public String getCrossRateSymbol(String from, String to) {
        final Quote quote = getCrossRateQuote(from, to);
        return quote != null ? quote.getSymbolVwdfeed() : null;
    }

    public static void main(String[] args) throws Exception {
        final IsoCurrencyConversionProviderImpl cc = new IsoCurrencyConversionProviderImpl();
        cc.afterPropertiesSet();

        //final List<String> isocodes = Arrays.asList("AUD","CAD","CHF","EUR", "USD", "GBP","JPY"
        // ,"BGN", "CZK", "DKK", "EEK", "HUF", "ISK", "LTL", "LVL", "MTL", "NOK", "PLN", "RON"
        // , "RUB", "SEK", "SKK", "UAH");
        final List<String> isocodes = Arrays.asList("EUR", "JMD", "AOA", "MMK", "PAB", "VND", "IQD", "BSD", "BIF", "BGN", "MVR", "ECS", "KPW", "LYD", "KGS", "SYP", "PYG", "LSL", "MZN", "GTQ", "NPR", "ANG", "NGN", "KHR", "XA1", "IRR", "DJF", "PGK", "TTD", "ZMK", "XPF", "YER");
        final Map<String, String> map = cc.getCrossRateSymbols(isocodes);
        System.out.println("#" + map.size());
        for (final String from : Arrays.asList("EUR")) {
            for (final String to : isocodes) {
                if (from.equals(to)) {
                    continue;
                }
                final String symbol = map.get(from + "-" + to);
                System.out.println(from + " -> " + to + ": " + (symbol == null ? "___ERROR___" : symbol));
            }
        }
    }
}
