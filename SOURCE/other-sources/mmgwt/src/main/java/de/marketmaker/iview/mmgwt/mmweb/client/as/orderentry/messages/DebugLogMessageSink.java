/*
 * DebugLogMessageSink.java
 *
 * Created on 20.12.12 12:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * This MessageSink thing is only usable for debugging.
 * @author Markus Dick
 */
public class DebugLogMessageSink implements MessageSink {
    @Override
    public void addMessage(Object target, Object source, String message) {
        Firebug.log("<DebugLogMessageSink.addMessage()>" + target.toString() + " " + source.toString() + " " + message);
    }

    @Override
    public void clearMessages(Object target, Object source) {
        Firebug.log("<DebugLogMessageSink.clearMessages()>" + target.toString() + " " + source.toString());
    }
}
