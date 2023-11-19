/*
* PortfolioView.java
*
* Created on 24.09.2008 13:57:50
*
* Copyright (c) vwd GmbH. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.WidgetComponent;
import com.extjs.gxt.ui.client.widget.form.LabelField;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.PortfolioWerte;
import de.marketmaker.iview.mmgwt.mmweb.client.ColumnConfigurator;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.ChartIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.desktop.Desktop;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DecimalBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.InfoIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NumberUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PortfolioUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.PositionNoteController.PositionNoteRenderer.CommentViewMode;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TextBox;

import static com.google.gwt.dom.client.Style.Unit.PCT;

/**
 * @author Michael Lösch
 */
class PortfolioView extends AbstractWatchlistView {
    private final HTML ttiFooter;

    private final Menu menuGroupings;

    private List<PortfolioElement> portfoliosInMenu;

    static final ViewMode VIEW_TABLE = new ViewMode(I18n.I.table(), "portfolio-viewMode-table-icon", "portfolio.pdf");  // $NON-NLS$

    static final ViewMode VIEW_CHARTS = new ViewMode(I18n.I.charts(), "portfolio-viewMode-gallery-icon", "chartlist.pdf");  // $NON-NLS$

    static final ViewMode VIEW_ORDERS = new ViewMode(I18n.I.transactions(), "portfolio-viewMode-orders-icon", "portfolio.pdf");  // $NON-NLS$

    static final ViewMode VIEW_VISUAL = new ViewMode(I18n.I.visualisation(), "portfolio-viewMode-orders-icon", "portfolio-visualization.pdf");  // $NON-NLS$

    private final PortfolioController controller;

    private final Optional<Menu> menuPortfolios;

    private SnippetTableWidget stwPortfolio;

    private final SnippetTableWidget stwOrders;

    private final Optional<SelectButton> tbtnPortfolios;

    private ImageButton btnCreatePortfolio;

    private ImageButton btnDeletePortfolio;

    private final Desktop<QuoteWithInstrument> desktop;

    private ContentPanel pvView;

    private WidgetComponent view0;

    private WidgetComponent view1;

    private WidgetComponent view2;

    private final List<String> currencies;

    private final Label groupByLabel;

    private final SelectButton groupBy;

    private final ImageButton collapseComments;

    private final java.util.Map<String, String> groupingNames;

    public PortfolioView(final PortfolioController controller, final ContentPanel pvView,
            boolean readOnly) {
        this.controller = controller;
        this.pvView = pvView;
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
        setLayout(this.cardLayout);

        this.currencies = new ArrayList<>(GuiDefsLoader.getPortfolioCurrencies());

        //ToolBar ///////////////////////////////////////////////////////////////////
        final FloatingToolbar toolbar = new FloatingToolbar();

        if(SessionData.isAsDesign()) {
            this.menuPortfolios = Optional.empty();
            this.tbtnPortfolios = Optional.empty();
        }
        else {
            toolbar.addLabel(I18n.I.portfolio() + ":");  // $NON-NLS$

            final Menu portfolioMenu = new Menu();
            final SelectButton portfolioSelectButton = new SelectButton()
                    .withMenu(portfolioMenu)
                    .withClickOpensMenu()
                    .withSelectionHandler(e -> {
                        final MenuItem selectedItem = e.getSelectedItem();
                        if (selectedItem != null) {
                            PlaceUtil.goTo(controller.getControllerToken() + "/" + selectedItem.getData("id")); // $NON-NLS$
                        }
                    });
            toolbar.add(portfolioSelectButton);
            this.menuPortfolios = Optional.of(portfolioMenu);
            this.tbtnPortfolios = Optional.of(portfolioSelectButton);
        }

        if (!readOnly) {
            addUpdateControls(toolbar);
        }
        toolbar.addEmpty();

        addViewMenuTo(toolbar, VIEW_TABLE, VIEW_CHARTS, VIEW_ORDERS, VIEW_VISUAL);

        setTopComponent(toolbar);

        final FloatingToolbar footerToolbar = new FloatingToolbar();
        footerToolbar.addStyleName("mm-portfolio-footer"); // $NON-NLS-0$
        this.ttiFooter = new HTML();
        this.ttiFooter.setStyleName("mm-toolbar-text");
        footerToolbar.add(this.ttiFooter);

        setBottomComponent(footerToolbar);

        /////////////////////////////////////////////////////////////////////////////
        //Table /////////////////////////////////////////////////////////////////////
        this.stwPortfolio = createStwPortfolio();
        final TableColumnModel columnModelOrders = this.controller.createOrdersTableColumnModel();
        this.stwOrders = SnippetTableWidget.create(columnModelOrders, "mm-listSnippetTable"); // $NON-NLS-0$
        /////////////////////////////////////////////////////////////////////////////

        this.desktop = new Desktop<>(Desktop.Mode.FLOW);

        this.view1 = new WidgetComponent(this.stwOrders);
        this.view2 = new WidgetComponent(this.desktop);

        add(this.view1);
        add(this.view2);
        add(this.pvView);

        add(new LabelField());

        this.groupByLabel = new Label(I18n.I.groupBy() + ":");
        this.groupByLabel.setStyleName("mm-toolbar-text");

        this.menuGroupings = new Menu();
        this.groupBy = new SelectButton()
                .withMenu(this.menuGroupings)
                .withClickOpensMenu()
                .withSelectionHandler(e -> {
                    final MenuItem selectedItem = e.getSelectedItem();
                    if (selectedItem != null) {
                        controller.groupBy((String) selectedItem.getData("value")); // $NON-NLS$
                    }
                });

        this.groupingNames = new HashMap<>();
        this.groupingNames.put("MARKET", I18n.I.marketName());   //$NON-NLS$
        this.groupingNames.put("INSTRUMENT", I18n.I.instrument());   //$NON-NLS$

        this.collapseComments = GuiUtil.createImageButton("portfolio-comment-expand", null, "portfolio-comment-collapse", I18n.I.expandComments()); // $NON-NLS$
        this.collapseComments.addClickHandler(event -> {
            controller.toggleCommentViewMode();
            updateToggleCommentsBtn();
        });

        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() || FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled()) {
            toolbar.addEmpty();
            toolbar.add(this.groupByLabel);
            toolbar.add(this.groupBy);
        }

