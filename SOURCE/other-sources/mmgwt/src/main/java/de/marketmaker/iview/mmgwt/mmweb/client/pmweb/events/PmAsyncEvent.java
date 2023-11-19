package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.AsyncData;

/**
 * @author umaurer
 */
public class PmAsyncEvent extends GwtEvent<PmAsyncHandler> {
    private static Type<PmAsyncHandler> TYPE;
    private final AsyncData asyncData;


    public static Type<PmAsyncHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<PmAsyncHandler>();
        }
        return TYPE;
    }

    public static void fire(AsyncData asyncData) {
        EventBusRegistry.get().fireEvent(new PmAsyncEvent(asyncData));
    }

    public PmAsyncEvent(AsyncData asyncData) {
        this.asyncData = asyncData;
    }

    public AsyncData getAsyncData() {
        return asyncData;
    }

    @Override
    public Type<PmAsyncHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(PmAsyncHandler handler) {
        handler.onAsync(this);
    }
}
