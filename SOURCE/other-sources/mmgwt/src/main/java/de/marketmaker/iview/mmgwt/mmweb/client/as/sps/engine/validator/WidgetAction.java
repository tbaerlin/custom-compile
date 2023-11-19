package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
 * Created on 02.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *         <p/>
 *         With a WidgetAction one can manipulate a SpsWidget in context of validation, which is implemented
 *         in its subclasses {@link #doIt(de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget, ValidationResponse)} method.
 */
public abstract class WidgetAction {
    public abstract void doIt(SpsWidget widget, ValidationResponse response);

    public String toString() {
        return "WidgetAction{" + // $NON-NLS$
                "subclass=" + getClass().getSimpleName() + // $NON-NLS$
                '}';
    }

}
