package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import java.util.Collections;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractDepotObjectPortraitController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.ObjectIdSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermStr;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SymbolSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.function.SingleConsumable;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ProvidesContentHeader;

/**
 * Author: umaurer
 * Created: 23.03.15
 */
public class DashboardPageController extends AbstractPageController implements ProvidesContentHeader {
    public static final String HISTORY_TOKEN_DASHBOARDS = "D_DBS"; // $NON-NLS$
    private static final String SUB_CONTROLLER_ID_PREFIX = "DASH-"; // $NON-NLS$
    private final HandlerRegistration eventBusHandlerRegistration;
    private boolean active = false;

    private final SingleConsumable<DashboardController> dashboardControllerOfNewAndClonedDashboards = new SingleConsumable<>();

    public enum DashboardIdStrategy {
        BY_ID_PARAMETER {
            @Override
            public String getId(PlaceChangeEvent event) {
                final HistoryToken historyToken = event.getHistoryToken();
                return historyToken.get("id");  // $NON-NLS$
            }
        },
        BY_SUB_CONTROLLER_ID {
            @Override
            public String getId(PlaceChangeEvent event) {
                final HistoryToken historyToken = event.getHistoryToken();
                final String sc = historyToken.get("sc");  // $NON-NLS$
                if(!StringUtil.hasText(sc)) {
                    return null;
                }
                return sc.replace(SUB_CONTROLLER_ID_PREFIX, "");
            }
        };

        protected abstract String getId(PlaceChangeEvent event);
    }

    private final DashboardIdStrategy dashboardIdStrategy;

    private DashboardController dashboardController;
    private String id;

    private IdHandler idHandler;

    public DashboardPageController(DashboardIdStrategy dashboardIdStrategy) {
        this.dashboardIdStrategy = dashboardIdStrategy;

        this.eventBusHandlerRegistration = EventBusRegistry.get().addHandler(DashboardStateChangeEvent.getType(), this::onDashboardStateChanged);
    }

    private void onDashboardStateChanged(DashboardStateChangeEvent event) {
        // Refresh dashboard if its config has changed. Especially necessary for sub controller dashboards that have
        // more than one role.
        if (event.getAction() == DashboardStateChangeEvent.Action.UPDATE
                && this.dashboardController != null
                && this.dashboardController.getConfig() != null
                && StringUtil.equals(event.getId(), this.dashboardController.getConfig().getId())) {
            refresh();
            return;
        }
        if(!this.active) {
            return;
        }
        switch (event.getAction()) {
            case EDIT_NEW:
                editNewDashboard(event.getId());
                break;
            case EDIT_CLONE:
                editCopyOfDashboard(event.getId());
                break;
            case CANCEL_EDIT:
                if (DashboardController.isNewDashboard(event.getId())) {
                    cancelNewDashboard();
                }
                else {
                    refresh();
                }
                break;
            case EDIT_SELECTED:
                editCurrentDashboard();
                break;
        }
    }

    private void editCurrentDashboard() {
        if(this.dashboardController != null) {
            this.dashboardController.startEditMode();
        }
    }

    @Override
    public void onPlaceChange(final PlaceChangeEvent event) {
        final HistoryToken historyToken = event.getHistoryToken();
        if (historyToken.containsKey(AbstractDepotObjectPortraitController.OBJECTID_KEY)) {
            final String objectId = historyToken.get(AbstractDepotObjectPortraitController.OBJECTID_KEY);
            this.idHandler = snippet -> {
                if (snippet instanceof ObjectIdSnippet) {
                    ((ObjectIdSnippet) snippet).setObjectId(objectId);
                }
            };
        }
        else if (historyToken.containsKey("symbol")) { // $NON-NLS$
            final String symbol = historyToken.get("symbol"); // $NON-NLS$
            this.idHandler = snippet -> {
                if (snippet instanceof SymbolSnippet) {
                    ((SymbolSnippet) snippet).setSymbol(null, symbol, null);
                }
            };
        }
        else {
            this.idHandler = null;
        }

        final String idFromToken = this.dashboardIdStrategy.getId(event);
        final String id = idFromToken != null ? idFromToken : this.id;
        final DashboardConfig dc = ConfigDao.getInstance().getConfigById(id);
        if (dc == null) {
            handleNonExistingConfig(event);
            return;
        }

        if (this.dashboardController != null && StringUtil.equals(id, this.id)) {
            applyId(event, this.dashboardController, idHandler);
            return;
        }

        destroyDashboardController();
        this.dashboardController = DashboardController.create(context, dc);
        applyId(event, this.dashboardController, this.idHandler);
        this.id = id;
    }

