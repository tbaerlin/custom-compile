package de.marketmaker.itools.gwtutil.client.util;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Widget;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 17.07.14
 */
public class WidgetUtil {
    public static HandlerRegistration makeFocusable(Widget widget, KeyDownHandler handler) {
        makeFocusable(widget);
        return widget.addDomHandler(handler, KeyDownEvent.getType());
    }

    public static HandlerRegistration makeFocusable(final Widget widget, final FocusKeyHandler focusKeyHandler) {
        return makeFocusable(widget, focusKeyHandler, true);
    }

    public static HandlerRegistration makeFocusable(final Widget widget, final FocusKeyHandler focusKeyHandler, boolean captureFocus) {
        final Element element = widget.getElement();
        makeFocusable(element);
        return widget.addDomHandler(createFocusKeyDownHandler(captureFocus ? element : null, focusKeyHandler), KeyDownEvent.getType());
    }

    public static KeyDownHandler createFocusKeyDownHandler(final Element element, final FocusKeyHandler focusKeyHandler) {
        return new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                final int keyCode = event.getNativeKeyCode();
                boolean consumed = false;
                switch (keyCode) {
                    case KeyCodes.KEY_ENTER:
                    case KeyCodes.KEY_SPACE:
                        consumed = focusKeyHandler.onFocusKeyClick();
                        break;
                    case KeyCodes.KEY_ESCAPE:
                        consumed = focusKeyHandler.onFocusKeyEscape();
                        break;
                    case KeyCodes.KEY_HOME:
                        consumed = focusKeyHandler.onFocusKeyHome();
                        break;
                    case KeyCodes.KEY_PAGEUP:
                        consumed = focusKeyHandler.onFocusKeyPageUp();
                        break;
                    case KeyCodes.KEY_UP:
                        consumed = focusKeyHandler.onFocusKeyUp();
                        break;
                    case KeyCodes.KEY_DOWN:
                        consumed = focusKeyHandler.onFocusKeyDown();
                        break;
                    case KeyCodes.KEY_PAGEDOWN:
                        consumed = focusKeyHandler.onFocusKeyPageDown();
                        break;
                    case KeyCodes.KEY_END:
                        consumed = focusKeyHandler.onFocusKeyEnd();
                        break;
                    case KeyCodes.KEY_INSERT:
                    case 171:
                    case KeyCodes.KEY_NUM_PLUS:
                        consumed = focusKeyHandler.onFocusAdd();
                        break;
                    case KeyCodes.KEY_DELETE:
                    case 173: // MINUS
                    case KeyCodes.KEY_NUM_MINUS:
                        consumed = focusKeyHandler.onFocusDelete();
                        break;
                    case 186: // ö
                        consumed = focusKeyHandler.onFocusKey('ö');
                        break;
                    case 189: // ß
                        consumed = focusKeyHandler.onFocusKey('ß');
                        break;
                    case 219: // ü
                        consumed = focusKeyHandler.onFocusKey('ü');
                        break;
                    case 222: // ä
                        consumed = focusKeyHandler.onFocusKey('ä');
                        break;
                }
                if (!consumed) {
                    if (keyCode >= KeyCodes.KEY_A && keyCode <= KeyCodes.KEY_Z) {
                        consumed = focusKeyHandler.onFocusKey((char) ('a' + keyCode - KeyCodes.KEY_A));
                    }
                    else if (keyCode >= KeyCodes.KEY_ZERO && keyCode <= KeyCodes.KEY_NINE) {
                        consumed = focusKeyHandler.onFocusKey((char) ('0' + keyCode - KeyCodes.KEY_ZERO));
                    }
                    else if (keyCode >= KeyCodes.KEY_NUM_ZERO && keyCode <= KeyCodes.KEY_NUM_NINE) {
                        consumed = focusKeyHandler.onFocusKey((char) ('0' + keyCode - KeyCodes.KEY_NUM_ZERO));
                    }
                }
                if (consumed) {
                    if (element != null) {
                        element.focus();
                    }
                    event.preventDefault();
                }
            }
        };
    }

    public static void makeFocusable(Widget widget) {
        makeFocusable(widget.getElement());
    }

    public static void makeFocusable(Element element) {
        element.setTabIndex(0);
    }

    public static boolean hasFocus(Widget widget) {
        if(widget instanceof HasFocusableElement) {
            return hasFocus(((HasFocusableElement) widget).getFocusableElement());
        }
        return hasFocus(widget.getElement());
    }

    public static native boolean hasFocus(Element element) /*-{
        //doesn't work with old browsers. see https://developer.mozilla.org/en-US/docs/Web/API/document.activeElement?redirectlocale=en-US&redirectslug=DOM%2Fdocument.activeElement#Browser_compatibility
        return $doc.activeElement == element;
    }-*/;

    public static void click(Widget widget) {
        click(widget.getElement());
    }

    public static native void click(Element element) /*-{
        element.click();
    }-*/;

    public static void deferredSetFocus(final Widget widget) {
        if (widget instanceof Focusable) {
            Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                @Override
                public void execute() {
                    ((Focusable) widget).setFocus(true);
                }
            });
        }
        deferredSetFocus(widget.getElement());
    }

    public static void deferredSetFocus(final Element element) {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                element.focus();
            }
        });
    }

    private static class DragMouseHandlers implements MouseDownHandler, MouseMoveHandler, MouseUpHandler {
        private final Widget widgetDraggable;
        private Style styleDraggable;
        private final Element elementHandle;
        private boolean dragging = false;
        private int deltaX;
        private int deltaY;
        private int width;
        private int height;
        private int maxLeft;
        private int maxTop;

        public DragMouseHandlers(Widget widgetDraggable, Widget widgetHandle) {
            this.widgetDraggable = widgetDraggable;
            this.styleDraggable = this.widgetDraggable.getElement().getStyle();
            this.elementHandle  = widgetHandle.getElement();
        }

        @Override
        public void onMouseDown(MouseDownEvent e) {
            this.width = widgetDraggable.getOffsetWidth();
            this.height = widgetDraggable.getOffsetHeight();
            this.maxLeft = Window.getClientWidth() - this.width;
            this.maxTop = Window.getClientHeight() - this.height;
            this.deltaX = e.getScreenX() - this.widgetDraggable.getAbsoluteLeft();
            this.deltaY = e.getScreenY() - this.widgetDraggable.getAbsoluteTop();
            this.dragging = true;
            e.preventDefault();
            DOM.setCapture(this.elementHandle);
            this.styleDraggable.setOpacity(0.7);
        }

        @Override
        public void onMouseMove(MouseMoveEvent e) {
            if (!this.dragging) {
                return;
            }
            setPosition(e.getScreenX() - this.deltaX, e.getScreenY() - this.deltaY);
            e.preventDefault();
        }

        @Override
        public void onMouseUp(MouseUpEvent e) {
            if (!this.dragging) {
                return;
            }
            setPosition(e.getScreenX() - this.deltaX, e.getScreenY() - this.deltaY);
            this.dragging = false;
            e.preventDefault();
            DOM.releaseCapture(this.elementHandle);
            this.styleDraggable.clearOpacity();
        }

        private void setPosition(final int left, final int top) {
            // capture inside window client area
            this.styleDraggable.setLeft(left < 0
                    ? 0
                    : left > this.maxLeft ? this.maxLeft : left, PX);
            this.styleDraggable.setTop(top < 0
                    ? 0
                    : top > this.maxTop ? this.maxTop : top, PX);
        }
    }

    public static void makeDraggable(Widget widgetDraggable, Widget widgetHandle) {
/*
        if (!(widgetHandle instanceof HasAllMouseHandlers)) {
            throw new IllegalArgumentException("cannot make widget draggable: " + widgetHandle.getClass().getSimpleName());
        }
*/

        final DragMouseHandlers dragMouseHandlers = new DragMouseHandlers(widgetDraggable, widgetHandle);
//        final HasAllMouseHandlers hasAllMouseHandlers = (HasAllMouseHandlers) widgetHandle;
        widgetHandle.addDomHandler(dragMouseHandlers, MouseDownEvent.getType());
        widgetHandle.addDomHandler(dragMouseHandlers, MouseMoveEvent.getType());
        widgetHandle.addDomHandler(dragMouseHandlers, MouseUpEvent.getType());
    }

}
