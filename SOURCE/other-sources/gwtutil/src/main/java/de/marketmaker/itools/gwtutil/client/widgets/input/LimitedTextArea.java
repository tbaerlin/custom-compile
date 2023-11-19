/*
 * LimitedTextArea.java
 *
 * Created on 28.01.14 12:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.itools.gwtutil.client.widgets.input;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.TextArea;
import de.marketmaker.itools.gwtutil.client.event.EventUtil;

/**
 * A TextArea that limits the input to a configurable number of characters.
 *
 * @author oflege
 */
public class LimitedTextArea extends TextArea {
    private final int maxLength;

    /**
     * Constructor
     * @param maxLength max number of allowed characters
     */
    public LimitedTextArea(int maxLength) {
        sinkEvents(Event.ONPASTE | Event.ONKEYDOWN);

        this.maxLength = maxLength;

        LimitedTextArea.this.addValueChangeHandler(new ValueChangeHandler<String>() {
            @Override
            public void onValueChange(ValueChangeEvent<String> event) {
                setText(event.getValue()); // browser may modify text

                String text = getText();
                if (text.length() > LimitedTextArea.this.maxLength) {
                    LimitedTextArea.this.setValue(text.substring(0, LimitedTextArea.this.maxLength));
                }
            }
        });

    }

    /**
     * Description: Takes the browser event.
     * @param event declared.
     */
    @Override
    public void onBrowserEvent(Event event) {
        if (event.getTypeInt() == Event.ONPASTE) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                @Override
                public void execute() {
                    ValueChangeEvent.fire(LimitedTextArea.this,
                            LimitedTextArea.this.getText());
                }

            });
            return;
        }

        if (event.getTypeInt() == Event.ONKEYDOWN
                && getText().length() >= this.maxLength
                && !EventUtil.keyCodeIn(event
                // only allow navigation
                , KeyCodes.KEY_LEFT, KeyCodes.KEY_RIGHT
                , KeyCodes.KEY_UP, KeyCodes.KEY_DOWN
                , KeyCodes.KEY_HOME, KeyCodes.KEY_END
                , KeyCodes.KEY_PAGEUP, KeyCodes.KEY_PAGEDOWN
                , KeyCodes.KEY_TAB
                // delete
                , KeyCodes.KEY_DELETE, KeyCodes.KEY_BACKSPACE
                // and reload
                , KeyCodes.KEY_F5)) {
            event.preventDefault();
        }
        //necessary to fire value change events etc!
        super.onBrowserEvent(event);
    }
}
