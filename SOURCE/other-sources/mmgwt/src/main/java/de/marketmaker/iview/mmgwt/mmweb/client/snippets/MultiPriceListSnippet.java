/*
 * MultiPriceListSnippet.java
 *
 * Created on Jan 20, 2009 8:40:41 AM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.json.client.JSONArray;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.FUTSymbolElement;
import de.marketmaker.iview.dmxml.FUTSymbolFinder;
import de.marketmaker.iview.dmxml.IdentifierData;
import de.marketmaker.iview.dmxml.MSCPriceDataExtended;
import de.marketmaker.iview.dmxml.MSCPriceDataExtendedElement;
import de.marketmaker.iview.dmxml.MSCStaticData;
import de.marketmaker.iview.dmxml.MSCStaticDataList;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.QwiPosition;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.data.CurrentTrendBar;
import de.marketmaker.iview.mmgwt.mmweb.client.data.InstrumentTypeEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.TrendBarData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PricesUpdatedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PushRegisterHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceSupport;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Michael Lösch
 */
public class MultiPriceListSnippet extends
        AbstractSnippet<MultiPriceListSnippet, MultiPriceListView> implements PushRegisterHandler,
        PdfUriSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("MultiPriceList"); // $NON-NLS$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new MultiPriceListSnippet(context, config);
        }
    }

    public enum Type {
        DEFAULT, COMPACT
    }

    /**
     * Definition of a single list
     */
    static class ListDef {
        private static final String CON = "CON"; // $NON-NLS$

        private final String name;

        private final List<String> symbols = new ArrayList<>();

        private List<String> symbolPrefixes;

        private final boolean columnHeader;

        private final boolean withCon;

        private final int limit;

        private final boolean lmeCurrentPreviousGroups;

        ListDef(String name, List<String> symbols, boolean hasColumnHeader,
                boolean lmeCurrentPreviousGroups) {
            this(name, null, symbols, false, -1, hasColumnHeader, lmeCurrentPreviousGroups);
        }

        ListDef(String name, List<String> symbolPrefixes, boolean withCon, int limit,
                boolean columnHeader, boolean lmeCurrentPreviousGroups) {
            this(name, symbolPrefixes, null, withCon, limit, columnHeader, lmeCurrentPreviousGroups);
        }

        ListDef(String name, List<String> symbolPrefixes, List<String> symbols,
                boolean withCon, int limit, boolean columnHeader,
                boolean lmeCurrentPreviousGroups) {
            this.name = name;
            this.symbolPrefixes = symbolPrefixes;
            if (symbols != null) {
                this.symbols.addAll(symbols);
            }
            this.withCon = withCon;
            this.limit = limit;
            this.columnHeader = columnHeader;
            this.lmeCurrentPreviousGroups = lmeCurrentPreviousGroups;
        }

        public String getName() {
            return name;
        }

        public List<String> getSymbolPrefixes() {
            return symbolPrefixes;
        }

        boolean hasColumnHeader() {
            return this.columnHeader;
        }

        public List<String> getSymbols() {
            return symbols;
        }

        private boolean hasSymbolPrefix(String prefix) {
            return this.symbolPrefixes.contains(prefix);
        }

        public boolean isLmeCurrentPreviousGroups() {
            return lmeCurrentPreviousGroups;
        }

        private void extractSymbols(FUTSymbolElement element) {
            final List<IdentifierData> futures = element.getFuture();
            for (IdentifierData future : futures) {
                final String vwdcode = future.getQuotedata().getVwdcode();
                if (vwdcode.endsWith(CON) && this.withCon) {
                    this.symbols.add(0, vwdcode);
                }
                else if (!vwdcode.endsWith(CON)) {
                    this.symbols.add(vwdcode);
                }
                if (this.limit > 0 && this.symbols.size() >= this.limit) {
                    break;
                }
            }
        }

        public void resolvePrefixSymbols(List<FUTSymbolElement> elements) {
            if (this.symbolPrefixes == null) {
                return;
            }
            for (FUTSymbolElement element : elements) {
                if (hasSymbolPrefix(element.getPrefix())) {

                    extractSymbols(element);
                }
            }
            this.symbolPrefixes = null;
        }
    }

    /**
     * Definition of a list of ListDefs, usually a page
     */
    private static class MultiListDef {
        private List<ListDef> listDefs = new ArrayList<>();

        private final Type type;

        private final String id;

        private boolean isWithPrefixes = false;

        private final String period;

        MultiListDef(String id, Type type, String period) {
            this.id = id;
            this.type = type;
            this.period = period;
        }

        void add(ListDef def) {
            this.listDefs.add(def);
            this.isWithPrefixes |= (def.getSymbolPrefixes() != null);
        }

        List<String> getSymbols() {
            final List<String> symbols = new ArrayList<>();
            for (ListDef element : this.listDefs) {
                symbols.addAll(element.getSymbols());
            }
            return symbols;
        }

        List<String> getSymbolPrefixes() {
            if (!this.isWithPrefixes) {
                return Collections.emptyList();
            }
            final List<String> symbols = new ArrayList<>();
            for (ListDef element : listDefs) {
                if (element.symbolPrefixes != null) {
                    symbols.addAll(element.getSymbolPrefixes());
                }
            }
            return symbols;
        }

        public void resolvePrefixSymbols(List<FUTSymbolElement> elements) {
            for (ListDef listDef : this.listDefs) {
                listDef.resolvePrefixSymbols(elements);
            }
            this.isWithPrefixes = false;
        }

        public String getPeriod() {
            return period;
        }
    }

    private static final String DEF_PREFIX = "multilist_"; // $NON-NLS$

    private final boolean listSymbolsAreQids;

    private final boolean onlyTopColumnHeader;

    private final boolean lmeCurrentPreviousGroups;

    private final PriceSupport priceSupport = new PriceSupport(this);

    private DmxmlContext.Block<MSCPriceDataExtended> priceBlock;

    private DmxmlContext.Block<MSCStaticDataList> underlyingBlock;

    private Map<String, MultiListDef> listDefsByName = new HashMap<>();

    private DefaultTableDataModel[] dataModels;

    private MultiListDef current = null;

    public MultiPriceListSnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.priceBlock = this.context.addBlock("MSC_PriceDataExtended"); // $NON-NLS$
        this.priceBlock.setParameter("onlyEntitledQuotes", "true"); // $NON-NLS$
        this.priceBlock.setParameter("symbolStrategy", "AUTO"); // $NON-NLS$
        this.priceBlock.setParameter("currency", configuration.getString("currency", null)); // $NON-NLS$

        this.listSymbolsAreQids = configuration.getBoolean("listSymbolsAreQids", false); // $NON-NLS$
        if (configuration.getBoolean("withUnderlying", false)) { // $NON-NLS$
            this.underlyingBlock = this.context.addBlock("MSC_StaticData_List"); // $NON-NLS$
        }
        this.onlyTopColumnHeader = configuration.getBoolean("onlyTopColumnHeader", false); // $NON-NLS$
        this.lmeCurrentPreviousGroups = configuration.getBoolean("lmeCurrentPreviousGroups", false);  // $NON-NLS$

        setView(new MultiPriceListView(this));
    }

    @Override
    public void deactivate() {
        super.deactivate();
        this.priceSupport.deactivate();
        this.dataModels = null;
    }

    public ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event) {
        if (this.priceBlock.isResponseOk()) {
            final int numAdded = event.addVwdcodes(this.priceBlock.getResult());
            if (numAdded == this.priceBlock.getResult().getElement().size()) {
                event.addComponentToReload(this.priceBlock, this);
            }
            if (numAdded > 0) {
                return this.getView().getRenderItems(this.dataModels);
            }
        }
        return null;
    }

    public void onPricesUpdated(PricesUpdatedEvent event) {
        if (this.priceBlock.isResponseOk() && !event.isPushedUpdate() &&
                !this.priceSupport.isLatestPriceGeneration()) {
            doUpdateView();
        }
    }

    private void doUpdateView() {
        getView().update(this.dataModels);
        this.priceSupport.updatePriceGeneration();
    }

    private void reload(final String id, boolean currencyChanged) {
        if (isCurrent(id)) {
            if (currencyChanged) {
                this.dataModels = null;
            }
            return;
        }

        this.dataModels = null;
        getView().reset();

        this.current = getOrCreateMultiListDef(id);
        if (this.current == null) {
            setBlocksEnabled(false);
            return;
        }

        if (this.current.isWithPrefixes) {
            setBlocksEnabled(false);
            handleSymbolPrefixes(id);
        }
        else {
            useCurrentSymbols();
        }
    }

    private void setBlocksEnabled(final boolean enabled) {
        this.priceBlock.setEnabled(enabled);
        if (this.underlyingBlock != null) {
            this.underlyingBlock.setEnabled(enabled);
        }
    }

    private MultiListDef getOrCreateMultiListDef(String id) {
        final MultiListDef existing = this.listDefsByName.get(id);
        if (existing != null) {
            return existing;
        }

        final JSONWrapper guiDef = SessionData.INSTANCE.getGuiDef(DEF_PREFIX + id);
        if (!guiDef.isValid()) {
            return null;
        }

        final MultiListDef result = new MultiListDef(id, getType(guiDef), getPeriod(guiDef));
        this.listDefsByName.put(result.id, result);

        final JSONWrapper lists = guiDef.get("lists"); // $NON-NLS-0$
        for (int i = 0; i < lists.size(); i++) {
            final boolean hasColumnHeader = (i == 0) || !this.onlyTopColumnHeader;
            result.add(toListDef(lists.get(i), hasColumnHeader, this.lmeCurrentPreviousGroups));
        }

        return result;
    }

    private boolean isCurrent(String id) {
        return this.current != null && StringUtil.equals(this.current.id, id);
    }

    private void useCurrentSymbols() {
        setBlocksEnabled(true);
        final List<String> tmp = this.current.getSymbols();
        final String[] symbols = tmp.toArray(new String[tmp.size()]);
        this.priceBlock.setParameters("symbol", symbols); // $NON-NLS$
        if (this.underlyingBlock != null) {
            this.underlyingBlock.setParameters("symbol", symbols); // $NON-NLS$
        }
        ackParametersChanged();
    }

    private void handleSymbolPrefixes(final String id) {
        final List<String> prefixes = this.current.getSymbolPrefixes();
        final String period = this.current.getPeriod();

        final DmxmlContext.Block<FUTSymbolFinder> futSymbolsBlock
                = new DmxmlContext().addBlock("FUT_SymbolFinder");// $NON-NLS$
        futSymbolsBlock.setParameters("symbolPrefix", prefixes.toArray(new String[prefixes.size()])); // $NON-NLS$
        futSymbolsBlock.setParameter("period", period); // $NON-NLS-0$

        futSymbolsBlock.issueRequest(new ResponseTypeCallback() {
            @Override
            protected void onResult() {
                if (isCurrent(id) && futSymbolsBlock.isResponseOk()) {
                    current.resolvePrefixSymbols(futSymbolsBlock.getResult().getElement());
                    useCurrentSymbols();
                }
            }
        });
    }

    private ListDef toListDef(JSONWrapper wrapper, boolean hasColumnHeader,
            boolean lmeCurrentPreviousGroups) {
        if (wrapper.get("symbolprefix").isValid()) { // $NON-NLS$
            final boolean withCon = wrapper.get("withCon").booleanValue();// $NON-NLS$
            final int limit = wrapper.get("limit").intValue(-1); // $NON-NLS$
            return new ListDef(getListName(wrapper), getSymbolPrefixes(wrapper), withCon, limit, hasColumnHeader, lmeCurrentPreviousGroups);
        }
        return new ListDef(getListName(wrapper), getSymbols(wrapper), hasColumnHeader, lmeCurrentPreviousGroups);
    }

    private Type getType(JSONWrapper guiDef) {
        final String typeStr = guiDef.get("type").stringValue(); // $NON-NLS$
        return (typeStr == null) ? Type.DEFAULT : Type.valueOf(typeStr.toUpperCase());
    }

    private String getPeriod(JSONWrapper guiDef) {
        final String typeStr = guiDef.get("period").stringValue(); // $NON-NLS-0$
        return (typeStr == null) ? "P1Y" : typeStr; // $NON-NLS-0$
    }

    private List<String> getSymbolPrefixes(JSONWrapper wrapper) {
        final JSONWrapper symbolprefix = wrapper.get("symbolprefix"); // $NON-NLS$
        final JSONArray array = symbolprefix.getValue().isArray();
        final ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            result.add(array.get(i).isString().stringValue());
        }
        return result;
    }

    private String getListName(JSONWrapper wrapper) {
        return wrapper.get("list").stringValue(); // $NON-NLS-0$
    }

    private List<String> getSymbols(JSONWrapper wrapper) {
        final List<String> result = new ArrayList<>();
        final JSONWrapper symbols = wrapper.get("symbols"); // $NON-NLS-0$
        for (int n = 0; n < symbols.size(); n++) {
            result.add(symbols.get(n).stringValue());
        }
        return result;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String listId = historyToken.getByNameOrIndex("multilistid", 1); // $NON-NLS$
        if (StringUtil.hasText(listId)) {
            final String currency = historyToken.get("cur"); // $NON-NLS$
            final boolean currencyChanged = this.priceBlock.setParameter("currency", currency);// $NON-NLS$
            reload(listId, currencyChanged);
        }
    }

    public void updateView() {
        if (!isResponseOk()) {
            getView().reset();
            return;
        }
        this.priceSupport.invalidateRenderItems();
        if (this.dataModels == null) {
            this.dataModels = createDataModels();
        }
        doUpdateView();
        this.priceSupport.activate();
    }

    private boolean isResponseOk() {
        return this.priceBlock.isResponseOk()
                && (this.underlyingBlock == null || this.underlyingBlock.isResponseOk());
    }

    private DefaultTableDataModel[] createDataModels() {
        final List<MSCPriceDataExtendedElement> data = this.priceBlock.getResult().getElement();
        final Map<String, MSCPriceDataExtendedElement> map = createDataMap(data);
        final List<ListDef> listDefs = removeEmptyPriceLists(getListDefs());
        final DefaultTableDataModel[] result = new DefaultTableDataModel[listDefs.size()];
        for (int i = 0; i < listDefs.size(); i++) {
            final ListDef listDef = listDefs.get(i);
            final List<RowData> rows = new ArrayList<>();
            final List<String> symbols = listDef.getSymbols();
            final TrendBarData trendBarData = TrendBarData.create(data);
            for (final String symbol : symbols) {
                final MSCPriceDataExtendedElement symbolData = map.get(symbol);
                if (symbolData == null) {
                    continue;
                }
                // hack: change currency name to reduce name length
                StringUtil.reduceCurrencyNameLength(symbolData.getInstrumentdata());
                rows.add(getRowData(trendBarData, symbolData, getUnderlying(symbolData)));
            }
            result[i] = DefaultTableDataModel.createWithRowData(rows);
        }
        return result;
    }

    private List<ListDef> removeEmptyPriceLists(List<ListDef> list) {
        final List<ListDef> result = new ArrayList<>();
        for (ListDef listDef : list) {
            if (listDef.getSymbols().isEmpty()) {
                Firebug.log(getClass().getSimpleName() + " <removeEmptyPriceLists> no data: " + listDef.getName());
            }
            else {
                result.add(listDef);
            }
        }
        return result;
    }

    private IdentifierData getUnderlying(MSCPriceDataExtendedElement symbolData) {
        final MSCStaticDataList underlyings = this.underlyingBlock != null
                ? this.underlyingBlock.getResult() : null;
        if (underlyings == null || underlyings.getElement() == null) {
            return null;
        }
        final List<MSCStaticData> elements = underlyings.getElement();
        for (MSCStaticData element : elements) {
            if (element != null && element.getInstrumentdata() != null &&
                    element.getInstrumentdata().getIid().equals(symbolData.getInstrumentdata().getIid()) &&
                    !element.getUnderlying().isEmpty()) {
                return element.getUnderlying().get(0);
            }
        }
        return null;
    }

    private Map<String, MSCPriceDataExtendedElement> createDataMap(
            List<MSCPriceDataExtendedElement> data) {
        final Map<String, MSCPriceDataExtendedElement> result = new HashMap<>();
        for (final MSCPriceDataExtendedElement element : data) {
            result.put(getKey(element), element);
        }
        return result;
    }

    private String getKey(MSCPriceDataExtendedElement element) {
        if (this.listSymbolsAreQids) {
            return element.getQuotedata().getQid();
        }
        return element.getQuotedata().getVwdcode();
    }

    private RowData getRowData(TrendBarData trendBarData, MSCPriceDataExtendedElement element,
            IdentifierData underlying) {
        final QwiPosition qwi = new QwiPosition(element.getInstrumentdata(), element.getQuotedata(),
                element.getInstrumentdata().getName(), null);
        final QuoteWithInstrument underlyingQwi = underlying == null ? null :
                new QuoteWithInstrument(underlying.getInstrumentdata(), underlying.getQuotedata());

        final Price price = Price.create(element);
        List<Object> rd = new ArrayList<>();
        rd.add(qwi);
        rd.add(getUnderlying(underlyingQwi));
        rd.add(price);
        rd.add(price);
        rd.add(price);
        rd.add(price);
        rd.add(price);
        rd.add(price);
        rd.add(price.getOpen());
        rd.add(price);
        rd.add(price);
        rd.add(price.getClose());
        rd.add(price);
        rd.add(price);
        rd.add(new CurrentTrendBar(price.getChangePercent(), trendBarData));
        rd.add(element.getPricedataExtended().getLmeSubsystemBid());
        rd.add(element.getPricedataExtended().getLmeSubsystemAsk());
        rd.add(price.getLastPrice().getSupplement());
        rd.add(element.getPricedataExtended().getLmeSubsystemBid());
        rd.add(element.getPricedataExtended().getLmeSubsystemAsk());
        rd.add(price.getProvisionalEvaluation());
        rd.add(price.getPreviousPrice().getPrice()); //without supplement
        rd.add(price.getPreviousPrice()); //with supplement
        rd.add(price.getPreviousPrice()); //previous price with supplement, LME grouping
        rd.add(price.getPreviousPrice().getPrice()); //previous price without supplement, LME grouping
        rd.add(price.getPreviousPrice().getSupplement()); //supplement for previous price, LME grouping
        rd.add(price.getPreviousOfficialBid()); //LME grouping
        rd.add(price.getPreviousOfficialAsk()); //LME grouping
        rd.add(price.getPreviousUnofficialBid()); //LME grouping
        rd.add(price.getPreviousUnofficialAsk()); //LME grouping
        rd.add(element.getPricedataExtended().getSettlement());
        return new RowData(rd.toArray(new Object[rd.size()]));
    }

    private Object getUnderlying(QuoteWithInstrument underlyingQwi) {
        if (underlyingQwi == null) {
            return null;
        }
        return InstrumentTypeEnum.valueOf(underlyingQwi.getInstrumentData().getType()) == InstrumentTypeEnum.UND
                ? underlyingQwi.getInstrumentData().getName()
                : underlyingQwi;
    }


    List<ListDef> getListDefs() {
        return this.current != null ? this.current.listDefs : Collections.emptyList();
    }

    public Type getType() {
        return this.current != null ? this.current.type : Type.DEFAULT;
    }

    public PdfOptionSpec getPdfOptionSpec() {
        final Map<String, String> mapParameters = new HashMap<>();
        final StringBuilder sbLists = new StringBuilder();
        final StringBuilder sbValues = new StringBuilder();

        if (this.current != null && this.priceBlock.getResult() != null) {
            final List<MSCPriceDataExtendedElement> elements = this.priceBlock.getResult().getElement();
            final Map<String, MSCPriceDataExtendedElement> map = new HashMap<>();
            for (final MSCPriceDataExtendedElement element : elements) {
                map.put(element.getQuotedata().getVwdcode(), element);
            }
            List<ListDef> listDefs = getListDefs();
            for (int i = 0; i < listDefs.size(); i++) {
                final ListDef listDef = listDefs.get(i);
                if (i > 0) {
                    sbLists.append('-');
                }
                sbLists.append(i);
                mapParameters.put("name" + i, listDef.getName()); // $NON-NLS-0$
                sbValues.setLength(0);

                final List<String> symbols = listDef.getSymbols();
                for (final String symbol : symbols) {
                    final MSCPriceDataExtendedElement element = map.get(symbol);
                    if (element == null) {
                        continue;
                    }
                    if (sbValues.length() > 0) {
                        sbValues.append('-');
                    }
                    sbValues.append(element.getQuotedata().getQid());
                }
                mapParameters.put("values" + i, sbValues.toString()); // $NON-NLS-0$
            }
            mapParameters.put("lists", sbLists.toString()); // $NON-NLS-0$
        }
        return new PdfOptionSpec("multiquotelist.pdf", mapParameters, "pdf_options_format"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public boolean isTopToolbarUri() {
        return true;
    }

    public void destroy() {
        this.priceSupport.deactivate();
        destroyBlock(this.priceBlock);
    }

    boolean isWithUnderlying() {
        return this.underlyingBlock != null;
    }
}
