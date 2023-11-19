/*
 * PrintWindow.java
 *
 * Created on 24.10.2008 18:05:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * @author Ulrich Maurer
 */
public class PrintWindow {

    public static void print(String html, String[] additionalStyleSheets) {
        print(null, html, additionalStyleSheets);
    }

    public static void print(Widget printView, String[] additionalStyleSheets) {
        print(printView, null, additionalStyleSheets);
    }

    private static void print(Widget printView, String html, String[] additionalStyleSheets) {
        final String timestamp = JsDateFormatter.formatDdmmyyyyHhmmss(new MmJsDate());
        final JavaScriptObject windowHandle = createWindow(timestamp);
        final BodyElement bodyElement = getBodyElement(windowHandle);
        bodyElement.addClassName("mm-printview"); // $NON-NLS$

        if(printView != null) {
            final HTMLPanel bodyPanel = HTMLPanel.wrap(bodyElement);
            bodyPanel.add(printView);
        }
        else {
            bodyElement.setInnerHTML(html);
        }

        DOMUtil.loadStylesheet(bodyElement.getOwnerDocument(), "gxt/css/gxt-all.css"); // $NON-NLS-0$
        DOMUtil.loadStylesheet(bodyElement.getOwnerDocument(), "gwtutil.css"); // $NON-NLS-0$
        if(SessionData.isAsDesign()) {
            DOMUtil.loadStylesheet(bodyElement.getOwnerDocument(), "as/mmgwt.css"); // $NON-NLS-0$
            bodyElement.addClassName("asView"); //$NON-NLS$
        }
        else {
            DOMUtil.loadStylesheet(bodyElement.getOwnerDocument(), "mmgwt.css"); // $NON-NLS-0$
        }
        DOMUtil.loadStylesheet(bodyElement.getOwnerDocument(), "mmgwt-print.css"); // $NON-NLS-0$
        if (additionalStyleSheets != null) {
            for (String additionalStyleSheet : additionalStyleSheets) {
                DOMUtil.loadStylesheet(bodyElement.getOwnerDocument(), additionalStyleSheet);
            }
        }

        final boolean defaultValue = SessionData.isAsDesign();
        final AppConfig appConfig = SessionData.INSTANCE.getUser().getAppConfig();
        if(appConfig.getBooleanProperty(AppConfig.TRIGGER_BROWSER_PRINT_DIALOG, defaultValue)) {
            // Scheduler ist used to give Chrome (at time of the fix 47.0.2526.73 m) and
            // Firefox (42.0) some time to render the scripted document content before the print
            // dialog is called. This ensures that Chromes print preview shows the properly
            // styled and rendered content of the print window. Additionally, it enables FF to stop
            // the animation of its loading icon in the tab's caption.
            // Remark: The delay of scheduleDeferred is not sufficient to let Chrome render
            // the chart center image. Cf. MMWEB-653.
            Scheduler.get().scheduleFixedDelay(() -> { triggerPrintDialog(windowHandle); return false;}, 250);
        }
    }

    /**
     * In IE >= 7 the menubar isn't shown anymore by default. If it is shown or not depends on the users preference.
     * Developers cannot force IE to show the menubar via 'menubar=yes'. Therefore some users aren't able to print
     * (possibly they don't know anything about STRG-P or the existence of the context menu).
     * According to http://social.msdn.microsoft.com/Forums/ie/en-US/e1a3ad88-d352-4f16-9afc-e4ea84e6069b/ie7-menubaryes
     * the only way to show a 'findable' print menu item is to enable the so called command bar, which can be only
     * enabled by enabling the toolbar.
     * Therefore, the toolbar is enabled in this window's feature set.
     * This behaviour applies to all IEs >= IE 7 and was tested with 8, 9, and 10.
     */
    private static native JavaScriptObject createWindow(String timestamp) /*-{
        var win = $wnd.open("", "printwindow", "dependent=yes,width=880,height=750,location=no,toolbar=yes,menubar=yes,resizable=yes,scrollbars=yes,status=no"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        var doc = win.document;
        doc.open();
        doc.write("<html><head>" + // $NON-NLS-0$
                "<title>" + timestamp + "</title>" + // $NON-NLS$
//                  "<script type='text/javascript' src='http://getfirebug.com/releases/lite/1.2/firebug-lite-compressed.js'></script>" +
                "</head><body></body></html>"); // $NON-NLS-0$
        doc.close();
        return win;
    }-*/;

    private static native BodyElement getBodyElement(JavaScriptObject windowHandle) /*-{
        return windowHandle.document.getElementsByTagName("body")[0]; // $NON-NLS-0$
    }-*/;

    private static native void triggerPrintDialog(JavaScriptObject windowHandle) /*-{
        windowHandle.print();
    }-*/;
}
