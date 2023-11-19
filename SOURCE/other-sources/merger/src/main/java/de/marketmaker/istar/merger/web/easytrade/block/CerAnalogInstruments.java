/**
 * Created on 07.09.11 13:44
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */


package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.domain.data.HighLow;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.CertificateTypeEnum;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.IllegalInstrumentTypeException;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.HighLowProvider;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.RatiosProvider;
import de.marketmaker.istar.merger.provider.UnknownSymbolException;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.RatioDataResult;


/**
 * This block returns certificates that are quite similar to the given certificate (by symbol)
 * and were issued by the given issuer.
 * <p>
 * For each certificates type, a number of rules exist to determine "similar" instruments.
 * Those rules are ordered hierarchically (the first rule finds the most similar instruments)
 * and will be applied iteratively until the requested number of similar instruments
 * has been found.
 *
 * @sample symbol 228545.qid
 */

public class CerAnalogInstruments extends AbstractFindersuchergebnis {

    public static class AnalogInstrumentsCommand extends DefaultSymbolCommand {

        private static final int DEFAULT_COUNT = 5;

        private String issuername;

        private int count = DEFAULT_COUNT;

        /**
         * @return name of issuer of requested similar certificates
         * @sample Commerzbank
         */
//        @NotNull
        public String getIssuername() {
            return issuername;
        }

        public void setIssuername(String issuername) {
            this.issuername = issuername;
        }

        /**
         * @return requested number of similar instruments, default is {@value #DEFAULT_COUNT}.
         */
        @Range(min = 1, max = 100)
        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private IntradayProvider intradayProvider;

    private HighLowProvider highLowProvider;

    private RatiosProvider ratiosProvider;

    public CerAnalogInstruments() {
        super(AnalogInstrumentsCommand.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setRatiosProvider(RatiosProvider ratiosProvider) {
        this.ratiosProvider = ratiosProvider;
    }

    public void setHighLowProvider(HighLowProvider highLowProvider) {
        this.highLowProvider = highLowProvider;
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    @Override
    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) throws Exception {
        final AnalogInstrumentsCommand cmd = (AnalogInstrumentsCommand) o;
        final Quote quote = getQuote(cmd);

        final Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields
                = getFields(InstrumentTypeEnum.CER);
        final RatioDataRecord reference = this.ratiosProvider.getRatioData(quote, fields);

        final Quote underlyingQuote
                = this.instrumentProvider.getUnderlyingQuote(quote.getInstrument(), null);

        final List<RatioDataResult> results = query(fields, reference, underlyingQuote, cmd);
        final List<Quote> quotes = identifyQuotes(results);
        final List<PriceRecord> priceRecords = this.intradayProvider.getPriceRecords(quotes);
        final List<HighLow> highLows = this.highLowProvider.getHighLows52W(quotes, priceRecords);

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("priceRecords", priceRecords);
        model.put("highLows", highLows);

        return new ModelAndView("ceranaloginstruments", model);
    }

    private List<Quote> identifyQuotes(List<RatioDataResult> results) {
        final List<Long> quoteids = new ArrayList<>();
        for (RatioDataResult result : results) {
            quoteids.add(result.getQuoteid());
        }
        return this.instrumentProvider.identifyQuotes(quoteids);
    }

    private Quote getQuote(SymbolCommand cmd) {
        final Quote result = this.instrumentProvider.getQuote(cmd);
        if (result == null) {
            throw new UnknownSymbolException("'" + cmd.getSymbol() + "', strategy="
                    + cmd.getSymbolStrategy());
        }
        if (result.getInstrument().getInstrumentType() != InstrumentTypeEnum.CER) {
            throw new IllegalInstrumentTypeException(result.getInstrument(), InstrumentTypeEnum.CER);
        }
        return result;
    }

    private List<RatioDataResult> query(
            Map<RatioDataRecord.Field, RatioFieldDescription.Field> fields,
            RatioDataRecord reference, Quote underlyingQuote, AnalogInstrumentsCommand cmd) {

        final String issuerName = getIssuerName(cmd);

        final CerAnalogInstrumentsQueryExecutor queryExecutor =
                CerAnalogInstrumentsQueryExecutor.getQueryExecutor(getType(reference));

        return queryExecutor.execute(this.ratiosProvider,
                fields, underlyingQuote, reference, issuerName, cmd.getCount());
    }

    private String getIssuerName(AnalogInstrumentsCommand cmd) {
        if (cmd.getIssuername() != null) {
            return cmd.getIssuername();
        }
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        return profile.isAllowed(Selector.DZ_BANK_USER) ? "DZ BANK"
                : profile.isAllowed(Selector.WGZ_BANK_USER) ? "WGZ BANK" : "DZ BANK";
    }

    private CertificateTypeEnum getType(RatioDataRecord reference) {
        final String certificateType = reference.getCertificateType();
        if (certificateType == null) {
            return CertificateTypeEnum.CERT_OTHER;
        }
        return CertificateTypeEnum.valueOf(certificateType);
    }
}