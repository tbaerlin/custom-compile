/*
 * MultiPriceListView.java
 *
 * Created on Jan 20, 2009 8:48:25 AM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;

/**
 * @author Michael Lösch
 */
public class MultiPriceListView extends SnippetView<MultiPriceListSnippet> {

    private ArrayList<ContentPanel> panels = new ArrayList<>();

    private final MultiPriceListSnippet snippet;

    private final Panel panel;

    public MultiPriceListView(MultiPriceListSnippet snippet) {
        super(snippet);
        this.panel = new FlowPanel();
        panel.addStyleName("mm-contentData"); // $NON-NLS-0$
        this.snippet = snippet;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setHeaderVisible(StringUtil.hasText(this.container.getHeading()));
        this.container.setContentWidget(this.panel);
    }

    void reset() {
        this.panels.clear();
    }

    private void initPanel() {
        this.panel.clear();

        for (MultiPriceListSnippet.ListDef listDef : this.snippet.getListDefs()) {
            if (listDef.getSymbols().isEmpty()) {
                continue;
            }
            final ContentPanel container = createPanel(listDef);

            this.panel.add(container);

            this.panels.add(container);
        }
    }

    private HTML createSpace() {
        final HTML result = new HTML("&nbsp;"); // $NON-NLS-0$
        result.setStyleName("mm-mpl-space"); // $NON-NLS-0$
        return result;
    }

    private ContentPanel createPanel(MultiPriceListSnippet.ListDef listDef) {
        final ContentPanel result = new ContentPanel();
        result.setHeaderVisible(isHeaderVisible());
        result.setHeading(listDef.getName());
        result.setStyleName("mm-snippet"); // $NON-NLS-0$
        result.add(createTable(listDef));
        result.add(createSpace());
        return result;
    }

    private SnippetTableWidget createTable(MultiPriceListSnippet.ListDef listDef) {
        final SnippetTableWidget result = SnippetTableWidget.create(getColumns(listDef));
        result.setWidth("100%"); // $NON-NLS-0$
        return result;
    }

    private boolean isHeaderVisible() {
        return !getConfiguration().getBoolean("noTitle", false); // $NON-NLS-0$
    }

