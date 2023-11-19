package de.marketmaker.itools.gwtutil.client.widgets;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CssUtil;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.util.Transitions;

/**
 * Author: umaurer
 * Created: 31.01.14
 */
public class Tooltip implements MouseMoveHandler, MouseOutHandler, ClickHandler {
    public static final String ATT_QTIP = "qtip";
    public static final String ATT_QTIP_LABEL = "qtipLabel";
    public static final String ATT_COMPLETION = "completion";
    public static final String ATT_BACKGROUND = "tooltipBackground";
    public static final String ATT_STYLE = "tooltipStyle";
    public static final String ATT_WRAPPED = "tooltipWrapped";
    public static final String WRAP_STYLE = "mm-tooltipWrapped";

    public static final String COMPLETION_AUTO = "auto";
    public static final String COMPLETION_QTIP = "qtip";

    private static boolean supported = false;
    private static Tooltip I = null;
    private static final int COMPLETION_MILLIS = 100;
    private static final int QTIP_MILLIS = 400;
    private Element lastTarget;
    private Element triggerElement;
    private final PopupPanel popupPanel = new PopupPanel(true, false);
    private Scheduler.ScheduledCommand scheduledPopup = null;
    private final Timer timer = new Timer() {
        @Override
        public void run() {
            if (scheduledPopup != null) {
                scheduledPopup.execute();
            }
        }
    };

    public static class TooltipFactory {
        private final Element element;
        private final String attribute;

        public TooltipFactory(Element element, String attribute) {
            this.element = element;
            this.attribute = attribute;
        }

        public TooltipFactory withBackground(String background) {
            this.element.setAttribute(ATT_BACKGROUND, background);
            return this;
        }

        public TooltipFactory withStyle(String styleName) {
            this.element.setAttribute(ATT_STYLE, styleName);
            return this;
        }

        public TooltipFactory wrapped() {
            this.element.setAttribute(ATT_WRAPPED, WRAP_STYLE);
            return this;
        }

        public void remove() {
            set((SafeHtml)null);
        }

        public void set(final String tooltipText) {
            set(toSafeHtml(tooltipText));
        }

        public void set(final SafeHtml tooltipText) {
            if (tooltipText == null) {
                this.element.removeAttribute(this.attribute);
            }
            else {
                this.element.setAttribute(this.attribute, tooltipText.asString());
            }
        }
    }

    public static TooltipFactory addAutoCompletion(final Widget source) {
        return addTo(source, ATT_COMPLETION, SafeHtmlUtils.fromTrustedString(COMPLETION_AUTO));
    }

    public static TooltipFactory addCompletion(final Widget source, final String tooltipText) {
        return addTo(source, ATT_COMPLETION, tooltipText);
    }

    public static TooltipFactory addCompletion(final Widget source, final SafeHtml tooltipText) {
        return addTo(source, ATT_COMPLETION, tooltipText);
    }

    public static TooltipFactory addQtip(final Widget source, final String tooltipText) {
        return addTo(source, ATT_QTIP, tooltipText);
    }

    public static TooltipFactory addQtip(final Widget source, final SafeHtml tooltipText) {
        return addTo(source, ATT_QTIP, tooltipText);
    }

    public static TooltipFactory addQtipLabel(final Widget source, final String tooltipText) {
        return addTo(source, ATT_QTIP_LABEL, tooltipText);
    }

    public static TooltipFactory addQtipLabel(final Widget source, final SafeHtml tooltipText) {
        return addTo(source, ATT_QTIP_LABEL, tooltipText);
    }

    public static TooltipFactory addTo(final Widget source, final String attribute, final String tooltipText) {
        return addTo(source, attribute, toSafeHtml(tooltipText));
    }

    public static TooltipFactory addTo(final Widget source, final String attribute, final SafeHtml tooltipText) {
        initialize();
        final TooltipFactory factory = new TooltipFactory(source.getElement(), attribute);
        factory.set(tooltipText);
        return factory;
    }

