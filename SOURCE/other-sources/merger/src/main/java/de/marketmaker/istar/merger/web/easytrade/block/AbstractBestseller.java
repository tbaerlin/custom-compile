/*
 * AbstractBestseller.java
 *
 * Created on 08.12.2006 10:17:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.CountCommand;
import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.iidSymbol;
import de.marketmaker.istar.merger.web.easytrade.provider.Bestseller;
import de.marketmaker.istar.merger.web.easytrade.provider.BestsellerProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractBestseller extends EasytradeCommandController {
    protected BestsellerProvider bestsellerProvider;

    private IntradayProvider intradayProvider;

    private EasytradeInstrumentProvider instrumentProvider;

    protected AbstractBestseller(Class aClass) {
        super(aClass);
    }

    public void setBestsellerProvider(BestsellerProvider bestsellerProvider) {
        this.bestsellerProvider = bestsellerProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected List<Quote> getQuotes(CountCommand c, Bestseller bestseller) {
        final List<Quote> quotes = new ArrayList<>(c.getCount());
        for (Long iid : bestseller.getInstruments()) {
            final Quote quote;
            try {
                quote = this.instrumentProvider.identifyQuote(iidSymbol(iid), null, null, null);
            } catch (UnknownSymbolException e) {
                this.logger.warn("<getQuotes> " + e.getMessage());
                continue;
            }
            if (quote != null) {
                quotes.add(quote);
            }
            if (quotes.size() >= c.getCount()) {
                break;
            }
        }
        return quotes;
    }

    protected Bestseller getBestseller(InstrumentTypeEnum type) {
        return this.bestsellerProvider.getBestseller(type);
    }

    protected List<PriceRecord> getPriceRecords(List<Quote> quotes) {
        return this.intradayProvider.getPriceRecords(quotes);
    }
}
