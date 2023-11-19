package de.marketmaker.iview.mmgwt.mmweb.client.events;

import java.util.ArrayList;

import de.marketmaker.iview.mmgwt.mmweb.client.push.PushRenderItem;

/**
 * Created on 10.02.2010 13:41:00
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public interface PushRegisterHandler extends PricesUpdatedHandler {
    /**
     * Components that want data to be pushed need to add the vwdcodes of the quotes to be
     * pushed to the event
     * @param event collects keys for data to be pushed
     */
    ArrayList<PushRenderItem> onPushRegister(PushRegisterEvent event);
}
