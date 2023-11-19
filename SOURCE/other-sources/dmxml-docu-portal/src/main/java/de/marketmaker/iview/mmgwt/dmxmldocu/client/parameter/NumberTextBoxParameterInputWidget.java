/*
 * NumberTextBoxParameterInputWidget.java
 *
 * Created on 29.03.12 16:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class NumberTextBoxParameterInputWidget extends TextBoxParameterInputWidget {

    private Long minValue, maxValue;

    public NumberTextBoxParameterInputWidget(DmxmlBlockParameterDocumentation paramDocu) {
        super(paramDocu);
    }

    public NumberTextBoxParameterInputWidget(DmxmlBlockParameterDocumentation paramDocu,
            Long minValue, Long maxValue) {
        super(paramDocu);
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    protected boolean isValid(String val) {
        try {
            final long longValue = Long.parseLong(val); // TODO: decimal format?
            return (minValue == null || minValue <= longValue) &&
                    (maxValue == null || maxValue >= longValue);
        } catch (NumberFormatException e) {
            return false;
        }
    }

}
