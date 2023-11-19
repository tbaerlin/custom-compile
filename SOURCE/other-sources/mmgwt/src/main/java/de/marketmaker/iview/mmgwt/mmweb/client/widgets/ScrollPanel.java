package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: umaurer
 * Date: 08.10.13
 * Time: 15:33
 */
public class ScrollPanel extends com.google.gwt.user.client.ui.ScrollPanel {
    public ScrollPanel(Widget child) {
        super(child);
    }

    public void scrollToTop(UIObject item) {
        Element scroll = getScrollableElement();
        Element element = item.getElement();
        scrollToTopImpl(scroll, element);
    }

    private native void scrollToTopImpl(Element scroll, Element e) /*-{
        if (!e)
            return;

        var item = e;
        var realOffset = 0;
        while (item && (item != scroll)) {
            realOffset += item.offsetTop;
            item = item.offsetParent;
        }

        scroll.scrollTop = realOffset;
    }-*/;

}
