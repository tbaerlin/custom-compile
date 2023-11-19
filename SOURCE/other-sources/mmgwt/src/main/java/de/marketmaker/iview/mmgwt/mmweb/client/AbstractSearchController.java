/*
 * AbstractSearchController.java
 *
 * Created on 07.05.2008 10:33:44
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.event.logical.shared.SelectionHandler;
import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PageLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.InstrumentTypeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModelImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemToIndexedSelectionModelWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ObjectTreeModel;

import java.util.HashMap;
import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.InstrumentTypeUtil.ALL_KEY;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.InstrumentTypeUtil.MSC_KEY;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractSearchController<V extends BlockListType> extends
        AbstractPageController implements PageLoader {
    public static final String[] DEFAULT_COUNT_TYPES = new String[]{
            "STK", "FND", "IND", "BND", "CER", "WNT" // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$ $NON-NLS-5$
    };

    private static final String KEY_FILTER_TYPE = "filterType"; // $NON-NLS-0$

    private static final String KEY_COUNT_TYPE = "countType"; // $NON-NLS-0$

    private static final String FILTER_FOR_UNDERLYINGS_FOR_TYPE = "filterForUnderlyingsForType"; // $NON-NLS$

    private static final String FILTER_FOR_UNDERLYINGS_OF_LEVERAGE_PRODUCTS = "filterForUnderlyingsOfLeverageProducts"; // $NON-NLS$

    protected static final String SEARCHSTRING = "searchstring"; // $NON-NLS-0$

    private static final String ADDITIONAL_SEARCHFIELD = "additionalSearchfield"; // $NON-NLS-0$

    private static final String KEY_FILTER_MSC = "filterMSC"; // $NON-NLS-0$

    private static final String[] VALOR = new String[]{"VALOR"}; // $NON-NLS-0$

    protected DefaultTableDataModel dtm;

    private String[] countTypes = DEFAULT_COUNT_TYPES;

    private String[] filterTypes = null;

    private String[] viewNames = null;

    private String[] viewTypes = null;

    private boolean withMsc = true;

    private boolean withAll = true;

    private final HashMap<String, String> typeCounts = new HashMap<>();

    private final PagingFeature pagingFeature;

    protected final DmxmlContext.Block<V> block;

    private String filterForUnderlyingsForType;

    private Boolean filterForUnderlyingsOfLeveragProducts;

    protected final NavItemSpec navItemSpecRoot = new NavItemSpec("root", "root");  // $NON-NLS$

    protected final NavItemSelectionModel navItemSelectionModel;

    private final IndexedViewSelectionModelImpl viewSelectionModel;

    public AbstractSearchController(ContentContainer contentContainer, String blockName, int pageSize) {
        super(contentContainer);
        init();

        if (needsIndexedViewSelectionModel()) {
            final NavItemToIndexedSelectionModelWrapper indexedSelectionModel =
                    new NavItemToIndexedSelectionModelWrapper(this.navItemSpecRoot, getViewGroup());
            this.viewSelectionModel = indexedSelectionModel.getViewSelectionModel();
            indexedSelectionModel.addSelectionHandler(getSelectionHandler());
            this.navItemSelectionModel = indexedSelectionModel;
        }
        else {
            this.navItemSelectionModel = new ObjectTreeModel(this.navItemSpecRoot);
            this.viewSelectionModel = null;
        }
        this.block = this.context.addBlock(blockName);
        this.pagingFeature = new PagingFeature(this, this.block, pageSize);
    }

    protected void init() {
        initNavItemSpecs();
    }

    protected boolean needsIndexedViewSelectionModel() {
        return !AbstractMainController.INSTANCE.getView().hasNavPanel();
    }

    protected abstract String getViewGroup();
    protected abstract SelectionHandler<NavItemSpec> getSelectionHandler();

    public void reload() {
        refresh();
    }

    public V getResult() {
        return this.block.getResult();
    }

    public boolean hasData() {
        return this.block.isResponseOk() && this.block.getResult() != null;
    }

    public PagingFeature getPagingFeature() {
        return pagingFeature;
    }

    protected abstract boolean doUpdateModel();

    protected abstract List<SearchTypeCount> getTypecount();

    protected void onResult() {
        this.pagingFeature.onResult();
        if (updateModel()) {
            updateView();
        }
    }

    protected void reset(String query) {
        this.block.removeAllParameters();
        this.block.setParameter(SEARCHSTRING, query);
        final String[] additionalSearchField = getAdditionalSearchField();
        if (additionalSearchField != null) {
            this.block.setParameters(ADDITIONAL_SEARCHFIELD, additionalSearchField);
        }
        this.block.setParameters(KEY_COUNT_TYPE, this.countTypes);
        this.block.setParameters(KEY_FILTER_TYPE, this.filterTypes);
        if (StringUtil.hasText(this.filterForUnderlyingsForType)) {
            this.block.setParameter(FILTER_FOR_UNDERLYINGS_FOR_TYPE, this.filterForUnderlyingsForType);
        }
        if (this.filterForUnderlyingsOfLeveragProducts != null) {
            this.block.setParameter(FILTER_FOR_UNDERLYINGS_OF_LEVERAGE_PRODUCTS, this.filterForUnderlyingsOfLeveragProducts);
        }
        this.pagingFeature.resetPaging();
        this.navItemSelectionModel.setSelected(this.navItemSpecRoot.getChildren().get(0), false);
    }

    protected String[] getAdditionalSearchField() {
        if (this.sessionData.getUser().getAppConfig().getBooleanProperty(AppConfig.SEARCH_BY_VALOR, false)) {
            return VALOR;
        }
        return null;
    }

    protected String getCurrentQuery() {
        return this.block.getParameter(SEARCHSTRING);
    }

    protected void initNavItemSpecs() {
        final int num = this.countTypes.length + (this.withAll ? 1 : 0) + (this.withMsc ? 1 : 0);
        this.viewNames = new String[num];
        this.viewTypes = new String[num];
        int n = 0;
        if (this.withAll) {
            this.viewTypes[n++] = ALL_KEY;
        }
        for (String countType : countTypes) {
            this.viewTypes[n++] = countType;
        }
        if (this.withMsc) {
            this.viewTypes[n] = MSC_KEY;
        }
        for (int i = 0; i < viewTypes.length; i++) {
            this.viewNames[i] = InstrumentTypeUtil.getName(viewTypes[i]);
        }

        for (int i = 0; i < viewNames.length; i++) {
            this.navItemSpecRoot.addChild(createNavItemSpec(i));
        }
    }

    private NavItemSpec createNavItemSpec(final int i) {
        final HistoryToken token = HistoryToken.buildKeyValue("t", viewTypes[i]); // $NON-NLS$
        return new NavItemSpec(viewTypes[i], viewNames[i], token, this)
                .withGoToDelegate(value -> {
                    token.withPrefix("M_S").with("s", value).fire(); // $NON-NLS$
                });
    }

    public void setTypes(String[] types) {
        this.filterTypes = types;
        this.countTypes = types;
    }

    public void setFilterTypes(String[] filterTypes) {
        this.filterTypes = filterTypes;
    }

    public void setCountTypes(String[] countTypes) {
        this.countTypes = countTypes;
    }

    public void setWithMsc(boolean withMsc) {
        this.withMsc = withMsc;
    }

    public void setWithAll(boolean withAll) {
        this.withAll = withAll;
    }

    @SuppressWarnings({"StringEquality"})
    public void onViewChanged() {
        final NavItemSpec selected = this.navItemSelectionModel.getSelected();

        this.pagingFeature.resetPaging();
        this.block.removeParameter(KEY_FILTER_TYPE);
        this.block.removeParameter(KEY_FILTER_MSC);
        if (ALL_KEY == selected.getId()) {
            this.block.setParameters(KEY_FILTER_TYPE, this.filterTypes);
        }
        else if (MSC_KEY == selected.getId()) {
            this.block.setParameter(KEY_FILTER_MSC, "true"); // $NON-NLS-0$
        }
        else {
            this.block.setParameter(KEY_FILTER_TYPE, selected.getId());
        }
        if (StringUtil.hasText(this.filterForUnderlyingsForType)) {
            this.block.setParameter(FILTER_FOR_UNDERLYINGS_FOR_TYPE, this.filterForUnderlyingsForType);
        }
        else {
            this.block.removeParameter(FILTER_FOR_UNDERLYINGS_FOR_TYPE);
        }
        if (this.filterForUnderlyingsOfLeveragProducts != null) {
            this.block.setParameter(FILTER_FOR_UNDERLYINGS_OF_LEVERAGE_PRODUCTS, this.filterForUnderlyingsOfLeveragProducts);
        }
        else {
            this.block.removeParameter(FILTER_FOR_UNDERLYINGS_OF_LEVERAGE_PRODUCTS);
        }
        reload();
    }

    protected final boolean updateModel() {
        this.typeCounts.clear();
        if (this.block.getResult() == null) {
            this.dtm = null;
            return true;
        }

        if (!doUpdateModel()) {
            return false;
        }
        updateViewNames();
        return true;
    }

    protected abstract void updateView();

    protected void updateViewNames() {
        int totalCount = 0;
        for (SearchTypeCount count : getTypecount()) {
            if (this.withMsc || !"MSC".equals(count.getType())) { // $NON-NLS-0$
                this.typeCounts.put(count.getType(), count.getValue());
                totalCount += Integer.parseInt(count.getValue());
            }
        }
        if (this.withAll) {
            this.typeCounts.put("ALL", Integer.toString(totalCount)); // $NON-NLS-0$
        }

        for (int i = 0; i < this.viewNames.length; i++) {
            final String count = getViewCount(i);
            final NavItemSpec navItem = getNavItemByType(this.viewTypes[i]);
            if(navItem != null) {
                navItem.setEnabled(!"0".equals(count)); // $NON-NLS$
                navItem.setNameSuffix("(" + count + ")");
            }
        }
        this.navItemSelectionModel.doUpdate();
    }

    private NavItemSpec getNavItemByType(String type) {
        final List<NavItemSpec> children = this.navItemSpecRoot.getChildren();
        for (NavItemSpec child : children) {
            if (child.getId().equals(type)) {
                return child;
            }
        }
        return null;
    }

    private String getViewCount(int i) {
        final String result = this.typeCounts.get(this.viewTypes[i]);
        return (result != null) ? result : "0"; // $NON-NLS-0$
    }

    public void setFilterForUnderlyingsForType(String filterForUnderlyingsForType, boolean fireChange) {
        this.filterForUnderlyingsForType = filterForUnderlyingsForType;
        if (fireChange) {
            onViewChanged();
        }
    }

    public void setFilterForUnderlyingsOfLeverageProducts(Boolean filterForUnderlyingsOfLeveragProducts, boolean fireChange) {
        this.filterForUnderlyingsOfLeveragProducts = filterForUnderlyingsOfLeveragProducts;
        if (fireChange) {
            onViewChanged();
        }
    }

    public void setFilterForUnderlyingsForType(String filterForUnderlyingsForType) {
        setFilterForUnderlyingsForType(filterForUnderlyingsForType, false);
    }

    public IndexedViewSelectionModel getIndexedViewSelectionModel() {
        return this.viewSelectionModel;
    }
}