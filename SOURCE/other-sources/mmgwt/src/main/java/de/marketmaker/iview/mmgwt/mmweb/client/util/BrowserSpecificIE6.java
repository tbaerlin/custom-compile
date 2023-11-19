package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author umaurer
 */
public class BrowserSpecificIE6 extends BrowserSpecificIE {
    private static final com.google.gwt.user.client.EventListener ROW_HOVER_MOUSE_LISTENER = new com.google.gwt.user.client.EventListener() {
            public void onBrowserEvent(Event event) {
                if (DOM.eventGetType(event) == Event.ONMOUSEOVER) {
                    modifyHoverEffect(DOM.eventGetCurrentTarget(event), true);
                }
                else if (DOM.eventGetType(event) == Event.ONMOUSEOUT) {
                    modifyHoverEffect(DOM.eventGetCurrentTarget(event), false);
                }
            }
        };

    private static void modifyHoverEffect(Element element, boolean add) {
        final String className = element.getClassName();
        if (className.contains("mm-hover")) { // $NON-NLS-0$
            if (!add) {
                element.setClassName(className.replaceAll(" *mm-hover *", " ")); // $NON-NLS-0$ $NON-NLS-1$
            }
        }
        else {
            if (add) {
                element.setClassName(className + " mm-hover"); // $NON-NLS-0$
            }
        }
    }

    // removed overriding of applyRowAction by underline prefix
    public void _applyRowAction(HTML html, String rowClass) {
        final Element e = html.getElement();
        final JavaScriptObject list = DOMUtil.getElementsByTagNameImpl(e, "tr"); // $NON-NLS-0$
        final int n = DOMUtil.getLength(list);
        for (int i = 0; i < n; i++) {
            final Element tr = DOMUtil.getElementFromList(i, list);
            final String styleName = tr.getClassName();
            if (!styleName.contains(rowClass)) {
                continue;
            }
            DOM.sinkEvents(tr, Event.ONMOUSEOVER | Event.ONMOUSEOUT | DOM.getEventsSunk(tr));
            DOM.setEventListener(tr, ROW_HOVER_MOUSE_LISTENER);
        }
    }

    public void setStyle(Widget widget, String style, String additionalStyle) {
        final String fullStyle = additionalStyle == null ? (style + "-ie6") : (style + "-ie6 " + additionalStyle); // $NON-NLS-0$ $NON-NLS-1$
        widget.setStyleName(fullStyle);
    }

    @Override
    public String getBodyStyles() {
        return "bs-ie6"; // $NON-NLS-0$
    }

    @Override
    public void forceLayout(DockLayoutPanel layoutPanel) {
        layoutPanel.forceLayout();
    }

    @Override
    public void clearWidthBeforeRecalculation(com.google.gwt.dom.client.Style style) {
        style.setWidth(50000d, com.google.gwt.dom.client.Style.Unit.PX);
    }

    @Override
    public boolean isVmlSupported() {
        return true;
    }
}
