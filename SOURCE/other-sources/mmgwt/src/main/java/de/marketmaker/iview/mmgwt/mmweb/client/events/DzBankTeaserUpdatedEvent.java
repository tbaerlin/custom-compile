/*
 * DzBankTeaserUpdatedEvent.java
 *
 * Created on 09.02.2016 07:47:23
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.events;

import com.google.gwt.event.shared.GwtEvent;

import de.marketmaker.iview.mmgwt.mmweb.client.workspace.TeaserConfigData;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * Informs handlers about an updated DZ BANK teaser.
 * @author mdick
 */
@NonNLS
public class DzBankTeaserUpdatedEvent extends GwtEvent<DzBankTeaserUpdatedHandler> {
    private static Type<DzBankTeaserUpdatedHandler> TYPE;

    private final TeaserConfigData teaserConfigData;

    public static Type<DzBankTeaserUpdatedHandler> getType() {
        if (TYPE == null) {
            TYPE = new Type<>();
        }
        return TYPE;
    }

    public static void fire(TeaserConfigData teaserConfigData) {
        EventBusRegistry.get().fireEvent(new DzBankTeaserUpdatedEvent(teaserConfigData));
    }

    public DzBankTeaserUpdatedEvent(TeaserConfigData teaserConfigData) {
        this.teaserConfigData = teaserConfigData;
    }

    public TeaserConfigData getTeaserConfigData() {
        return teaserConfigData;
    }

    public Type<DzBankTeaserUpdatedHandler> getAssociatedType() {
        return TYPE;
    }

    protected void dispatch(DzBankTeaserUpdatedHandler handler) {
        handler.onDzBankTeaserUpdated(this);
    }

    @Override
    public String toString() {
        return "DzBankTeaserUpdatedEvent{" +
                "teaserConfigData=" + teaserConfigData +
                "} " + super.toString();
    }
}
