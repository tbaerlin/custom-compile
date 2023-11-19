/*
 * DOMUtil.java
 *
 * Created on 08.04.2008 16:18:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Node;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.UrlBuilder;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DOMUtil {
    private static class HistoryLinkEventListener implements EventListener {
        private final String historyToken;

        private HistoryLinkEventListener(String historyToken) {
            this.historyToken = historyToken;
        }

        public void onBrowserEvent(Event event) {
            if (DOM.eventGetType(event) == Event.ONCLICK) {
                PlaceUtil.goTo(this.historyToken);
                event.preventDefault();
            }
        }
    }

    public interface LinkListenerFactory {
        EventListener createListener(String linkToken);
    }

    public static final LinkListenerFactory DEFAULT_LINK_LISTENER_FACTORY = new LinkListenerFactory() {
        public EventListener createListener(String linkToken) {
            return new HistoryLinkEventListener(linkToken);
        }
    };

    public static native Element getElementFromList(int index, JavaScriptObject list) /*-{
       return list.item(index) || null;
  }-*/;

    public static native JavaScriptObject getElementsByTagNameImpl(final Node node, String tagName) /*-{
    return node.getElementsByTagName(tagName);
  }-*/;

    public static native int getLength(JavaScriptObject list) /*-{
       return list.length;
  }-*/;

    /**
     * For an element with embedded a-tag elements, replace the default click action
     * for those child elements. The new action will call {@link PlaceUtil#goTo(String)} with
     * the text after '#' in the original link passed as parameter.<p>
     * This method has to be called for all HTML content that contains links (a-tags), because
     * even if a link such as <tt>href="#FOO"</tt> is used, IE7 will reload the application // $NON-NLS-0$
     * when such a link will be clicked. After laundering the links, everything should work
     * as expected.
     *
     * @param node contains a-tag children to be laundered
     */
    public static void launderLinks(Node node) {
        // TODO: handle links just as in addEventListener, is that possible? how?
        final JavaScriptObject list = getElementsByTagNameImpl(node, "a"); // $NON-NLS-0$
        final int n = getLength(list);
//        DebugUtil.logToFirebugConsole("<launderLinks> length:" + n);
        for (int i = 0; i < n; i++) {
            final Element anchor = getElementFromList(i, list);
//            DebugUtil.logToFirebugConsole("<launderLinks> element:" + anchor);
            launderFrameLinkForHistory(anchor);
        }
    }

    public static native void launderFrameLinkForHistory(final Element anchor) /*-{
        if (anchor.hash) {
            anchor.onclick = function() {
                var place = anchor.hash.replace(/#(.*)/, "$1"); // $NON-NLS$
                @de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil::goTo(Ljava/lang/String;)(place);
                return false;
            }
        }
    }-*/;

    public static String getAbsoluteUri(String relativeUri) {
        final UrlBuilder urlBuilder = new UrlBuilder();
        urlBuilder.setProtocol(Window.Location.getProtocol());
        urlBuilder.setHost(Window.Location.getHost());
        String path = Window.Location.getPath();
        if (path != null && path.length() > 0) {
            int pos = path.lastIndexOf('/');
            path = path.substring(0, pos + 1) + relativeUri;
            urlBuilder.setPath(path);
        }
        String port = Window.Location.getPort();
        if (port != null && port.length() > 0) {
            urlBuilder.setPort(Integer.parseInt(port));
        }
        return urlBuilder.buildString();
    }

    /**
     * Dynamically load specified stylesheet.
     *
     * @param name The filename of the stylesheet.
     */
    public static void loadStylesheet(String name) {
        loadStylesheet(Document.get(), name, false);
    }

    public static void loadStylesheet(Document doc, String name) {
        loadStylesheet(doc, name, false);
    }

    public static native void loadStylesheet(Document doc, String name, boolean insertBeforeStyles) /*-{
        var oLink = doc.createElement("link"); // $NON-NLS-0$
        oLink.href = name;
        oLink.rel = "stylesheet"; // $NON-NLS-0$
        oLink.type = "text/css"; // $NON-NLS-0$
        var elHead = doc.getElementsByTagName("head")[0]; // $NON-NLS-0$
        var appended = false;
        if (insertBeforeStyles) {
            var listStyles = elHead.getElementsByTagName("style"); // $NON-NLS-0$
            if (listStyles.length > 0) {
                elHead.insertBefore(oLink, listStyles[0]);
                appended = true;
            }
        }
        if (!appended) {
            elHead.appendChild(oLink);
        }
    }-*/;

    public static void loadStylesheet(final Frame frame, final String name,
                                      boolean insertBeforeStyles) {
        loadStylesheet(getDocument(frame), name, insertBeforeStyles);
    }

    public static boolean hasContentWindow(final Frame frame) {
        return hasContentWindow(frame.getElement());
    }

    private static native boolean hasContentWindow(final Element element) /*-{
        return element.contentWindow != null
    }-*/;

    public static void fillFrame(final Frame frame, final String html) {
        fillFrame(frame.getElement(), html);
    }

    private static native void fillFrame(final Element element, final String html) /*-{
        var doc = element.contentWindow.document;
        doc.open();
        doc.write(html);
        doc.close();
    }-*/;

    public static Document getDocument(final Frame frame) {
        return getDocument(frame.getElement());
    }

    public static native Document getDocument(final Element element) /*-{
        return element.contentWindow.document;
    }-*/;

    public static void setTableRowVisible(com.google.gwt.dom.client.Element trElement,
                                          boolean visible) {
        try {
            // "display: table-row" funktioniert nicht in IE
            trElement.getStyle().setProperty("display", visible ? "table-row" : "none"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        }
        catch (Exception e) {
            trElement.getStyle().setProperty("display", visible ? "" : "none"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        }
    }


    // this only checks for display=none in the styles to determine visibility
    public static boolean isTableRowVisible(com.google.gwt.dom.client.Element trElement) {
        String display = trElement.getStyle().getProperty("display");                 // $NON-NLS-0$
        return !(display != null && display.trim().equalsIgnoreCase("none"));         // $NON-NLS-0$
    }


    public static native int getRowId(final Element elt) /*-{
        if (elt.tagName == "BODY") { // $NON-NLS-0$
            return -1;
        }
        if (elt.tagName !=  "TR") { // $NON-NLS-0$
            return @de.marketmaker.iview.mmgwt.mmweb.client.util.DOMUtil::getRowId(Lcom/google/gwt/dom/client/Element;)(elt.parentNode);
        }

        var counter = 0;
        while (elt.previousSibling != null) {
            elt = elt.previousSibling;
            if (elt.tagName == "TR") { // $NON-NLS-0$
                counter++;
            }
        }
        return counter;
    }-*/;


    public static String getOrCreateId(Element elt) {
        String id = elt.getId();
        if (id == null || id.isEmpty()) {
            id = DOM.createUniqueId();
            elt.setId(id);
        }
        return id;
    }

    public static String getSubelementId(Widget widget, String tagName) {
        return getSubelementId(widget.getElement(), tagName);
    }

    public static String getSubelementId(Element elt, String tagName) {
        if (tagName.equals(elt.getTagName())) {
            return elt.getId();
        }
        final NodeList<com.google.gwt.dom.client.Element> list = elt.getElementsByTagName(tagName);
        if (list.getLength() == 0) {
            return null;
        }
        return list.getItem(0).getId();
    }

    public static native Element elementFromPoint(final int x, final int y) /*-{
        return $doc.elementFromPoint(x, y);
    }-*/;
}
