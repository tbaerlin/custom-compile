/*
* StructDataSnippetView.java
*
* Created on 18.07.2008 10:59:36
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.table.*;
import com.google.gwt.user.client.ui.*;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Michael Lösch
 */
public class StructDataSnippetView extends SnippetView<StructDataSnippet> {

    private TableColumnModel columnModel;

    private SnippetTableWidget twStruct;

    private final SimplePanel panel;

    public StructDataSnippetView(StructDataSnippet snippet) {
        super(snippet);

        final SnippetConfiguration config = snippet.getConfiguration();

        super.setTitle(I18n.I.fundStructure()); 

        final TableColumn[] columns = new TableColumn[2];
        columns[0] = new TableColumn(config.getString("firstColHead",I18n.I.title()), 0.30f);  // $NON-NLS-0$
        columns[0].setRenderer(TableCellRenderers.STRING);
        columns[1] = new TableColumn(config.getString("secondColHead",I18n.I.portion()), 0.1f);  // $NON-NLS-0$
        columns[1].setRenderer(TableCellRenderers.PERCENT);

        this.columnModel = new DefaultTableColumnModel(columns);

        this.panel = new SimplePanel();
        this.panel.setWidth("100%"); // $NON-NLS-0$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.panel);
    }

    public void update(DefaultTableDataModel dtm) {
        reloadTitle();
        if (this.twStruct == null) {
            this.twStruct = SnippetTableWidget.create(this.columnModel);

/*            final Grid g = new Grid(4, 1);
            final HTMLTable.CellFormatter formatter = g.getCellFormatter();
            g.setCellPadding(0);
            g.setCellSpacing(0);
            g.setWidth("100%"); // $NON-NLS-0$
            g.setWidget(0, 0, this.twTops);
            g.setText(1, 0, " "); // $NON-NLS-0$
            formatter.setStyleName(1, 0, "mm-snippet-topflopBlank"); // $NON-NLS-0$
            g.setWidget(2, 0, this.twFlops);
            g.setWidget(3, 0, this.numUpDown);
*/
            this.panel.add(this.twStruct);
        }
        this.twStruct.updateData(dtm);
    }

}
