/*
 * PriceListSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.MSCBasicRatios;
import de.marketmaker.iview.dmxml.MSCBasicRatiosElement;
import de.marketmaker.iview.dmxml.MSCDerivedRiskData;
import de.marketmaker.iview.dmxml.MSCDerivedRiskDataElement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosSnippet extends AbstractSnippet<RatiosSnippet, RatiosSnippetView> implements SymbolSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("Ratios"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new RatiosSnippet(context, config);
        }
    }

    abstract class RowModel<E> {
        private final String title;

        protected RowModel(String title) {
            this.title = title;
        }

        abstract boolean isVisible();

        String getTitle() {
            return this.title;
        }

        abstract List<E> getDataList();

        abstract String getColumnData(E e);

        void setRowValues(final DefaultTableDataModel tdm, int row) {
            int col = 0;
            tdm.setValueAt(row, col, getTitle());
            for (E e : getDataList()) {
                col++;
                tdm.setValueAt(row, col, getColumnData(e));
            }
        }
    }

    abstract class RowModelRisk extends RowModel<MSCDerivedRiskDataElement> {
        protected RowModelRisk(String title) {
            super(title);
        }

        @Override
        boolean isVisible() {
            return blockRisk.isResponseOk();
        }

        @Override
        List<MSCDerivedRiskDataElement> getDataList() {
            return blockRisk.getResult().getData();
        }
    }

    abstract class RowModelRatios extends RowModel<MSCBasicRatiosElement> {
        protected RowModelRatios(String title) {
            super(title);
        }

        @Override
        boolean isVisible() {
            return blockRatios.isResponseOk();
        }

        @Override
        List<MSCBasicRatiosElement> getDataList() {
            return blockRatios.getResult().getData();
        }
    }


    private final DmxmlContext.Block<MSCDerivedRiskData> blockRisk;
    private final DmxmlContext.Block<MSCBasicRatios> blockRatios;
    private final String[] periods;
    private final boolean displayVolume;
    private Map<String, RowModel> mapRowModel;
    private final String[] rowids;

    private RatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.setView(new RatiosSnippetView(this));

        this.displayVolume = config.getBoolean("displayVolume", true); // $NON-NLS-0$

        final ArrayList<String> periodValues = config.getList("periodValues"); // $NON-NLS$
        final String[] periodList = periodValues.toArray(new String[periodValues.size()]);
        this.periods = replaceYearToDate(periodList);

        this.blockRisk = createBlock("MSC_DerivedRiskData"); // $NON-NLS-0$
        this.blockRisk.setParameter("symbol", config.getString("symbol", "314085.qid")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.blockRisk.setParameters("period", this.periods); // $NON-NLS-0$

        this.blockRatios = createBlock("MSC_BasicRatios"); // $NON-NLS-0$
        this.blockRatios.setParameter("symbol", config.getString("symbol", "314085.qid")); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        this.blockRatios.setParameters("period", this.periods); // $NON-NLS-0$

        this.mapRowModel = new HashMap<String, RowModel>();
        this.mapRowModel.put("volatility", new RowModelRisk(I18n.I.volatility()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCDerivedRiskDataElement element) {
                return Renderer.PERCENT.render(element.getVolatility());
            }
        });
        this.mapRowModel.put("beta", new RowModelRisk(I18n.I.beta()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCDerivedRiskDataElement element) {
                return Renderer.PRICE2.render(element.getBeta());
            }
        });
        this.mapRowModel.put("correlation", new RowModelRisk(I18n.I.correlation()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCDerivedRiskDataElement element) {
                return Renderer.PRICE2.render(element.getCorrelation());
            }
        });
        this.mapRowModel.put("performance", new RowModelRatios(I18n.I.performance()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return Renderer.CHANGE_PERCENT.render(element.getPerformance());
            }
        });
        this.mapRowModel.put("benchmark", new RowModelRatios(I18n.I.benchmark()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return Renderer.CHANGE_PERCENT.render(element.getPerformanceBenchmark());
            }
        });
        this.mapRowModel.put("avg-price", new RowModelRatios(I18n.I.averagePrice1()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return Renderer.PRICE2.render(element.getAveragePrice());
            }
        });
        this.mapRowModel.put("avg-volume", new RowModelRatios(I18n.I.averageVolume()) {  // $NON-NLS-0$
            @Override
            boolean isVisible() {
                return super.isVisible() && displayVolume;
            }

            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return Renderer.VOLUME.render(element.getAverageVolume());
            }
        });
        this.mapRowModel.put("high", new RowModelRatios(I18n.I.high()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return Renderer.PRICE2.render(element.getHigh());
            }
        });
        this.mapRowModel.put("high-date", new RowModelRatios(I18n.I.highWithDate()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return DateRenderer.date("").render(element.getHighDate()); // $NON-NLS-0$
            }
        });
        this.mapRowModel.put("low", new RowModelRatios(I18n.I.low()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return Renderer.PRICE2.render(element.getLow());
            }
        });
        this.mapRowModel.put("low-date", new RowModelRatios(I18n.I.lowWithDate()) {  // $NON-NLS-0$
            @Override
            String getColumnData(MSCBasicRatiosElement element) {
                return DateRenderer.date("").render(element.getLowDate()); // $NON-NLS-0$
            }
        });
        this.rowids = config.getArray("rowids", new String[]{ // $NON-NLS-0$
                "volatility", "beta", "correlation", "performance", "benchmark", // $NON-NLS$
                "avg-price", "avg-volume", "high", "high-date", "low", "low-date" // $NON-NLS$
        });
    }

    private String[] replaceYearToDate(String[] periodList) {
        for (int i = 0; i < periodList.length; i++) {
            if ("YTD".equals(periodList[i])) {  // $NON-NLS$
                periodList[i] = getYTDPeriod();
            }
        }
        return periodList;
    }

    private String getYTDPeriod() {
        final MmJsDate today = new MmJsDate().getMidnight();
        final MmJsDate januaryFirst = new MmJsDate(today.getFullYear(), MmJsDate.MONTH_JANUARY, 1, 0, 0, 0, 0);
        return "P" + today.getDiffDays(januaryFirst) + "D"; // $NON-NLS$
    }

    public void destroy() {
        destroyBlock(this.blockRisk);
    }

    void setLastPeriod(String period) {
        final String[] periods = new String[this.periods.length];
        System.arraycopy(this.periods, 0, periods, 0, periods.length);
        periods[periods.length - 1] = period;
        replaceYearToDate(periods);
        this.blockRisk.setParameters("period", periods); // $NON-NLS-0$
        this.blockRatios.setParameters("period", periods); // $NON-NLS-0$
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockRisk.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockRatios.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void setTitle(String title) {
        getView().setTitle(title);
    }

    public void updateView() {
        int rowCount = 0;
        for (String rowid : this.rowids) {
            if (this.mapRowModel.get(rowid).isVisible()) {
                rowCount++;
            }
        }
        final DefaultTableDataModel tdm = new DefaultTableDataModel(rowCount, this.periods.length + 1);
        int row = -1;
        for (String rowid : this.rowids) {
            final RowModel rowModel = this.mapRowModel.get(rowid);
            if (rowModel.isVisible()) {
                row++;
                rowModel.setRowValues(tdm, row);
            }
        }
        getView().update(tdm);
    }
}
