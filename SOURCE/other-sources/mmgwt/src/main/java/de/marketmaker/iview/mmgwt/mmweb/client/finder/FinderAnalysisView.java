/*
 * FinderAnalysisView.java
 *
 * Created on 11.06.2008 13:43:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.dmxml.RSCAnalysis;
import de.marketmaker.iview.dmxml.RSCFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SellHoldBuy;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderAnalysisView<F extends AbstractFinder> extends AbstractFinderView<F> {
    private static final TableCellRenderer STRING_22 = new MaxLengthStringRenderer(22, "");
    private final DateRenderer dateRenderer = DateRenderer.compactDateTime("--");

    private static final TableCellRenderer BUY_HOLD_SELL_BAR_RENDERER = new TableCellRendererAdapter() {
        public void render(Object data, StringBuffer sb, Context context) {
            final SellHoldBuy bhs = (SellHoldBuy) data;
            sb.append(Renderer.SELL_HOLD_BUY.render(bhs));
        }
    };

    private static final TableCellRenderer BUY_HOLD_SELL_RENDERER = new TableCellRendererAdapter() {
        public void render(Object data, StringBuffer sb, TableCellRenderer.Context context) {
            final SellHoldBuy shb = (SellHoldBuy) data;
            sb.append(shb.getAllSell()).append(" / ").append(shb.getHold()).append(" / ").append(shb.getAllBuy());
            sb.append(" : ");
            sb.append(Renderer.CHANGE_LARGE_PRICE_MAX2.render(String.valueOf(shb.getCoefficient())));
        }
    };


    FinderAnalysisView(F controller) {
        super(controller);
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    protected int getViewCount() {
        return 2;
    }

    protected int getNumRowsInDataGrid() {
        return 3;
    }

    protected int getSelectedView() {
        if (this.controller.isLiveFinder()) {
            return ((LiveFinderAnalysis) this.controller).isForIndex() ? 1 : 0;
        }
        return ((FinderAnalysis) this.controller).isForIndex() ? 1 : 0;
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        assert 2 == columnModels.length;

        TableCellRenderer analysisLinkRenderer = new MaxLengthStringRenderer(40, "--") {
            public void render(Object data, StringBuffer sb, Context context) {
                LinkContext lc = (LinkContext) data;
                RSCFinderElement e = (RSCFinderElement) lc.data;
                final String display = getMaxLengthText(e.getHeader());
                context.appendLink(lc, display, display != e.getHeader() ? e.getHeader() : null, sb);
            }
        };

        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.date(), -1f, DATE, "date").alignRight()  // $NON-NLS-0$
                , new TableColumn(I18n.I.analysis(), -1f, analysisLinkRenderer)
                , new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "instrumentname") // $NON-NLS$
                , new TableColumn(I18n.I.estimation(), -1f, RSC_RECOMMENDATION, "recommendation") // $NON-NLS$
                , new TableColumn(I18n.I.analystsCompany(), -1f, STRING_RIGHT, "analyst") // $NON-NLS$
        });

        columnModels[1] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "instrumentname") // $NON-NLS$
                , new TableColumn(I18n.I.sellHoldBuyCoefficientAbbr(), -1f, BUY_HOLD_SELL_RENDERER).alignRight()
                , new TableColumn(I18n.I.sellHoldBuy(), -1f, BUY_HOLD_SELL_BAR_RENDERER).withCellClass("mm-sellHoldBuyCell")  // $NON-NLS-0$
                , new TableColumn(I18n.I.index(), -1f, STRING)
                , new TableColumn(I18n.I.sector(), -1f, STRING_22)
        });
    }

    protected void showView(int n) {
        this.g.setHTML(2, 0, ""); // $NON-NLS-0$
    }

    public void showAnalysis(RSCAnalysis analysis) {
        if (analysis == null) {
            this.g.setWidget(2, 0, null);
            return;
        }
        final ContentPanel panel = new ContentPanel();
        panel.addStyleName("mm-snippet"); // $NON-NLS-0$
        panel.addStyleName("mm-analysis-panel"); // $NON-NLS-0$
        panel.setHeading(this.dateRenderer.render(analysis.getDate()) + " - " + analysis.getHeader());
        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.setStyleName("mm-analysis"); // $NON-NLS-0$
        flowPanel.add(new HTML(Renderer.RSC_RECOMMENDATION.render(analysis.getRecommendation())));
        flowPanel.add(new Label(analysis.getText()));
        panel.add(flowPanel);
        this.g.setWidget(2, 0, panel);
    }
}
