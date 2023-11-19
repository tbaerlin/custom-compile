package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;

/**
 * YieldCurveController.java
 * Created on Feb 3, 2009 11:45:08 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class YieldCurveController extends DelegatingPageController {
    private static final String DEF = "bnd_yieldcurve"; // $NON-NLS-0$

    public YieldCurveController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), DEF);
    }
}
