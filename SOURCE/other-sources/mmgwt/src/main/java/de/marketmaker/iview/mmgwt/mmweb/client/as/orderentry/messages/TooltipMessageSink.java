/*
 * TooltipMessageSink.java
 *
 * Created on 03.01.13 15:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages;

import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;

import java.util.LinkedHashMap;

/**
 * @author Markus Dick
 */
public class TooltipMessageSink implements MessageSink {
    private static final String ERROR_MESSAGE_STYLE = "error"; //$NON-NLS$
    private static final String ADDITIONAL_TOOLTIP_STYLE = "validatorTooltip"; //$NON-NLS$
    public static final String BR_TAG = "<br/>"; //$NON-NLS$
    private static final int SHOW_TIMER = 5000;
    private static final int HIDE_TIMER = 1000;

    private final Widget target;
    private LinkedHashMap<Object, String> messages = new LinkedHashMap<Object, String>();
    private Tooltip tooltip;
    private final Timer hideTimer;

    public TooltipMessageSink(final Widget target) {
        this.target = target;

        this.hideTimer = new Timer() {
            @Override
            public void run() {
                handleHide();
            }
        };

        target.addDomHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                handleShow(target);
            }
        }, FocusEvent.getType());

        target.addDomHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                handleShow(target);
            }
        }, MouseOverEvent.getType());

        target.addDomHandler(new MouseOutHandler() {
            @Override
            public void onMouseOut(MouseOutEvent event) {
                hideTimer.schedule(HIDE_TIMER);
            }
        }, MouseOutEvent.getType());
    }

    private void handleHide() {
        if (tooltip != null) {
            tooltip.hide();
        }
    }

    private void handleShow(Widget target) {
        this.hideTimer.cancel();
        handleHide();

        if (!this.messages.isEmpty()) {
            if(tooltip == null) {
                tooltip = new Tooltip(SHOW_TIMER, ADDITIONAL_TOOLTIP_STYLE);
            }

            String content = "";
            boolean first = true;
            for(String text : this.messages.values()) {
                if(!first) {
                    content += BR_TAG;
                }
                else {
                    first = false;
                }
                content += text;
            }
            tooltip.setContent(content, ERROR_MESSAGE_STYLE);
            tooltip.showRelativeTo(target);
        }
    }

    @Override
    public void addMessage(Object target, Object source, String message) {
        if(this.target == target) {
            this.messages.put(source, message);
        }
    }

    @Override
    public void clearMessages(Object target, Object source) {
        if(this.target == target) {
            this.messages.remove(source);
        }
    }

    /**
     * @author Markus Dick
     */
    static class Tooltip extends PopupPanel {
        private final int delay;

        private final HTML contents = new HTML();

        public Tooltip(final int delay, final String styleName) {
            super(true);

            this.delay = delay;
            addStyleName(styleName);

            add(contents);
        }

        public void show() {
            super.show();

            final Timer t = new Timer() {
                public void run() {
                    Tooltip.this.hide();
                }
            };

            t.schedule(delay);
        }

        void setContent(String content, String styleName) {
            contents.setStyleName(styleName);
            contents.setHTML(content);
        }
    }
}
