/*
 * DOMUtil.java
 *
 * Created on 08.04.2008 16:18:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.Node;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DOMUtil {
    private static int zIndexId = 1000;

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
        var oLink = doc.createElement("link");
        oLink.href = name;
        oLink.rel = "stylesheet";
        oLink.type = "text/css";
        var elHead = doc.getElementsByTagName("head")[0];
        var appended = false;
        if (insertBeforeStyles) {
            var listStyles = elHead.getElementsByTagName("style");
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
            trElement.getStyle().setProperty("display", visible ? "table-row" : "none");
        }
        catch (Exception e) {
            trElement.getStyle().setProperty("display", visible ? "" : "none");
        }
    }


    public static native int getRowId(final Element elt) /*-{
        if (elt.tagName == "BODY") {
            return -1;
        }
        if (elt.tagName !=  "TR") {
            return @de.marketmaker.itools.gwtutil.client.util.DOMUtil::getRowId(Lcom/google/gwt/dom/client/Element;)(elt.parentNode);
        }

        var counter = 0;
        while (elt.previousSibling != null) {
            elt = elt.previousSibling;
            if (elt.tagName == "TR") {
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

    public static void forwardMouseWheelEvents(final PopupPanel popupPanel) {
        popupPanel.addDomHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(final MouseWheelEvent event) {
                popupPanel.hide();

                // forward mouse wheel events to underlying FloatingPanel
                final Element element = DOMUtil.elementFromPoint(event.getClientX(), event.getClientY());
                if (event.getNativeEvent() != null) {
                    final NativeEvent nativeEvent = event.getNativeEvent();
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            if (isAbleToDispatch(element)) {
                                element.dispatchEvent(nativeEvent);
                            } else {
                                ServerLogger.log("Found element without dispatchEvent method: "  + element.toString());
                            }
                        }
                    });
                }
            }
        }, MouseWheelEvent.getType());
    }

    public static native boolean isAbleToDispatch(Element e) /*-{
        return typeof e.dispatchEvent === 'function';
    }-*/;

    public static native Element elementFromPoint(final int x, final int y) /*-{
        return $doc.elementFromPoint(x, y);
    }-*/;

    public static int getTopZIndex() {
        return ++zIndexId;
    }

    public static int getTopZIndex(int i) {
        zIndexId += i + 1;
        return zIndexId;
    }

    public static void setTopZIndex(Widget widget) {
        widget.getElement().getStyle().setZIndex(getTopZIndex());
    }

    public static boolean isOrHasDescendant(final Node ancestor, final Node descendant) {
        return descendant != null
                && (ancestor.isOrHasChild(descendant) || isOrHasDescendant(ancestor, descendant.getParentNode()));
    }
}
