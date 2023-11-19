/*
 * InstrumentAssembleInfo.java
 *
 * Created on 24.10.13 11:49
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;
import de.marketmaker.istar.domainimpl.DomainContext;
import de.marketmaker.istar.domainimpl.instrument.InstrumentDp2;
import de.marketmaker.istar.domainimpl.instrument.MinimumQuotationSizeDp2;
import de.marketmaker.istar.domainimpl.instrument.QuoteDp2;
import de.marketmaker.istar.instrument.InstrumentUtil;

/**
 * Collects the information about an instrument and provides a method to create an
 * instrument with its quotes.
 */
class InstrumentAssembleInfo {
    static final InstrumentAssembleInfo EOF =
            new InstrumentAssembleInfo(null, -1, -1, MinimumQuotationSize.Unit.NOTHING);

    private final InstrumentDp2 instrument;

    private final int gd440;

    private final int gd455a;

    private final MinimumQuotationSize.Unit quotedPer;

    InstrumentAssembleInfo(InstrumentDp2 instrument, int gd440, int gd455a,
            MinimumQuotationSize.Unit quotedPer) {
        this.gd440 = gd440;
        this.gd455a = gd455a;
        this.instrument = instrument;
        this.quotedPer = quotedPer;
    }

    long getIid() {
        return this.instrument.getId();
    }

    boolean isUnderlying() {
        return instrument.getInstrumentType() == InstrumentTypeEnum.UND;
    }

    InstrumentDp2 getInstrument(QuotesForInstrument quotes, DomainContext dc) {
        if (quotes == null) {
            // only happens for quoteless underlyings
            return this.instrument;
        }
        if (this.instrument instanceof UnionDp2) {
            final InstrumentAssembleInfo realInfo = resolveUnion(quotes, dc);
            if (realInfo == null) {
                return null;
            }
            return realInfo.getInstrument(quotes, dc);
        }
        addQuotes(quotes);
        return this.instrument;
    }

    private InstrumentAssembleInfo resolveUnion(QuotesForInstrument quotes, DomainContext dc) {
        final UnionDp2 unionDp2 = (UnionDp2) this.instrument;

        final InstrumentDp2 ins = unionDp2.toRealInstrument(quotes, dc);
        if (ins == null) {
            return null;
        }

        return new InstrumentAssembleInfo(ins, this.gd440, this.gd455a, this.quotedPer);
    }

    private void addQuotes(QuotesForInstrument quotes) {
        this.instrument.setMmInstrumentclass(quotes.getInstrumentclass());

        for (final QuoteDp2 quote : quotes.quotes) {
            this.instrument.addQuote(quote);

            if (!QuotesReader.isQuoteWithLocalUnitSize(quote)) {
                MinimumQuotationSizeDp2 mqs = (MinimumQuotationSizeDp2) quote.getMinimumQuotationSize();
                mqs.setUnit(MinimumQuotationSize.Unit.valueOf(this.gd440));
                if (InstrumentUtil.isVwdFund(quote)) {
                    mqs.setNumber(BigDecimal.valueOf(this.gd455a));
                }
            }

            if (this.quotedPer != MinimumQuotationSize.Unit.NOTHING
                    && isUndefined(quote.getMinimumQuotationSize().getUnit())) {
                MinimumQuotationSizeDp2 mqs = (MinimumQuotationSizeDp2) quote.getMinimumQuotationSize();
                mqs.setUnit(this.quotedPer);
            }
        }
        HomeExchangeHeuristic.ensureHomeExchange(this.instrument);
    }

    private boolean isUndefined(final MinimumQuotationSize.Unit unit) {
        return unit == MinimumQuotationSize.Unit.NOTHING || unit == MinimumQuotationSize.Unit.OTHER;
    }
}
