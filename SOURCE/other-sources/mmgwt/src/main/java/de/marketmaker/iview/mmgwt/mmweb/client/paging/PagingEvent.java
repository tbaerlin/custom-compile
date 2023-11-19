/*
 * PagingEvent.java
 *
 * Created on 31.03.2008 17:50:45
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.paging;

/**
 * @author Ulrich Maurer
 */
public class PagingEvent {
    public interface Callback {
        void handle(PagingEvent event);
        void setPageSize(int entries);
    }

    public enum Action {
        FIRST, PREVIOUS, SPECIFIED, NEXT, LAST, REFRESH
    }

    private static int counter = 0;

    private int id;

    private Action action;

    private int page;

    public PagingEvent(Action action) {
        this.id = counter++;
        if (action == Action.SPECIFIED) {
            throw new IllegalArgumentException("page must be specified, when action == Action.SPECIFIED"); // $NON-NLS-0$
        }
        this.action = action;
        this.page = -1;
    }

    public PagingEvent(Action action, int page) {
        this.id = counter++;
        this.action = action;
        this.page = page;
    }

    public Action getAction() {
        return this.action;
    }

    public int getPage() {
        return page;
    }

    public String toString() {
        return "PagingEvent[" + this.id + ", " // $NON-NLS-0$ $NON-NLS-1$
                + (this.action == Action.SPECIFIED
                    ? Action.SPECIFIED + " (" + this.page + ")" : this.action) + "]"; // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
    }
}
