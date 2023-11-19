package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author umaurer
 */
public abstract class SimpleTabController extends MultiViewPageController {
    protected final MultiContentView view;
    private String controllerName;
    protected String defaultListId;
    protected final PageController pageController;

    public SimpleTabController(ContentContainer contentContainer, String tabViewGroup) {
        super(contentContainer);
        this.view = new MultiContentView(this.getContentContainer());
        initViewSelectionModel(initViewSpecs(), 0, tabViewGroup);
        this.view.init(this.getViewSelectionModel()); // viewSelectionModel must be initialized before
        Firebug.log(getClass().getSimpleName() + ": # of views in MultiContentView " + getViewSelectionModel().getViewCount());
        this.pageController = createPageController(this.view);
        defaultListId = getTabId(0);
    }

    protected abstract PageController createPageController(MultiContentView view);

    protected abstract ViewSpec[] initViewSpecs();

    public void onPlaceChange(PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        this.controllerName = historyToken.getControllerId();
        final String listId = historyToken.get(1, this.defaultListId);
        changeSelectedView(getViewIndex(listId, 0));
        this.pageController.onPlaceChange(
                HistoryToken.builder(this.controllerName, listId).buildEvent()
        );
    }

    @Override
    public void activate() {
        this.pageController.activate();
    }

    @Override
    public void deactivate() {
        this.pageController.deactivate();
    }

    private int getViewIndex(String listId, int defaultValue) {
        for (int i = 0; i < getViewSelectionModel().getViewCount(); i++) {
            final ViewSpec viewSpec = getViewSelectionModel().getViewSpec(i);
            if (listId.equals(viewSpec.getId())) {
                return i;
            }
        }
        return defaultValue;
    }

    private String getTabId(int viewId) {
        final ViewSpec viewSpec = getViewSelectionModel().getViewSpec(viewId);
        if (viewSpec == null) {
            Firebug.log("SimpleTabController - No viewSpec found for viewId " + viewId);
            return "tab-0"; // $NON-NLS$
        }
        final String id = viewSpec.getId();
        return id == null ? ("tab-" + viewId) : id; // $NON-NLS-0$
    }

    @Override
    public void onViewChanged() {
        PlaceUtil.goTo(StringUtil.joinTokens(this.controllerName, getTabId(getViewSelectionModel().getSelectedView())));
    }
}
