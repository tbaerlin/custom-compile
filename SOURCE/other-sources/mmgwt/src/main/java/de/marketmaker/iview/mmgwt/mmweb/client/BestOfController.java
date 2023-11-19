package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.dmxml.MSCListDetailElement;
import de.marketmaker.iview.dmxml.MSCListDetails;
import de.marketmaker.iview.dmxml.TopProductsCell;
import de.marketmaker.iview.dmxml.TopProductsElement;
import de.marketmaker.iview.dmxml.TopProductsRow;
import de.marketmaker.iview.dmxml.TopProductsTable;
import de.marketmaker.iview.mmgwt.mmweb.client.data.BestOfCell;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.BestOfProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.PagingPanel;

/**
 * @author Ulrich Maurer
 *         Date: 28.10.11
 */
public class BestOfController extends AbstractPageController {
    private static final int PAGE_SIZE = 40;
    private static final String ENDLESS = "isEndless"; // $NON-NLS$
    final TableView tableView;

    private final BestOfProvider.BestOfConfiguration boConfig;
    private DmxmlContext.Block<TopProductsTable> topBlock;
    private PagingPanel pagingPanel;
    private SafeHtml originalHeader;

    public static class Factory implements MultiTabController.TabControllerFactory {
        public AbstractPageController createTabController(MultiContentView view, JSONWrapper tabConfig) {
            return new BestOfController(view, tabConfig.get("id").stringValue()); // $NON-NLS$
        }
    }

    public BestOfController(ContentContainer contentContainer, String configId) {
        super(contentContainer);

        this.boConfig = BestOfProvider.INSTANCE.getConfiguration(configId);

        this.topBlock = context.addBlock("MSC_TopProducts", this.boConfig.getParameters()); // $NON-NLS$
        this.topBlock.setParameter("issuername", GuiDefsLoader.getIssuerName()); // $NON-NLS$
        this.topBlock.setParameter("count", PAGE_SIZE); // $NON-NLS$

        final DefaultTableColumnModel tcm = createTableColumnModel();

        this.tableView = new TableView(this, tcm);
        this.pagingPanel = new PagingPanel(new PagingPanel.Config(this.tableView)
                .withPageSize(PAGE_SIZE)
                .withPagingOnTop(false)
        );
        this.pagingPanel.setHandler(new PagingPanel.Handler() {
            public void ackNewOffset(int offset) {
                topBlock.setParameter("offset", offset); // $NON-NLS$
                refresh();
            }
        });
    }

    private DefaultTableColumnModel createTableColumnModel() {
        final List<TableColumn> tabCols = new ArrayList<>();
        tabCols.add(new TableColumn(I18n.I.underlyingInstrument(), 0.2f, TableCellRenderers.QUOTELINK_32));

        tabCols.add(new TableColumn(I18n.I.price(), 0.05f, TableCellRenderers.PRICE23));
        tabCols.add(new TableColumn(I18n.I.currency(), 0.05f, TableCellRenderers.STRING));
        tabCols.add(new TableColumn(I18n.I.changePercentAbbr(), 0.05f, TableCellRenderers.CHANGE_PERCENT));
        final List<BestOfCell> cells = this.boConfig.getCells();
        float width = 0.65f / cells.size();
        for (BestOfCell cell : cells) {
            tabCols.add(new TableColumn(cell.getTitle(), width, TableCellRenderers.DEFAULT_RIGHT));
        }
        return new DefaultTableColumnModel(tabCols.toArray(new TableColumn[tabCols.size()]));
    }

    @Override
    public void destroy() {
        this.context.removeBlock(this.topBlock);
        super.destroy();
    }


    public void onPlaceChange(PlaceChangeEvent event) {
        if (!StringUtil.hasText(this.originalHeader)) {
            this.originalHeader = AbstractMainController.INSTANCE.getView().getContentHeader();
        }
        AbstractMainController.INSTANCE.getView().setContentHeader(StringUtil.asHeader(this.originalHeader, this.boConfig.getLongTitle()));
        refresh();
    }

    @Override
    protected void onResult() {
        if (!topBlock.isResponseOk()) {
            this.tableView.show(DefaultTableDataModel.NULL);
            return;
        }
        final TableDataModel tdm = createTableDataModel(this.topBlock, null);
        this.tableView.show(tdm);
    }

