/*
 * SpsSecurityDocumentsLinkWidget.java
 *
 * Created on 16.03.2018 12:53
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import de.marketmaker.iview.mmgwt.mmweb.client.util.UrlBuilder;

/**
 * @author Markus Dick
 */
public class SpsSecurityDocumentsLinkWidget extends AbstractSpsAsyncLinkWidget {

    public SpsSecurityDocumentsLinkWidget() {
        super("sps-secDocsLink");  // $NON-NLS$ 
    }

    @Override
    protected String getUrl(String handle) {
        return UrlBuilder.forPmReport("pmweb/securityDocuments?handle=" + handle).toURL();  // $NON-NLS$
    }
}
