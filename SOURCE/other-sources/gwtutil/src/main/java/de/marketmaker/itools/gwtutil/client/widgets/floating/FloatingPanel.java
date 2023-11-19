package de.marketmaker.itools.gwtutil.client.widgets.floating;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Transitions;
import de.marketmaker.itools.gwtutil.client.widgets.TransparentScrollbar;

/**
 * @author Ulrich Maurer
 *         Date: 19.02.13
 */
public class FloatingPanel implements IsWidget, HasOneWidget, HasFloatingHandlers {
    private final FlowPanel panelOuter = new RequiresResizeFlowPanel();
    private final SimplePanel panelInner = new SimplePanel();
    private final Label labelBack = new HTML("<img src=\"clear.cache.gif\"/>");
    private final Label labelForward = new HTML("<img src=\"clear.cache.gif\"/>");
    private final TransparentScrollbar scrollbar;
    private final FloatingWidgetSupport floatingWidgetSupport = new FloatingWidgetSupport();
    private final OrientationSupport orientationSupport;
    private FloatingAnimation animation = new FloatingAnimation(this.floatingWidgetSupport);
    private Element selectedElement = null;
    private boolean needsResize = true;
    private boolean withTransition = false;

    public enum Orientation {
        HORIZONTAL, VERTICAL
    }

    private interface OrientationSupport {
        String getBackStyle();
        String getForwardStyle();
        int getPosition(Element element);
        void setPosition(Element element, int pos, boolean withTransition);
        int getOffsetSize(Widget widget);
    }

    private class HorizontalSupport implements OrientationSupport {
        @Override
        public String getBackStyle() {
            return "left";
        }

        @Override
        public String getForwardStyle() {
            return "right";
        }

        @Override
        public int getPosition(Element element) {
            return element.getOffsetLeft();
        }

        @Override
        public void setPosition(Element element, int pos, boolean withTransition) {
            if (FloatingPanel.this.withTransition != withTransition) {
                FloatingPanel.this.withTransition = withTransition;
                Transitions.setTransitionStyle(element, withTransition ? "left" : null, 50, "linear", 0);
            }
            element.getStyle().setLeft(pos, Style.Unit.PX);
        }

        @Override
        public int getOffsetSize(Widget widget) {
            return widget.getOffsetWidth();
        }
    }

    private class VerticalSupport implements OrientationSupport {
        @Override
        public String getBackStyle() {
            return "up";
        }

        @Override
        public String getForwardStyle() {
            return "down";
        }

        @Override
        public int getPosition(Element element) {
            return element.getOffsetTop();
        }

        @Override
        public void setPosition(Element element, int pos, boolean withTransition) {
            if (FloatingPanel.this.withTransition != withTransition) {
                FloatingPanel.this.withTransition = withTransition;
                Transitions.setTransitionStyle(element, withTransition ? "top" : null, 50, "linear", 0);
            }
            element.getStyle().setTop(pos, Style.Unit.PX);
        }

        @Override
        public int getOffsetSize(Widget widget) {
            return widget.getOffsetHeight();
        }
    }

    class RequiresResizeFlowPanel extends FlowPanel implements RequiresResize {
        @Override
        public void onResize() {
            FloatingPanel.this.onResize();
        }
    }

    class FloatingWidgetSupport implements FloatingWidget {
        private boolean floating = false;
        private int pos = 0;
        private int minPos = 0;
        private int maxPos = 0;

        public void init(int floatWidth, int position) {
            this.floating = floatWidth != 0;
            this.minPos = -floatWidth;
            this.maxPos = 0;
            setPosition(position, false);
            updateButtonVisibility();
        }

        @Override
        public int getMaximumPosition() {
            return this.maxPos;
        }

        @Override
        public int getMinimumPosition() {
            return this.minPos;
        }

        @Override
        public int getPosition() {
            return orientationSupport.getPosition(panelInner.getElement());
        }

        @Override
        public int setPosition(int position, boolean withTransition) {
            int previousPosition = this.pos;
            if (position < this.minPos) {
                this.pos = this.minPos;
            }
            else if (position > this.maxPos) {
                this.pos = this.maxPos;
            }
            else {
                this.pos = position;
            }
            orientationSupport.setPosition(panelInner.getElement(), this.pos, withTransition);
            if (this.pos != previousPosition) {
                FloatingEvent.fire(FloatingPanel.this, this.pos - previousPosition);
            }
            if (scrollbar != null) {
                scrollbar.setPosition(-this.pos);
            }
            return this.pos;
        }

        @Override
        public void updateButtonVisibility() {
            updateButtonVisibility(this.floating && this.pos < this.maxPos, this.floating && this.pos > this.minPos);
        }

        public void updateButtonVisibility(boolean backwardVisible, boolean forwardVisible) {
            labelBack.setVisible(backwardVisible);
            labelForward.setVisible(forwardVisible);
        }
    }


    public FloatingPanel(Orientation orientation) {
        this(orientation, true);
    }

