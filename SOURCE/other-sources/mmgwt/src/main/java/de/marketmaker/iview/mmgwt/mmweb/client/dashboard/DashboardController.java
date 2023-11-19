package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.DashboardSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.DashboardWidgetNotAvailableSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsContextController;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Author: umaurer
 * Created: 20.03.15
 */

public class DashboardController extends AbstractPageController implements SnippetsContextController, SnippetDropHandler {
    private static final String NEW_DASHBOARD_PREFIX = "@new@"; // $NON-NLS$
    private static final String COPY_DASHBOARD_PREFIX = "@copy@"; // $NON-NLS$

    private DashboardConfig config;
    private DashboardViewIfc dashboardView;
    private TableStructureModel<Snippet> tableStructure;
    private boolean deferredRequestPending = false;

    private static int newDashboardCounter = 0;
    private IdHandler idHandler;

    private final static HashMap<String, SnippetClass> MARKET_DATA_DUMMY_SNIPPETS = new HashMap<>();

    private DashboardController(final DmxmlContext context) {
        super(context);
    }

    private DashboardController() {
        super();
    }

    public static DashboardController create(final DmxmlContext context, final DashboardConfig config) {
        final DashboardController dashboardController = new DashboardController(context);
        dashboardController.init(config);
        return dashboardController;
    }

    public static DashboardController createEmpty() {
        final DashboardController dashboardController = new DashboardController();
        dashboardController.initEmpty();
        return dashboardController;
    }

    private void initEmpty() {
        this.dashboardView = new DashboardViewEmpty();
    }

    private void init(final DashboardConfig config) {
        this.dashboardView = new DashboardView(this, config);
        setConfig(config);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        maybeSaveEdit();
        for (Snippet snippet : getSnippets()) {
            snippet.onPlaceChange(event);
        }
        reload();
        MainController.INSTANCE.getView().setContentHeader(getDashboardName());
        show();
    }

    public void show() {
        getContentContainer().setContent(this.dashboardView.asWidget());
    }

    public DashboardViewIfc getDashboardView() {
        return this.dashboardView;
    }

    public void startEditMode() {
        this.dashboardView.enableEdit();
    }

    public boolean isInEditMode() {
        return this.dashboardView.isEditMode();
    }

    public boolean isNewDashboard() {
        return isNewDashboard(this.config.getId());
    }

    public static boolean isNewDashboard(String id) {
        return id.startsWith(NEW_DASHBOARD_PREFIX) || id.startsWith(COPY_DASHBOARD_PREFIX);
    }

    public void cancelEditMode() {
        this.dashboardView.disableEdit();
        if (isNewDashboard()) {
            destroy();
        }
        else {
            resetConfig();
        }
        DashboardStateChangeEvent.Action.CANCEL_EDIT.fire(this.config.getId());
    }

    public void saveEditMode() {
        this.dashboardView.disableEdit();
        if (isNewDashboard()) {
            saveNewAndDestroy();
        }
        else {
            saveConfig();
        }
    }

    public void maybeSaveEdit() {
        if (this.dashboardView.isEditMode()) {
            saveEditMode();
        }
    }

    @Override
    public Snippet getSnippet(String id) {
        throw new UnsupportedOperationException("getSnippet(id) is not supported"); // $NON-NLS$
    }

    @Override
    public void updateVisibility(SnippetView sv, boolean visible) {
        throw new UnsupportedOperationException("updateVisibility(SnippetView,boolean) is not supported"); // $NON-NLS$
    }

    @Override
    public void handleVisibility() {
        throw new UnsupportedOperationException("handleVisibility() is not supported"); // $NON-NLS$
    }

    public void reload() {
        if (this.deferredRequestPending) {
            return;
        }

        this.deferredRequestPending = true;

        // it is important that the reload happens deferred, so that for example
        // all property change listeners will be invoked before a reload is triggered
        Scheduler.get().scheduleDeferred((Command) () -> {
            refresh();
            deferredRequestPending = false;
        });
    }

    @Override
    protected void onResult() {
        for (Snippet snippet : getSnippets()) {
            try {
                snippet.updateView();
            }
            catch (Exception e) {
                Firebug.error("cannot update snippet view: " + snippet.getId(), e); // $NON-NLS-0$
            }
        }
    }

