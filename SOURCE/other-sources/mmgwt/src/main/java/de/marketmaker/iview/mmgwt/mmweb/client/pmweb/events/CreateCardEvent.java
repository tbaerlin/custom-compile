package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events;

import com.google.gwt.event.shared.GwtEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.InvestorItem;

/**
 * @author umaurer
 */
public class CreateCardEvent extends GwtEvent<CreateCardHandler> {
    private static Type<CreateCardHandler> TYPE;

    private final InvestorItem investor;

    public static Type<CreateCardHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<CreateCardHandler>();
        }
        return TYPE;
    }

    public static void fire(InvestorItem investor) {
        EventBusRegistry.get().fireEvent(new CreateCardEvent(investor));
    }

    public CreateCardEvent(InvestorItem investor) {
        this.investor = investor;
    }

    public InvestorItem getInvestor() {
        return investor;
    }

    @Override
    public Type<CreateCardHandler> getAssociatedType() {
        return TYPE;
    }

    @Override
    protected void dispatch(CreateCardHandler handler) {
        handler.createCard(this);
    }
}
