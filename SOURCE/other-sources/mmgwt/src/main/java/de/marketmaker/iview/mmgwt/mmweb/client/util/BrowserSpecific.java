package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BodyElement;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.TextBoxBase;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author umaurer
 */
public class BrowserSpecific {
    public static final BrowserSpecific INSTANCE = createInstance();
    private static Boolean svgSupported = null;

    public static native String getUserAgent() /*-{
        return navigator.userAgent.toLowerCase();
    }-*/;

    private static BrowserSpecific createInstance() {
        final String userAgent = getUserAgent();
        if (userAgent.contains("msie 6")) { // $NON-NLS$
            return new BrowserSpecificIE6();
        }
        else if (userAgent.contains("msie 7")) { // $NON-NLS$
            return new BrowserSpecificIE7();
        }
        else if (userAgent.contains("msie 8")) { // $NON-NLS$
            return new BrowserSpecificIE8();
        }
        else if (userAgent.contains("msie 9") || userAgent.contains("trident/5")) { // $NON-NLS$
            return new BrowserSpecificIE9();
        }
        else if (userAgent.contains("trident/6") || userAgent.contains("msie 10")) { // $NON-NLS$
            return new BrowserSpecificIE10();
        }
        else if (userAgent.contains("trident/7")) { // $NON-NLS$  //IE11 does not identify itself as MSIE
            return new BrowserSpecificIE11();
        }
        else if (userAgent.contains("msie")) { // $NON-NLS$
            return new BrowserSpecificIE();
        }
        else if (userAgent.contains("rv:1.8") && userAgent.contains("gecko")) { // $NON-NLS$
            return new BrowserSpecificGecko18();
        }
        else if (userAgent.contains("gecko")) { //$NON-NLS$
            return new BrowserSpecificGecko();
        }

        return new BrowserSpecific();
    }

    /**
     * Special implementation in BrowserSpecificIE6: IE6 cannot handle tr:hover.
     * @param html HTML component, containing a table
     * @param rowClass Only rows with this className are adapted.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    public void applyRowAction(HTML html, String rowClass) {
        // Implementation in BrowserSpecificIE6
    }

    /**
     * Special implementation in BrowserSpecificIE6/7: Fix for an IE6/7 problem of losing focus.
     * @param component e.g. TextField
     */
    public void focus(final Component component) {
        // Different implementation in BrowserSpecificIE6
        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                component.focus();
            }
        });
    }

    public void setFocus(Focusable focusable, boolean focus) {
        if(focusable == null) {
            return;
        }
        focusable.setFocus(focus);
    }

    /**
     * Special implementation in BrowserSpecificFF2: Cursor does not appear in TextField inside of Window.
     * @param window The Window.
     */
    public void fixCursorBug(com.extjs.gxt.ui.client.widget.Window window) {
        // Implementation in BrowserSpecificGecko18
    }


    /**
     * Special implementation in BrowserSpecificIE6: set fixed width style
     * @param widget The widget which should have a special IE6 style
     * @param style The style which should be set
     * @param additionalStyle Will be appended after the browser specific style
     */
    public void setStyle(Widget widget, String style, String additionalStyle) {
        final String fullStyle = additionalStyle == null ? style : (style + " " + additionalStyle);
        widget.setStyleName(fullStyle);
    }

    public void setPriceTeaserMarket(final Button button, final String text) {
        button.setText(text);
    }

    public String getBodyStyles() {
        return "bs-hover"; // $NON-NLS$
    }

    public static boolean isSvgSupported() {
        if (svgSupported == null) {
            svgSupported = _isSvgSupported();
        }
        return svgSupported;
    }

    private static native boolean _isSvgSupported() /*-{
        return !!document.createElementNS && !!document.createElementNS('http://www.w3.org/2000/svg', "svg").createSVGRect;
    }-*/;

    public boolean isVmlSupported() {
        return false;
    }

    public static void addBodyStyles() {
/*
        final Document doc = DOMUtil.getDocument(widget.getElement());
        final Element bodyElement = doc.getElementsByTagName("body").getItem(0); // $NON-NLS$
        bodyElement.addClassName(BrowserSpecific.INSTANCE.getBodyStyles());
*/
        final BodyElement body = Document.get().getBody();
        body.addClassName(BrowserSpecific.INSTANCE.getBodyStyles());
        body.addClassName("locale-" + I18n.I.locale()); // $NON-NLS$
        if (isSvgSupported()) {
            body.addClassName("svgSupported"); // $NON-NLS$
        }
    }

    public void setCardContentHideMode(Component component) {
        component.setHideMode(com.extjs.gxt.ui.client.Style.HideMode.VISIBILITY);
    }

    public void forceLayout(DockLayoutPanel layoutPanel) {
        // Implementation only for IE6 and IE7
    }

    public void clearWidthBeforeRecalculation(com.google.gwt.dom.client.Style style) {
        style.clearWidth();
    }

    public boolean isToolbarFillSupported() {
        return true;
    }

    public void fixIe7FloatingToolbar(ResizeLayoutPanel toolbarPanel, FlowPanel panelOuter) {
        // Implementation only for IE7
    }

    /**
     * Calling this method is necessary when setting a read-only TextBox to editable after it got the focus.
     * This is because IE does not focus an element that is read-only as all other browser do.
     *
     * @see BrowserSpecificIE#setReadOnly(com.google.gwt.user.client.ui.TextBoxBase, boolean)
     */
    public void setReadOnly(TextBoxBase textBoxBase, boolean readonly) {
        textBoxBase.setReadOnly(readonly);
    }

    public boolean isUriTooLong(String uri) {
        return false;
    }

    public int getScrollTopMax(Element element) {
        final int scrollHeight = element.getScrollHeight();
        final int clientHeight = element.getClientHeight();
        final int scrollTopMax;

        if(scrollHeight >= clientHeight) {
            scrollTopMax = scrollHeight - clientHeight;
        }
        else {
            scrollTopMax = 0;
        }
        Firebug.log("Calculating scrollTopMax for gradient bottom style: " + scrollTopMax);

        return scrollTopMax;
    }

    /**
     * Remark: Part 1 of the fix is the PopupPanelFix that places an iframe behind each pop up to solve
     * layering in IE7-IE9. Unfortunately, for IE10 and IE11 a re-flow is required otherwise the popup
     * panel fix won't work.
     */
    public void fixDivBehindPdfObjectBugPart2(HTML divContainingTheObjectTag) {
        /* nothing to do here */
    }

    public void initialize(Command finished) {
        finished.execute();
    }

    public boolean isNeedsHistoryTokenDecoding() {
        return false;
    }

    public static class BrowserSpecificLoader implements Command {
        @Override
        public void execute() {
            BrowserSpecific.INSTANCE.initialize(new Command() {
                @Override
                public void execute() {
                    AbstractMainController.INSTANCE.runInitSequence();
                }
            });
        }
    }
}
