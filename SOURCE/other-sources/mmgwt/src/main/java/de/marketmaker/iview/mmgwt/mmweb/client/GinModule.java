/*
 * GinModule.java
 *
 * Created on 2/16/16 12:57 PM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;

import de.marketmaker.iview.mmgwt.mmweb.client.as.SouthPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.GisPortalCommandProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz.OpenGisPortalCommand;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.FirebugLoggerAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.AppName;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.AppNameProvider;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.Permutation;
import de.marketmaker.iview.mmgwt.mmweb.client.runtime.PermutationProvider;

/**
 * @author kmilyut
 */
public class GinModule extends AbstractGinModule {
    @Override
    protected void configure() {
        bind(SessionData.class).in(Singleton.class);
        requestStaticInjection(SessionData.class);

        bind(Logger.class).to(FirebugLoggerAdapter.class).in(Singleton.class);

        bind(Permutation.class).toProvider(PermutationProvider.class).in(Singleton.class);

        bind(String.class).annotatedWith(AppName.class).toProvider(AppNameProvider.class).in(Singleton.class);

        bind(OpenGisPortalCommand.class).toProvider(GisPortalCommandProvider.class);

        bind(SouthPanel.class).in(Singleton.class);
    }

    @Provides
    public MainControllerInitializerCommand provideMainControllerInitializer(Ginjector ginjector) {
        return new MainControllerInitializerCommand(AbstractMainController.INSTANCE, ginjector);
    }

    @Provides
    public SnippetInitializerCommand provideSnippetInitializerCommand() {
        return new SnippetInitializerCommand();
    }
}
