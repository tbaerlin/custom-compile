package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created on 13.11.12 11:02
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class HistoryThreadEvent extends GwtEvent<HistoryThreadHandler> {

    public enum Reason {
        NEW,
        DELETE,
        SWITCHED_TO,
        REPLACED_ALL
    }

    private static Type<HistoryThreadHandler> TYPE;
    private final int threadId;
    private final Reason reason;

    public static HistoryThreadEvent createNewThreadEvent(int threadId) {
        return new HistoryThreadEvent(threadId, Reason.NEW);
    }

    public static HistoryThreadEvent createDelThreadEvent(int threadId) {
        return new HistoryThreadEvent(threadId, Reason.DELETE);
    }

    public static HistoryThreadEvent createThreadChangedEvent(int threadId) {
        return new HistoryThreadEvent(threadId, Reason.SWITCHED_TO);
    }

    public static HistoryThreadEvent createThreadReplacedAllEvent(int threadId) {
        return new HistoryThreadEvent(threadId, Reason.REPLACED_ALL);
    }

    private HistoryThreadEvent(int threadId, Reason reason) {
        this.threadId = threadId;
        this.reason = reason;
    }

    public static Type<HistoryThreadHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }


    @Override
    public Type<HistoryThreadHandler> getAssociatedType() {
        return getType();
    }

    @Override
    protected void dispatch(HistoryThreadHandler handler) {
        handler.onHistoryThreadChange(this);
    }

    public int getThreadId() {
        return this.threadId;
    }

    public Reason getReason() {
        return this.reason;
    }
}
