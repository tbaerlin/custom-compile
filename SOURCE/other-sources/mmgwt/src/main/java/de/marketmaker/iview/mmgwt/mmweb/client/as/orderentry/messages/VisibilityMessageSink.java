/*
 * VisibilityMessageSink.java
 *
 * Created on 19.08.13 14:04
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages;

import com.google.gwt.user.client.ui.HasVisibility;

import java.util.HashSet;

/**
 * @author Markus Dick
 */
public class VisibilityMessageSink implements MessageSink {
    private final HashSet<Object> sources = new HashSet<Object>();

    final HasVisibility target;

    public VisibilityMessageSink(HasVisibility target) {
        this.target = target;
    }

    @Override
    public void addMessage(Object target, Object source, String message) {
        if(this.sources.add(source)) {
            this.target.setVisible(true);
        }
    }

    @Override
    public void clearMessages(Object target, Object source) {
        final Object removedSource = this.sources.remove(source);
        if(removedSource != null) {
            this.target.setVisible(!this.sources.isEmpty());
        }
    }
}
