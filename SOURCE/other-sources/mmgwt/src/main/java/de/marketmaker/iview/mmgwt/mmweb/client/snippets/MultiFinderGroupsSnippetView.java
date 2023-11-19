/*
 * MultiFinderGroupsSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MultiFinderGroupsController;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MultiFinderGroupsSnippetView extends SnippetView<MultiFinderGroupsSnippet> {
    private final Panel panel;
    private SnippetTableWidget tw;

    public MultiFinderGroupsSnippetView(MultiFinderGroupsSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.issuerCategoryMatrix()); 

        this.panel = new SimplePanel();
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    void update(TableDataModel dtm, List<String> columns) {
        reloadTitle();
        if (this.tw != null) {
            this.tw.removeFromParent();
        }
        this.tw = MultiFinderGroupsController.createTable(
                this.snippet.getConfiguration().getString("type"), // Y-Axis     $NON-NLS$
                this.snippet.getConfiguration().getString("secondaryField"), // X-Axis    $NON-NLS-0$
                I18n.I.issuer(), columns, dtm, this.snippet.linkListener, true);
        this.panel.add(this.tw);
    }
}
