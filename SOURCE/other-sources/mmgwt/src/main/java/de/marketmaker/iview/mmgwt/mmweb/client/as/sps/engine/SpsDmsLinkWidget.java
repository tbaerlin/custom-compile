/*
 * SpsDmsLinkWidget.java
 *
 * Created on 06.06.2014 07:49
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

/**
 * @author Markus Dick
 */
public class SpsDmsLinkWidget extends AbstractSpsAsyncLinkWidget {

    public SpsDmsLinkWidget() {
        super("sps-dmsLink"); // $NON-NLS$
    }

    @Override
    protected String getUrl(String handle) {
        return UrlBuilder.forPmReport("pmweb/dms?handle=" + handle).toURL();  // $NON-NLS$
    }
}
