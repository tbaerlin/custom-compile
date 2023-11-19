package de.marketmaker.itools.gwtutil.client.widgets.datepicker;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * @author umaurer
 */
public class PopupPositionCallback implements PopupPanel.PositionCallback {
    final Element element;
    final PopupPanel popupPanel;

    public PopupPositionCallback(Widget widget, PopupPanel popupPanel) {
        this(widget.getElement(), popupPanel);
    }

    public PopupPositionCallback(Element element, PopupPanel popupPanel) {
        this.element = element;
        this.popupPanel = popupPanel;
    }

    public void setPosition(int offsetWidth, int offsetHeight) {
        final int windowWidth = Window.getClientWidth();
        final int windowHeight = Window.getClientHeight();

        final int widgetLeft = this.element.getAbsoluteLeft();
        final int widgetTop = this.element.getAbsoluteTop();

        int left = widgetLeft;
        int top = widgetTop + this.element.getOffsetHeight();

        if (left + offsetWidth > windowWidth) {
            int testLeft = widgetLeft + this.element.getOffsetWidth() - offsetWidth;
            if (testLeft > 0) {
                left = testLeft;
            }
        }

        if (top + offsetHeight > windowHeight) {
            int testTop = widgetTop - offsetHeight;
            if (testTop > 0) {
                top = testTop;
            }
        }

        this.popupPanel.setPopupPosition(left, top);
    }
}
