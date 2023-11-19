/*
 * IndexSearchListView.java
 *
 * Created on 19.03.2015 16:43:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.ArrayList;

@NonNLS
public class IndexSearchListView extends ContentPanel {
    private final IndexSearchListController controller;
    private SnippetTableWidget stwIndizes;
    private SnippetTableWidget stwConstituents;
    private HTMLTable.RowFormatter rowFormatter;
    private final Grid grid;

    public IndexSearchListView(final IndexSearchListController controller) {
//        setLayout(new VerticalLayout(20));
        addStyleName("mm-contentData");
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        this.controller = controller;

        grid = new Grid(3, 1);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.setStyleName("mm-widthDefault");
        this.rowFormatter = grid.getRowFormatter();
        grid.getCellFormatter().setHeight(1, 0, "20");

        final TableColumnModel columnModel = controller.createColumnModel();
        this.stwIndizes = SnippetTableWidget.create(columnModel, "mm-snippetTable");
        this.stwConstituents = SnippetTableWidget.create(columnModel, "mm-snippetTable");
        this.stwConstituents.setSortLinkListener(controller.getSortLinkListener());

        grid.setWidget(0, 0, this.stwIndizes);
        grid.setWidget(2, 0, this.stwConstituents);
        add(this.grid);

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config()
                .withAddFill(false)
        );
        setBottomComponent(pagingWidgets.getToolbar());
        controller.getPagingFeature().setPagingWidgets(pagingWidgets);
    }

    public void show(TableDataModel tdmIndizes, TableDataModel tdmConstituents) {
        final boolean indizesVisible = tdmIndizes == null ? false : isVisible(tdmIndizes);
        final boolean constituentsVisible = tdmConstituents == null ? false : isVisible(tdmConstituents);
        this.rowFormatter.setVisible(0, indizesVisible);
        this.rowFormatter.setVisible(1, indizesVisible);
        this.stwIndizes.updateData(tdmIndizes);
        this.rowFormatter.setVisible(2, constituentsVisible);
        this.stwConstituents.updateData(tdmConstituents);
        this.controller.getContentContainer().setContent(this);
    }

    private boolean isVisible(TableDataModel m) {
        return m.getRowCount() > 0 || m.getMessage() != null;
    }

    ArrayList<PushRenderItem> getRenderItems(DefaultTableDataModel dtm, boolean indices) {
        return (indices ? this.stwIndizes : this.stwConstituents).getRenderItems(dtm);
    }

    String getPrintHtml() {
        return this.grid.getElement().getInnerHTML();
    }
}
