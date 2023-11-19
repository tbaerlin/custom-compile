package de.marketmaker.iview.mmgwt.mmweb.client;


import com.google.gwt.http.client.*;
import com.google.gwt.json.client.*;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.dmxml.MSCProfiledQuoteList;
import de.marketmaker.iview.dmxml.ResponseType;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.KwtCertificatePricelistsController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.kwt.KwtProductMap;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.GuiDefsChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.fund.FundprospectKWTSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;

import java.util.ArrayList;
import java.util.List;

/**
 * load additional guidef values from the "guidefs-import" parameter
 * and try to merge them into the current guidef tree.
 *
 * this only works for the first level of imports
 * (imports from an imported file are not processed)
 */
public class GuiDefsUpdater implements RequestCallback, Command {

    public static final String KWT_PRODUCTMAP = "kwt_productmap"; // $NON-NLS$
    public static final String DYN_IMPORT_KEY = "guidefs-import"; // $NON-NLS$

    public static Timer updateTimer;

    private List<DmxmlContext.Block<MSCProfiledQuoteList>> blocks = new ArrayList<DmxmlContext.Block<MSCProfiledQuoteList>>();
    private final DmxmlContext context = new DmxmlContext();

    // top-level guidef elements that will be overridden by an updated kwtadmin.json
    public static final String[] GUIDEF_OVERRIDES= new String[] {
            KwtProductMap.INTEREST_KEY,                  // included in the product map, update triggered by the event bus
            FundprospectKWTSnippet.KWT_REPORTS_LIST,     // guidefs value is not cached
            "list_kwt_portal_fonds1",                    // $NON-NLS-0$
            "list_kwt_portal_fonds2",                    // $NON-NLS-0$
            "list_kwt_portal_fonds3",                    // $NON-NLS-0$
            "list_kwt_portal_zerts1",                    // $NON-NLS-0$
            "list_kwt_portal_zerts2",                    // $NON-NLS-0$
            "list_kwt_portal_zerts3",                    // $NON-NLS-0$
            "kwt_fonds_issuer_query",                    // $NON-NLS-0$
            "kwt_zert_issuer_query",                     // $NON-NLS-0$
            KwtCertificatePricelistsController.JSON_KEY_PRICELISTS,
            "Kwtgui",                                    // $NON-NLS-0$
    };

    public static void scheduleGuiDefUpdates(String updateInterval) {
        if (updateTimer != null) {
            Firebug.info("<GuiDefsUpdater> already got an update schedule for guidefs,"   // $NON-NLS-0$
                    + " ignoring call to scheduleGuiDefUpdates()");                   // $NON-NLS-0$
            return;
        }
        Firebug.debug("<GuiDefsUpdater> setup update schedule to '" + updateInterval + "'");  // $NON-NLS$
        updateTimer = new Timer() {
            @Override
            public void run() {
                Firebug.debug("running GuiDefsUpdater"); // $NON-NLS-0$
                new GuiDefsUpdater().execute();
            }
        };
        updateTimer.scheduleRepeating(parseIntervalTime(updateInterval));
    }

    private static int parseIntervalTime(String interval) {
        try {
            final int amount = Integer.parseInt(interval.substring(0, interval.length() - 1));
            if (interval.endsWith("s")) { // $NON-NLS-0$
                return 1000 * amount;
            }
            if (interval.endsWith("m")) { // $NON-NLS-0$
                return 60 * 1000 * amount;
            }
            if (interval.endsWith("h")) { // $NON-NLS-0$
                return 60 * 60 * 1000 * amount;
            }
        }
        catch (NumberFormatException ex) {
            Firebug.warn("invalid value was: '" + interval + "', fallback to '60m'", ex); // $NON-NLS$
        }
        return 60 * 60 * 1000; // default: 60 min
    }

