/*
 * MscQuoteMetadata.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.ContentFlags;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IntradayProvider;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Returns meta data for a quote, such as whether certain types of information are available
 * or not; if they are not, clients can avoid trying to request such data using other blocks.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscQuoteMetadata extends EasytradeCommandController {
    protected EasytradeInstrumentProvider instrumentProvider;

    protected IntradayProvider intradayProvider;

    protected CertificateDataProvider certificateDataProvider;

    protected UnderlyingShadowProvider underlyingShadowProvider;

    public MscQuoteMetadata() {
        super(DefaultSymbolCommand.class);
    }

    public void setIntradayProvider(IntradayProvider intradayProvider) {
        this.intradayProvider = intradayProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final InstrumentTypeEnum insType = quote.getInstrument().getInstrumentType();
        final ContentFlags cf = quote.getContentFlags();

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("screener", cf.isScreener() && profile.isAllowed(Selector.SCREENER));
        model.put("indexWithConstituents", cf.isIndexWithConstituents());
        model.put("edg", cf.isEdg() && (profile.isAllowed(Selector.EDG_RATING) || profile.isAllowed(Selector.EDG_DATA) || profile.isAllowed(Selector.EDG_DATA_2)));
        model.put("gisFndReport",
                InstrumentTypeEnum.FND == insType && cf.isVRPIF() && profile.isAllowed(Selector.UNION_PIFS));
        model.put("gisCerReport",
                InstrumentTypeEnum.CER == insType && cf.isVRPIF() && profile.isAllowed(Selector.DZBANK_CERTIFICATE_REPORTS));
        model.put("gisBndReport",
                InstrumentTypeEnum.BND == insType && cf.isVRPIF() && profile.isAllowed(Selector.DZBANK_CERTIFICATE_REPORTS));
        model.put("stockselectionFndReport",
                InstrumentTypeEnum.FND == insType && cf.isStockselectionFndReport()
                        && profile.isAllowed(Selector.STOCKSELECTION_FUND_REPORTS_DE));
        model.put("stockselectionCerReport",
                InstrumentTypeEnum.CER == insType && cf.isStockselectionCerReport()
                        && profile.isAllowed(Selector.STOCKSELECTION_CERTIFICATE_REPORTS));
        model.put("convensysI", cf.isConvensys() && profile.isAllowed(Selector.CONVENSYS_I));
        model.put("convensysII", cf.isConvensys() && profile.isAllowed(Selector.CONVENSYS_II));
        model.put("ilSole24OreAmf", cf.isIlSole24OreAmf() && profile.isAllowed(Selector.IL_SOLE_COMPANY_DATA_XML));
        model.put("orderbook", this.intradayProvider.isWithOrderbook(quote));
        model.put("estimates", (cf.isEstimatesReuters() && profile.isAllowed(Selector.THOMSONREUTERS_ESTIMATES_DZBANK))
                || (cf.isFactset() && profile.isAllowed(Selector.FACTSET)));
        model.put("analysis", profile.isAllowed(Selector.SMARTHOUSE_ANALYSES));
        model.put("kgvQuote", getKgvQuote(quote, profile));
        final List<Quote> underlyings = new RetrieveUnderlyingsMethod(quote, this.instrumentProvider,
                this.certificateDataProvider, this.underlyingShadowProvider).invoke();
        model.put("underlyings", underlyings);
        model.put("contributorQuote", isContributorQuote(quote));
        return new ModelAndView("mscquotemetadata", model);
    }


    private Quote getKgvQuote(Quote quote, Profile profile) {
        final String market = "JCF";

        for (final Quote q : quote.getInstrument().getQuotes()) {
            if (market.equals(q.getSymbolVwdfeedMarket())) {
                return profile.getPriceQuality(q) == PriceQuality.NONE ? null : q;
            }
        }

        return null;
    }

    private boolean isContributorQuote(Quote quote) {
        return quote.getMarket().getMarketcategory() != MarketcategoryEnum.BOERSE
                && this.intradayProvider.getPriceRecords(Collections.singletonList(quote)).get(0).getPrice().getValue() == null;
    }
}                                                                                             
