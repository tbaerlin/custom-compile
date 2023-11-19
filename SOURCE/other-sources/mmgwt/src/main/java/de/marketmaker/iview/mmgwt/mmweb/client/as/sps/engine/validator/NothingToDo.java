package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
 * Created on 02.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class NothingToDo extends WidgetAction {

    public final static NothingToDo INSTANCE = new NothingToDo();

    private NothingToDo() {
        //nothing to do
    }

    @Override
    public void doIt(SpsWidget widget, ValidationResponse response) {
        //nothing to do
    }
}