    private static SafeHtml toSafeHtml(String text) {
        return text == null ? null : StringUtility.toHtmlLines(text);
    }

    public static boolean initialize() {
        if (I == null) {
            I = new Tooltip();
        }
        return supported;
    }

    private Tooltip() {
        if (!isSupported()) {
            Firebug.info("Tooltips are not supported on this Browser -> initialization failed");
            return;
        }
        supported = true;
        final RootPanel widget = RootPanel.get();
        widget.addDomHandler(this, MouseMoveEvent.getType());
        widget.addDomHandler(this, MouseOutEvent.getType());
        Transitions.fadeInAfterAttach(this.popupPanel, 600);
        DOMUtil.forwardMouseWheelEvents(this.popupPanel);
    }

    public static boolean isSupported() {
        return _isElementClickSupported(new Label().getElement());
    }

    @Override
    public void onMouseMove(MouseMoveEvent event) {
        final Element eventTarget = Element.as(event.getNativeEvent().getEventTarget());
        if (this.lastTarget == eventTarget) {
            return;
        }
        if (this.popupPanel.getElement().isOrHasChild(eventTarget)) {
            this.lastTarget = eventTarget;
            return;
        }
        if (this.lastTarget != null) {
            onMouseOut(this.lastTarget);
        }
        this.lastTarget = eventTarget;
        onMouseOver(eventTarget);
    }

    private void onMouseOver(Element elt) {
        while (elt != null && elt.getNodeType() == Element.ELEMENT_NODE) {
            final String complete = elt.getAttribute(ATT_COMPLETION);
            if (complete != null && !complete.isEmpty()) {
                showCompletion(elt, complete);
                return;
            }
            final String qtip = elt.getAttribute(ATT_QTIP);
            if (qtip != null && !qtip.isEmpty()) {
                showTooltip(elt, qtip);
                return;
            }
            final String qtipLabel = elt.getAttribute(ATT_QTIP_LABEL);
            if (qtipLabel != null && !qtipLabel.isEmpty()) {
                showTooltipLabel(elt, qtipLabel);
                return;
            }
            elt = elt.getParentElement();
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private void info(String message, Element elt) {
        final StringBuilder sb = new StringBuilder();
        sb.append(message);
        sb.append(": ");
        while (elt != null) {
            sb.append(elt.getTagName()).append("/");
            elt = elt.getParentElement();
        }
        Firebug.debug(sb.toString());
    }

    private void onMouseOut(Element elt) {
        hideAll();
    }

    @Override
    public void onMouseOut(MouseOutEvent event) {
        if (this.lastTarget != null) {
            onMouseOut(this.lastTarget);
        }
    }

    @Override
    public void onClick(ClickEvent event) {
        if (this.triggerElement != null) {
            click(this.triggerElement);
            hideAll();
        }
    }

    private static native boolean _isElementClickSupported(Element element) /*-{
        if (element.click) {
            return true;
        }
        return false;
    }-*/;

    private native void click(Element target) /*-{
        target.click();
    }-*/;

    private void showCompletion(final Element elt, final String tooltipHtml) {
        final String popupTextHtml;
        if (COMPLETION_AUTO.equals(tooltipHtml)) {
            if (elt.getOffsetWidth() >= elt.getScrollWidth()) {
                return;
            }
            popupTextHtml = elt.getInnerHTML();
        }
        else if (COMPLETION_QTIP.equals(tooltipHtml)) {
            popupTextHtml = elt.getAttribute(ATT_QTIP);
            if (popupTextHtml == null) {
                return;
            }
        }
        else {
            popupTextHtml = tooltipHtml;
        }
        this.timer.cancel();
        this.scheduledPopup = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                final int paddingLeft = (int) CssUtil.getComputedPropertyPx(elt, "padding-left");
                final int paddingTop = (int) CssUtil.getComputedPropertyPx(elt, "padding-top");
                showPopup(elt, popupTextHtml, "mm-completionPopup", paddingLeft, paddingTop, paddingTop, true);
            }
        };
        this.timer.schedule(COMPLETION_MILLIS);
    }

