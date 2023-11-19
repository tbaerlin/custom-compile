package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.BNDFinder;
import de.marketmaker.iview.dmxml.BNDFinderElement;
import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.CERFinder;
import de.marketmaker.iview.dmxml.CERFinderElement;
import de.marketmaker.iview.dmxml.FNDFinder;
import de.marketmaker.iview.dmxml.FNDFinderElement;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.STKFinder;
import de.marketmaker.iview.dmxml.STKFinderElement;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.KwtProductMap;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CertificateTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.*;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.FlexTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SelectorVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SortedProxyTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.*;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.CHANGE_PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.COMPACT_DATE_OR_TIME;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DATE_RIGHT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT_CENTER;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DEFAULT_RIGHT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PERCENT;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.PRICE23;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.QUOTELINK_32;
import static de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.STRING_CENTER;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil.TOKEN_DIVIDER;

/**
 * @author Ulrich Maurer
 *         Date: 14.11.11
 */
public class TabbedPricelistController extends AbstractPageController {
    private final DmxmlContext.Block<? extends BlockListType> block;
    private final TableDefinition<? extends BlockListType> tableDefinition;
    private final TableView tableView;
    private KwtProductMap productMap = null;
    private final String instrumentType;
    private List<String> pdfSymbols = new ArrayList<>();
    private boolean localInstruments = false;
    private String listTitle = null;
    private final DefaultTableColumnModel tableColumnModel;

    public TabbedPricelistController(ContentContainer contentContainer, String instrumentType) {
        super(contentContainer);
        this.instrumentType = instrumentType;

        switch (instrumentType) {
            case "STK":  // $NON-NLS$
                this.tableDefinition = new TableDefinitionStk();
                break;
            case "BND":  // $NON-NLS$
                this.tableDefinition = new TableDefinitionBnd();
                break;
            case "FND":  // $NON-NLS$
                this.tableDefinition = new TableDefinitionFnd();
                break;
            case "CER":  // $NON-NLS$
                this.tableDefinition = new TableDefinitionCer();
                break;
            case "all":  // $NON-NLS$
                this.tableDefinition = new TableDefinitionAll();
                break;
            default:
                throw new IllegalArgumentException("unhandled instrumentType: " + instrumentType); // $NON-NLS$
        }
        this.block = this.tableDefinition.getBlock();

        final TableColumn[] cols = this.tableDefinition.getColumns();
        this.tableColumnModel = new DefaultTableColumnModel("tcols:tp:" + instrumentType, cols).withColumnOrder(this.tableDefinition.getColumnOrder()); // $NON-NLS$
        this.tableView = new TableView(this, tableColumnModel);
        this.tableView.setHeaderVisible(false);
        this.tableView.setSortLinkListener(this.tableDefinition.getSortLinkListener());

        EventBusRegistry.get().addHandler(GuiDefsChangedEvent.getType(), new GuiDefsChangedHandler(){
            @Override
            public void onGuidefsChange(GuiDefsChangedEvent event) {
                productMap = null;
            }
        });
    }

    private interface TableDefinition<T extends BlockListType> {
        DmxmlContext.Block<T> getBlock();

        TableColumn[] getColumns();

        void setList(KwtProductMap.List list, String[] symbols);

        TableDataModel createTableDataModel();

        LinkListener<String> getSortLinkListener();

        String[] getColumnOrder();
    }

    private class TableDefinitionAll implements TableDefinition<MSCPriceDataExtended> {
        private final DmxmlContext.Block<MSCPriceDataExtended> block;
        private KwtProductMap.List list;
        private final TableColumn[] columns;
        private final SortedProxyTableDataModel tdmProxy;
        private final String[] columnOrder;

        class ValueMapping implements SortedProxyTableDataModel.ValueMapping {
            public String toString(Object value, String columnKey) {
                if ("date".equals(columnKey)) { // $NON-NLS$
                    final String s = (String) value;
                    return s.replaceFirst("(\\d{4})-(\\d{1,2})-(\\d{1,2})(.*)$", "$3.$2.$1$4"); // $NON-NLS$
                }
                return null;
            }
        }

        private TableDefinitionAll() {
            this.block = context.addBlock("MSC_PriceDataExtended"); // $NON-NLS$

            final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());

            this.columns = new TableColumn[]{
                    new TableColumn(I18n.I.instrumentTypeAbbr(), -1f, TableCellRenderers.DEFAULT, "instrumentType").withId("it"), // $NON-NLS$
                    new TableColumn(I18n.I.name(), -1f, DEFAULT, "name").withId("nm"), // $NON-NLS$
                    new TableColumn("WKN", -1f, DEFAULT, "wkn").withId("wk").withVisibilityCheck(showWknCheck), // $NON-NLS$
                    new TableColumn("ISIN", -1f, DEFAULT, "isin").withId("is"), // $NON-NLS$
                    new TableColumn(I18n.I.price(), -1f, DEFAULT_RIGHT).withId("pr"), // $NON-NLS$
                    new TableColumn(I18n.I.currency(), -1f, DEFAULT, "currency").withId("ci"), // $NON-NLS$
                    new TableColumn(I18n.I.market(), -1f, DEFAULT_CENTER, "market").withId("mv"), // $NON-NLS$
                    new TableColumn(I18n.I.time(), -1f, COMPACT_DATE_OR_TIME, "date").withId("dt"),  // $NON-NLS$
                    new TableColumn(I18n.I.kwtNotices(), -1f, DEFAULT).withId("kn"), // $NON-NLS$
                    new TableColumn(I18n.I.url(), -1f, DEFAULT).withCellClass("mm-nobreak").withId("url"), // $NON-NLS$
            };
            this.columnOrder = new String[]{"it", "nm", "wk", "is", "pr", "ci", "mv", "dt", "kn", "url"}; // $NON-NLS$
            this.tdmProxy = SortedProxyTableDataModel.create(this.columns, new Command() {
                public void execute() {
                    tableView.show(tdmProxy);
                    resetPdfSymbols();
                }
            }, new ValueMapping());
        }

        public String[] getColumnOrder() {
            return this.columnOrder;
        }

