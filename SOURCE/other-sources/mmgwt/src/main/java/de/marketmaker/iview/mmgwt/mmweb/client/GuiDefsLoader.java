/*
 * GuiDefsLoader.java
 *
 * Created on 13.01.2009 16:56:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONParser;
import com.google.gwt.json.client.JSONValue;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Loads json files that define which snippets appear on which page. Multiple files are loaded,
 * definitions in files loaded later replace/enhance definitions loaded in previous files.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GuiDefsLoader implements RequestCallback, Command {
    private final List<String> toLoad = new ArrayList<>();

    private final Set<String> loaded = new HashSet<>();

    private int current = 0;

    private RequestBuilder rb;

    public GuiDefsLoader() {
        add("guidefs"); // $NON-NLS$
        if (!SessionData.isWithPmBackend()) {
            add("guidefs-" + getModuleName()); // $NON-NLS$
        }
        if(SessionData.isAsDesign()) {
            if (Permutation.AS.isActive()) {
                add("as-guidefs"); // $NON-NLS$
            }
            else {
                add("ice-guidefs"); // $NON-NLS$
            }
        }
    }

    private void add(String name) {
        if (name.endsWith(".json")) { // $NON-NLS$
            this.toLoad.add(name);
        }
        else {
            this.toLoad.add(name + "-" + I18n.I.locale() + ".json");  // $NON-NLS$
        }
    }

    public static String getModuleName() {
        final String url = GWT.getModuleBaseURL();  // e.g., http://server.com/foo/ : we need to extract foo
        final int p = url.lastIndexOf('/', url.length() - 2);
        return url.substring(p + 1, url.length() - 1);
    }

    public static String getHostUrl() {
        final String url = GWT.getModuleBaseURL();  // e.g., http://server.com/foo/ : we need to extract http://server.com/
        final int p = url.lastIndexOf('/', url.length() - 2);
        return url.substring(0, p + 1);
    }

    public void execute() {
        if (this.current == 0) {
            AbstractMainController.INSTANCE.updateProgress(I18n.I.loadGUI());
        }
        if (this.current == this.toLoad.size()) {
            AbstractMainController.INSTANCE.runInitSequence();
        }
        else {
            final String filename = this.toLoad.get(this.current++);
            if (this.loaded.contains(filename)) {
                // filename already loaded
                execute();
                return;
            }
            this.loaded.add(filename);
            this.rb = new RequestBuilder(RequestBuilder.GET, filename);
            this.rb.setHeader("Pragma", "no-cache"); // $NON-NLS$
            this.rb.setHeader("Cache-Control", "no-cache"); // $NON-NLS$
            try {
                this.rb.sendRequest(null, this);
            }
            catch (RequestException e) {
                onGuiDefsError(this.rb.getUrl(), e);
            }
        }
    }

    public void onResponseReceived(Request request, Response response) {
        final int sc = response.getStatusCode();
        if (sc == 200) {
            final JSONValue jsonValue = JSONParser.parseLenient(response.getText());
            SessionData.INSTANCE.setGuiDefs(jsonValue);
            final JSONValue jsonImport = jsonValue.isObject().get("guidefs-import"); // $NON-NLS$
            if (jsonImport != null) {
                addGuiDefs(jsonImport);
            }
            final JSONValue jsonImportTrigger = jsonValue.isObject().get("guidefs-import-trigger");  // $NON-NLS$
            final String triggerValue = jsonImportTrigger==null?null:jsonImportTrigger.isString().stringValue();
            if (triggerValue != null && !"manual".equals(triggerValue)) {  // $NON-NLS-0$
                GuiDefsUpdater.scheduleGuiDefUpdates(triggerValue);
            }
            execute();
        }
        else if (isClientError(sc) && SessionData.INSTANCE.getGuiDefs() != null) {
            Firebug.log("not found: " + this.rb.getUrl()); // $NON-NLS-0$
            execute();
        }
        else {
            onGuiDefsError(sc + "\n" + response.getHeadersAsString(), null); // $NON-NLS-0$
        }
    }

    private boolean isClientError(int sc) {
        // limiting this to 404 (not found) may sometimes not be sufficient.
        // in R-71530 the user received 444 although the server returned 404, probably because
        // an nginx proxy between client and server changed the status code.
        return sc >= 400 && sc < 500;
    }

    private void addGuiDefs(JSONValue jsonImport) {
        final JSONArray jsonArray = jsonImport.isArray();
        if (jsonArray != null) {
            for (int i = 0; i < jsonArray.size(); i++) {
                addGuiDefs(jsonArray.get(i).isObject());
            }
            return;
        }
        final JSONObject jsonObject = jsonImport.isObject();
        if (jsonObject != null) {
            addGuiDefs(jsonObject);
        }
    }

    private void addGuiDefs(JSONObject jsonImport) {
        final JSONValue jSelector = jsonImport.get("selector"); // $NON-NLS-0$
        if (jSelector != null) {
            final Selector selector = Selector.valueOf(jSelector.isString().stringValue());
            if (!selector.isAllowed()) {
                return;
            }
        }
        final JSONValue jFilename = jsonImport.get("filename"); // $NON-NLS-0$
        assert (jFilename != null);
        add(jFilename.isString().stringValue());
    }

    public void onError(Request request, Throwable t) {
        onGuiDefsError("onError: " + t.getMessage(), t); // $NON-NLS-0$
        execute();
    }

    private void onGuiDefsError(String s, Throwable t) {
        Firebug.error(s, t);
        final String message = I18n.I.fatalGUINotLoaded() + this.rb.getUrl() + "\n" + s;  // $NON-NLS-0$
        DebugUtil.logToServer(message);
        Window.alert(message);
    }
    //TODO: migrate to Customer-Object
    public static String getIssuerName() {
        return getValueByKey("producthighlighting_issuername", "key", "issuername", "value"); // $NON-NLS$
    }

    //TODO: migrate to Customer-Object
    public static String getIssuerDisplayName() {
        return getValueByKey("producthighlighting_issuername", "key", "issuernameDisplay", "value"); // $NON-NLS$
    }

    //TODO: delete
    private static String getValueByKey(String topEntry, String keyField, String keyValue, String valueField) {
        final JSONWrapper jsonConfig = SessionData.INSTANCE.getGuiDef(topEntry);
        if (!jsonConfig.isValid()) {
            return null;
        }
        final JSONArray array = jsonConfig.getValue().isArray();
        for (int i = 0; i < array.size(); i++) {
            final JSONObject def = array.get(i).isObject();
            if (def.containsKey("ifFeatureFlag")) { // $NON-NLS$
                final String ifFeatureFlag = def.get("ifFeatureFlag").isString().stringValue(); // $NON-NLS$

                //noinspection deprecation
                if (!FeatureFlags.isEnabled(ifFeatureFlag)) {
                    continue;
                }
            }
            //noinspection Duplicates
            if (def.containsKey("ifSelector")) { // $NON-NLS$
                final String ifSelector = def.get("ifSelector").isString().stringValue(); // $NON-NLS$

                if (ifSelector.startsWith("!")) {  // $NON-NLS$
                    if (Selector.valueOf(ifSelector.substring(1)).isAllowed()) {
                        continue;
                    }
                }
                else {
                    if (!Selector.valueOf(ifSelector).isAllowed()) {
                        continue;
                    }
                }
            }
            final String key = def.get(keyField).isString().stringValue();
            if (keyValue.equals(key)) {
                return def.get(valueField).isString().stringValue();
            }
        }
        return null;
    }

    public static List<String> getPortfolioCurrencies() {
        final List<String> currencies = new ArrayList<>();
        final JSONWrapper guiDef = SessionData.INSTANCE.getGuiDef("portfolio_currencies"); // $NON-NLS$
        if (!guiDef.isValid()) {
            throw new IllegalStateException("guidefs: array 'portfolio_currencies' expected!"); // $NON-NLS$
        }
        final JSONArray array = guiDef.getValue().isArray();
        for (int i = 0; i < array.size(); i++) {
            currencies.add(array.get(i).isString().stringValue());
        }
        return currencies;
    }

    @SuppressWarnings("UnusedDeclaration")
    public static void useI18nKeysForGuidefs() {
        // these i18n keys are used in guidefs
        // they are listed here to prevent idea from marking them as unused
        Window.alert(I18n.I.commBasketIndicesOf());
        Window.alert(I18n.I.commIndicesOf());
        Window.alert(I18n.I.energy());
        Window.alert(I18n.I.futuresEurex());
        Window.alert(I18n.I.futuresUS());
        Window.alert(I18n.I.metals());
        Window.alert(I18n.I.misc());
        Window.alert(I18n.I.preciousMetals());
        Window.alert(I18n.I.softCommodities());
        Window.alert(I18n.I.stockIndices());
    }
}