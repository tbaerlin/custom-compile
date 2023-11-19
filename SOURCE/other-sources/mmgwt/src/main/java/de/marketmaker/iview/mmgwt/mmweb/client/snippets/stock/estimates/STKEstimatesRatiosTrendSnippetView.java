/*
 * STKEstimatesRatiosTrendSnippetView.java
 *
 * Created on 1/23/14 9:38 AM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.estimates;

import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.iview.dmxml.EstimatesField;
import de.marketmaker.iview.dmxml.EstimatesFields;
import de.marketmaker.iview.dmxml.STKHistoricEstimates;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PriceWithCurrency;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Willenbrock
 */
public class STKEstimatesRatiosTrendSnippetView extends SnippetView<STKEstimatesRatiosTrendSnippet> {

    private TableColumnModel columnModelHistoricEstimates;

    private SnippetTableWidget twHistoricEstimates;

    private TableColumnModel columnModelPriceTarget;

    private SnippetTableWidget twPriceTarget;

    protected STKEstimatesRatiosTrendSnippetView(STKEstimatesRatiosTrendSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.ratiosTrend());

        this.columnModelHistoricEstimates = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("", 0.4f, TableCellRenderers.LABEL),
                new TableColumn(I18n.I.monthsAgo(3), 0.2f, TableCellRenderers.DEFAULT_RIGHT),
                new TableColumn(I18n.I.monthsAgo(1), 0.2f, TableCellRenderers.DEFAULT_RIGHT),
                new TableColumn(I18n.I.current(), 0.2f, TableCellRenderers.DEFAULT_RIGHT)
        });

        this.columnModelPriceTarget = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("&nbsp;", .6f, TableCellRenderers.LABEL), // $NON-NLS-0$
                new TableColumn("&nbsp;", .4f, TableCellRenderers.DEFAULT_RIGHT), // $NON-NLS-0$
        });
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        this.twHistoricEstimates = SnippetTableWidget.create(this.columnModelHistoricEstimates);
        this.twPriceTarget = SnippetTableWidget.create(this.columnModelPriceTarget);
        final FlowPanel panel = new FlowPanel();
        panel.add(this.twHistoricEstimates);
        panel.add(this.twPriceTarget);
        this.container.setContentWidget(panel);
        this.twHistoricEstimates.setVisible(true);
        this.twPriceTarget.setVisible(true);
    }

    protected void update(STKHistoricEstimates stkHistoricEstimates, EstimatesFields estimateFields,
                          PriceWithCurrency targetPrice, BigDecimal targetChange, PriceWithCurrency crossRate) {

        setTitle(I18n.I.ratiosTrend() + " " + estimateFields.getFiscalYearEnd().substring(6));
        final String currencySuffix = estimateFields.getCurrency() == null ? "" : (I18n.I.inCurrency(estimateFields.getCurrency()));

        {
            final List<RowData> listHistoric = new ArrayList<>(4);
            listHistoric.add(new RowData(I18n.I.earningPerShare() + currencySuffix,
                    Renderer.PRICE.render(stkHistoricEstimates.getEarningPerShare3M()),
                    Renderer.PRICE.render(stkHistoricEstimates.getEarningPerShare1M()),
                    render(Renderer.PRICE, estimateFields.getEarningPerShare())));
            listHistoric.add(new RowData(I18n.I.priceEarningRatioShortcut(),
                    Renderer.PRICE_MAX2.render(stkHistoricEstimates.getPriceEarningRatio3M()),
                    Renderer.PRICE_MAX2.render(stkHistoricEstimates.getPriceEarningRatio1M()),
                    render(Renderer.PRICE_MAX2, estimateFields.getPriceEarningRatio())));
            listHistoric.add(new RowData(I18n.I.cashFlow() + currencySuffix,
                    Renderer.PRICE_MAX2.render(stkHistoricEstimates.getCashflow3M()),
                    Renderer.PRICE_MAX2.render(stkHistoricEstimates.getCashflow1M()),
                    render(Renderer.PRICE_MAX2, estimateFields.getCashflow())));
            listHistoric.add(new RowData(I18n.I.earningPerShareGrowth(),
                    Renderer.PERCENT.render(stkHistoricEstimates.getEarningPerShareGrowth3M()),
                    Renderer.PERCENT.render(stkHistoricEstimates.getEarningPerShareGrowth1M()),
                    render(Renderer.PERCENT, estimateFields.getEarningPerShareGrowth())));
            this.twHistoricEstimates.updateData(DefaultTableDataModel.createWithRowData(listHistoric));
        }

        {
            final List<RowData> listPriceTarget = new ArrayList<>(4);

            listPriceTarget.add(new RowData(I18n.I.priceInMonths(12), Renderer.PRICE23_WITH_CURRENCY.render(targetPrice)));
            listPriceTarget.add(new RowData(I18n.I.differencePerformanceToCurrentPrice(), Renderer.CHANGE_PERCENT.render(targetChange.toString())));
            listPriceTarget.add(new RowData(I18n.I.currencyCalculatorExchangeRate(), Renderer.PRICE23_WITH_CURRENCY.render(crossRate)));

            this.twPriceTarget.updateData(DefaultTableDataModel.createWithRowData(listPriceTarget));
        }
    }

    protected void invalidate() {
        this.twHistoricEstimates.updateData(DefaultTableDataModel.NULL);
        this.twPriceTarget.updateData(DefaultTableDataModel.NULL);
    }

    private String render(final Renderer<String> renderer, final EstimatesField field) {
        return renderer.render(field == null ? null : field.getValue());
    }

}
