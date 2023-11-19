/*
 * MscStaticDataList.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.common.validator.Size;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.Sector;
import de.marketmaker.istar.domain.data.MasterDataStock;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.instrument.UnderlyingShadowProvider;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.ProfiledIndexCompositionProvider;
import de.marketmaker.istar.merger.provider.certificatedata.CertificateDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataProvider;
import de.marketmaker.istar.merger.provider.stockdata.StockDataRequest;
import de.marketmaker.istar.merger.web.easytrade.BaseMultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.MultiSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.util.LocalizedUtil;

/**
 * Similar to {@see MSC_StaticData}, but allows to request data for more than one instrument.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MscStaticDataList extends EasytradeCommandController {
    public static class Command extends BaseMultiSymbolCommand {
        @NotNull
        @Size(min = 1, max = 500)
        public String[] getSymbol() {
            return super.getSymbol();
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private EasytradeInstrumentProvider instrumentProvider;

    private CertificateDataProvider certificateDataProvider;

    private ProfiledIndexCompositionProvider indexCompositionProvider;

    private FundDataProvider fundDataProvider;

    private StockDataProvider stockDataProvider;

    private UnderlyingShadowProvider underlyingShadowProvider;

    public MscStaticDataList() {
        super(Command.class);
    }

    public void setUnderlyingShadowProvider(UnderlyingShadowProvider underlyingShadowProvider) {
        this.underlyingShadowProvider = underlyingShadowProvider;
    }

    public void setCertificateDataProvider(CertificateDataProvider certificateDataProvider) {
        this.certificateDataProvider = certificateDataProvider;
    }

    public void setStockDataProvider(StockDataProvider stockDataProvider) {
        this.stockDataProvider = stockDataProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setIndexCompositionProvider(
            ProfiledIndexCompositionProvider indexCompositionProvider) {
        this.indexCompositionProvider = indexCompositionProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final MultiSymbolCommand cmd = (MultiSymbolCommand) o;

        final String[] symbols = cmd.getSymbol();
        final Map<String, Instrument> instrumentsBySymbol
                = this.instrumentProvider.identifyInstrument(Arrays.asList(symbols), cmd.getSymbolStrategy());

        final List<Quote> quotes = new ArrayList<>();
        final List<Quote> quoteBenchmarks = new ArrayList<>();
        final List<List<Quote>> underlyings = new ArrayList<>();

        for (final String symbol : symbols) {
            final Instrument instrument = instrumentsBySymbol.get(symbol);
            if (instrument == null) {
                quotes.add(null);
                continue;
            }
            try {
                quotes.add(this.instrumentProvider.getQuote(instrument, null, cmd.getMarketStrategy()));
            } catch (Exception e) {
                if (this.logger.isDebugEnabled()) {
                    this.logger.debug("<doHandle> no quote found, ignore instrument: " + instrument.getId() + ".iid");
                }
                quotes.add(null);
            }
        }

        final List<Long> iids = new ArrayList<>(quotes.size());

        for (final Quote quote : quotes) {
            if (quote == null) {
                quoteBenchmarks.add(null);
                underlyings.add(null);
                iids.add(null);
                continue;
            }

            iids.add(quote.getInstrument().getId());

            final Quote quoteBenchmark = new BenchmarkQuoteMethod(quote, this.indexCompositionProvider,
                    this.fundDataProvider, this.instrumentProvider, null).invoke();
            quoteBenchmarks.add(quoteBenchmark);

            final List<Quote> qunderlyings = new RetrieveUnderlyingsMethod(quote, this.instrumentProvider,
                    this.certificateDataProvider, this.underlyingShadowProvider).invoke();
            underlyings.add(qunderlyings);
        }

        final List<String> sectors = getSectors(quotes, iids);

        final Map<String, Object> model = new HashMap<>();
        model.put("quotes", quotes);
        model.put("sectors", sectors);
        model.put("quoteBenchmarks", quoteBenchmarks);
        model.put("underlyings", underlyings);



        return new ModelAndView("mscstaticdatalist", model);
    }

    private List<String> getSectors(List<Quote> quotes, List<Long> iids) {
        final List<String> sectors = new ArrayList<>(iids.size());

        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (profile.isAllowed(Selector.ANY_VWD_TERMINAL_PROFILE)) {
            final List<MasterDataStock> masterDatas =
                this.stockDataProvider.getMasterData(iids, profile);
            for (final MasterDataStock masterData : masterDatas) {
                final String language = LocalizedUtil.getLanguage(masterData);
                sectors.add(masterData.getSector() != null
                        ? masterData.getSector().getLocalized(Language.valueOf(language))
                        : null);
            }
        }
        else {
            final Locale locale = RequestContextHolder.getRequestContext().getLocale();
            for (final Quote quote : quotes) {
                if (quote == null) {
                    sectors.add(null);
                    continue;
                }
                final Sector isector = quote.getInstrument().getSector();
                final Language lang = Language.valueOf(locale);
                sectors.add(isector != null ? isector.getNameOrDefault(lang) : null);
            }
        }
        return sectors;
    }
}
