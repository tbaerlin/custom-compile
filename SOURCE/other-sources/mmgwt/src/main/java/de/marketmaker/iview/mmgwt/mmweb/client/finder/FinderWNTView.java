/*
 * FinderFNDView.java
 *
 * Created on 11.06.2008 13:43:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
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
class FinderWNTView<F extends AbstractFinder> extends AbstractFinderView<F> {
    FinderWNTView(F controller) {
        super(controller);
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        assert 4 == columnModels.length || 5 == columnModels.length;

        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());

        columnModels[0] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.paid(), -1f, PRICE) 
                , new TableColumn(I18n.I.bid(), -1f, PRICE) 
                , new TableColumn(I18n.I.ask(), -1f, PRICE) 
                , new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight() // $NON-NLS-0$
                , new TableColumn(I18n.I.date(), -1f, COMPACT_DATETIME).alignRight() 
                , new TableColumn(I18n.I.marketName(), -1f).alignRight()
                , new TableColumn(I18n.I.issuer(), -1f, "issuername")  // $NON-NLS-0$
        ).asTableColumnModel();

        columnModels[1] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
        ).addGroup(I18n.I.performanceInPercentAbbr()
                , new TableColumn(I18n.I.yearToDate(), -1f, PCT_NO_SUFFIX, "performanceCurrentYear")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nWeekAbbr(1), -1f, PCT_NO_SUFFIX, "performance1w")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, PCT_NO_SUFFIX, "performance1m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, PCT_NO_SUFFIX, "performance3m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(6), -1f, PCT_NO_SUFFIX, "performance6m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nYearAbbr(1), -1f, PCT_NO_SUFFIX, "performance1y")  // $NON-NLS-0$
        ).addGroup(I18n.I.volaInPercentAbbr()
                , new TableColumn(I18n.I.nMonthAbbr(1), -1f, PCT_NO_SUFFIX, "volatility1m")  // $NON-NLS-0$
                , new TableColumn(I18n.I.nMonthAbbr(3), -1f, PCT_NO_SUFFIX, "volatility3m")  // $NON-NLS-0$
        ).asTableColumnModel();

        columnModels[2] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.omega(), -1f, PRICE_MAX2, "omega")  // $NON-NLS-0$
                , new TableColumn(I18n.I.delta(), -1f, PRICE_MAX2, "delta")  // $NON-NLS-0$
                , new TableColumn(I18n.I.gamma(), -1f, PRICE_MAX2, "gamma")  // $NON-NLS-0$
                , new TableColumn(I18n.I.rho(), -1f, PRICE_MAX2, "rho")  // $NON-NLS-0$
                , new TableColumn(I18n.I.vega(), -1f, PRICE_MAX2, "vega")  // $NON-NLS-0$
                , new TableColumn(I18n.I.theta(), -1f, PRICE_MAX2, "theta")  // $NON-NLS-0$
                , new TableColumn(I18n.I.impliedVolatilityAbbr(), -1f, PERCENT, "impliedVolatility")  // $NON-NLS-0$
                , new TableColumn(I18n.I.intrinsicValue(), -1f, PRICE_MAX2, "intrinsicValue")  // $NON-NLS-0$
                , new TableColumn(I18n.I.fairValue(), -1f, PRICE_MAX2, "extrinsicValue")  // $NON-NLS-0$
                , new TableColumn(I18n.I.optionPrice(), -1f, PERCENT, "optionPrice")  // $NON-NLS-0$
                , new TableColumn(I18n.I.optionPricePerYear(), -1f, PERCENT, "optionPricePerYear")  // $NON-NLS-0$
                , new TableColumn(I18n.I.breakeven(), -1f, PRICE_MAX2, "breakeven")  // $NON-NLS-0$
                , new TableColumn(I18n.I.fairValue2(), -1f, PRICE_MAX2, "fairValue")  // $NON-NLS-0$
                , new TableColumn(I18n.I.leverage(), -1f, PRICE_MAX2, "leverage")  // $NON-NLS-0$
                , new TableColumn(I18n.I.spread(), -1f, PRICE_MAX2, "spread")  // $NON-NLS-0$
                , new TableColumn(I18n.I.spread() + "%", -1f, PERCENT, "spreadPercent")  // $NON-NLS$
        ).asTableColumnModel();

        columnModels[3] = new TableColumnModelBuilder().addColumns(
                new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.name(), 220, TableCellRenderers.QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn("C/P", -1f) // $NON-NLS-0$
                , new TableColumn(I18n.I.strike(), -1f, PRICE) 
                , new TableColumn(I18n.I.currency(), -1f) 
                , new TableColumn(I18n.I.maturity2(), -1f, DATE, "expirationDate")  // $NON-NLS-0$
                , new TableColumn(I18n.I.isAmerican(), -1f)
/*
        ).addGroup(I18n.I.underlying()
                , new TableColumn("Name", -1f)
                , new TableColumn("Symbol", -1f)
*/
        ).asTableColumnModel();

        /*
         * Remark: columnModels[4] will work in JavaScript code even if the size of the array is 4.
         *         As defined in the JavaScript-Spec, columnModels[columnModels.size] adds a new element to the array.
         *         Unfortunately, this will never work in hosted mode, because the JVM will throw an IndexOutOfBoundsException.
         */
        if(columnModels.length > 4) {
            columnModels[4] = new TableColumnModelBuilder().addColumns(
                    new TableColumn("WKN", -1f).withVisibilityCheck(showWknCheck), // $NON-NLS-0$
                    new TableColumn("ISIN", -1f).withVisibilityCheck(showIsinCheck), // $NON-NLS-0$
                    new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name"),  // $NON-NLS-0$
                    new TableColumn(I18n.I.riskClass(), -1f, TableCellRenderers.STRING, "edgTopClass"),  // $NON-NLS-0$
                    new TableColumn(I18n.I.rating(), -1f, TableCellRenderers.DEFAULT, "edgTopScore")  // $NON-NLS-0$
            ).asTableColumnModel();
        }
    }
}
