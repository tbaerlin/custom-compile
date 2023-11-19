package de.marketmaker.itools.gwtutil.client.widgets.floating;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;
import de.marketmaker.itools.gwtutil.client.widgets.TransparentScrollbar;

/**
 * @author Ulrich Maurer
 *         Date: 19.10.12
 */
public class FloatingPopupPanel extends PopupPanel {
    private static final int POPUP_MIN_HEIGHT = 100;
    private final FlowPanel panelOuter = new FlowPanel();
    private final SimplePanel panelInner = new SimplePanel();
    private final Label labelUp = new HTML("<img src=\"clear.cache.gif\"/>");
    private final Label labelDown = new HTML("<img src=\"clear.cache.gif\"/>");
    private final FloatingWidgetSupport floatingWidgetSupport = new FloatingWidgetSupport();
    private final TransparentScrollbar scrollbar = new TransparentScrollbar(this.floatingWidgetSupport);
    private FloatingAnimation animation = new FloatingAnimation(this.floatingWidgetSupport);
    private boolean initialized = false;

    public FloatingPopupPanel() {
        initWidgets();
    }

    public FloatingPopupPanel(boolean autoHide) {
        super(autoHide);
        initWidgets();
    }

    public FloatingPopupPanel(boolean autoHide, boolean modal) {
        super(autoHide, modal);
        initWidgets();
    }

