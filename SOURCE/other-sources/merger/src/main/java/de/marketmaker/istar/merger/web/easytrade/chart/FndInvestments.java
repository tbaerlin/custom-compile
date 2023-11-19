/*
 * FndInvestments.java
 *
 * Created on 29.08.2006 14:00:50
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.chart;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.bind.ServletRequestDataBinder;

import de.marketmaker.istar.domain.data.InstrumentAllocation;
import de.marketmaker.istar.domain.data.InstrumentAllocations;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.merger.provider.funddata.FundDataProvider;
import de.marketmaker.istar.merger.provider.funddata.FundDataRequest;
import de.marketmaker.istar.merger.provider.funddata.FundDataResponse;
import de.marketmaker.istar.merger.web.easytrade.EnumEditor;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.chart.ChartModelAndView;
import de.marketmaker.istar.chart.data.PieData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FndInvestments extends AbstractImgChart {

    private EasytradeInstrumentProvider instrumentProvider;

    private FundDataProvider fundDataProvider;

    public FndInvestments() {
        super(FndInvestmentsCommand.class);
    }

    protected void initBinder(HttpServletRequest httpServletRequest,
                              ServletRequestDataBinder binder) throws Exception {
        super.initBinder(httpServletRequest, binder);
        binder.registerCustomEditor(FndInvestmentsCommand.LabelType.class,
                new EnumEditor<>(FndInvestmentsCommand.LabelType.class));
    }

    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    public void setFundDataProvider(FundDataProvider fundDataProvider) {
        this.fundDataProvider = fundDataProvider;
    }

    protected ChartModelAndView createChartModelAndView(HttpServletRequest request,
                                                        HttpServletResponse response, Object object,
                                                        BindException bindException) throws Exception {

        final FndInvestmentsCommand cmd = (FndInvestmentsCommand) object;

        final Instrument instrument = instrumentProvider.getInstrument(cmd);
        if (instrument == null) {
            this.logger.warn("<handle> no instrument for symbol '" + cmd.getSymbol() + "'");
            return null;
        }

        final FundDataRequest fdr = new FundDataRequest(instrument).withAllocations();
        if (cmd.isWithConsolidatedAllocations()) {
            fdr.withConsolidatedAllocations();
        }
        fdr.setProviderPreference(cmd.getProviderPreference());

        final FundDataResponse fundResponse = this.fundDataProvider.getFundData(fdr);
        final InstrumentAllocations allAllocations = fundResponse.getInstrumentAllocationses().get(0);

        final List<InstrumentAllocation> allocations = getAllocations(cmd, allAllocations);

        final PieData pd = createPieData(cmd, allocations);
        if (pd == null) {
            return null;
        }

        final ChartModelAndView result = new ChartModelAndView(cmd.getLayout(), cmd.getStyle(),
                cmd.getWidth(), cmd.getHeight(), getEncodingConfig());
        result.addObject("pie", pd);
        return result;
    }

    private ImgPieChart.LabelFactory getNewLabelFactory(FndInvestmentsCommand cmd) {
        return cmd.getLabelType() == FndInvestmentsCommand.LabelType.NUMBER
                    ? new ImgPieChart.NumberLabelFactory() : new ImgPieChart.FullLabelFactory();
    }

    private List<InstrumentAllocation> getAllocations(FndInvestmentsCommand cmd,
            InstrumentAllocations allAllocations) {
        if ("TYP".equals(cmd.getAllokationstyp())) {
            return allAllocations.getAllocations(InstrumentAllocation.Type.SECTOR);
        }
        if ("LAND".equals(cmd.getAllokationstyp())) {
            return allAllocations.getAllocations(InstrumentAllocation.Type.COUNTRY);
        }
        if ("WAEHRUNG".equals(cmd.getAllokationstyp())) {
            return allAllocations.getAllocations(InstrumentAllocation.Type.CURRENCY);
        }
        if ("UNTERNEHMEN".equals(cmd.getAllokationstyp())) {
            return allAllocations.getAllocations(InstrumentAllocation.Type.INSTRUMENT);
        }
        for (InstrumentAllocation.Type type : InstrumentAllocation.Type.values()) {
            if (type.name().equals(cmd.getAllokationstyp())) {
                return allAllocations.getAllocations(type);
            }
        }
        return Collections.emptyList();
    }

    private PieData createPieData(FndInvestmentsCommand cmd, List<InstrumentAllocation> allocations) {
        for (InstrumentAllocation allocation : allocations) {
            if (allocation.getShare().signum() == -1) {
                // a short position, cannot add to pie, cannot render
                return null;
            }
        }

        PieData pd = null;
        if (!cmd.isByValue()) {
            try {
                pd = getByPercent(allocations, getNewLabelFactory(cmd), cmd.getNumItems(), cmd.getMinimumValue(), cmd.getDefaultLabel());
            } catch (IllegalArgumentException e) {
                // allocations add up to more than 100%, ignore and use getByValue instead
            }
        }

        if (pd == null) {
            pd = getByValue(allocations, getNewLabelFactory(cmd), cmd.getNumItems(), cmd.getMinimumValue());
        }
        pd.finish();
        return pd;
    }

    private PieData getByValue(List<InstrumentAllocation> allocations, ImgPieChart.LabelFactory lf, Integer numItems, Double minimumValue) {
        final PieData.ByValue pdByValue;
        if (numItems != null) {
            pdByValue = new PieData.ByValue(numItems);
        }
        else if (minimumValue != null) {
            pdByValue = new PieData.ByValue(minimumValue);
        }
        else {
            pdByValue = new PieData.ByValue();
        }

        if (allocations.isEmpty()) {
            pdByValue.add("100% Unbekannt", BigDecimal.ONE);
        }
        else {
            for (InstrumentAllocation allocation : allocations) {
                if (allocation.getShare().signum() == 1) {
                    pdByValue.add(lf.getLabel(allocation.getShare(), allocation.getCategory()),
                            allocation.getShare());
                }
            }
        }
        return pdByValue;
    }

    private PieData getByPercent(List<InstrumentAllocation> allocations, ImgPieChart.LabelFactory lf, Integer numItems, Double minimumValue, String defaultLabel) {
        final PieData.ByPercent pdByPercent;
        if (numItems != null) {
            pdByPercent = new PieData.ByPercent(numItems);
        }
        else if (minimumValue != null) {
            pdByPercent = new PieData.ByPercent(minimumValue);
        }
        else {
            pdByPercent = new PieData.ByPercent();
        }

        pdByPercent.setDefaultLabel(defaultLabel);
        if (!allocations.isEmpty()) {
            for (InstrumentAllocation allocation : allocations) {
                pdByPercent.add(lf.getLabel(allocation.getShare(),allocation.getCategory()), allocation.getShare().doubleValue() * 100);
            }
        }
        return pdByPercent;
    }
}
