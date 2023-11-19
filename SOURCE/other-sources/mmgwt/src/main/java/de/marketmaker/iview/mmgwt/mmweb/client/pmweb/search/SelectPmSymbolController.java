/*
 * SelectPmSymbolController.java
 *
 * Created on 04.01.13 10:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolFormControllerInterface;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModelImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.pmxml.SearchType;
import de.marketmaker.iview.pmxml.SearchTypeWP;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.internaltypes.ShellSearchResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.CombinedSearchElement.State.AVAILABLE;
import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search.CombinedSearchElement.State.NOT_AVAILABLE;

/**
 * @author Markus Dick
 */
public class SelectPmSymbolController implements SelectSymbolFormControllerInterface, IndexedViewSelectionModel.Callback,
        PageLoader, AsyncCallback<ResponseType> {

    public enum SearchMode {
        COMPLETE,
        INSTRUMENT,
        FOLDER,
        DEPOT_OBJECT
    }

    private static final int DEFAULT_COUNT = 10;
    private static final String DEFAULT_VIEW_GROUP = "pmsearch"; //$NON-NLS$

    private final SearchMode searchMode;
    private SelectSymbolForm view;
    private final IndexedViewSelectionModelImpl indexedViewSelectionModel;
    private int selectedView;
    private final PagingFeature pagingFeature;
    private TableDataModel tableDataModel;
    private final ArrayList<String> viewTypes = new ArrayList<>();
    private final HashSet<ShellMMType> filterTypes = new HashSet<>();
    private boolean withMsc = false;
    private String[] types = new String[0];
    private DmxmlContext.Block<ShellSearchResult> block;
    private String pagingHandle;
    private boolean updateViewSelection;

    /**
     * Determines which types of securities are available for OrderEntry.
     * Affects only the state of the found security; unavailable securities are left in the list.
     * Use a filter to sort them out.
     * Has no effect, if <code>checkAvailabilityForOrderEntry</code> is @value{false}
     *
     * @see CombinedSearchElement.State#NO_ORDER_ENTRY_DUE_TO_BUSINESS_RULES
     * @see #checkAvailabilityForOrderEntry
     */
    private Set<ShellMMType> typesAllowedForOrderEntry = null;

    /**
     * Determines whether or not to check if a security is available for OrderEntry.
     * If @value{true} issues a request for each entry of the result page.
     *
     * @see CombinedSearchElement.State#AMBIGUOUS_ISIN
     * @see CombinedSearchElement.State#EMPTY_ISIN
     * @see CombinedSearchElement.State#NO_ORDER_ENTRY_DUE_TO_BUSINESS_RULES
     * @see #typesAllowedForOrderEntry
     */
    private boolean checkAvailabilityForOrderEntry = false;

    private final SearchMethods.UpdateSearchElementStateCallback callback = new SearchMethods.UpdateSearchElementStateCallback() {
        @Override
        public void onSuccess(List<CombinedSearchElement> searchElements) {
            onAny(searchElements);
        }

        @Override
        public void onFailure(List<CombinedSearchElement> searchElements) {
            onAny(searchElements);
        }

        private void onAny(List<CombinedSearchElement> searchElements) {
            proceedWithReload(searchElements);
        }
    };

    public static SelectPmSymbolController createControllerForOrderEntry(Set<ShellMMType> pmTypesAvailableForOrdering) {
        return new SelectPmSymbolController(SearchMode.INSTRUMENT, pmTypesAvailableForOrdering)
                .withCheckAvailabilityForOrderEntry()
                .withTypesAvailableForOrderEntry(pmTypesAvailableForOrdering);
    }

    public SelectPmSymbolController() {
        this(SearchMode.INSTRUMENT);
    }

    public SelectPmSymbolController(SearchMode mode) {
        this(mode, null);
    }

    public SelectPmSymbolController(SearchMode mode, Set<ShellMMType> filterTypes) {
        this.block = new DmxmlContext().addBlock("AS_ShellSearch"); // $NON-NLS$
        this.searchMode = mode;

        if (filterTypes != null) {
            this.filterTypes.addAll(filterTypes);
        }

        final ViewSpec[] viewSpec = new ViewSpec[1];
        viewSpec[0] = new ViewSpec(ShellMMTypeInstrumentUtil.TYPE_ALL, ShellMMTypeInstrumentUtil.getLabel(ShellMMTypeInstrumentUtil.TYPE_ALL), null, null);

        this.selectedView = 0;
        this.indexedViewSelectionModel = new IndexedViewSelectionModelImpl(this, viewSpec, this.selectedView, DEFAULT_VIEW_GROUP);
        this.pagingFeature = new PagingFeature(this, this.block, DEFAULT_COUNT);

        this.tableDataModel = null;
    }

    public SelectPmSymbolController withCheckAvailabilityForOrderEntry() {
        this.checkAvailabilityForOrderEntry = true;
        return this;
    }

    public SelectPmSymbolController withTypesAvailableForOrderEntry(Set<ShellMMType> typesAllowedForOrderEntry) {
        this.typesAllowedForOrderEntry = typesAllowedForOrderEntry;
        return this;
    }

    @Override
    public void search(String searchString) {
        switch (this.searchMode) {
            case COMPLETE:
                this.block.setParameter("searchType", SearchType.SEARCH_ALLES.toString()); // $NON-NLS$
                break;
            case INSTRUMENT:
                this.block.setParameter("searchType", SearchType.SEARCH_WP.toString()); // $NON-NLS$
                this.block.setParameter("searchTypeWP", SearchTypeWP.STWP_SECURITY.toString()); // $NON-NLS$
                break;
            case FOLDER:
                this.block.setParameter("searchType", SearchType.SEARCH_ORDNER.toString()); // $NON-NLS$
                break;
            case DEPOT_OBJECT:
                this.block.setParameter("searchType", SearchType.SEARCH_DEPOT.toString()); // $NON-NLS$
                break;
            default:
                throw new IllegalStateException("unknown searchMode " + this.searchMode); // $NON-NLS$
        }
        this.block.setParameter("searchString", searchString); // $NON-NLS$
        if (!this.filterTypes.isEmpty()) {
            this.block.setParameters("shellMMTypes", toArray(new ArrayList<>(this.filterTypes))); // $NON-NLS$
        }
        this.indexedViewSelectionModel.selectView(0);
        resetPaging();
        this.updateViewSelection = true;
        this.block.issueRequest(this);
    }

    private void resetPaging() {
        this.pagingFeature.resetPaging();
        this.pagingHandle = null;
        this.block.removeParameter("pagingHandle"); // $NON-NLS$
    }

    private String[] toArray(List<ShellMMType> filterTypes) {
        final String[] result = new String[filterTypes.size()];
        for (int i = 0; i < filterTypes.size(); i++) {
            result[i] = filterTypes.get(i).toString();
        }
        return result;
    }

    @Override
    public void onFailure(Throwable caught) {
        this.pagingFeature.onResult();
        Firebug.error("<SelectPmSymbolController.onFailure>", caught);
        AbstractMainController.INSTANCE.showError(caught.getMessage());
    }

    @Override
    public void onSuccess(ResponseType result) {
        this.pagingFeature.onResult();
        if (!this.block.isResponseOk()) {
            Firebug.error("<SelectPmSymbolController.onSuccess> response not ok");
            return;
        }
        final ShellSearchResult ssr = this.block.getResult();
        updateViewTypes();
        if (this.updateViewSelection) {
            updateViewSelectionModel(ssr);
        }
        createModelAndUpdateView(ssr.getObjects());
        setPagingHandle(ssr);
    }

    private void setPagingHandle(ShellSearchResult ssr) {
        if (this.pagingHandle == null) {
            this.pagingHandle = ssr.getPagingHandle();
        }
    }

    private Map<String, String> toCountMap(ShellSearchResult ssr) {
        final HashMap<String, String> result = new HashMap<>();
        final List<SearchTypeCount> typeCounts = ssr.getTypeCounts();
        if (typeCounts == null) {
            return result;
        }
        int counter = 0;
        for (SearchTypeCount typeCount : typeCounts) {
            result.put(typeCount.getType(), typeCount.getValue());
            counter += Integer.valueOf(typeCount.getValue());
        }
        result.put(ShellMMTypeInstrumentUtil.TYPE_ALL, ssr.getTotal());
        result.put(ShellMMTypeInstrumentUtil.TYPE_OTHER, String.valueOf(Integer.valueOf(ssr.getTotal()) - counter));
        for (Map.Entry<String, String> entry : result.entrySet()) {
            Firebug.info(entry.getKey() + " : " + entry.getValue());
        }
        return result;
    }

    private static final NaturalComparator<String> NC = NaturalComparator.createDefault();
    class Type implements Comparable<Type> {
        private final String key;
        private final String name;

        public Type(String key, String name) {
            this.key = key;
            this.name = name;
        }

        @Override
        public int compareTo(Type o) {
            return NC.compareIgnoreCase(this.name, o.name);
        }
    }

    private String[] getTypeKeys(List<SearchTypeCount> typeCounts) {
        if (this.types.length > 0 || typeCounts == null || typeCounts.isEmpty()) {
            Firebug.debug("<SelectPmSymbolController.getTypeKeys> use configured viewTypes");
            return this.viewTypes.toArray(new String[this.viewTypes.size()]);
        }

        Firebug.debug("<SelectPmSymbolController.getTypeKeys> no viewTypes configured -> use types from search result");
        final List<Type> list = new ArrayList<>(typeCounts.size());
        for (SearchTypeCount typeCount : typeCounts) {
            if (!"0".equals(typeCount.getValue())) { // $NON-NLS$
                final String typeKey = typeCount.getType();
                list.add(new Type(typeKey, ShellMMTypeInstrumentUtil.getLabel(typeKey)));
            }
        }
        Collections.sort(list);
        final String[] typeKeys = new String[list.size() + (this.withMsc ? 2 : 1)];
        int i = 0;
        typeKeys[i] = ShellMMTypeInstrumentUtil.TYPE_ALL;
        for (Type type : list) {
            typeKeys[++i] = type.key;
        }
        if (this.withMsc) {
            typeKeys[++i] = ShellMMTypeInstrumentUtil.TYPE_OTHER;
        }
        return typeKeys;
    }

    private void updateViewSelectionModel(ShellSearchResult ssr) {
        Firebug.debug("<SelectPmSymbolController.updateViewSelectionModel>");
        final Map<String, String> counts = toCountMap(ssr);
        final String[] typeKey = getTypeKeys(ssr.getTypeCounts());

        final ViewSpec[] viewSpecs = new ViewSpec[typeKey.length];
        for (int i = 0; i < viewSpecs.length; i++) {
            final String viewType = typeKey[i];
            final String count = counts.get(viewType);
            final String sizeSuffix = count == null ? " (0)" : (" (" + count + ")"); // $NON-NLS$
            final String viewSpecName = ShellMMTypeInstrumentUtil.getLabel(viewType) + sizeSuffix;
            viewSpecs[i] = new ViewSpec(viewType, viewSpecName, null, null);
            Firebug.debug("<SelectPmSymbolController.updateViewSelectionModel> adding view type spec: " + viewType + " - " + viewSpecName);
        }

        this.indexedViewSelectionModel.update(viewSpecs, this.selectedView, DEFAULT_VIEW_GROUP);

        for (int i = 0; i < viewSpecs.length; i++) {
            if (viewSpecs[i].getName().endsWith("(0)")) { // $NON-NLS$
                this.indexedViewSelectionModel.setSelectable(i, false);
            }
        }
        this.updateViewSelection = false;
    }

    private void createTableModel(List<CombinedSearchElement> result) {
        Firebug.debug("<SelectPmSymbolController.createTableModel>");
        this.tableDataModel = DefaultTableDataModel.create(result,
                new AbstractRowMapper<CombinedSearchElement>() {
                    public Object[] mapRow(CombinedSearchElement element) {
                        final QuoteWithInstrument qwi = toQwI(element.getShellMMInfo());
                        return new Object[]{
                                AVAILABLE.equals(element.getShellMmInfoState()),
                                qwi,
                                qwi.getInstrumentData().getIsin(),
                                qwi.getInstrumentData().getWkn(),
                                "",
                                "",
                                element.getShellMMInfo().getTyp(),
                                element.getShellMmInfoState()
                        };
                    }
                });
    }

    private void updateView() {
        Firebug.debug("<SelectPmSymbolController.updateView>");
        if (this.tableDataModel != null) {
            this.view.updateViewNames();
            this.view.show(this.tableDataModel);
        }
    }

    private QuoteWithInstrument toQwI(ShellMMInfo info) {
        final InstrumentData instrumentData = new InstrumentData();
        instrumentData.setName(info.getBezeichnung());
        instrumentData.setIsin(info.getISIN());
        instrumentData.setType(info.getTyp() != null ? info.getTyp().name() : null);
        instrumentData.setWkn(info.getNumber());

        return new QuoteWithInstrument(instrumentData, QuoteWithInstrument.NULL_QUOTE_DATA);
    }

    @Override
    public IndexedViewSelectionModel getIndexedViewSelectionModel() {
        return this.indexedViewSelectionModel;
    }

    @Override
    public void setView(SelectSymbolForm view) {
        this.view = view;
    }

    @Override
    public void setTypes(String[] types) {
        Firebug.debug("<SelectPmSymbolController.updateViewTypes> types = " + StringUtil.join(',', types));

        this.types = types;
        updateViewTypes();
    }

    @Override
    public void setWithMsc(boolean withMsc) {
        Firebug.debug("<SelectPmSymbolController.setWithMsc> withMsc = " + withMsc);
        this.withMsc = withMsc;
        updateViewTypes();
    }

    public SelectPmSymbolController withMsc() {
        this.withMsc = true;
        updateViewTypes();
        return this;
    }

    private void updateViewTypes() {
        Firebug.debug("<SelectPmSymbolController.updateViewTypes> types=" + StringUtil.join(',', this.types) + " withMsc=" + this.withMsc);

        this.viewTypes.clear();
        this.viewTypes.add(ShellMMTypeInstrumentUtil.TYPE_ALL);
        Collections.addAll(this.viewTypes, this.types);

        if (this.withMsc) {
            this.viewTypes.add(ShellMMTypeInstrumentUtil.TYPE_OTHER);
        }
    }

    @Override
    public PagingFeature getPagingFeature() {
        return this.pagingFeature;
    }

    @Override
    public QuoteWithInstrument getResultQwi(int n) {
        if (this.block.isResponseOk()) {
            final ShellMMInfo element = this.block.getResult().getObjects().get(n);
            return toQwI(element);
        }
        throw new IllegalStateException("can't get result. block response not ok."); // $NON-NLS$
    }

    public ShellMMInfo getResult(int n) {
        if (this.block.isResponseOk()) {
            return this.block.getResult().getObjects().get(n);
        }
        return null;
    }

    @Override
    public boolean hasData() {
        return this.block.isResponseOk() && this.block.getResult() != null;
    }

    @Override
    public void cancelPendingRequests() {
        MmwebServiceAsyncProxy.cancelPending();
    }

    @Override
    public void setFilterForUnderlyingsOfLeverageProducts(Boolean filterForUnderlyingsOfLeveragProducts, boolean fireChange) {
        Firebug.debug("<SelectPmSymbolController.setFilterForUnderlyingsForType> " + //$NON-NLS$
                "filterForUnderlyingsOfLeveragProducts='" + filterForUnderlyingsOfLeveragProducts + "' fireChange='" + fireChange + "'"); //$NON-NLS$
    }

    @Override
    public void setFilterForUnderlyingsForType(String filterForUnderlyingsForType) {
        Firebug.debug("<SelectPmSymbolController.setFilterForUnderlyingsForType> " + //$NON-NLS$
                "filterForUnderlyingsForType='" + filterForUnderlyingsForType); //$NON-NLS$
    }

    @Override
    public void setFilterForUnderlyingsForType(String filterForUnderlyingsForType, boolean fireChange) {
        Firebug.debug("<SelectPmSymbolController.setFilterForUnderlyingsForType> " + //$NON-NLS$
                "filterForUnderlyingsForType='" + filterForUnderlyingsForType + "' fireChange='" + fireChange + "'");//$NON-NLS$
    }

    @Override
    public void reload() {
        this.view.disableSearchControls();
        if (this.pagingHandle != null) {
            this.block.setParameter("pagingHandle", this.pagingHandle); // $NON-NLS$
        }
        this.block.issueRequest(this);
    }

    private void proceedWithReload(List<CombinedSearchElement> searchElements) {
        createTableModel(searchElements);
        updateView();
    }

    private void createModelAndUpdateView(List<ShellMMInfo> infos) {
        if (infos == null) {
            proceedWithReload(Collections.<CombinedSearchElement>emptyList());
            return;
        }
        if (this.checkAvailabilityForOrderEntry) {
            issueUpdateSearchElementStateForOrderEntry(infos);
            return;
        }

        final List<CombinedSearchElement> elements = toCombinedSearchElements(infos, AVAILABLE);
        proceedWithReload(elements);
    }

    private void issueUpdateSearchElementStateForOrderEntry(List<ShellMMInfo> infos) {
        final List<CombinedSearchElement> searchElements = toCombinedSearchElements(infos, NOT_AVAILABLE);

        SearchMethods.INSTANCE.updateSearchElementState(searchElements, this.typesAllowedForOrderEntry, this.callback);
    }

    private static ArrayList<CombinedSearchElement> toCombinedSearchElements(
            List<ShellMMInfo> shellMMInfoList,
            CombinedSearchElement.State defaultState) {

        final ArrayList<CombinedSearchElement> combinedSearchElements = new ArrayList<>();

        for (ShellMMInfo element : shellMMInfoList) {
            final CombinedSearchElement combinedSearchElement = new CombinedSearchElement(element);
            combinedSearchElement.setShellMmInfoState(defaultState);
            combinedSearchElements.add(combinedSearchElement);
        }

        return combinedSearchElements;
    }

    @Override
    public void onViewChanged() {
        this.selectedView = this.indexedViewSelectionModel.getSelectedView();
        final ViewSpec viewSpec = this.indexedViewSelectionModel.getViewSpec(selectedView);
        final String type = viewSpec.getId();
        if (ShellMMTypeInstrumentUtil.TYPE_ALL.equals(type)) {
            resetShellMMTypes();
        }
        else {
            this.block.setParameters("shellMMTypes", new String[]{ShellMMType.valueOf(type).toString()}); // $NON-NLS$
        }
        resetPaging();
        reload();
    }

    private void resetShellMMTypes() {
        if (!this.filterTypes.isEmpty()) {
            this.block.setParameters("shellMMTypes", toArray(new ArrayList<>(this.filterTypes))); // $NON-NLS$
        }
        else {
            this.block.removeParameter("shellMMTypes"); // $NON-NLS$
        }
    }
}