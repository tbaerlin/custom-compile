/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItemCollector;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.table.Subheading;

import java.util.ArrayList;

/**
 * A simple view that show a single SnippetTableWidget
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetTableView<S extends Snippet<S>> extends SnippetView<S> {
    private final TableColumnModel columnModel;

    protected final Panel panel;

    private SnippetTableWidget tw;
    private LinkListener<String> sortLinkListener;
    private Subheading subheading;
    private final String tableClass;
    private PriceSupport priceSupport;

    public static <V extends Snippet<V>> SnippetTableView<V> create(V snippet, TableColumnModel model) {
        return new SnippetTableView<>(snippet, model, null);
    }

    public SnippetTableView(S snippet, TableColumnModel columnModel) {
        this(snippet, columnModel, null);
    }

    public SnippetTableView(S snippet, TableColumnModel columnModel, String tableClass) {
        super(snippet);
        this.tableClass = tableClass == null ? "mm-snippetTable" : tableClass; // $NON-NLS-0$
        final SnippetConfiguration config = getConfiguration();
        setTitle(config.getString("title")); // $NON-NLS-0$

        this.columnModel = columnModel;

        final String height = config.getString("height", null); // $NON-NLS-0$
        this.panel = createPanel(height);
    }

    public SnippetTableView<S> withPriceSupport(PriceSupport priceSupport) {
        this.priceSupport = priceSupport;
        return this;
    }

    public SnippetTableView<S> withSubheading(Subheading subheading) {
        this.subheading = subheading;
        return this;
    }


    @Override
    protected void onContainerAvailable() {
        this.container.setHeaderVisible(StringUtility.hasText(this.container.getHeading()));
        this.container.setContentWidget(this.panel);
    }

    protected Panel createPanel(String height) {
        if (height == null) {
            return new SimplePanel();
        }
        final ScrollPanel sp = new ScrollPanel();
        sp.setHeight(height);
        return sp;
    }

    public void setSortLinkListener(LinkListener<String> sortLinkListener) {
        this.sortLinkListener = sortLinkListener;
    }

    private SnippetTableWidget getTableWidget() {
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel, new String[] {this.tableClass}, this.priceSupport);
            if (this.sortLinkListener != null) {
                this.tw.setSortLinkListener(this.sortLinkListener);
            }
            if (this.subheading != null) {
                this.tw.setSubheading(this.subheading);
            }
            this.panel.add(this.tw);
        }
        return this.tw;
    }

    public void update(TableDataModel dtm) {
        reloadTitle();
        getTableWidget().updateData(dtm);
    }

    public void update(TableColumnModel tcm, TableDataModel dtm) {
        reloadTitle();
        getTableWidget().update(tcm, dtm);
    }

    public void setMessage(String text, boolean asHtml) {
        reloadTitle();
        getTableWidget().setMessage(text, asHtml);
    }

    @Override
    public void setTitle(String title) {
        this.snippet.getConfiguration().put("title", title); // $NON-NLS-0$
        reloadTitle();
    }

    public ArrayList<PushRenderItem> getRenderItems(DefaultTableDataModel model) {
        return getTableWidget().getRenderItems(model);
    }

    public ArrayList<PushRenderItem> getRenderItems(PushRenderItemCollector collector) {
        return getTableWidget().getRenderItems(collector);
    }
}
