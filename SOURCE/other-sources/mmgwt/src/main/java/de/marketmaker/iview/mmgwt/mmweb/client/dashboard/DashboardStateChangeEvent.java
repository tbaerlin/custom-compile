package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;

/**
 * Author: umaurer
 * Created: 03.06.15
 */
public class DashboardStateChangeEvent extends GwtEvent<DashboardStateChangeHandler> {
    private static Type<DashboardStateChangeHandler> TYPE;

    public enum Action {
        EDIT_SELECTED, EDIT_NEW, EDIT_CLONE, CANCEL_EDIT, SAVE_EDIT, UPDATE;

        public void fire(String id) {
            EventBusRegistry.get().fireEvent(new DashboardStateChangeEvent(id, this));
        }
    }

    private final String id;
    private final Action action;

    public static Type<DashboardStateChangeHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<DashboardStateChangeHandler>();
        }
        return TYPE;
    }

    public DashboardStateChangeEvent(String id, Action action) {
        this.id = id;
        this.action = action;
    }

    public String getId() {
        return this.id;
    }

    public Action getAction() {
        return this.action;
    }

    @Override
    public Type<DashboardStateChangeHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DashboardStateChangeHandler handler) {
        handler.onStateChanged(this);
    }

}
