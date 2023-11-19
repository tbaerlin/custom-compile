/*
 * AbstractFinder.java
 *
 * Created on 17.06.2008 13:00:54
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.extjs.gxt.ui.client.dnd.DropTarget;
import com.extjs.gxt.ui.client.event.DNDEvent;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.dmxml.InstrumentData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MultiViewPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.EmptyContext;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractFinder<V extends BlockListType, M extends BlockType> extends
        MultiViewPageController implements FinderController, PageLoader {

    public static final String SORT_SECTION_ID = "sort-section"; // $NON-NLS$

    public static final int DEFAULT_PAGE_SIZE = 20;

    public static final Integer[] POSSIBLE_PAGE_SIZES = {20, 40, 60, 80, 100};

    private final int settingsViewCount;

    protected FinderFormElements.OrderByOption orderByOption;

    protected List<FinderFormElements.Item> sortFields = new ArrayList<>();

    protected String defaultSortField = null;

    protected boolean defaultSortDescending = false;

    protected final DmxmlContext.Block<V> block;

    protected DmxmlContext.Block<M> metaBlock;

    protected final ArrayList<String> sectionIds = new ArrayList<>();

    protected AbstractFinderForm ff;

    protected FinderFormConfig pending;

    protected final PagingFeature pagingFeature;

    protected AbstractFinderView view;

    protected int lastResultView = 0;

    /**
     * block actually used for a query, might be different from this.block if subclasses override
     * {@link #getBlockForQuery()} and return another one.
     */
    protected DmxmlContext.Block<? extends BlockListType> queryBlock = null;

    protected static final String[] UNDERLYING_TYPES
            = new String[]{"STK", "IND", "UND", "MER"}; // $NON-NLS$

    protected AbstractFinder(String blockName) {
        this(blockName, SessionData.INSTANCE.getUser().getAppConfig()
                .getIntProperty(AppConfig.PROP_KEY_FINDER_PAGE_SIZE, DEFAULT_PAGE_SIZE));
    }

    protected AbstractFinder(String blockName, int pageSize) {
        this.block = this.context.addBlock(blockName);
        this.block.disable();
        this.metaBlock = this.context.addBlock(blockName + "Metadata"); // $NON-NLS-0$
        this.pagingFeature = new PagingFeature(this, this.block, pageSize);

        final ViewSpec[] settings = getSettingsViewSpec();
        this.settingsViewCount = settings.length;

        final ViewSpec[] result = getResultViewSpec();

        initViewSelectionModel(merge(settings, result), 0, getViewGroup());
        setResultViewsSelectable(false);

        this.defaultSortField = "name"; // $NON-NLS$

        //scheduler is necessary, because getId() might depend on data set in the sub-constructor
        //TODO: any idea how to do that better?
        Scheduler.get().scheduleDeferred(() -> FinderControllerRegistry.put(getId(), AbstractFinder.this));
    }

    protected void setResultViewsSelectable(final boolean selectable) {
        for (int i = getSettingsViewCount(); i < getViewSelectionModel().getViewCount(); i++) {
            setViewSelectable(i, selectable);
        }
    }

    private static ViewSpec[] merge(ViewSpec[] specs, ViewSpec[] tmp) {
        final ViewSpec[] result = new ViewSpec[specs.length + tmp.length];
        System.arraycopy(specs, 0, result, 0, specs.length);
        System.arraycopy(tmp, 0, result, specs.length, tmp.length);
        return result;
    }

    static QuoteWithInstrument createQuoteWithInstrument(InstrumentData i, QuoteData q) {
        return new QuoteWithInstrument(i, q).withHistoryContext(EmptyContext.create(I18n.I.searchResults()));
    }

    protected ViewSpec[] getSettingsViewSpec() {
        return new ViewSpec[]{
                new ViewSpec(I18n.I.settings()),
                new ViewSpec(I18n.I.search(), "mm-icon-finder"),  // $NON-NLS$
                new ViewSpec(I18n.I.reset())
        };
    }

    protected final int getSettingsViewCount() {
        return this.settingsViewCount;
    }

    protected abstract ViewSpec[] getResultViewSpec();

    public PagingFeature getPagingFeature() {
        return pagingFeature;
    }

    public V getResult() {
        return this.block.getResult();
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        if (historyToken.getAllParamCount() > 1) {
            this.pending = createFormConfig(historyToken);
        }

        if (this.ff == null) {
            doRefresh(forceRefreshOnPlaceChange());
        }
        else {
            handlePending();
        }
    }

    protected boolean forceRefreshOnPlaceChange() {
        return true;
    }


    protected FinderFormConfig createFormConfig(HistoryToken historyToken) {
        return null;
    }

    public void reload() {
        doRefresh(true);
    }

    public void refresh() {
        // empty, does not make sense for finders, if they still want it:  doRefresh(false)
    }

    protected void doRefresh(boolean force) {
        if (force || (this.ff != null && !this.ff.formShowing)) {
            super.refresh();
        }
    }

    public void search() {
        beforeSearch();

        this.queryBlock = getBlockForQuery();
        this.block.setEnabled(this.queryBlock == this.block);

        final String query = getQuery();
        if (isInvalidQuery(query)) {
            Firebug.warn(getClass().getName() + "<search> invalid query string '" + query + "'");
        }
        else {
            this.queryBlock.setParameter("query", query); // $NON-NLS$
        }
        this.queryBlock.setParameter("offset", "0"); // $NON-NLS$
        this.queryBlock.setParameter("count", getCount()); // $NON-NLS$

        if (this.orderByOption != null) {
            this.queryBlock.setParameter("sortBy", this.orderByOption.getOrderBy()); // $NON-NLS$
            this.queryBlock.setParameter("ascending",  // $NON-NLS$
                    Boolean.toString(!orderByOption.isDescending()));
        }
        doRefresh(true);
    }

    private String getCount() {
        // a non-custom query will not be rendered anyway (see updateResultView) and
        // ratios-frontend will be faster if no results are requested, so use count="0"
        // TODO: enable customizedQuery logic after istar-ratios-frontend update
        return
                // isCustomizedQuery(query) ?
                String.valueOf(this.pagingFeature.getPageSize())
                // : "1" // $NON-NLS$
                ;
    }

    protected boolean isInvalidQuery(String query) {
        return query == null;
    }

    protected void beforeSearch() {
        // subclasses can override this to perform actions before a query is created and executed
    }

    protected DmxmlContext.Block<? extends BlockListType> getBlockForQuery() {
        return this.block;
    }

    public void prepareFind(FinderFormConfig config) {
        this.pending = config;
    }

    public void onSearchLoaded() {
        setResultViewsSelectable(false);
        this.view.updateViewButtons();
    }

    protected void addOnRenderCommand(Command c) {
        this.ff.addOnRenderCommand(c);
    }

    protected FinderSection addSection(String id, String name, boolean alwaysEnabled) {
        return addSection(id, name, alwaysEnabled, null, null);
    }

    protected FinderSection addSection(String id, String name, boolean alwaysEnabled,
            DataLoader<List<FinderFormElement>> loader, SearchHandler searchHandler) {
        this.sectionIds.add(id);
        return this.ff.addSection(id, name, alwaysEnabled, loader, searchHandler);
    }

    protected abstract void addSections();

    protected abstract TableDataModel createDataModel(int view);

    @SuppressWarnings("unused")
    protected FinderFormElements.FromToBoxOption createFromToBoxOption(String field,
            String label, List<FinderFormElements.Item> items) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return new FinderFormElements.FromToBoxOption(field, label, items);
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.FromToTextOption createFromToTextOption(String field,
            String label, String textFieldSuffix) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return new FinderFormElements.FromToTextOption(field, label, textFieldSuffix);
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.FromToTextOption createFromToTextOption(String field,
            String label) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return new FinderFormElements.FromToTextOption(field, label);
    }

    protected FinderFormElements.MultiListBoxOption createListBoxOption(String field,
            String label, List<FinderFormElements.Item> items, String defaultKey) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return new FinderFormElements.MultiListBoxOption(field, label, items, defaultKey);
    }

    protected FinderFormElements.MultiListBoxOption createSortableListBoxOption(String field,
            String label, List<FinderFormElements.Item> items, String defaultKey) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return createListBoxOption(field, label, items, defaultKey);
    }

    protected FinderFormElements.FromToTextOption createEdgFromToTextOption(
            String field, String label, String textFieldSuffix) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return FinderFormElements.createEdgFromToTextOption(this.sortFields, field, label, textFieldSuffix);
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.StartEndOption createStartEndOption(String id, String field,
            String label, String textFieldSuffix, List<FinderFormElements.Item> items,
            DateTimeUtil.PeriodMode mode) {
        this.sortFields.add(new FinderFormElements.Item(label, field));
        return new FinderFormElements.StartEndOption(id, field, label, textFieldSuffix, items, mode);
    }

    protected <V2> DefaultTableDataModel createModel(List<V2> elements, RowMapper<V2> mapper) {
        if (elements.isEmpty()) {
            return DefaultTableDataModel.NULL;
        }
        return DefaultTableDataModel.create(elements, mapper).withSort(this.queryBlock.getResult().getSort());
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.MultiOption createMultiPeriodFromTo(String field, String label,
            List<FinderFormElements.Item> periods) {
        return createMultiPeriodFromTo(field, label, null, periods);
    }

    protected FinderFormElements.MultiOption createMultiPeriodFromTo(String field, String label,
            String textFieldSuffix,
            List<FinderFormElements.Item> periods) {
        FinderFormElements.PeriodFromToTextOption p1 = createPeriodFromToTextOption(field + "1", field, label, textFieldSuffix, periods); // $NON-NLS-0$
        FinderFormElements.PeriodFromToTextOption p2 = new FinderFormElements.PeriodFromToTextOption(field + "2", field, "", textFieldSuffix, periods, 1); // $NON-NLS-0$ $NON-NLS-1$
        return new FinderFormElements.MultiOption(field, label, p1, p2);
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.PeriodFromToTextOption createPeriodFromToTextOption(String field,
            String label,
            List<FinderFormElements.Item> items) {
        return createPeriodFromToTextOption(field, field, label, null, items);
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.PeriodFromToTextOption createPeriodFromToTextOption(String field,
            String label,
            String textFieldSuffix, List<FinderFormElements.Item> items) {
        return createPeriodFromToTextOption(field, field, label, textFieldSuffix, items);
    }

    protected FinderFormElements.PeriodFromToTextOption createPeriodFromToTextOption(String id,
            String field,
            String label,
            String textFieldSuffix,
            List<FinderFormElements.Item> items) {
        addToSortFields(field, label, items);
        return new FinderFormElements.PeriodFromToTextOption(id, field, label, textFieldSuffix, items);
    }

    protected void addToSortFields(String field, String label,
            List<FinderFormElements.Item> items) {
        for (FinderFormElements.Item item : items) {
            this.sortFields.add(new FinderFormElements.Item(handleLabelExceptions(label) + " " + item.item, field + item.value)); // $NON-NLS-0$
        }
    }

    protected String handleLabelExceptions(String label) {
        if (label.contains(I18n.I.averageVolume())) {
            return label.replace(I18n.I.averageVolume(), I18n.I.averageVolume2());
        }
        return label;
    }

    @SuppressWarnings("unused")
    protected FinderFormElements.ListFromToTextOption createListFromToTextOption(String field,
            String label, String textFieldSuffix, List<FinderFormElements.Item> items,
            boolean addSort) {
        if (addSort) {
            this.sortFields.addAll(items);
        }
        return new FinderFormElements.ListFromToTextOption(field, field, label, items, textFieldSuffix);
    }

    protected abstract AbstractFinderView createView();

    protected abstract Map<String, FinderMetaList> getMetaLists();

    @Override
    protected void onResult() {
        if (this.ff == null) {
            onMetaBlockResult();
            getViewSelectionModel().selectView(0);
            this.view.updateViewButtons();

            this.context.removeBlock(this.metaBlock);

            prepareBlock();
            handlePending();
            return;
        }
        if (this.queryBlock == null || !this.queryBlock.isResponseOk()) {
            return;
        }

        this.pagingFeature.setBlock(this.queryBlock);
        this.pagingFeature.onResult();

        if (lastResultView == 0) {
            this.lastResultView = getSettingsViewCount() + getDefaultViewOffset();
        }
        if (getViewSelectionModel().getSelectedView() == this.lastResultView) {
            updateResultView();
        }
        else {
            // this will trigger a call of selectedViewChanged
            getViewSelectionModel().selectView(this.lastResultView);
            setResultViewsSelectable(true);
            this.view.updateViewButtons();
        }

        if (this.pending != null) {
            this.pending = null;
            handlePending();
        }
    }

    protected int getDefaultViewOffset() {
        return 0;
    }

    protected void onMetaBlockResult() {
        initFinderForm();
    }

    private void handlePending() {
        if (this.pending != null) {
            onBeforeHandlePending();
            this.ff.apply(this.pending);
            if (FinderFormKeys.DISPLAY_SETTINGS.equals(this.pending.get(FinderFormKeys.VIEW))) {
                this.ff.showSettings();
                super.getContentContainer().setContent(this.ff);
            }
            else {
                search();
            }
        }
        else {
            super.getContentContainer().setContent(this.ff);
        }
    }

    protected void onBeforeHandlePending() {
    }

    protected String getQuery() {
        return this.ff.getQuery();
    }

    protected void prepareBlock() {
        this.block.setParameter("offset", "0"); // $NON-NLS$
        this.block.setParameter("count", this.pagingFeature.getPageSize()); // $NON-NLS$
    }

    public void onViewChanged() {
        final int selectedView = getViewSelectionModel().getSelectedView();
        if (selectedView < getSettingsViewCount()) {
            onSettingsViewChanged(selectedView);
        }
        else {
            this.lastResultView = selectedView;
            updateResultView();
        }
    }

    protected void onSettingsViewChanged(int selectedView) {
        if (selectedView == 0) {
            this.ff.showSettings();
        }
        else if (selectedView == 1) {
            search();
        }
        else if (selectedView == 2) {
            reset();
        }
    }

    protected void reset() {
        this.ff.reset();
        getViewSelectionModel().selectView(0);
        setResultViewsSelectable(false);
        this.view.updateViewButtons();
    }

    protected void updateResultView() {
        if (this.queryBlock != null && this.queryBlock.isResponseOk()
                && isValidQuery(this.queryBlock.getParameter("query"))) { // $NON-NLS$
            // view - SETTINGS_VIEW_COUNT to ignore Settings view
            this.view.show(createDataModel(this.lastResultView - getSettingsViewCount()),
                    this.ff.getExplanationWidget());
        }
        else {
            this.getPagingFeature().forceNullPage();
            this.view.show(DefaultTableDataModel.NULL, null);
        }
        this.ff.showResult();
    }

    private boolean isValidQuery(String query) {
        if (!StringUtil.hasText(query)) {
            return allowEmptyQuery();
        }
        final List<String> defaultQueries = this.ff.getDefaultQueries();
        for (String defaultQuery : defaultQueries) {
            query = query.replaceAll(defaultQuery, "");
        }
        return StringUtil.hasText(query);
    }

    protected boolean allowEmptyQuery() {
        return false;
    }

    void toggleSort(LinkContext<String> linkContext) {
        final String sortBy = linkContext.getData();
        final String current = this.queryBlock.getParameter("sortBy"); // $NON-NLS-0$
        if (current.equals(sortBy)) {
            final boolean ascending
                    = !Boolean.valueOf(this.queryBlock.getParameter("ascending")); // $NON-NLS$
            this.queryBlock.setParameter("ascending", Boolean.toString(ascending)); // $NON-NLS$
            this.orderByOption.setDescending(!ascending);
        }
        else {
            this.queryBlock.setParameter("sortBy", sortBy); // $NON-NLS-0$
            this.queryBlock.setParameter("ascending", "true"); // $NON-NLS-0$ $NON-NLS-1$
            this.orderByOption.setItem(sortBy);
            this.orderByOption.setDescending(false);
        }

        this.queryBlock.setParameter("offset", "0"); // $NON-NLS-0$ $NON-NLS-1$
        reload();
    }

    private void addSectionSort() {
        final TreeSet<FinderFormElements.Item> tsSortFields = new TreeSet<>(this.sortFields);
        this.sortFields = new ArrayList<>(tsSortFields);
        final FinderSection section = addSection(SORT_SECTION_ID, I18n.I.sort(), true);
        this.orderByOption = createSortElement();
        this.orderByOption.setDefaultSortField(this.defaultSortField);
        this.orderByOption.setDefaultSortDescending(this.defaultSortDescending);
        this.orderByOption.setDescending(this.defaultSortDescending);
        section.add(this.orderByOption);
    }

    protected FinderFormElements.OrderByOption createSortElement() {
        return new FinderFormElements.OrderByOption(FinderFormKeys.SORT, I18n.I.sortField(),
                this.sortFields, this.defaultSortField);
    }

    private void initFinderForm() {
        setUnselected();
        this.view = createView();
        this.ff = createFinderForm();

        addSections();

        if (!this.sortFields.isEmpty()) {
            // important to add as last as sortFields are filled while other sections are being added
            addSectionSort();
        }

        final Map<String, FinderMetaList> map = getMetaLists();
        this.ff.initialize(map);
        this.ff.addResultPanel(this.view);
    }

    protected void addMetaList(HashMap<String, FinderMetaList> map,
            List<FinderTypedMetaList> metaListList, String type) {
        for (FinderTypedMetaList list : metaListList) {
            if (type.equals(list.getType())) {
                map.put(type, list);
                return;
            }
        }
    }

    protected void addMetaLists(HashMap<String, FinderMetaList> map,
            List<FinderTypedMetaList> metaListList, String type) {
        int n = 0;
        for (FinderTypedMetaList list : metaListList) {
            if (type.equals(list.getType())) {
                n++;
                map.put(type + "-" + n, list); // $NON-NLS-0$
            }
        }
    }

    protected FinderFormElements.SymbolOption createUnderlyingOption() {
        final FinderFormElements.SymbolOption result =
                new FinderFormElements.SymbolOption("underlyingIid", I18n.I.underlyingInstrument(), UNDERLYING_TYPES, true);  // $NON-NLS-0$
        asInstrumentDropTarget(result);
        return result;
    }

    /**
     * as soon as the form is rendered, the widget representing the given section will act as
     * a drop target for instruments. On drop, the symbolOption's
     * {@link de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.SymbolOption#setParameters(de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument)}
     * will be invoked with the dropped instrument.
     * @param symbolOption .
     */
    protected void asInstrumentDropTarget(final FinderFormElements.SymbolOption symbolOption) {
        addOnRenderCommand(new Command() {
            public void execute() {
                final DropTarget dt = new DropTarget(symbolOption.getName()) {
                    @Override
                    protected void onDragDrop(DNDEvent dndEvent) {
                        super.onDragDrop(dndEvent);
                        symbolOption.setParameters((QuoteWithInstrument) dndEvent.getData());
                    }
                };
                dt.setGroup("ins"); // $NON-NLS-0$
                dt.setOverStyle("drag-ok"); // $NON-NLS-0$
            }
        });
    }

    protected void addSectionEdg() {
        final FinderSection section = addSection("EDG", I18n.I.edgRating(), false); // $NON-NLS$
        section.add(createEdgFromToTextOption("edgTopScore", I18n.I.optimalRiskClass(), I18n.I.stars()));  // $NON-NLS-0$
        section.add(createEdgFromToTextOption("edgScore1", I18n.I.riskClassN(1), I18n.I.stars()));  // $NON-NLS-0$
        section.add(createEdgFromToTextOption("edgScore2", I18n.I.riskClassN(2), I18n.I.stars()));  // $NON-NLS-0$
        section.add(createEdgFromToTextOption("edgScore3", I18n.I.riskClassN(3), I18n.I.stars()));  // $NON-NLS-0$
        section.add(createEdgFromToTextOption("edgScore4", I18n.I.riskClassN(4), I18n.I.stars()));  // $NON-NLS-0$
        section.add(createEdgFromToTextOption("edgScore5", I18n.I.riskClassN(5), I18n.I.stars()));  // $NON-NLS-0$
    }

    // influences some behaviours of this class. subclasses which are livefinders must override it and return true
    protected boolean isLiveFinder() {
        return false;
    }

    protected AbstractFinderForm createFinderForm() {
        return new FinderForm(this);
    }

    protected List<FinderFormElement> orderBySectionConf(List<FinderFormElement> from,
            String[] sectionConf) {
        final List<FinderFormElement> result = new ArrayList<>();
        if (sectionConf != null) {
            for (String s : sectionConf) {
                for (FinderFormElement e : from) {
                    if (s.equals(e.getId())) {
                        result.add(e);
                    }
                }
            }
            from.removeAll(result);
        }
        result.addAll(from);
        return result;
    }

    protected List<? extends FinderFormElement> handleClones(String[] sectionConf,
            List<FinderFormElement> elements) {
        final List<FinderFormElement> result = new ArrayList<>();
        if (sectionConf != null) {
            for (String id : sectionConf) {
                final String origId = FinderFormUtils.getOriginalId(id);
                if (!id.equals(origId) && findElement(elements, id) == null) {
                    final FinderFormElement origElement = findElement(elements, origId);
                    if (origElement != null && origElement instanceof CloneableFinderFormElement) {
                        result.add(((CloneableFinderFormElement) origElement).cloneElement(id));
                    }
                }
            }
        }
        return result;
    }

    private FinderFormElement findElement(List<FinderFormElement> elements, String id) {
        for (FinderFormElement element : elements) {
            if (element.getId().equals(id)) {
                return element;
            }
        }
        return null;
    }

    @Override
    public String getPrintHtml() {
        return this.ff.getContentPanelHtml();
    }

    @Override
    public void activate() {
        int pageSize = SessionData.INSTANCE.getUser().getAppConfig()
                .getIntProperty(AppConfig.PROP_KEY_FINDER_PAGE_SIZE, this.pagingFeature.getPageSize());
        if (this.pagingFeature.getPageSize() != pageSize) {
            this.pagingFeature.setPageSize(pageSize);
        }
    }
}