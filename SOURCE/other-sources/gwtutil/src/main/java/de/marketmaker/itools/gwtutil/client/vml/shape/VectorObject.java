package de.marketmaker.itools.gwtutil.client.vml.shape;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasDoubleClickHandlers;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Widget;

/**
 * User: umaurer
 * Date: 15.11.13
 * Time: 11:53
 */
public abstract class VectorObject extends Widget implements HasClickHandlers,
        HasAllMouseHandlers, HasDoubleClickHandlers {
    private Widget parent;

    protected VectorObject() {
        setElement(createElement());
    }

    public abstract Element createElement();

    public Widget getParent() {
        return parent;
    }

    /**
     * Sets this widget's parent.
     *
     * @param parent the widget's new parent
     * @throws IllegalStateException if <code>parent</code> is non-null and the
     *           widget already has a parent
     */
    public void setParent(Widget parent) {
        Widget oldParent = this.parent;
        if (parent == null) {
            try {
                if (oldParent != null && oldParent.isAttached()) {
                    onDetach();
                    assert !isAttached() : "Failure of " + this.getClass().getName()
                            + " to call super.onDetach()";
                }
            } finally {
                // Put this in a finally in case onDetach throws an exception.
                this.parent = null;
            }
        } else {
            if (oldParent != null) {
                throw new IllegalStateException(
                        "Cannot set a new parent without first clearing the old parent");
            }
            this.parent = parent;
            if (parent.isAttached()) {
                onAttach();
                assert isAttached() : "Failure of " + this.getClass().getName()
                        + " to call super.onAttach()";
            }
        }
    }

    @Override
    public void onAttach() {
        super.onAttach();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /*
         * (non-Javadoc)
         *
         * @see
         * com.google.gwt.event.dom.client.HasClickHandlers#addClickHandler(com.
         * google.gwt.event.dom.client.ClickHandler)
         */
    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasDoubleClickHandlers#addDoubleClickHandler
     * (com.google.gwt.event.dom.client.DoubleClickHandler)
     */
    public HandlerRegistration addDoubleClickHandler(DoubleClickHandler handler) {
        return addDomHandler(handler, DoubleClickEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasMouseDownHandlers#addMouseDownHandler
     * (com.google.gwt.event.dom.client.MouseDownHandler)
     */
    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasMouseUpHandlers#addMouseUpHandler(
     * com.google.gwt.event.dom.client.MouseUpHandler)
     */
    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return addDomHandler(handler, MouseUpEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasMouseOutHandlers#addMouseOutHandler
     * (com.google.gwt.event.dom.client.MouseOutHandler)
     */
    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasMouseOverHandlers#addMouseOverHandler
     * (com.google.gwt.event.dom.client.MouseOverHandler)
     */
    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasMouseMoveHandlers#addMouseMoveHandler
     * (com.google.gwt.event.dom.client.MouseMoveHandler)
     */
    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.google.gwt.event.dom.client.HasMouseWheelHandlers#addMouseWheelHandler
     * (com.google.gwt.event.dom.client.MouseWheelHandler)
     */
    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return addDomHandler(handler, MouseWheelEvent.getType());
    }


}
