/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler;
import de.marketmaker.istar.domainimpl.data.HistoricDataProfiler.Entitlement;

/**
 * Since the historic data for securities are separately priced by data vendors
 * a check is needed for the permission of its display.
 * Clients of this factory are prevented from using the actual implementation of @link {@link PriceRecord} since
 * it might very well be subject to change or replacement.
 *
 * @implNote Due to module dependencies upon {@link PriceRecordIntraday} this is not part of {@link HistoricDataProfiler}
 */
public class RestrictedPriceRecordFactory {

    private static final HistoricDataProfiler PROFILER = new HistoricDataProfiler();

    public static PriceRecord createPriceRecord(Profile profile, Quote quote, PriceRecord priceRecord) {
        if (priceRecord == null) {
            return priceRecord;
        }

        final Entitlement entitlement = PROFILER.getEntitlement(profile, quote);
        switch (entitlement) {
            case ITRAXX_NOT_ENTITLED:
                return new PriceRecordItraxx(priceRecord);
            case FTSE_NOT_ENTITLED:
                return new PriceRecordIntraday(priceRecord, entitlement.getStart().get());
            default:
                return priceRecord;
        }
    }
}
