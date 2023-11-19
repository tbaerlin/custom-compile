/*
 * VwdPageSearchView.java
 *
 * Created on 06.08.2010 16:24:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author swild
 */
public class VwdPageSearchView extends ContentPanel {

    private final SnippetTableWidget mainContent;

    private final SearchToolbar searchToolbar;

    public VwdPageSearchView(VwdPageSearch searchController) {
        addStyleName("mm-contentData"); // $NON-NLS-0$

        setHeaderVisible(false);
        setBorders(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        searchToolbar = new SearchToolbar(searchController)
                .addPagingButtons()
                .addSpacer()
                .addSearchLabel(I18n.I.pageOrSearchterm())
                .addSearchBox()
                .withBookmarker();
        setTopComponent(searchToolbar);

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config());
        setBottomComponent(pagingWidgets.getToolbar());
        searchController.getPagingFeature().setPagingWidgets(pagingWidgets);

        final TableColumn[] columns = new TableColumn[]{
                new TableColumn(I18n.I.vwdPageHeading(),
                        400f, TableCellRenderers.VWD_PAGE_HEADING_LINK, "heading"), // $NON-NLS-0$
                new TableColumn(I18n.I.vwdPageNumber(),
                        70f, TableCellRenderers.VWD_PAGENUMER_LINK, "pagenumber") // $NON-NLS-0$
        };
        final TableColumnModel columnModel = new DefaultTableColumnModel(columns);
        this.mainContent = SnippetTableWidget.create(columnModel, "mm-snippetTable mm-WidthAuto"); // $NON-NLS-0$

        add(this.mainContent);
    }

    public void show(TableDataModel model, String query) {
        this.mainContent.updateData(model);
        this.searchToolbar.setQueryText(query);
    }

}
