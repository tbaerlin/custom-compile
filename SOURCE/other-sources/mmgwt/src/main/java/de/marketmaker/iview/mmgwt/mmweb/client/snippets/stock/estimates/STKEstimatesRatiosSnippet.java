/*
 * STKEstimatesTrendSnippet.java
 *
 * Created on 17.09.2008 13:14:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates;

import java.util.List;

import de.marketmaker.iview.dmxml.EstimatesField;
import de.marketmaker.iview.dmxml.EstimatesFields;
import de.marketmaker.iview.dmxml.STKEstimates;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

/**
 * @author Ulrich Maurer
 */
public class STKEstimatesRatiosSnippet extends
        AbstractSnippet<STKEstimatesRatiosSnippet, SnippetTableView<STKEstimatesRatiosSnippet>> implements
        SymbolSnippet {
    public static class Class extends SnippetClass {
        public Class() {
            super("STKEstimatesRatios", I18n.I.ratios()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new STKEstimatesRatiosSnippet(context, config);
        }
    }

    private DmxmlContext.Block<STKEstimates> blockEstimates;

    private final int colCount;


    protected STKEstimatesRatiosSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);
        this.blockEstimates = context.addBlock("STK_Estimates"); // $NON-NLS-0$
        if (Selector.isDzProfitEstimate()) {
            this.colCount = 3;
            this.blockEstimates.setParameters("year", new String[]{"fy0", "fy1", "fy2"}); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$
            this.setView(new SnippetTableView<>(this,
                    new DefaultTableColumnModel(new TableColumn[]{
                            new TableColumn("", 0.4f, TableCellRenderers.LABEL), // $NON-NLS-0$
                            new TableColumn("fy0", 0.2f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
                            new TableColumn("fy1", 0.2f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
                            new TableColumn("fy2", 0.2f, TableCellRenderers.DEFAULT_RIGHT) // $NON-NLS-0$
                    }, false)));
        }
        else {
            if (FeatureFlags.Feature.VWD_RELEASE_2015.isEnabled()) {
                this.colCount = 4;
                this.blockEstimates.setParameters("year", new String[]{"fy1", "fy2", "fy3", "fy4"}); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$
                this.setView(new SnippetTableView<>(this,
                        new DefaultTableColumnModel(new TableColumn[]{
                                new TableColumn("", 0.4f, TableCellRenderers.LABEL), // $NON-NLS-0$
                                new TableColumn("fy1", 0.15f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
                                new TableColumn("fy2", 0.15f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
                                new TableColumn("fy3", 0.15f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
                                new TableColumn("fy4", 0.15f, TableCellRenderers.DEFAULT_RIGHT) // $NON-NLS-0$
                        }, false)));
            } else {
                this.colCount = 2;
                this.blockEstimates.setParameters("year", new String[]{"fy1", "fy2"}); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
                this.setView(new SnippetTableView<>(this,
                        new DefaultTableColumnModel(new TableColumn[]{
                                new TableColumn("", 0.4f, TableCellRenderers.LABEL), // $NON-NLS-0$
                                new TableColumn("fy1", 0.2f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
                                new TableColumn("fy2", 0.2f, TableCellRenderers.DEFAULT_RIGHT) // $NON-NLS-0$
                        }, false)));
            }
        }
    }

    public void destroy() {
        destroyBlock(this.blockEstimates);
    }

    public void setSymbol(InstrumentTypeEnum type, String symbol, String name, String... compareSymbols) {
        this.blockEstimates.setParameter("symbol", symbol); // $NON-NLS-0$
    }

    public void updateView() {
        if (!(this.blockEstimates.isResponseOk())) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final STKEstimates estimates = this.blockEstimates.getResult();
        final List<EstimatesFields> listFields = estimates.getFields();

        final String currencySuffix;
        final String currencyMioSuffix;
        if (listFields.size() > 0 && listFields.get(0).getCurrency() != null) {
            final String currency = listFields.get(0).getCurrency();
            currencySuffix = I18n.I.inCurrency(currency);
            currencyMioSuffix = I18n.I.inMioCurrency(currency);
        }
        else {
            currencySuffix = ""; // $NON-NLS-0$
            currencyMioSuffix = ""; // $NON-NLS-0$
        }

        final DefaultTableDataModel tdm = new DefaultTableDataModel(13, this.colCount + 1);
        int row = -1;
        int column = 0;
        tdm.setValueAt(++row, column, ""); // $NON-NLS-0$
        tdm.setValueAt(++row, column, I18n.I.earningPerShare() + currencySuffix);
        tdm.setValueAt(++row, column, I18n.I.priceEarningRatioShortcut());
        tdm.setValueAt(++row, column, I18n.I.cashFlow() + currencySuffix);
        tdm.setValueAt(++row, column, I18n.I.netAssetPerShare() + currencySuffix);
        tdm.setValueAt(++row, column, "<b>" + I18n.I.targetFigures() + currencyMioSuffix + "</b>");  // $NON-NLS$
        tdm.setValueAt(++row, column, I18n.I.turnover());
        tdm.setValueAt(++row, column, I18n.I.ebit());
        tdm.setValueAt(++row, column, I18n.I.ebitda());
        tdm.setValueAt(++row, column, I18n.I.netDebt());
        tdm.setValueAt(++row, column, "<b>"+I18n.I.dividend()+"</b>"); // $NON-NLS$
        tdm.setValueAt(++row, column, I18n.I.dividendAndStock() + currencySuffix);
        tdm.setValueAt(++row, column, I18n.I.dividendYield());
        for (EstimatesFields fields : listFields) {
            row = -1;
            column++;
            tdm.setValueAt(++row, column, "<b>" + fields.getFiscalYearEnd().substring(6) + "</b>"); // $NON-NLS-0$ $NON-NLS-1$
            tdm.setValueAt(++row, column, render(Renderer.PRICE, fields.getEarningPerShare()));
            tdm.setValueAt(++row, column, render(Renderer.PRICE_MAX2, fields.getPriceEarningRatio()));
            tdm.setValueAt(++row, column, render(Renderer.PRICE_MAX2, fields.getCashflow()));
            tdm.setValueAt(++row, column, render(Renderer.PRICE_MAX2, fields.getNetAssetsPerShare()));
            ++row;
            tdm.setValueAt(++row, column, render(Renderer.LARGE_NUMBER, fields.getSales()));
            tdm.setValueAt(++row, column, render(Renderer.LARGE_NUMBER, fields.getEBIT()));
            tdm.setValueAt(++row, column, render(Renderer.LARGE_NUMBER, fields.getEBITDA()));
            tdm.setValueAt(++row, column, render(Renderer.LARGE_NUMBER, fields.getNetDebt()));
            ++row;
            tdm.setValueAt(++row, column, render(Renderer.PRICE, fields.getDividend()));
            tdm.setValueAt(++row, column, render(Renderer.PERCENT, fields.getDividendYield()));
        }

        // fill up if estimates.getFields() has not the expected size
        while (column < this.colCount) {
            row = -1;
            column++;
            tdm.setValueAt(++row, column, ""); // $NON-NLS-0$
        }
        getView().update(tdm);
    }

    private String render(final Renderer<String> renderer, final EstimatesField field) {
        return renderer.render(field == null ? null : field.getValue());
    }
}