    public FloatingPanel(Orientation orientation, boolean withScrollbar) {
        this.orientationSupport = orientation == Orientation.HORIZONTAL ? new HorizontalSupport() : new VerticalSupport();

        this.panelOuter.setStyleName("mm-floatPanel outer");
        this.panelInner.setStyleName("inner");
        this.labelBack.setStyleName("floatButton");
        this.labelBack.addStyleName(this.orientationSupport.getBackStyle());
        this.labelForward.setStyleName("floatButton");
        this.labelForward.addStyleName(this.orientationSupport.getForwardStyle());

        this.panelOuter.add(this.panelInner);
        this.panelOuter.add(this.labelBack);
        this.panelOuter.add(this.labelForward);

        if (orientation == Orientation.VERTICAL && withScrollbar) {
            this.scrollbar = new TransparentScrollbar(this.floatingWidgetSupport);
            this.panelOuter.add(this.scrollbar);
        }
        else {
            this.scrollbar = null;
        }

        this.panelOuter.sinkEvents(Event.ONMOUSEWHEEL);
        this.panelOuter.addHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                selectedElement = null;
                if (event.isNorth()) {
                    animation.stepBack();
                }
                else {
                    animation.stepForward();
                }
            }
        }, MouseWheelEvent.getType());
        this.panelOuter.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (event.isAttached()) {
                    Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
                        @Override
                        public void execute() {
                            onResize(0);
                        }
                    });
                }
            }
        });

        this.labelBack.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                selectedElement = null;
                animation.startBack();
            }
        });
        this.labelForward.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                selectedElement = null;
                animation.startForward();
            }
        });
        final MouseOutHandler outHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                animation.stop();
            }
        };
        this.labelBack.addMouseOutHandler(outHandler);
        this.labelForward.addMouseOutHandler(outHandler);
    }

    @Override
    public HandlerRegistration addFloatingHandler(FloatingHandler handler) {
        return this.panelInner.addHandler(handler, FloatingEvent.getType());
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        this.panelInner.fireEvent(event);
    }

    public void onResize() {
        if (!this.panelOuter.isAttached()) {
            return;
        }
        onResize(this.floatingWidgetSupport.getPosition());
    }

    private void onResize(int position) {
        final Element parentElement = this.panelOuter.getElement().getParentElement();
        if (parentElement == null
                || parentElement.getOffsetWidth() == 0
                || parentElement.getOffsetHeight() == 0) {
            this.needsResize = true;
            return;
        }

        this.needsResize = false;
        this.panelOuter.setPixelSize(parentElement.getOffsetWidth(), parentElement.getOffsetHeight());

        int outerSize = this.orientationSupport.getOffsetSize(this.panelOuter);
        int innerSize = this.orientationSupport.getOffsetSize(this.panelInner);
        int floatSize = innerSize - outerSize;

        if (this.scrollbar != null) {
            if (floatSize > 0) {
                this.scrollbar.setEnabled(true);
                this.scrollbar.setDimensions(innerSize, outerSize);
            }
            else {
                this.scrollbar.setEnabled(false);
            }
        }

        final int elementVisiblePosition = getElementVisiblePosition(this.selectedElement);
        if (elementVisiblePosition <= 0) {
            position = elementVisiblePosition;
        }

        this.floatingWidgetSupport.init(floatSize > 0 ? floatSize : 0, position);
    }

    public FloatingPanel withWidget(Widget widget) {
        setWidget(widget);
        return this;
    }

    @Override
    public Widget getWidget() {
        return this.panelInner.getWidget();
    }

    public void setWidget(Widget widget) {
        this.panelInner.setWidget(widget);
        onResize();
    }

    @Override
    public void setWidget(IsWidget w) {
        this.panelInner.setWidget(w);
        onResize();
    }

    @Override
    public Widget asWidget() {
        return this.panelOuter;
    }

    public void addStyleName(String style) {
        this.panelOuter.addStyleName(style);
    }

    public void scrollTo(Element element) {
        this.selectedElement = element;
        if (!this.panelOuter.isAttached() || this.needsResize) {
            return;
        }
        final int position = getElementVisiblePosition(element);
        if (position > 0) {
            return;
        }
        this.floatingWidgetSupport.setPosition(position, true);
        this.floatingWidgetSupport.updateButtonVisibility();
    }

    private int getElementVisiblePosition(Element element) {
        if (element == null) {
            return 1;
        }
        final int outerHeight = this.panelOuter.getOffsetHeight();
        if (outerHeight == 0) {
            return 1;
        }
        final int eltTop = element.getAbsoluteTop();
        final int eltHeight = element.getOffsetHeight();
        final int eltMid = eltHeight / 2;
        final int innerTop = this.panelInner.getAbsoluteTop();
        final int outerTop = this.panelOuter.getAbsoluteTop();
        final int posInInnerPanel = eltTop + eltMid - innerTop;
        int scrollPosition = posInInnerPanel - outerHeight / 2;
        if (eltTop >= outerTop && eltTop + eltHeight <= outerTop + outerHeight) {
            return 1;
        }
        return -scrollPosition;
    }
}
