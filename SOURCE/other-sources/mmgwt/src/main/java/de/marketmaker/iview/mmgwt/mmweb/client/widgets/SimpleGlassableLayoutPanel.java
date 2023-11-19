package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.layout.client.Layout;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.Widget;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created on 17.01.13 09:16
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class SimpleGlassableLayoutPanel extends Panel implements RequiresResize, ProvidesResize, HasOneWidget {
    private final Layout layout;
    private Layout.Layer layerWidget;
    private Widget widget;
    private Layout.Layer layerGlass;
    private Label glass;
    private Timer timer;

    public SimpleGlassableLayoutPanel() {
        this(DOM.createDiv());
    }

    protected SimpleGlassableLayoutPanel(Element elem) {
        setElement(elem);
        this.glass = new Label();
        setGlassStyleName("mm-glassPanel"); // $NON-NLS$
        this.layout = new Layout(getElement());
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
        if (visible) {
            if (this.layerGlass == null) {
                this.layerGlass = this.layout.attachChild(this.glass.getElement(), this.glass);
                this.layerGlass.setTopHeight(0.0, Style.Unit.PX, 100.0, Style.Unit.PCT);
                this.layerGlass.setLeftWidth(0.0, Style.Unit.PX, 100.0, Style.Unit.PCT);
                if(!this.glass.isAttached()) {
                    adopt(this.glass);
                }
                this.layout.layout();
            }
        }
        else if (this.layerGlass != null) {
            try {
                orphan(this.glass);
            }
            catch(Exception e) {
                /* do nothing */
            }
            this.layout.removeChild(this.layerGlass);
            this.layerGlass = null;
        }
    }

    private boolean isGlassVisible() {
        return this.layerGlass != null;
    }

    @Override
    public void onResize() {
        if (widget instanceof RequiresResize) {
            ((RequiresResize) widget).onResize();
        }
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
            layout.removeChild(layerWidget);
            layerWidget = null;

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
            throw new IllegalStateException("SimpleGlassableLayoutPanel can only contain one child widget"); // $NON-NLS$
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
                    SimpleGlassableLayoutPanel.this.remove(returned);
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
                this.layerWidget = this.layout.attachChild(this.widget.getElement(), this.glass.getElement(), this.widget);
            }
            else {
                this.layerWidget = this.layout.attachChild(this.widget.getElement(), this.widget);
            }
            this.layerWidget.setTopHeight(0.0, Style.Unit.PX, 100.0, Style.Unit.PCT);
            this.layerWidget.setLeftWidth(0.0, Style.Unit.PX, 100.0, Style.Unit.PCT);

            adopt(w);

            // Update the layout.
            this.layout.layout();
            onResize();
        }
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        layout.onAttach();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        layout.onDetach();
    }


    /**
     * Override this method to specify that an element other than the root element
     * be the container for the panel's child widget. This can be useful when you
     * want to create a simple panel that decorates its contents.
     *
     * @return the element to be used as the panel's container
     */
    protected Element getContainerElement() {
        return getElement();
    }
}