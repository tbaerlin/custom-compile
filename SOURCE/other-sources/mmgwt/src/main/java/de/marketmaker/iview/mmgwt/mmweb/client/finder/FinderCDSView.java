package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE;

public class FinderCDSView<F extends AbstractFinder> extends AbstractFinderView<F> {

    FinderCDSView(F controller) {
        super(controller);
    }

    protected void initColumnModels(TableColumnModel[] columnModels) {
        columnModels[0] = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.issuer(), -1f, TableCellRenderers.STRING_CENTER, "issuername"), // $NON-NLS$
                new TableColumn(I18n.I.name(), 100, TableCellRenderers.QUOTELINK_32, "name"),  // $NON-NLS-0$
                new TableColumn(I18n.I.maturity(), -1f, TableCellRenderers.STRING_10, "maturity"), // $NON-NLS$
                new TableColumn(I18n.I.debtRankings(), -1f, TableCellRenderers.STRING, "debtRanking"), // $NON-NLS$
                new TableColumn(I18n.I.issuerType(), -1f, TableCellRenderers.STRING_10, "issuerType"), // $NON-NLS$
                new TableColumn(I18n.I.restructuringRule(), -1f, TableCellRenderers.STRING_10, "restructuringRule"), // $NON-NLS$
                new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.STRING_10, "currency"), // $NON-NLS$
                new TableColumn(I18n.I.price(), -1f, PRICE),
                new TableColumn(I18n.I.date(), -1f, TableCellRenderers.DATE).alignRight(),
                new TableColumn(I18n.I.changeNetAbbr(), -1f, TableCellRenderers.CHANGE_NET),
                new TableColumn(I18n.I.changePercentAbbr(), -1f, TableCellRenderers.CHANGE_PERCENT, "changePercent"),  // $NON-NLS-0$
        });
    }

}
