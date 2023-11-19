/*
 * FinderFNDView.java
 *
 * Created on 11.06.2008 13:43:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SelectorVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChangeRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LargeNumberRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PercentRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PriceStringRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderSTKView<F extends AbstractFinder> extends AbstractFinderView<F> {
    private static final TableCellRenderer STRING_22 = new MaxLengthStringRenderer(22, ""); // $NON-NLS$

    FinderSTKView(F controller) {
        super(controller);
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        assert 5 == columnModels.length;

        final QuoteLinkRenderer quoteLinkRenderer = new QuoteLinkRenderer(32, "&nbsp;"); // $NON-NLS$
        final DelegateRenderer<String> largeNumberRenderer = new DelegateRenderer<String>(new LargeNumberRenderer("", Renderer.LARGE_NUMBER_LABELS), "mm-right"); // $NON-NLS$ $NON-NLS-1$
        final PercentRenderer percent = new PercentRenderer(""); // $NON-NLS$
        final DelegateRenderer<String> changePercentRenderer = new DelegateRenderer<String>(new ChangeRenderer(percent), "mm-right"); // $NON-NLS$
        final DelegateRenderer<String> compactDateTimeRenderer = new DelegateRenderer<String>(DateRenderer.compactDateTime("")); // $NON-NLS$
        final PriceStringRenderer price = new PriceStringRenderer(StringBasedNumberFormat.DEFAULT, ""); // $NON-NLS$
        final DelegateRenderer<String> priceRenderer = new DelegateRenderer<String>(price, "mm-right"); // $NON-NLS$
        final DelegateRenderer<String> priceMax2Renderer = new DelegateRenderer<String>(new PriceStringRenderer(StringBasedNumberFormat.ROUND_0_2, ""), "mm-right"); // $NON-NLS$ $NON-NLS-1$
        final DelegateRenderer<String> changeNetRenderer = new DelegateRenderer<String>(price, "mm-right"); // $NON-NLS$
        final DelegateRenderer<String> pctNoSuffixRenderer = new TableCellRenderers.DelegateRenderer<String>(new PercentRenderer("", false), "mm-right"); // $NON-NLS$ $NON-NLS-1$
        final DelegateRenderer<String> percentRenderer = new DelegateRenderer<String>(percent, "mm-right"); // $NON-NLS$

        final VisibilityCheck dzBankLink = SimpleVisibilityCheck.valueOf(Permutation.GIS.isActive());
        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());
        final VisibilityCheck vwd2014 = SimpleVisibilityCheck.valueOf(FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled());

        columnModels[0] = new TableColumnModelBuilder().addColumns(
                new TableColumn("RS", -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK) // $NON-NLS$
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch()))
                , new TableColumn(I18n.I.info(), 20, TableCellRenderers.VR_ICON_LINK).withVisibilityCheck(dzBankLink)
                , new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS$
                , new TableColumn(I18n.I.name(), 220, quoteLinkRenderer, "name")  // $NON-NLS$
                , new TableColumn(I18n.I.paid(), -1f, priceRenderer) 
                , new TableColumn(I18n.I.volumeTrade(), -1f, largeNumberRenderer) 
                , new TableColumn("+/-", -1f, changeNetRenderer).alignRight() // $NON-NLS$
                , new TableColumn("+/-%", -1f, changePercentRenderer).alignRight() // $NON-NLS$
                , new TableColumn(I18n.I.totalTurnoverAbbr(), -1f, largeNumberRenderer) 
                , new TableColumn(I18n.I.bid(), -1f, priceRenderer) 
                , new TableColumn(I18n.I.ask(), -1f, priceRenderer) 
                , new TableColumn(I18n.I.date(), -1f, compactDateTimeRenderer).alignRight() 
                , new TableColumn(I18n.I.marketName(), -1f).alignRight()
                , new TableColumn(I18n.I.marketCapitalizationAbbr(), -1f, largeNumberRenderer, "marketCapitalization")  // $NON-NLS$
                , new TableColumn(I18n.I.marketCapitalizationEURAbbr(), -1f, largeNumberRenderer, "marketCapitalizationEUR").withVisibilityCheck(vwd2014)  // $NON-NLS$
                , new TableColumn(I18n.I.marketCapitalizationUSDAbbr(), -1f, largeNumberRenderer, "marketCapitalizationUSD").withVisibilityCheck(vwd2014)  // $NON-NLS$
                , new TableColumn(I18n.I.sector(), -1f, STRING_22)
        ).asTableColumnModel();

        columnModels[1] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS$
                , new TableColumn(I18n.I.name(), 220, quoteLinkRenderer, "name")  // $NON-NLS$
        ).addGroup(I18n.I.performanceInPercentAbbr()
                , new TableColumn(I18n.I.yearToDate(), -1f, pctNoSuffixRenderer, "performanceCurrentYear")  // $NON-NLS$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, pctNoSuffixRenderer, "performance1w")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, pctNoSuffixRenderer, "performance1m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, pctNoSuffixRenderer, "performance3m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, pctNoSuffixRenderer, "performance6m")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, pctNoSuffixRenderer, "performance1y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, pctNoSuffixRenderer, "performance3y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, pctNoSuffixRenderer, "performance5y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, pctNoSuffixRenderer, "performance10y")  // $NON-NLS$
        ).addGroup(I18n.I.differenceInPercentTo()
                , new TableColumn(I18n.I.high52wAbbr(), -1f, pctNoSuffixRenderer, "changePercentHigh1y")  // $NON-NLS$
                , new TableColumn(I18n.I.low52wAbbr(), -1f, pctNoSuffixRenderer, "changePercentLow1y")  // $NON-NLS$
                , new TableColumn(I18n.I.highAlltimeAbbr(), -1f, pctNoSuffixRenderer, "changePercentHighAlltime")  // $NON-NLS$
        ).addGroup(I18n.I.averageVolume()
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, largeNumberRenderer, "averageVolume1w")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, largeNumberRenderer, "averageVolume3m")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, largeNumberRenderer, "averageVolume1y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, largeNumberRenderer, "averageVolume3y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, largeNumberRenderer, "averageVolume5y")  // $NON-NLS$
        ).asTableColumnModel();


        columnModels[2] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS$
                , new TableColumn(I18n.I.name(), 220, quoteLinkRenderer, "name")  // $NON-NLS$
        ).addGroup(I18n.I.volaInPercentAbbr()
                , new TableColumn(I18n.I.yearToDate(), -1f, pctNoSuffixRenderer, "volatilityCurrentYear")  // $NON-NLS$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, pctNoSuffixRenderer, "volatility1w")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, pctNoSuffixRenderer, "volatility1m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, pctNoSuffixRenderer, "volatility3m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, pctNoSuffixRenderer, "volatility6m")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, pctNoSuffixRenderer, "volatility1y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, pctNoSuffixRenderer, "volatility3y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, pctNoSuffixRenderer, "volatility5y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, pctNoSuffixRenderer, "volatility10y")  // $NON-NLS$
        ).addGroup(I18n.I.betaInPercent()
                , new TableColumn("30", -1f, pctNoSuffixRenderer, "beta1m") // $NON-NLS$
                , new TableColumn("250", -1f, pctNoSuffixRenderer, "beta1y") // $NON-NLS$
        ).addGroup(I18n.I.alphaInPercent()
                , new TableColumn("30", -1f, pctNoSuffixRenderer, "alpha1m") // $NON-NLS$
                , new TableColumn("250", -1f, pctNoSuffixRenderer, "alpha1y") // $NON-NLS$
        ).asTableColumnModel();

        final String y1 = "'" + LiveFinderSTK.YEAR1.substring(2); // $NON-NLS$
        final String y2 = "'" + LiveFinderSTK.YEAR2.substring(2); // $NON-NLS$
        columnModels[3] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS$
                , new TableColumn(I18n.I.name(), 220, quoteLinkRenderer, "name")  // $NON-NLS$
        ).addGroup(I18n.I.priceEarningRatioShortcut()
                , new TableColumn(y1, -1f, priceMax2Renderer, "priceEarningRatio1y") // $NON-NLS$
                , new TableColumn(y2, -1f, priceMax2Renderer, "priceEarningRatio2y") // $NON-NLS$
        ).addGroup(I18n.I.priceSalesRatioAbbr1()
                , new TableColumn(y1, -1f, priceMax2Renderer, "priceSalesRatio1y") // $NON-NLS$
                , new TableColumn(y2, -1f, priceMax2Renderer, "priceSalesRatio2y") // $NON-NLS$
        ).addGroup(I18n.I.priceCashflowRatioAbbr1()
                , new TableColumn(y1, -1f, priceMax2Renderer, "priceCashflowRatio1y") // $NON-NLS$
                , new TableColumn(y2, -1f, priceMax2Renderer, "priceCashflowRatio2y") // $NON-NLS$
        ).addGroup(I18n.I.priceBookvalueRatioAbbr1()
                , new TableColumn(y1, -1f, priceMax2Renderer, "priceBookvalueRatio1y").withVisibilityCheck(new SelectorVisibilityCheck(Selector.FACTSET)) // $NON-NLS$
                , new TableColumn(y2, -1f, priceMax2Renderer, "priceBookvalueRatio2y").withVisibilityCheck(new SelectorVisibilityCheck(Selector.FACTSET)) // $NON-NLS$
        ).addGroup(I18n.I.dividendYieldAbbrNoBreak()
                , new TableColumn(y1, -1f, percentRenderer, "dividendYield1y") // $NON-NLS$
                , new TableColumn(y2, -1f, percentRenderer, "dividendYield2y") // $NON-NLS$
        ).addGroup(I18n.I.volumeMio()
                , new TableColumn(y1, -1f, largeNumberRenderer, "sales1y") // $NON-NLS$
                , new TableColumn(y2, -1f, largeNumberRenderer, "sales2y") // $NON-NLS$
        ).addGroup(I18n.I.profitMioAbbr()
                , new TableColumn(y1, -1f, largeNumberRenderer, "profit1y") // $NON-NLS$
                , new TableColumn(y2, -1f, largeNumberRenderer, "profit2y") // $NON-NLS$
        ).addGroup(I18n.I.ebitMio()
                , new TableColumn(y1, -1f, largeNumberRenderer, "ebit1y") // $NON-NLS$
                , new TableColumn(y2, -1f, largeNumberRenderer, "ebit2y") // $NON-NLS$
        ).addGroup(I18n.I.ebitdaMio()
                , new TableColumn(y1, -1f, largeNumberRenderer, "ebitda1y") // $NON-NLS$
                , new TableColumn(y2, -1f, largeNumberRenderer, "ebitda2y") // $NON-NLS$
        ).addColumns(
                new TableColumn(I18n.I.screenerRatingWithNewline(), -1f, DEFAULT_CENTER, "screenerInterest").withVisibilityCheck(new SelectorVisibilityCheck(Selector.SCREENER))  // $NON-NLS$
                , new TableColumn(I18n.I.trRecommendationWithNewline(), -1f, priceMax2Renderer, "recommendation").withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDzProfitEstimate())).alignCenter()  // $NON-NLS$
        ).asTableColumnModel();

        columnModels[4] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1).withVisibilityCheck(showWknCheck) // $NON-NLS$
                , new TableColumn("ISIN", -1).withVisibilityCheck(showIsinCheck) // $NON-NLS$
                , new TableColumn(I18n.I.name(), 220, quoteLinkRenderer, "name")  // $NON-NLS$
        ).addGroup(I18n.I.differenceToBenchmarkInPercent()
                , new TableColumn(I18n.I.yearToDate(), -1f, pctNoSuffixRenderer, "performanceToBenchmarkCurrentYear")  // $NON-NLS$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, pctNoSuffixRenderer, "performanceToBenchmark1w")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, pctNoSuffixRenderer, "performanceToBenchmark1m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, pctNoSuffixRenderer, "performanceToBenchmark3m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, pctNoSuffixRenderer, "performanceToBenchmark6m")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, pctNoSuffixRenderer, "performanceToBenchmark1y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, pctNoSuffixRenderer, "performanceToBenchmark3y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, pctNoSuffixRenderer, "performanceToBenchmark5y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, pctNoSuffixRenderer, "performanceToBenchmark10y")  // $NON-NLS$
        ).addGroup(I18n.I.correlationPercent()
                , new TableColumn(I18n.I.yearToDate(), -1f, pctNoSuffixRenderer, "correlationCurrentYear")  // $NON-NLS$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, pctNoSuffixRenderer, "correlation1w")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, pctNoSuffixRenderer, "correlation1m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, pctNoSuffixRenderer, "correlation3m")  // $NON-NLS$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, pctNoSuffixRenderer, "correlation6m")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, pctNoSuffixRenderer, "correlation1y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, pctNoSuffixRenderer, "correlation3y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, pctNoSuffixRenderer, "correlation5y")  // $NON-NLS$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, pctNoSuffixRenderer, "correlation10y")  // $NON-NLS$
        ).addColumns(
                new TableColumn(I18n.I.benchmark(), -1f, STRING_22)
        ).asTableColumnModel();
    }
}