        public DmxmlContext.Block<MSCPriceDataExtended> getBlock() {
            return this.block;
        }

        public TableColumn[] getColumns() {
            return this.columns;
        }

        public void setList(KwtProductMap.List list, String[] symbols) {
            this.block.setEnabled(symbols.length > 0);
            this.block.setParameters("symbol", symbols); // $NON-NLS$

            this.list = list;
        }

        public TableDataModel createTableDataModel() {
            final HashMap<String, MSCPriceDataExtendedElement> map = createPriceDataMap(this.block);
            final FlexTableDataModel tdm = new FlexTableDataModel(this.columns.length);
            for (KwtProductMap.ListEntry entry : this.list.getListEntries()) {
                final QuoteWithInstrument qwi = entry.getQwi();
                if (qwi != null) {
                    final InstrumentData instrumentData = qwi.getInstrumentData();
                    final QuoteData quoteData = qwi.getQuoteData();
                    pdfSymbols.add(quoteData.getQid());
                    final MSCPriceDataExtendedElement element = map.get(quoteData.getQid());
                    final Price price = element == null ? null : Price.create(element);
                    tdm.addValues(new Object[]{
                            InstrumentTypeEnum.valueOf(instrumentData.getType()).getDescription(),
                            new QuoteWithInstrument(instrumentData, quoteData),
                            instrumentData.getWkn(),
                            instrumentData.getIsin(),
                            price == null ? null : price,
                            quoteData.getCurrencyIso(),
                            quoteData.getMarketVwd(),
                            price == null ? null : (price.getDate() == null ? price.getBidAskDate() : price.getDate()),
                            entry.getNote(),
                            getUrl(entry)
                    });
                }
                else if (entry.hasData()) {
                    tdm.addValues(new Object[]{
                            InstrumentTypeEnum.valueOf(entry.getInstrumentType()).getDescription(),
                            entry.getName(),
                            null,
                            entry.getData(KwtProductMap.CSV_FIELD_ISIN),
                            entry.getData(KwtProductMap.CSV_FIELD_PRICE),
                            null,
                            null,
                            entry.getData(KwtProductMap.CSV_FIELD_PRICEDATE),
                            entry.getNote(),
                            getUrl(entry)
                    });
                }
            }
            this.tdmProxy.setDelegate(tdm);
            return this.tdmProxy;
        }

        public LinkListener<String> getSortLinkListener() {
            return this.tdmProxy;
        }
        
