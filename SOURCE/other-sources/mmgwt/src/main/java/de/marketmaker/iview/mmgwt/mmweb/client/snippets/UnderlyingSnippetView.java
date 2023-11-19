/*
 * NewsHeadlinesSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.VerticalPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UnderlyingSnippetView extends SnippetView<UnderlyingSnippet> {
    private TableColumnModel columnModel;

    private SnippetTableWidget tw;

    private final Panel panel;

    public UnderlyingSnippetView(UnderlyingSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.underlying()); 

        this.columnModel = createTableColumnModel();

        this.panel = new VerticalPanel();
        this.panel.setWidth("100%"); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    public static DefaultTableColumnModel createTableColumnModel() {
        final TableColumn[] result = new TableColumn[3];
        result[0] = new TableColumn(null, 0.4f).withCellClass("mm-snippetTable-label"); // $NON-NLS-0$
        result[1] = new TableColumn(null, 0.4f).withCellClass("mm-right"); // $NON-NLS-0$
        result[2] = new TableColumn(null, 0.2f).withCellClass("mm-right"); // $NON-NLS-0$

        result[0].setRenderer(TableCellRenderers.STRING);
        result[1].setRenderer(TableCellRenderers.STRING);
        result[2].setRenderer(TableCellRenderers.STRING);

        return new DefaultTableColumnModel(result, false);
    }

    void update(TableDataModel dtm) {
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel);
            panel.add(this.tw);
        }
        this.tw.updateData(dtm);
    }
}
