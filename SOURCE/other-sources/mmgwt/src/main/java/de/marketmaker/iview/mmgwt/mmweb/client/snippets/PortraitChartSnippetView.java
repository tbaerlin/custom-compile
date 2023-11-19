/*
 * PortraitChartSnippetView.java
 *
 * Created on 15.05.2008 14:07:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.ButtonGroup;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.LeftRightToolbar;
import de.marketmaker.itools.gwtutil.client.widgets.Separator;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;

import java.util.HashMap;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortraitChartSnippetView
        extends BasicChartSnippetView<PortraitChartSnippet, PortraitChartSnippetView>
        implements MarketSelectionButton.Callback {

    private static final java.util.Map<String, String> MENU_ITEM_TEXTS = new HashMap<String, String>() {{
        put("P1M", I18n.I.nMonths(1));  // $NON-NLS-0$
        put("P3M", I18n.I.nMonths(3));  // $NON-NLS-0$
        put("P6M", I18n.I.nMonths(6));  // $NON-NLS-0$
        put("P1Y", I18n.I.nYears(1));   // $NON-NLS-0$
        put("P2Y", I18n.I.nYears(2));   // $NON-NLS-0$
        put("P3Y", I18n.I.nYears(3));   // $NON-NLS-0$
        put("P5Y", I18n.I.nYears(5));   // $NON-NLS-0$
        put("P10Y", I18n.I.nYears(10)); // $NON-NLS-0$
        put(DateTimeUtil.PERIOD_KEY_YEAR_TO_DATE, I18n.I.yearToDate());
        put(DateTimeUtil.PERIOD_KEY_ALL, I18n.I.total());
    }};

    private final FeatureClickHandler corporateBtnClickHandler = new FeatureClickHandler("blendCorporateActions") { // $NON-NLS-0$
        @Override
        public void onClick(ClickEvent event) {
            final Button button = (Button) event.getSource();
            if (button.isActive()) {
                btnPerformance.setEnabled(false);
                btnPerformance.setActive(false);
            }
            else {
                btnPerformance.setEnabled(!btnDividends.isActive());
            }
            super.onClick(event);
        }
    };

    private final FeatureClickHandler underlyingBtnClickHandler = new FeatureClickHandler("underlying") { // $NON-NLS$
        @Override
        public void onClick(ClickEvent event) {
            final Button button = (Button) event.getSource();
            btnBenchmark.setEnabled(!button.isActive() || isIntraday());
            super.onClick(event);
        }
    };

    private final FeatureClickHandler dividendBtnClickHandler = new FeatureClickHandler("blendDividends") { // $NON-NLS-0$
        @Override
        public void onClick(ClickEvent event) {
            final Button button = (Button) event.getSource();
            if (button.isActive()) {
                btnPerformance.setEnabled(false);
                btnPerformance.setActive(false);
            }
            else {
                btnPerformance.setEnabled(!btnCorporateActions.isActive());
            }
            super.onClick(event);

        }
    };

    private final FeatureClickHandler percentBtnClickHandler = new FeatureClickHandler("percent");  // $NON-NLS-0$

    private final FeatureClickHandler performanceBtnClickHandler = new FeatureClickHandler("bviPerformanceForFunds") { // $NON-NLS-0$
        @Override
        public void onClick(ClickEvent event) {
            final Button button = (Button) event.getSource();
            if (button.isActive()) {
                btnDividends.setActive(false);
                btnDividends.setEnabled(false);
                btnCorporateActions.setActive(false);
                btnCorporateActions.setEnabled(false);
            }
            else {
                btnDividends.setEnabled(true);
                btnCorporateActions.setEnabled(true);
            }
            super.onClick(event);
        }
    };

    private final FeatureClickHandler benchmarkBtnClickHandler = new FeatureClickHandler("benchmark") { // $NON-NLS-0$
        @Override
        public void onClick(ClickEvent event) {
            final Button button = (Button) event.getSource();
            btnUnderlying.setEnabled(!button.isActive());
            super.onClick(event);
        }
    };

    interface MyBinder extends UiBinder<LeftRightToolbar, PortraitChartSnippetView> {
    }

    private static MyBinder uiBinder = GWT.create(MyBinder.class);

    private static final Button NULL_BUTTON = Button.build();

    private ButtonGroup buttonGroup;

    @UiField(provided = true)
    MarketSelectionButton msButton;

    @UiField
    LeftRightToolbar toolbar;

    @UiField
    Button idButton;

    @UiField
    Button wButton;

    @UiField
    SelectButton hButton;

    @UiField
    Button btnBid;

    @UiField
    Button btnAsk;

    @UiField
    Button btnUnderlying;

    @UiField
    Button btnBenchmark;

    @UiField
    Button btnCorporateActions;

    @UiField
    Button btnDividends;

    @UiField
    Button btnPercent;

    @UiField
    Menu menu;

    @UiField
    LazyPanel msButtonPanel;

    private InstrumentTypeEnum instrumentType = null;

    @UiField
    Separator idSeparator;

    @UiField
    Separator wSeparator;

    @UiField
    Button btnPerformance;

    @UiField
    MenuItem defaultItem;

    @UiField
    Button linkButton;

    @UiField
    LazyPanel linkButtonPanel;

    private String selectedPeriod;

    public PortraitChartSnippetView(final PortraitChartSnippet snippet) {
        super(snippet);

        uiBinder.createAndBindUi(this);

        if (this.snippet.isStandalone()) {
            this.msButton = new MarketSelectionButton(this, 12);
            this.msButtonPanel.setVisible(true);
        }

        idButton.setText(I18n.I.nDayAbbr(1));
        wButton.setText(I18n.I.nDayAbbr(5));

        setMenuItemText();

        this.hButton.setSelectedItem(defaultItem);

        this.buttonGroup = new ButtonGroup(this.idButton, this.wButton, this.hButton);

        if (this.msButton != null) {
            this.linkButtonPanel.setVisible(true);
            IconImage.setIconStyle(this.linkButton, "mm-compactQuoteJumpPortrait"); // $NON-NLS$
            Tooltip.addQtip(this.linkButton, I18n.I.tooltipGotoPortrait());
        }

        final String buttonConfig = this.snippet.getButtonConfig();

        if (buttonConfig.contains("U")) { // $NON-NLS-0$
            btnUnderlying.setVisible(true);
        } else {
            btnUnderlying = NULL_BUTTON;
        }

        if (buttonConfig.contains("B")) { // $NON-NLS-0$
            btnBenchmark.setVisible(true);
        } else {
            btnBenchmark = NULL_BUTTON;
        }

        if (buttonConfig.contains("K")) { // $NON-NLS-0$
            btnCorporateActions.setVisible(true);
        } else {
            btnCorporateActions = NULL_BUTTON;
        }

        if (buttonConfig.contains("A")) { // $NON-NLS-0$
            btnDividends.setVisible(true);
        } else {
            btnDividends = NULL_BUTTON;
        }

        if (buttonConfig.contains("P")) { // $NON-NLS-0$
            btnPerformance.setVisible(true);
        } else {
            btnPerformance = NULL_BUTTON;
        }

        if (buttonConfig.contains("%")) { // $NON-NLS$
            btnPercent.setVisible(true);
        } else {
            btnPercent = NULL_BUTTON;
        }

        this.selectedPeriod = this.snippet.getConfiguration().getString("period"); // $NON-NLS$
        if (this.selectedPeriod == null) {
            if ("start".equals(this.snippet.getConfiguration().getString("from"))) { // $NON-NLS$
                this.selectedPeriod = DateTimeUtil.PERIOD_KEY_ALL;
            }
            else {
                this.selectedPeriod = "P1Y"; // $NON-NLS$
            }
        }

        this.buttonGroup.setSelectedData("period", this.selectedPeriod); // $NON-NLS$
        this.buttonGroup.addSelectionHandler(new SelectionHandler<Button>() {
            @Override
            public void onSelection(SelectionEvent<Button> event) {
                onPeriodChange(event.getSelectedItem(), false);
            }
        });
        onPeriodChange(this.buttonGroup.getSelected(), true);

        if (!getConfiguration().getBoolean("intraday", true)) { // $NON-NLS-0$
            setIntradayEnabled(false);
        }
    }

    private void setMenuItemText() {
        for (MenuItem item : this.menu.getItems()) {
            Object period = item.getData("period");   // $NON-NLS-0$
            item.setText(MENU_ITEM_TEXTS.get(period));
        }
    }

    public void reset(InstrumentTypeEnum instrumentType) {
        if (this.instrumentType != instrumentType) {
            if (instrumentType != InstrumentTypeEnum.STK) {
                this.btnDividends.setText("A"); // $NON-NLS-0$
                this.btnDividends.setTitle(I18n.I.chartOptionEvaluateDistributionForNonStocks() +
                        " " + I18n.I.chartOptionHistoricPricesAdapted());
            }
            else {
                this.btnDividends.setText("D"); // $NON-NLS-0$
                this.btnDividends.setTitle(I18n.I.chartOptionEvaluateDistributionForStocks() +
                        " " + I18n.I.chartOptionHistoricPricesAdapted());
            }
            this.instrumentType = instrumentType;
        }
        // reset buttons to default according to MMWEB-96
        this.btnUnderlying.setActive(false);
        this.btnUnderlying.setEnabled(false);
        this.btnDividends.setActive(false);
        if (this.btnPerformance != NULL_BUTTON) {
            this.btnPerformance.setActive(true);
            this.btnCorporateActions.setActive(false);
        }
        else {
            this.btnCorporateActions.setActive(true);
        }
        this.btnBenchmark.setActive(this.btnBenchmark != NULL_BUTTON && this.snippet.isBenchmarkConfigured());
        this.btnPercent.setActive(this.btnPercent != NULL_BUTTON && this.snippet.isPercentConfigured());
        this.btnCorporateActions.setActive(this.btnCorporateActions != NULL_BUTTON && this.snippet.isCorporateActionsConfigured());
    }

    public void setIntradayEnabled(boolean flag) {
        setVisible(flag, this.idButton, this.idSeparator, this.wButton, this.wSeparator);
        setVisible(flag && isIntraday(), this.btnAsk, this.btnBid);
    }

    void enableUnderlyingButton() {
        this.btnUnderlying.setEnabled(!isBenchmarkPressed());
    }

    private boolean isBenchmarkPressed() {
        return this.btnBenchmark != NULL_BUTTON && this.btnBenchmark.isActive();
    }

    protected void update(IMGResult imgResult) {
        super.update(imgResult);
        setPrintCaption(imgResult);
    }

    private void setPrintCaption(IMGResult imgResult) {
        final SnippetConfiguration config = snippet.getConfiguration();
        final StringBuilder sb = new StringBuilder();
        addPrintCaption(sb, "mm-chartcenter-main", // $NON-NLS-0$
                SafeHtmlUtils.htmlEscape(imgResult.getInstrumentdata().getName()));
        if (config.getString("bid") != null) { // $NON-NLS-0$
            addPrintCaption(sb, "mm-chartcenter-bid", I18n.I.bid());  // $NON-NLS-0$
        }
        if (config.getString("ask") != null) { // $NON-NLS-0$
            addPrintCaption(sb, "mm-chartcenter-ask", I18n.I.ask());  // $NON-NLS-0$
        }
        super.setPrintCaption(new HTML(sb.toString()));
    }

    private void addPrintCaption(StringBuilder sb, String colorStyle, String text) {
        sb.append("<div class=\"mm-printCaption\"><span class=\"").append(colorStyle) // $NON-NLS-0$
                .append("\">&#160;&#160;</span> ").append(text).append("</div>"); // $NON-NLS-0$ $NON-NLS-1$
    }

    protected void onContainerAvailable() {
        this.container.setTopWidget(this.toolbar);
        super.onContainerAvailable();
    }

    public void updateQuote(String qid) {
        this.snippet.updateQuote(qid);
    }

    private void onPeriodChange(Button button, boolean viewOnly) {
        final boolean intraday = isIntraday();
        setVisible(intraday, this.btnAsk, this.btnBid);
        this.btnBenchmark.setEnabled(!intraday || (this.btnUnderlying != NULL_BUTTON && this.btnUnderlying.isActive()));
        setVisible(!intraday, this.btnBenchmark, this.btnCorporateActions, this.btnDividends);

        if (viewOnly) {
            return;
        }

        this.selectedPeriod = (String) button.getData("period"); // $NON-NLS$
        this.snippet.setPeriod(this.selectedPeriod);
    }


    @Override
    protected void goToChartcenter(PlaceChangeEvent event) {
        super.goToChartcenter(event.withProperty("period", this.selectedPeriod)); // $NON-NLS-0$
    }

    private boolean isIntraday() {
        return this.buttonGroup.getSelected() != this.hButton;
    }

    public void updateQuotesMenu(List<QuoteData> quotedata, QuoteData qd) {
        this.msButton.updateQuotesMenu(quotedata, qd);
    }

    public void disableQuotesMenu() {
        this.msButton.setEnabled(false);
    }

    protected String getImageUrl(IMGResult ipr) {
        final String url = ChartUrlFactory.getUrl(ipr.getRequest());
        final String benchmark = this.snippet.getBenchmarkQid();
        if (benchmark == null) {
            return url;
        }
        return url + "&benchmark=" + benchmark; // $NON-NLS-0$
    }

    protected void setImageUrl(IMGResult ipr, final String instrumentType) {
        super.setImageUrl(ipr, instrumentType);
        if (this.linkToChartcenter != null && this.snippet.isBenchmarkConfigured()) {
            this.linkToChartcenter.withProperty("compareSymbols", this.snippet.getBenchmarkQid()); // $NON-NLS-0$
        }
    }

    public void setVisible(boolean visible, Widget... widgets) {
        for (Widget widget : widgets) {
            widget.setVisible(visible);
        }
    }

    @UiHandler("btnBenchmark")
    public void handleBtnBenchmark(ClickEvent event) {
        benchmarkBtnClickHandler.onClick(event);
    }

    @UiHandler("btnDividends")
    public void handleBtnDividends(ClickEvent event) {
        dividendBtnClickHandler.onClick(event);
    }

    @UiHandler("btnPerformance")
    public void handleBtnPerformance(ClickEvent event) {
        performanceBtnClickHandler.onClick(event);
    }

    @UiHandler("btnPercent")
    public void handleBtnPercent(ClickEvent event) {
        percentBtnClickHandler.onClick(event);
    }

    @UiHandler("btnCorporateActions")
    public void handleBtnCorporate(ClickEvent event) {
        corporateBtnClickHandler.onClick(event);
    }

    @UiHandler("btnUnderlying")
    public void handleBtnUnderlying(ClickEvent event) {
        underlyingBtnClickHandler.onClick(event);
    }

    @UiHandler("linkButton")
    public void handleLinkButton(ClickEvent event) {
        PortraitChartSnippetView.this.snippet.goToPortrait();
    }

    @UiHandler({"btnBid", "btnAsk"})
    public void handleBidAskButtons(ClickEvent event) {
        snippet.getConfiguration().put("ask", btnAsk.isActive() ? "true" : null); // $NON-NLS$
        snippet.getConfiguration().put("bid", btnBid.isActive() ? "true" : null); // $NON-NLS$
        snippet.ackParametersChanged();
    }

    private class FeatureClickHandler implements ClickHandler {
        private final String configName;

        FeatureClickHandler(String configName) {
            this.configName = configName;
        }

        @Override
        public void onClick(ClickEvent event) {
            final Button button = (Button) event.getSource();
            snippet.getConfiguration().put(this.configName, button.isActive() ? "true" : "false"); // $NON-NLS$
            snippet.ackParametersChanged();
        }
    }
}
