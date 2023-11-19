/*
 * AbstractPortraitController.java
 *
 * Created on 17.03.2008 17:07:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.portrait;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Optional;

import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.IsWidget;

import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.MSCQuoteMetadata;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.DelegatingPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.HasObjectInfoSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.PageController;
import de.marketmaker.iview.mmgwt.mmweb.client.ResponseTypeCallback;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadManager;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FlashCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentHeaderProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasNavWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemToIndexedSelectionModelWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTree;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractPortraitController extends AbstractPageController implements
        HasNavWidget {
    protected static final String DEF_ARBITRAGE = "wp_arbitrage"; // $NON-NLS-0$

    protected static final String DEF_ARBITRAGE_NO_VOLUME = "wp_arbitrage_no_vol"; // $NON-NLS-0$

    protected static final String DEF_ORDERBOOK = "wp_orderbook"; // $NON-NLS-0$

    protected static final String DEF_RATIOS = "wp_ratios"; // $NON-NLS-0$

    protected static final String DEF_RATIOS_NO_VOLUME = "wp_ratios_no_vol"; // $NON-NLS-0$

    protected static final String DEF_NEWS = "wp_news"; // $NON-NLS-0$

    protected static final String DEF_NEWS_UNDERLYING = "wp_news_underlying"; // $NON-NLS-0$

    private static final String DEF_CHARTCENTER = "wp_chartcenter"; // $NON-NLS-0$

    private static final String DEF_CHARTCENTER_WITH_UNDERLYING = "wp_chartcenter_with_underlying"; // $NON-NLS-0$

    protected static final String DEF_ANALYSER_FLEXCHART = "wp_flexchart"; // $NON-NLS-0$

    protected static final String DEF_PCALC = "wp_pcalc"; // $NON-NLS-0$

    protected static final String DEF_REGULATORY = "wp_reporting"; // $NON-NLS$

    private final NavItemSpec navItemSpecRoot = new NavItemSpec("root", "root");  // $NON-NLS$

    private NavItemSpec current = null;

    private DmxmlContext contextMetadata = new DmxmlContext();

    private DmxmlContext.Block<MSCQuoteMetadata> blockMetadata;

    private DmxmlContext.Block<MSCPriceDatas> blockPriceDatas;

    private HistoryToken historyToken;

    private String symbol;

    private final ContentContainer innerContainer;

    private final NavItemSelectionModel navItemSelectionModel;

    private final ObjectPanel navWidget;

    private PlaceChangeEvent lastPlaceChangeEvent;

    private List<NavItemExtension> navItemExtensions = new ArrayList<>();

    private final NavItemsChangedCallback navItemsChangedCallback = root -> doOnNavItemsChanged();

    public AbstractPortraitController(ContentContainer contentContainer, String guidefKey) {
        super(contentContainer);

        this.blockMetadata = this.contextMetadata.addBlock("MSC_QuoteMetadata"); // $NON-NLS-0$
        this.blockMetadata.disableRefreshOnRequest();

        /* Request arbitrage data to render the market nav items, iff the root nav item
         * 'A' (aka Arbitrage), which is the root of market nav items, was found.
         * see ArbitrageNavItemExtension
         */
        this.blockPriceDatas = this.contextMetadata.addBlock("MSC_PriceDatas"); // $NON-NLS$
        this.blockPriceDatas.setParameter("disablePaging", "true"); // $NON-NLS$
        this.blockPriceDatas.disableRefreshOnRequest();
        this.blockPriceDatas.setEnabled(false);

        this.contextMetadata.setCancellable(false);

        final SelectionHandler<NavItemSpec> selectionHandler = event -> goTo(event.getSelectedItem());
        final ObjectTree objectTree;
        if (AbstractMainController.INSTANCE.getView().hasNavPanel()) {
            this.innerContainer = contentContainer;
            initNavItems();
            final ObjectTreeModel model = new ObjectTreeModel(this.navItemSpecRoot);
            this.navItemSelectionModel = model;
            final ObjectTree tree = objectTree = new ObjectTree(model);
            this.navWidget = new ObjectPanel(null, objectTree.asWidget());
            model.addModelChangeHandler(tree);
            model.addVisibilityUpdatedHandler(tree);
            tree.addSelectionHandler(selectionHandler);

            // display arbitrage/market lists within left menu
            // apply arbitrary block parameters to the priceDatas block
            applyGuidefsBlockParams(guidefKey, "priceDatas", this.blockPriceDatas); // $NON-NLS$
            addNavItemExtension(new ArbitrageNavItemExtension(this.blockPriceDatas));
        }
        else {
            final MultiContentView view = new MultiContentView(contentContainer);
            this.innerContainer = view;
            initNavItems();
            final NavItemToIndexedSelectionModelWrapper navItemSelectionModel = new NavItemToIndexedSelectionModelWrapper(this.navItemSpecRoot);
            view.init(navItemSelectionModel.getViewSelectionModel());
            navItemSelectionModel.addSelectionHandler(selectionHandler);
            this.navWidget = null;
            this.navItemSelectionModel = navItemSelectionModel;
        }
    }

    /**
     * Configures the specified block with arbitrary simple string parameters from guidefs.
     * <p>
     * Example guidefs code:
     * </p>
     * <pre>
     * &quot;portrait_stk&quot; : {
     *      &quot;priceDatas&quot; : { &quot;marketStrategy&quot; : &quot;market:DDF&quot; }
     * }
     * </pre>
     * @param portraitGuidefKey the key of the portrait in guidefs.
     * @param blockGuidefKey the key of the block in guidefs.
     * @param block the block.
     */
    protected final void applyGuidefsBlockParams(String portraitGuidefKey, String blockGuidefKey,
            DmxmlContext.Block<MSCPriceDatas> block) {
        final JSONWrapper portrait = SessionData.INSTANCE.getGuiDef(portraitGuidefKey);
        if (portrait.isObject()) {
            final JSONWrapper priceDatas = portrait.get(blockGuidefKey);
            if (priceDatas.isObject()) {
                final Set<String> keys = priceDatas.getValue().isObject().keySet();
                for (String key : keys) {
                    final String value = priceDatas.getString(key);
                    block.setParameter(key, value);
                }
            }
        }
    }

    public void setMetadataAwareEnabled(boolean enabled) {
        this.blockMetadata.setEnabled(enabled);
    }

    public ContentContainer getInnerContainer() {
        return this.innerContainer;
    }

    public HistoryToken getHistoryToken() {
        return historyToken;
    }

    public void requestNavWidget(final NavWidgetCallback callback) {
        doRequestNavWidget(callback);
    }

    private void doRequestNavWidget(final NavWidgetCallback callback) {
        if (!(this.current.getController() instanceof HasObjectInfoSnippet)) {
            throw new IllegalStateException("current controller must be an implementation of HasObjectInfoSnippet!"); // $NON-NLS$
        }
        if (this.navWidget == null) {
            Firebug.error("<" + getClass().getSimpleName() + "..AbstractPortraitController.doRequestNavWidget> this.navWidget is null!");
            return;
        }

        callback.showGlass();
        callback.setNavWidget(this.navWidget);
    }

    private void tryUpdateNavWidget() {
        if (this.navWidget == null) {
            return;
        }
        this.navWidget.updateNorthWidget(getObjectInfoWidget());
        this.navWidget.updateHistoryContext();
    }

    private Widget getObjectInfoWidget() {
        final Snippet snippet = ((HasObjectInfoSnippet) this.current.getController()).getObjectInfoSnippet();
        if (snippet != null) {
            final SnippetView snippetView = snippet.getView();
            if (snippetView instanceof IsWidget) {
                return ((IsWidget) snippetView).asWidget();
            }
        }
        return null;
    }

    public boolean providesContentHeader() {
        if (this.current.getController() instanceof DelegatingPageController) {
            final List<Snippet> snippets = ((DelegatingPageController) this.current.getController()).getSnippets();
            for (Snippet snippet : snippets) {
                if (snippet instanceof ContentHeaderProvider) {
                    return true;
                }
            }
        }
        return false;
    }

    public NavItemSelectionModel getNavItemSelectionModel() {
        return this.navItemSelectionModel;
    }

    protected void setSymbol(String symbol) {
        this.symbol = symbol;
        this.blockMetadata.setParameter("symbol", symbol); // $NON-NLS-0$
        this.blockPriceDatas.setParameter("symbol", symbol); // $NON-NLS$
        this.contextMetadata.issueRequest(new ResponseTypeCallback() {
            protected void onResult() {
                onMetadataResponse();
            }
        });
    }

    protected String getSymbol() {
        return this.symbol;
    }

    protected void onMetadataResponse() {
        if (this.blockMetadata.isResponseOk()) {
            final MSCQuoteMetadata metadata = this.blockMetadata.getResult();
            // update metadata of nav item extensions (before metadata aware because
            // metadata aware may change visibility of nav items, so an already updated
            // model may be needed there.)
            try {
                for (NavItemExtension nie : this.navItemExtensions) {
                    nie.onMetadataAvailable(metadata);
                }

                // update metadata in subclasses
                if (this instanceof MetadataAware) {
                    final MetadataAware metadataAware = (MetadataAware) this;
                    if (metadataAware.isMetadataNeeded()) {
                        metadataAware.onMetadataAvailable(metadata);
                    }
                }
            } finally {
                if (!isHandleCurrentControllerAndNavItemImmediately()) {
                    handleCurrentControllerAndNavItemSelection();
                }
            }

            // update metadata in snippets of current controller
            if (this.current != null) {
                final PageController controller = this.current.getController();
                if (controller instanceof MetadataAware && ((MetadataAware) controller).isMetadataNeeded()) {
                    ((MetadataAware) controller).onMetadataAvailable(metadata);
                }
            }
        }
        else {
            if (!isHandleCurrentControllerAndNavItemImmediately()) {
                handleCurrentControllerAndNavItemSelection();
            }
        }
    }

    protected boolean isHandleCurrentControllerAndNavItemImmediately() {
        return this.navItemExtensions.isEmpty();
    }

    public void destroy() {
        this.contextMetadata.removeBlock(this.blockMetadata);
        this.contextMetadata.removeBlock(this.blockPriceDatas);
    }

    protected abstract void initNavItems();

    protected NavItemSpec addChartcenter(boolean withUnderlying) {
        return addChartcenter(getNavItemSpecRoot(), withUnderlying);
    }

    protected NavItemSpec addChartcenter(NavItemSpec parent, boolean withUnderlying) {
        return addChartcenter(parent, withUnderlying ? DEF_CHARTCENTER_WITH_UNDERLYING : DEF_CHARTCENTER);
    }

    protected NavItemSpec addChartcenter(String def) {
        return addChartcenter(getNavItemSpecRoot(), def);
    }

    protected NavItemSpec addChartcenter(NavItemSpec parent, String def) {
        return addNavItemSpec(parent, "C", I18n.I.chart(), new PortraitChartcenterController(this.innerContainer, def));  // $NON-NLS-0$
    }

    protected void addChartAnalyser() {
        addChartAnalyser(getNavItemSpecRoot());
    }

    protected void addChartAnalyser(NavItemSpec parent) {
        addNavItemSpec(parent, "X", FlashCheck.isFlashAvailable() && Selector.FLEX_CHART.isAllowed(), "flexChart", newOverviewController(DEF_ANALYSER_FLEXCHART)); // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    public boolean isPrintable() {
        return getSelectedController().isPrintable();
    }

    public String getPrintHtml() {
        return getSelectedController().getPrintHtml();
    }

    protected PageController getSelectedController() {
        return this.navItemSelectionModel.getSelected().getController();
    }

    private NavItemSpec getFirstControllerNavItem() {
        return getFirstControllerNavItem(this.navItemSpecRoot);
    }

    private NavItemSpec getFirstControllerNavItem(NavItemSpec navItemSpec) {
        if (navItemSpec.getController() != null) {
            return navItemSpec;
        }
        if (navItemSpec.getChildren() == null) {
            return null;
        }
        for (NavItemSpec child : navItemSpec.getChildren()) {
            final NavItemSpec found = getFirstControllerNavItem(child);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    public NavItemSpec getNavItemSpecRoot() {
        return this.navItemSpecRoot;
    }

    protected Optional<NavItemSpec> addNavItemSpec(String id, boolean allowed, String name,
            AbstractPageController controller) {
        return addNavItemSpec(getNavItemSpecRoot(), id, allowed, name, controller);
    }

    protected Optional<NavItemSpec> addNavItemSpec(NavItemSpec parent, String id, boolean allowed,
            String name,
            AbstractPageController controller) {
        if (allowed) {
            return Optional.of(addNavItemSpec(parent, id, name, controller));
        }
        return Optional.empty();
    }

    protected NavItemSpec addNavItemSpec(String id, Selector s, String name,
            AbstractPageController controller) {
        return addNavItemSpec(getNavItemSpecRoot(), id, s, name, controller);
    }

    protected NavItemSpec addNavItemSpec(NavItemSpec parent, String id, Selector s, String name,
            AbstractPageController controller) {
        if (s.isAllowed()) {
            return addNavItemSpec(parent, id, name, controller);
        }
        return null;
    }

    protected NavItemSpec addNavItemSpec(String id, String name,
            AbstractPageController controller) {
        return addNavItemSpec(getNavItemSpecRoot(), id, name, controller);
    }

    protected NavItemSpec addNavItemSpec(NavItemSpec parent, String id, String name) {
        return addNavItemSpec(parent, id, name, null, null, true);
    }

    protected NavItemSpec addNavItemSpec(NavItemSpec parent, String id, String name,
            HistoryToken token, AbstractPageController controller, boolean withAlwaysOpen) {
        final NavItemSpec spec = new NavItemSpec(id, name, token, controller);
        if(withAlwaysOpen) {
            spec.withAlwaysOpen();
        }
        parent.addChild(spec);
        return spec;
    }

    protected NavItemSpec addNavItemSpec(NavItemSpec parent, String id, String name,
            AbstractPageController controller) {
        return addNavItemSpec(parent, id, name, controller, true);
    }

    protected NavItemSpec addNavItemSpec(NavItemSpec parent, String id, String name,
            AbstractPageController controller, boolean withAlwaysOpen) {
        final HistoryToken token = id == null ? null : HistoryToken.Builder.fromToken(id).build();
        return addNavItemSpec(parent, id, name, token, controller, withAlwaysOpen);
    }

    protected Optional<NavItemSpec> addNavItemSpecValidDef(NavItemSpec parent, String def,
            String id, String name, AbstractPageController controller) {
        final JSONWrapper guiDef = SessionData.INSTANCE.getGuiDef(def);
        if (guiDef.isValid()) {
            return Optional.of(addNavItemSpec(parent, id, name, controller));
        }
        return Optional.empty();
    }

    protected PortraitOverviewController newOverviewController(String def) {
        return new PortraitOverviewController(this.innerContainer, def);
    }

    protected PortraitTimesAndSalesController newTimesAndSalesController() {
        return new PortraitTimesAndSalesController(this.innerContainer);
    }

    protected PortraitTimesAndSalesController newLmeTimesAndSalesController() {
        return new LmePortraitTimesAndSalesController(this.innerContainer, new DmxmlContext());
    }

    @Override
    public void activate() {
        super.activate();
        if (this.current != null) {
            this.current.getController().activate();
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (this.current != null) {
            this.current.getController().deactivate();
        }
        if (navWidget != null) {
            this.navWidget.updateNorthWidget(null);
        }
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.lastPlaceChangeEvent = event;
        this.historyToken = event.getHistoryToken();
        // nav items may be manipulated by extensions, therefore if there is any extension,
        // handle current currentControllerAndNavItems after metadata is available.
        if (isHandleCurrentControllerAndNavItemImmediately() || this.current == null) {
            handleCurrentControllerAndNavItemSelection();
        }
        setSymbol(this.historyToken.get(1));
    }

    protected void doOnNavItemsChanged() {
        this.navItemSelectionModel.doUpdate();
        handleCurrentControllerAndNavItemSelection();
    }

    protected void handleCurrentControllerAndNavItemSelection() {
        final NavItemSpec spec = getNavItemSpec(this.historyToken);

        final boolean viewChanged = spec != this.navItemSelectionModel.getSelected();
        if (viewChanged) {
            this.navItemSelectionModel.setSelected(spec, true);
        }
        else {
            if (SessionData.isAsDesign()) {
                this.navItemSelectionModel.setSelected(spec, true, true);
            }
        }

        if (spec != this.current) {
            if (this.current != null) {
                this.current.getController().deactivate();
            }
            this.current = spec;

            final PageController controller = spec.getController();
            controller.activate();
        }

        final HistoryToken.Builder builder = this.lastPlaceChangeEvent.getHistoryToken().toBuilder();
        final HistoryToken dummyToken = builder.with(2, spec.getId()).build();
        final PlaceChangeEvent dummy = new PlaceChangeEvent(this.lastPlaceChangeEvent, dummyToken);

        this.current.getController().onPlaceChange(dummy);

        checkViewSpecVisibility(dummy.getHistoryToken().getControllerId());

        tryUpdateNavWidget();
    }

    private NavItemSpec getNavItemSpec(HistoryToken historyToken) {
        final NavItemSpec result;
        final String navItemId = historyToken.get(2, null);
        if (navItemId != null) {
            final String lastSelectedNavItemId = LastSelectedNavItemIdStore.INSTANCE.get();
            if (PlaceUtil.UNDEFINDED_PORTRAIT_VIEW.equals(navItemId) && lastSelectedNavItemId != null) {
                result = this.getNavItemSpecRoot().findChildById(lastSelectedNavItemId);
            }
            else {
                result = this.getNavItemSpecRoot().findChildById(navItemId);
            }
        }
        else {
            result = this.navItemSelectionModel.getSelected();
        }

        final NavItemSpec resultResult;
        if (result != null) {
            resultResult = result;
            LastSelectedNavItemIdStore.INSTANCE.set(resultResult.getId());
        }
        else {
            resultResult = getFirstControllerNavItem();
        }
        return resultResult;
    }

    private void checkViewSpecVisibility(String pageId) {
        checkViewSpecVisibility(getNavItemSpecRoot(), pageId);
    }

    private void checkViewSpecVisibility(NavItemSpec parent, String pageId) {
        if (parent.getChildren() == null) {
            return;
        }
        for (NavItemSpec navItemSpec : parent.getChildren()) {
            if (this.navItemSelectionModel.isVisible(navItemSpec)) {
                this.navItemSelectionModel.setVisibility(navItemSpec, Customer.INSTANCE.isJsonMenuElementNotFalse(pageId + "/" + navItemSpec.getId()));
            }
            checkViewSpecVisibility(navItemSpec, pageId);
        }
    }

    public void refresh() {
        this.navItemSelectionModel.getSelected().getController().refresh();
    }

    protected void goTo(NavItemSpec navItemSpec) {
        if (navItemSpec.getController() == null && navItemSpec.hasSelectionHandler()) {
            navItemSpec.goTo("");
            return;
        }

        assert (navItemSpec.getController() != null) : "goTo \"" + navItemSpec.getId() + "\" without controller should not occur";
        assert (this.historyToken.getIndexedParamCount() >= 2);

        final String controllerId = this.historyToken.getControllerId();
        final String symbol = this.historyToken.get(1);
        navItemSpec.getHistoryToken().withPrefix(controllerId, symbol).fire();
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        final PageController controller = getSelectedController();
        final PdfOptionSpec spec = controller.getPdfOptionSpec();
        if (spec == null) {
            final Map<String, String> map = new HashMap<>();
            map.put("symbol", this.symbol); // $NON-NLS-0$
            return getDefaultPdfOptionSpec(map);
        }
        else {
            return spec;
        }
    }

    protected abstract PdfOptionSpec getDefaultPdfOptionSpec(Map<String, String> map);

    @Override
    public void addPdfPageParameters(Map<String, String> mapParameters) {
        getSelectedController().addPdfPageParameters(mapParameters);
    }

    public void addNavItemExtension(NavItemExtension extension) {
        final String targetNavItemId = extension.getTargetNavItemId();
        final NavItemSpec targetNavItem = getNavItemSpecRoot().findChildById(targetNavItemId);
        if (targetNavItem != null) {
            this.navItemExtensions.add(extension);

            extension.init(getContentContainer(), targetNavItem, targetNavItem.getId(),
                    this.navItemsChangedCallback);
        }
    }

    protected void addPmInstrumentData(NavItemSpec typeNavItemsRoot, String def) {
        if (SessionData.isWithPmBackend()) {
            addNavItemSpec(typeNavItemsRoot, "PM", I18n.I.pmInstrumentData(), newOverviewController(def));  // $NON-NLS-0$
        }
    }

    protected void addPmReports(NavItemSpec parent) {
        if (SessionData.isWithPmBackend()) {
            addNavItemSpec(parent, "PMR", I18n.I.pmReports());  // $NON-NLS$
        }
    }

    public interface NavItemExtension extends MetadataAware {
        void init(ContentContainer contentContainer, NavItemSpec rootNavItem, String defaultToken,
                NavItemsChangedCallback callback);

        String getTargetNavItemId();
    }

    public interface NavItemsChangedCallback {
        void onNavItemsChanged(NavItemSpec root);
    }

    public static class LastSelectedNavItemIdStore {
        public static final LastSelectedNavItemIdStore INSTANCE = new LastSelectedNavItemIdStore();

        private final HashMap<Integer, String> lastSelectedNavItemIdForThread = new HashMap<>();

        public String get() {
            final HistoryThreadManager historyThreadManager = AbstractMainController.INSTANCE.getHistoryThreadManager();
            final int activeThreadId = historyThreadManager.getActiveThreadId();
            final String navItemId = this.lastSelectedNavItemIdForThread.get(activeThreadId);
            Firebug.debug("<LastSelectedNavItemIdStore.get> " + navItemId + " activeThreadId=" + activeThreadId);
            return navItemId;
        }

        public void set(String lastNavItemId) {
            final HistoryThreadManager historyThreadManager = AbstractMainController.INSTANCE.getHistoryThreadManager();
            final int activeThreadId = historyThreadManager.getActiveThreadId();
            Firebug.debug("<LastSelectedNavItemIdStore.set> " + lastNavItemId + " old=" + this.lastSelectedNavItemIdForThread.get(activeThreadId) + " activeThreadId=" + activeThreadId);
            this.lastSelectedNavItemIdForThread.put(activeThreadId, lastNavItemId);
        }
    }

    public static class ArbitrageNavItemExtension implements NavItemExtension {
        private NavItemsChangedCallback navItemsChangedCallback;

        private NavItemSpec arbitrageNavItem;

        private final DmxmlContext.Block<MSCPriceDatas> block;

        public ArbitrageNavItemExtension(DmxmlContext.Block<MSCPriceDatas> block) {
            this.block = block;
        }

        @Override
        public void init(ContentContainer contentContainer, NavItemSpec rootNavItem,
                String defaultToken, NavItemsChangedCallback callback) {
            this.arbitrageNavItem = rootNavItem;
            this.navItemsChangedCallback = callback;
            this.block.setEnabled(true);
        }

        @Override
        public String getTargetNavItemId() {
            return "A"; // $NON-NLS$
        }

        @Override
        public boolean isMetadataNeeded() {
            return true;
        }

        @Override
        public void onMetadataAvailable(MSCQuoteMetadata metadata) {
            if (this.block.isResponseOk()) {
                if (this.arbitrageNavItem.getChildren() != null) {
                    this.arbitrageNavItem.getChildren().clear();
                }
                addMarketNavItems(this.arbitrageNavItem, metadata.getQuotedata(), this.block.getResult());
                this.navItemsChangedCallback.onNavItemsChanged(this.arbitrageNavItem);
            }
        }

        protected NavItemSpec addMarketNavItems(NavItemSpec parent, QuoteData currentQuoteData,
                MSCPriceDatas priceDatas) {
            for (final MSCPriceDatasElement element : priceDatas.getElement()) {
                final QuoteData quoteData = element.getQuotedata();

                final NavItemSpec navItemSpec = new NavItemSpec("MARKET_" + quoteData.getMarketVwd(), // $NON-NLS$
                        TableCellRenderers.MarketLinkRenderer.getMarketName(quoteData),
                        value -> PlaceUtil.changeQuoteInView(quoteData))
                        .withIsTransient();
                if (StringUtil.equals(currentQuoteData.getQid(), quoteData.getQid())) {
                    navItemSpec.withLeftIcon("current-dot", SafeHtmlUtils.EMPTY_SAFE_HTML); // $NON-NLS$
                }
                parent.addChild(navItemSpec);
            }
            return parent;
        }
    }
}

