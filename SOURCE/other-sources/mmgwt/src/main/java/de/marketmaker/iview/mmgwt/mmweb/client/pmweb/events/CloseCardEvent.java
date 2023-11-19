package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.card.PmReportCard;

/**
 * @author umaurer
 */
public class CloseCardEvent extends GwtEvent<CloseCardHandler> {
    private static Type<CloseCardHandler> TYPE;

    private PmReportCard card;

    public static Type<CloseCardHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<CloseCardHandler>();
        }
        return TYPE;
    }

    public static void fire(PmReportCard card) {
        EventBusRegistry.get().fireEvent(new CloseCardEvent(card));
    }

    public CloseCardEvent(PmReportCard card) {
        this.card = card;
    }

    public PmReportCard getCard() {
        return this.card;
    }

    @Override
    public Type<CloseCardHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CloseCardHandler handler) {
        handler.closeCard(this);
    }
}
