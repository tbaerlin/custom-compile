/*
 * UnderlyingResolver.java
 *
 * Created on 20.03.13 16:19
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.instrument.Underlying;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;

/**
 * Used to determine the underlying iids for a set of instruments.
 * @author oflege
 */
public class UnderlyingResolver {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final EasytradeInstrumentProvider instrumentProvider;

    private final UnderlyingShadowProvider underlyingShadowProvider;

    private final Set<Quote> quotes = new HashSet<>();

    public UnderlyingResolver(EasytradeInstrumentProvider instrumentProvider,
            UnderlyingShadowProvider underlyingShadowProvider) {
        this.instrumentProvider = instrumentProvider;
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void addSymbol(String s, SymbolStrategyEnum symbolStrategy) {
        try {
            this.quotes.add(this.instrumentProvider.identifyQuote(s, symbolStrategy, null, null));
        } catch (UnknownSymbolException e) {
            // ignore
        }
    }

    public Set<Long> getUnderlyingIids(boolean withSiblings) {
        if (this.quotes.isEmpty()) {
            return Collections.emptySet();
        }
        Set<Long> result = new HashSet<>();
        for (Quote quote : quotes) {
            result.addAll(getUnderlyingIids(quote.getInstrument(), withSiblings));
        }
        return result;
    }

    public Set<Long> getUnderlyingIids(Instrument instrument, boolean withSiblings) {
        final Set<Long> result = new HashSet<>();

        final long iid = instrument.getId();
        result.add(iid);
        addShadows(result, iid);

        final List<Instrument> ders = this.instrumentProvider.getDerivates(iid, 20, InstrumentTypeEnum.FUT);
        if (!ders.isEmpty()) {
            for (final Instrument d : ders) {
                result.add(d.getId());
            }
        }

        final Long uiid = addUnderlying(result, instrument, withSiblings);
        if (uiid != null) {
            final Instrument uuiid = this.instrumentProvider.identifyInstruments(Arrays.asList(uiid)).get(0);
            if (uuiid != null) {
                addUnderlying(result, uuiid, withSiblings);
            }
        }

        return result;
    }

    private Long addUnderlying(Set<Long> set, Instrument instrument, boolean withSiblings) {
        if (instrument instanceof Derivative) {
            final long uiid = ((Derivative) instrument).getUnderlyingId();
            addShadows(set, uiid);
            addSiblings(set, withSiblings, uiid);

            // also use existent underlyingid independent of the instrument for that
            // iid (which may not exist in our environment because of a quoteless instrument of
            // type UND) => allows to find all futures based on CON-future quote as underlyingVwdcode
            // as search criteria
            set.add(uiid);

            return uiid;
        }

        if (instrument instanceof Underlying) {
            addSiblings(set, withSiblings, instrument.getId());
        }

        return null;
    }

    private void addSiblings(Set<Long> set, boolean withSiblings, long iid) {
        if (!withSiblings) {
            return;
        }
        final List<Instrument> instruments = this.instrumentProvider.getDerivates(iid, 20);
        if (instruments.size() == 20) {
            logger.warn("<addSiblings> num siblings larger than requested for " + iid + ".iid");
        }
        for (final Instrument sibling : instruments) {
            set.add(sibling.getId());
        }
    }

    private void addShadows(Set<Long> set, long iid) {
        final List<Long> list = this.underlyingShadowProvider.getInstrumentids(iid);
        if (list != null) {
            set.addAll(list);
        }

        // added to allow for mapping future -> ... -> option, sample is 965265.DTB.003
        final Long reverseShadow = this.underlyingShadowProvider.getShadowInstrumentId(iid);
        if (reverseShadow != null) {
            set.add(reverseShadow);
        }
    }



}
