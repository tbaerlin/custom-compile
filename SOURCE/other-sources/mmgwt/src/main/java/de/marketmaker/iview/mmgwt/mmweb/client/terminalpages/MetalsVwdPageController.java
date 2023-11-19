package de.marketmaker.iview.mmgwt.mmweb.client.terminalpages;

import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;

/**
 * Created on 18.05.2010 08:27:54
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class MetalsVwdPageController extends VwdPageController {
    
    public MetalsVwdPageController(ContentContainer contentContainer) {
        super(contentContainer);
    }

    @Override
    protected boolean withCurrency() {
        return true;
    }
}
