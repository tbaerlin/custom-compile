/*
 * DmxmlDocuFactory.java
 *
 * Created on 07.09.2012 13:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.ioc;

import com.google.gwt.event.shared.HandlerManager;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.DmxmlDocuController;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.DmxmlDocuServiceAsyncProxy;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.login.LoginController;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.login.LoginView;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.presenter.AboutPresenter;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.ui.AboutDialog;

/**
 * @author mdick
 */
public class DmxmlDocuFactory {
    private final HandlerManager eventBus = new HandlerManager(null);

    private final DmxmlDocuController dmxmlDocuController = new DmxmlDocuController(eventBus);
    private final AboutPresenter aboutPresenter = new AboutPresenter(eventBus, new AboutDialog());

    public HandlerManager getEventBus() {
        return eventBus;
    }

    public DmxmlDocuController getDmxmlDocuController() {
        return dmxmlDocuController;
    }

    public LoginController getLoginController() {
        return new LoginController(eventBus, DmxmlDocuServiceAsyncProxy.INSTANCE, new LoginView());
    }

    public AboutPresenter getAboutPresenter() {
        return aboutPresenter;
    }
}
