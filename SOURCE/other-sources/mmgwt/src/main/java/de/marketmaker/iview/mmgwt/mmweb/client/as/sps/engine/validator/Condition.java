package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;

/**
 * Created on 27.05.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 *         <p/>
 *         A Condition checks the values of one or n SpsProperties. Subclasses need to implement {@link #complied()} to do so.
 *         If the complied returns true, the {@link de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.Validator}
 *         triggers a defined {@link de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator.WidgetAction}.
 */
public abstract class Condition {
    protected final SpsLeafProperty sourceProperty;

    protected Condition(SpsLeafProperty sourceProperty) {
        this.sourceProperty = sourceProperty;
    }

    abstract boolean complied();

    public SpsLeafProperty getSourceProperty() {
        return sourceProperty;
    }

    @Override
    public String toString() {
        return "Condition{" + // $NON-NLS$
                "sourceProperty.stringValue=" + sourceProperty.getStringValue() + // $NON-NLS$
                '}';
    }
}