    private void showTooltip(final Element elt, final String tooltipHtml) {
        this.timer.cancel();
        this.scheduledPopup = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                showPopup(elt, tooltipHtml, "mm-tooltip", 5, elt.getOffsetHeight() + 5, 5, false);
            }
        };
        this.timer.schedule(QTIP_MILLIS);
    }

    private void showTooltipLabel(final Element elt, final String tooltipHtml) {
        this.timer.cancel();
        this.scheduledPopup = new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                showPopup(elt, tooltipHtml, "mm-tooltip", -20, -2, -2, false);
            }
        };
        this.timer.schedule(QTIP_MILLIS);
    }

    private void showPopup(final Element elt, String html, String popupStyle, final int leftOffset, final int topOffset1, final int topOffset2, boolean transferFontSize) {
        this.popupPanel.setStyleName(popupStyle);
        final HTML htmlWidget = new HTML(html);
        htmlWidget.setStyleName("mm-tooltipContent");
        if (transferFontSize) {
            transferComputedCssProperty(elt, htmlWidget, "font-size", "fontSize");
        }
        transferPopupCssProperty(elt, htmlWidget.getElement(), ATT_BACKGROUND, "background");
        transferStyle(elt, htmlWidget, ATT_STYLE);
        transferStyle(elt, htmlWidget, ATT_WRAPPED);
        this.triggerElement = elt;
        htmlWidget.addClickHandler(this);
        this.popupPanel.setWidget(htmlWidget);
        DOMUtil.setTopZIndex(this.popupPanel);
        this.popupPanel.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
            @Override
            public void setPosition(int offsetWidth, int offsetHeight) {
                Tooltip.this.setPosition(elt, offsetWidth, offsetHeight, leftOffset, topOffset1, topOffset2);
            }
        });
    }

    private void setPosition(Element elt, int offsetWidth, int offsetHeight, int leftOffset, int topOffset1, int topOffset2) {
        final int clientWidth = Window.getClientWidth();
        final int clientHeight = Window.getClientHeight();
        int left = elt.getAbsoluteLeft() + leftOffset;
        if (left + offsetWidth > clientWidth) {
            left = clientWidth - offsetWidth;
            if (left < 0) {
                left = 0;
            }
        }
        int top;
        if (topOffset1 >= 0) {
            top = elt.getAbsoluteTop() + topOffset1;
            if (top + offsetHeight > clientHeight) {
                top = elt.getAbsoluteTop() - offsetHeight - topOffset2;
            }
        }
        else {
            top = elt.getAbsoluteTop() - offsetHeight + topOffset1;
        }
        if (top < 0) {
            top = 0;
        }
        this.popupPanel.setPopupPosition(left, top);
    }

    private void transferComputedCssProperty(Element elt, Widget widget, String computedProperty, String destinationProperty) {
        final String value = CssUtil.getComputedPropertyValue(elt, computedProperty);
        if (value == null) {
            return;
        }
        widget.getElement().getStyle().setProperty(destinationProperty, value);
    }

    private void transferPopupCssProperty(Element elt, Element popupElement, String attribute, String property) {
        final String value = elt.getAttribute(attribute);
        if (value != null && !value.isEmpty()) {
            popupElement.getStyle().setProperty(property, value);
        }
    }

    private void transferStyle(Element elt, Widget widget, String attribute) {
        final String value = elt.getAttribute(attribute);
        if (value != null && !value.isEmpty()) {
            widget.addStyleName(value);
        }
    }

    private void hideAll() {
        this.triggerElement = null;
        this.scheduledPopup = null;
        this.popupPanel.hide();
    }
}
