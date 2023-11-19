/*
 * StkStammdaten.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.provider.InternalFailure;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataProvider;
import de.marketmaker.istar.merger.provider.cdapi.CommonDataResponse;
import de.marketmaker.istar.merger.provider.edi.EdiDataResponse;
import de.marketmaker.istar.merger.provider.equity.EquityDataProvider;
import de.marketmaker.istar.merger.provider.equity.EquityDataResponse;
import de.marketmaker.istar.merger.web.easytrade.BadRequestException;
import io.grpc.Status;
import io.grpc.Status.Code;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Sector;
import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataRequest;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ErrorUtils;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;

import static de.marketmaker.istar.merger.web.easytrade.block.DividendCalculationStrategy.DIV_IN_QUOTE_CURRENCY;
import static de.marketmaker.istar.merger.web.easytrade.block.DividendCalculationStrategy.DIV_LAST_YEAR_IN_QUOTE_CURRENCY;
import static java.util.stream.Collectors.toList;

/**
 * Queries <b>key securities data</b> identified by a given financial <b>instrument symbol</b>. The
 * subject security is selected based on the given symbol w/o <b>symbol strategy</b> and market w/o
 * <b>market strategy</b>.
 *
 * <p>
 *     The following two Requests share the same code but deliver slightly different result fields:
 *     {@see STK_StaticData} and {@see MSC_StaticData}.
 *     Consider using {@see STK_StaticData} for stocks and {@see MSC_StaticData} for other instrument types.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StkStammdaten extends EasytradeCommandController {

    private EasytradeInstrumentProvider instrumentProvider;

    private CertificateDataProvider certificateDataProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private StockDataProvider stockDataProvider;

    private FundDataProvider fundDataProvider;

    private IsoCurrencyConversionProvider currencyConversionProvider;

    private UnderlyingShadowProvider underlyingShadowProvider;

    private EquityDataProvider equityDataProvider;

    private CommonDataProvider commonDataProvider;

    private String template = "stkstammdaten";

    public StkStammdaten() {
        super(DefaultSymbolCommand.class);
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setCurrencyConversionProvider(IsoCurrencyConversionProvider currencyConversionProvider) {
        this.currencyConversionProvider = currencyConversionProvider;
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIndexCompositionProvider(ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    public void setEquityDataProvider(EquityDataProvider equityDataProvider) {
        this.equityDataProvider = equityDataProvider;
    }

    public void setCommonDataProvider(CommonDataProvider commonDataProvider) {
        this.commonDataProvider = commonDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        if (quote == null) {
            ErrorUtils.rejectSymbol(cmd.getSymbol(), errors);
            return null;
        }

        final Map<String, Object> model = buildModel(quote);
        return new ModelAndView(this.template, model);
    }

    private Map<String, Object> buildModel(Quote quote) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final Quote quoteBenchmark = new BenchmarkQuoteMethod(quote, this.indexCompositionProvider,
                this.fundDataProvider, this.instrumentProvider, null).invoke();

        final MasterDataStock masterData =
            this.stockDataProvider.getMasterData(quote.getInstrument().getId(), profile);

        final String language = LocalizedUtil.getLanguage(masterData);

        final String sector;
        if (profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)) {
            sector = masterData.getSector() != null
                    ? masterData.getSector().getLocalized(Language.valueOf(language))
                    : null;
        }
        else {
            final Sector isector = quote.getInstrument().getSector();
            final Language lang = Language.valueOf(RequestContextHolder.getRequestContext().getLocale());
            sector = isector != null ? isector.getNameOrDefault(lang) : null;
        }

        final String gicsSector = masterData.getGicsSector() != null
                ? masterData.getGicsSector().getLocalized(Language.valueOf(language))
                : null;
        final String gicsIndustryGroup = masterData.getGicsIndustryGroup() != null
                ? masterData.getGicsIndustryGroup().getLocalized(Language.valueOf(language))
                : null;
        final String gicsIndustry = masterData.getGicsIndustry() != null
                ? masterData.getGicsIndustry().getLocalized(Language.valueOf(language))
                : null;
        final String gicsSubIndustry = masterData.getGicsSubIndustry() != null
                ? masterData.getGicsSubIndustry().getLocalized(Language.valueOf(language))
                : null;

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("quoteBenchmark", quoteBenchmark);
        model.put("masterData", masterData);
        model.put("sector", sector);

        model.put("dividendInQuoteCurrency",
                DIV_IN_QUOTE_CURRENCY.calculate(this.currencyConversionProvider, quote, masterData));
        model.put("dividendLastYearInQuoteCurrency",
                DIV_LAST_YEAR_IN_QUOTE_CURRENCY.calculate(this.currencyConversionProvider, quote, masterData));

        final List<Quote> underlyings = new RetrieveUnderlyingsMethod(quote, this.instrumentProvider,
                this.certificateDataProvider, this.underlyingShadowProvider).invoke();
        model.put("underlyings", underlyings);

        model.put("gicsSector", gicsSector);
        model.put("gicsIndustryGroup", gicsIndustryGroup);
        model.put("gicsIndustry", gicsIndustry);
        model.put("gicsSubIndustry", gicsSubIndustry);

        final List<EquityDataResponse> equityDataResponses =
            this.equityDataProvider.fetchEquityData(language,
                Collections.singletonList(quote.getInstrument().getSymbolIsin()));


        if (equityDataResponses.size() == 1 && !equityDataResponses.get(0).getResult().hasError()) {
            EquityDataResponse response = equityDataResponses.get(0);
            model.put("equity", response.getFields());
        }

        if (profile.isAllowed(Selector.INFRONT_SECTOR_CODES)) {
          final List<CommonDataResponse> commonDataResponses =
              this.commonDataProvider.fetchCommonData(
                  language, Collections.singletonList(quote.getInstrument().getSymbolIsin()));

          if (commonDataResponses.size() == 1 && !commonDataResponses.get(0).getResult().hasError()) {
            CommonDataResponse response = commonDataResponses.get(0);
            model.put("common", response.getFields());
          }
        }

        return model;
    }
}
