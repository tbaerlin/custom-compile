package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.validator;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;

/**
 * Created on 03.06.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class StringCompare extends Condition {

    protected final String checkValue;
    protected final boolean not;

    protected StringCompare(String checkValue, SpsLeafProperty spsProperty, boolean not) {
        super(spsProperty);
        this.checkValue = checkValue;
        this.not = not;
    }

    public static StringCompare createEqual(String value, BindToken bindToken, Context context) {
        final SpsLeafProperty leafProperty = create(bindToken, context);
        return new StringCompare(value, leafProperty, false);
    }

    public static StringCompare createUnEqual(String value, BindToken bindToken, Context context) {
        final SpsLeafProperty leafProperty = create(bindToken, context);
        return new StringCompare(value, leafProperty, true);
    }

    private static SpsLeafProperty create(BindToken bindToken, Context context) {
        final SpsProperty spsProperty = context.getRootProp().get(bindToken.getHead());
        if (!(spsProperty instanceof SpsLeafProperty)) {
            throw new IllegalArgumentException("property of bind " + bindToken + " is not an instanceof SpsLeadProperty! Is: " + spsProperty.getClass());  // $NON-NLS$
        }
        return (SpsLeafProperty) spsProperty;
    }


    @Override
    boolean complied() {
        final String propValue = this.sourceProperty.getStringValue();
        if (this.checkValue == null && this.sourceProperty.getDataItem() == null) {
            return true;
        }
        if (this.checkValue == null) {
            return this.not;
        }
        final boolean equals = this.checkValue.equals(propValue);
        return this.not ? !equals : equals;
    }

    @Override
    public String toString() {
        return "StringCompare{" + // $NON-NLS$
                "sourceProperty.stringValue=" + this.sourceProperty.getStringValue() + '\'' + // $NON-NLS$
                "checkValue='" + this.checkValue + '\'' + // $NON-NLS$
                ", not=" + this.not + // $NON-NLS$
                '}';
    }
}