    private TableColumnModel getColumns(MultiPriceListSnippet.ListDef listDef) {
        final VisibilityCheck withUnderlying = tc -> this.snippet.isWithUnderlying();
        final DefaultTableColumnModel model = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), 120f, TableCellRenderers.QUOTELINK_22).withId("name"), // $NON-NLS$
                new TableColumn(I18n.I.reference(), 120f, TableCellRenderers.DEFAULT).withId("ulying") // $NON-NLS$
                        .withVisibilityCheck(withUnderlying),
                new TableColumn(I18n.I.price(), 55f, TableCellRenderers.LAST_PRICE_WITH_SUPPLEMENT_PUSH).withId("prc"), // $NON-NLS$
                new TableColumn(I18n.I.price(), 55f, TableCellRenderers.LAST_PRICE_PUSH).withId("prcn"), // $NON-NLS$
                new TableColumn(I18n.I.time(), 55f, TableCellRenderers.DATE_OR_TIME_PUSH).withId("time"),  // $NON-NLS-0$
                new TableColumn(I18n.I.time(), 55f, TableCellRenderers.BIDASK_DATE_COMPACT_PUSH).withId("bat"),  // $NON-NLS-0$
                new TableColumn("+/-", 48f, TableCellRenderers.CHANGE_NET_PUSH).withId("chg"), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn("+/-%", 48f, TableCellRenderers.CHANGE_PERCENT_PUSH).withId("chg%"), // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.openAbbr(), 55f, TableCellRenderers.PRICE).withId("opn"),  // $NON-NLS-0$
                new TableColumn(I18n.I.high(), 55f, TableCellRenderers.HIGH_PUSH).withId("hd"),  // $NON-NLS-0$
                new TableColumn(I18n.I.low(), 55f, TableCellRenderers.LOW_PUSH).withId("ld"),  // $NON-NLS-0$
                new TableColumn(I18n.I.close(), 55f, TableCellRenderers.PRICE).withId("cls"),  // $NON-NLS-0$
                new TableColumn(I18n.I.bid(), 55f, TableCellRenderers.BID_PUSH).withId("bid"),  // $NON-NLS-0$
                new TableColumn(I18n.I.ask(), 55f, TableCellRenderers.ASK_PUSH).withId("ask"),  // $NON-NLS-0$
                new TableColumn(I18n.I.trend(), 55f, TableCellRenderers.TRENDBAR).withId("trnd"),  // $NON-NLS-0$
                new TableColumn("", 55f, TableCellRenderers.STRING_RIGHT).withId("ls"), // $NON-NLS$
                new TableColumn("", 55f, TableCellRenderers.STRING_RIGHT).withId("hs"), // $NON-NLS$
                new TableColumn("", 55f, TableCellRenderers.STRING_LEFT).withId("psl"), // $NON-NLS$
                new TableColumn("", 55f, TableCellRenderers.STRING_LEFT).withId("lsl"), // $NON-NLS$
                new TableColumn("", 55f, TableCellRenderers.STRING_LEFT).withId("hsl"), // $NON-NLS$
                new TableColumn(I18n.I.priceProvisionalEvaluationAbbr(), 55f, TableCellRenderers.PRICE).withId("lme_pe"),  // $NON-NLS$
                new TableColumn(I18n.I.previousClose(), 55f, TableCellRenderers.PRICE).withId("pprc"),  // $NON-NLS-0$
                new TableColumn(I18n.I.previousClose(), 55f, TableCellRenderers.PRICE_WITH_SUPPLEMENT).withId("pprcs"),  // $NON-NLS$
                new TableColumn(I18n.I.price(), 55f, TableCellRenderers.PRICE_WITH_SUPPLEMENT).withId("lme_pprcs"),  // $NON-NLS$ (within lme previous grouping)
                new TableColumn(I18n.I.price(), 55f, TableCellRenderers.PRICE).withId("lme_pprc"),  // $NON-NLS$ (within lme previous grouping)
                new TableColumn("", 55f, TableCellRenderers.STRING_LEFT).withId("lme_pprc_supp"),  // $NON-NLS$ (within lme previous grouping)
                new TableColumn(I18n.I.bidPreviousOfficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_pob"), //  $NON-NLS-0$
                new TableColumn(I18n.I.askPreviousOfficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_poa"), // $NON-NLS-0$
                new TableColumn(I18n.I.bidPreviousUnofficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_pub"), // $NON-NLS-0$
                new TableColumn(I18n.I.askPreviousUnofficialPriceAbbr(), -1f, TableCellRenderers.PRICE).withId("lme_pua"), // $NON-NLS-0$
                new TableColumn(I18n.I.settlement(), 55f, TableCellRenderers.PRICE).withId("sttl")  // $NON-NLS$
        }, listDef.hasColumnHeader());

        if(listDef.isLmeCurrentPreviousGroups()) {
            model.groupColumns(0, model.findIndexOfColumnById("pprcs"), I18n.I.dataOfCurrentDay()); // $NON-NLS$
            model.groupColumns(model.findIndexOfColumnById("lme_pprcs"), model.getColumnCount(), I18n.I.dataOfPreviousDay()); // $NON-NLS$
        }

        return model.withColumnOrder(getConfiguration().getArray("orderids", getDefaultOrderIds())); // $NON-NLS-0$
    }

    private String[] getDefaultOrderIds() {
        if (this.snippet.getType() == MultiPriceListSnippet.Type.COMPACT) {
            return new String[]{"name", "prc", "time", "chg", "chg%", "trnd"}; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$ $NON-NLS-5$
        }
        return new String[]{"name", "ulying", "prc", "time", "chg", "chg%", "opn", "hd", "ld", "cls", "bid", "ask", "trnd"}; // $NON-NLS$
    }

    public void update(DefaultTableDataModel[] dataModels) {
        if (dataModels == null || dataModels.length == 0) {
            reset();
            return;
        }

        if (this.panels.isEmpty()) {
            initPanel();
        }

        for (int i = 0; i < this.panels.size(); i++) {
            final ContentPanel panel = this.panels.get(i);
            if (dataModels[i] != null) {
                ((SnippetTableWidget) panel.getWidget(0)).updateData(dataModels[i]);
            }
            panel.setVisible(dataModels[i] != null);
        }
    }

    public ArrayList<PushRenderItem> getRenderItems(DefaultTableDataModel[] dataModels) {
        final ArrayList<PushRenderItem> result = new ArrayList<>();
        for (int i = 0; i < this.panels.size(); i++) {
            final ContentPanel panel = this.panels.get(i);
            if (dataModels[i] != null) {
                result.addAll(((SnippetTableWidget) panel.getWidget(0)).getRenderItems(dataModels[i]));
            }
            panel.setVisible(dataModels[i] != null);
        }
        return result.isEmpty() ? null : result;
    }
}
