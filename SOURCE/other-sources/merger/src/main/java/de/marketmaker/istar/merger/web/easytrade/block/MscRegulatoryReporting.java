/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.domain.data.RegulatoryReportingRecord;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.data.RegulatoryReportingRecordImpl;
import de.marketmaker.istar.merger.provider.RegulatoryReportingProvider;
import de.marketmaker.istar.merger.provider.RegulatoryReportingRequest;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

public class MscRegulatoryReporting extends EasytradeCommandController {

    private static final String TEMPLATE = "mscregulatoryreporting";

    private EasytradeInstrumentProvider instrumentProvider;

    private RegulatoryReportingProvider regulatoryReportingProvider;

    protected MscRegulatoryReporting() {
        super(DefaultSymbolCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRegulatoryReportingProvider(RegulatoryReportingProvider regulatoryReportingProvider) {
        this.regulatoryReportingProvider = regulatoryReportingProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response, Object o, BindException errors) throws Exception {
        final DefaultSymbolCommand cmd = (DefaultSymbolCommand) o;
        final Quote quote = this.instrumentProvider.getQuote(cmd);

        if (quote == null) {
            errors.reject("quote.unknown", "no quote found");
            return null;
        }

        final RegulatoryReportingRequest reportingRequest = new RegulatoryReportingRequest(quote.getInstrument().getId());
        final RegulatoryReportingRecord record = this.regulatoryReportingProvider.getPriceRegulatoryReportingRecord(reportingRequest);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("record", record);
        return new ModelAndView(TEMPLATE, model);
    }
}
