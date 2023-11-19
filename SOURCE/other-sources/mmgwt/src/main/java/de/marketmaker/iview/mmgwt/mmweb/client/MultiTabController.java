package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.core.XDOM;
import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich Maurer
 *         Date: 28.10.11
 */
public class MultiTabController extends MultiViewPageController {
    private final NavItemSpec[] navItemSpecs;
    private final MultiContentView view;
    private HistoryToken historyToken;
    private final String jsonKey;
    private final TabControllerFactory tabControllerFactory;


    public interface TabControllerFactory {
        public AbstractPageController createTabController(MultiContentView view, JSONWrapper tabConfig);
    }


    public MultiTabController(ContentContainer contentContainer, String jsonKey, TabControllerFactory tabControllerFactory) {
        super(contentContainer);
        this.jsonKey = jsonKey;
        this.tabControllerFactory = tabControllerFactory;
        this.view = new MultiContentView(this.getContentContainer());

        this.navItemSpecs = initTabSpecs();
        initViewSelectionModel(getViewNames(), XDOM.getUniqueId());

        this.view.init(this.getViewSelectionModel());
    }

    private NavItemSpec[] initTabSpecs() {
        final JSONWrapper jsonWrapper = SessionData.INSTANCE.getGuiDef(this.jsonKey);
        if (!jsonWrapper.isValid()) {
            return new NavItemSpec[]{};
        }

        final int tabCount = jsonWrapper.size();
        final List<NavItemSpec> list = new ArrayList<NavItemSpec>(tabCount);

        for (int i = 0; i < tabCount; i++) {
            final JSONWrapper tabConfig = jsonWrapper.get(i);
            final String id = tabConfig.get("id").stringValue(); // $NON-NLS$
            final String title = tabConfig.get("title").stringValue(); // $NON-NLS$
            list.add(new NavItemSpec(id, title, HistoryToken.fromToken(id), this.tabControllerFactory.createTabController(this.view, tabConfig)));
        }

        return list.toArray(new NavItemSpec[list.size()]);
    }

    private String[] getViewNames() {
        final String[] result = new String[this.navItemSpecs.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = this.navItemSpecs[i].getName();
        }
        return result;
    }

    private int getSelectedView() {
        return getViewSelectionModel().getSelectedView();
    }

    private NavItemSpec getSelectedTabSpec() {
        return this.navItemSpecs[getSelectedView()];
    }

    private PageController getSelectedController() {
        return getSelectedTabSpec().getController();
    }

    @Override
    public void refresh() {
        getSelectedController().refresh();
    }

    private int getViewIndex(String viewId, int defaultValue) {
        for (int i = 0; i < this.navItemSpecs.length; i++) {
            if (viewId.equals(this.navItemSpecs[i].getId())) {
                return i;
            }
        }
        return defaultValue;
    }

    @Override
    public void onViewChanged() {
        final int viewId = getSelectedView();
        if (this.navItemSpecs.length > viewId && this.navItemSpecs[viewId].getController() != null) {
            assert (viewId < this.navItemSpecs.length);
            HistoryToken.builder(this.historyToken.getControllerId(), this.navItemSpecs[viewId].getId()).fire();
        }
        else {
            this.view.setContent(new HTML("<b>TODO...<b>")); // $NON-NLS-0$
        }
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.historyToken = event.getHistoryToken();

        final String p1 = this.historyToken.get(1, null);
        final int viewId = (p1 != null) ? getViewIndex(p1, 0) : getSelectedView();

        final boolean viewChanged = viewId != getSelectedView();
        if (viewChanged) {
            changeSelectedView(viewId);
        }

        getSelectedController().onPlaceChange(event);
    }
}
