package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.MSCMostActive;
import de.marketmaker.iview.dmxml.MSCMostActiveElement;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BlockPipe;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BlockPipeResult;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich Maurer
 * @author Michael Lösch
 *         Date: 08.11.11
 */
public class PopularInstrumentsController extends AbstractPageController {
    final TableView tableView;

    private final DmxmlContext.Block<MSCListDetails> priceBlock;

    private final DmxmlContext.Block<MSCMostActive> mostActiveBlock;
    private final String vendor;
    private final BlockPipe pipe;

    public static class Factory implements MultiTabController.TabControllerFactory {
        public AbstractPageController createTabController(MultiContentView view, JSONWrapper tabConfig) {
            return new PopularInstrumentsController(view, tabConfig);
        }
    }

    public PopularInstrumentsController(ContentContainer contentContainer, JSONWrapper jsonConfig) {
        super(contentContainer);

        final SnippetConfiguration config = SnippetConfiguration.createFrom(jsonConfig);
        this.vendor = config.getString("vendor"); // $NON-NLS$
        this.mostActiveBlock = this.context.addBlock("MSC_MostActive"); // $NON-NLS$
        this.priceBlock = this.context.addBlock("MSC_PriceDataMulti"); // $NON-NLS$
        this.pipe = new BlockPipe(this.mostActiveBlock, null, "symbol", true, new BlockPipeResult<MSCMostActive>() { // $NON-NLS$
            public String[] getResult(DmxmlContext.Block<MSCMostActive> mscMostActiveBlock) {
                final List<String> result = new ArrayList<String>();
                final List<MSCMostActiveElement> elements = mscMostActiveBlock.getResult().getElement();
                Firebug.log("elements.size(): " + elements.size());
                for (MSCMostActiveElement element : elements) {
                    Firebug.log("element.getVendor(): " + element.getVendor());
                    if (element.getVendor().equals(vendor)) {
                        final List<IdentifierData> items = element.getItem();
                        for (IdentifierData item : items) {
                            Firebug.log("result.add(item.getQuotedata().getQid());: " + item.getQuotedata().getQid());
                            result.add(item.getQuotedata().getQid());
                        }
                    }
                }
                return result.toArray(new String[result.size()]);
            }
        });
        pipe.setNext(this.priceBlock);

        final VisibilityCheck dzCheck = SimpleVisibilityCheck.valueOf(Selector.DZ_BANK_USER.isAllowed());
        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());

        final TableColumn[] cols = new TableColumn[]{
                new TableColumn(I18n.I.info(), 20, TableCellRenderers.DZPIB_DOWNLOAD_ICON_LINK_QWI).withVisibilityCheck(dzCheck),
                new TableColumn("ISIN", 0.05f, TableCellRenderers.STRING).withVisibilityCheck(showIsinCheck), // $NON-NLS$
                new TableColumn("WKN", 0.05f, TableCellRenderers.STRING).withVisibilityCheck(showWknCheck), // $NON-NLS$
                new TableColumn("Name", 0.3f, TableCellRenderers.QUOTELINK_32), // $NON-NLS$
                new TableColumn(I18n.I.priceValue(), 0.01f, TableCellRenderers.PRICE_WITH_SUPPLEMENT),
                new TableColumn(I18n.I.marketName(), 0.1f, TableCellRenderers.STRING),
                new TableColumn("+/-", 0.05f, TableCellRenderers.CHANGE_NET), // $NON-NLS-0$
                new TableColumn("+/- %", 0.05f, TableCellRenderers.CHANGE_PERCENT), // $NON-NLS-0$
                new TableColumn(I18n.I.trend(), 0.1f, TableCellRenderers.TRENDBAR),
                new TableColumn(I18n.I.dateTime(), 0.1f, TableCellRenderers.COMPACT_DATETIME).alignRight(),
                new TableColumn(I18n.I.bid(), 0.05f, TableCellRenderers.PRICE),
                new TableColumn(I18n.I.ask(), 0.05f, TableCellRenderers.PRICE),
        };
        final TableColumnModel tcm = new DefaultTableColumnModel(cols);
        this.tableView = new TableView(this, tcm);
        this.tableView.setHeaderVisible(false);
    }

    @Override
    public void destroy() {
        this.context.removeBlock(this.mostActiveBlock);
        this.context.removeBlock(this.priceBlock);
        super.destroy();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.pipe.issueRequest(this);
    }

    @Override
    protected void onResult() {
        if (!mostActiveBlock.isResponseOk() || !priceBlock.isResponseOk()) {
            this.tableView.show(DefaultTableDataModel.NULL);
            return;
        }

        final TableDataModel tdm = createTableDataModel(this.mostActiveBlock, this.priceBlock);
        this.tableView.show(tdm);
    }


    private List<QuoteWithInstrument> getQwis(MSCMostActive mostActive, String vendor) {
        final List<QuoteWithInstrument> result = new ArrayList<QuoteWithInstrument>();
        final List<MSCMostActiveElement> elements = mostActive.getElement();
        for (MSCMostActiveElement element : elements) {
            if (vendor.equals(element.getVendor())) {
                final List<IdentifierData> ident = element.getItem();
                for (int i = 0, identSize = ident.size(); i < identSize; i++) {
                    result.add(new QuoteWithInstrument(ident.get(i).getInstrumentdata(),
                            ident.get(i).getQuotedata()));
                }
            }
        }
        return result;
    }


    private DefaultTableDataModel createTableDataModel(DmxmlContext.Block<MSCMostActive> mostActiveBlock,
                                                       DmxmlContext.Block<MSCListDetails> priceBlock) {
        final List<QuoteWithInstrument> qwis = getQwis(mostActiveBlock.getResult(), this.vendor);
        final DefaultTableDataModel tdm = new DefaultTableDataModel(qwis.size(), 12);
        final List<MSCListDetailElement> prices = priceBlock.getResult().getElement();
        final TrendBarData tbd = TrendBarData.create(priceBlock.getResult());
        for (int i = 0, instrumentsSize = qwis.size(); i < instrumentsSize; i++) {
            final QuoteWithInstrument quoteWithInstrument = qwis.get(i);
            final Price price = Price.create(prices.get(i));
            tdm.setValuesAt(i, new Object[]{
                    quoteWithInstrument,
                    quoteWithInstrument.getInstrumentData().getIsin(),
                    quoteWithInstrument.getInstrumentData().getWkn(),
                    qwis.get(i),
                    price.getLastPrice(),
                    prices.get(i).getQuotedata().getMarketVwd(),
                    price.getChangeNet(),
                    price.getChangePercent(),
                    new CurrentTrendBar(price.getChangePercent(), tbd),
                    price.getDate(),
                    price.getBid(),
                    price.getAsk()
            });
        }
        return tdm;
    }
}
