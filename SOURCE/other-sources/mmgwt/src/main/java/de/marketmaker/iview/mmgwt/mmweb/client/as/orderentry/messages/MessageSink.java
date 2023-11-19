/*
 * MessageSink.java
 *
 * Created on 20.12.12 11:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.messages;

/**
 * @author Markus Dick
 */
public interface MessageSink {
    void addMessage(Object target, Object source, String message);
    void clearMessages(Object target, Object source);
}
