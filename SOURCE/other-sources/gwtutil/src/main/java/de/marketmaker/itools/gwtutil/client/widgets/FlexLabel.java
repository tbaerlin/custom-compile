/*
 * SpanLabel.java
 *
 * Created on 11.09.2008 11:44:19
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasAllMouseHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
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
import com.google.gwt.i18n.client.BidiUtils;
import com.google.gwt.i18n.client.HasDirection;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.HasWordWrap;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * This is a copy of gwt Label, but allows to wrap all tags and not only &lt;div&gt; and &lt;span&gt;
 *
 * @author Ulrich Maurer
 */
@SuppressWarnings("deprecation")
public class FlexLabel
        extends Widget
        implements HasHorizontalAlignment, HasText, HasWordWrap, HasDirection, HasClickHandlers, HasAllMouseHandlers {

    /**
     * Creates a Label widget that wraps an existing element.
     * <p/>
     * This element must already be attached to the document. If the element is
     * removed from the document, you must call
     * {@link com.google.gwt.user.client.ui.RootPanel#detachNow(Widget)}.
     *
     * @param element the element to be wrapped
     */
    public static FlexLabel wrap(Element element) {
        // Assert that the element is attached.
        assert Document.get().getBody().isOrHasChild(element);

        FlexLabel label = new FlexLabel(element);

        // Mark it attached and remember it for cleanup.
        label.onAttach();
        RootPanel.detachOnWindowClose(label);

        return label;
    }

    private HorizontalAlignmentConstant horzAlign;

    /**
     * Creates an empty label.
     */
    public FlexLabel() {
        setElement(Document.get().createDivElement());
        //noinspection GWTStyleCheck
        setStyleName("mm-flexLabel");
    }

    /**
     * Creates a label with the specified text.
     *
     * @param text the new label's text
     */
    public FlexLabel(String text) {
        this();
        setText(text);
    }

    /**
     * Creates a label with the specified text.
     *
     * @param text   the new label's text
     * @param asHtml <code>true</code> to allow HTML code as text
     */
    public FlexLabel(String text, boolean asHtml) {
        this();
        setText(text, asHtml);
    }

    /**
     * This constructor may be used by subclasses to explicitly use an existing
     * element. This element must be either a &lt;div&gt; or &lt;span&gt; element.
     *
     * @param element the element to be used
     */
    public FlexLabel(Element element) {
        setElement(element);
        //noinspection GWTStyleCheck
        setStyleName("mm-flexLabel");
    }

    public FlexLabel(Element element, SafeHtml safeHtml) {
        this(element);
        setHTML(safeHtml);
    }

    /**
     * This constructor may be used by subclasses to explicitly use an existing
     * element. This element must be either a &lt;div&gt; or &lt;span&gt; element.
     *
     * @param element the element to be used
     */
    public FlexLabel(Element element, String text, boolean asHtml) {
        this(element);
        setText(text, asHtml);
    }

    private void setText(String text, boolean asHtml) {
        if (asHtml) {
            setHTML(text);
        }
        else {
            setText(text);
        }
    }

    public void setHTML(String html) {
        getElement().setInnerHTML(html);
    }

    public void setHTML(SafeHtml safeHtml) {
        setHTML(safeHtml.asString());
    }


    public HandlerRegistration addClickHandler(ClickHandler handler) {
        return addDomHandler(handler, ClickEvent.getType());
    }

    public HandlerRegistration addMouseDownHandler(MouseDownHandler handler) {
        return addDomHandler(handler, MouseDownEvent.getType());
    }

    public HandlerRegistration addMouseMoveHandler(MouseMoveHandler handler) {
        return addDomHandler(handler, MouseMoveEvent.getType());
    }

    public HandlerRegistration addMouseOutHandler(MouseOutHandler handler) {
        return addDomHandler(handler, MouseOutEvent.getType());
    }

    public HandlerRegistration addMouseOverHandler(MouseOverHandler handler) {
        return addDomHandler(handler, MouseOverEvent.getType());
    }

    public HandlerRegistration addMouseUpHandler(MouseUpHandler handler) {
        return addDomHandler(handler, MouseUpEvent.getType());
    }

    public HandlerRegistration addMouseWheelHandler(MouseWheelHandler handler) {
        return addDomHandler(handler, MouseWheelEvent.getType());
    }

    public Direction getDirection() {
        return BidiUtils.getDirectionOnElement(getElement());
    }

    public HorizontalAlignmentConstant getHorizontalAlignment() {
        return horzAlign;
    }

    public String getText() {
        return getElement().getInnerText();
    }

    public boolean getWordWrap() {
        return !getElement().getStyle().getProperty("whiteSpace").equals("nowrap");
    }

    public void setDirection(Direction direction) {
        BidiUtils.setDirectionOnElement(getElement(), direction);
    }

    public void setHorizontalAlignment(HorizontalAlignmentConstant align) {
        horzAlign = align;
        getElement().getStyle().setProperty("textAlign", align.getTextAlignString());
    }

    public void setText(String text) {
        getElement().setInnerText(text);
    }

    public void setWordWrap(boolean wrap) {
        getElement().getStyle().setProperty("whiteSpace",
                wrap ? "normal" : "nowrap");
    }
}
