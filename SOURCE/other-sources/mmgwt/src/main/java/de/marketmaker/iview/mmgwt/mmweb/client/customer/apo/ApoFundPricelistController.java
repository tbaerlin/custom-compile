package de.marketmaker.iview.mmgwt.mmweb.client.customer.apo;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.TableView;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.SortLinkSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class ApoFundPricelistController extends AbstractPageController {
    final TableView view;


    private DmxmlContext.Block<FNDFinder> block;


    public ApoFundPricelistController(ContentContainer contentContainer) {
        super(contentContainer);
        final TableColumnModel tcm = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.name(), -1f, TableCellRenderers.QUOTELINK_32, "name"),  // $NON-NLS-0$
                new TableColumn(I18n.I.market1(), -1f, TableCellRenderers.DEFAULT, "market1"),  // $NON-NLS-0$
                new TableColumn(I18n.I.priceValue(), -1f, TableCellRenderers.PRICE, "priceValue"),  // $NON-NLS-0$
                new TableColumn(I18n.I.currency(), -1f, TableCellRenderers.DEFAULT, "currency"),  // $NON-NLS-0$
                new TableColumn(I18n.I.nMonths(3), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance3m"),  // $NON-NLS-0$
                new TableColumn(I18n.I.nYears(1), -1f, TableCellRenderers.CHANGE_PERCENT, "bviperformance1y"),  // $NON-NLS-0$
                new TableColumn(I18n.I.dateTime(), -1f, TableCellRenderers.DATE_OR_TIME, "date").withCellClass("mm-right")  // $NON-NLS-0$ $NON-NLS-1$
        });
        this.view = new TableView(this, tcm);

        this.block = this.context.addBlock("FND_Finder"); // $NON-NLS-0$
        this.block.setParameter("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("count", "100"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("sortBy", "name"); // $NON-NLS-0$ $NON-NLS-1$
        this.block.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$

        this.view.setSortLinkListener(new SortLinkSupport(this.block, new Command() {
            public void execute() {
                refresh();
            }
        }, true, true));

    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final String jsonSuffix = event.getHistoryToken().get(1, null);
        assert (jsonSuffix != null);
        this.block.setParameter("query", SessionData.INSTANCE.getListAsFinderQuery(ApoFundPricelistsController.JSON_KEY_PRICELIST_PREFIX + jsonSuffix)); // $NON-NLS-0$
        refresh();
    }

    @Override
    protected void onResult() {
        if (!this.block.isResponseOk()) {
            this.view.show(DefaultTableDataModel.NULL);
            return;
        }

        final FNDFinder fndFinder = this.block.getResult();
        final List<Object[]> list = new ArrayList<Object[]>();
        for (FNDFinderElement e : fndFinder.getElement()) {
            list.add(new Object[]{
                    new QuoteWithInstrument(e.getInstrumentdata(), e.getQuotedata()),
                    e.getQuotedata().getMarketName(),
                    e.getPrice(),
                    e.getQuotedata().getCurrencyIso(),
                    e.getBviperformance3M(),
                    e.getBviperformance1Y(),
                    e.getDate()
            });
        }

        this.view.show(DefaultTableDataModel.create(list).withSort(fndFinder.getSort()));
    }
}
