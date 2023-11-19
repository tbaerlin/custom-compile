package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ScrollPanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.SearchResultEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmWebModule;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Counter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ProvidesContentHeader;
import de.marketmaker.iview.pmxml.SearchType;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;
import de.marketmaker.iview.pmxml.ShellObjectSearchRequest;
import de.marketmaker.iview.pmxml.ShellObjectSearchResponse;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ulrich Maurer
 *         Date: 12.02.13
 */
public class PmSearchController extends AbstractPageController implements ProvidesContentHeader {

    private static final ShellMMType DEFAULT_TYPE = ShellMMType.ST_INHABER;
    private final DmxmlContext.Block<ShellObjectSearchResponse> searchBlock;
    private ShellMMType shellMmType;
    private final SearchType searchType;
    private final SearchResultEvent searchResultEvent;
    protected final NavItemSpec navItemSpecRoot = new NavItemSpec("root", "root");  // $NON-NLS$
    protected final NavItemSelectionModel navItemSelectionModel;
    private SnippetTableWidget view;
    private ScrollPanel scrollPanel;
    private final Map<ShellMMType, PmSearchCategory> categories = new HashMap<>();
    private static final Map<String, ZoneDesc> zoneDescs = new HashMap<>();
    private final PmSearchCategory defaultCategory;

    public static PmSearchController createPmInstrumentSearchController() {
        return new PmSearchController(ShellMMType.ST_WP, SearchType.SEARCH_WP,
                SearchResultEvent.createPmInstrumentSearchResultEvent());
    }

    public static PmSearchController createPmDepotSearchController() {
        return new PmSearchController(ShellMMType.ST_INHABER, SearchType.SEARCH_DEPOT,
                SearchResultEvent.createPmDepotSearchResultEvent());
    }

    private PmSearchController(ShellMMType shellMmType, SearchType searchType, SearchResultEvent searchResultEvent) {
        this.shellMmType = shellMmType;
        this.searchType = searchType;
        this.searchResultEvent = searchResultEvent;
        this.searchBlock = this.context.addBlock("PM_ShellSearch"); // $NON-NLS$
        this.defaultCategory = initCategories();
        initNavItemSpecs(searchType);
        this.navItemSelectionModel = new ObjectTreeModel(this.navItemSpecRoot);
        this.view = SnippetTableWidget.create(defaultCategory.getTableColumnModel(), "mm-snippetTable mm-WidthAuto"); // $NON-NLS-0$
        this.view.updateData(DefaultTableDataModel.NULL);
        this.scrollPanel = new ScrollPanel(this.view);
        this.scrollPanel.addStyleName("mm-contentData"); // $NON-NLS-0$
    }

    private PmSearchCategory initCategories() {
        this.categories.put(ShellMMType.ST_INHABER, new DepotObjectCategory(PmWebModule.HISTORY_TOKEN_INVESTOR_PROFILE));
        this.categories.put(ShellMMType.ST_PORTFOLIO, new DepotObjectCategory(PmWebModule.HISTORY_TOKEN_PORTFOLIO));
        this.categories.put(ShellMMType.ST_DEPOT, new DepotObjectCategory(PmWebModule.HISTORY_TOKEN_DEPOT));
        this.categories.put(ShellMMType.ST_KONTO, new DepotObjectCategory(PmWebModule.HISTORY_TOKEN_ACCOUNT));
        if(Selector.AS_ACTIVITIES.isAllowed()) {
            this.categories.put(ShellMMType.ST_INTERESSENT, new DepotObjectCategory(PmWebModule.HISTORY_TOKEN_PROSPECT));
        }
        return this.categories.get(DEFAULT_TYPE);
    }

    private void initNavItemSpecs(SearchType searchType) {
        if (SearchType.SEARCH_DEPOT == searchType) {
            initForDepotSearch();
        }
        else {
            initForWpSearch();
        }
    }

