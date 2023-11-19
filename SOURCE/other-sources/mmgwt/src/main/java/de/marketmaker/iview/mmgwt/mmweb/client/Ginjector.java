/*
 * Ginjector.java
 *
 * Created on 3/31/16 4:58 PM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.inject.client.GinModules;

import de.marketmaker.iview.mmgwt.mmweb.client.as.SouthPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.OpenGisPortalCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderBNDIssuer;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.AppName;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;

/**
 * @author kmilyut
 */
@GinModules(GinModule.class)
public interface Ginjector extends com.google.gwt.inject.client.Ginjector {

    Ginjector INSTANCE = GWT.create(Ginjector.class);

    void injectIntoEntryPoint(MmwebEntryPoint entryPoint);

    FeatureFlags getFeatureFlags();

    Logger getLogger();

    SessionData getSessionData();

    OpenGisPortalCommand getOpenGisPortalCommand();

    LiveFinderBNDIssuer getLiveFinderBNDIssuer();

    SouthPanel getSouthPanel();

    Permutation getPermutation();

    @AppName
    String getAppName();

    MainControllerInitializerCommand getMainControllerInitializer();

    SnippetInitializerCommand getSnippetInitializer();
}
