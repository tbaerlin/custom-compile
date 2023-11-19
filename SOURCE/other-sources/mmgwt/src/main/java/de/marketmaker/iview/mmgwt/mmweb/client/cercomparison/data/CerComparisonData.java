package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data;

import de.marketmaker.iview.dmxml.CERDetailedStaticData;
import de.marketmaker.iview.dmxml.CERRatioData;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 08.09.2010 14:01:00
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class CerComparisonData {
    private final CerChartData chartData;
    private final CerPriceData priceData;
    private final CerData staticData;
    private final CerData ratioStaticData;
    private final CerUnderlyingData underlyingData;

    public CerComparisonData(DmxmlContext context, String symbol, String certType) {
        final DmxmlContext.Block<IMGResult> blockImg = context.addBlock("IMG_Chart_Analysis"); // $NON-NLS$
        blockImg.setParameter("period", "P1Y"); // $NON-NLS$
        blockImg.setParameter("width", "220"); // $NON-NLS$
        blockImg.setParameter("height", "160"); // $NON-NLS$
        blockImg.setParameter("chartlayout", "basic"); // $NON-NLS$
        blockImg.setParameter("symbol", symbol); // $NON-NLS$

        final DmxmlContext.Block<MSCPriceDataExtended> blockPrice = context.addBlock("MSC_PriceDataExtended"); // $NON-NLS$
        blockPrice.setParameter("symbol", symbol); // $NON-NLS$

        final DmxmlContext.Block<CERDetailedStaticData> blockCerStatic = context.addBlock("CER_DetailedStaticData"); // $NON-NLS$
        blockCerStatic.setParameter("symbol", symbol); // $NON-NLS$

        final DmxmlContext.Block<CERRatioData> blockRatios = context.addBlock("CER_RatioData"); // $NON-NLS$
        blockRatios.setParameter("symbol", symbol); // $NON-NLS$

        final DmxmlContext.Block<EDGData> blockEdg = context.addBlock("CER_EDG_Data"); // $NON-NLS$
        blockEdg.setParameter("symbol", symbol); // $NON-NLS$

        final DmxmlContext.Block<MSCQuoteMetadata> blockMetadata = context.addBlock("MSC_QuoteMetadata"); // $NON-NLS$
        blockMetadata.setParameter("symbol", symbol); // $NON-NLS$

        final DmxmlContext.Block<MSCPriceDataExtended> blockPriceUnderlying = context.addBlock("MSC_PriceDataExtended"); // $NON-NLS$
        blockPriceUnderlying.setParameter("symbol", "underlying(" + symbol + ")"); // $NON-NLS$

        this.chartData = new CerChartData(blockImg);
        this.priceData = new CerPriceData(blockPrice);
        this.staticData = new CerStaticData(blockCerStatic, blockRatios, blockEdg, certType);
        this.ratioStaticData = new CerStaticAndRatioData(blockCerStatic, blockRatios, blockEdg, certType);
        this.underlyingData = new CerUnderlyingData(blockCerStatic, blockMetadata, blockPriceUnderlying);
    }

    public CellData[] getFieldsOfSection(CerDataSection section) {
        switch (section) {
            case CHART:
                return this.chartData.getValues();
            case PRICE:
                return this.priceData.getValues();
            case STATIC:
                return this.staticData.getValues();
            case RATIOS:
                return this.ratioStaticData.getValues();
            case COMPARE_CHART:
                return new CellData[]{ CerData.EMPTY_CELL_DATA };
            case UNDERLYING:
                return this.underlyingData.getValues();
        }
        return null;
    }

    public String[] getFieldNamesOfSection(CerDataSection section) {
        switch (section) {
            case CHART:
                return this.chartData.getNames();
            case PRICE:
                return this.priceData.getNames();
            case STATIC:
                return this.staticData.getNames();
            case RATIOS:
                return this.ratioStaticData.getNames();
            case COMPARE_CHART:
                // Hack
                return new String[]{""};
            case UNDERLYING:
                return this.underlyingData.getNames();
        }
        return null;
    }
}