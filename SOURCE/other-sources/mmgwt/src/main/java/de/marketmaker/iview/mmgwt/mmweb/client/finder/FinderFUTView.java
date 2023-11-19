/*
 * FinderFNDView.java
 *
 * Created on 11.06.2008 13:43:14
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderFUTView<F extends AbstractFinder> extends AbstractFinderView<F> {

    FinderFUTView(F controller) {
        super(controller);
    }

    @Override
    protected PagingWidgets.Config getPagingWidgetsConfig() {
        return super.getPagingWidgetsConfig().withAddEntryCount(true);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), 220, QUOTELINK_32, "name")  // $NON-NLS-0$
                , new TableColumn(I18n.I.maturity2(), -1f, TableCellRenderers.DATE, "expirationDate")  // $NON-NLS-0$
                , new TableColumn(I18n.I.paid(), -1f, PRICE) 
                , new TableColumn(I18n.I.turnover(), -1f, TURNOVER) 
                , new TableColumn("+/-", -1f, CHANGE_NET).alignRight() // $NON-NLS-0$
                , new TableColumn("+/-%", -1f, CHANGE_PERCENT).alignRight() // $NON-NLS-0$
                , new TableColumn(I18n.I.totalTurnoverAbbr(), -1f, TURNOVER) 
                , new TableColumn(I18n.I.bid(), -1f, PRICE) 
                , new TableColumn(I18n.I.ask(), -1f, PRICE) 
                , new TableColumn(I18n.I.date(), -1f, COMPACT_DATETIME).alignRight() 
                , new TableColumn(I18n.I.previousSettlement(), -1f, PRICE) 
        });
    }
}