    private void initWidgets() {
        if (this.initialized) {
            return;
        }
        this.initialized = true;
        PopupPanelFix.addFrameDummy(this);
        setStyleName(getContainerElement(), "popupContent mm-floatPanel");
        this.panelOuter.setStyleName("outer");
        this.panelInner.setStyleName("inner");
        this.labelUp.setStyleName("floatButton");
        this.labelUp.addStyleName("up");
        this.labelDown.setStyleName("floatButton");
        this.labelDown.addStyleName("down");

        this.panelOuter.add(this.panelInner);
        this.panelOuter.add(this.labelUp);
        this.panelOuter.add(this.labelDown);
        this.panelOuter.add(this.scrollbar);

        this.panelOuter.sinkEvents(Event.ONMOUSEWHEEL);
        this.panelOuter.addHandler(new MouseWheelHandler() {
            @Override
            public void onMouseWheel(MouseWheelEvent event) {
                if (event.isNorth()) {
                    animation.stepBack();
                }
                else {
                    animation.stepForward();
                }
            }
        }, MouseWheelEvent.getType());

        this.labelUp.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                animation.startBack();
            }
        });
        this.labelDown.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                animation.startForward();
            }
        });
        final MouseOutHandler outHandler = new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                animation.stop();
            }
        };
        this.labelUp.addMouseOutHandler(outHandler);
        this.labelDown.addMouseOutHandler(outHandler);
    }

    @Override
    public void setWidget(Widget w) {
        this.panelInner.setWidget(w);
        super.setWidget(this.panelOuter);
    }

    @Override
    public Widget getWidget() {
        return this.panelInner.getWidget();
    }

    @Override
    public void setPopupPositionAndShow(PositionCallback callback) {
        setVisible(false);
        show();
        callback.setPosition(this.panelInner.getOffsetWidth(), this.panelInner.getOffsetHeight());
        setVisible(true);
    }

    public void showNearby(final UIObject target, final Widget selectedWidget) {
        showNearby(target, selectedWidget == null ? null : selectedWidget.getElement());
    }

    public void showNearby(final UIObject target, final Element selectedElement) {
        setPopupPositionAndShow(new PositionCallback() {
            public void setPosition(int offsetWidth, int offsetHeight) {
                position(target, offsetWidth, offsetHeight, selectedElement);
            }
        });
    }

    private void position(final UIObject relativeObject, int offsetWidth, int offsetHeight, final Element selectedElement) {
        int textBoxOffsetWidth = relativeObject.getOffsetWidth();
        int offsetWidthDiff = offsetWidth - textBoxOffsetWidth;
        int left = relativeObject.getAbsoluteLeft();
        if (offsetWidthDiff > 0) {
            // Make sure scrolling is taken into account, since
            // box.getAbsoluteLeft() takes scrolling into account.
            int windowRight = Window.getClientWidth() + Window.getScrollLeft();
            int windowLeft = Window.getScrollLeft();
            // Distance from the left edge of the text box to the right edge of the window
            int distanceToWindowRight = windowRight - left;
            // Distance from the left edge of the text box to the left edge of the window
            int distanceFromWindowLeft = left - windowLeft;
            if (distanceToWindowRight < offsetWidth && distanceFromWindowLeft >= offsetWidthDiff) {
                left -= offsetWidthDiff;
            }
        }

        this.panelOuter.setHeight(offsetHeight + "px");
        final int border = this.getElement().getOffsetHeight() - this.panelOuter.getElement().getOffsetHeight();

        int top = relativeObject.getAbsoluteTop();
        // Make sure scrolling is taken into account, since
        // box.getAbsoluteTop() takes scrolling into account.
        int windowTop = Window.getScrollTop();
        int windowBottom = Window.getScrollTop() + Window.getClientHeight();
        int distanceFromWindowTop = top - windowTop;
        int distanceToWindowBottom = windowBottom - (top + relativeObject.getOffsetHeight());
        final int popupHeight;
        if (distanceToWindowBottom >= offsetHeight + border) { // enough space below relativeObject
            popupHeight = offsetHeight;
            top += relativeObject.getOffsetHeight();
        }
        else if (distanceFromWindowTop >= offsetHeight + border) { // enough space above relativeObject
            popupHeight = offsetHeight;
            top -= popupHeight + border;
        }
        else if (distanceFromWindowTop > distanceToWindowBottom) { // more space above relativeObject than below
            popupHeight = (distanceFromWindowTop > POPUP_MIN_HEIGHT ? distanceFromWindowTop : POPUP_MIN_HEIGHT) - border;
            top -= popupHeight + border;
        }
        else { // more space below relativeObject than above
            popupHeight = (distanceToWindowBottom > POPUP_MIN_HEIGHT ? distanceToWindowBottom : POPUP_MIN_HEIGHT) - border;
            top += relativeObject.getOffsetHeight();
        }

        setPopupSize(offsetWidth, popupHeight);
        setPopupPosition(left, top);
        final int scrollPosition;
        if (selectedElement == null) {
            scrollPosition = 0;
        }
        else {
            final int posInInnerPanel = selectedElement.getAbsoluteTop()
                    + selectedElement.getOffsetHeight() / 2
                    - this.panelInner.getAbsoluteTop();
            scrollPosition = posInInnerPanel - popupHeight / 3;
        }
        this.scrollbar.setDimensions(offsetHeight, popupHeight);
        this.floatingWidgetSupport.init(offsetHeight - popupHeight, -scrollPosition);
    }

    private void setPopupSize(int width, int height) {
        this.panelOuter.setPixelSize(width, height);
    }

    class FloatingWidgetSupport implements FloatingWidget {
        private boolean floating = false;
        private int pos = 0;
        private int minPos = 0;
        private int maxPos = 0;

        public void init(int floatHeight, int position) {
            this.floating = floatHeight != 0;
            this.minPos = -floatHeight;
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
            return panelInner.getElement().getOffsetTop();
        }

        @Override
        public int setPosition(int position, boolean withTransition) {
            if (position < this.minPos) {
                this.pos = this.minPos;
            }
            else if (position > this.maxPos) {
                this.pos = this.maxPos;
            }
            else {
                this.pos = position;
            }
            panelInner.getElement().getStyle().setTop(this.pos, Style.Unit.PX);
            scrollbar.setPosition(-this.pos);
            return this.pos;
        }

        @Override
        public void updateButtonVisibility() {
            updateButtonVisibility(this.floating && this.pos < this.maxPos, this.floating && this.pos > this.minPos);
        }

        public void updateButtonVisibility(boolean backwardVisible, boolean forwardVisible) {
            labelUp.setVisible(backwardVisible);
            labelDown.setVisible(forwardVisible);
        }
    }

    public void scrollToElement(Element element) {
        final int eltTop = element.getAbsoluteTop();
        final int eltHeight = element.getOffsetHeight();
        final int eltMid = eltHeight / 2;
        final int innerTop = this.panelInner.getAbsoluteTop();
        final int popupTop = this.panelOuter.getAbsoluteTop();
        final int popupHeight = this.panelOuter.getOffsetHeight();
        final int posInInnerPanel = eltTop + eltMid - innerTop;
        int scrollPosition = posInInnerPanel - popupHeight / 2;
        if (eltTop >= popupTop && eltTop + eltHeight <= popupTop + popupHeight) {
            return;
        }
        this.floatingWidgetSupport.setPosition(-scrollPosition, true);
        this.floatingWidgetSupport.updateButtonVisibility();
    }
}
