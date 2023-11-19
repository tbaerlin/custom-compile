/*
 * BndStammdaten.java
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

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.WMDataProvider;
import de.marketmaker.istar.merger.provider.WMDataRequest;
import de.marketmaker.istar.merger.provider.WMDataResponse;
import de.marketmaker.istar.merger.provider.bonddata.BenchmarkHistoryRequest;
import de.marketmaker.istar.merger.provider.bonddata.BenchmarkHistoryResponse;
import de.marketmaker.istar.merger.provider.bonddata.BondDataProvider;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.SymbolCommand;

/**
 * Retrieves a bond's benchmark history.
 * <p>
 * The performance of a bond can be measured against a standard bond, which is called benchmark bond.
 * To have an appropriate and useful comparison, the benchmark bond and the bond being measured against
 * it should have a comparable liquidity, issue size and coupon. Therefore a bond will be measured
 * before its maturity by different benchmark bonds.
 * </p>
 * <p>
 * Long-term bonds might have benchmark bonds that are already expired, therefore in the response:
 * <ul>
 * <li>current instrument data will be returned for those benchmark bonds that are still valid</li>
 * <li>WM data will be returned for those expired ones</li>
 * </ul>
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class BndBenchmarkHistory extends EasytradeCommandController {
    private EasytradeInstrumentProvider instrumentProvider;

    private BondDataProvider bondDataProvider;

    private WMDataProvider wmDataProvider;

    public static class Command extends DefaultSymbolCommand {

        /**
         * @sample BMKAU01Y.TFI
         */
        @NotNull
        @Override
        public String getSymbol() {
            return super.getSymbol();
        }
    }

    public BndBenchmarkHistory() {
        super(Command.class);
    }

    public void setWmDataProvider(WMDataProvider wmDataProvider) {
        this.wmDataProvider = wmDataProvider;
    }

    public void setBondDataProvider(BondDataProvider bondDataProvider) {
        this.bondDataProvider = bondDataProvider;
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final SymbolCommand cmd = (SymbolCommand) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);
        final BenchmarkHistoryResponse bhr = this.bondDataProvider.getBenchmarkHistory(new BenchmarkHistoryRequest(quote));
        final List<BenchmarkHistoryResponse.BenchmarkHistoryItem> items = bhr.getItems();

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("items", items);

        if (items != null) {
            final List<Long> iids = new ArrayList<>();
            for (final BenchmarkHistoryResponse.BenchmarkHistoryItem item : items) {
                iids.add(item.getBenchmarkIid());
            }
            final List<Instrument> instruments = this.instrumentProvider.identifyInstruments(iids);

            final Map<Long, Quote> iid2quote = new HashMap<>();
            for (final Instrument instrument : instruments) {
                if (instrument != null) {
                    try {
                        final Quote q = this.instrumentProvider.getQuote(instrument, null, null);
                        iid2quote.put(q.getInstrument().getId(), q);
                    } catch (Exception e) {
                        if (this.logger.isDebugEnabled()) {
                            this.logger.debug("<doHandle> no quote for " + instrument.getId() + ".iid => ignore");
                        }
                    }
                }
            }
            model.put("iid2quote", iid2quote);

            final WMDataResponse wmr = this.wmDataProvider.getData(
                new WMDataRequest(RequestContextHolder.getRequestContext().getProfile(), iids));
            final Map<Long, WMData> wmData = new HashMap<>();
            for (final Long iid : iids) {
                wmData.put(iid, wmr.getData(iid));
            }
            model.put("wmData", wmData);
        }

        return new ModelAndView("bndbenchmarkhistory", model);
    }

}