        addColumnConfigButton(toolbar);
        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()) {
            toolbar.add(this.collapseComments);
        }
        toolbar.addEmpty();
        addExportButton(toolbar);
    }

    protected void onViewChange(ViewMode mode) {
        this.controller.switchView(mode);
        this.btnPeriod.setVisible(mode == VIEW_CHARTS);
        if (this.btnExport != null) {
            this.btnExport.setVisible(mode == VIEW_TABLE);
        }
        if (this.btnConf != null) {
            this.btnConf.setVisible(mode == VIEW_TABLE);
        }

        final boolean tableOrChartView = (mode == VIEW_TABLE || mode == VIEW_CHARTS);
        this.groupByLabel.setVisible(tableOrChartView);
        this.groupBy.setVisible(tableOrChartView);
        updateToggleCommentsBtn();
    }

    private void addUpdateControls(FloatingToolbar toolbar) {
        this.btnCreatePortfolio = GuiUtil.createImageButton("portfolio-new-icon", null, null, I18n.I.createNewPortfolio());  // $NON-NLS-0$
        this.btnCreatePortfolio.addClickHandler(event -> showPortfolioEditPopup(null, null));
        toolbar.add(this.btnCreatePortfolio);

        final ImageButton btnEditPortfolio = GuiUtil.createImageButton("portfolio-edit-icon", null, null, I18n.I.editCurrentPortfolio()); // $NON-NLS$
        btnEditPortfolio.addClickHandler(event -> {
            final PortfolioWerte portfolio = this.controller.getCurrentPortfolio();
            if (portfolio != null) {
                showPortfolioEditPopup(portfolio.getName(), portfolio.getCash());
            }
            else {
                Firebug.log("btnEditPortfolio.onClick !pfBlock.isResponseOk()"); // $NON-NLS-0$
            }
        });
        toolbar.add(btnEditPortfolio);

        this.btnDeletePortfolio = GuiUtil.createImageButton("portfolio-delete-icon", null, null, I18n.I.deleteCurrentPortfolio());  // $NON-NLS-0$
        this.btnDeletePortfolio.addClickHandler(event -> Dialog.confirm(I18n.I.shouldDeletePortfolio(), this.controller::deletePortfolio));

        toolbar.add(this.btnDeletePortfolio);

        toolbar.addEmpty();
        toolbar.addSeparator();
        toolbar.addEmpty();

        final ImageButton btnConfig = GuiUtil.createImageButton("order-add-icon", null, null, I18n.I.addOrderToPortfolio());  // $NON-NLS-0$
        btnConfig.addClickHandler(event -> this.controller.createBuyOrder(this.controller.getCurrentPortfolioId(), null));
        toolbar.add(btnConfig);
    }

    public void updateCharts(List<ChartIcon> list) {
        this.desktop.clear();
        for (ChartIcon icon : list) {
            this.desktop.add(icon);
        }
        this.cardLayout.setActiveItem(this.view2);
    }

    public void buildPortfolioMenu(List<PortfolioElement> portfolios) {
        this.menuPortfolios.ifPresent(menu -> {
            if (!PortfolioUtil.portfoliosDiffer(this.portfoliosInMenu, portfolios)) {
                return;
            }
            this.portfoliosInMenu = portfolios;

            menu.removeAll(false);

            for (PortfolioElement portfolio : portfolios) {
                final String name = portfolio.getName();
                final String id = portfolio.getPortfolioid();

                menu.add(new MenuItem(name).withData("id", id)); // $NON-NLS$
            }
            setSelectedPortfolio(this.controller.getCurrentPortfolioId());
        });
    }

    public void buildGroupingMenu(List<String> fields) {
        this.menuGroupings.removeAll(false);
        for (final String field : fields) {
            this.menuGroupings.add(
                    new MenuItem(this.groupingNames.get(field))
                            .withData("value", field) // $NON-NLS$
            );
        }
    }

    private void setFooter(String initialInvestment, String cash, String realizedGain,
            String portfolioCurrency) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        // investment
        sb.appendEscaped(I18n.I.investment())
                .appendHtmlConstant(": ") // $NON-NLS$
                .appendEscaped(Renderer.LARGE_PRICE_MAX2.render(initialInvestment))
                .appendHtmlConstant(" ") // $NON-NLS$
                .appendEscaped(portfolioCurrency);
        // cash
        if (config.getBooleanProperty(AppConfig.SHOW_CASH_IN_PORTFOLIO, true)) {
            sb.appendHtmlConstant("&nbsp;&nbsp;&nbsp;") // $NON-NLS$
                    .appendEscaped(I18n.I.moneyAsCash())
                    .appendHtmlConstant(": ") // $NON-NLS$
                    .appendEscaped(Renderer.LARGE_PRICE_MAX2.render(cash))
                    .appendHtmlConstant(" ") // $NON-NLS$
                    .appendEscaped(portfolioCurrency);
        }
        // profit
        sb.appendHtmlConstant("&nbsp;&nbsp;&nbsp;") // $NON-NLS$
                .appendEscaped(I18n.I.realizedProfit())
                .appendHtmlConstant(": ") // $NON-NLS$
                .appendEscaped(Renderer.LARGE_PRICE_MAX2.render(realizedGain))
                .appendHtmlConstant(" ") // $NON-NLS$
                .appendEscaped(portfolioCurrency);
        this.ttiFooter.setHTML(sb.toSafeHtml());
    }

    private SnippetTableWidget createStwPortfolio() {
        final SnippetTableWidget stw = SnippetTableWidget.create(this.controller.getPortfoliosTableColumnModel(), "mm-listSnippetTable"); // $NON-NLS-0$
        if (this.view0 != null) {
            remove(this.view0);
        }
        this.view0 = new WidgetComponent(stw);
        insert(this.view0, 0);
        return stw;
    }

    public void updatePortfolio(TableDataModel dtmPortfolio, String initialInvestment,
            String cash, String realizedGain, String portfolioCurrency) {
        this.stwPortfolio = createStwPortfolio();
        setFooter(initialInvestment, cash, realizedGain, portfolioCurrency);
        this.stwPortfolio.updateData(dtmPortfolio);
        this.cardLayout.setActiveItem(this.view0);
        updateToggleCommentsBtn();
    }

    private void updateToggleCommentsBtn() {
        this.collapseComments.setActive(this.controller.getCommentViewMode() != CommentViewMode.SHORT);
        Tooltip.addQtip(this.collapseComments, this.controller.getCommentViewMode() == CommentViewMode.SHORT ? I18n.I.expandComments() : I18n.I.collapseComments());
        this.collapseComments.setVisible(this.controller.getViewMode() == VIEW_TABLE && controller.getPortfoliosTableColumnModel().configContainsColumn("US"));  // $NON-NLS$
    }

    public void updateOrders(TableDataModel dtmOrders, String initialInvestment, String cash,
            String realizedGain,
            String portfolioCurrency) {
        setFooter(initialInvestment, cash, realizedGain, portfolioCurrency);
        this.stwOrders.updateData(dtmOrders);
        this.cardLayout.setActiveItem(this.view1);
    }

    public void showVisualizationView() {
        this.cardLayout.setActiveItem(this.pvView);
    }

    public void setSelectedPortfolio(String portfolioid) {
        this.tbtnPortfolios.ifPresent(button -> button.setSelectedData("id", portfolioid, false)); // $NON-NLS$
    }

    public void setBtnGrouping(String grouping, boolean fireEvent) {
        this.groupBy.setSelectedData("value", grouping, fireEvent); // $NON-NLS$
    }

    public void enableAddBtn(boolean value) {
        if (this.btnCreatePortfolio != null) {
            this.btnCreatePortfolio.setEnabled(value);
        }
    }

    public void enableDelBtn(boolean value) {
        if (this.btnDeletePortfolio != null) {
            this.btnDeletePortfolio.setEnabled(value);
        }
    }

    private void showPortfolioEditPopup(String name, String liquidity) {
        final DialogIfc dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.portfolio())
                .withCloseButton();

        final String labelTextLiquidity;
        final SelectButton btnCurrency;

        final FlexTable table = new FlexTable();
        Styles.tryAddStyles(table, Styles.get().generalFormStyle());
        if (!SessionData.isAsDesign()) {
            table.setCellSpacing(4);
        }


        final TextBox tbPortfolioName = new TextBox();
        tbPortfolioName.setValue(name);

        final InfoIcon liquidityInfoIcon = new InfoIcon();
        final DecimalBox tbLiquidity = SessionData.isAsDesign()
                ? new DecimalBox(false, liquidityInfoIcon.getMessagePopup())
                : new DecimalBox(); //no info icon in legacy mode
        liquidityInfoIcon.setEditWidget(tbLiquidity);

        final boolean newPortfolio = name == null;
        if (newPortfolio) {
            if (this.currencies.size() > 1) {
                final Menu menuCurrency = new Menu();
                for (final String currency : this.currencies) {
                    menuCurrency.add(new MenuItem(currency).withData("value", currency)); // $NON-NLS$
                }
                btnCurrency = new SelectButton()
                        .withMenu(menuCurrency)
                        .withClickOpensMenu();
            }
            else {
                btnCurrency = null;
            }

            labelTextLiquidity = I18n.I.initialInvestmentSum();
            dialog.withDefaultButton(I18n.I.save(), () -> {
                final String currency = btnCurrency == null
                        ? (this.currencies.isEmpty() ? "" : this.currencies.get(0))
                        : (String) btnCurrency.getSelectedItem().getData("value"); // $NON-NLS$
                this.controller.createPortfolio(
                        tbPortfolioName.getValue(),
                        NumberUtil.toPlainStringValue(tbLiquidity.getValue()),
                        currency);
            });
        }
        else {
            btnCurrency = null;
            labelTextLiquidity = I18n.I.cash();
            if (StringUtil.hasText(liquidity)) {
                try {
                    tbLiquidity.setValue(new BigDecimal(liquidity).setScale(2, BigDecimal.ROUND_HALF_UP));
                } catch (Exception e) {
                    Firebug.error("<PortfolioView.showPortfolioEditPopup>", e);
                    tbLiquidity.setValue(null);
                }
            }
            else {
                tbLiquidity.setValue(null);
            }

            dialog.withDefaultButton(I18n.I.save(), () -> {
                final String portfolioNameStr = tbPortfolioName.getValue();
                final String liquidityStr = NumberUtil.toPlainStringValue(tbLiquidity.getValue());
                this.controller.updatePortfolio(portfolioNameStr, liquidityStr);
            });
        }

        int row = 0;
        table.setWidget(row, 0, new Caption(I18n.I.name()));
        table.setWidget(row, 1, tbPortfolioName);
        table.getFlexCellFormatter().getElement(row, 0).getStyle().setWidth(50, PCT);
        if (showCashInPortfolio()) {
            table.setWidget(++row, 0, SessionData.isAsDesign()
                    ? new Caption(labelTextLiquidity).withInfoIcon(liquidityInfoIcon)
                    : new Caption(labelTextLiquidity));
            table.setWidget(row, 1, tbLiquidity);
        }
        if (btnCurrency != null) {
            table.setWidget(++row, 0, new Caption(I18n.I.currency()));
            table.setWidget(row, 1, btnCurrency);
        }

        final SimplePanel view = new SimplePanel();
        Styles.tryAddStyles(view, Styles.get().generalViewStyle());
        view.add(table);
        dialog.withWidget(view).withFocusWidget(tbPortfolioName).show();
    }

    private boolean showCashInPortfolio() {
        final User user = SessionData.INSTANCE.getUser();
        return user.getAppConfig().getBooleanProperty(AppConfig.SHOW_CASH_IN_PORTFOLIO, true);
    }

    protected void setPeriod(String period) {
        this.controller.setChartPeriod(period);
    }


    @Override
    protected String getActionToken(ExportType exportType) {
        switch (exportType) {
            case CSV:
                return "X_PF_CSV"; // $NON-NLS$
            case XLS:
                return "X_PF_XLS"; // $NON-NLS$
            case WEB_QUERY:
                return null;
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
                return null;
            default:
                throw new IllegalArgumentException("unhandled ExportType: " + exportType); // $NON-NLS$
        }
    }

    @Override
    protected void configureColumns() {
        ColumnConfigurator.show(this.controller.getPortfoliosTableColumnModel(), () -> {
            updateToggleCommentsBtn();
            controller.selectPortfolio(controller.getCurrentPortfolioId());
        });
    }

    public ArrayList<PushRenderItem> getRenderItems(TableDataModel portfolioTdm) {
        return this.stwPortfolio.getRenderItems(portfolioTdm);
    }
}
