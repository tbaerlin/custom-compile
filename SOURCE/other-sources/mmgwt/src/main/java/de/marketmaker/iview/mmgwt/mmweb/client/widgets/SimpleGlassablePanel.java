/*
 * SimpleGlassablePanel.java
 *
 * Created on 11.02.14 08:46
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * This panel works the other way around than the layout variant:
 * the size of the panel is automatically defined by the size
 * of the child widget.
 *
 * @author Markus Dick
 */
public class SimpleGlassablePanel extends Panel implements HasOneWidget {
    private Widget widget;
    private Label glass;
    private Timer timer;
    private boolean glassVisible;

    public SimpleGlassablePanel() {
        this(DOM.createDiv());
    }

    protected SimpleGlassablePanel(Element elem) {
        setElement(elem);

        elem.getStyle().setPosition(Style.Position.RELATIVE);

        this.glass = new Label();
        final Style s = this.glass.getElement().getStyle();
        s.setPosition(Style.Position.ABSOLUTE);
        s.setHeight(100, Style.Unit.PCT);
        s.setWidth(100, Style.Unit.PCT);
        s.setTop(0, Style.Unit.PX);

        this.glassVisible = false;

        setGlassStyleName("mm-glassPanel"); // $NON-NLS$
    }

    public void setGlassStyleName(String styleName) {
        this.glass.setStyleName(styleName);
    }

    public HandlerRegistration addGlassClickHandler(ClickHandler handler) {
        return this.glass.addClickHandler(handler);
    }

    public void showGlass() {
        setGlassVisible(true);
    }

    public void showGlass(int delay) {
        cancelTimer();
        this.timer = new Timer() {
          public void run() {
              setGlassVisible(true);
          }
        };
        this.timer.schedule(delay);
    }

    public void hideGlass() {
        cancelTimer();
        setGlassVisible(false);
    }

    private void cancelTimer() {
        if (this.timer == null) {
            return;
        }
        this.timer.cancel();
    }

    private void setGlassVisible(boolean visible) {
        Firebug.debug("<SimpleGlassablePanel.setGlassVisible> visible=" + visible + " this.glassVisible=" + (this.glassVisible));

        if(visible && !this.glassVisible) {
            DOM.appendChild(getContainerElement(), this.glass.getElement());
            try {
                adopt(this.glass);
            }
            catch(Exception e) {
                Firebug.warn("<SimpleGlassablePanel.setGlassVisible> Failed to adopt glass", e);
            }
        }
        else if(!visible && this.glassVisible) {
            try {
                orphan(this.glass);
            }
            catch(Exception e) {
                /* do nothing */
            }
            finally {
                getContainerElement().removeChild(this.glass.getElement());
            }
        }
        this.glassVisible = visible;
    }

    private boolean isGlassVisible() {
        return this.glass.getParent() != null;
    }

    @Override
    public boolean remove(Widget w) {
        // Validate.
        if (widget != w) {
            return false;
        }

        // Orphan.
        try {
            orphan(w);
        }
        finally {
            // Physical detach.
            getContainerElement().removeChild(this.widget.getElement());

            // Logical detach.
            widget = null;
        }
        return true;
    }

    /**
     * Adds a widget to this panel.
     *
     * @param w the child widget to be added
     */
    @Override
    public void add(Widget w) {
        // Can't add() more than one widget to a SimplePanel.
        if (getWidget() != null) {
            throw new IllegalStateException("SimpleGlassablePanel can only contain one child widget"); // $NON-NLS$
        }
        setWidget(w);
    }

    /**
     * Gets the panel's child widget.
     *
     * @return the child widget, or <code>null</code> if none is present
     */
    public Widget getWidget() {
        return this.widget;
    }

    public Iterator<Widget> iterator() {
        // Return a simple iterator that enumerates the 0 or 1 elements in this
        // panel.
        return new Iterator<Widget>() {
            boolean hasElement = widget != null;
            Widget returned = null;

            public boolean hasNext() {
                return hasElement;
            }

            public Widget next() {
                if (!hasElement || (widget == null)) {
                    throw new NoSuchElementException();
                }
                hasElement = false;
                return (returned = widget);
            }

            public void remove() {
                if (returned != null) {
                    SimpleGlassablePanel.this.remove(returned);
                }
            }
        };
    }

    public void setWidget(IsWidget w) {
        setWidget(asWidgetOrNull(w));
    }

    public void setWidget(Widget w) {
        cancelTimer();
        setGlassVisible(false);

        // Validate
        if (w == this.widget) {
            return;
        }

        // Detach new child.
        if (w != null) {
            w.removeFromParent();
        }

        // Remove old child.
        if (this.widget != null) {
            remove(this.widget);
        }

        // Logical attach.
        this.widget = w;

        if (w != null) {
            // Physical attach.
            if (isGlassVisible()) {
                DOM.insertBefore(getContainerElement(), this.widget.getElement(), this.glass.getElement());
            }
            else {
                DOM.appendChild(getContainerElement(), this.widget.getElement());
            }
            adopt(w);
        }
    }

    /**
     * Override this method to specify that an element other than the root element
     * be the container for the panel's child widget. This can be useful when you
     * want to create a simple panel that decorates its contents.
     *
     * Note that this method continues to return the
     * {@link com.google.gwt.user.client.Element} class defined in the
     * <code>User</code> module to maintain backwards compatibility.
     *
     * @return the element to be used as the panel's container
     */
    @SuppressWarnings("deprecation")
    protected com.google.gwt.user.client.Element getContainerElement() {
        return getElement();
    }
}
