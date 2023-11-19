/*
 * PriceSearchView.java
 *
 * Created on 19.03.2008 16:43:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceListView extends ContentPanel {
    private final PriceListController controller;
    private SnippetTableWidget stwIndizes;
    private SnippetTableWidget stwConstituents;
    private HTMLTable.RowFormatter rowFormatter;
    private final Grid grid;

    public PriceListView(final PriceListController controller) {
//        setLayout(new VerticalLayout(20));
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        this.controller = controller;

        grid = new Grid(3, 1);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.setStyleName("mm-widthDefault"); // $NON-NLS-0$
        this.rowFormatter = grid.getRowFormatter();
        grid.getCellFormatter().setHeight(1, 0, "20"); // $NON-NLS-0$

        final TableColumnModel columnModel = controller.getListDetailsHelper().createTableColumnModel();
        this.stwIndizes = SnippetTableWidget.create(columnModel, "mm-snippetTable"); // $NON-NLS-0$
        this.stwConstituents = SnippetTableWidget.create(columnModel, "mm-snippetTable"); // $NON-NLS-0$
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
        final boolean indizesVisible = isVisible(tdmIndizes);
        final boolean constituentsVisible = isVisible(tdmConstituents);
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
