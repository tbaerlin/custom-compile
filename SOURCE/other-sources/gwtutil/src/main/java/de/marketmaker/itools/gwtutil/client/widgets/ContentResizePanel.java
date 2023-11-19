package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

import static com.google.gwt.dom.client.Style.Overflow.AUTO;
import static com.google.gwt.dom.client.Style.Position.RELATIVE;
import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * A panel that fires a ResizeEvent, if its content (specified by setContentWidget()) changes its size.
 *
 * Author: umaurer
 * Created: 06.06.14
 */
public class ContentResizePanel extends Composite implements HasResizeHandlers {
    private final SimplePanel contentPanel = new SimplePanel();
    private final Element expandTrigger;
    private final Element expandTriggerChild;
    private final Element contractTrigger;

    private boolean gradientsEnabled = false;
    private int gradientHeight = 20;
    private Style gradientTopStyle;
    private Style gradientBottomStyle;

    private Size lastSize = new Size(0, 0);

    private class Size {
        private final int width;
        private final int height;

        Size(Widget w) {
            this(w.getOffsetWidth(), w.getOffsetHeight());
        }

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Size)) {
                return false;
            }
            final Size os = (Size) o;
            return this.width == os.width && this.height == os.height;
        }

        @Override
        public String toString() {
            return "" + width + "x" + height;
        }
    }

    public ContentResizePanel() {
        final SimplePanel panel = new SimplePanel();
        initWidget(panel);
        Element resizeTriggers = DOM.createDiv();
        this.expandTrigger = DOM.createDiv();
        this.expandTriggerChild = DOM.createDiv();
        this.contractTrigger = DOM.createDiv();
        resizeTriggers.appendChild(this.expandTrigger);
        resizeTriggers.appendChild(this.contractTrigger);
        this.expandTrigger.appendChild(this.expandTriggerChild);
        this.contractTrigger.appendChild(DOM.createDiv());

        resizeTriggers.setClassName("mm-crp-resizeTriggers");
        this.expandTrigger.setClassName("mm-crp-expandTrigger");
        this.contractTrigger.setClassName("mm-crp-contractTrigger");

        this.getElement().getStyle().setOverflow(AUTO);
        this.contentPanel.getElement().insertFirst(resizeTriggers);
        this.contentPanel.getElement().getStyle().setPosition(RELATIVE);

        panel.setWidget(this.contentPanel);

        Event.sinkEvents(this.expandTrigger, Event.ONSCROLL);
        Event.sinkEvents(this.contractTrigger, Event.ONSCROLL);
    }

    public ContentResizePanel withGradients() {
        this.gradientsEnabled = true;
        updateScrollIndicators();
        return this;
    }

    public void createGradients() {
        final Element gradientTop = DOM.createDiv();
        gradientTop.setClassName("gradient-top");
        this.gradientTopStyle = gradientTop.getStyle();
        this.gradientTopStyle.setTop(0, PX);
        final Element gradientBottom = DOM.createDiv();
        gradientBottom.setClassName("gradient-bottom");
        this.gradientBottomStyle = gradientBottom.getStyle();

        final SimplePanel panel = (SimplePanel) getWidget();
        panel.addDomHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                updateScrollIndicators();
            }
        }, ScrollEvent.getType());

        final Element parentElement = panel.getElement().getParentElement();
        parentElement.appendChild(gradientTop);
        parentElement.appendChild(gradientBottom);
        this.gradientHeight = gradientBottom.getOffsetHeight();
    }

    public void updateScrollIndicators() {
        if (!this.gradientsEnabled || !isAttached()) {
            return;
        }
        if (this.gradientTopStyle == null) {
            createGradients();
        }
        final Element contentElement = getWidget().getElement();
        final int scrollTopMax = getScrollTopMax(contentElement);

        final int top = contentElement.getOffsetTop();
        final int left = contentElement.getOffsetLeft();
        final int width = contentElement.getClientWidth();
        this.gradientTopStyle.setTop(top, PX);
        this.gradientTopStyle.setLeft(left, PX);
        this.gradientTopStyle.setWidth(width, PX);
        this.gradientTopStyle.setDisplay(contentElement.getScrollTop() > 1 ? Style.Display.BLOCK : Style.Display.NONE);

        this.gradientBottomStyle.setTop(top + contentElement.getClientHeight() - this.gradientHeight, PX);
        this.gradientBottomStyle.setLeft(left, PX);
        this.gradientBottomStyle.setWidth(width, PX);
        this.gradientBottomStyle.setDisplay(contentElement.getScrollTop() < scrollTopMax - 1 ? Style.Display.BLOCK : Style.Display.NONE);
    }

    public int getScrollTopMax(Element element) {
        final int scrollHeight = element.getScrollHeight();
        final int clientHeight = element.getClientHeight();
        return scrollHeight >= clientHeight ? scrollHeight - clientHeight : 0;
    }


    public void setContentWidget(Widget w) {
        this.contentPanel.setWidget(w);
        resetTriggers();
    }

    public void setContentStyleName(String styleName) {
        this.contentPanel.setStyleName(styleName);
    }

    @Override
    public HandlerRegistration addResizeHandler(ResizeHandler handler) {
        return addHandler(handler, ResizeEvent.getType());
    }

    @Override
    protected void onAttach() {
        super.onAttach();
        DOM.setEventListener(this.expandTrigger, this);
        DOM.setEventListener(this.contractTrigger, this);
        resetTriggers();
    }

    @Override
    protected void onDetach() {
        super.onDetach();
        DOM.setEventListener(this.expandTrigger, null);
        DOM.setEventListener(this.contractTrigger, null);
    }

    private void resetTriggers() {
        final Size size = new Size(this.contentPanel.getWidget());
        if (size.equals(this.lastSize)) {
            return;
        }
        this.lastSize = size;
        Scheduler.get().scheduleFixedDelay(new Scheduler.RepeatingCommand() {
            @Override
            public boolean execute() {
                doResetTriggers();
                return false;
            }
        }, 20);
    }

    private void doResetTriggers() {
        setScrollMax(this.contractTrigger);
        setSize(this.expandTriggerChild, this.expandTrigger.getOffsetWidth() + 1, this.expandTrigger.getOffsetHeight() + 1);
        setScrollMax(this.expandTrigger);
        ResizeEvent.fire(this, this.lastSize.width, this.lastSize.height);
        updateScrollIndicators();
    }

    private static void setSize(Element e, double width, double height) {
        final Style style = e.getStyle();
        style.setWidth(width, PX);
        style.setHeight(height, PX);
    }

    private static void setScrollMax(Element e) {
        e.setScrollLeft(e.getScrollWidth());
        e.setScrollTop(e.getScrollHeight());
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (Event.ONSCROLL == event.getTypeInt()) {
            resetTriggers();
        }
        super.onBrowserEvent(event);
    }
}
