/*
* WatchlistView.java
*
* Created on 11.08.2008 10:41:32
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.extjs.gxt.ui.client.widget.WidgetComponent;

import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.WatchlistElement;
import de.marketmaker.iview.mmgwt.mmweb.client.ColumnConfigurator;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.ChartIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.Desktop;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PositionNoteController.PositionNoteRenderer.CommentViewMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author Michael Lösch
 */
class WatchlistView extends AbstractWatchlistView {

    static final ViewMode VIEW_TABLE = new ViewMode(I18n.I.table(), "watchlist-viewMode-table-icon", "watchlist.pdf");  // $NON-NLS$

    static final ViewMode VIEW_CHARTS = new ViewMode(I18n.I.charts(), "watchlist-viewMode-gallery-icon", "chartlist.pdf");  // $NON-NLS$

    private final WatchlistController controller;

    private final Optional<Menu> menuWatchlists;

    private SnippetTableWidget stw;

    private final Optional<SelectButton> tbtnWatchlists;

    private final ImageButton btnCreateWatchlist;

    private final ImageButton btnDeleteWatchlist;

    private final Desktop<QuoteWithInstrument> desktop;

    private ViewMode viewMode = VIEW_TABLE;

    private WidgetComponent view0;

    private WidgetComponent view1;

    private final ImageButton collapseComments;

    public WatchlistView(final WatchlistController controller, boolean withoutToolbar) {
        super();
        this.controller = controller;
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        setLayout(this.cardLayout);

        //ToolBar ///////////////////////////////////////////////////////////////////
        final FloatingToolbar toolbar = new FloatingToolbar();

        if (SessionData.isAsDesign()) {
            this.menuWatchlists = Optional.empty();
            this.tbtnWatchlists = Optional.empty();
        }
        else {
            toolbar.addLabel(I18n.I.watchlist() + ":"); // $NON-NLS$

            final Menu watchlistMenu = new Menu();
            final SelectButton watchlistSelectButton = createWatchlistSelectButton(watchlistMenu);
            toolbar.add(watchlistSelectButton);
            this.menuWatchlists = Optional.of(watchlistMenu);
            this.tbtnWatchlists = Optional.of(watchlistSelectButton);
        }

        this.btnCreateWatchlist = GuiUtil.createImageButton("watchlist-new-icon", null, null, I18n.I.createNewWatchlist());  // $NON-NLS-0$
        this.btnCreateWatchlist.addClickHandler(event -> createWatchlist());
        toolbar.add(this.btnCreateWatchlist);

        ImageButton btnEditWatchlist = GuiUtil.createImageButton("watchlist-edit-icon", null, null, I18n.I.editCurrentWatchlist()); // $NON-NLS$
        btnEditWatchlist.addClickHandler(event -> updateWatchlist());
        toolbar.add(btnEditWatchlist);

        this.btnDeleteWatchlist = GuiUtil.createImageButton("watchlist-delete-icon", null, null, I18n.I.deleteCurrentWatchlist());  // $NON-NLS-0$
        this.btnDeleteWatchlist.addClickHandler(event ->
                Dialog.confirm(I18n.I.shouldDeleteWatchlist(), controller::deleteWatchlist));

        toolbar.add(this.btnDeleteWatchlist);

        toolbar.addEmpty();
        toolbar.addSeparator();
        toolbar.addEmpty();

        final ImageButton btnConfig = GuiUtil.createImageButton("position-add-icon", null, null, I18n.I.addPositionToWatchlist());  // $NON-NLS-0$
        btnConfig.addClickHandler(event -> controller.showConfigView());
        toolbar.add(btnConfig);

        toolbar.addEmpty();

        addViewMenuTo(toolbar, VIEW_TABLE, VIEW_CHARTS);

        this.collapseComments = GuiUtil.createImageButton("portfolio-comment-expand", null, "portfolio-comment-collapse", I18n.I.expandComments()); // $NON-NLS$
        this.collapseComments.addClickHandler(event -> {
            controller.toggleCommentViewMode();
            updateToggleCommentsBtn();
        });

        addColumnConfigButton(toolbar);
        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
            toolbar.add(this.collapseComments);
        }
        toolbar.addEmpty();
        addExportButton(toolbar);

        if (!withoutToolbar) {
            setTopComponent(toolbar);
        }

        /////////////////////////////////////////////////////////////////////////////
        //Table /////////////////////////////////////////////////////////////////////
        this.stw = createTable();
        /////////////////////////////////////////////////////////////////////////////

        this.desktop = new Desktop<>(Desktop.Mode.FLOW);

        this.view1 = new WidgetComponent(this.desktop);

