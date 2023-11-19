/*
 * LiveFinder.java
 *
 * Created on 09.11.11 14:33
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.user.client.ui.CheckBox;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.BlockType;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.dmxml.FinderTypedMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MmwebServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;

import static de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements.selectedValue;

/**
 * Common superclass for LiveFinders.
 * @author oflege
 */
public abstract class LiveFinder<V extends BlockListType, M extends BlockType>
        extends AbstractFinder<V, M> implements ConfigChangedHandler {

    public static final String DEFAULT_WIDTH = "width160"; // $NON-NLS-0$

    private static final String EDG_ID = "EDG"; // $NON-NLS-0$

    private FinderFormConfig config = null;

    private boolean forceMetadataUpdate = false;

    private boolean searchActive = true;

    private boolean withResult;

    private boolean queryBlockPending;

    protected List<MutableMetadata> forceSingleElementsMetadataUpdate = new ArrayList<>();

    protected final SearchHandler searchHandler = new SearchHandler() {
        public void onSearch() {
            Scheduler.get().scheduleDeferred(() -> search());
        }
    };

    protected class DynamicSearchHandler extends SearchHandler {
        private FinderFormElements.AbstractOption element;

        protected FinderFormElements.AbstractOption withElement(
                FinderFormElements.AbstractOption element) {
            this.element = element;
            return element;
        }

        @Override
        public void onSearch() {
            if (ff.deactivateFollowingElementsInSection(this.element) && this.element.getValue()
                    && this.element instanceof MutableMetadata) {
                forceSingleElementsMetadataUpdate.add((MutableMetadata) this.element);
                this.element.setActive(false);
            }
            search();
        }
    }

    /**
     * Intended for localized versions of metadata <em>key</em> fields,
     * where {@link FinderMetaList.Element#key} contains the localized name of the key
     */
    public static FinderMetaList swapKeyName(FinderMetaList metaList) {
        FinderMetaList result = new FinderMetaList();
        for (FinderMetaList.Element e : metaList.getElement()) {
            FinderMetaList.Element item = new FinderMetaList.Element();
            item.setName(e.getKey());
            item.setKey(e.getName());
            item.setCount(e.getCount());
            result.getElement().add(item);
        }
        return result;
    }

    protected Map<String, FinderMetaList> getLiveMetaLists() {
        final Map<String, FinderMetaList> metaLists = getMetaLists();
        if (!this.block.isResponseOk() || this.block.getResult() == null) {
            return metaLists;
        }
        final List<FinderTypedMetaList> liveMetadata = getLiveMetadata();
        for (FinderTypedMetaList m : liveMetadata) {
            if (metaLists.containsKey(m.getType())) {
                metaLists.put(m.getType(), m);
            }
        }
        return metaLists;
    }

    protected abstract List<FinderTypedMetaList> getLiveMetadata();

    protected LiveFinder(String blockName) {
        super(blockName);
        EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), this);
    }

    protected LiveFinder(String blockName, int pageSize) {
        super(blockName, pageSize);
        EventBusRegistry.get().addHandler(ConfigChangedEvent.getType(), this);
    }

    @Override
    protected ViewSpec[] getSettingsViewSpec() {
        return new ViewSpec[]{
                new ViewSpec(I18n.I.reset())
        };
    }

    @Override
    protected boolean isInvalidQuery(String query) {
        return false;
    }

    @Override
    protected AbstractFinderForm createFinderForm() {
        return new LiveFinderForm(this);
    }

    @Override
    protected void onSettingsViewChanged(int selectedView) {
        reset();
        search();
    }

    private void addToSortFields(String label, String field) {
        this.sortFields.add(new FinderFormElements.Item(handleLabelExceptions(label), field));
    }

    @Override
    protected FinderFormElements.OrderByOption createSortElement() {
        return new FinderFormElements.LiveOrderByOption(FinderFormKeys.SORT, I18n.I.sortField(),
                this.sortFields, this.defaultSortField, this.searchHandler);
    }

    public void onConfigChange(ConfigChangedEvent event) {
        if (isResponsibleFor(event)) {
            if (this.ff != null) {
                ((LiveFinderForm) this.ff).onConfigChange(true);
            }
        }
    }

    private boolean isResponsibleFor(ConfigChangedEvent event) {
        if (event.getProperty().startsWith(AppConfig.LIVE_FINDER_ELEMENT_PREFIX)
                && event.getProperty().substring(AppConfig.LIVE_FINDER_ELEMENT_PREFIX.length()).startsWith(getId())) {
            return true;
        }

        if (!event.getProperty().startsWith(AppConfig.LIVE_FINDER_SECTION_PREFIX)) {
            return false;
        }
        for (String sectionId : this.sectionIds) {
            if (event.getProperty().endsWith(sectionId)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected FinderFormElements.SymbolOption createUnderlyingOption() {
        return createUnderlyingOption(true, null, null);
    }

    protected FinderFormElements.SymbolOption createUnderlyingOption(boolean showMarketsPage,
            String filterForUnderlyingsForType,
            Boolean filterForUnderlyingsOfLeveragProducts) {
        final DynamicSearchHandler underlyingHandler = new DynamicSearchHandler();
        final FinderFormElements.SymbolOption result =
                new FinderFormElements.LiveSymbolOption(FinderFormKeys.UNDERLYING, I18n.I.underlyingInstrument(), UNDERLYING_TYPES,
                        showMarketsPage, filterForUnderlyingsForType,
                        filterForUnderlyingsOfLeveragProducts, underlyingHandler);
        underlyingHandler.withElement(result);
        asInstrumentDropTarget(result);
        return result;
    }

    protected void addSectionEdg() {
        final DataLoader<List<FinderFormElement>> elementsLoader = () -> {
            final List<FinderFormElement> elements = new ArrayList<>();
            final String[] sectionConf = SectionConfigUtil.getSectionConf(EDG_ID);

            elements.add(createEdgFromToTextOption("edgTopScore", I18n.I.optimalRiskClass(), I18n.I.stars()));  // $NON-NLS-0$
            elements.add(createEdgFromToTextOption("edgScore1", I18n.I.riskClassN(1), I18n.I.stars()));  // $NON-NLS-0$
            elements.add(createEdgFromToTextOption("edgScore2", I18n.I.riskClassN(2), I18n.I.stars()));  // $NON-NLS-0$
            elements.add(createEdgFromToTextOption("edgScore3", I18n.I.riskClassN(3), I18n.I.stars()));  // $NON-NLS-0$
            elements.add(createEdgFromToTextOption("edgScore4", I18n.I.riskClassN(4), I18n.I.stars()));  // $NON-NLS-0$
            elements.add(createEdgFromToTextOption("edgScore5", I18n.I.riskClassN(5), I18n.I.stars()));  // $NON-NLS-0$

            elements.addAll(handleClones(sectionConf, elements));
            return orderBySectionConf(elements, sectionConf);

        };
        addSection(EDG_ID, I18n.I.edgRating(), false, elementsLoader, this.searchHandler)
                .withConfigurable().loadElements();
    }

    protected FinderFormElements.FromToTextOption createEdgFromToTextOption(
            String field, String label, String textFieldSuffix) {
        addToSortFields(label, field);
        return FinderFormElements.createEdgFromToTextOption(this.sortFields, field, label, textFieldSuffix, this.searchHandler);
    }

    @Override
    protected boolean isLiveFinder() {
        return true;
    }


    @Override
    protected void onResult() {
        super.onResult();
        final Map<String, FinderMetaList> metaLists = getLiveMetaLists();
        ((LiveFinderForm) ff).updateMetadata(metaLists, this.forceMetadataUpdate);
        this.forceMetadataUpdate = false;
        this.queryBlockPending = false;
        this.withResult = true;
        this.ff.setControlsEnabled(true);
        if (!this.forceSingleElementsMetadataUpdate.isEmpty()) {
            for (MutableMetadata element : this.forceSingleElementsMetadataUpdate) {
                if (!element.isActive()) {
                    element.setActive(true);
                }
                element.updateMetadata(metaLists, true);
            }
            this.forceSingleElementsMetadataUpdate.clear();
            search();
        }
        if (getViewSelectionModel().getSelectedView() != this.lastResultView) {
            getViewSelectionModel().selectView(this.lastResultView, false);
        }
    }

    @Override
    public void onSearchLoaded() {
        this.forceMetadataUpdate = true;
        this.view.updateViewButtons();
        search();
    }

    @Override
    protected void onMetaBlockResult() {
        super.onMetaBlockResult();
        if (this.config != null) {
            final FinderFormConfig tmp = this.config;
            this.config = null;
            Scheduler.get().scheduleDeferred(() -> apply(tmp));
        }
    }

    @Override
    protected void reset() {
        if (this.withResult) {
            boolean tmp = searchActive;
            searchActive = false;
            LiveFinder.super.reset();
            searchActive = tmp;
        }
    }

    @Override
    public void prepareFind(FinderFormConfig config) {
        this.searchActive = false;
        if (this.ff == null) {
            this.config = config;
        }
        else {
            reset();
            apply(config);
        }
    }

    protected void apply(final FinderFormConfig config) {
        ((LiveFinderForm) this.ff).addMissingElements(config);
        Scheduler.get().scheduleDeferred(() -> {
            ff.apply(config);
            forceMetadataUpdate = true;
            searchActive = true;
            search();
        });
    }

    @Override
    public void search() {
        if (this.searchActive) {
            this.queryBlockPending = (this.queryBlock != null && this.queryBlock.isPending());
            super.search();
        }
    }

    @Override
    protected void doRefresh(boolean force) {
        if (!this.searchActive) {
            return;
        }

        if (this.queryBlockPending) {
            if (this.queryBlock.isPending()) {
                // parameters did not change, as block is not "to be requested"
                // so no need to request the same thing again
                return;
            }
            // parameters changed, ignore result when it arrives
            MmwebServiceAsyncProxy.cancelPending();
        }

        if (this.ff != null) {
            this.ff.setControlsEnabled(false);
        }
        super.doRefresh(force);
    }

    protected final void addCustomIssuerOnlyElement(List<FinderFormElement> elements) {
        if (!Selector.PRODUCT_HIGHLIGHTING.isAllowed() || !StringUtil.hasText(GuiDefsLoader.getIssuerName())) {
            return;
        }

        final boolean customIssuerEnabled = !"false".equals(SessionData.INSTANCE.getUserProperty(AppConfig.CUSTOM_ISSUER_ONLY));// $NON-NLS-0$

        final SearchHandler customSearchHandler = new SearchHandler() {
            @Override
            public void onSearch() {
                ff.deactivateElementById(FinderFormKeys.ISSUER_NAME);
                search();
            }
        };

        final String issuerName = GuiDefsLoader.getIssuerName();
        final String issuerNameDisplay = GuiDefsLoader.getIssuerDisplayName();
        final FinderFormElements.AbstractOption customIssuer = new FinderFormElements.LiveBooleanOption(FinderFormKeys.ONLY_CUSTOM_ISSUER,
                I18n.I.onlyIssuer(issuerNameDisplay), customSearchHandler) {

            @Override
            protected String doGetQuery() {
                return "issuername=='" + issuerName + "'"; // $NON-NLS$
            }

            @Override
            public void reset() {
                super.reset();
                setValue(customIssuerEnabled);
            }
        };
        elements.add(customIssuer);
        customIssuer.setValue(customIssuerEnabled);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        if (this.ff == null && this.config != null) {
            this.metaBlock.issueRequest(this);
        }
        else {
            super.onPlaceChange(event);
        }
    }

    static class MarketsLiveMultiEnumOption extends FinderFormElements.LiveMultiEnumOption {
        protected MarketsLiveMultiEnumOption(String liveFinderId, String elementId, String field,
                String label,
                List<FinderFormElements.Item> items, int checkBoxCount, String defaultKey,
                String[] defaultConfig, FinderMetaList metaList, String metaName,
                SearchHandler searchHandler) {
            super(liveFinderId, elementId, field, label, items, checkBoxCount, defaultKey, defaultConfig,
                    metaList, metaName, searchHandler);
        }

        static MarketsLiveMultiEnumOption createMarket(String liveFinderId,
                Map<String, FinderMetaList> metaLists) {
            final String marketgroups = FinderFormKeys.MARKET_GROUPS;
            String[] defaultConfig = getDefaultConfigFromGuiDefs(liveFinderId, marketgroups);
            return new MarketsLiveMultiEnumOption(liveFinderId, marketgroups, marketgroups, I18n.I.exchangePlace(),
                    getItems(liveFinderId, marketgroups, metaLists.get(marketgroups), defaultConfig),
                    getCheckBoxCount(liveFinderId, marketgroups, defaultConfig), null, defaultConfig, metaLists.get(marketgroups),
                    marketgroups, null);
        }

        @Override
        protected String doGetQuery() {
            final Set<String> set = new HashSet<>();
            for (CheckBox box : boxes) {
                if (box.getValue()) {
                    set.add(box.getName());
                }
            }
            if (this.lbCb.getValue()) {
                set.add(selectedValue(this.lb));
            }
            return "market==" + FinderFormElements.quoteMultiple(set); // $NON-NLS-0$
        }
    }

    protected void addMarkets(List<FinderFormElement> elements, String[] sectionConf) {
        addMarkets(elements, sectionConf, null);
    }

    protected void addMarkets(List<FinderFormElement> elements, String[] sectionConf,
            Integer defaultOrder) {
        final DynamicSearchHandler handler = new DynamicSearchHandler();
        final MarketsLiveMultiEnumOption markets = MarketsLiveMultiEnumOption.createMarket(getId(), getMetaLists());
        markets.setSearchHandler(handler);
        elements.add(handler.withElement(markets.withStyle(DEFAULT_WIDTH).withConf(sectionConf, defaultOrder)));
    }

    protected FinderFormElements.FromToBoxOption createLiveFromToBoxOption(String field,
            String label, List<FinderFormElements.Item> items,
            SearchHandler searchHandler) {
        addToSortFields(label, field);
        final FinderFormElements.LiveFromToBoxOption element = new FinderFormElements.LiveFromToBoxOption(field, field, label, items, searchHandler);
        if (searchHandler instanceof LiveFinder.DynamicSearchHandler) {
            ((LiveFinder.DynamicSearchHandler) searchHandler).withElement(element);
        }
        return element;
    }

    protected FinderFormElements.LiveListBoxOption createSortableLiveListBoxOption(String field,
            String label,
            List<FinderFormElements.Item> items,
            String defaultKey, SearchHandler searchHandler) {
        addToSortFields(label, field);
        return createLiveListBoxOption(field, label, items, defaultKey, searchHandler);
    }

    protected FinderFormElements.LiveListBoxOption createLiveListBoxOption(String field,
            String label,
            List<FinderFormElements.Item> items,
            String defaultKey, SearchHandler searchHandler) {
        return new FinderFormElements.LiveListBoxOption(field, field, label, items, defaultKey, searchHandler);
    }

    protected FinderFormElements.LiveMultiEnumOption createLiveMultiEnum(String field, String label,
            String defaultKey,
            Map<String, FinderMetaList> metaLists, String metaName,
            SearchHandler searchHandler) {
        return createLiveMultiEnum(field, field, label, defaultKey, metaLists.get(metaName), metaName, searchHandler, false);
    }

    protected FinderFormElements.LiveMultiEnumOption createSortableLiveMultiEnum(String field,
            String label, String defaultKey,
            List<FinderFormElements.Item> items,
            SearchHandler searchHandler) {
        addToSortFields(label, field);
        String[] defaultConfig = getDefaultConfigFromGuiDefs(getId(), field);
        return FinderFormElements.LiveMultiEnumOption
                .create(getId(), field, field, label, items, defaultKey, defaultConfig, searchHandler);

    }


    protected FinderFormElements.LiveMultiEnumOption createSortableLiveMultiEnum(String field,
            String label, String defaultKey,
            Map<String, FinderMetaList> metaLists, String metaName,
            SearchHandler searchHandler) {
        FinderMetaList metaList = metaLists.get(metaName);
        if (metaList == null) {
            Firebug.warn("LiveFinder <createSortableLiveMultiEnum> metaList ist null for metaName: " + metaName);
            metaList = new FinderMetaList();
        }
        return createLiveMultiEnum(field, field, label, defaultKey, metaList, metaName, searchHandler, true);
    }


    protected FinderFormElements.LiveMultiEnumOption createLiveMultiEnum(String id, String field,
            String label, String defaultKey,
            FinderMetaList metaList, String metaName,
            SearchHandler searchHandler, boolean sort) {
        if (sort) {
            addToSortFields(label, field);
        }
        String[] defaultConfig = getDefaultConfigFromGuiDefs(getId(), field);
        final FinderFormElements.LiveMultiEnumOption element = FinderFormElements.LiveMultiEnumOption
                .create(getId(), id, field, label, defaultKey, metaList, metaName, defaultConfig, searchHandler);
        if (searchHandler instanceof LiveFinder.DynamicSearchHandler) {
            ((LiveFinder.DynamicSearchHandler) searchHandler).withElement(element);
        }
        return element;
    }

    protected FinderFormElements.LiveSuggestEnumOption createSortableLiveSuggestEnum(String field,
            String label, String defaultKey, Map<String, FinderMetaList> metaLists, String metaName,
            SearchHandler searchHandler) {
        return createLiveSuggestEnum(field, field, label, defaultKey, metaLists.get(metaName), metaName, searchHandler, true);
    }


    protected FinderFormElements.LiveSuggestEnumOption createLiveSuggestEnum(String id,
            String field, String label, String defaultKey, FinderMetaList metaList, String metaName,
            SearchHandler searchHandler, boolean sort) {
        if (sort) {
            addToSortFields(label, field);
        }
        String[] defaultConfig = getDefaultConfigFromGuiDefs(getId(), field);
        final FinderFormElements.LiveSuggestEnumOption element = FinderFormElements.LiveSuggestEnumOption
                .create(getId(), id, field, label, defaultKey, metaList, metaName, defaultConfig, searchHandler);
        if (searchHandler instanceof LiveFinder.DynamicSearchHandler) {
            ((LiveFinder.DynamicSearchHandler) searchHandler).withElement(element);
        }
        return element;
    }


    /**
     * @param elementId: id of LiveFinderElement
     * @return a list of values
     * <p/>
     * Guidefs Example:
     * "liveFinderElement_defaults" : [
     * {"id":"LSTKcountry", "values":["Deutschland","Schweiz","Österreich"]},
     * {"id":"LSTKindex", "values":["846900.ETR"]}
     * ]
     */
    private static String[] getDefaultConfigFromGuiDefs(String finderId, String elementId) {
        String[] defaultConfig = null;
        final JSONWrapper guiDef = SessionData.INSTANCE.getGuiDef("liveFinderElement_defaults"); // $NON-NLS$
        if (!guiDef.isValid()) {
            return null;
        }
        final JSONArray array = guiDef.getValue().isArray();
        final String enumId = finderId + elementId;
        for (int i = 0; i < array.size(); i++) {
            final JSONObject object = array.get(i).isObject();
            final String id = object.get("id").isString().stringValue(); // $NON-NLS$
            if (enumId.equals(id)) {
                final JSONArray values = object.get("values").isArray(); // $NON-NLS$
                defaultConfig = new String[values.size()];
                for (int n = 0; n < values.size(); n++) {
                    defaultConfig[n] = values.get(n).isString().stringValue();
                }
            }
        }
        return defaultConfig;
    }

    protected FinderFormElements.LiveStartEndOption createLiveStartEndOption(String id,
            String field, String label, String textFieldSuffix, List<FinderFormElements.Item> items,
            DateTimeUtil.PeriodMode mode, SearchHandler searchHandler) {
        addToSortFields(label, field);
        return new FinderFormElements.LiveStartEndOption(id, field, label, textFieldSuffix, items, mode, searchHandler);
    }

    protected FinderFormElements.LivePeriodFromToTextOption createLivePeriodFromToTextOption(
            String id, String field, String label, String textFieldSuffix,
            List<FinderFormElements.Item> items, SearchHandler searchHandler) {
        addToSortFields(field, label, items);
        return new FinderFormElements.LivePeriodFromToTextOption(id, field, label, textFieldSuffix, items, searchHandler);
    }

    protected FinderFormElements.LivePeriodFromToTextOption createLivePeriodFromToTextOption(
            String id, String field, String label, List<FinderFormElements.Item> items,
            SearchHandler searchHandler) {
        return createLivePeriodFromToTextOption(id, field, label, "", items, searchHandler);
    }

    protected FinderFormElements.FromToTextOption createLiveFromToTextOption(String field,
            String label, SearchHandler searchHandler) {
        return createLiveFromToTextOption(field, field, label, searchHandler);
    }

    protected FinderFormElements.FromToTextOption createLiveFromToTextOption(String id,
            String field, String label, String textFieldSuffix, SearchHandler searchHandler) {
        addToSortFields(label, field);
        return new FinderFormElements.LiveFromToTextOption(id, field, label, textFieldSuffix, searchHandler);
    }

    protected FinderFormElements.FromToTextOption createLiveFromToTextOption(String id,
            String field, String label, SearchHandler searchHandler) {
        return createLiveFromToTextOption(id, field, label, "", searchHandler);
    }

    protected FinderFormElements.LiveBooleanOption createAverageVolume1wGtZero(
            SearchHandler searchHandler) {
        return new FinderFormElements.LiveBooleanOption(FinderFormKeys.AVG_VOL_1W, I18n.I.tradedInLast5Days(), searchHandler) {

            protected String doGetQuery() {
                return this.field + ">0"; // $NON-NLS-0$
            }
        };
    }

    protected FinderFormElements.LiveBooleanOption createTotalVolumeGtZero(
            SearchHandler searchHandler) {
        return new FinderFormElements.LiveBooleanOption(FinderFormKeys.TRADED_TODAY, I18n.I.tradedToday(), searchHandler) {

            protected String doGetQuery() {
                return this.field + ">0"; // $NON-NLS-0$
            }
        };
    }

    protected FinderFormElements.LiveFromToTextOption createLiveFromToInMio(String id, String field,
            String label, SearchHandler searchHandler) {
        return new FinderFormElements.LiveFromToTextOption(id, field, label, I18n.I.millionAbbr(), searchHandler) {
            @Override
            protected String toQueryValue(String input, boolean query) {
                if ("".equals(input)) {
                    return "";
                }
                try {
                    final Double aDouble = Double.valueOf(input.replace(",", "."));
                    return query ? String.valueOf(aDouble * 1000000) : (input);
                } catch (Exception e) {
                    return "";
                }
            }
        };
    }

    protected FinderFormElements.LiveFromToTextOption createLiveFromToInPercent(String id,
            String field, String label, SearchHandler searchHandler) {
        return new FinderFormElements.LiveFromToTextOption(id, field, label, "%", searchHandler) {
            @Override
            protected String toQueryValue(String input, boolean query) {
                if ("".equals(input)) {
                    return "";
                }
                try {
                    final Double aDouble = Double.valueOf(input.replace(",", "."));
                    return query ? Formatter.FORMAT_NUMBER23.format(aDouble / 100) : (input);
                } catch (Exception e) {
                    return "";
                }
            }
        };
    }
}