    @Override
    public SafeHtml getContentHeader() {
        if(this.dashboardController == null || this.dashboardController.getDashboardName() == null) {
            return null;
        }
        return TextUtil.toSafeHtml(this.dashboardController.getDashboardName());
    }

    @Override
    public void refresh() {
        // this is necessary because refresh is used as cancel.
        if(this.dashboardController != null && this.dashboardController.isInEditMode()) {
            return;
        }
        destroyDashboardController();
        this.dashboardController = DashboardController.create(context, ConfigDao.getInstance().getConfigById(this.id));
        applyId(null, this.dashboardController, this.idHandler);
        super.refresh();
    }

    private void handleNonExistingConfig(PlaceChangeEvent event) {
        destroyDashboardController();
        this.id = null;
        this.dashboardController = DashboardController.createEmpty();
        this.dashboardController.onPlaceChange(event);
    }

    private void destroyDashboardController() {
        if (this.dashboardController != null) {
            this.dashboardController.destroy();
        }
    }

    private void applyId(PlaceChangeEvent event, DashboardController controller, IdHandler idHandler) {
        if (idHandler != null) {
            controller.applyId(idHandler);
        }
        controller.onPlaceChange(event);
    }

    private void editNewDashboard(String dashboardId) {
        final DashboardConfig config = new DashboardConfig();
        config.setId(dashboardId);
        config.setRoles(Collections.singletonList(ConfigDao.DASHBOARD_ROLE_GLOBAL));
        config.setName(PermStr.DASHBOARD_DEFAULT_NAME_ADD.value());
        config.setAccess(DashboardConfig.Access.PRIVATE);
        final DashboardController newController = DashboardController.create(new DmxmlContext(), config);
        newController.show();
        newController.startEditMode();
        this.dashboardControllerOfNewAndClonedDashboards.push(newController);
    }

    private void editCopyOfDashboard(String dashboardId) {
        final DashboardConfig sourceConfig = ConfigDao.getInstance().getConfigById(DashboardController.getCopySourceDashboardId(dashboardId));
        final DashboardConfig config = sourceConfig.createCopy();
        config.setId(dashboardId);
        config.setName(I18n.I.dashboardDefaultNameCopy(sourceConfig.getName()));
        config.setAccess(DashboardConfig.Access.PRIVATE);
        final DashboardController newController = DashboardController.create(new DmxmlContext(), config);
        newController.show();
        newController.startEditMode();
        this.dashboardControllerOfNewAndClonedDashboards.push(newController);
    }

    private void cancelNewDashboard() {
        this.dashboardControllerOfNewAndClonedDashboards.pull();
        this.dashboardController.show();
    }

    @Override
    public void activate() {
        this.active = true;
        if (this.dashboardController != null) {
            this.dashboardController.activate();
        }
    }

    @Override
    public void deactivate() {
        this.active = false;
        if (this.dashboardController != null) {
            this.dashboardController.deactivate();
        }
        //noinspection Convert2MethodRef
        this.dashboardControllerOfNewAndClonedDashboards.pull()
                .ifPresent(c -> c.maybeSaveEdit()); // do not replace lambda with method reference, because this will cause a NPE
        // calling deactivate on the temporary dashboard controller is not necessary, because maybeSaveEdit calls destroy on all of its snippets if it is a new or cloned dashboard.
    }

    @Override
    public void destroy() {
        destroyDashboardController();
        this.eventBusHandlerRegistration.removeHandler();
        super.destroy();
    }

    public static String toSubControllerId(String layoutGuid) {
        return DashboardPageController.SUB_CONTROLLER_ID_PREFIX + layoutGuid;
    }
}