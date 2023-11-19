package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import com.google.gwt.event.shared.GwtEvent;

/**
 * Created on 19.10.11 16:19
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class FinderFormElementEvent extends GwtEvent<FinderFormElementEventHandler> {
    private static Type<FinderFormElementEventHandler> TYPE;
    public enum Action {
        CLONE,
        DELETE
    }

    public static Type<FinderFormElementEventHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<FinderFormElementEventHandler>();
        }
        return TYPE;
    }

    private final String id;
    private final Action action;


    public static FinderFormElementEvent newClone(String parentId) {
        return new FinderFormElementEvent(FinderFormElementEvent.Action.CLONE,  parentId);
    }

    public static FinderFormElementEvent cloneDeleted(String id) {
        return new FinderFormElementEvent(FinderFormElementEvent.Action.DELETE, id);
    }

    private FinderFormElementEvent(Action action, String id) {
        this.id = id;
        this.action = action;
    }

    public String getId() {
        return id;
    }

    public Action getAction() {
        return action;
    }

    public Type<FinderFormElementEventHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(FinderFormElementEventHandler elementClonedHandler) {
        elementClonedHandler.onElementCloneAction(this);
    }

}
