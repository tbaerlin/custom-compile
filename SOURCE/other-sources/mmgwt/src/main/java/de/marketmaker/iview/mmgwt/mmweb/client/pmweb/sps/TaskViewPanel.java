package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ProvidesResize;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ContentResizePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * Author: umaurer
 * Created: 02.06.14
 */
public class TaskViewPanel extends Composite implements RequiresResize, ProvidesResize {
    public interface PinDisplay {
        void setPinned(boolean pinned);
    }

    private static final int MIN_CONTENT_HEIGHT = 260;
    public static final String SPS_SOUTH_WIDGET_PINNED = "sps-southWidget-pinned"; // $NON-NLS$

    public static boolean scrollDownRequested = false;

    public boolean scrollToRequested = false;
    public Element scrollToElement = null;

    private final FlowPanel panel = new FlowPanel();
    private final ContentResizePanel crp = new ContentResizePanel().withGradients();
    private final Style crpStyle;
    private final SimplePanel northPanel = new SimplePanel();
    private final SimplePanel southPanel = new SimplePanel();
    private final Style southPanelStyle = this.southPanel.getElement().getStyle();
    private Widget contentWidget;
    private Widget northWidget;
    private Widget southWidget;
    private boolean layoutPending = false;
    private boolean southWidgetPinned = SessionData.INSTANCE.isUserPropertyTrue(SPS_SOUTH_WIDGET_PINNED);
    private PinDisplay pinDisplay;

    public TaskViewPanel() {
        this.northPanel.setStyleName(TaskDisplay.SPS_TASK_VIEW_STYLE + " sps-flexSPanel-north");
        this.southPanel.setStyleName("sps-flexSPanel-south");
        this.panel.setStyleName("sps-flexSPanel");
        this.crp.setStyleName("sps-flexSPanel-crp");
        this.crp.addResizeHandler(event -> layout());
        this.crpStyle = this.crp.getElement().getStyle();
        this.panel.add(this.northPanel);
        this.panel.add(this.crp);
        this.panel.add(this.southPanel);
        this.southPanel.setVisible(false);
        initWidget(this.panel);
    }

    public void setContentStyleName(String style) {
        this.crp.setContentStyleName(style);
    }

    public void clearContent() {
        this.contentWidget = null;
        this.crp.setContentWidget(new Label());
    }

    public void setContentWidget(Widget widget) {
        this.southPanel.setVisible(true);
        this.contentWidget = widget;
        this.crp.setContentWidget(widget);
        layout();
    }

    public void setNorthWidget(Widget widget) {
        this.northWidget = widget;
        this.northPanel.setWidget(widget);
        layout();
    }

    public void setSouthWidget(Widget widget) {
        if(widget instanceof PinDisplay) {
            this.pinDisplay = (PinDisplay)widget;
        }
        this.southWidget = widget;
        this.southPanel.setWidget(widget);
        layout();
    }

    @Override
    public void onResize() {
        layout();
    }

    private void onResize(Widget widget) {
        if (widget == null) {
            return;
        }
        if (widget instanceof RequiresResize) {
            ((RequiresResize) widget).onResize();
        }
    }

    public void toggleSouthWidgetPinned() {
        this.southWidgetPinned = !this.southWidgetPinned;
        layout();
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(TaskViewPanel.SPS_SOUTH_WIDGET_PINNED, this.southWidgetPinned ? "true" : null); // $NON-NLS$
    }

    public boolean isSouthWidgetPinned() {
        return this.southWidgetPinned;
    }

    public void layout() {
        if (this.layoutPending) {
            return;
        }
        this.layoutPending = true;
        Scheduler.get().scheduleDeferred(this::doLayout);
    }

    public void updateSouthWidgetPinned() {
        this.southWidgetPinned = SessionData.INSTANCE.isUserPropertyTrue(SPS_SOUTH_WIDGET_PINNED);
        if(this.pinDisplay != null) {
            this.pinDisplay.setPinned(this.southWidgetPinned);
        }
    }

    private void doLayout() {
        if (!this.layoutPending) {
            return;
        }
        this.layoutPending = false;
        if (this.contentWidget == null) {
            return;
        }
        final int panelHeight = this.panel.getOffsetHeight();

        final int contentHeight = this.contentWidget.getOffsetHeight();
        final int northHeight = this.northWidget == null ? 0 : this.northWidget.getOffsetHeight();
        final int southHeight = this.southWidget == null ? 0 : this.southWidget.getOffsetHeight();
        final int nsHeight = northHeight + southHeight;

        final int contentPanelHeight;
        if (contentHeight <= 0 || contentHeight + nsHeight > panelHeight || this.contentWidget instanceof RequiresResize || this.southWidgetPinned) {
            contentPanelHeight = panelHeight - nsHeight;
        }
        else if (contentHeight < MIN_CONTENT_HEIGHT) {
            if (MIN_CONTENT_HEIGHT + nsHeight > panelHeight) {
                contentPanelHeight = panelHeight - nsHeight;
            }
            else {
                contentPanelHeight = MIN_CONTENT_HEIGHT;
            }
        }
        else {
            contentPanelHeight = contentHeight;
        }
        this.crpStyle.setTop(northHeight, PX);
        this.crpStyle.setHeight(contentPanelHeight, PX);
        this.southPanelStyle.setTop(contentPanelHeight + northHeight, PX);
        if (this.contentWidget instanceof RequiresResize) {
            this.contentWidget.setHeight(contentPanelHeight + "px"); // $NON-NLS$
        }
        if (scrollDownRequested) {
            scrollDownRequested = false;
            this.crp.getElement().setScrollTop(this.crp.getElement().getScrollHeight());
        }
        if (scrollToRequested) {
            scrollToRequested = false;
            ensureVisibleImpl(this.crp.getElement(), scrollToElement);
        }
        onResize(this.contentWidget);
        onResize(this.southWidget);
        this.crp.updateScrollIndicators();
    }

    public static void requestScrollDown() {
        scrollDownRequested = true;
    }

    /**
     * Scrolls an element into view.
     * FF ESR 31 and FF ESR 38 do not automatically scroll the widget that gains the focus via JavaScript
     * calls into view, as IE 9, 10, 11, and Chrome do. Moreover it preserves any scroll positions from
     * the previous page views. Hence, it is necessary to scroll the widget that should gain the focus into
     * view by ourselves. See AS-1263 for further details.
     */
    public void ensureVisible(Element element) {
        this.scrollToElement = element;
        this.scrollToRequested = true;
    }

    @SuppressWarnings("unused")
    public Widget getContentWidget() {
        return this.contentWidget;
    }

    /**
     * This methods code is copied from GWT code.
     * @see com.google.gwt.user.client.ui.ScrollPanel#ensureVisibleImpl(Element, Element)
     */
    private native static void ensureVisibleImpl(Element scroll, Element e) /*-{
        if (!e)
            return;

        var item = e;
        var realOffset = 0;
        while (item && (item != scroll)) {
            realOffset += item.offsetTop;
            item = item.offsetParent;
        }

        scroll.scrollTop = realOffset - scroll.offsetHeight / 2;
    }-*/;
}
