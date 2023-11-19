package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.user.client.ui.RootPanel;

/**
 * @author Ulrich Maurer
 *         Date: 21.07.11
 */
public class JsUtil {
    /* copied from $getRuntimeValue_0() in compiled code */
    public static native String getRuntimeValue() /*-{
        var ua = navigator.userAgent.toLowerCase();
        var makeVersion = function(result) {
            return parseInt(result[1]) * 1000 + parseInt(result[2]);
        }
                ;
        if (function() {
            return ua.indexOf('opera') != -1;
        }
                ())
            return 'opera';
        if (function() {
            return ua.indexOf('webkit') != -1;
        }
                ())
            return 'safari';
        if (function() {
            return ua.indexOf('msie') != -1 && $doc.documentMode >= 9;
        }
                ())
            return 'ie9';
        if (function() {
            return ua.indexOf('msie') != -1 && $doc.documentMode >= 8;
        }
                ())
            return 'ie8';
        if (function() {
            var result = /msie ([0-9]+)\.([0-9]+)/.exec(ua);
            if (result && result.length == 3)
                return makeVersion(result) >= 6000;
        }
                ())
            return 'ie6';
        if (function() {
            return ua.indexOf('gecko') != -1;
        }
                ())
            return 'gecko1_8';
        return 'unknown';
    }-*/;

    private static String getBodyPermutationStyle() {
        final String runtimeValue = getRuntimeValue();
        if (runtimeValue.startsWith("ie")) {
            return "mm-ie mm-" + runtimeValue;
        }
        if (runtimeValue.startsWith("gecko")) {
            return "mm-gecko";
        }
        return "mm-" + runtimeValue;
    }

    public static void addBodyPermutationStyle() {
        RootPanel.get().addStyleName(getBodyPermutationStyle());
    }

    public static native void startDebugger() /*-{
        debugger;
    }-*/;
}