    public TableDataModel createTableDataModel(DmxmlContext.Block<TopProductsTable> topBlock,
                                               DmxmlContext.Block<MSCListDetails> priceBlock) {
        final List<Object[]> list = new ArrayList<>();
        final List<MSCListDetailElement> underlyings = topBlock.getResult().getUnderlying();
        for (int i = 0, underlyingsSize = underlyings.size(); i < underlyingsSize; i++) {
            final MSCListDetailElement underlying = underlyings.get(i);
            final List<Object> cols = new ArrayList<>();
            final QuoteWithInstrument qwi = new QuoteWithInstrument(underlying.getInstrumentdata(), underlying.getQuotedata());
            cols.add(qwi);
            cols.add(underlying.getPricedata() == null ? null : underlying.getPricedata().getPrice());
            cols.add(underlying.getQuotedata() == null ? null : underlying.getQuotedata().getCurrencyIso());
            cols.add(underlying.getPricedata() == null ? null : underlying.getPricedata().getChangePercent());
            final TopProductsRow row = topBlock.getResult().getRow().get(i);
            final List<TopProductsCell> columns = row.getColumn();
            for (BestOfCell cell : this.boConfig.getCells()) {
                final TopProductsCell column = findColumn(columns, cell.getKey());
                if (column != null && column.getItem() != null && !column.getItem().isEmpty() && column.getItem().get(0) != null) {
                    final TopProductsElement e = column.getItem().get(0);
                    final QwiAndValue<String> qwiAndValue = new QwiAndValue<>(qwi, column.getKey());
                    final Link link = new Link(new LinkListener<Link>() {
                        @Override
                        public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                            //noinspection unchecked
                            openFinder((QwiAndValue<String>) linkLinkContext.getData().getData(),
                                    boConfig.getFinderParameters().get("finderType")); // $NON-NLS$
                        }
                    }, "PERCENT".equals(this.boConfig.getRenderer()) // $NON-NLS$
                            ? Renderer.PERCENT.render(e.getValue())
                            : Renderer.PRICE23.render(e.getValue()), I18n.I.gotoFinder()).withData(qwiAndValue);
                    cols.add(link);
                }
                else {
                    cols.add("--");
                }
            }

            list.add(cols.toArray());
        }

        this.pagingPanel.update(Integer.parseInt(topBlock.getResult().getOffset()), Integer.parseInt(topBlock.getResult().getCount()),
                Integer.parseInt(topBlock.getResult().getTotal()));

        return DefaultTableDataModel.create(list);
    }

    private TopProductsCell findColumn(List<TopProductsCell> columns, String key) {
        for (TopProductsCell column : columns) {
            if (column != null && key.equals(column.getKey())) {
                return column;
            }
        }
        return null;
    }

    private static HashMap<String, TopProductsRow> asMap(List<TopProductsRow> listRows) {
        final HashMap<String, TopProductsRow> map = new HashMap<>(listRows.size());
        for (TopProductsRow row : listRows) {
            if (row == null) {
                continue;
            }
            map.put(row.getKey(), row);
        }
        return map;
    }

    private void openFinder(QwiAndValue<String> qwiAndValue, String finderType) {
        final FinderController controller = "LEV".equals(finderType) ? LiveFinderCER.INSTANCE_LEV : LiveFinderCER.INSTANCE_CER; // $NON-NLS$
        final FinderFormConfig ffc = new FinderFormConfig("temp", controller.getId()); // $NON-NLS$
        ffc.put(LiveFinderCER.UNDERLYING_ID, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.UNDERLYING, "true"); // $NON-NLS-0$
        ffc.put(FinderFormKeys.UNDERLYING + "-symbol", qwiAndValue.getIid(false)); // $NON-NLS-0$
        ffc.put(FinderFormKeys.UNDERLYING + "-name", qwiAndValue.getInstrumentData().getName()); // $NON-NLS-0$

        final Map<String, String> finderParameters = new HashMap<>(this.boConfig.getFinderParameters());
        for (Map.Entry<String, String> entry : finderParameters.entrySet()) {
            String value = entry.getValue();
            if (value.startsWith("@")) { // $NON-NLS$
                value = getFromToValue(value, qwiAndValue.getValue());
            }
            if (ENDLESS.equals(value)) {
                ffc.put(ENDLESS, "true"); // $NON-NLS$
            }
            else {
                ffc.put(entry.getKey(), value);
            }
        }
        ffc.put(FinderFormKeys.ONLY_CUSTOM_ISSUER, "true"); // $NON-NLS$
        controller.prepareFind(ffc);
        PlaceUtil.goTo("M_LF_" + finderType); // $NON-NLS$
    }


    private String getFromToValue(String fromToValue, String value) {
        if ("NULL".equals(value)) { // $NON-NLS$
            return ENDLESS;
        }
        int endPos = fromToValue.indexOf('@', 1);
        if (endPos == -1) {
            throw new RuntimeException("getFromToValue() - closing '@' missing: " + fromToValue); // $NON-NLS$
        }
        final String regEx = fromToValue.substring(1, endPos);
        int intValue = "0".equals(value) ? 0 : Integer.parseInt(value.replaceAll(regEx, "$1")); // $NON-NLS$

        if (endPos + 1 != fromToValue.length()) {
            final char operator = fromToValue.charAt(endPos + 1);
            final int increment = Integer.parseInt(fromToValue.substring(endPos + 2));
            if (operator == '+') {
                intValue += increment;
            }
            else if (operator == '-') {
                intValue -= increment;
            }
            else {
                throw new RuntimeException("getFromToValue() - unknown operator: " + fromToValue); // $NON-NLS$
            }
        }

        return String.valueOf(intValue);
    }
}