/*
 * FndAllocations.java
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
import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.data.MasterDataFund;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.web.easytrade.DefaultSymbolCommand;
import de.marketmaker.istar.merger.web.easytrade.ProviderSelectionCommand;

/**
 * Provides investment structures of a given fund.
 * <p>
 * The structure of a fund can be described in its holdings and investment focuses in terms of
 * shares(percentage). Each share is defined by its type and values(short/long position).
 * <table border="1">
 * <tr><th>Share Type</th><th>Explanation</th></tr>
 * <tr><td>CONSOLIDATED</td><td>share value is consolidated from values in long and short position</td></tr>
 * <tr><td>STANDARDIZED</td><td>share value is standardized to 100</td></tr>
 * <tr><td>NIL</td><td>share value is given just as it is</td></tr>
 * </table>
 * Following allocations are available:
 * <table border="1">
 * <tr><th>Type</th><th>Explanation</th></tr>
 * <tr><td>holdings</td><td>share in which instrument<br/><b>Only top holdings are listed</b></td></tr>
 * <tr><td>currency</td><td>share in which currency</td></tr>
 * <tr><td>country</td><td>share in which country</td></tr>
 * <tr><td>sector</td><td>share in which industry sector</td></tr>
 * <tr><td>asset classes</td><td>share in what kind of asset classes</td></tr>
 * </table>
 * </p>
 * <p>
 * Following remarks apply for allocations of funds with Morningstar data:
 * <table border="1">
 * <tr><th>Allocation Type</th><th>Content</th></tr>
 * <tr><td>currency</td><td>value in share is same as in long position. negative value is possible</td></tr>
 * <tr><td>country</td><td>value in share with share type STANDARDIZED</td></tr>
 * <tr><td>sector</td><td>value in share with share type STANDARDIZED</td></tr>
 * <tr><td>asset classes</td><td>value in share is consolidated from values in long and short position</td></tr>
 * </table>
 * </p>
 * <p>
 * Not all funds have shares in all kinds of allocations.
 * </p>
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndAllocations extends EasytradeCommandController {
    public static class Command extends DefaultSymbolCommand implements ProviderSelectionCommand {
        private boolean withConsolidatedAllocations;

        private String providerPreference;

        @RestrictedSet("FWW,VWDIT,VWDBENL,MORNINGSTAR,SSAT,FIDA,VWD")
        public String getProviderPreference() {
            return providerPreference;
        }

        public void setProviderPreference(String providerPreference) {
            this.providerPreference = providerPreference;
        }

        /**
         * @return if set true, share value is consolidated from values in long and short position.
         * Currently only for Morningstar.
         */
        public boolean isWithConsolidatedAllocations() {
            return withConsolidatedAllocations;
        }

        public void setWithConsolidatedAllocations(boolean withConsolidatedAllocations) {
            this.withConsolidatedAllocations = withConsolidatedAllocations;
        }

        /**
         * @sample 4229.qid
         */
        @NotNull
        public String getSymbol() {
            return super.getSymbol();
        }
    }

    private EasytradeInstrumentProvider instrumentProvider;

    private FundDataProvider fundDataProvider;

    public FndAllocations() {
        super(Command.class);
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;

        final Quote quote = this.instrumentProvider.getQuote(cmd);

        final FundDataRequest fdr = new FundDataRequest(quote.getInstrument()).withMasterData().withAllocations();
        if (cmd.isWithConsolidatedAllocations()) {
            fdr.withConsolidatedAllocations();
        }
        fdr.setProviderPreference(cmd.getProviderPreference());

        final FundDataResponse fundResponse = this.fundDataProvider.getFundData(fdr);
        final InstrumentAllocations allAllocations = fundResponse.getInstrumentAllocationses().get(0);
        final MasterDataFund.Source source = allAllocations.getSource();
        final MasterDataFund masterData = fundResponse.getMasterDataFunds().get(0);

        final List<List<InstrumentAllocation>> allocations = new ArrayList<>();
        final List<InstrumentAllocation.Type> types = new ArrayList<>();

        for (final InstrumentAllocation.Type type : InstrumentAllocation.Type.values()) {
            allocations.add(allAllocations.getAllocations(type));
            types.add(type);
        }

        final Map<String, Object> model = new HashMap<>();
        model.put("quote", quote);
        model.put("types", types);
        model.put("source", source);
        model.put("masterData", masterData);
        model.put("allocations", allocations);
        return new ModelAndView("fndallocations", model);
    }
}