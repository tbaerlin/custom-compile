package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.HashMap;

import com.extjs.gxt.ui.client.core.XDOM;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryToken;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PdfOptionSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.MultiContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * @author Ulrich Maurer
 *         Date: 01.12.11
 */
public class TabbedPricelistTabController extends MultiViewPageController {
    static final String INSTRUMENT_TYPE_ALL = "all"; // $NON-NLS$
    private static final NavItemSpec[] NAV_ITEM_SPECS = new NavItemSpec[]{
            new NavItemSpec("STK", I18n.I.stock(), HistoryToken.build("STK")), // $NON-NLS$
            new NavItemSpec("BND", I18n.I.bonds(), HistoryToken.build("BND")), // $NON-NLS$
            new NavItemSpec("FND", I18n.I.funds(), HistoryToken.build("FND")), // $NON-NLS$
            new NavItemSpec("CER", I18n.I.certificates(), HistoryToken.build("CER")), // $NON-NLS$
            new NavItemSpec(INSTRUMENT_TYPE_ALL, I18n.I.all(), HistoryToken.build(INSTRUMENT_TYPE_ALL))
    };
    static final String DEFAULT_TAB_ID = "BND"; // $NON-NLS$

    private final MultiContentView view;
    private final HashMap<String, TabbedPricelistController> mapControllers = new HashMap<String, TabbedPricelistController>();
    private TabbedPricelistController currentController;
    private HistoryToken historyToken;
    private String listid;

    public TabbedPricelistTabController(ContentContainer contentContainer) {
        super(contentContainer);
        this.view = new MultiContentView(this.getContentContainer());
        initViewSelectionModel(getViewNames(), XDOM.getUniqueId());
        this.view.init(this.getViewSelectionModel());
        final FloatingToolbar toolbar = this.view.getToolbar();
        toolbar.addFill();
        final ImageButton btnConf = GuiUtil.createImageButton("column-config-icon", null, null, I18n.I.configColumns());// $NON-NLS$
        btnConf.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                currentController.configureColumns();
            }
        });
        toolbar.add(btnConf);
    }

    private String[] getViewNames() {
        final String[] result = new String[NAV_ITEM_SPECS.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = NAV_ITEM_SPECS[i].getName();
        }
        return result;
    }

    @Override
    public void refresh() {
        this.currentController.refresh();
    }

    @Override
    public void onViewChanged() {
        final int viewId = getViewSelectionModel().getSelectedView();
        HistoryToken.builder(this.historyToken.getControllerId())
                .with("listid", this.listid) // $NON-NLS$
                .with("itype", NAV_ITEM_SPECS[viewId].getId()) // $NON-NLS$
                .fire();
    }

    private int getViewIndex(String instrumentType) {
        if (instrumentType == null) {
            instrumentType = DEFAULT_TAB_ID;
        }
        for (int i = 0, tab_specsLength = NAV_ITEM_SPECS.length; i < tab_specsLength; i++) {
            if (NAV_ITEM_SPECS[i].getId().equals(instrumentType)) {
                return i;
            }
        }
        Firebug.log("TabbedPricelistTabController - unhandled instrumentType: " + instrumentType);
        return 0;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        this.historyToken = event.getHistoryToken();
        this.listid = this.historyToken.get("listid"); // $NON-NLS$
        String instrumentType = this.historyToken.get("itype"); // $NON-NLS$
        if (instrumentType == null) {
            instrumentType = DEFAULT_TAB_ID;
        }

        final int viewId = getViewIndex(instrumentType);
        if (viewId != getViewSelectionModel().getSelectedView()) {
            changeSelectedView(viewId);
        }

        this.currentController = this.mapControllers.get(instrumentType);
        if (this.currentController == null) {
            this.currentController = new TabbedPricelistController(this.view, instrumentType);
            this.mapControllers.put(instrumentType, this.currentController);
        }
        this.currentController.onPlaceChange(event);
    }

    @Override
    public PdfOptionSpec getPdfOptionSpec() {
        if (this.currentController == null) {
            return null;
        }
        return this.currentController.getPdfOptionSpec();
    }
}