    private void initForDepotSearch() {
        final ArrayList<NavItemSpec> navItemSpecs = new ArrayList<>();
        navItemSpecs.add(createNavItemSpec(ShellMMType.ST_INHABER, I18n.I.pmInvestor()));
        navItemSpecs.add(createNavItemSpec(ShellMMType.ST_PORTFOLIO, I18n.I.pmPortfolio()));
        navItemSpecs.add(createNavItemSpec(ShellMMType.ST_DEPOT, I18n.I.pmDepot()));
        navItemSpecs.add(createNavItemSpec(ShellMMType.ST_KONTO, I18n.I.pmAccount()));
        if(Selector.AS_ACTIVITIES.isAllowed()) {
            navItemSpecs.add(createNavItemSpec(ShellMMType.ST_INTERESSENT, I18n.I.prospect()));
        }
        this.navItemSpecRoot.addChildren(navItemSpecs.toArray(new NavItemSpec[navItemSpecs.size()]));
    }

    private NavItemSpec createNavItemSpec(final ShellMMType shellMmType, String name) {
        return new NavItemSpec(shellMmType.value(), name, HistoryToken.buildKeyValue("t", shellMmType.value()), this) // $NON-NLS$
                .withGoToDelegate(new NavItemSpec.GoToDelegate() {
                    @Override
                    public void goTo(String value) {
                        Firebug.log("NavItemSpec " + shellMmType.value() + " doing 'goto'!");
                        PlaceUtil.goTo(PmWebModule.HISTORY_TOKEN_SEARCH_DEPOT + "/s=" + value + // $NON-NLS$
                                "/t=" + shellMmType.value()); // $NON-NLS$
                    }
                });
    }

    private void selectNavItem(ShellMMType shellMmType) {
        Firebug.log("PmSearchController <selectNavItem> " + shellMmType);
        final NavItemSpec item = this.navItemSpecRoot.findChildById(shellMmType.value());
        if (item != null) {
            Firebug.log("PmSearchController <selectNavItem> " + item.getName());
            this.navItemSelectionModel.setSelected(item, false);
        }
    }

    private void initForWpSearch() {
        this.navItemSpecRoot.addChildren(
                new NavItemSpec("all", "Alle", null, this) // $NON-NLS$
        );
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        final String s = historyToken.get("s"); // $NON-NLS$
        if (!StringUtil.hasText(s)) {
            onEmptySearch();
            return;
        }
        this.searchResultEvent.withSearchText(s);

        final String t = historyToken.get("t"); // $NON-NLS$
        if (StringUtil.hasText(t)) {
            this.shellMmType = ShellMMType.fromValue(t);
        }
        else {
            this.shellMmType = null;
        }

        configureBlock(this.searchBlock, s, this.shellMmType);

        if (this.shellMmType != null) {
            this.categories.get(this.shellMmType).issueRequest(this);
        }
        else {
            this.defaultCategory.issueRequest(this);
        }
    }

    private void onEmptySearch() {
        MainController.INSTANCE.getView().setContentHeader(I18n.I.searchResults());
        this.view.updateData(DefaultTableDataModel.NULL);
        getContentContainer().setContent(this.scrollPanel);
    }

    @Override
    public void activate() {
        getContentContainer().setContent(this.scrollPanel);
    }

    private void configureBlock(DmxmlContext.Block<ShellObjectSearchResponse> block, String searchString, ShellMMType shellMmType) {
        final ShellObjectSearchRequest req = new ShellObjectSearchRequest();
        req.setSearchType(this.searchType);
        req.setSearchString(searchString);
        if (shellMmType != null) {
            req.getShellMMTypes().add(shellMmType);
        }
        else {
            req.getShellMMTypes().addAll(this.categories.keySet());
        }
        this.searchResultEvent.withSearchText(searchString);
        block.setParameter(req);
    }

