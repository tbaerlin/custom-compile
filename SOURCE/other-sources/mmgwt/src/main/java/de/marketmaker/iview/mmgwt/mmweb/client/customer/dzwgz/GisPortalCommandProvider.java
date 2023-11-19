/*
 * GisPortalCommandProvider.java
 *
 * Created on 14.06.2016 16:56
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.customer.dzwgz;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;


import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.logging.Logger;

/**
 * In GIN/GUICE providers produce a value each time when the get() method is invoked.
 * @author mdick
 */
@Singleton
public class GisPortalCommandProvider implements Provider<OpenGisPortalCommand> {
    private FeatureFlags featureFlags;

    private SessionData sessionData;

    private Logger logger;

    @Override
    public OpenGisPortalCommand get() {
        if (this.featureFlags.isEnabled0(FeatureFlags.Feature.DZ_RELEASE_2016)) {

            final OpenGisPortalXhrBasicAuthPreFlightMethod.PortalUrl portalUrl;
            if (this.sessionData.isUserPropertyTrue("useGisPortalTestUrl")) {  // $NON-NLS$
                portalUrl = OpenGisPortalXhrBasicAuthPreFlightMethod.PortalUrl.TEST;
            }
            else {
                portalUrl = OpenGisPortalXhrBasicAuthPreFlightMethod.PortalUrl.PROD;
            }

            return new OpenGisPortalXhrBasicAuthPreFlightMethod(
                    portalUrl,
                    this.sessionData.getUser()::getGisPortalHttpBasicAuthCredentials,
                    EventBusRegistry.get(),
                    AbstractMainController.INSTANCE::showError,
                    this.logger
                    );
        }
        else {
            return new OpenGisPortalMethod();
        }
    }

    @Inject
    public void setFeatureFlags(FeatureFlags featureFlags) {
        this.featureFlags = featureFlags;
    }

    @Inject
    public void setSessionData(SessionData sessionData) {
        this.sessionData = sessionData;
    }

    @Inject
    public void setLogger(Logger logger) {
        this.logger = logger;
    }
}
