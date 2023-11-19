/*
 * StyleMessageSink.java
 *
 * Created on 03.01.13 13:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages;

import com.google.gwt.user.client.ui.Widget;

import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Markus Dick
 */
public class StyleMessageSink implements MessageSink {
    public static final String INVALID_STYLE = "invalid"; //$NON-NLS$

    private final HashMap<Widget, HashSet<Object>> targets = new HashMap<Widget, HashSet<Object>>();

    @Override
    public void addMessage(Object target, Object source, String message) {
        if(target instanceof Widget) {
            final Widget widget = (Widget)target;

            HashSet<Object>objects = this.targets.get(widget);
            if(objects == null) {
                objects = new HashSet<Object>();
            }
            objects.add(source);
            this.targets.put(widget, objects);

            widget.addStyleName(INVALID_STYLE);
        }
    }

    @Override
    public void clearMessages(Object target, Object source) {
        if(target instanceof Widget) {
            final Widget widget = (Widget) target;

            HashSet<Object> sources = this.targets.get(widget);
            if(sources != null) {
                sources.remove(source);
                if(sources.isEmpty()) {
                    widget.removeStyleName(INVALID_STYLE);
                    this.targets.remove(widget);
                }
            }
        }
    }
}
