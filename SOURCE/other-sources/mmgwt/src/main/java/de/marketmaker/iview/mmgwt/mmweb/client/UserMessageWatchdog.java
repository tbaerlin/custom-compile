/*
 * UserMessageWatchdog.java
 *
 * Created on 07.08.2008 11:01:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.event.shared.HandlerManager;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMessageWatchdog extends AbstractMessageWatchdog {
    public UserMessageWatchdog(HandlerManager eventBus) {
        super(eventBus);
    }

    @Override
    protected String getPropertyKey() {
        return AppConfig.PROP_KEY_MSGID;
    }

    @Override
    protected String getTitle() {
        return I18n.I.newUserMessage(); 
    }

    @Override
    protected String getMessage() {
        return I18n.I.newUserMessageArrived(); 
    }

    @Override
    protected String getDirectAccessItem() {
        return "N_BN"; // $NON-NLS-0$
    }
}