    @Override
    public void activate() {
        for (Snippet snippet : getSnippets()) {
            snippet.activate();
        }
    }

    @Override
    public void deactivate() {
        maybeSaveEdit();
        for (Snippet snippet : getSnippets()) {
            snippet.deactivate();
        }
    }

    @Override
    public void destroy() {
        for (Snippet snippet : getSnippets()) {
            snippet.destroy();
        }
    }

    public void setConfig(DashboardConfig config) {
        for (Snippet snippet : getSnippets()) {
            snippet.destroy();
        }
        this.config = config;
        this.tableStructure = createTableStructure(config.getSnippetConfigs());
        this.dashboardView.setName(this.config.getName());
        updateView();
        if (this.idHandler != null) {
            applyId(this.idHandler);
        }
    }

    private void updateView() {
        this.dashboardView.show(getSnippets());
    }

    public List<Snippet> getSnippets() {
        final List<Snippet> snippets = new ArrayList<>();
        if (this.tableStructure == null) {
            return snippets;
        }

        for (TableStructureModel<Snippet>.Cell cell : this.tableStructure.getCells()) {
            Snippet snippet = cell.getUserObject();
            if (snippet == null) {
                snippet = new DropTargetSnippet();
                snippet.setContextController(this);
            }

            snippet.getConfiguration().put("row", cell.getRow()); // $NON-NLS$
            snippet.getConfiguration().put("col", cell.getTCol()); // $NON-NLS$
            snippets.add(snippet);
        }

        return snippets;
    }

    private TableStructureModel<Snippet> createTableStructure(ArrayList<SnippetConfiguration> configs) {
        final TableStructureModel<Snippet> tableStructure = new TableStructureModel<>();

        for (final SnippetConfiguration c : configs) {
            final SnippetConfiguration config = c.copy();
            Snippet snippet;
            final SnippetClass marketDataDummySnippetClass = MARKET_DATA_DUMMY_SNIPPETS.get(config.getName());
            if(marketDataDummySnippetClass != null) {
                snippet = marketDataDummySnippetClass.newSnippet(this.context, config);
                Firebug.info("<DashboardController.createTableStructure> created dummy snippet for market data: " + config.getName());
            }
            else {
                snippet = SnippetClass.create(this.context, config);
                // Sort out market data snippets if the user does not have a licence to view market data.
                if (snippet != null && !SessionData.isWithMarketData() && !(snippet instanceof DashboardSnippet)) {
                    snippet.destroy();
                    snippet = registerAndCreateDummySnippetForMarketData(config);
                }
                // If it is not a market data snippet, it must be a dashboard widget snippet with an invalid layout.
                else if (snippet == null) {
                    snippet = registerAndCreateDummySnippetForLayout(config);
                }
            }
            assert snippet != null;
            snippet.setContextController(this);
            tableStructure.add(snippet, config.getString("row"), config.getString("col"), config.getString("rowSpan"), config.getString("colSpan")); // $NON-NLS$
        }
        return tableStructure;
    }

    private Snippet registerAndCreateDummySnippetForLayout(SnippetConfiguration config) {
        // Lets create a dummy snippet to preserve its configuration even if the user edits the dashboard but is not
        // allowed to see a widget that was put on the dashboard by sb. who is allowed to see the widget.
        // We do not register this dummy snippet for the snippet menu config, because the user does not have the
        // permission to view it, so there is no necessity for him to put it on a dashboard again.
        if(!StringUtil.hasText(config.getName())) {
            throw new IllegalArgumentException("snippet config does not contain a snippet name " + config);  // $NON-NLS$
        }
        Firebug.warn("<DashboardController.registerAndCreateDummySnippetForLayout> " + config.getName());
        final DashboardWidgetNotAvailableSnippet.Class dummySnippetClass = new DashboardWidgetNotAvailableSnippet.Class(config.getName(), false);
        SnippetClass.addClass(dummySnippetClass);
        return dummySnippetClass.newSnippet(this.context, config);
    }

