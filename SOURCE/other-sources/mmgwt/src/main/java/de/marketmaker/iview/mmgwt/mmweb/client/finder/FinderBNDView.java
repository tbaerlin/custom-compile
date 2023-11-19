/*
 * FinderBNDView.java
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
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderBNDView<F extends AbstractFinder> extends AbstractFinderView<F> {

    FinderBNDView(F controller) {
        super(controller);
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        assert 4 == columnModels.length;
        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());
        final VisibilityCheck showSmallestTransferableUnit = SimpleVisibilityCheck.valueOf(FeatureFlags.Feature.VWD_RELEASE_2014.isEnabled() ||
                FeatureFlags.Feature.DZ_RELEASE_2016.isEnabled());

        columnModels[0] = new TableColumnModelBuilder().addColumns(
                new TableColumn("RS", -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK) // $NON-NLS$
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch()))
                , new TableColumn(I18n.I.info(), -1f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI)
                , new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 170, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.marketName(), 100f, TableCellRenderers.STRING_CENTER)
                , new TableColumn(I18n.I.price(), -1f, PRICE, "price") // $NON-NLS-0$
                , new TableColumn("", -1f, TableCellRenderers.STRING_10) // $NON-NLS-0$
                , new TableColumn(I18n.I.bid(), -1f, PRICE)
                , new TableColumn(I18n.I.ask(), -1f, PRICE)
                , new TableColumn(I18n.I.turnover(), -1f, TableCellRenderers.TURNOVER)
                , new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE_RIGHT)
                , new TableColumn(I18n.I.time(), -1f, TableCellRenderers.COMPACT_TIME).alignRight()
                , new TableColumn(I18n.I.issuer(), 64f, TableCellRenderers.STRING_CENTER, "issuername")  // $NON-NLS-0$
                , new TableColumn(I18n.I.issuerCategoryAbbr(), 100f, TableCellRenderers.STRING_CENTER, "issuerCategory")  // $NON-NLS-0$
                , new TableColumn(I18n.I.sector(), 100f, TableCellRenderers.STRING_CENTER, "sector")  // $NON-NLS-0$
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.ANY_VWD_TERMINAL_PROFILE.isAllowed()))
                , new TableColumn(I18n.I.couponType(), 100f, TableCellRenderers.STRING_CENTER, "couponType")  // $NON-NLS-0$
                , new TableColumn(I18n.I.category(), 120f, TableCellRenderers.STRING_CENTER, "bondType")  // $NON-NLS-0$
                , new TableColumn(I18n.I.country(), 60f, TableCellRenderers.STRING_CENTER, "country")  // $NON-NLS-0$
                , new TableColumn(I18n.I.coupon(), -1f, TableCellRenderers.PERCENT, "coupon")  // $NON-NLS-0$
                , new TableColumn(I18n.I.maturity2(), -1f, TableCellRenderers.DATE_RIGHT, "expirationDate") // $NON-NLS-0$
                , new TableColumn(I18n.I.smallestTransferableUnit(), -1f, TableCellRenderers.PRICE, "smallestTransferableUnit"). // $NON-NLS-0$
                        withVisibilityCheck(showSmallestTransferableUnit)
        ).asTableColumnModel();

        columnModels[1] = new TableColumnModelBuilder().addColumns(
                new TableColumn("RS", -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK) // $NON-NLS$
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch()))
                , new TableColumn(I18n.I.info(), -1f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI)
                , new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 170, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.marketName(), 100f, TableCellRenderers.STRING_CENTER)
                , new TableColumn(I18n.I.price(), -1f, PRICE, "price") // $NON-NLS-0$
                , new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE).alignRight()
                , new TableColumn(I18n.I.yield(), -1f, PERCENT, "yieldRelativePerYear")  // $NON-NLS-0$
                , new TableColumn(I18n.I.modifiedDurationAbbr(), -1f, PRICE_2, "modifiedDuration")  // $NON-NLS-0$
                , new TableColumn(I18n.I.duration(), -1f, PRICE_2, "duration")  // $NON-NLS-0$
                , new TableColumn(I18n.I.accruedInterestAbbr(), -1f, PRICE_2, "accruedInterest")  // $NON-NLS-0$
                , new TableColumn(I18n.I.basePointValueAbbr(), -1f, PRICE_2, "basePointValue")  // $NON-NLS-0$
                , new TableColumn(I18n.I.convexityAbbr(), -1f, PRICE_2, "convexity")  // $NON-NLS-0$
                , new TableColumn(I18n.I.interestRateElasticityAbbr(), -1f, PRICE_2, "interestRateElasticity")  // $NON-NLS-0$
                , new TableColumn(I18n.I.fitch(), -1f, "ratingFitchLongTerm").alignCenter().withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.RATING_FITCH.isAllowed()))  // $NON-NLS-0$
                , new TableColumn(I18n.I.moodys(), -1f, "ratingMoodys").alignCenter().withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.RATING_MOODYS.isAllowed()))  // $NON-NLS-0$
        ).asTableColumnModel();

        columnModels[2] = new TableColumnModelBuilder().addColumns(
                new TableColumn("RS", -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK) // $NON-NLS$
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch()))
                , new TableColumn(I18n.I.info(), -1f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI)
                , new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 170, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.marketName(), 100f, TableCellRenderers.STRING_CENTER)
                , new TableColumn(I18n.I.price(), -1f, PRICE, "price")  // $NON-NLS-0$
                , new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE_RIGHT)
        ).addGroup(I18n.I.performanceInPercentAbbr()
                , new TableColumn(I18n.I.yearToDate(), -1f, PCT_NO_SUFFIX, "performanceCurrentYear")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, PCT_NO_SUFFIX, "performance1w")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, PCT_NO_SUFFIX, "performance1m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, PCT_NO_SUFFIX, "performance3m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, PCT_NO_SUFFIX, "performance6m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, PCT_NO_SUFFIX, "performance1y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, PCT_NO_SUFFIX, "performance3y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, PCT_NO_SUFFIX, "performance5y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, PCT_NO_SUFFIX, "performance10y")  // $NON-NLS-0$
        ).asTableColumnModel();

        columnModels[3] = new TableColumnModelBuilder().addColumns(
                new TableColumn("RS", -1f, TableCellRenderers.DZ_RESEARCH_POPUP_ICON_LINK) // $NON-NLS$
                        .withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDZResearch()))
                , new TableColumn(I18n.I.info(), -1f, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI)
                , new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 170, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.marketName(), 100f, TableCellRenderers.STRING_CENTER)
                , new TableColumn(I18n.I.price(), -1f, PRICE, "price")  // $NON-NLS-0$
                , new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE_RIGHT)
        ).addGroup(I18n.I.volatility()
                , new TableColumn(I18n.I.nMonthAbbr(3), -1, PCT_NO_SUFFIX, "volatility3m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1, PCT_NO_SUFFIX, "volatility6m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(1), -1, PCT_NO_SUFFIX, "volatility1y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1, PCT_NO_SUFFIX, "volatility3y")  // $NON-NLS-0$
        ).asTableColumnModel();
    }
}
