/*
 * PriceTeaserSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.LoadEvent;
import com.google.gwt.event.dom.client.LoadHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuButton;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.iview.dmxml.Alert;
import de.marketmaker.iview.dmxml.EDGData;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.AlertController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiMenu;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.DzPibDownloadLinkRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.PriceWithSupplement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderResearch;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.ObjectWidgetFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.util.AlertUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChangeRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ContentFlagUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.CurrencyRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.watchlist.WatchlistController;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.DzPibMarginDialog;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.HighLowPriceGraph;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SecretActivationLabel;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TinyEdgBadge;

import java.util.List;

import static com.google.gwt.dom.client.Style.Clear.BOTH;
import static com.google.gwt.dom.client.Style.Float.LEFT;
import static com.google.gwt.dom.client.Style.Float.RIGHT;
import static com.google.gwt.dom.client.Style.Unit.EM;
import static com.google.gwt.dom.client.Style.Unit.PX;
import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.WGZ_BANK_USER;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceTeaserSnippetView<S extends Snippet<S>> extends SnippetView<S> implements
        IsWidget {
    private final Widget viewWidget;

    private final HighLowPriceGraph hlpGraph;

    private QuoteWithInstrument qwi;

    private Menu menuMarkets;

    private MenuButton buttonMarkets = null;

    private Label labelMarket = null;

    private Button buttonAddLimit = null;

    private final Image diffImage;

    private final Image qualityImage;

    private Image edgImage;

    private QuoteData currentQuote = null;

    private InstrumentData currentInstrument;

    private String currentPrice;

    private static PriceTeaserSnippetView current = null;

    private static final String BASE_STYLE = "pt"; // $NON-NLS-0$

    private final FlowPanel panelName = new FlowPanel();

    private final HTML labelPrice = new HTML("? EUR"); // $NON-NLS$

    private final Label labelTime = new Label(I18n.I.time());

    private final HTML labelDiff = new HTML("+0%"); // $NON-NLS$

    private final SecretActivationLabel labelVwdcode = new SecretActivationLabel("vwdcode"); // $NON-NLS$

    private final Label labelAsInstrumentType = new Label();

    private final Label labelAsMarket = new InlineLabel();

    private final Label labelAsName = new Label();

    private final boolean marketSelectionByVwdcode;

    private TinyEdgBadge tinyEdgWidget;

    private final SimplePanel pibImageWrap;

    private final SimplePanel researchImageWrap;

    public PriceTeaserSnippetView(S snippet, boolean withLimits,
            boolean withPin, boolean withSimpleWatchlist, boolean withEdg) {
        super(snippet);

        final boolean objectInfo = getConfiguration().getBoolean("isObjectInfo", false); // $NON-NLS$

        this.marketSelectionByVwdcode
                = getConfiguration().getBoolean("marketSelectionByVwdcode", false); // $NON-NLS$

        this.labelAsInstrumentType.setStyleName(ObjectWidgetFactory.STYLE_TYPE);
        this.labelAsName.setStyleName(ObjectWidgetFactory.STYLE_HEAD);
        Tooltip.addAutoCompletion(this.labelAsName).withStyle(ObjectWidgetFactory.STYLE_HEAD_TOOLTIP);

        this.labelAsMarket.setStyleName("as-objectWidget-market"); // $NON-NLS$
        Tooltip.addAutoCompletion(this.labelAsMarket).withStyle("as-objectWidget-market-tooltip"); // $NON-NLS$

        this.panelName.setStyleName(BASE_STYLE + " mm-name");
        this.labelPrice.setStyleName(BASE_STYLE + " mm-price");
        this.labelDiff.setStyleName(BASE_STYLE + " mm-diff");

        this.qualityImage = new Image();
        this.diffImage = new Image();
        this.hlpGraph = new HighLowPriceGraph(objectInfo ? "52w-span-180" : "52w-span", objectInfo); // $NON-NLS$

        final Widget marketWidget;
        if (getConfiguration().getBoolean("marketSelection", true)) { // $NON-NLS-0$
            this.menuMarkets = new Menu();
            this.buttonMarkets = new MenuButton();
            this.buttonMarkets.withMenu(this.menuMarkets);
            this.buttonMarkets.addStyleName("mm-frameless"); // $NON-NLS-0$
            this.buttonMarkets.setClickOpensMenu(true);
            marketWidget = this.buttonMarkets;
        }
        else {
            this.labelMarket = new Label();
            this.labelMarket.setStyleName("mm-right"); // $NON-NLS-0$
            marketWidget = this.labelMarket;
        }

        final IconImageIcon quoteLink;
        if (withPin) {
            // pin
            quoteLink = IconImage.getIcon("mm-contextTrigger-16"); // $NON-NLS$
            quoteLink.setStyleName("mm-contextTrigger-16"); // $NON-NLS$
            quoteLink.addStyleName("mm-print-invisible"); // $NON-NLS$
            quoteLink.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent clickEvent) {
                    QwiMenu.INSTANCE.show(qwi, quoteLink.getElement());
                }
            });
        }
        else {
            quoteLink = null;
        }

        final Button addToWatchlist;
        if (withSimpleWatchlist) {
            addToWatchlist = Button.text(I18n.I.addToWatchlist())
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            WatchlistController.INSTANCE.createPosition(qwi, null);
                        }
                    })
                    .build();
        }
        else {
            addToWatchlist = null;
        }

        if (withLimits) {
            // limit create/edit
            this.buttonAddLimit = Button.icon("mm-limits-icon") // $NON-NLS$
                    .tooltip(I18n.I.addLimit())
                    .clickHandler(new ClickHandler() {
                        @Override
                        public void onClick(ClickEvent clickEvent) {
                            AlertController.INSTANCE.edit(createAlert());
                        }
                    })
                    .build();
            this.buttonAddLimit.addStyleName("mm-frameless"); // $NON-NLS$
            this.buttonAddLimit.addStyleName("mm-print-invisible"); // $NON-NLS$
        }

        if (withEdg) {
            this.edgImage = new Image();
            this.edgImage.setStyleName("mm-edg-siegel"); // $NON-NLS-0$
            this.edgImage.addLoadHandler(new LoadHandler() {
                public void onLoad(LoadEvent loadEvent) {
                    edgImage.setVisible(true);
                }
            });
            this.edgImage.setVisible(false);

            if (SessionData.isAsDesign()) {
                this.tinyEdgWidget = new TinyEdgBadge();
                this.tinyEdgWidget.setVisible(false);
            }
        }

        this.labelVwdcode.setSecretCommand(new Command() {
            public void execute() {
                if (qwi != null) {
                    QwiMenu.showInstrumentAndQuoteData(qwi);
                }
            }
        });


        if (objectInfo) {
            this.pibImageWrap = new SimplePanel();
            this.researchImageWrap = new SimplePanel();
            this.viewWidget = initWidgetForObjectInfo(marketWidget, quoteLink, addToWatchlist);
        }
        else {
            this.pibImageWrap = null;
            this.researchImageWrap = null;
            this.viewWidget = initTableForSnippet(marketWidget, quoteLink, addToWatchlist);
        }
    }

    private Widget initWidgetForObjectInfo(Widget marketWidget, Widget quoteLink,
            Button addToWatchlist) {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("as-objectWidget");

        if (quoteLink != null) {
            quoteLink.getElement().getStyle().setFloat(RIGHT);
            panel.add(quoteLink);
        }

        final Image objectIcon = IconImage.get("pm-instrument-32").createImage(); // $NON-NLS$
        objectIcon.addStyleName("as-objectIcon");
        panel.add(objectIcon);
        panel.add(this.labelAsInstrumentType);
        panel.add(this.labelAsName);

        final Label clearLabel = new Label();
        clearLabel.getElement().getStyle().setClear(BOTH);
        clearLabel.getElement().getStyle().setHeight(0.5, EM);
        panel.add(clearLabel);

        //market
        this.labelAsMarket.getElement().getStyle().setFloat(LEFT);
        panel.add(this.labelAsMarket);

        // price
        this.labelPrice.getElement().getStyle().setFloat(RIGHT);
        panel.add(this.labelPrice);

        // time
        final Style styleTime = this.labelTime.getElement().getStyle();
        styleTime.setClear(BOTH);
        styleTime.setFloat(RIGHT);
        styleTime.setMarginLeft(6, PX);
        panel.add(this.labelTime);

        // quality icon
        final SimplePanel qualityImageWrap = new SimplePanel(this.qualityImage);
        qualityImageWrap.getElement().getStyle().setFloat(RIGHT);
        panel.add(qualityImageWrap);

        // research icon
        this.researchImageWrap.getElement().getStyle().setFloat(RIGHT);
        this.researchImageWrap.getElement().getStyle().setMarginTop(2, PX);
        this.researchImageWrap.getElement().getStyle().setMarginRight(5, PX);
        panel.add(this.researchImageWrap);

        // pib icon
        this.pibImageWrap.getElement().getStyle().setFloat(RIGHT);
        this.pibImageWrap.getElement().getStyle().setMarginTop(2, PX);
        this.pibImageWrap.getElement().getStyle().setMarginRight(5, PX);
        panel.add(this.pibImageWrap);

        // vwdcode
        this.labelVwdcode.getElement().getStyle().setFloat(LEFT);
        panel.add(this.labelVwdcode);

        addClearLabel(panel);

        // diff value
        final Style styleDiff = this.labelDiff.getElement().getStyle();
        styleDiff.setFloat(RIGHT);
        styleDiff.setMarginLeft(6, PX);
        panel.add(this.labelDiff);

        // diff icon
        final SimplePanel diffImageWrap = new SimplePanel(this.diffImage);
        diffImageWrap.getElement().getStyle().setFloat(RIGHT);
        panel.add(diffImageWrap);

        if (this.tinyEdgWidget != null) {
            panel.add(this.tinyEdgWidget);
        }

        addClearLabel(panel);

        // high low price graph
        this.hlpGraph.getElement().setAttribute("align", "center"); // $NON-NLS$
        panel.add(this.hlpGraph);


/*
        // markets
        marketWidget.getElement().getStyle().setFloat(Style.Float.RIGHT);
        panel.add(marketWidget);

        this.labelName.getElement().getStyle().setClear(Style.Clear.BOTH);
        panel.add(this.labelName);

        addClearLabel(panel);

        if (addToWatchlist != null) {
            addToWatchlist.getElement().getStyle().setFloat(Style.Float.LEFT);
            panel.add(addToWatchlist);
        }

        if (this.buttonAddLimit != null) {
            this.buttonAddLimit.getElement().getStyle().setFloat(Style.Float.LEFT);
            panel.add(this.buttonAddLimit);
        }
*/


        return panel;
    }

    private Label addClearLabel(FlowPanel panel) {
        final HTML label = new HTML("&nbsp;"); // $NON-NLS$
        label.getElement().getStyle().setClear(BOTH);
        panel.add(label);
        return label;
    }

    private FlexTable initTableForSnippet(Widget marketWidget, Widget quoteLink,
            Button addToWatchlist) {
        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        table.setStyleName("mm-priceTeaser");
        table.setCellPadding(0);
        table.setCellSpacing(0);

        int col0 = 0;
        int col1 = 0;

        // price
        table.setWidget(0, col0, this.labelPrice);
        formatter.setWidth(0, col0, "10%"); // $NON-NLS-0$
        col0++;

        // time
        table.setWidget(1, col1, this.labelTime);
        formatter.setStyleName(1, col1, BASE_STYLE + " mm-date"); // $NON-NLS-0$
        col1++;

        // diff value
        table.setWidget(0, col0, this.labelDiff);
        formatter.setWidth(0, col0, "10%"); // $NON-NLS-0$
        formatter.setStyleName(0, col0, BASE_STYLE + " mm-diff"); // $NON-NLS-0$
        formatter.setColSpan(0, col0, 2);
        col0++;

        // quality icon
        table.setWidget(1, col1, this.qualityImage);
        formatter.setStyleName(1, col1, BASE_STYLE + " mm-qualityIcon"); // $NON-NLS-0$
        col1++;

        // diff icon
        table.setWidget(1, col1, this.diffImage);
        formatter.setStyleName(1, col1, BASE_STYLE + " mm-diffIcon"); // $NON-NLS-0$
        col1++;

        // high low price graph
        table.setWidget(0, col0, this.hlpGraph);
        formatter.setRowSpan(0, col0, 2);
        col0++;

        // name
        table.setWidget(0, col0, this.panelName);
        formatter.setStyleName(0, col0, BASE_STYLE + " mm-name"); // $NON-NLS-0$

        // vwdcode
        table.setWidget(1, col1, this.labelVwdcode);
        formatter.setStyleName(1, col1, BASE_STYLE + " mm-right"); // $NON-NLS$
        formatter.setColSpan(0, col0, 2);
        col1++;
        col0++;

        // markets
        table.setWidget(1, col1, marketWidget);
        formatter.setStyleName(1, col1, BASE_STYLE + " mm-market"); // $NON-NLS-0$
        col1++;

        if (quoteLink != null) {
            table.setWidget(0, col0, quoteLink);
            col0++;
        }

        if (addToWatchlist != null) {
            table.setWidget(0, col0, addToWatchlist);
            col0++;
        }

        if (this.buttonAddLimit != null) {
            table.setWidget(1, col1, this.buttonAddLimit);
            //noinspection UnusedAssignment
            col1++;
        }

        if (this.edgImage != null) {
            table.setWidget(0, col0, this.edgImage);
            table.getFlexCellFormatter().setRowSpan(0, col0, 2);
            //noinspection UnusedAssignment
            col0++;
        }

        return table;
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setHeaderVisible(false);
        this.container.setContentWidget(this.viewWidget);
    }

    public void setTitle(String title) {
        setTitleForNextUpdate(title);
    }

    public void update(final InstrumentData instrumentData, final QuoteData quoteData,
            MSCPriceDatas datas, Price price, IMGResult edg, String pibReportUrl, EDGData edgData) {
        current = this;
        updateTitle();

        this.currentQuote = quoteData;
        this.currentInstrument = instrumentData;

        String name = "";
        String market = "";
        String currency = "";
        String vwdcode = null;
        final String instrumentTypeName;
        final InstrumentTypeEnum instrumentType;

        if (this.currentInstrument != null && this.currentInstrument.getType() != null) {
            StringUtil.reduceCurrencyNameLength(this.currentInstrument);
            name = this.currentInstrument.getName();
            instrumentTypeName = this.currentInstrument.getType();
            instrumentType = InstrumentTypeEnum.valueOf(instrumentTypeName);
        }
        else {
            instrumentTypeName = null;
            instrumentType = null;
        }
        if (quoteData != null) {
            market = getMarket(quoteData);
            vwdcode = quoteData.getVwdcode();
            if (quoteData.getCurrencyIso() != null) {
                currency = " " + CurrencyRenderer.DEFAULT.render(quoteData.getCurrencyIso());
            }
        }

        this.labelAsInstrumentType.setText(InstrumentTypeEnum.getDescription(instrumentTypeName, ""));
        this.labelAsName.setText(name);

        this.panelName.clear();
        if(this.pibImageWrap != null) { // is not null if is object info
            this.pibImageWrap.clear();
        }
        if(this.researchImageWrap != null) {  // is not null if is object info
            this.researchImageWrap.clear();
        }
        if (ContentFlagsEnum.PibDz.isAvailableFor(quoteData)
                && Selector.PRODUCT_WITH_PIB.isAllowed()) {
            final IsWidget pibDownloadWidget = createPibDownloadWidget(instrumentData.getWkn(), pibReportUrl);
            if(this.pibImageWrap != null) { // is not null if is object info
                this.pibImageWrap.setWidget(pibDownloadWidget);
            }
            else {
                this.panelName.add(pibDownloadWidget);
            }
        }
        if (ContentFlagUtil.hasAccessibleResearchDocuments(quoteData)
                && Selector.isDZResearch()
                && (quoteData != null)) {
            final IsWidget research = createTriggerWidget(instrumentData, quoteData);
            if(this.researchImageWrap != null) { // is not null if is object info
                this.researchImageWrap.setWidget(research);
            }
            else {
                this.panelName.add(research);
            }
        }
        this.panelName.add(new InlineLabel(name));

        this.labelVwdcode.setVisible(false);
        this.labelVwdcode.setText("");
        if (vwdcode != null) {
            this.panelName.getElement().setAttribute("mm-vwdcode", vwdcode); // $NON-NLS$
            if ("true".equals(SessionData.INSTANCE.getUserProperty("vwdcode"))) { // $NON-NLS$
                final SecretActivationLabel labelVwdCode = new SecretActivationLabel(vwdcode);
                labelVwdCode.setSecretCommand(new Command() {
                    public void execute() {
                        if (qwi != null) {
                            QwiMenu.showInstrumentAndQuoteData(qwi);
                        }
                    }
                });
                this.labelVwdcode.setText(vwdcode);
                this.labelVwdcode.setVisible(true);
            }
        }
        this.qwi = new QuoteWithInstrument(this.currentInstrument, quoteData);
        if (this.buttonMarkets != null) {
            BrowserSpecific.INSTANCE.setPriceTeaserMarket(this.buttonMarkets, market);
        }
        else {
            this.labelMarket.setText(market);
        }
        this.labelAsMarket.setText(market);

        if (this.edgImage != null) {
            if (edg != null && edg.getRequest() != null) {
                this.edgImage.setUrl(ChartUrlFactory.getUrl(edg.getRequest()));
                if (this.tinyEdgWidget != null) {
                    this.tinyEdgWidget.update(edgData, this.edgImage);
                }
            }
            else {
                this.edgImage.setVisible(false);
                if (this.tinyEdgWidget != null) {
                    this.tinyEdgWidget.setVisible(false);
                }
            }
        }

        this.currentPrice = null;
        if (price == null || price.getType() == PriceDataType.INVALID) {
            this.labelPrice.setText("n/a"); // $NON-NLS$
            this.labelDiff.setText("");
            this.labelTime.setText("");
            updateDiffImage(Renderer.CHANGE_PERCENT, null);
            updateQuality(null, false);
            this.hlpGraph.setData(null, null, null);
        }
        else if (price.getType() == PriceDataType.STANDARD) {
            final String diffRendered = Renderer.CHANGE_PERCENT.render(price.getChangePercent());
            final PriceWithSupplement pws = price.getLastPrice();
            if ((price.getBid() != null || price.getAsk() != null)
                    && (instrumentType == InstrumentTypeEnum.CER || instrumentType == InstrumentTypeEnum.WNT
                    || pws.getPrice() == null)) {
                this.labelPrice.setHTML(SessionData.isAsDesign()
                        ? renderBidAskAs(price, currency)
                        : renderBidAsk(price, currency));
                this.labelPrice.setStyleName(BASE_STYLE + " mm-bidAsk");
                this.labelTime.setText(DateRenderer.dateOrTime(false, "--").render(price.getBidAskDate()));
                this.labelDiff.setText("");
            }
            else {
                this.labelPrice.setHTML(Renderer.PRICE.render(pws.getPrice()) + Renderer.SUPPLEMENT.render(pws.getSupplement()) + currency);
                this.labelPrice.setStyleName(BASE_STYLE + " mm-price");
                this.labelTime.setText(DateRenderer.dateOrTime(false, "--").render(price.getDate()));
                this.labelDiff.setHTML(diffRendered);
            }

            updateDiffImage(Renderer.CHANGE_PERCENT, price.getChangePercent());
            updateQuality(price.getQuality(), price.isPushPrice());
            this.currentPrice = pws.getPrice();
            this.hlpGraph.setData(price.getLow52W(), price.getHigh52W(), pws.getPrice());
        }
        else if (price.getType() == PriceDataType.FUND_OTC) {
            final String diffRendered = Renderer.CHANGE_PERCENT.render(price.getChangePercent());
            final PriceWithSupplement pws = price.getLastPrice();

            this.labelPrice.setText(Renderer.PRICE.render(pws.getPrice()) + currency);
            this.labelPrice.setStyleName(BASE_STYLE + " mm-price");
            this.labelTime.setText(Formatter.LF.formatDate(price.getDate()));
            this.labelDiff.setHTML(diffRendered);
            updateQuality(price.getQuality(), price.isPushPrice());
            updateDiffImage(Renderer.CHANGE_PERCENT, price.getChangePercent());
            this.currentPrice = pws.getPrice();
            this.hlpGraph.setData(price.getLow52W(), price.getHigh52W(), pws.getPrice());
        }
        else if (price.getType() == PriceDataType.CONTRACT_EXCHANGE || price.getType() == PriceDataType.LME) {
            final String diffRendered = Renderer.CHANGE_PERCENT.render(price.getChangePercent());
            final PriceWithSupplement pws = price.getLastPrice();

            this.labelPrice.setHTML(Renderer.PRICE.render(pws.getPrice()) + Renderer.SUPPLEMENT.render(pws.getSupplement()) + currency);
            this.labelPrice.setStyleName(BASE_STYLE + " mm-price");
            this.labelTime.setText(Formatter.formatTime(price.getDate()));
            this.labelDiff.setHTML(diffRendered);
            updateQuality(price.getQuality(), price.isPushPrice());
            updateDiffImage(Renderer.CHANGE_PERCENT, price.getChangePercent());
            this.currentPrice = pws.getPrice();
            this.hlpGraph.setData(price.getLow52W(), price.getHigh52W(), pws.getPrice());
        }

//        updateLimits();
        if (this.menuMarkets != null) {
            this.menuMarkets.removeAll();
            if (datas != null) {
                fillMenuMarkets(datas);
            }
        }
    }

    private IsWidget createPibDownloadWidget(String wkn, String pibReportUrl) {
        if (ContentFlagsEnum.DzMarginDialogRequired.isAvailableFor(currentQuote)) {
            return DzPibMarginDialog.createTriggerWidget(wkn);
        }
        else {
            return DzPibDownloadLinkRenderer.createWidget(DzPibMarginDialog.DYN_PIB_BASE_URL + pibReportUrl);
        }
    }

    private IsWidget createTriggerWidget(final InstrumentData instrumentData,
            final QuoteData quoteData) {
        final AbstractImagePrototype aip = IconImage.get("mm-icon-dzbank-rs"); // $NON-NLS-0$
        final Image image = aip.createImage();
        image.getElement().getStyle().setCursor(Style.Cursor.POINTER);
        image.setTitle(I18n.I.dzBankRsTooltip());
        image.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                LiveFinderResearch.INSTANCE.prepareFindInstrument(new QuoteWithInstrument(instrumentData, quoteData));
                if (WGZ_BANK_USER.isAllowed()) {
                    PlaceUtil.goTo("WZ_LF_RES1"); // $NON-NLS-0$
                }
                else {
                    PlaceUtil.goTo("DZ_LF_RES1"); // $NON-NLS-0$
                }
            }
        });
        return image;
    }

    private String getMarket(QuoteData quoteData) {
        if (this.marketSelectionByVwdcode) {
            return quoteData.getVwdcode();
        }
        String market = quoteData.getMarketName();
        if (FeatureFlags.Feature.NAME_SUFFIX.isEnabled()) {
            if (quoteData.getNameSuffix() != null && !quoteData.getNameSuffix().equals(quoteData.getCurrencyIso())) {
                market = market + " - " + quoteData.getNameSuffix(); // $NON-NLS$
            }
        }
        return market;
    }

    private SafeHtml renderBidAskAs(Price price, String currency) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<span qtip=\"").appendEscaped(I18n.I.bid()).appendHtmlConstant("\">").appendEscaped(Renderer.PRICE.render(price.getBid())).appendHtmlConstant("</span>");
        sb.appendHtmlConstant("<span class=\"header-separator\">|</span>");
        sb.appendHtmlConstant("<span qtip=\"").appendEscaped(I18n.I.ask()).appendHtmlConstant("\">").appendEscaped(Renderer.PRICE.render(price.getAsk())).appendHtmlConstant("</span>");
        sb.appendEscaped(currency);
        return sb.toSafeHtml();
    }

    private SafeHtml renderBidAsk(Price price, String currency) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<table cellspacing=\"0\" cellpadding=\"0\">"); // $NON-NLS$
        addBidAskRow(sb, I18n.I.bid(), price.getBid(), currency);
        addBidAskRow(sb, I18n.I.ask(), price.getAsk(), currency);
        sb.appendHtmlConstant("</table>"); // $NON-NLS$
        return sb.toSafeHtml();
    }

    private void addBidAskRow(SafeHtmlBuilder sb, final String label, final String value,
            String currency) {
        sb.appendHtmlConstant("<tr>"); // $NON-NLS$
        sb.appendHtmlConstant("<td class=\"mm-bidAsk-label\">").appendEscaped(label).appendHtmlConstant(":&nbsp;</td>"); // $NON-NLS$
        sb.appendHtmlConstant("<td class=\"mm-bidAsk-value\">").appendEscaped(Renderer.PRICE.render(value) + currency).appendHtmlConstant("</td>"); // $NON-NLS$
        sb.appendHtmlConstant("</tr>"); // $NON-NLS$
    }

    private void updateQuality(final String priceQuality, final boolean push) {
        updateImage(this.qualityImage, getQualityIcon(priceQuality, push));
        this.qualityImage.setTitle(translate(priceQuality, push));
    }

    private void updateDiffImage(ChangeRenderer changeRenderer, String diffRaw) {
        updateImage(this.diffImage, changeRenderer.getDiffIcon(diffRaw));
    }

    private void updateImage(Image img, AbstractImagePrototype aip) {
        img.setVisible(aip != null);
        if (aip != null) {
            aip.applyTo(img);
        }
    }

    private String translate(String priceQuality, boolean push) {
        final String suffix = push ? (" " + I18n.I.push()) : "";
        if (isRealtime(priceQuality)) {
            return I18n.I.priceQualityRealtime() + suffix;
        }
        if (isDelayed(priceQuality)) {
            return I18n.I.priceQualityDelayed() + suffix;
        }
        if (isEndOfDay(priceQuality)) {
            return I18n.I.priceQualityEndOfDay() + suffix;
        }
        return (priceQuality != null ? priceQuality : I18n.I.priceQualityUnknown()) + suffix;
    }

    private boolean isEndOfDay(String quality) {
        return quality != null && quality.startsWith("END_OF_DAY"); // $NON-NLS-0$
    }

    private boolean isDelayed(String quality) {
        return quality != null && quality.startsWith("DELAYED"); // $NON-NLS-0$
    }

    private boolean isRealtime(String quality) {
        return quality != null && quality.startsWith("REALTIME"); // $NON-NLS-0$
    }

    private AbstractImagePrototype getQualityIcon(String priceQuality, boolean push) {
        final String suffix = push ? "-push" : ""; // $NON-NLS$
        if (isRealtime(priceQuality)) {
            return IconImage.get("mm-quality-realtime" + suffix); // $NON-NLS-0$
        }
        if (isDelayed(priceQuality)) {
            return IconImage.get("mm-quality-neartime-20" + suffix); // $NON-NLS-0$
        }
        if (isEndOfDay(priceQuality)) {
            return IconImage.get("mm-quality-end-of-day" + suffix); // $NON-NLS-0$
        }
        return null;
    }

    void updateLimits() {
        if (this.buttonAddLimit != null) {
            this.buttonAddLimit.setVisible(this == current && this.currentQuote != null);
        }
    }

    private void fillMenuMarkets(MSCPriceDatas datas) {
        for (final MSCPriceDatasElement element : datas.getElement()) {
            final QuoteData quoteData = element.getQuotedata();
            final MenuItem item = new MenuItem(getItemName(quoteData));
            item.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent clickEvent) {
                    PlaceUtil.changeQuoteInView(quoteData);

                }
            });
            final String cls = getIconClass(Renderer.CHANGE_PERCENT, element);
            if (cls != null) {
                IconImage.setIconStyle(item, "mm-menu-diff-" + cls); // $NON-NLS-0$
            }
            this.menuMarkets.add(item);
        }
    }

    private String getIconClass(ChangeRenderer changeRenderer, MSCPriceDatasElement element) {
        return changeRenderer.getDiffStyle(Price.create(element).getChangePercent());
    }

    private String getItemName(QuoteData qd) {
        if (this.marketSelectionByVwdcode) {
            return qd.getVwdcode();
        }
        final StringBuilder sb = new StringBuilder().append(qd.getMarketName());
        String currencyIso = qd.getCurrencyIso();
        if (FeatureFlags.Feature.NAME_SUFFIX.isEnabled()) {
            String suffix = qd.getNameSuffix();
            if (suffix != null) {
                sb.append(" - ").append(suffix); // $NON-NLS-0$
                if (currencyIso != null && suffix.contains(currencyIso)) {
                    return sb.toString();
                }
            }
        }
        if (currencyIso != null) {
            sb.append(" - ").append(currencyIso); // $NON-NLS-0$
        }
        return sb.toString();
    }

    private Alert createAlert() {
        final Alert result = new Alert();
        result.setQuotedata(this.currentQuote);
        result.setInstrumentdata(this.currentInstrument);
        result.setReferenceValue(this.currentPrice);
        result.setFieldId(AlertUtil.getDefaultFieldId(this.currentQuote));
        return result;
    }

    private List<Alert> getAlerts(InstrumentData data) {
        return AlertController.INSTANCE.getAlertInformation(data.getIid());
    }

    @Override
    public Widget asWidget() {
        return this.viewWidget;
    }
}
