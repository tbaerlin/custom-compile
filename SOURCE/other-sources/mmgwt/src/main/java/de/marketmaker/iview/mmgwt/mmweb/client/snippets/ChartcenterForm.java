/*
 * ChartcenterForm.java
 *
 * Created on 28.05.2008 14:35:06
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.extjs.gxt.ui.client.event.DNDListener;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDateUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.FlexLabel;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.CheckBox;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.Caption;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
public class ChartcenterForm extends FlexTable implements ConfigurableSnippet {
    private static final String MM_CHARTCENTER_FORM_STYLE = "mm-chartcenter-form"; // $NON-NLS$

    private static final String MM_CHARTCENTER_BENCHMARK_STYLE = "mm-chartcenter-benchmark"; // $NON-NLS$

    private static final String MM_CHARTCENTER_MAIN_STYLE = "mm-chartcenter-main"; // $NON-NLS$

    private static final String MM_CHARTCENTER_FORM_ITEM_STYLE = "mm-chartcenter-form-item"; // $NON-NLS$

    private static final String MM_CHARTCENTER_FORM_LABEL_STYLE = "mm-chartcenter-form-label"; // $NON-NLS$

    private static final String MM_MINUS_STYLE = "mm-minus"; // $NON-NLS$

    private static final String MM_PLUS_STYLE = "mm-plus"; // $NON-NLS$

    private static final String MM_CHARTCENTER_DD_STYLE = "mm-chartcenter-dd"; // $NON-NLS$

    private static final String DEFAULT_PERIOD = "P3M"; //$NON-NLS$

    private static final String PERIOD_KEY_FROM_TO = "from-to"; // $NON-NLS-0$

    private static final String PERIOD_KEY_YEAR_TO_DATE = DateTimeUtil.PERIOD_KEY_YEAR_TO_DATE;

    private static final String PERIOD_KEY_ALL = DateTimeUtil.PERIOD_KEY_ALL;

    private static final String KEY_NONE = "none"; // $NON-NLS-0$

    private static final String[] GD_NAMES = new String[]{"38", "100", "200"}; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$

    private static final int MESSAGE_DISPLAYTIME = 7500; // ms

    private static final String MENU_KEY = "key";  // $NON-NLS$

    private final SnippetConfiguration config;

    private SelectButton cbType;

    private SelectButton cbPeriod;

    private DateBox dateFieldFrom;

    private DateBox dateFieldTo;

    private CheckBox[] cbGd = new CheckBox[GD_NAMES.length];

    private SelectButton cbBenchmark;

    private SelectButton cbIndicator;

    private final SelectButton cbCurrency;

    private final SelectButton cbLmeField;

    private CheckBox cbChartSizeLarge;

    private CheckBox cbChartPrintColor;

    private CheckBox cbVolume;

    private CheckBox cbCorporateActions;

    private CheckBox cbDividends;

    private final Label cbDividendsLabel;

    private CheckBox cbLog;

    private CheckBox cbAbsolute;

    private CheckBox cbPercent;

    private CheckBox cbBvi;

    private CheckBox cbAdjustFrom;

    private Panel pBvi;

    private CheckBox cbSplits;

    private Panel pSplits;

    private CheckBox cbBid;

    private CheckBox cbAsk;

    private CheckBox cbUnderlying;

    private Panel pUnderlying;

    public static final int MAX_NUM_COMPARE = 10;

    private int numCompareItems = 0;

    private QuoteWithInstrument[] compareItems = new QuoteWithInstrument[MAX_NUM_COMPARE];

    private InlineHTML[] spanCompareColor = new InlineHTML[MAX_NUM_COMPARE];

    private CheckBox[] cbCompareItem = new CheckBox[MAX_NUM_COMPARE];

    private Button[] btnCompareItem = new Button[MAX_NUM_COMPARE];

    private Label[] compareSymbols = new Label[MAX_NUM_COMPARE];

    private final LayoutContainer dragNdropPanel;

    private InstrumentTypeEnum instrumentType = null;

    private boolean convertibleCurrency = true;

    private static final String[][] chartTypes = {
            {"line.mnt", I18n.I.chartTypeLineMountain()},  // $NON-NLS-0$
            {"line", I18n.I.chartTypeLine()},  // $NON-NLS-0$
            {"bar", I18n.I.chartTypeBar()},  // $NON-NLS-0$
            {"ohlc", I18n.I.chartTypeOpenHighLowClose()},  // $NON-NLS-0$
            {"candle", I18n.I.chartTypeCandlesticks()}  // $NON-NLS-0$
    };

    private static final Map<String, String[][]> mapBenchmarks = new HashMap<>();

    private static final String[][] indicator = new String[][]{
            {KEY_NONE, I18n.I.indicatorNone()},
            {"momentum", I18n.I.indicatorMomentum()},  // $NON-NLS-0$
            {"roc", I18n.I.indicatorRateOfChange()},  // $NON-NLS-0$
            {"rsi", I18n.I.indicatorRelativeStrength()},  // $NON-NLS-0$
            {"macd", I18n.I.indicatorMacd()},  // $NON-NLS-0$
            {"ss", I18n.I.indicatorSlowStochastik()},  // $NON-NLS-0$
            {"fs", I18n.I.indicatorFastStochastik()},  // $NON-NLS-0$
            {"vma", I18n.I.indicatorVariableMa()},  // $NON-NLS-0$
            {"bb", I18n.I.indicatorBollingerBands()}  // $NON-NLS-0$
    };

    private static String[][] shortPeriods;

    private static final String[][] shortPeriodsIntraday = new String[][]{
            {PERIOD_KEY_FROM_TO, I18n.I.periodFromTo()},
            {PERIOD_KEY_YEAR_TO_DATE, I18n.I.periodCurrentYear()},
            {"P1D", I18n.I.nDays(1)},  // $NON-NLS-0$
            {"P5D", I18n.I.nDays(5)},  // $NON-NLS-0$
            {"P1M", I18n.I.nMonths(1)},  // $NON-NLS-0$
            {"P3M", I18n.I.nMonths(3)},  // $NON-NLS-0$
            {"P6M", I18n.I.nMonths(6)},  // $NON-NLS-0$
            {"P1Y", I18n.I.nYears(1)},  // $NON-NLS-0$
            {"P2Y", I18n.I.nYears(2)},  // $NON-NLS-0$
            {"P3Y", I18n.I.nYears(3)},  // $NON-NLS-0$
            {"P5Y", I18n.I.nYears(5)},  // $NON-NLS-0$
            {"P10Y", I18n.I.nYears(10)},  // $NON-NLS-0$
            {PERIOD_KEY_ALL, I18n.I.periodMaximum()}
    };

    //Periods for funds that have no intraday data.
    //In this case we add an additional period of two weeks.
    private static final String[][] shortPeriodsNoIntraday = new String[][]{
            {PERIOD_KEY_FROM_TO, I18n.I.periodFromTo()},
            {PERIOD_KEY_YEAR_TO_DATE, I18n.I.periodCurrentYear()},
            {"P2W", I18n.I.nWeeks(2)},  // $NON-NLS-0$
            {"P1M", I18n.I.nMonths(1)},  // $NON-NLS-0$
            {"P3M", I18n.I.nMonths(3)},  // $NON-NLS-0$
            {"P6M", I18n.I.nMonths(6)},  // $NON-NLS-0$
            {"P1Y", I18n.I.nYears(1)},  // $NON-NLS-0$
            {"P2Y", I18n.I.nYears(2)},  // $NON-NLS-0$
            {"P3Y", I18n.I.nYears(3)},  // $NON-NLS-0$
            {"P5Y", I18n.I.nYears(5)},  // $NON-NLS-0$
            {"P10Y", I18n.I.nYears(10)},  // $NON-NLS-0$
            {PERIOD_KEY_ALL, I18n.I.periodMaximum()}
    };

    private static final String[][] supportedCurrencies = new String[][]{
            {KEY_NONE, "- " + I18n.I.currencyOriginal() + " -"},  // $NON-NLS-0$ $NON-NLS-1$
            {"EUR", "EUR"}, // $NON-NLS-0$ $NON-NLS-1$
            {"USD", "USD"}, // $NON-NLS-0$ $NON-NLS-1$
            {"JPY", "JPY"}, // $NON-NLS-0$ $NON-NLS-1$
            {"GBP", "GBP"}, // $NON-NLS-0$ $NON-NLS-1$
            {"CHF", "CHF"}, // $NON-NLS-0$ $NON-NLS-1$
    };

    private static final String[][] lmeFieldset = new String[][]{
            {KEY_NONE, I18n.I.closing()},
            {"30", I18n.I.bid()},               // $NON-NLS$
            {"28", I18n.I.ask()},               // $NON-NLS$
            {"1169", I18n.I.bid() + I18n.I.officialSuffix()},     // $NON-NLS$
            {"1168", I18n.I.ask() + I18n.I.officialSuffix()},     // $NON-NLS$
            {"1171", I18n.I.bid() + I18n.I.unOfficialSuffix()},   // $NON-NLS$
            {"1170", I18n.I.ask() + I18n.I.unOfficialSuffix()},   // $NON-NLS$
            {"235", I18n.I.interpoClosing()},   // $NON-NLS$
            {"236", I18n.I.provEvaluation()},   // $NON-NLS$
    };

    protected final ChartcenterSnippet snippet;

    private final Panel mainQuotePanel;

    private final Label mainQuoteLabel;

    private final Panel lmePanel;

    private static String[][] getBenchmarkData(final String benchmarkName) {
        String[][] result = mapBenchmarks.get(benchmarkName);
        if (result == null) {
            result = createBenchmarkData(benchmarkName);
            mapBenchmarks.put(benchmarkName, result);
        }
        return result;
    }

    private static String[][] createBenchmarkData(final String benchmarkName) {
        final List<QuoteWithInstrument> qwis = SessionData.INSTANCE.getList(benchmarkName);
        assert (qwis != null);
        final String[][] result = new String[qwis.size() + 1][];
        int n = 0;
        result[n++] = new String[]{KEY_NONE, I18n.I.benchmarkNone()};
        for (QuoteWithInstrument qwi : qwis) {
            result[n++] = new String[]{qwi.getQuoteData().getQid(), qwi.getName()};
        }
        return result;
    }

    public ChartcenterForm(final SnippetConfiguration config, final ChartcenterSnippet snippet) {
        this.snippet = snippet;
        this.config = config;

        final MmJsDate now = new MmJsDate();
        final MmJsDate threeMonthsAgo = new MmJsDate();
        MmJsDateUtil.minus(threeMonthsAgo, "P3M"); // $NON-NLS-0$

        this.dateFieldFrom = new DateBox(new MmJsDate(threeMonthsAgo));
        this.dateFieldFrom.addValueChangeHandler(e -> {
            dateFieldTo.setMinDate(e.getValue());
            validateDates();
            cbAdjustFrom.setChecked(false, false);
        });
        addReloadListener(this.dateFieldFrom);
        this.dateFieldTo = new DateBox(new MmJsDate(now));
        this.dateFieldTo.addValueChangeHandler(e -> {
            dateFieldFrom.setMaxDate(e.getValue());
            validateDates();
        });
        addReloadListener(this.dateFieldTo);

        this.dateFieldFrom.setMaxDate(this.dateFieldTo.getDate());
        this.dateFieldTo.setMinDate(this.dateFieldFrom.getDate());

        this.dateFieldFrom.setEnabled(false);
        this.dateFieldTo.setEnabled(false);


        shortPeriods = shortPeriodsIntraday;
        this.cbPeriod = createComboBox(shortPeriods);
        initShortPeriodComboBox();
        this.cbPeriod.addSelectionHandler(e -> {
            setDateByPeriod();
            redraw();
        });

        this.cbCurrency = createComboBox(supportedCurrencies);
        addReloadListener(this.cbCurrency);

        this.cbLmeField = createComboBox(lmeFieldset);
        addReloadListener(this.cbLmeField);

        this.cbBvi = createCheckBox2(false);
        this.cbBvi.addValueChangeHandler(e -> checkFieldsEnabled());
        this.cbBvi.addValueChangeHandler(e -> redraw());

        this.cbType = createComboBox(chartTypes);
        addReloadListener(this.cbType);

        final Widget[][] cbGdLabelHelper = new Widget[GD_NAMES.length][2];
        for (int i = 0; i < this.cbGd.length; i++) {
            final CheckBox checkBox2 = createCheckBox2();
            this.cbGd[i] = checkBox2;
            cbGdLabelHelper[i][0] = checkBox2;
            cbGdLabelHelper[i][1] = checkBox2.createSpan(getCheckBoxColorMarkLabel2(GD_NAMES[i], "mm-chartcenter-gd" + GD_NAMES[i]));  // $NON-NLS$
        }

        this.cbBid = createCheckBox2();
        this.cbAsk = createCheckBox2();

        this.cbUnderlying = createCheckBox2();

        final String benchmarkName = config.getString("benchmarkName", "list_chartcenter_benchmarks"); // $NON-NLS-0$ $NON-NLS-1$
        final String[][] benchmarkData = getBenchmarkData(benchmarkName);
        this.cbBenchmark = createComboBox(benchmarkData);
        addReloadListener(this.cbBenchmark);

        this.cbIndicator = createComboBox(indicator);
        addReloadListener(this.cbIndicator);

        this.cbAdjustFrom = createCheckBox2();
        this.cbAdjustFrom.setChecked(config.getBoolean("adjustFrom", true), false); // $NON-NLS-0$

        final int compareGridStart;
        final Grid underlyingGrid;
        if (config.getBoolean("withUnderlying", false)) { // $NON-NLS-0$
            compareGridStart = 1;
            underlyingGrid = new Grid(1, 3);
            underlyingGrid.setStyleName("mm-chartcenter-compare-underlying");
            underlyingGrid.setCellPadding(0);
            underlyingGrid.setCellSpacing(0);

            this.spanCompareColor[0] = new InlineHTML();
            this.spanCompareColor[0].setStyleName(MM_CHARTCENTER_BENCHMARK_STYLE + "1"); // $NON-NLS$
            this.spanCompareColor[0].setHTML("&nbsp;&nbsp;"); // $NON-NLS-0$
            this.cbCompareItem[0] = new CheckBox(false);
            this.compareSymbols[0] = new Label();
            this.btnCompareItem[0] = null;
            this.numCompareItems = 1;
            underlyingGrid.setWidget(0, 0, this.spanCompareColor[0]);
            underlyingGrid.setWidget(0, 1, this.cbCompareItem[0]);
            underlyingGrid.setWidget(0, 2, this.compareSymbols[0]);
        }
        else {
            compareGridStart = 0;
            underlyingGrid = null;
        }

        final Grid compareItemsGrid = new Grid(MAX_NUM_COMPARE - compareGridStart, 4);
        final HTMLTable.ColumnFormatter columnFormatter = compareItemsGrid.getColumnFormatter();
        columnFormatter.setWidth(0, "12"); // $NON-NLS-0$
        columnFormatter.setWidth(1, "20"); // $NON-NLS-0$
        columnFormatter.setWidth(2, "120"); // $NON-NLS-0$
        columnFormatter.setWidth(3, "18"); // $NON-NLS-0$
        compareItemsGrid.setStyleName("mm-chartcenter-compare-items");
        compareItemsGrid.setCellPadding(0);
        compareItemsGrid.setCellSpacing(0);
        for (int i = compareGridStart; i < MAX_NUM_COMPARE; i++) {
            this.spanCompareColor[i] = new InlineHTML();
            this.spanCompareColor[i].setStyleName(MM_CHARTCENTER_BENCHMARK_STYLE + (i + 1));
            this.spanCompareColor[i].setHTML("&nbsp;&nbsp;"); // $NON-NLS-0$
            this.spanCompareColor[i].setVisible(false);
            this.cbCompareItem[i] = new CheckBox(false);
            this.cbCompareItem[i].addValueChangeHandler(e -> redraw());
            this.cbCompareItem[i].setVisible(false);
            this.compareSymbols[i] = new Label();
            final int buttonId = i;
            this.btnCompareItem[i] = Button.icon(MM_PLUS_STYLE)
                    .tooltip(I18n.I.addComparisonValue())
                    .clickHandler(e -> modifyCompare(buttonId))
                    .build();
            this.btnCompareItem[i].setVisible(i == compareGridStart);
            compareItemsGrid.setWidget(i - compareGridStart, 0, this.spanCompareColor[i]);
            compareItemsGrid.setWidget(i - compareGridStart, 1, this.cbCompareItem[i]);
            compareItemsGrid.setWidget(i - compareGridStart, 2, this.compareSymbols[i]);
            compareItemsGrid.setWidget(i - compareGridStart, 3, this.btnCompareItem[i]);
        }

        addReloadListener(this.cbCompareItem);

        this.dragNdropPanel = new LayoutContainer();
        BrowserSpecific.INSTANCE.setStyle(this.dragNdropPanel, MM_CHARTCENTER_DD_STYLE, null);
        this.dragNdropPanel.add(compareItemsGrid);
        final FlowPanel panelCompare = new FlowPanel();
        panelCompare.setStyleName("mm-chartcenter-compare");
        if (underlyingGrid != null) {
            panelCompare.add(new Caption(amendLabel(I18n.I.underlyingInstrument())));
            panelCompare.add(underlyingGrid);
        }
        final InlineHTML spanLabel = new InlineHTML();
        spanLabel.setStyleName(MM_CHARTCENTER_MAIN_STYLE);
        spanLabel.setHTML("&nbsp;&nbsp;"); // $NON-NLS-0$

        this.mainQuoteLabel = new Label();

        final Grid grid = new Grid(1, 2);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.addStyleName("mm-chartcenter-compare-mainQuote");
        grid.setWidget(0, 0, spanLabel);
        grid.setWidget(0, 1, this.mainQuoteLabel);

        this.mainQuotePanel = new FlowPanel();
        this.mainQuotePanel.setStyleName("mm-chartcenter-compare-mainQuotePanel");
        this.mainQuotePanel.add(new Caption(amendLabel(I18n.I.mainQuote())));
        this.mainQuotePanel.add(grid);
        hideMainInstrumentPanel();

        panelCompare.add(this.mainQuotePanel);
        panelCompare.add(new Caption(amendLabel(I18n.I.comparisonValues(2))));
        panelCompare.add(this.dragNdropPanel);

        initDragNDrop();

        this.cbChartSizeLarge = createCheckBox2();
        this.cbChartPrintColor = createCheckBox2();
        this.cbVolume = createCheckBox2();
        this.cbLog = createCheckBox2();
        this.cbAbsolute = createCheckBox2(false);
        this.cbPercent = createCheckBox2(false);
        this.cbCorporateActions = createCheckBox2();
        this.cbCorporateActions.setChecked(config.getBoolean("blendCorporateActions", true), false); // $NON-NLS$

        this.cbDividends = createCheckBox2();
        this.cbDividendsLabel = this.cbDividends.createSpan(SafeHtmlUtils.EMPTY_SAFE_HTML);
        this.cbSplits = createCheckBox2();

        final ValueChangeHandler<Boolean> absoluteOrPercentChangeHandler = event -> {
            if (event.getValue()) {
                final CheckBox target = (event.getSource() == this.cbAbsolute) ? this.cbPercent : this.cbAbsolute;
                target.setChecked(false);
            }
            redraw();
        };
        this.cbAbsolute.addValueChangeHandler(absoluteOrPercentChangeHandler);
        this.cbPercent.addValueChangeHandler(absoluteOrPercentChangeHandler);

        /* put widgets on the page */
        final FlowPanel fpCenter = new FlowPanel();
        //noinspection GWTStyleCheck
        fpCenter.setStyleName(MM_CHARTCENTER_FORM_STYLE + " mm-chartcenter-center");  // $NON-NLS$
        add(fpCenter, I18n.I.chartType(), this.cbType);
        add(fpCenter, I18n.I.period(), this.cbPeriod);
        add(fpCenter, I18n.I.fromUpperCase(), this.dateFieldFrom);
        add(fpCenter, I18n.I.toUpperCase(), this.dateFieldTo);
        lmePanel = add(fpCenter, "<span class=\"" + MM_CHARTCENTER_MAIN_STYLE + "\">&nbsp;&nbsp;</span>&nbsp;" + I18n.I.lmeField(), this.cbLmeField);  // $NON-NLS$
        lmePanel.setVisible(FeatureFlags.Feature.LME_CHANGES_2014.isEnabled());
        add(fpCenter, "<span class=\"mm-chartcenter-benchmark\">&nbsp;&nbsp;</span>&nbsp;" + I18n.I.benchmark(), this.cbBenchmark);  // $NON-NLS-0$
        add(fpCenter, "<span class=\"mm-chartcenter-indicator\">&nbsp;&nbsp;</span>&nbsp;" + I18n.I.indicator(), this.cbIndicator);  // $NON-NLS-0$
        add(fpCenter, I18n.I.currency(), this.cbCurrency);
        addCheckBox(fpCenter, I18n.I.movingAverageInDaysAbbr(), cbGdLabelHelper);
        addCheckBox(fpCenter, I18n.I.chartOptionIntradayWithBidAsk(),
                new Widget[]{this.cbBid, this.cbBid.createSpan(getCheckBoxColorMarkLabel2(I18n.I.bid(), "mm-chartcenter-bid"))},  // $NON-NLS$
                new Widget[]{this.cbAsk, this.cbAsk.createSpan(getCheckBoxColorMarkLabel2(I18n.I.ask(), "mm-chartcenter-ask"))});  // $NON-NLS$

        final FlowPanel fpRight = new FlowPanel();
        //noinspection GWTStyleCheck
        fpRight.setStyleName(MM_CHARTCENTER_FORM_STYLE + " mm-chartcenter-right");  // $NON-NLS$
        addCheckBox(fpRight, I18n.I.chartSizeLarge(), this.cbChartSizeLarge);
        addCheckBox(fpRight, I18n.I.chartPrintColor(), this.cbChartPrintColor);
        addCheckBox(fpRight, I18n.I.volume(), this.cbVolume);
        addCheckBox(fpRight, I18n.I.chartOptionLogarithmicScaleAbbr(), this.cbLog);
        addCheckBox(fpRight, I18n.I.absoluteValues(), this.cbAbsolute);
        addCheckBox(fpRight, I18n.I.relativeValues(), this.cbPercent);
        this.pBvi = addCheckBox(fpRight, I18n.I.chartOptionBviPerformance(), this.cbBvi);
        this.pSplits = addCheckBox(fpRight, I18n.I.chartOptionShowSplits(), this.cbSplits);
        addCheckBox(fpRight, I18n.I.evaluateCorporateActions(), this.cbCorporateActions);
        addCheckBox(fpRight, this.cbDividends, this.cbDividendsLabel);
        this.pUnderlying = addCheckBox(fpRight, I18n.I.underlyingInstrument(), this.cbUnderlying);
        this.pUnderlying.setVisible(false);

        addCheckBox(fpRight, I18n.I.adjustFrom(), this.cbAdjustFrom);

        final HTMLTable.ColumnFormatter cf = getColumnFormatter();
        final FlexTable.FlexCellFormatter formatter = getFlexCellFormatter();

        final boolean allowCompare = config.getBoolean("allowCompare", true); // $NON-NLS-0$

        if (allowCompare) {
            cf.setWidth(0, "33%"); // $NON-NLS-0$
            cf.setWidth(1, "37%"); // $NON-NLS-0$
            cf.setWidth(2, "30%"); // $NON-NLS-0$
        }
        else {
            cf.setWidth(0, "10%"); // $NON-NLS-0$
            cf.setWidth(1, "40%"); // $NON-NLS-0$
            cf.setWidth(2, "40%"); // $NON-NLS-0$
        }

        setCellPadding(0);
        setCellSpacing(0);

        DecoratorPanel warnPanel = new DecoratorPanel();
        warnPanel.add(new Label(I18n.I.startFromAdjusted()));
        setWidget(0, 0, warnPanel);
        formatter.setColSpan(0, 0, 3);
        formatter.setVisible(0, 0, false);
        formatter.setStyleName(0, 0, "mm-chartcenter-form-msg"); // $NON-NLS-0$

        setStyleName(MM_CHARTCENTER_FORM_STYLE);
        if (SessionData.isAsDesign()) {
            Styles.tryAddStyles(this, Styles.get().generalViewStyle());
        }
        if (allowCompare) {
            setWidget(1, 0, panelCompare);
        }
        else {
            setWidget(1, 0, null);
        }
        formatter.setStyleName(1, 0, MM_CHARTCENTER_FORM_STYLE);

        setWidget(1, 1, fpCenter);
        formatter.setStyleName(1, 1, MM_CHARTCENTER_FORM_STYLE);

        setWidget(1, 2, fpRight);
        formatter.setStyleName(1, 2, MM_CHARTCENTER_FORM_STYLE);

        checkFieldsEnabled();
    }

    private String amendLabel(String label) {
        if (!SessionData.isAsDesign()) {
            return label + ":"; // $NON-NLS$
        }
        return label;
    }

    private SelectButton createComboBox(final String[][] values) {
        final Menu menu = new Menu();
        fillMenu(menu, values);
        final SelectButton sb = new SelectButton(
                SessionData.isAsDesign() ? Button.RendererType.SPAN : Button.RendererType.TABLE)
                .withMenu(menu).withClickOpensMenu();
        Styles.tryAddStyles(sb, Styles.get().comboBox(), Styles.get().comboBoxWidth180(), Button.FORCED_LEGACY_BORDERS_STYLE);
        sb.setSelectedData(MENU_KEY, values[0][0]);
        return sb;
    }

    private void fillMenu(Menu menu, String[][] values) {
        for (String[] value : values) {
            menu.add(new MenuItem(value[1]).withData(MENU_KEY, value[0]));
        }
    }

    private Panel add(Panel panel, String label, Widget widget) {
        final FlowPanel p = new FlowPanel();
        p.setStyleName(MM_CHARTCENTER_FORM_ITEM_STYLE);
        if (label != null) {
            addFormLabel(p, label);
        }
        p.add(widget);
        addFloatClean(p);
        panel.add(p);
        return p;
    }

    private Panel addCheckBox(Panel panel, String label, CheckBox widget) {
        return addCheckBox(panel, widget, widget.createSpan(SafeHtmlUtils.fromString(label)));
    }

    private Panel addCheckBox(Panel panel,
            CheckBox widget, Widget label) {
        final FlowPanel p = new FlowPanel();
        p.setStyleName(MM_CHARTCENTER_FORM_ITEM_STYLE);
        label.addStyleName(MM_CHARTCENTER_FORM_LABEL_STYLE);
        p.add(label);
        p.add(widget);
        addFloatClean(p);
        panel.add(p);
        return p;
    }

    private Panel addCheckBox(Panel panel, String label, Widget[]... widgets) {
        final FlowPanel p = new FlowPanel();
        p.setStyleName(MM_CHARTCENTER_FORM_ITEM_STYLE);
        addFormLabel(p, label);
//        boolean first = true;

        final FlexTable flexTable = new FlexTable();
        flexTable.setCellSpacing(0);
        flexTable.setCellPadding(0);
        flexTable.setStyleName("mm-chartcenter-form-item-multiples");
        p.add(flexTable);

        int i = 0;

        for (Widget[] ws : widgets) {
            /*if (!first) {
                horizontalPanel.add(new InlineHTML("&nbsp;&nbsp;")); // $NON-NLS-0$
            }
            else {
                first = false;
            }*/
            for (Widget w : ws) {
                flexTable.setWidget(0, i++, w);
            }
        }
//        addFloatClean(p);
        panel.add(p);
        return p;
    }

    private void addFormLabel(FlowPanel panel, String text) {
        final FlexLabel label = new FlexLabel(Document.get().createLabelElement(), text, true);
        label.setStyleName(MM_CHARTCENTER_FORM_LABEL_STYLE);
        panel.add(label);
    }

    private void addFloatClean(Panel panel) {
        final Label label = new Label();
        label.setStyleName("x-form-clear-left"); // $NON-NLS-0$
        panel.add(label);
    }

    private void redraw() {
        if (!validateDates()) {
            return;
        }
        setConfigParameters();
        this.snippet.ackParametersChanged();
    }

    private boolean validateDates() {
        return !this.dateFieldFrom.isEnabled() || this.dateFieldFrom.isValid() && this.dateFieldTo.isValid();
    }

    private CheckBox createCheckBox2() {
        return createCheckBox2(true);
    }

    private CheckBox createCheckBox2(
            boolean withReload) {
        final CheckBox cb = new CheckBox(false);
        if (withReload) {
            cb.addValueChangeHandler(event -> redraw());
        }
        return cb;
    }

    private SafeHtml getCheckBoxColorMarkLabel2(String label, String style) {
        return SafeHtmlUtils.fromTrustedString("&nbsp;<span class=\"" + style + "\">&nbsp;&nbsp;</span>&nbsp;" + label);   // $NON-NLS$
    }

    protected void onRender() {
        setConfigParameters(); // ensure legend can be rendered in printout w/o clicking redraw first
    }

    protected void initDragNDrop() {
        DropTarget target = new DropTarget(this.dragNdropPanel) {
            @Override
            protected void onDragEnter(DNDEvent e) {
                super.onDragEnter(e);
                e.getStatus().setStatus(canAcceptDrop(e));
            }
        };
        target.addDNDListener(new DNDListener() {
            @Override
            public void dragDrop(DNDEvent e) {
                onDrop(e);
            }
        });
        target.setGroup("ins"); // $NON-NLS-0$
        target.setOverStyle("drag-ok"); // $NON-NLS-0$
    }

    private void onDrop(DNDEvent e) {
        if (canAcceptDrop(e)) {
            addCompare(e.getData());
        }
    }

    boolean canAcceptDrop(DNDEvent e) {
        final QuoteWithInstrument o = e.getData();
        return this.numCompareItems < MAX_NUM_COMPARE && o != null && !isCompareItem(o)
                && !this.snippet.isSameQuote(o);
    }

    private void setConfigParameters() {
        this.config.put("type", getSelectedData(this.cbType)); // $NON-NLS-0$
        setPeriodParameters();
        this.config.put("indicator", getValueOrNull(this.cbIndicator)); // $NON-NLS-0$
        this.config.put("volume", getValueOrNull(this.cbVolume, "true")); // $NON-NLS$
        this.config.put("logScales", getValueOrNull(this.cbLog, "true")); // $NON-NLS-0$ $NON-NLS-1$
        this.config.put("bid", getValueOrNull(this.cbBid, "true")); // $NON-NLS-0$ $NON-NLS-1$
        this.config.put("ask", getValueOrNull(this.cbAsk, "true")); // $NON-NLS-0$ $NON-NLS-1$
        this.config.put("underlying", getValueOrNullIfVisible(this.pUnderlying, this.cbUnderlying, "true")); // $NON-NLS-0$ $NON-NLS-1$
        setGdParameters();
        setBenchmarkParameters();
        if (this.cbChartSizeLarge.isChecked()) {
            this.config.put("chartsizelarge", "true"); //$NON-NLS$
            this.config.put("chartwidth", "840"); // $NON-NLS-0$ $NON-NLS-1$
            this.config.put("chartheight", "460"); // $NON-NLS-0$ $NON-NLS-1$
        }
        else {
            this.config.put("chartsizelarge", "false"); //$NON-NLS$
            this.config.put("chartwidth", "600"); // $NON-NLS-0$ $NON-NLS-1$
            this.config.put("chartheight", "260"); // $NON-NLS-0$ $NON-NLS-1$
        }
        this.config.put("chartStyle", getValueOrNull(this.cbChartPrintColor, "color")); // $NON-NLS-0$ $NON-NLS-1$
        if (isBviSelected()) {
            // TODO: forbid intraday periods for bvi
            this.config.put("bviPerformanceForFunds", "true"); // $NON-NLS-0$ $NON-NLS-1$
            return;
        }
        this.config.remove("bviPerformanceForFunds"); // $NON-NLS-0$
        this.config.put("percent", getPercentValue()); // cbAbsolute and cbPercent // $NON-NLS-0$
        this.config.put("currency", getValueOrNull(this.cbCurrency)); // $NON-NLS-0$
        this.config.put("blendCorporateActions", Boolean.toString(cbCorporateActions.isChecked())); // $NON-NLS-0$
        this.config.put("blendDividends", Boolean.toString(cbDividends.isChecked())); // $NON-NLS-0$
        this.config.put("splits", getValueOrNullIfVisible(this.pSplits, this.cbSplits, "true")); // $NON-NLS-0$ $NON-NLS-1$
        this.config.put("hilo", !(this.config.getString("benchmark", "").length() > 0)); // $NON-NLS$
        this.config.put("adjustFrom", Boolean.toString(cbAdjustFrom.isChecked()));  // $NON-NLS-0$
        if (FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled()
                && lmePanel.isVisible()
                && !KEY_NONE.equals(getSelectedData(this.cbLmeField))) {
            this.config.put("mainField", getSelectedData(this.cbLmeField));  // $NON-NLS$
        }
        else {
            this.config.remove("mainField");  // $NON-NLS$
        }
    }

    private static String getSelectedData(SelectButton cbType) {
        return (String) cbType.getData(MENU_KEY);
    }

    private String getPercentValue() {
        if (this.cbAbsolute.isChecked()) {
            return "false"; // $NON-NLS-0$
        }
        if (this.cbPercent.isChecked()) {
            return "true"; // $NON-NLS-0$
        }
        return null;
    }

    private void setPeriodParameters() {
        final String period = getSelectedData(this.cbPeriod);

        if (PERIOD_KEY_FROM_TO.equals(period)) {
            this.snippet.setPeriod(
                    JsDateFormatter.formatDdmmyyyy(this.dateFieldFrom.getDate()),
                    JsDateFormatter.formatDdmmyyyy(this.dateFieldTo.getDate())
            );
        }
        else {
            this.snippet.setPeriod(period);
        }
    }

    private void setBenchmarkParameters() {
        final StringBuilder b = new StringBuilder();
        final StringBuilder bc = new StringBuilder();

        final String value = getValueOrNull(this.cbBenchmark);
        if (value != null) {
            append(b, value);
            append(bc, "bm1"); // $NON-NLS-0$
        }
        for (int i = 0; i < this.numCompareItems; i++) {
            if (this.cbCompareItem[i].isChecked()) {
                append(b, this.compareItems[i].getId());
                append(bc, getBenchmarkColor(i));
            }
        }

        this.config.put("benchmark", getValueOrNull(b)); // $NON-NLS-0$
        this.config.put("benchmarkColor", getValueOrNull(bc)); // $NON-NLS-0$
    }

    public static String getBenchmarkColor(int i) {
        return "bm" + (i + 2); // $NON-NLS$
    }

    public List<String> getBenchmarkNames() {
        final List<String> list = new ArrayList<>();
        final String display = getDisplayOrNull(this.cbBenchmark);
        if (display != null) {
            list.add(display);
        }
        for (int i = 0; i < this.numCompareItems; i++) {
            if (this.cbCompareItem[i].isChecked()) {
                list.add(this.compareItems[i].getInstrumentData().getName());
            }
        }

        return list;
    }

    private void setGdParameters() {
        StringBuilder gd = new StringBuilder();
        StringBuilder gdc = new StringBuilder();
        for (int i = 0; i < this.cbGd.length; i++) {
            if (this.cbGd[i].isChecked()) {
                append(gd, GD_NAMES[i]);
                append(gdc, "gd" + (i + 1)); // $NON-NLS-0$
            }
        }
        this.config.put("gd", getValueOrNull(gd)); // $NON-NLS-0$
        this.config.put("gdColor", getValueOrNull(gdc)); // $NON-NLS-0$
    }

    private void append(StringBuilder sb, String value) {
        if (sb.length() > 0) {
            sb.append(";"); // see de.marketmaker.istar.merger.web.easytrade.chart.BaseImgCommand#VALUE_SEPARATOR // $NON-NLS-0$
        }
        sb.append(value);
    }


    private String getValueOrNull(StringBuilder sb) {
        return sb.length() > 0 ? sb.toString() : null;
    }

    private String getValueOrNull(SelectButton sb) {
        if (!sb.isEnabled()) {
            return null;
        }
        final String value = getSelectedData(sb);
        return KEY_NONE.equals(value) ? null : value;
    }

    private String getValueOrNull(CheckBox cb,
            String value) {
        return (cb.isEnabled() && cb.isChecked()) ? value : null;
    }

    private String getValueOrNullIfVisible(Panel p,
            CheckBox cb, String value) {
        return (p.isVisible() && cb.isEnabled() && cb.isChecked()) ? value : null;
    }

    private String getDisplayOrNull(SelectButton sb) {
        if (!sb.isEnabled()) {
            return null;
        }
        final String value = getSelectedData(sb);
        return KEY_NONE.equals(value) ? null : sb.getSelectedItem().getHtml();
    }

    private void setDateByPeriod() {
        final String period = getSelectedData(this.cbPeriod);

        if (period == null || PERIOD_KEY_FROM_TO.equals(period)) {
            disableDateFromTo(false);
            return;
        }

        checkFieldsEnabled();
        disableDateFromTo(true);
        final MmJsDate dateTo = new MmJsDate();
        if (PERIOD_KEY_YEAR_TO_DATE.equals(period)) {
            this.dateFieldTo.setDate(new MmJsDate(dateTo));
            this.dateFieldFrom.setDate(new MmJsDate(dateTo.getFullYear(), 0, 1));
        }
        else if (!PERIOD_KEY_ALL.equals(period)) {
            this.dateFieldTo.setDate(new MmJsDate(dateTo));
            final MmJsDate dateFrom = MmJsDateUtil.minus(dateTo, period);
            this.dateFieldFrom.setDate(new MmJsDate(dateFrom));
        }
    }

    private boolean isIntradaySelected() {
        final String period = getSelectedData(this.cbPeriod);
        return period != null && period.endsWith("D"); // $NON-NLS-0$
    }

    // enable/disable elements based on whether an intraday chart is requested

    private void checkFieldsEnabled() {
        final boolean intradaySelected = isIntradaySelected();
        final boolean bviSelected = isBviSelected();

        this.cbBid.setEnabled(intradaySelected);
        this.cbAsk.setEnabled(intradaySelected);
        for (CheckBox box : this.cbGd) {
            box.setEnabled(!intradaySelected);
        }
        this.cbIndicator.setEnabled(!intradaySelected);
        this.cbCorporateActions.setEnabled(!(intradaySelected || bviSelected));
        this.cbDividends.setEnabled(!(intradaySelected || bviSelected));
        this.cbCurrency.setEnabled(this.convertibleCurrency && !bviSelected);
        this.cbAbsolute.setEnabled(!bviSelected);
        this.cbPercent.setEnabled(!bviSelected);
        this.cbSplits.setEnabled(!intradaySelected);
    }

    private void disableDateFromTo(boolean disable) {
        this.dateFieldFrom.setEnabled(!disable);
        this.dateFieldTo.setEnabled(!disable);
    }

    public void setType(InstrumentTypeEnum type) {
        this.cbUnderlying.setChecked(false);
        if (this.instrumentType != type) {
            this.cbDividends.setChecked(false);
            this.cbDividendsLabel.setText((type != InstrumentTypeEnum.STK
                    ? I18n.I.chartOptionEvaluateDistributionForNonStocks()
                    : I18n.I.chartOptionEvaluateDistributionForStocks()) +
                    " " + I18n.I.chartOptionHistoricPricesAdapted()
            );
            this.instrumentType = type;
        }
    }

    private void modifyCompare(int buttonId) {
        if (buttonId >= this.numCompareItems) {
            addCompare();
        }
        else {
            this.numCompareItems--;
            for (int i = buttonId; i < this.numCompareItems; i++) {
                this.cbCompareItem[i].setChecked(this.cbCompareItem[i + 1].isChecked(), false);
                this.compareItems[i] = this.compareItems[i + 1];
                this.compareSymbols[i].setText(getDisplayName(this.compareItems[i]));
                this.btnCompareItem[i].setIcon(MM_MINUS_STYLE);
                Tooltip.addQtip(this.btnCompareItem[i], I18n.I.removeComparisonValue());
            }

            for (int i = this.numCompareItems; i < MAX_NUM_COMPARE; i++) {
                this.spanCompareColor[i].setVisible(false);
                this.cbCompareItem[i].setChecked(false, false);
                this.cbCompareItem[i].setVisible(false);
                this.compareSymbols[i].setText(""); // $NON-NLS-0$
                this.compareItems[i] = null;
                this.btnCompareItem[i].setIcon(MM_PLUS_STYLE);
                this.btnCompareItem[i].setVisible(i == this.numCompareItems);
                Tooltip.addQtip(this.btnCompareItem[i], I18n.I.addComparisonValue());
            }
            setCmpDisabled(false);
            hideMainInstrumentPanel();
            redraw();
        }
    }

    private void hideMainInstrumentPanel() {
        if (this.numCompareItems == 0) {
            this.mainQuotePanel.setVisible(false);
        }
    }

    private void addCompare() {
        final SnippetConfigurationView configView = new SnippetConfigurationView(this);
        configView.addSelectSymbol(null);
        configView.show();
    }

    public HashMap<String, String> getCopyOfParameters() {
        return new HashMap<>();
    }

    public void setParameters(HashMap<String, String> params) {
        final QuoteWithInstrument qwi = QuoteWithInstrument.getLastSelected();
        if (qwi != null) {
            addCompare(qwi);
        }
    }

    private boolean isCompareItem(QuoteWithInstrument qwi) {
        for (int i = 0; i < this.numCompareItems; i++) {
            if (this.compareItems[i] != null && this.compareItems[i].getId().equals(qwi.getId())) {
                return true;
            }
        }
        return false;
    }

    boolean addCompare(QuoteWithInstrument qwi) {
        return addCompare(qwi, true);
    }

    boolean addCompare(QuoteWithInstrument qwi, boolean withRedraw) {
        if (isCompareItem(qwi)) {
            return false;
        }

        final int row = this.numCompareItems++;

        this.spanCompareColor[row].setVisible(true);
        this.cbCompareItem[row].setEnabled(true);
        this.cbCompareItem[row].setVisible(true);
        this.cbCompareItem[row].setChecked(true, false);
        this.compareSymbols[row].setText(getDisplayName(qwi));
        this.compareItems[row] = qwi;
        this.btnCompareItem[row].setIcon(IconImage.get(MM_MINUS_STYLE));
        Tooltip.addQtip(this.btnCompareItem[row], I18n.I.removeComparisonValue());

        if (this.numCompareItems < MAX_NUM_COMPARE) {
            this.btnCompareItem[this.numCompareItems].setIcon(IconImage.get(MM_PLUS_STYLE));
            this.btnCompareItem[this.numCompareItems].setVisible(true);
            Tooltip.addQtip(this.btnCompareItem[this.numCompareItems], I18n.I.addComparisonValue());
        }

        setCmpDisabled(this.numCompareItems == MAX_NUM_COMPARE);
        this.mainQuotePanel.setVisible(true);
        if (withRedraw) {
            redraw();
        }
        return true;
    }

    private void setCmpDisabled(boolean disabled) {
        BrowserSpecific.INSTANCE.setStyle(this.dragNdropPanel, MM_CHARTCENTER_DD_STYLE, disabled ? "nodrop" : null); // $NON-NLS$
    }

    public void update(IMGResult ipr, Boolean isIntraday) {
        final InstrumentData instrumentdata = ipr.getInstrumentdata();
        final QuoteData quotedata = ipr.getQuotedata();
        final String instrumentType = instrumentdata.getType();
        final boolean typeFND = "FND".equals(instrumentType); // $NON-NLS-0$
        this.pBvi.setVisible(typeFND);
        this.pSplits.setVisible("STK".equals(instrumentType)); // $NON-NLS-0$
        this.convertibleCurrency = isConvertibleCurrency(quotedata.getCurrencyIso());
        this.cbCurrency.setEnabled(this.convertibleCurrency && !isBviSelected());
        QuoteWithInstrument qwi = new QuoteWithInstrument(instrumentdata, quotedata);
        this.mainQuoteLabel.setText(getDisplayName(qwi));

        final String[][] oldPeriods = shortPeriods;
        if (isIntraday) {
            shortPeriods = shortPeriodsIntraday;
        }
        else {
            shortPeriods = shortPeriodsNoIntraday;
        }
        //Update the periods only if the value has changed since the last update.
        if (oldPeriods != shortPeriods) {
            reloadShortPeriodComboBox();
        }
    }

    private void reloadShortPeriodComboBox() {
        initShortPeriodComboBox();
        setDateByPeriod();
    }

    private void initShortPeriodComboBox() {
        final Menu menu = this.cbPeriod.getMenu();
        menu.clear();
        fillMenu(menu, shortPeriods);
        selectPeriod();
    }

    private void selectPeriod() {
        if (!this.cbPeriod.setSelectedData(MENU_KEY, getPeriodFromConfigOrDefault())) {
            this.cbPeriod.setSelectedData(MENU_KEY, DEFAULT_PERIOD);
        }
    }

    private String getPeriodFromConfigOrDefault() {
        String period = getPeriodKeyFromConfig();
        return StringUtil.hasText(period) ? period : DEFAULT_PERIOD;
    }

    private boolean isConvertibleCurrency(String currency) {
        return currency != null
                && !("XXP".equals(currency) || "XXZ".equals(currency)) // $NON-NLS$
                && currency.length() == 3;
    }

    private boolean isBviSelected() {
        return this.pBvi.isVisible() && this.cbBvi.isChecked();
    }

    public static String[][] getChartTypes() {
        return chartTypes;
    }

    public static String[][] getShortPeriods() {
        return shortPeriods;
    }

    public void setUnderlyingIsBasket() {
        this.compareSymbols[0].setText(I18n.I.underlyingNotAvailableItIsBasket());
        this.cbCompareItem[0].setEnabled(false);
        this.cbCompareItem[0].setChecked(false, false);
        this.pUnderlying.setVisible(false);
    }

    public void setUnderlying(QuoteWithInstrument underlying) {
        if (underlying == null) {
            this.compareSymbols[0].setText(I18n.I.underlyingNotAvailable());
            this.cbCompareItem[0].setEnabled(false);
            this.cbCompareItem[0].setChecked(false, false);
        }
        else {
            this.compareItems[0] = underlying;
            this.compareSymbols[0].setText(getDisplayName(underlying));
            this.cbCompareItem[0].setEnabled(true);
        }
        this.pUnderlying.setVisible(underlying != null);
    }

    private String getDisplayName(QuoteWithInstrument qwi) {
        return qwi.getInstrumentData().getName() + ", " + qwi.getQuoteData().getMarketVwd(); // $NON-NLS-0$
    }

    protected void updatePeriod() {
        selectPeriod();
        setDateByPeriod();
    }

    protected void updatePercent() {
        final String percent = this.config.getString("percent", null); // $NON-NLS$
        this.cbAbsolute.setChecked("false".equals(percent)); // $NON-NLS$
        this.cbPercent.setChecked("true".equals(percent)); // $NON-NLS$
    }

    private String getPeriodKeyFromConfig() {
        if ("start".equals(this.config.getString("from"))) { // $NON-NLS-0$ $NON-NLS-1$
            return PERIOD_KEY_ALL;
        }
        else if ("today".equals(this.config.getString("to"))) { // $NON-NLS-0$ $NON-NLS-1$
            return PERIOD_KEY_YEAR_TO_DATE;
        }
        return this.config.getString("period"); // $NON-NLS-0$
    }

    public void updateCurrency() {
        final String currency = this.config.getString("currency", KEY_NONE); // $NON-NLS-0$
        for (String[] supportedCurrency : supportedCurrencies) {
            if (currency.equals(supportedCurrency[0])) {
                this.cbCurrency.setSelectedData(MENU_KEY, supportedCurrency[0]);
                break;
            }
        }
    }

    private void addReloadListener(SelectButton sb) {
        sb.addSelectionHandler(e -> redraw());
    }

    private void addReloadListener(DateBox dateBox) {
        dateBox.addValueChangeHandler(e -> redraw());
    }

    private void addReloadListener(CheckBox... checkBoxes) {
        for (final CheckBox box : checkBoxes) {
            box.addValueChangeHandler(e -> redraw());
        }
    }

    public void setCompareItems(QuoteWithInstrument[] qwis) {
        Firebug.log("setCompareItems - " + qwis.length);

        this.numCompareItems = Math.min(qwis.length, MAX_NUM_COMPARE);
        for (int i = 0; i < this.numCompareItems; i++) {
            setCompareItem(i, qwis[i]);
        }
        for (int i = this.numCompareItems; i < MAX_NUM_COMPARE; i++) {
            setCompareItem(i, null);
        }
        setCmpDisabled(this.numCompareItems == MAX_NUM_COMPARE);
        this.mainQuotePanel.setVisible(true);
    }

    private void setCompareItem(int row, QuoteWithInstrument qwi) {
        this.compareItems[row] = qwi;
        this.cbCompareItem[row].setEnabled(true);
        if (qwi == null) {
            this.spanCompareColor[row].setVisible(false);
            this.cbCompareItem[row].setVisible(false);
            this.cbCompareItem[row].setChecked(false, false);
            this.compareSymbols[row].setText("");
            this.btnCompareItem[row].setIcon(IconImage.get(MM_PLUS_STYLE));
            this.btnCompareItem[row].setVisible(row <= this.numCompareItems);
            Tooltip.addQtip(this.btnCompareItem[row], I18n.I.addComparisonValue());
        }
        else {
            this.spanCompareColor[row].setVisible(true);
            this.cbCompareItem[row].setVisible(true);
            this.cbCompareItem[row].setChecked(true, false);
            this.compareSymbols[row].setText(getDisplayName(qwi));
            this.btnCompareItem[row].setIcon(IconImage.get(MM_MINUS_STYLE));
            this.btnCompareItem[row].setVisible(true);
            Tooltip.addQtip(this.btnCompareItem[row], I18n.I.removeComparisonValue());
        }

    }

    public void setAdjustedFrom(MmJsDate adjustFromDate) {
        getFlexCellFormatter().setVisible(0, 0, true);
        this.cbPeriod.setSelectedData(MENU_KEY, PERIOD_KEY_FROM_TO);
        this.disableDateFromTo(false);
        this.dateFieldFrom.setDate(adjustFromDate, false); // don't fire an event
        new Timer() {
            @Override
            public void run() {
                getFlexCellFormatter().setVisible(0, 0, false);
            }
        }.schedule(MESSAGE_DISPLAYTIME);
    }

    public void setPriceType(String priceType) {
        this.lmePanel.setVisible(FeatureFlags.Feature.LME_CHANGES_2014.isEnabled() && "lme".equals(priceType)); // $NON-NLS-0$
    }
}
