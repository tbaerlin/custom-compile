/*
 * RetrieveUnderlyingsMethod.java
 *
 * Created on 03.11.2011 19:45:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.marketmaker.istar.domainimpl.instrument.NullQuote;
import de.marketmaker.istar.merger.provider.ProviderPreference;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.DerivativeWithStrike;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.QuoteComparator;
import de.marketmaker.istar.domain.instrument.Underlying;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RetrieveUnderlyingsMethod {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EasytradeInstrumentProvider instrumentProvider;

    private final CertificateDataProvider certificateDataProvider;

    private final UnderlyingShadowProvider underlyingShadowProvider;

    private final List<Quote> quotes;

    public RetrieveUnderlyingsMethod(Quote quote,
            EasytradeInstrumentProvider instrumentProvider,
            CertificateDataProvider certificateDataProvider,
            UnderlyingShadowProvider underlyingShadowProvider) {
        this(Collections.singletonList(quote), instrumentProvider, certificateDataProvider, underlyingShadowProvider);
    }

    public RetrieveUnderlyingsMethod(List<Quote> quotes,
            EasytradeInstrumentProvider instrumentProvider,
            CertificateDataProvider certificateDataProvider,
            UnderlyingShadowProvider underlyingShadowProvider) {
        this.quotes = quotes;
        this.instrumentProvider = instrumentProvider;
        this.certificateDataProvider = certificateDataProvider;
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    /**
     * determine set of underlyings for a single quote, may return more than underlying quote if the
     * quote parameter is a basket
     * @return for standard derivatives: a single quote, for baskets: a list of quotes
     */
    public List<Quote> invoke() {
        if (this.quotes.size() != 1) {
            throw new IllegalStateException("only for quote lists of size 1");
        }
        final Quote quote = this.quotes.get(0);

        final Quote uquote = getUnderlyingQuote(quote);

        if (uquote != null) {
            return Collections.singletonList(uquote);
        }

        if (this.certificateDataProvider != null && mayBeBasket(quote)) {
            final Derivative d = (Derivative) quote.getInstrument();
            final String marketStrategy = getUnderlyingMarketStrategy(d);
            final MasterDataCertificate data
                    = this.certificateDataProvider.getMasterData(quote.getInstrument().getId(), ProviderPreference.VWD);
            final List<Quote> result = getBasketQuotes(data.getBasketIids(), marketStrategy);
            if (result.size() > 1) {
                Collections.sort(result,
                        QuoteComparator.byName(RequestContextHolder.getRequestContext().getQuoteNameStrategy()));
            }
            return result;
        }

        return Collections.emptyList();
    }

    private Quote getUnderlyingQuote(Quote quote) {
        if (quote.getInstrument() instanceof Underlying) {
            if (isWith1DTBQuotes()) {
                final Long uiid = this.underlyingShadowProvider.getShadowInstrumentId(quote.getInstrument().getId());
                if (uiid == null) {
                    return null;
                }

                final Instrument instrument = getInstrument(uiid);
                return getQuote(instrument, null);
            }
        }

        if (!(quote.getInstrument() instanceof Derivative)) {
            return null;
        }

        final Derivative d = (Derivative) quote.getInstrument();
        final String marketStrategy = getUnderlyingMarketStrategy(d);
        if (d.getUnderlyingId() > 0) {
            final Instrument uinstrument = getInstrument(d.getUnderlyingId());
            if (uinstrument == null) {
                return null;
            }

            return findUnderlyingQuote(marketStrategy, uinstrument);
        }

        // no underlying iid for baskets

        return null;
    }

    /**
     * get list of underlying iids for a list of quotes
     */
    public List<Quote> getUnderlyingQuotes() {
        final List<Quote> uqs = new ArrayList<>();
        for (final Quote quote : this.quotes) {
            uqs.add(getUnderlyingQuote(quote));
        }
        return uqs;
    }

    public static boolean isWith1DTBQuotes() {
        // TODO: needs refactoring
        return RequestContextHolder.getRequestContext().getBaseQuoteFilter() == QuoteFilters.FILTER_SPECIAL_MARKETS_WITH_1DTB;
    }

    private Quote findUnderlyingQuote(String marketStrategy, Instrument uinstrument) {
        if (!isWith1DTBQuotes() && uinstrument.getInstrumentType() == InstrumentTypeEnum.UND) {
            final Quote underlyingFUTQuote = findUnderlyingQuote(marketStrategy, getDerivates(uinstrument));
            if (underlyingFUTQuote != null) {
                return underlyingFUTQuote;
            }
        }
        return getQuote(uinstrument, marketStrategy);
    }

    private List<Instrument> getDerivates(Instrument uinstrument) {
        return this.instrumentProvider.getDerivates(uinstrument.getId(), 20, InstrumentTypeEnum.FUT);
    }

    private Quote findUnderlyingQuote(String marketStrategy, List<Instrument> instruments) {
        if (instruments.size() == 1) {
            return getQuote(instruments.get(0), marketStrategy);
        }
        for (final Instrument instrument : instruments) {
            if (instrument.getInstrumentType() != InstrumentTypeEnum.FUT) {
                continue;
            }
            for (final Quote q : instrument.getQuotes()) {
                final String defaultMmSymbol = instrument.getSymbol(KeysystemEnum.DEFAULTMMSYMBOL);
                if (StringUtils.endsWith(q.getSymbolVwdcode(), ".CON")
                        && StringUtils.equals(q.getSymbolMmwkn(), defaultMmSymbol)) {
                    return q;
                }
            }
            for (final Quote q : instrument.getQuotes()) {
                if (StringUtils.endsWith(q.getSymbolVwdcode(), ".CON")) {
                    return q;
                }
            }
        }
        return null;
    }

    private Quote getQuote(Instrument instrument, String marketStrategy) {
        try {
            return this.instrumentProvider.getQuote(instrument, null, marketStrategy);
        } catch (Exception e) {
            if (instrument != null) {
                return NullQuote.create(instrument);
            }
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("<getQuote> failed, returning null", e);
            }
        }

        return null;
    }

    private Instrument getInstrument(long iid) {
        return this.instrumentProvider.identifyInstruments(Collections.singletonList(iid)).get(0);
    }

    private boolean mayBeBasket(Quote q) {
        final InstrumentTypeEnum type = q.getInstrument().getInstrumentType();
        return type == InstrumentTypeEnum.CER || type == InstrumentTypeEnum.WNT;
    }

    private List<Quote> getBasketQuotes(List<Long> basketIids, String marketStrategy) {
        if (basketIids == null) {
            return Collections.emptyList();
        }
        final List<String> iids = new ArrayList<>(basketIids.size());
        for (Long basketIid : basketIids) {
            iids.add(basketIid + ".iid");
        }
        return this.instrumentProvider.identifyQuotes(iids, SymbolStrategyEnum.IID, null, marketStrategy);
    }

    private String getUnderlyingMarketStrategy(Derivative d) {
        if (d instanceof DerivativeWithStrike) {
            final DerivativeWithStrike dws = (DerivativeWithStrike) d;
            if (dws.getStrikeCurrency() != null) {
                final String iso = dws.getStrikeCurrency().getSymbolIso();
                if (iso != null) {
                    return "underlying,with_prices:" + iso;
                }
            }
        }
        // ensures special handling for crossrates, uses default strategy otherwise
        return "underlying,with_prices:XXX";
    }
}
