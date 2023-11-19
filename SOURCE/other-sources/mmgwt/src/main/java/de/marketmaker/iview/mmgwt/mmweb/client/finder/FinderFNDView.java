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
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModelBuilder;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE_2;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderFNDView<F extends AbstractFinder> extends AbstractFinderView<F> {
    private final static VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
    private final static VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());
    private final static VisibilityCheck showOgawCheck = SimpleVisibilityCheck.valueOf(Selector.DZ_BANK_USER.isAllowed() && FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled());



    private static final TableCellRenderer DIST_STRATEGY_RENDERER = new TableCellRendererAdapter() {
        public void render(Object data, StringBuffer sb, Context context) {
            final String s = (String) data;
            if (s != null) {
                if (s.startsWith(I18n.I.retaining())) {
                    sb.append(I18n.I.retainingAbbr());
                    return;
                }
                if (s.startsWith(I18n.I.distributing())) {
                    sb.append(I18n.I.distributingAbbr());
                    return;
                }
            }
            sb.append("?"); // $NON-NLS-0$
        }
    };

    public static final DefaultTableColumnModel BASE_COLUMN_MODEL
            = new TableColumnModelBuilder().addColumns(
            new TableColumn("WKN", -1f, "wkn").withVisibilityCheck(showWknCheck) // $NON-NLS-0$ $NON-NLS-1$
            , new TableColumn("ISIN", -1f, "isin").withVisibilityCheck(showIsinCheck) // $NON-NLS-0$ $NON-NLS-1$
            , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
            , new TableColumn(I18n.I.type(), -1f, TableCellRenderers.STRING, "fundtype")  // $NON-NLS-0$
            , new TableColumn(I18n.I.ogawAif(), -1f, TableCellRenderers.STRING, "wmInvestmentAssetPoolClass").withVisibilityCheck(showOgawCheck) // $NON-NLS$
            , new TableColumn(I18n.I.issuePrice2(), -1f, TableCellRenderers.PRICE)
            , new TableColumn(I18n.I.redemption(), -1f, TableCellRenderers.PRICE)
            , new TableColumn(I18n.I.date(), -1f, TableCellRenderers.COMPACT_DATETIME).alignRight()
            , new TableColumn(I18n.I.marketName(), -1f).alignRight()
            , new TableColumn(I18n.I.changeNetAbbr(), -1f, TableCellRenderers.CHANGE_NET)
            , new TableColumn(I18n.I.changePercentAbbr(), -1f, TableCellRenderers.CHANGE_PERCENT, "changePercent")  // $NON-NLS-0$
    ).asTableColumnModel();

    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    FinderFNDView(F controller) {
        super(controller);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        assert 4 == columnModels.length;

        columnModels[0] = BASE_COLUMN_MODEL;

        columnModels[1] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f, "wkn").withVisibilityCheck(showWknCheck) // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn("ISIN", -1f, "isin").withVisibilityCheck(showIsinCheck) // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
        ).addGroup(I18n.I.volaInPercentAbbr()
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, PCT_NO_SUFFIX, "volatility1y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, PCT_NO_SUFFIX, "volatility3y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, PCT_NO_SUFFIX, "volatility5y")  // $NON-NLS-0$
        ).addGroup(I18n.I.sharpeRatio()
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, PRICE_2, "sharpeRatio1y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, PRICE_2, "sharpeRatio3y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, PRICE_2, "sharpeRatio5y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, PRICE_2, "sharpeRatio10y")  // $NON-NLS-0$
        ).addColumns(
                new TableColumn(I18n.I.feri(), -1f, "ratingFeri").alignCenter().withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.RATING_FERI.isAllowed()))  // $NON-NLS-0$
                , new TableColumn(I18n.I.morningstar(), -1f, "ratingMorningstar").alignCenter().withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.RATING_MORNINGSTAR.isAllowed() || Selector.RATING_MORNINGSTAR_UNION_FND.isAllowed()))  // $NON-NLS-0$
                , new TableColumn(I18n.I.srri(), -1f, "srriValue").alignCenter().withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.FUNDDATA_VWD_BENL.isAllowed()))  // $NON-NLS$
                , new TableColumn(I18n.I.diamondRating(), -1f, "diamondRating").alignCenter().withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.FUNDDATA_VWD_BENL.isAllowed()))  // $NON-NLS$
        ).asTableColumnModel();

        columnModels[2] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f, "wkn").withVisibilityCheck(showWknCheck) // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn("ISIN", -1f, "isin").withVisibilityCheck(showIsinCheck) // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
        ).addGroup(I18n.I.bviPerformanceInPercentAbbr()
                , new TableColumn(I18n.I.nDayAbbr(1), -1f, PCT_NO_SUFFIX, "bviperformance1d")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, PCT_NO_SUFFIX, "bviperformance1w")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, PCT_NO_SUFFIX, "bviperformance1m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, PCT_NO_SUFFIX, "bviperformance3m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, PCT_NO_SUFFIX, "bviperformance6m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, PCT_NO_SUFFIX, "bviperformance1y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(3), -1f, PCT_NO_SUFFIX, "bviperformance3y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(5), -1f, PCT_NO_SUFFIX, "bviperformance5y")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(10), -1f, PCT_NO_SUFFIX, "bviperformance10y")  // $NON-NLS-0$
        ).addColumns(
                new TableColumn(I18n.I.maximumLossInThreeYears(), -1f, PERCENT, "maximumLoss3y")  // $NON-NLS-0$
        ).asTableColumnModel();

        columnModels[3] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f, "wkn").withVisibilityCheck(showWknCheck) // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn("ISIN", -1f, "isin").withVisibilityCheck(showIsinCheck) // $NON-NLS-0$ $NON-NLS-1$
                , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(
                    (I18n.I.assetManagementCompany()), 200, TableCellRenderers.STRING, "issuername")  // $NON-NLS-0$
                , new TableColumn(I18n.I.investmentFocusTwoLines(), 200, TableCellRenderers.STRING, "investmentFocus")  // $NON-NLS-0$
                , new TableColumn(I18n.I.volume(), -1, TableCellRenderers.LARGE_NUMBER, "fundVolume").alignRight()  // $NON-NLS-0$
                , new TableColumn(I18n.I.issueDate3(), -1, TableCellRenderers.DATE_RIGHT, "issueDate2")  // $NON-NLS-0$
                , new TableColumn(I18n.I.distributionStrategyTAAbbr(), -1, DIST_STRATEGY_RENDERER).withToolTip(I18n.I.distributionStrategyTA()).alignCenter()
                , new TableColumn(I18n.I.issueSurchargeTwoLine(), -1, PERCENT, "issueSurcharge")  // $NON-NLS-0$
                , new TableColumn(I18n.I.managementFeeTwoLine(), -1, PERCENT, "managementFee")  // $NON-NLS-0$
                , new TableColumn(I18n.I.depotAccountFeeTwoLine(), -1, PERCENT, "accountFee")  // $NON-NLS-0$
                , new TableColumn(I18n.I.ongoingCharges(), -1, PERCENT, "ogc")  // $NON-NLS-0$
                , new TableColumn(I18n.I.totalExpenseRatio(), -1, PERCENT, "ter")  // $NON-NLS-0$
        ).asTableColumnModel();
    }
}
