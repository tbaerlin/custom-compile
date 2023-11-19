package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnAndData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * ConfigurableTableSnippetView.java
 * Created on Nov 5, 2008 3:29:31 PM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ConfigurableTableSnippetView<S extends Snippet<S>> extends SnippetView<S> {

    private TableColumnAndData config;

    private SnippetTableWidget stw = null;

    private final SimplePanel panel = new SimplePanel();

    public ConfigurableTableSnippetView(S controller, TableColumnAndData config) {
        super(controller);
        super.setTitle(controller.getConfiguration().getString("title", "")); // $NON-NLS-0$ $NON-NLS-1$
        this.config = config;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    public void setConfig(TableColumnAndData config) {
        this.config = config;
    }

    public void update(TableDataModel tabData) {
        if (this.stw == null) {
            initWidgets();
        }
        this.stw.updateData(tabData);
        this.container.layout(true);
    }

    private void initWidgets() {
        if (this.config != null) {
            this.stw = SnippetTableWidget.create(this.config.getTableColumnModel());
            this.panel.setWidget(this.stw);
            this.container.layout();
        }
        else {
            Firebug.log("ConfigurableTableSnippetView.initWidgets(): this.conf == null"); // $NON-NLS-0$
        }
    }
}
