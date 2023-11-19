package de.marketmaker.itools.gwtutil.client.widgets.highcharts;

import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.ScriptInjector;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONString;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Initialize Highchart/Highstock API by loading highcharts js files and executing Callbacks afterwards.
 * <p>
 * Author: umaurer
 * Created: 31.03.15
 */
public class Highcharts {
    public static final String URL_FRAMEWORK =  "highcharts-4.0.4-standalone-framework.cache.js";
    public static final String URL_HIGHSTOCK =  "highstock-2.0.4.cache.js";
    public static final String URL_MORE =       "highcharts-4.0.4-more.cache.js";
    public static final String URL_SOLIDGAUGE = "highcharts-4.0.4-solid-gauge.cache.js";

    public enum State {
        INIT, LOAD, ERROR
    }

    public enum Type {
        DEFAULT(URL_FRAMEWORK, URL_HIGHSTOCK),
        SOLIDGAUGE(URL_FRAMEWORK, URL_HIGHSTOCK, URL_MORE, URL_SOLIDGAUGE);

        final String[] urls;

        Type(String... urls) {
            this.urls = urls;
        }

        public String[] getUrls() {
            return urls;
        }
    }

    private static State state = State.INIT;
    private static Set<String> loaded = new HashSet<>();
    private static LinkedHashSet<String> toLoad = new LinkedHashSet<>(); // LinkedHashSet -> keep order of elements

    private static final List<Callback<Void, Exception>> listCallbacks = new ArrayList<>();

    public static void initialize(Type type, Callback<Void, Exception> callback) {
        switch (state) {
            case INIT:
                if (addUrls(type.getUrls())) {
                    state = State.LOAD;
                    listCallbacks.add(callback);
                    loadNext();
                }
                else {
                    callback.onSuccess(null);
                }
                break;
            case LOAD:
                addUrls(type.getUrls());
                listCallbacks.add(callback);
                break;
            case ERROR:
                callback.onFailure(new Exception("Chart API was not initialized - see previous errors."));
                break;
        }
    }

    private static boolean addUrls(String... urls) {
        boolean added = false;
        for (String url : urls) {
            if (loaded.contains(url)) {
                continue;
            }
            if (toLoad.contains(url)) {
                continue;
            }
            toLoad.add(url);
            added = true;
        }
        return added;
    }

    private static void _onFailure(String url, Exception reason) {
        Firebug.error("initialization api failed: " + url, reason);
        if (state == State.ERROR) {
            return;
        }
        state = State.ERROR;
        for (Callback<Void, Exception> callback : listCallbacks) {
            callback.onFailure(reason);
        }
    }

    private static void loadNext() {
        final String url = toLoad.iterator().next();
        ScriptInjector.fromUrl(url)
                .setCallback(new Callback<Void, Exception>() {
                    @Override
                    public void onFailure(Exception reason) {
                        _onFailure(url, reason);
                    }

                    @Override
                    public void onSuccess(Void result) {
                        _onSuccess(url);
                    }
                })
                .setWindow(ScriptInjector.TOP_WINDOW)
                .inject();

    }

    private static void _onSuccess(final String url) {
        toLoad.remove(url);
        loaded.add(url);
        if (state == State.LOAD) {
            if (toLoad.isEmpty()) {
                state = State.INIT;
                initializeLanguage();
                for (Callback<Void, Exception> callback : new ArrayList<>(listCallbacks)) {
                    listCallbacks.remove(callback);
                    callback.onSuccess(null);
                }
            }
            else {
                loadNext();
            }
        }
        else {
            Firebug.error("Highchart._onSuccess(" + url + ") - unexpected state: " + state);
        }
    }

    private static JSONArray createArray(String... values) {
        final JSONArray array = new JSONArray();
        for (int i = 0; i < values.length; i++) {
            array.set(i, new JSONString(values[i]));
        }
        return array;
    }

    private static boolean langInitialized = false;
    private static void initializeLanguage() {
        if (langInitialized) {
            return;
        }
        setGlobalOptions(
                createArray(JsDateFormatter.MONTH_NAMES).getJavaScriptObject(),
                createArray(JsDateFormatter.MONTH_NAMES_SHORT).getJavaScriptObject()
        );
        langInitialized = true;
    }

    private static native void setGlobalOptions(final JavaScriptObject _months, final JavaScriptObject _shortMonths) /*-{
        $wnd.Highcharts.setOptions({
            lang: {
                months: _months,
                shortMonths: _shortMonths
            }
        });
    }-*/;
}
