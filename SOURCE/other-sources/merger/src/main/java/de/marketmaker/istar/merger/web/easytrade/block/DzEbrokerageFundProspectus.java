/*
 * DzEbrokerageFundProspectus.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.data.DownloadableItem;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.provider.report.ReportConstraint;
import de.marketmaker.istar.merger.provider.report.ReportServiceDelegate;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.PreferIssuerFundQuoteStrategy;
import de.marketmaker.istar.ratios.frontend.RatioDataRecordImpl;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

import static de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum.QID;
import static de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider.qidSymbol;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@MmInternal
public class DzEbrokerageFundProspectus extends EasytradeCommandController {
    private static final ReportConstraint RC = new ReportConstraint(null, null, null, true);

    public static class Command {
        private String issuername;

        private String name;

        public String getIssuername() {
            return issuername;
        }

        public void setIssuername(String issuername) {
            this.issuername = issuername;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    private RatiosProvider ratiosProvider;

    private FundDataProvider fundDataProvider;

    private ReportServiceDelegate reportService;

    private EasytradeInstrumentProvider instrumentProvider;

    public DzEbrokerageFundProspectus() {
        super(Command.class);
    }

    public void setReportService(ReportServiceDelegate reportService) {
        this.reportService = reportService;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = AbstractFindersuchergebnis.getFields(InstrumentTypeEnum.FND, PermissionType.FUNDDATA);

        final RatioSearchRequest rsr = new RatioSearchRequest(RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
        rsr.setDataRecordStrategyClass(PreferIssuerFundQuoteStrategy.class);
        rsr.setType(InstrumentTypeEnum.FND);
        rsr.addParameter(fields.get(RatioDataRecord.Field.issuername).name().toLowerCase(),
                cmd.getIssuername());
        if (StringUtils.hasText(cmd.getName())) {
            rsr.addParameter(fields.get(RatioDataRecord.Field.name).name().toLowerCase(),
                    cmd.getName());
        }

        rsr.addParameter("sort1", fields.get(RatioDataRecord.Field.name).name().toLowerCase());
        rsr.addParameter("i", "0");
        rsr.addParameter("n", "500");


        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final List<Quote> quotes = new ArrayList<>();
        final List<RatioDataRecord> records = new ArrayList<>();
        final List<List<DownloadableItem>> reports = new ArrayList<>();

        final DefaultRatioSearchResponse drsr = (DefaultRatioSearchResponse) sr;
        for (final RatioDataResult rdr : drsr.getElements()) {
            final Quote quote;
            try {
                quote = this.instrumentProvider.identifyQuote(qidSymbol(rdr.getQuoteid()), QID, null);

            } catch (Exception e) {
                continue;
            }

            if (quote == null) {
                continue;
            }

            final List<DownloadableItem> l
                    = this.reportService.getReports(quote.getInstrument().getId(), InstrumentTypeEnum.FND, RC);
            records.add(new RatioDataRecordImpl(rdr.getInstrumentRatios(), rdr.getQuoteData(),
                    fields, RequestContextHolder.getRequestContext().getLocales()));
            quotes.add(quote);
            reports.add(l);
        }

        final Map<String, Object> model = new HashMap<>();
        if (!quotes.isEmpty()) {
            final FundDataRequest fdr = new FundDataRequest(quotes.get(0).getInstrument())
                    .withMasterData();

            final FundDataResponse fundResponse = this.fundDataProvider.getFundData(fdr);
            final MasterDataFund data = fundResponse.getMasterDataFunds().get(0);
            model.put("KAG", data.getIssuerName());
            model.put("FND_CompStreet", data.getIssuerStreet());
            model.put("FND_CompZip", data.getIssuerPostalcode());
            model.put("FND_CompCity", data.getIssuerCity());
            model.put("FND_CompCountry", data.getCountry());
            model.put("FND_CompContactEmail", data.getIssuerEmail());
            model.put("FND_CompUrl", data.getIssuerUrl());
        }
        model.put("quotes", quotes);
        model.put("records", records);
        model.put("reports", reports);
        return new ModelAndView("dzebrokeragefundprospectus", model);
    }
}