        private void resetPdfSymbols() {
            pdfSymbols.clear();
            for (int i = 0, count = this.tdmProxy.getRowCount(); i < count; i++) {
                final Object value = this.tdmProxy.getValueAt(i, 1);
                if (value instanceof QuoteWithInstrument) {
                    pdfSymbols.add(((QuoteWithInstrument) value).getQuoteData().getQid());
                }
            }
        }
    }

    private String getUrl(KwtProductMap.ListEntry entry) {
        if (entry == null) {
            return null;
        }
        String url = entry.getUrl();
        if (url == null) {
            url = entry.getData(KwtProductMap.CSV_FIELD_URL);
        }
        if (url != null && !url.startsWith("<a")) { // $NON-NLS$
            url = "<a href=\"" + url + "\" target=\"_blank\">" + url + "</a>"; // $NON-NLS$
        }
        return url;
    }

    private abstract class FinderTableDefinition<T extends BlockListType> implements TableDefinition<T> {
        protected final DmxmlContext.Block<T> block;
        protected final DmxmlContext.Block<MSCPriceDataExtended> blockPriceData;
        protected KwtProductMap.List list;
        protected int columnCount;
        protected String[] columnOrder;

        protected FinderTableDefinition(String blockKey, boolean withPriceData) {
            this.block = context.addBlock(blockKey);
            if (withPriceData) {
                this.blockPriceData = context.addBlock("MSC_PriceDataExtended"); // $NON-NLS$
            }
            else {
                this.blockPriceData = null;
            }
        }

        public final TableColumn[] getColumns() {
            final TableColumn[] columns = createColumnArray();
            this.columnCount = columns.length;
            return columns;
        }

        abstract TableColumn[] createColumnArray();

        public DmxmlContext.Block<T> getBlock() {
            return this.block;
        }

        public String[] getColumnOrder() {
            return this.columnOrder;
        }

        public void setList(KwtProductMap.List list, String[] symbols) {
            this.list = list;
            final boolean blocksEnabled = symbols.length != 0;
            this.block.setEnabled(blocksEnabled);
            if (this.blockPriceData != null) {
                this.blockPriceData.setEnabled(blocksEnabled);
            }
            if (!blocksEnabled) {
                return;
            }

            final StringBuilder sb = new StringBuilder();
            sb.append("qid == '"); // $NON-NLS$
            String divider = "";
            for (String symbol : symbols) {
                sb.append(divider).append(symbol.replace(".qid", "")); // $NON-NLS$
                divider = "@"; // $NON-NLS$
            }
            sb.append("'");
            this.block.setParameter("count", String.valueOf(symbols.length)); // $NON-NLS$
            this.block.setParameter("query", sb.toString()); // $NON-NLS$
            if (this.blockPriceData != null) {
                this.blockPriceData.setParameters("symbol", symbols); // $NON-NLS$
            }
        }

        public LinkListener<String> getSortLinkListener() {
            return new SortLinkSupport(this.block, new Command() {
                public void execute() {
                    if (localInstruments) {
                        tableView.show(tableDefinition.createTableDataModel());
                    }
                    else {
                        refresh();
                    }
                }
            });
        }

        protected Map<String, KwtProductMap.ListEntry> createEntryMap(final ArrayList<KwtProductMap.ListEntry> listEntries) {
            final HashMap<String, KwtProductMap.ListEntry> map = new HashMap<>();
            for (KwtProductMap.ListEntry listEntry : listEntries) {
                final QuoteWithInstrument qwi = listEntry.getQwi();
                if (qwi != null) {
                    map.put(qwi.getQuoteData().getQid(), listEntry);
                }
            }
            return map;
        }
    }

    private class TableDefinitionStk extends FinderTableDefinition<STKFinder> {
        private final String currentYear = String.valueOf(new MmJsDate().getFullYear());
        private final TableColumn columnDividend;
        private final TableColumn columnPriceEarningRatio;
        private final TableColumn columnDividendYield;

        private TableDefinitionStk() {
            super("STK_Finder", true); // $NON-NLS$
            this.columnPriceEarningRatio = new TableColumn(I18n.I.priceEarningsRatioAbbrE(), -1f, DEFAULT_RIGHT, "priceEarningRatio1y").withId("per"); // $NON-NLS$
            this.columnDividend = new TableColumn(I18n.I.dividend(), -1f, DEFAULT_RIGHT, "dividend").withId("div"); // $NON-NLS$
            this.columnDividendYield = new TableColumn(I18n.I.dividendYieldAbbrNoBreak(), -1f, DEFAULT_RIGHT, "dividendYield").withId("divy"); // $NON-NLS$
            
            this.columnOrder = new String[]{ "se", "nm", "is", "pr", "ci", "dt", "mv", "pc", "cp", "pytd", "p1y", "p3y", "h52", "l52", "per", "div", "divy", "ana", "kn", "url" }; // $NON-NLS$
        }

        public TableColumn[] createColumnArray() {
            return new TableColumn[]{
                    new TableColumn(I18n.I.sector(), -1f, DEFAULT, "sector").withId("se"), // $NON-NLS$
                    new TableColumn(I18n.I.name(), -1f, QUOTELINK_32, "name").withId("nm"), // $NON-NLS$
                    new TableColumn("ISIN", -1f, DEFAULT, "isin").withId("is"), // $NON-NLS$
                    new TableColumn(I18n.I.price(), -1f, PRICE).withId("pr"), // $NON-NLS$
                    new TableColumn(I18n.I.currency(), -1f, DEFAULT, "currency").withId("ci"), // $NON-NLS$
                    new TableColumn(I18n.I.time(), -1f, COMPACT_DATE_OR_TIME).alignRight().withId("dt"), // $NON-NLS$
                    new TableColumn(I18n.I.market(), -1f, STRING_CENTER, "market").withId("mv"), // $NON-NLS$
                    new TableColumn(I18n.I.previousClose(), -1f, PRICE).withId("pc"), // $NON-NLS$
                    new TableColumn("+/-%", -1f, CHANGE_PERCENT, "changePercent").withCellClass("mm-right mm-nobreak").withId("cp"), // $NON-NLS$
                    new TableColumn(I18n.I.performanceAbbr("YTD"), -1f, CHANGE_PERCENT, "performanceCurrentYear").withCellClass("mm-right mm-nobreak").withId("pytd"),  // $NON-NLS$
                    new TableColumn(I18n.I.performanceAbbr(I18n.I.nYearAbbr(1)), -1f, CHANGE_PERCENT, "performance1y").withCellClass("mm-right mm-nobreak").withId("p1y"),  // $NON-NLS$
                    new TableColumn(I18n.I.performanceAbbr(I18n.I.nYearAbbr(3)), -1f, CHANGE_PERCENT, "performance3y").withCellClass("mm-right mm-nobreak").withId("p3y"),  // $NON-NLS$
                    new TableColumn(I18n.I.high52wAbbr(), -1f, PRICE).withId("h52"), // $NON-NLS$
                    new TableColumn(I18n.I.low52wAbbr(), -1f, PRICE).withId("l52"), // $NON-NLS$
                    this.columnPriceEarningRatio,
                    this.columnDividend,
                    this.columnDividendYield,
                    new TableColumn(I18n.I.analystsTrend(), -1f, TableCellRenderers.PRICE_MAX2, "recommendation").withVisibilityCheck(SimpleVisibilityCheck.valueOf(Selector.isDzProfitEstimate())).alignCenter().withId("ana"),  // $NON-NLS$
                    new TableColumn(I18n.I.kwtNotices(), -1f, DEFAULT).withId("kn"), // $NON-NLS$
                    new TableColumn(I18n.I.url(), -1f, DEFAULT).withCellClass("mm-nobreak").withId("url"), // $NON-NLS$
            };
        }

        public FlexTableDataModel createTableDataModel() {
            final STKFinder result = this.block.getResult();
            final FlexTableDataModel tdm = new FlexTableDataModel(this.columnCount);
            final HashMap<String, MSCPriceDataExtendedElement> mapPriceData = createPriceDataMap(this.blockPriceData);
            final ArrayList<KwtProductMap.ListEntry> listEntries = list.getListEntries("STK"); // $NON-NLS$
            boolean hasCurrentFiscalYear = false;
            if (this.block.getParameter("sortBy") == null) { // $NON-NLS$
                tdm.withSort(null, true, result.getSort().getField());
                final Map<String, STKFinderElement> mapFinderElements = createFinderElementMap(result.getElement());
                for (KwtProductMap.ListEntry listEntry : listEntries) {
                    final String qid = listEntry.getQwi().getQuoteData().getQid();
                    final STKFinderElement finderElem = mapFinderElements.get(qid);
                    if (finderElem == null) {
                        Firebug.warn("TabbedPricelistController.TableDefinitionStk.createTableDataModel() finderElem == null for " + qid);
                        continue;
                    }
                    if (finderElem.getFiscalyear() != null && this.currentYear.equals(finderElem.getFiscalyear())) {
                        hasCurrentFiscalYear = true;
                    }
                    addTableRow(tdm, listEntry, finderElem, mapPriceData.get(qid));
                }
            }
            else {
                tdm.withSort(result.getSort());
                final Map<String, KwtProductMap.ListEntry> listEntryMap = createEntryMap(listEntries);
                for (final STKFinderElement finderElem : result.getElement()) {
                    final String qid = finderElem.getQuotedata().getQid();
                    if (finderElem.getFiscalyear() != null && this.currentYear.equals(finderElem.getFiscalyear())) {
                        hasCurrentFiscalYear = true;
                    }
                    addTableRow(tdm, listEntryMap.get(qid), finderElem, mapPriceData.get(qid));
                }
            }
            final String currentYearSuffix = hasCurrentFiscalYear ? ("<br/>(" + this.currentYear + ")") : ""; // $NON-NLS$
            this.columnPriceEarningRatio.setTitle(I18n.I.priceEarningsRatioAbbrE() + currentYearSuffix);
            this.columnDividend.setTitle(I18n.I.dividend() + currentYearSuffix);
            this.columnDividendYield.setTitle(I18n.I.dividendYieldAbbrNoBreak() + currentYearSuffix);
            return tdm;
        }

        private void addTableRow(FlexTableDataModel tdm, KwtProductMap.ListEntry listEntry, STKFinderElement finderElem, MSCPriceDataExtendedElement pde) {
            if (finderElem == null) {
                if (pde != null) {
                    Firebug.warn("TabbedPricelistController.addTableRow() finderElem == null for pde " + pde.getQuotedata().getQid());
                }
                else if (listEntry != null) {
                    Firebug.warn("TabbedPricelistController.addTableRow() finderElem == null for listEntry " + listEntry.getQwi().getQuoteData().getQid());
                }
                else {
                    Firebug.warn("TabbedPricelistController.addTableRow() finderElem == null for ???");
                }
                return;
            }
            final InstrumentData instrumentData = finderElem.getInstrumentdata();
            final QuoteData quoteData = finderElem.getQuotedata();
            pdfSymbols.add(quoteData.getQid());
            final String fiscalyear = finderElem.getFiscalyear() == null || this.currentYear.equals(finderElem.getFiscalyear()) ?
                    "" : ("<br/>(" + finderElem.getFiscalyear() + ")"); // $NON-NLS$
            tdm.addValues(new Object[]{
                    finderElem.getSector(),
                    new QuoteWithInstrument(instrumentData, quoteData),
                    instrumentData.getIsin(),
                    finderElem.getPrice(),
                    quoteData.getCurrencyIso(),
                    finderElem.getDate(),
                    quoteData.getMarketVwd(),
                    pde == null ? null : pde.getPricedata().getPreviousClose(),
                    pde == null ? null : pde.getPricedata().getChangePercent(),
                    finderElem.getPerformanceCurrentYear(),
                    finderElem.getPerformance1Y(),
                    finderElem.getPerformance3Y(),
                    pde == null ? null : pde.getPricedata().getHighYear(),
                    pde == null ? null : pde.getPricedata().getLowYear(),
                    Renderer.PRICE_MAX2.render(finderElem.getPriceEarningRatio1Y()) + fiscalyear,
                    finderElem.getDividend1Y() == null ? null : (Renderer.PRICE.render(finderElem.getDividend1Y()) + "&nbsp;" + finderElem.getDividendCurrency() + fiscalyear), // $NON-NLS$
                    finderElem.getDividendYield1Y() == null ? null : (Renderer.PERCENT.render(finderElem.getDividendYield1Y()) + fiscalyear),
                    finderElem.getRecommendation(),
                    listEntry == null ? null : listEntry.getNote(),
                    getUrl(listEntry),
            });
        }
    }

    private static Map<String, String> createFinderFieldNamesMapping() {
        final Map<String, String> map = new HashMap<>();
        map.put("name", "name"); // $NON-NLS$
        map.put("isin", KwtProductMap.CSV_FIELD_ISIN); // $NON-NLS$
        map.put("coupon", KwtProductMap.CSV_FIELD_COUPON); // $NON-NLS$
        map.put("price", KwtProductMap.CSV_FIELD_PRICE); // $NON-NLS$
        map.put("expirationDate", KwtProductMap.CSV_FIELD_MATURITY); // $NON-NLS$
        map.put("yieldRelativePerYear", KwtProductMap.CSV_FIELD_YIELD); // $NON-NLS$
        map.put("duration", KwtProductMap.CSV_FIELD_DURATION); // $NON-NLS$
        return map;
    }

    private class TableDefinitionBnd extends FinderTableDefinition<BNDFinder> {
        private final Map<String, String> MAP_FINDER_FIELD_NAMES = createFinderFieldNamesMapping();

        private TableDefinitionBnd() {
            super("BND_Finder", true); // $NON-NLS$
            this.columnOrder = new String[]{ "nm", "is", "ci", "cu", "dt", "mv", "pr", "yi", "ed", "bt", "ic", "kn", "url" }; // $NON-NLS$
        }

        public TableColumn[] createColumnArray() {
            return new TableColumn[]{
                    new TableColumn(I18n.I.name(), -1f, DEFAULT, "name").withId("nm"), // $NON-NLS$
                    new TableColumn("ISIN", -1f, DEFAULT, "isin").withId("is"), // $NON-NLS$
                    new TableColumn(I18n.I.currency(), -1f, DEFAULT, "currency").withId("ci"), // $NON-NLS$
                    new TableColumn(I18n.I.coupon(), -1f, DEFAULT, "coupon").withId("cu"), // $NON-NLS$
                    new TableColumn(I18n.I.time(), -1f, COMPACT_DATE_OR_TIME).alignRight().withId("dt"), // $NON-NLS$
                    new TableColumn(I18n.I.market(), -1f, DEFAULT_CENTER, "market").withId("mv"), // $NON-NLS$
                    new TableColumn(I18n.I.price(), -1f, DEFAULT_RIGHT, "price").withId("pr"), // $NON-NLS$
                    new TableColumn(I18n.I.yield(), -1f, DEFAULT_RIGHT, "yieldRelativePerYear").withId("yi"), // $NON-NLS$
                    new TableColumn(I18n.I.expirationDate(), -1f, DATE_RIGHT, "expirationDate").withId("ed"), // $NON-NLS$
                    new TableColumn(I18n.I.bondType(), -1f, DEFAULT, "bondType").withId("bt"), // $NON-NLS$
                    new TableColumn(I18n.I.issuerCategoryAbbr(), -1f, DEFAULT, "issuerCategory").withId("ic"), // $NON-NLS$
                    new TableColumn(I18n.I.kwtNotices(), -1f, DEFAULT).withId("kn"), // $NON-NLS$
                    new TableColumn(I18n.I.url(), -1f, DEFAULT).withCellClass("mm-nobreak").withId("url"), // $NON-NLS$
            };
        }

        private String getKwtFieldName(String fieldName) {
            final String s = MAP_FINDER_FIELD_NAMES.get(fieldName);
            return s == null ? fieldName : s;
        }

        public TableDataModel createTableDataModel() {
            final FlexTableDataModel tdm = new FlexTableDataModel(this.columnCount);
            final ArrayList<KwtProductMap.ListEntry> listEntriesBnd = this.list.getListEntries("BND"); // $NON-NLS$
            final ArrayList<KwtProductMap.ListEntry> listEntriesWithLocalData = new ArrayList<>();

            for (KwtProductMap.ListEntry entry : listEntriesBnd) {
                if (entry.getQwi() == null && entry.hasData()) {
                    listEntriesWithLocalData.add(entry);
                }
            }
            final String sortBy = this.block.getParameter("sortBy"); // $NON-NLS$
            final boolean ascending = !"false".equals(block.getParameter("ascending")); // $NON-NLS$
            if (sortBy != null) {
                Collections.sort(listEntriesWithLocalData, new KwtProductMap.ListEntryComparator(getKwtFieldName(block.getParameter("sortBy")), ascending)); // $NON-NLS$
            }
            for (KwtProductMap.ListEntry entry : listEntriesWithLocalData) {
                addTableRow(tdm, entry);
            }
            if (this.block.isEnabled()) {
                final BNDFinder result = this.block.getResult();
                final HashMap<String, MSCPriceDataExtendedElement> mapPriceData = createPriceDataMap(this.blockPriceData);
                if (sortBy == null) {
                    tdm.withSort(null, true, result.getSort().getField());
                    final Map<String, BNDFinderElement> mapFinderElements = createFinderElementMap(result.getElement());
                    for (KwtProductMap.ListEntry listEntry : listEntriesBnd) {
                        if (listEntry.getQwi() != null) {
                            final String qid = listEntry.getQwi().getQuoteData().getQid();
                            addTableRow(tdm, mapFinderElements.get(qid), mapPriceData.get(qid), listEntry);
                        }
                    }
                }
                else {
                    tdm.withSort(result.getSort());
                    final Map<String, KwtProductMap.ListEntry> listEntryMap = createEntryMap(listEntriesBnd);
                    for (BNDFinderElement finderElem : result.getElement()) {
                        final String qid = finderElem.getQuotedata().getQid();
                        addTableRow(tdm, finderElem, mapPriceData.get(qid), listEntryMap.get(qid));
                    }
                }
            }
            else {
                tdm.withSort(sortBy, ascending, new ArrayList<>(MAP_FINDER_FIELD_NAMES.keySet()));
            }
            return tdm;
        }

        private void addTableRow(FlexTableDataModel tdm, KwtProductMap.ListEntry entry) {
            tdm.addValues(new Object[]{
                    entry.getName(),
                    entry.getData(KwtProductMap.CSV_FIELD_ISIN),
                    null,
                    entry.getData(KwtProductMap.CSV_FIELD_COUPON),
                    entry.getData(KwtProductMap.CSV_FIELD_PRICEDATE),
                    null,
                    entry.getData(KwtProductMap.CSV_FIELD_PRICE),
                    entry.getData(KwtProductMap.CSV_FIELD_YIELD),
                    entry.getData(KwtProductMap.CSV_FIELD_MATURITY),
                    null,
                    null,
                    entry.getNote(),
                    getUrl(entry)
            });
        }

        private void addTableRow(FlexTableDataModel tdm, BNDFinderElement e, MSCPriceDataExtendedElement pde, KwtProductMap.ListEntry listEntry) {
            if (e == null) {
                if (listEntry != null) {
                    Firebug.warn("TabbedPricelistController.TableDefinitionBnd.addTableRow() e == null for " + listEntry.getQwi().getQuoteData().getQid());

                    final InstrumentData instrumentData = listEntry.getQwi().getInstrumentData();
                    final QuoteData quoteData = listEntry.getQwi().getQuoteData();
                    pdfSymbols.add(quoteData.getQid());
                    final Price price = pde == null ? null : Price.create(pde);
                    tdm.addValues(new Object[]{
                            new QuoteWithInstrument(instrumentData, quoteData),
                            instrumentData.getIsin(),
                            quoteData.getCurrencyIso(),
                            null,
                            price == null ? null : (price.getDate() == null ? price.getBidAskDate() : price.getDate()),
                            quoteData.getMarketVwd(),
                            price,
                            null,
                            null,
                            null,
                            null,
                            listEntry.getNote(),
                            getUrl(listEntry),
                    });
                }
                else if (pde != null) {
                    Firebug.warn("TabbedPricelistController.TableDefinitionBnd.addTableRow() e == null for " + pde.getQuotedata().getQid());
                }
                else {
                    Firebug.warn("TabbedPricelistController.TableDefinitionBnd.addTableRow() e == null for ???");
                }
            }
            else {
                final InstrumentData instrumentData = e.getInstrumentdata();
                final QuoteData quoteData = e.getQuotedata();
                pdfSymbols.add(quoteData.getQid());
                final Price price = pde == null ? null : Price.create(pde);
                tdm.addValues(new Object[]{
                        new QuoteWithInstrument(instrumentData, quoteData),
                        instrumentData.getIsin(),
                        quoteData.getCurrencyIso(),
                        Renderer.PERCENT.render(e.getCoupon()),
                        price == null ? null : (price.getDate() == null ? price.getBidAskDate() : price.getDate()),
                        quoteData.getMarketVwd(),
                        price,
                        Renderer.PERCENT.render(e.getYieldRelativePerYear()),
                        e.getExpirationDate(),
                        e.getBondType(),
                        e.getIssuerCategory(),
                        listEntry == null ? null : listEntry.getNote(),
                        getUrl(listEntry),
                });
            }
        }
    }

    private class TableDefinitionFnd extends FinderTableDefinition<FNDFinder> {
        private TableDefinitionFnd() {
            super("FND_Finder", false); // $NON-NLS$
            this.columnOrder = new String[]{ "nm", "is", "ft", "if", "in", "pytd", "p1y", "p3y", "dt", "rp", "ci", "isc", "ogc", "ter", "mf", "ma", "kn", "url" }; // $NON-NLS$
        }

        public TableColumn[] createColumnArray() {
            return new TableColumn[]{
                    new TableColumn(I18n.I.name(), -1f, QUOTELINK_32, "name").withId("nm"), // $NON-NLS$
                    new TableColumn("ISIN", -1f, DEFAULT, "isin").withId("is"), // $NON-NLS$
                    new TableColumn(I18n.I.fundType(), -1f, DEFAULT, "fundtype").withId("ft"), // $NON-NLS$
                    new TableColumn(I18n.I.investmentFocus(), -1f, DEFAULT, "investmentFocus").withId("if"), // $NON-NLS$
                    new TableColumn(I18n.I.issuer(), -1f, new TableCellRenderers.MultiLineMaxLengthStringRenderer(2, 20, "--"), "issuername").withId("in"), // $NON-NLS$
                    new TableColumn(I18n.I.performanceAbbr("YTD"), -1f, CHANGE_PERCENT, "bviperformanceCurrentYear").withCellClass("mm-right mm-nobreak").withId("pytd"), // $NON-NLS$
                    new TableColumn(I18n.I.performanceAbbr(I18n.I.nYearAbbr(1)), -1f, CHANGE_PERCENT, "bviperformance1y").withCellClass("mm-right mm-nobreak").withId("p1y"), // $NON-NLS$
                    new TableColumn(I18n.I.performanceAbbr(I18n.I.nYearAbbr(3)), -1f, CHANGE_PERCENT, "bviperformance3y").withCellClass("mm-right mm-nobreak").withId("p3y"), // $NON-NLS$
                    new TableColumn(I18n.I.date(), -1f, DATE_RIGHT).withId("dt"), // $NON-NLS$
                    new TableColumn(I18n.I.redemption(), -1f, PRICE).withId("rp"), // $NON-NLS$
                    new TableColumn(I18n.I.currency(), -1f, DEFAULT, "currency").withId("ci"), // $NON-NLS$
                    new TableColumn(I18n.I.issueSurcharge(), -1f, PERCENT, "issueSurcharge").withId("isc"), // $NON-NLS$
                    new TableColumn(I18n.I.ongoingCharges(), -1f, PERCENT, "ogc").withId("ogc"), // $NON-NLS$
                    new TableColumn(I18n.I.totalExpenseRatio(), -1f, PERCENT, "ter").withId("ter"), // $NON-NLS$
                    new TableColumn(I18n.I.managementFee(), -1f, PERCENT, "managementFee").withId("mf"), // $NON-NLS$
                    new TableColumn(I18n.I.marketAdmission(), -1f, DEFAULT, "marketAdmission").withId("ma"), // $NON-NLS$
                    new TableColumn(I18n.I.kwtNotices(), -1f, DEFAULT).withId("kn"), // $NON-NLS$
                    new TableColumn(I18n.I.url(), -1f, DEFAULT).withCellClass("mm-nobreak").withId("url"), // $NON-NLS$
            };
        }

        public FlexTableDataModel createTableDataModel() {
            final FNDFinder result = this.block.getResult();
            final FlexTableDataModel tdm = new FlexTableDataModel(this.columnCount);
            final ArrayList<KwtProductMap.ListEntry> listEntries = list.getListEntries("FND"); // $NON-NLS$
            if (this.block.getParameter("sortBy") == null) { // $NON-NLS$
                tdm.withSort(null, true, result.getSort().getField());
                final Map<String, FNDFinderElement> mapFinderElements = createFinderElementMap(result.getElement());
                for (KwtProductMap.ListEntry listEntry : listEntries) {
                    final String qid = listEntry.getQwi().getQuoteData().getQid();
                    addTableRow(tdm, listEntry, mapFinderElements.get(qid));
                }
            }
            else {
                tdm.withSort(result.getSort());
                final Map<String, KwtProductMap.ListEntry> listEntryMap = createEntryMap(listEntries);
                for (final FNDFinderElement finderElem : result.getElement()) {
                    final String qid = finderElem.getQuotedata().getQid();
                    addTableRow(tdm, listEntryMap.get(qid), finderElem);
                }
            }
            return tdm;
        }

        private void addTableRow(FlexTableDataModel tdm, KwtProductMap.ListEntry listEntry, FNDFinderElement finderElem) {
            if (finderElem == null) {
                if (listEntry != null) {
                    tdm.addValues(new Object[]{
                            listEntry.getQwi(),
                            listEntry.getQwi().getInstrumentData().getIsin(),
                            null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                            listEntry.getNote(),
                            getUrl(listEntry),
                    });
                }
                return;
            }
            final InstrumentData instrumentData = finderElem.getInstrumentdata();
            final QuoteData quoteData = finderElem.getQuotedata();
            pdfSymbols.add(quoteData.getQid());
            tdm.addValues(new Object[]{
                    new QuoteWithInstrument(instrumentData, quoteData),
                    instrumentData.getIsin(),
                    finderElem.getFundtype(),
                    finderElem.getInvestmentFocus(),
                    finderElem.getIssuername(),
                    finderElem.getBviperformanceCurrentYear(),
                    finderElem.getBviperformance1Y(),
                    finderElem.getBviperformance3Y(),
                    finderElem.getDate(),
                    finderElem.getRepurchasingPrice(),
                    finderElem.getQuotedata().getCurrencyIso(),
                    finderElem.getIssueSurcharge(),
                    finderElem.getOngoingCharge(),
                    finderElem.getTer(),
                    finderElem.getManagementFee(),
                    finderElem.getMarketAdmission(),
                    listEntry == null ? null : listEntry.getNote(),
                    getUrl(listEntry),
            });
        }
    }

    private class TableDefinitionCer extends FinderTableDefinition<CERFinder> {
        private TableDefinitionCer() {
            super("CER_Finder", false); // $NON-NLS$
            this.block.setParameter("providerPreference", SessionData.INSTANCE.getGuiDefValue("providerPreference")); // $NON-NLS$
            this.columnOrder = new String[]{ "is", "nm", "in", "un", "cat", "yr", "ed", "uy", "cap", "bad", "bl", "sl", "ar", "kn", "url" }; // $NON-NLS$
        }

        public TableColumn[] createColumnArray() {
            return new TableColumn[]{
                    new TableColumn("ISIN", -1f, DEFAULT, "isin").withId("is"), // $NON-NLS$
                    new TableColumn(I18n.I.name(), -1f, QUOTELINK_32, "name").withId("nm"), // $NON-NLS$
                    new TableColumn(I18n.I.issuer(), -1f, DEFAULT, "issuername").withId("in"), // $NON-NLS$
                    new TableColumn(I18n.I.underlying(), -1f, DEFAULT, "underlyingName").withId("un"), // $NON-NLS$
                    new TableColumn(I18n.I.category(), -1f, DEFAULT, "certificateTypeDZBANK").withId("cat"), // $NON-NLS$
                    new TableColumn(I18n.I.yieldRelativePerYearAbbr(), -1f, PERCENT, "yieldRelativePerYear").withId("yr"), // $NON-NLS$
                    new TableColumn(I18n.I.expirationDate(), -1f, DATE_RIGHT, "expirationDate").withId("ed"), // $NON-NLS$
                    new TableColumn(I18n.I.unchangedYieldRelativePerYearAbbr(), -1f, PERCENT, "unchangedYieldRelativePerYear").withId("uy"), // $NON-NLS$
                    new TableColumn(I18n.I.cap(), -1f, PRICE23, "cap").withId("cap"), // $NON-NLS$
                    new TableColumn(I18n.I.bufferAndDiscount(), -1f, PERCENT).withId("bad"), // $NON-NLS$
                    new TableColumn(I18n.I.bonusLevel(), -1f, PRICE23, "bonusLevel").withId("bl"), // $NON-NLS$
                    new TableColumn(I18n.I.securitiesLevel(), -1f, PRICE23, "barrier").withId("sl"), // $NON-NLS$
                    new TableColumn(I18n.I.agioRelativePerYearAbbr(), -1f, PERCENT, "agioRelativePerYear").withId("ar"), // $NON-NLS$
                    new TableColumn(I18n.I.kwtNotices(), -1f, DEFAULT).withId("kn"), // $NON-NLS$
                    new TableColumn(I18n.I.url(), -1f, DEFAULT).withCellClass("mm-nobreak").withId("url"), // $NON-NLS$
            };
        }

        private String getBufferOrDiscount(CERFinderElement finderElem) {
            final String certificateSubtype = finderElem.getCertificateSubtype();
            if (certificateSubtype != null) {
                if (certificateSubtype.toLowerCase().contains("bonus")) { // $NON-NLS$
                    return finderElem.getUnderlyingToCapRelative();
                }
                else if (certificateSubtype.toLowerCase().contains("aktienanleihe")) { // $NON-NLS$
                    return finderElem.getGapStrikeRelative();
                }
            }
            return finderElem.getDiscountRelative();
        }

        public FlexTableDataModel createTableDataModel() {
            final CERFinder result = this.block.getResult();
            final FlexTableDataModel tdm = new FlexTableDataModel(this.columnCount);
            final ArrayList<KwtProductMap.ListEntry> listEntries = list.getListEntries("CER"); // $NON-NLS$
            if (this.block.getParameter("sortBy") == null) { // $NON-NLS$
                tdm.withSort(null, true, result.getSort().getField());
                final Map<String, CERFinderElement> mapFinderElements = createFinderElementMap(result.getElement());
                for (KwtProductMap.ListEntry listEntry : listEntries) {
                    final String qid = listEntry.getQwi().getQuoteData().getQid();
                    addTableRow(tdm, listEntry, mapFinderElements.get(qid));
                }
            }
            else {
                tdm.withSort(result.getSort());
                final Map<String, KwtProductMap.ListEntry> listEntryMap = createEntryMap(listEntries);
                for (final CERFinderElement finderElem : result.getElement()) {
                    final String qid = finderElem.getQuotedata().getQid();
                    addTableRow(tdm, listEntryMap.get(qid), finderElem);
                }
            }
            return tdm;
        }

        private void addTableRow(FlexTableDataModel tdm, KwtProductMap.ListEntry listEntry, final CERFinderElement finderElem) {
            if (finderElem == null) {
                if (listEntry != null) {
                    tdm.addValues(new Object[]{
                            listEntry.getQwi().getInstrumentData().getIsin(),
                            listEntry.getQwi(),
                            null, null, null, null, null, null, null, null, null, null, null,
                            listEntry.getNote(),
                            getUrl(listEntry),
                    });
                }
                return;
            }
            final InstrumentData instrumentData = finderElem.getInstrumentdata();
            final QuoteData quoteData = finderElem.getQuotedata();
            pdfSymbols.add(quoteData.getQid());
            tdm.addValues(new Object[]{
                    instrumentData.getIsin(),
                    new QuoteWithInstrument(instrumentData, quoteData),
                    finderElem.getIssuername(),
                    createUnderlyingInfo(finderElem.getUnderlyingName(), finderElem.getUnderlyingIsin(), finderElem.getUnderlyingType()),
                    finderElem.getCertificateTypeDZBANK(),
                    finderElem.getYieldRelativePerYear(),
                    finderElem.getExpirationDate(),
                    finderElem.getUnchangedYieldRelativePerYear(),
                    finderElem.getCap(),
                    getBufferOrDiscount(finderElem),
                    finderElem.getBonusLevel(),
                    finderElem.getBarrier(),
                    hasAgio(finderElem) ? finderElem.getAgioRelativePerYear() : null,
                    listEntry == null ? null : listEntry.getNote(),
                    getUrl(listEntry),
            });
        }
        
        private Object createUnderlyingInfo(final String name, final String isin, final String type) {
            return isin == null ?
                    name :
                    new Link(new LinkListener<Link>() {
                        public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                            final String token1;
                            switch (type) {
                                case "STO":  // $NON-NLS$
                                    token1 = "P_STK"; // $NON-NLS$
                                    break;
                                case "IND":  // $NON-NLS$
                                    token1 = "P_IND"; // $NON-NLS$
                                    break;
                                default:
                                    token1 = "M_S"; // $NON-NLS$
                                    break;
                            }
                            PlaceUtil.goTo(token1 + TOKEN_DIVIDER + isin);
                        }
                    }, name, isin);
        }

        private boolean hasAgio(CERFinderElement finderElem) {
            return isType(finderElem, CertificateTypeEnum.CERT_BONUS, CertificateTypeEnum.KNOCK, CertificateTypeEnum.CERT_OUTPERFORMANCE);
        }

        private boolean isType(CERFinderElement finderElem, CertificateTypeEnum... types) {
            if (finderElem == null) {
                return false;
            }
            for (CertificateTypeEnum type : types) {
                final CertificateTypeEnum elemType;
                try {
                    elemType = CertificateTypeEnum.valueOf(finderElem.getCertificateType());
                    if (elemType == type) {
                        return true;
                    }
                }
                catch (IllegalArgumentException e) {
                    // ignore
                }
            }
            return false;
        }
    }


    @Override
    public void destroy() {
        this.context.removeBlock(this.block);
        super.destroy();
    }

    public KwtProductMap getProductMap() {
        if (this.productMap == null) {
            this.productMap = new KwtProductMap();
        }
        return this.productMap;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        if (historyToken.getAllParamCount() < 2) {
            this.tableView.show(DefaultTableDataModel.NULL);
            return;
        }
        final String listid = historyToken.get("listid"); // $NON-NLS$
        final String itype = historyToken.get("itype"); // $NON-NLS$
        final String instrumentType = itype == null ? TabbedPricelistTabController.DEFAULT_TAB_ID : itype;
        this.pdfSymbols.clear();
        this.listTitle = getProductMap().getList(listid, new KwtProductMap.ListCallback() {
            public void onListAvailable(KwtProductMap.List list) {
                loadPrices(list, instrumentType);
            }

            public void onListNotAvailable() {
                tableView.show(DefaultTableDataModel.NULL);
            }
        });
    }

    protected HashMap<String, MSCPriceDataExtendedElement> createPriceDataMap(DmxmlContext.Block<MSCPriceDataExtended> block) {
        final HashMap<String, MSCPriceDataExtendedElement> map = new HashMap<>();
        if (block == null) {
            throw new NullPointerException("TabbedPricelistController.FinderTableDefinition: blockPriceData not initialized"); // $NON-NLS$
        }
        if (block.isEnabled() && block.isResponseOk()) {
            for (MSCPriceDataExtendedElement element : block.getResult().getElement()) {
                map.put(element.getQuotedata().getQid(), element);
            }
        }
        return map;
    }

    private void loadPrices(KwtProductMap.List list, String instrumentType) {
        final ArrayList<String> symbols = new ArrayList<>(list.getListEntries().length);
        boolean hasValidEntries = false;
        for (KwtProductMap.ListEntry listEntry : list.getListEntries()) {
            if (instrumentType == null || TabbedPricelistTabController.INSTRUMENT_TYPE_ALL.equals(instrumentType) || instrumentType.equals(listEntry.getInstrumentType())) {
                final QuoteWithInstrument qwi = listEntry.getQwi();
                if (qwi != null) {
                    symbols.add(qwi.getQuoteData().getQid());
                    hasValidEntries = true;
                }
                else if (listEntry.hasData()) {
                    hasValidEntries = true;
                }
            }
        }

        this.localInstruments = false;

        if (!hasValidEntries) {
            this.tableView.show(DefaultTableDataModel.NULL);
            return;
        }

        this.tableDefinition.setList(list, symbols.toArray(new String[symbols.size()]));
        if (symbols.isEmpty()) {
            this.localInstruments = true;
            this.pdfSymbols.clear();
            this.tableView.show(this.tableDefinition.createTableDataModel());
        }
        else {
            refresh();
        }
    }

    @Override
    protected void onResult() {
        if (!block.isResponseOk()) {
            this.tableView.show(DefaultTableDataModel.NULL);
            return;
        }

        this.pdfSymbols.clear();
        this.tableView.show(this.tableDefinition.createTableDataModel());
        AbstractMainController.INSTANCE.getView().getTopToolbar().updatePdfButtonState();
    }

    private <T> Map<String, T> createFinderElementMap(List<T> listFinderElements) {
        final Map<String, T> map = new HashMap<>();
        for (T t : listFinderElements) {
            map.put(getQid(t), t);
        }
        return map;
    }

    private String getQid(Object e) {
        if (e instanceof STKFinderElement) {
            return ((STKFinderElement) e).getQuotedata().getQid();
        }
        else if (e instanceof BNDFinderElement) {
            return ((BNDFinderElement) e).getQuotedata().getQid();
        }
        else if (e instanceof CERFinderElement) {
            return ((CERFinderElement) e).getQuotedata().getQid();
        }
        else if (e instanceof FNDFinderElement) {
            return ((FNDFinderElement) e).getQuotedata().getQid();
        }
        throw new IllegalArgumentException("Not a FinderElement: " + e.getClass().getName()); // $NON-NLS$
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        if (this.pdfSymbols.isEmpty()) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        String divider = "";
        for (String symbol : pdfSymbols) {
            sb.append(divider).append(symbol.replace(".qid", "")); // $NON-NLS$
            divider = "@";
        }
        final Map<String, String> map = new HashMap<>();
        map.put("instrumentType", this.instrumentType); // $NON-NLS$
        map.put("symbols", sb.toString()); // $NON-NLS$
        map.put("listTitle", this.listTitle); // $NON-NLS$
        map.put("orderids", this.tableColumnModel.getOrderString()); // $NON-NLS-0$
        return new PdfOptionSpec("kwtproductmap.pdf", map, "pdf_options_format"); // $NON-NLS$
    }

    public void configureColumns() {
        ColumnConfigurator.show(this.tableColumnModel, new Command() {
            public void execute() {
                if (localInstruments) {
                    tableView.show(tableDefinition.createTableDataModel());
                }
                else {
                    refresh();
                }
            }
        });
    }
}
