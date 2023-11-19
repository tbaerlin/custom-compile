/*
 * DmxmlDocu.java
 *
 * Created on 07.09.2012 13:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.ui.RootLayoutPanel;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.LoggedInEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.LoggedInHandler;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.ioc.DmxmlDocuFactory;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.data.User;

/**
 * @author Markus Dick
 */
public class DmxmlDocu implements EntryPoint {
    @Override
    public void onModuleLoad() {
        final DmxmlDocuFactory factory = new DmxmlDocuFactory();

        //Initializing the history is necessary, because otherwise a first click
        //on the Documentation link in the footer will cause a strange navigation
        //to the blocks overview tab.
        String initToken = History.getToken();
        if (initToken.length() == 0) {
            History.newItem("");  //$NON-NLS$
        }

        factory.getEventBus().addHandler(LoggedInEvent.TYPE, new LoggedInHandler() {
            public void onLoggedIn(User u) {
                factory.getDmxmlDocuController().go(RootLayoutPanel.get());
            }
        });

        factory.getLoginController().go(RootLayoutPanel.get());
    }
}
