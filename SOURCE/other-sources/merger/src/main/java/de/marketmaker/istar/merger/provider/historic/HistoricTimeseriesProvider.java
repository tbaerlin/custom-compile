/*
 * HistoricTimeseriesProvider.java
 *
 * Created on 31.08.2006 14:49:27
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import java.util.List;

import org.joda.time.LocalDate;

import de.marketmaker.istar.merger.provider.PortfolioRatiosRequest;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface HistoricTimeseriesProvider {

    List<HistoricTimeseries> getTimeseries(HistoricTimeseriesRequest request);

    List<HistoricTimeseries> getTimeseries(PortfolioRatiosRequest request, LocalDate from, LocalDate to);

    List<HistoricTimeseries> getTimeseries(HistoricRequestImpl request);

    default PriceRecord getPriceRecord(HistoricRequest request, LocalDate to) {

        final PriceRecord pr = request.getPriceRecord();
        final InstrumentTypeEnum type = request.getQuote().getInstrument().getInstrumentType();
        final LocalDate onlyNewerThan = new LocalDate().minusDays(type == InstrumentTypeEnum.FND ? 5 : 2);

        if (pr != null
            && pr.getPriceQuality() != PriceQuality.NONE
            && pr.getPrice() != null
            && pr.getDate() != null
            && !pr.getDate().toLocalDate().isAfter(to)
            && pr.getDate().toLocalDate().isAfter(onlyNewerThan)) {
            return pr;
        }

        return null;
    }
}
