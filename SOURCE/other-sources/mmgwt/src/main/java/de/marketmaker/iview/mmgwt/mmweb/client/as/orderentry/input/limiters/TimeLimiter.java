/*
 * TimeLimiter.java
 *
 * Created on 12.12.13 13:14
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBoxBase;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.TimeFormat;

import java.util.HashMap;

/**
 * @author Markus Dick
 */
public class TimeLimiter implements Limiter<TextBoxBase> {
    HashMap<TextBoxBase, HandlerRegistration> handlerRegistrations = new HashMap<TextBoxBase, HandlerRegistration>();

    public static final char TIME_SEPARATOR = ':';

    private final int maxNumberOfColons;

    public TimeLimiter(TimeFormat timeFormat) {
        switch(timeFormat) {
            case HHMMSS:
                this.maxNumberOfColons = 2;
            break;

            case HHMM:
            default:
                this.maxNumberOfColons = 1;
        }
    }

    @Override
    public TimeLimiter attach(final TextBoxBase textBoxBase) {
        if(this.handlerRegistrations.containsKey(textBoxBase)) {
            return this;
        }

        final KeyPressHandler handler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                TimeLimiter.this.onKeyPress(event, textBoxBase);
            }
        };

        this.handlerRegistrations.put(textBoxBase, textBoxBase.addKeyPressHandler(handler));
        return this;
    }

    private void onKeyPress(KeyPressEvent event, TextBoxBase textBoxBase) {
        final char charCode = event.getCharCode();

        if(event.isControlKeyDown() || event.isMetaKeyDown() ||
                charCode == 0 || charCode >= '0' && charCode <= '9') {
            return;
        }
        else if(charCode == TIME_SEPARATOR) {
            final String text = textBoxBase.getText();

            int colons = 0;
            for(int i = 0; i < text.length(); i++) {
                if(text.charAt(i) == TIME_SEPARATOR) {
                    colons++;
                }
            }

            final int cursor = textBoxBase.getCursorPos();

            if(cursor > 0 && colons < this.maxNumberOfColons) {
                return;
            }
        }
        event.stopPropagation();
        event.preventDefault();
    }

    @Override
    public TimeLimiter detach(final TextBoxBase textBoxBase) {
        HandlerRegistration registration = handlerRegistrations.get(textBoxBase);
        if(registration != null) {
            registration.removeHandler();
            handlerRegistrations.remove(textBoxBase);
        }

        return this;
    }
}
