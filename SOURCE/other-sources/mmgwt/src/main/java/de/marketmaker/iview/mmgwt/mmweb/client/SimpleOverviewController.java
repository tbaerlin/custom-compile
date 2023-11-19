/*
 * SimpleOverviewController.java
 *
 * Created on 19.09.2008 19:00:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetsFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * @author Ulrich Maurer
 */
public class SimpleOverviewController extends DelegatingPageController {
    private final String def;

    public SimpleOverviewController(ContentContainer contentContainer, final String def) {
        super(contentContainer);
        this.def = def;
    }

    protected void initDelegate() {
        this.delegate = SnippetsFactory.createFlexController(getContentContainer(), this.def);
    }
}
