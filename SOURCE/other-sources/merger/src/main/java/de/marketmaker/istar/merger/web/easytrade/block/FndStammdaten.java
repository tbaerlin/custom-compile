/*
 * BndStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;


import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataProvider;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;

/**
 * Queries <b>key fund data</b> identified by the given <b>symbol</b>.
 * The key fund data contains data like issuer information, fees, ratings and other.
 * <p>
 *     The following two Requests share the same code but deliver slightly different result fields:
 *     {@see FND_StaticData} and {@see FND_DetailedStaticData}.
 *     Consider using {@see FND_StaticData}.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 *
 * @sample symbol 4229.qid
 */
@Slf4j
public class FndStammdaten extends EasytradeCommandController {
    public static class Command extends DefaultSymbolCommand implements
            ProviderSelectionCommand {
        private String providerPreference;

        @RestrictedSet("FWW,VWDIT,VWDBENL,MORNINGSTAR,SSAT,FIDA,VWD")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private FundDataProvider fundDataProvider;

    protected ProfiledIndexCompositionProvider indexCompositionProvider;

    private CommonDataProvider commonDataProvider;

    private String template = "fndstammdaten";

    public FndStammdaten() {
        super(Command.class);
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }


    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final FundDataRequest fdr = new FundDataRequest(quote.getInstrument()).withMasterData();

        fdr.setProviderPreference(cmd.getProviderPreference());

        final Profile profile = fdr.getProfile();
        if (profile.isAllowed(Selector.RATING_FERI)) {
            fdr.withFeriRating();
        }
        if (profile.isAllowed(Selector.RATING_MORNINGSTAR)) {
            fdr.withMorningstarRating();
        }
        if (profile.isAllowed(Selector.RATING_MORNINGSTAR_UNION_FND)) {
            fdr.withMorningstarRatingDz();
        }

        final FundDataResponse fundResponse = this.fundDataProvider.getFundData(fdr);

        final String feriRating = fdr.isWithFeriRating() ? fundResponse.getFeriRatings().get(0) : null;
        final Integer morningstars = fdr.isWithMorningstarRating() || fdr.isWithMorningstarRatingDz() ? fundResponse.getMorningstarRatings().get(0) : null;
        final MasterDataFund masterData = fundResponse.getMasterDataFunds().get(0);
        final String language = LocalizedUtil.getLanguage(masterData);

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("masterData", masterData);
        model.put("feriRating", feriRating);
        model.put("morningstarRating", morningstars);
        model.put("morningstarRatingDate", masterData.getMorningstarRatingDate());
        model.put("language", language);

        if (this.indexCompositionProvider != null) {
            final Map<Quote, BigDecimal> benchmarkQuotes = new BenchmarkQuoteMethod(quote, this.indexCompositionProvider,
                    this.fundDataProvider, this.instrumentProvider, masterData).invokeList();
            model.put("benchmarkQuote", getSingleQuote(benchmarkQuotes));
            model.put("benchmarkQuotes", benchmarkQuotes);
        }

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {

            final List<CommonDataResponse> commonDataResponses =
                this.commonDataProvider.fetchCommonData(
                    language, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

            if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
                CommonDataResponse commonDataResponse = commonDataResponses.get(0);
                model.put("common", commonDataResponse.getFields());
            }
        }

        return new ModelAndView(this.template, model);
    }

    private Quote getSingleQuote(Map<Quote, BigDecimal> benchmarkQuotes) {
        if (benchmarkQuotes == null || benchmarkQuotes.isEmpty()) {
            return null;
        }

        return benchmarkQuotes.entrySet().iterator().next().getKey();
    }
}