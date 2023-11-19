/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

import java.util.ArrayList;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceSnippetView extends SnippetView<PriceSnippet> {

    private SnippetTableWidget tw;

    private final Panel panel;

    public PriceSnippetView(PriceSnippet snippet) {
        super(snippet);
        setTitle(snippet.getConfiguration().getString("title")); // $NON-NLS-0$

        this.panel = new SimplePanel();
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    void update(TableColumnModel columnModel, TableDataModel dtm) {
        reloadTitle();
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(columnModel);
            this.panel.add(this.tw);
        }
        if (dtm != null) {
            this.tw.update(columnModel, dtm);
        }
    }

    void setVisible(String flipId, boolean visible) {
        this.tw.setVisible(flipId, visible);
    }

    void setVisible(int[] rows, boolean visible) {
        this.tw.setVisible(rows, visible);
    }

    void setText(int row, int column, String text, boolean asHtml) {
        this.tw.setText(row, column, text, asHtml);
    }

    void setLinkText(int row, int column, String text, boolean asHtml) {
        this.tw.setLinkText(row, column, text, asHtml);
    }

    public ArrayList<PushRenderItem> getRenderItems(TableDataModel tdm) {
        return this.tw.getRenderItems(tdm);
    }

    SnippetTableWidget getTableWidget() {
        return tw;
    }
}
