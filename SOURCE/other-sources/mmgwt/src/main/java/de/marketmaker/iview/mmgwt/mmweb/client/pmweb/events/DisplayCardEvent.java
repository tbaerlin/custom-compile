package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCard;

/**
 * @author umaurer
 */
public class DisplayCardEvent extends GwtEvent<DisplayCardHandler> {
    private static Type<DisplayCardHandler> TYPE;

    private PmReportCard card;

    public static Type<DisplayCardHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<DisplayCardHandler>();
        }
        return TYPE;
    }

    public static void fire(PmReportCard card) {
        EventBusRegistry.get().fireEvent(new DisplayCardEvent(card));
    }

    public DisplayCardEvent(PmReportCard card) {
        this.card = card;
    }

    public PmReportCard getCard() {
        return this.card;
    }

    @Override
    public Type<DisplayCardHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(DisplayCardHandler handler) {
        handler.displayCard(this);
    }
}