    private Snippet registerAndCreateDummySnippetForMarketData(SnippetConfiguration config) {
        Firebug.info("<DashboardController.registerAndCreateDummySnippetForMarketData> " + config.getName());
        final DashboardWidgetNotAvailableSnippet.Class dummySnippetClass = new DashboardWidgetNotAvailableSnippet.Class(config.getName(), true);
        MARKET_DATA_DUMMY_SNIPPETS.put(dummySnippetClass.getSnippetClassName(), dummySnippetClass);
        return dummySnippetClass.newSnippet(this.context, config);
    }

    public void applyId(IdHandler idHandler) {
        this.idHandler = idHandler;
        final List<Snippet> snippets = getSnippets();
        for (Snippet snippet : snippets) {
            idHandler.applyId(snippet);
        }
    }

    public SnippetCells getSnippetCells(String transferData, int toRow, int toCol) {
        final String[] splitted = transferData.split(":"); // $NON-NLS$
        final SnippetConfiguration sc;
        if (splitted.length == 4 && "move".equals(splitted[1])) { // $NON-NLS$
            final int fromRow = Integer.parseInt(splitted[2]);
            final int fromCol = Integer.parseInt(splitted[3]);
            final Snippet snippet = this.tableStructure.getUserObject(fromRow, fromCol);
            if (snippet == null) {
                return null;
            }
            sc = snippet.getConfiguration();
        }
        else if (splitted.length == 3 && "new".equals(splitted[1])) { // $NON-NLS$
            sc = SnippetClass.getDefaultConfig(splitted[2]);
        }
        else {
            throw new IllegalStateException("invalid transferData format: " + transferData); // $NON-NLS$
        }

        final int rowSpan = sc.getInt("rowSpan", 1); // $NON-NLS$
        final int colSpan = sc.getInt("colSpan", 1); // $NON-NLS$
        return new SnippetCells(toRow, toCol, rowSpan, colSpan);
    }

    public boolean isDropAllowed(String transferData, int toRow, int toCol) {
        final SnippetCells cells = getSnippetCells(transferData, toRow, toCol);
        return cells != null && this.tableStructure.isFree(toRow, toCol, cells.getRowSpan(), cells.getColSpan());
    }

    public boolean onDndEnter(String transferData, int toRow, int toCol) {
        final SnippetCells cells = getSnippetCells(transferData, toRow, toCol);
        if (cells == null) {
            return false;
        }
        final TableCellPos[] freeCells = this.tableStructure.getFreeCells(toRow, toCol, cells.getRowSpan(), cells.getColSpan());
        Firebug.debug("enter -> " + transferData + "   to " + toRow + ", " + toCol + "    free: " + Arrays.asList(freeCells));
        if (freeCells.length == cells.getCellCount()) {
            this.dashboardView.setHoverStyle(freeCells, "mm-drop-accepted"); // $NON-NLS$
            return true;
        }

        this.dashboardView.setHoverStyle(freeCells, "mm-drop-rejected"); // $NON-NLS$
        return false;
    }

    @Override
    public void onDndLeave() {
        this.dashboardView.removeHoverStyle();
    }

    public void onDrop(String transferData, int toRow, int toCol) {
        Firebug.debug("onDrop(" + transferData + ", " + toRow + ", " + toCol + ")");
        final String[] splitted = transferData.split(":"); // $NON-NLS$
        if (splitted.length == 4 && "move".equals(splitted[1])) { // $NON-NLS$
            moveSnippet(Integer.parseInt(splitted[2]), Integer.parseInt(splitted[3]), toRow, toCol);
            return;
        }
        if (splitted.length == 3 && "new".equals(splitted[1])) { // $NON-NLS$
            newSnippet(splitted[2], toRow, toCol);
            return;
        }
        throw new IllegalStateException("invalid transferData format: " + transferData); // $NON-NLS$
    }

    public void moveSnippet(int fromRow, int fromCol, int toRow, int toCol) {
        this.tableStructure.move(fromRow, fromCol, toRow, toCol);
        updateView();
    }

