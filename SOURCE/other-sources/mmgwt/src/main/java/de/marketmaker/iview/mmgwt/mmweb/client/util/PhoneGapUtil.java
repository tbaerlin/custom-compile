/*
 * PhoneGapUtil.java
 *
 * Created on 18.03.2014
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author  jseubert
 */
@NonNLS
public class PhoneGapUtil {

    private static final String DIALOG_TITLE = "Infront Advisory Solution";

    private static boolean isPhoneGapCache;
    private static boolean isPhoneGapCacheInitialized = false;
    private static boolean isInd2uceEnabledCache;
    private static boolean isInd2uceEnabledCacheInitialized = false;

    static {
        initializeEventInterface();
    }

    private static native void initializeEventInterface() /*-{
        $wnd.phonegapEventSend = @de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil::onPhoneGapEvent(Ljava/lang/String;);
    }-*/;

    public static void onPhoneGapEvent(String event) {
        log("onPhoneGapEvent: " + event);

        String eventParts[] = event.split(":");
        String eventId = eventParts[0];
        String eventBody = eventParts[1];

        //todo: handle events here
        PhoneGapUtil.showToast(event);
    }

    // interface definitions
    public interface Callback<T> {
        void onResult(T result);
    }

    // determines if code is running inside a phonegap app
    private static native boolean isPhoneGapNative() /*-{
        if($wnd.isPhoneGap != undefined) {
            return $wnd.isPhoneGap();
        }
        return false;
    }-*/;

    public static boolean isPhoneGap() {
        if (!isPhoneGapCacheInitialized) {
            isPhoneGapCache = isPhoneGapInternal();
            isPhoneGapCacheInitialized = true;
        }

        return isPhoneGapCache;
    }

    private static boolean isPhoneGapInternal() {
        if (AbstractMainController.INSTANCE != null && SessionData.isAsDesign()) {
            return isPhoneGapNative();
        } else {
            return false;
        }
    }

// todo: JS - implement
//    public static String getPdfViewerUrl(String pdfUrl) {
//        String pdfViewerUrl = UrlBuilder.getServerPrefix(true) + "/pdfjs/web/viewer.html";
//        pdfUrl = com.google.gwt.http.client.URL.encodeQueryString(pdfUrl);
//        return pdfViewerUrl + "?file=" + pdfUrl;
//    }

    public static native void openPdfExternal(String pdfUrl) /*-{
        if($wnd.openPdfExternal != undefined) {
            $wnd.openPdfExternal(pdfUrl);
        }
    }-*/;

    public static void showToast(String message) {
        if (isPhoneGap()) {
            showToastNative(message);
        } else {
            AbstractMainController.INSTANCE.showMessage(message);
        }
    }

    private static native void showToastNative(String message) /*-{
        $wnd.plugins.toast.showShortCenter(message);
    }-*/;

    public static native void log(String message) /*-{
        console.log(message);
    }-*/;

    public static void showAlert(String message) {
        if (isPhoneGap()) {
            showAlertNative(message, DIALOG_TITLE, I18n.I.ok());
        } else {
            com.google.gwt.user.client.Window.alert(message);
        }
    }

    private static native void showAlertNative(String message, String title, String buttonText) /*-{
        $wnd.navigator.notification.alert(message, function(){}, title , buttonText);
    }-*/;

    public static String showPrompt(String message, Callback callback) {
        if (isPhoneGap()) {
            String[] buttonLabels = {I18n.I.ok(), I18n.I.cancel()};

            return showPromptNative(message, DIALOG_TITLE, buttonLabels, callback);
        } else {
            String result = com.google.gwt.user.client.Window.prompt(message,"");
            callback.onResult(result);
            return result;
        }
    }

    private static native String showPromptNative(String message, String title, String[] buttonLabels, Callback<String> callback) /*-{
        var callbackFunc = function(results) {
            var str;

            if(results.buttonIndex == 1)
            {
                str = results.input1;
            }
            if(results.buttonIndex == 2)
            {
                str = "";
            }

            callback.@de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil.Callback::onResult(Ljava/lang/Object;)(str);
        };
        return $wnd.navigator.notification.prompt(message, $entry(callbackFunc), title , buttonLabels);
    }-*/;

    public static String showPinDialog(String message, Callback callback) {
        if (isPhoneGap()) {
            String[] buttonLabels = {I18n.I.ok(), I18n.I.cancel()};

            return showPinDialogNative(message, DIALOG_TITLE, buttonLabels, callback);
        } else {
            String result = com.google.gwt.user.client.Window.prompt(message,"");
            callback.onResult(result);
            return result;
        }
    }

    private static native String showPinDialogNative(String message, String title, String[] buttonLabels, Callback<String> callback) /*-{
        var callbackFunc = function(results) {
            var str;

            if(results.buttonIndex == 1)
            {
                str = results.input1;
            }
            if(results.buttonIndex == 2)
            {
                str = "";
            }

            callback.@de.marketmaker.iview.mmgwt.mmweb.client.util.PhoneGapUtil.Callback::onResult(Ljava/lang/Object;)(str);
        };
        return $wnd.plugins.pinDialog.prompt(message, $entry(callbackFunc), title, buttonLabels);
    }-*/;

    public static void exitApp() {
        exitAppNative();
    }

    private static native void exitAppNative() /*-{
        $wnd.navigator.app.exitApp();
    }-*/;

}