    @Override
    protected void onResult() {
        if (!this.searchBlock.isResponseOk()) {
            getContentContainer().setContent(new HTML(I18n.I.internalError()));
            Firebug.log("searchBlock.getError().getDescription(): " + this.searchBlock.getError().getDescription());
            return;
        }

        handleZones(this.searchBlock.getResult().getZones());

        final List<ShellMMInfo> infos;
        final List<ShellMMInfo> resultObjects = this.searchBlock.getResult().getObjects();
        if (this.shellMmType == null) { // means the resultset contains all valid types
            countTypes(resultObjects);
            this.shellMmType = chooseAvailableType(resultObjects, DEFAULT_TYPE);
            infos = filter(resultObjects, this.shellMmType);
        }
        else {
            infos = resultObjects;
            countType(infos, this.shellMmType);
        }
        selectNavItem(this.shellMmType);

        final PmSearchCategory category = this.categories.get(this.shellMmType);

        this.view = SnippetTableWidget.create(category.getTableColumnModel(), "mm-snippetTable mm-WidthAuto"); // $NON-NLS-0$
        final TableDataModel tdm = category.getTableDataModel(infos);
        this.view.updateData(tdm);
        this.scrollPanel.setWidget(this.view);

        getContentContainer().setContent(this.scrollPanel);

        EventBusRegistry.get().fireEvent(this.searchResultEvent.withNavItemSelectionModel(this.navItemSelectionModel));
    }

    private ShellMMType chooseAvailableType(List<ShellMMInfo> objects, ShellMMType defaultValue) {
        boolean hasPortfolio = false;
        boolean hasDepot = false;
        boolean hasAccount = false;
        for (ShellMMInfo object : objects) {
            switch (object.getTyp()) {
                case ST_INHABER:
                    return ShellMMType.ST_INHABER;
                case ST_PORTFOLIO:
                    hasPortfolio = true;
                    break;
                case ST_DEPOT:
                    hasDepot = true;
                    break;
                case ST_KONTO:
                    hasAccount = true;
                    break;
                case ST_INTERESSENT:
                    if(Selector.AS_ACTIVITIES.isAllowed()) {
                        return ShellMMType.ST_INTERESSENT;
                    }
            }
        }
        return hasPortfolio ? ShellMMType.ST_PORTFOLIO :
                hasDepot ? ShellMMType.ST_DEPOT :
                        hasAccount ? ShellMMType.ST_KONTO :
                                defaultValue;
    }

    private List<ShellMMInfo> filter(List<ShellMMInfo> objects, ShellMMType shellMmType) {
        final ArrayList<ShellMMInfo> result = new ArrayList<>();
        for (ShellMMInfo object : objects) {
            if (object.getTyp() == shellMmType) {
                result.add(object);
            }
        }
        return result;
    }

    private void handleZones(List<ZoneDesc> zones) {
        for (ZoneDesc zone : zones) {
            if (!zoneDescs.containsKey(zone.getId())) {
                zoneDescs.put(zone.getId(), zone);
            }
        }
    }

    private void countType(List<ShellMMInfo> objects, ShellMMType type) {
        final List<NavItemSpec> children = this.navItemSpecRoot.getChildren();
        for (NavItemSpec child : children) {
            final ShellMMType shellMMType = ShellMMType.fromValue(child.getId());
            if (shellMMType == type) {
                amendNavItem(child, objects.size());
                this.navItemSelectionModel.doUpdate();
                return;
            }
        }
    }

    private void countTypes(List<ShellMMInfo> objects) {
        final LinkedHashMap<ShellMMType, Counter> typeMap = new LinkedHashMap<>();
        final Set<ShellMMType> shellMMTypes = this.categories.keySet();
        for (ShellMMType shellMMType : shellMMTypes) {
            typeMap.put(shellMMType, new Counter());
        }
        for (ShellMMInfo object : objects) {
            typeMap.get(object.getTyp()).inc();
        }
        final List<NavItemSpec> children = this.navItemSpecRoot.getChildren();
        for (NavItemSpec child : children) {
            final ShellMMType shellMMType = ShellMMType.fromValue(child.getId());
            amendNavItem(child, typeMap.get(shellMMType).getValue());
        }
        this.navItemSelectionModel.doUpdate();
    }

    private void amendNavItem(NavItemSpec child, int size) {
        child.setNameSuffix("(" + size + ")");
        child.setEnabled(size > 0);
    }

    public static ZoneDesc getZoneDesc(String zoneId) {
        return zoneDescs.get(zoneId);
    }

    @Override
    public SafeHtml getContentHeader() {
        return SafeHtmlUtils.fromString(I18n.I.searchResults());
    }
}