    public void newSnippet(String snippetClassName, int row, int col) {
        final SnippetConfiguration sc = new SnippetConfiguration(snippetClassName);
        sc.put("context", Snippet.CONFIG_CONTEXT_DASHBOARD); // $NON-NLS$
        sc.put("row", row); // $NON-NLS$
        sc.put("col", col); // $NON-NLS$
        final Snippet snippet = SnippetClass.create(this.context, sc);
        assert snippet != null;
        snippet.setContextController(this);
        this.tableStructure.add(snippet, row, col, sc.getInt("rowSpan", 1), sc.getInt("colSpan", 1)); // $NON-NLS$
        updateView();
        reload(); // TODO: nur einzelnes snippet load?
    }

    public void removeSnippet(Snippet snippet) {
        final SnippetConfiguration sc = snippet.getConfiguration();
        final int row = Integer.parseInt(sc.getString("row")); // $NON-NLS$
        final int col = Integer.parseInt(sc.getString("col")); // $NON-NLS$
        this.tableStructure.remove(row, col);
        updateView();
        snippet.destroy();
    }

    private void resetConfig() {
        setConfig(this.config);
    }

    public DashboardConfig createEditedConfig() {
        final List<TableStructureModel<Snippet>.Cell> cells = this.tableStructure.getCells();
        final List<SnippetConfiguration> snippetConfigs = new ArrayList<>(cells.size());
        for (TableStructureModel<Snippet>.Cell cell : cells) {
            final Snippet snippet = cell.getUserObject();
            if (snippet != null) {
                snippetConfigs.add(snippet.getConfiguration());
            }
        }
        return this.config.createCopy(snippetConfigs);
    }

    private void saveConfig() {
        final DashboardConfig config = createEditedConfig();
        config.setName(this.dashboardView.getName());
        ConfigDao.getInstance().save(config, DashboardStateChangeEvent.Action.UPDATE::fire);
    }

    private void saveNewAndDestroy() {
        final DashboardConfig config = createEditedConfig();
        config.setName(this.dashboardView.getName());
        ConfigDao.getInstance().saveNew(config, id -> {
            config.setId(id);
            DashboardStateChangeEvent.Action.UPDATE.fire(id);
            destroy();
        });
    }

    public static String getNextNewDashboardId() {
        return NEW_DASHBOARD_PREFIX + newDashboardCounter++;
    }

    public static String getCopyDashboardId(String sourceId) {
        return COPY_DASHBOARD_PREFIX + sourceId;
    }

    public static String getCopySourceDashboardId(String id) {
        if (!id.startsWith(COPY_DASHBOARD_PREFIX)) {
            return null;
        }
        return id.substring(COPY_DASHBOARD_PREFIX.length());
    }

    public void configureSpans(final Snippet snippet, Widget headerToolWidget) {
        final SnippetConfiguration config = snippet.getConfiguration();
        final int row = config.getInt("row", -1); // $NON-NLS$
        final int col = config.getInt("col", -1); // $NON-NLS$
        final SpanConfigPopup popup = new SpanConfigPopup((rowSpan, colSpan) -> {
            config.put("rowSpan", rowSpan); // $NON-NLS$
            config.put("colSpan", colSpan); // $NON-NLS$
            this.tableStructure.setSpan(row, col, rowSpan, colSpan);
            updateView();
        });
        final int rowSpan = config.getInt("rowSpan", 1); // $NON-NLS$
        final int colSpan = config.getInt("colSpan", 1); // $NON-NLS$
        final List<TableCellPos> freeCells = new ArrayList<>();
        final int mCol = this.tableStructure.toModelCol(row, col);
        for (int r = row, rmax = row + rowSpan; r < rmax; r++) {
            int tc = this.tableStructure.toTableCol(r, mCol);
            for (int mc = mCol, cmax = mCol + colSpan; mc < cmax; mc++, tc++) {
                freeCells.add(new TableCellPos(r, tc, mc));
            }
        }
        freeCells.addAll(Arrays.asList(this.tableStructure.getFreeCells(row, col, SpanConfigPopup.ROW_COUNT, SpanConfigPopup.COLUMN_COUNT)));
        popup.show(headerToolWidget, row, mCol, rowSpan, colSpan, freeCells);
    }

    public String getDashboardName() {
        return this.dashboardView.getName();
    }

    public DashboardConfig getConfig() {
        return this.config;
    }
}