    public void execute() {
        final JSONValue jsonImport = currentGuiDefs().get(DYN_IMPORT_KEY);
        if (jsonImport == null) {
            return;
        }

        final JSONArray jsonArray = jsonImport.isArray();
        if (jsonArray == null) {
            return;
        }

        final List<String> importFiles = new ArrayList<String>();
        for (int i = 0; i < jsonArray.size(); i++) {
            final JSONObject current = jsonArray.get(i).isObject();
            if (isAllowedBySelector(current)) {
                final JSONValue jFilename = current.get("filename"); // $NON-NLS-0$
                importFiles.add(jFilename.isString().stringValue());
            }
        }

        for (String url : importFiles) {
            final RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.POST, url);
            requestBuilder.setHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8"); // $NON-NLS$
            requestBuilder.setHeader("Pragma", "no-cache"); // $NON-NLS$
            requestBuilder.setHeader("Cache-Control", "no-cache"); // $NON-NLS$
            try {
                requestBuilder.sendRequest("", this); // $NON-NLS$
            }
            catch (RequestException ex) {
                onError(url, ex);
            }
        }
    }

    private JSONObject currentGuiDefs() {
        final JSONWrapper currentGuiDefs = SessionData.INSTANCE.getGuiDefs();
        return currentGuiDefs.getValue().isObject();
    }

    private boolean isAllowedBySelector(JSONObject object) {
        final JSONValue jSelector = object.get("selector"); // $NON-NLS$
        if (jSelector != null) {
            final Selector selector = Selector.valueOf(jSelector.isString().stringValue());
            if (!selector.isAllowed()) {
                return false;
            }
        }
        return true;
    }

    public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == 200) {
            importJson(JSONParser.parseLenient(response.getText()));
        }
        else if (response.getStatusCode() == 404) {
            Firebug.debug("not found: " + request.toString()); // $NON-NLS-0$
        }
        else {
            onError(response.getStatusCode() + "\n" + response.getHeadersAsString(), null); // $NON-NLS-0$
        }
    }

    public void onError(Request request, Throwable t) {
        onError("onError: " + t.getMessage(), t); // $NON-NLS-0$
    }

    private void onError(String url, Throwable th) {
        Firebug.error(url, th);
        final String message = I18n.I.fatalGUINotLoaded() + url + "\n" + th;  // $NON-NLS-0$
        DebugUtil.logToServer(message);
        Window.alert(message);
    }

    private void importJson(JSONValue newJsonValues) {
        updateProductMaps(newJsonValues);
        overrideTopLevelValues(newJsonValues, GUIDEF_OVERRIDES);
    }

    private void overrideTopLevelValues(JSONValue newJsonValues, String... keys) {
        final JSONObject currentGuiDefsObject = currentGuiDefs();

        for (String key : keys) {
            JSONValue newValue = newJsonValues.isObject().get(key);
            currentGuiDefsObject.put(key, newValue);
            if (key.startsWith(ListLoader.LIST_PREFIX)) {
                this.blocks.add(ListLoader.createBlock(this.context, key));
            }
        }

        this.context.issueRequest(new AsyncCallback<ResponseType>() {
            public void onFailure(Throwable throwable) {
                Firebug.error("cannot load lists", throwable); // $NON-NLS-0$
                notifyListeners();
            }

            public void onSuccess(ResponseType result) {
                SessionData.INSTANCE.updateLists(ListLoader.createListsMap(blocks));
                notifyListeners();
            }
        });
    }

    private void notifyListeners() {
        Firebug.debug("fire GuiDefsChangedEvent"); // $NON-NLS-0$
        EventBusRegistry.get().fireEvent(new GuiDefsChangedEvent());
        // do we need to cleanup?
        for (DmxmlContext.Block<MSCProfiledQuoteList> block : blocks) {
            context.removeBlock(block);
        }
    }

    private void updateProductMaps(JSONValue newJsonValues) {
        final JSONObject currentGuiDefsObject = currentGuiDefs();
        final JSONValue oldProductMap = currentGuiDefsObject.get(KWT_PRODUCTMAP);

        // remove old listids
        if (oldProductMap != null) {
            JSONArray oldProductMapArray = oldProductMap.isArray();
            for (int i = oldProductMapArray.size()-1; i >= 0; i--) {
                final String listid = oldProductMapArray.get(i).isObject().get("listid").isString().stringValue(); // $NON-NLS$
                currentGuiDefsObject.put(listid, JSONNull.getInstance());
            }
        }

        // override product map
        final JSONValue newProductMap = newJsonValues.isObject().get(KWT_PRODUCTMAP);
        currentGuiDefsObject.put(KWT_PRODUCTMAP, newProductMap);

        // insert new listids
        if (newProductMap != null) {
            JSONArray newProductMapArray = newProductMap.isArray();
            for (int i = newProductMapArray.size()-1; i >= 0; i--) {
                final String listid = newProductMapArray.get(i).isObject().get("listid").isString().stringValue(); // $NON-NLS$
                currentGuiDefsObject.put(listid, newJsonValues.isObject().get(listid));
            }
        }
    }

}
