/*
 * UnambiguousNumberLimiter.java
 *
 * Created on 20.12.12 09:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.limiters;

import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.TextBoxBase;

import java.util.HashMap;

import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.DEFAULT_DECIMAL_SEPARATOR;
import static de.marketmaker.iview.mmgwt.mmweb.client.util.StringBasedNumberFormat.DEFAULT_GROUPING_SEPARATOR;

/**
 * Limits text box input to characters acceptable for {@see de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.Renderers.UnambiguousNumberRenderer}.
 * Treats any grouping delimiters as decimal delimiters.
 *
 * <p>
 *     This limiter is not as restrictive as a number format.
 *     Hence, the resulting number format has to be validated, e.g. if the target component looses the focus.
 * </p>
 * <p>The limiter does not allow to input:</p>
 * <ul>
 *     <li>more than one grouping or decimal delimiter</li>
 *     <li>decimal and grouping delimiters at cursor home position</li>
 *     <li>other characters than numbers (except delimiters)</li>
 * </ul>
 *
 * @author Markus Dick
 */
public class UnambiguousNumberLimiter implements Limiter<TextBoxBase> {
    HashMap<TextBoxBase, HandlerRegistration> handlerRegistrations = new HashMap<TextBoxBase, HandlerRegistration>();

    @Override
    public UnambiguousNumberLimiter attach(final TextBoxBase textBoxBase) {
        if(handlerRegistrations.containsKey(textBoxBase)) {
            return this;
        }

        final KeyPressHandler handler = new KeyPressHandler() {
            @Override
            public void onKeyPress(KeyPressEvent event) {
                final char charCode = event.getCharCode();

                if(event.isControlKeyDown() || event.isMetaKeyDown() ||
                        charCode == 0 || charCode >= '0' && charCode <= '9') {
                    return;
                }
                else if(charCode == DEFAULT_DECIMAL_SEPARATOR || charCode == DEFAULT_GROUPING_SEPARATOR) {
                    final int decimal = textBoxBase.getText().indexOf(DEFAULT_DECIMAL_SEPARATOR);
                    final int grouping = textBoxBase.getText().indexOf(DEFAULT_GROUPING_SEPARATOR);

                    final int cursor = textBoxBase.getCursorPos();

                    if(cursor > 0 && (decimal < 0 && grouping < 0)) {
                        return;
                    }
                }
                event.stopPropagation();
                event.preventDefault();
            }
        };

        this.handlerRegistrations.put(textBoxBase, textBoxBase.addKeyPressHandler(handler));
        return this;
    }

    @Override
    public UnambiguousNumberLimiter detach(final TextBoxBase textBoxBase) {
        HandlerRegistration registration = handlerRegistrations.get(textBoxBase);
        if(registration != null) {
            registration.removeHandler();
            handlerRegistrations.remove(textBoxBase);
        }

        return this;
    }
}
