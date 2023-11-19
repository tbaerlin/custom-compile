/*
 * PriceSearchView.java
 *
 * Created on 19.03.2008 16:43:26
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSelectionViewButtons;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceSearchView extends ContentPanel {
    private final PriceSearchController controller;

    private ViewSelectionViewButtons viewSelectionView;

    private SnippetTableWidget stw;

    public PriceSearchView(PriceSearchController controller) {
//        setLayout(new FitLayout());
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setBorders(false);
        setScrollMode(Style.Scroll.AUTO);

        this.controller = controller;
        final VisibilityCheck dzBankLink = SimpleVisibilityCheck.valueOf(Permutation.GIS.isActive());
        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());


        final TableColumn[] columns = new TableColumn[]{
                new TableColumn("", 0.05f, TableCellRenderers.VR_ICON_LINK).withVisibilityCheck(dzBankLink),
                new TableColumn(I18n.I.name(), 180f, new TableCellRenderers.QuoteLinkRenderer(30, "")),  // $NON-NLS-0$
                new TableColumn("WKN", 60f, TableCellRenderers.STRING).alignCenter().withVisibilityCheck(showWknCheck), // $NON-NLS-0$
                new TableColumn("ISIN", 80f, TableCellRenderers.STRING).alignCenter().withVisibilityCheck(showIsinCheck), // $NON-NLS-0$
                new TableColumn(I18n.I.marketName(), 80f, TableCellRenderers.STRING),
                new TableColumn(I18n.I.priceValue(), 50f, TableCellRenderers.PRICE_WITH_SUPPLEMENT),
                new TableColumn("", 20f, TableCellRenderers.STRING), // $NON-NLS-0$
                new TableColumn("+/-", 35f, TableCellRenderers.CHANGE_NET), // $NON-NLS-0$
                new TableColumn("+/- %", 35f, TableCellRenderers.CHANGE_PERCENT), // $NON-NLS-0$
                new TableColumn(I18n.I.trend(), 45f, TableCellRenderers.TRENDBAR), 
                new TableColumn(I18n.I.bid(), 35f, TableCellRenderers.PRICE), 
                new TableColumn(I18n.I.bidVolumeAbbr1(), 45f, TableCellRenderers.VOLUME_LONG),
                new TableColumn(I18n.I.ask(), 35f, TableCellRenderers.PRICE), 
                new TableColumn(I18n.I.askVolumeAbbr1(), 45f, TableCellRenderers.VOLUME_LONG),
                new TableColumn(I18n.I.dateTime(), 70f, TableCellRenderers.COMPACT_DATETIME).alignRight() 
        };

        final TableColumnModel columnModel = new DefaultTableColumnModel(columns);


        final IndexedViewSelectionModel viewSelectionModel = this.controller.getIndexedViewSelectionModel();
        if (viewSelectionModel != null) {
            this.viewSelectionView = new ViewSelectionViewButtons(viewSelectionModel);
            setTopComponent(this.viewSelectionView.getToolbar());
        }

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config());
        setBottomComponent(pagingWidgets.getToolbar());
        this.controller.getPagingFeature().setPagingWidgets(pagingWidgets);

        this.stw = SnippetTableWidget.create(columnModel, "mm-snippetTable mm-WidthAuto"); // $NON-NLS-0$
        add(this.stw);
    }

    public void show(TableDataModel dtm) {
        this.stw.updateData(dtm);
        this.controller.getContentContainer().setContent(this);
    }

    public void updateViewNames() {
        if (this.viewSelectionView != null) {
            this.viewSelectionView.updateButtons();
        }
    }
}