        add(this.view1);
        this.cardLayout.setActiveItem(this.view0);
    }

    private SelectButton createWatchlistSelectButton(Menu watchlistMenu) {
        final SelectButton watchlistSelectButton = new SelectButton().withMenu(watchlistMenu, true);
        watchlistSelectButton.setClickOpensMenu(true);
        watchlistSelectButton.addSelectionHandler(event -> {
            final MenuItem selectedItem = event.getSelectedItem();
            if (selectedItem != null) {
                PlaceUtil.goTo("B_W/" + selectedItem.getData("id")); // $NON-NLS$
            }
        });
        return watchlistSelectButton;
    }

    private SnippetTableWidget createTable() {
        final TableColumnModel columnModel = controller.getColumnModel();
        if (this.view0 != null) {
            remove(this.view0);
        }
        final SnippetTableWidget result = SnippetTableWidget.create(columnModel, "mm-listSnippetTable"); // $NON-NLS-0$
        result.setSortLinkListener(this.controller.getSortLinkListener());
        this.view0 = new WidgetComponent(result);
        insert(this.view0, 0);
        return result;
    }


    protected void onViewChange(ViewMode mode) {
        this.viewMode = mode;
        this.controller.refresh();
        this.btnPeriod.setVisible(mode == VIEW_CHARTS);
        if (this.btnExport != null) {
            this.btnExport.setVisible(mode == VIEW_TABLE);
        }
        if (this.btnConf != null) {
            this.btnConf.setVisible(mode == VIEW_TABLE);
        }

        updateToggleCommentsBtn();
    }

    private void updateToggleCommentsBtn() {
        final CommentViewMode controllerViewMode = this.controller.getCommentViewMode();
        this.collapseComments.setActive(controllerViewMode != CommentViewMode.SHORT);
        Tooltip.addQtip(this.collapseComments, controllerViewMode == CommentViewMode.SHORT ? I18n.I.expandComments() : I18n.I.collapseComments());
        this.collapseComments.setVisible(this.viewMode == VIEW_TABLE && this.controller.getColumnModel().configContainsColumn("US"));  // $NON-NLS$
    }

    public ViewMode getViewMode() {
        return this.viewMode;
    }

    protected void setPeriod(String period) {
        this.controller.setChartPeriod(period);
    }

    private void createWatchlist() {
        Dialog.prompt(I18n.I.createNewWatchlist(), I18n.I.name() + " ", null,
                new Dialog.PromptCallback() {
                    @Override
                    public void execute(String string) {
                        controller.createWatchlist(string);
                    }
                });
    }

    private void updateWatchlist() {
        Dialog.prompt(I18n.I.renameWatchlist(), I18n.I.name() + " ",
                controller.getCurrentWatchlistData().getWatchlistName(),
                new Dialog.PromptCallback() {
                    @Override
                    public void execute(String string) {
                        controller.updateWatchlistName(string);
                    }
                });
    }

    public void buildWatchlistMenu(String watchlistid) {
        this.menuWatchlists.ifPresent(menu -> {
            menu.removeAll();

            final List<WatchlistElement> watchlists = SessionData.INSTANCE.getWatchlists();

            for (WatchlistElement watchlist : watchlists) {
                final String name = watchlist.getName();
                final String id = watchlist.getWatchlistid();
                menu.add(new MenuItem(name).withData("id", id)); // $NON-NLS$
            }

            setSelectedWatchlist(watchlistid);
        });
    }

    public void update(TableDataModel dtm) {
        this.stw = createTable();
        setMode(WatchlistView.VIEW_TABLE);
        this.stw.updateData(dtm);
        this.cardLayout.setActiveItem(this.view0);
    }

    public void update(List<ChartIcon> list) {
        setMode(WatchlistView.VIEW_CHARTS);
        this.desktop.clear();
        for (ChartIcon icon : list) {
            this.desktop.add(icon);
        }
        this.cardLayout.setActiveItem(this.view1);
    }

    public void setSelectedWatchlist(String watchlistid) {
        this.tbtnWatchlists.ifPresent(button -> button.setSelectedData("id", watchlistid, false)); // $NON-NLS$
    }

    public void enableAddBtn(boolean value) {
        this.btnCreateWatchlist.setEnabled(value);
    }

    public void enableDelBtn(boolean value) {
        this.btnDeleteWatchlist.setEnabled(value);
    }

    public String getPrintHtml() {
        return this.stw.getElement().getInnerHTML();
    }

    @Override
    protected String getActionToken(ExportType exportType) {
        switch (exportType) {
            case CSV:
                return "X_WL_CSV"; // $NON-NLS$
            case XLS:
                return "X_WL_XLS"; // $NON-NLS$
            case WEB_QUERY:
                return "X_WL_WQ"; // $NON-NLS$
            default:
                throw new IllegalArgumentException("unhandled ExportType: " + exportType); // $NON-NLS$
        }
    }

    @Override
    protected String getUrl(ExportType exportType) {
        switch (exportType) {
            case CSV:
                return this.controller.getCsvUrl();
            case XLS:
                return this.controller.getXlsUrl();
            case WEB_QUERY:
                return this.controller.getWebQueryUrl();
            default:
                throw new IllegalArgumentException("unhandled ExportType: " + exportType); // $NON-NLS$
        }
    }

    @Override
    protected void configureColumns() {
        ColumnConfigurator.show(this.controller.getColumnModel(), () -> {
            updateToggleCommentsBtn();
            controller.updateView();
        });
    }

    public ArrayList<PushRenderItem> getRenderItems(TableDataModel currentDtm) {
        if (this.viewMode == VIEW_TABLE) {
            return this.stw.getRenderItems(currentDtm);
        }
        return null;
    }
}
