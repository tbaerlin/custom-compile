/*
 * ParameterGuiFactory.java
 *
 * Created on 28.03.12 14:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.parameter;

import java.util.Arrays;
import java.util.List;

import de.marketmaker.iview.dmxmldocu.DmxmlBlockParameterDocumentation;
import de.marketmaker.iview.dmxmldocu.MmValidator;

/**
 * @author Sebastian Wild (s.wild@market-maker.de)
 */
public class ParameterGuiFactory {
    public static ParameterInputWidget newInputWidgetFor(
            DmxmlBlockParameterDocumentation paramDocu) {
        final AbstractParameterInputWidget<?> result;
        switch (paramDocu.getGuiType()) {
            case STRING:
            case DATE_TIME:
            case LOCAL_DATE:
            case TIME_PERIOD:
            case BIG_DECIMAL:
            case DOUBLE:
            case BOOLEAN:
            case OTHER:
                final List<String> listValues = getRestrictedSet(paramDocu);
                if (listValues != null) {
                    result = new ListBoxParameterInputWidget(paramDocu) {
                        @Override
                        List<String> getListValues(DmxmlBlockParameterDocumentation paramDocu) {
                            return listValues;
                        }
                    };
                }
                else {
                    result = new TextBoxParameterInputWidget(paramDocu);
                }
                break;
            case LONG:
                result = new NumberTextBoxParameterInputWidget(paramDocu);
                break;
            case INTEGER:
                result = new NumberTextBoxParameterInputWidget(paramDocu, (long) Integer.MIN_VALUE,
                        (long) Integer.MAX_VALUE);
                break;
            case ENUM:
                result = new ListBoxParameterInputWidget(paramDocu) {
                    @Override
                    List<String> getListValues(DmxmlBlockParameterDocumentation paramDocu) {
                        final List<String> restrictedSet = getRestrictedSet(paramDocu);
                        return null == restrictedSet ?
                                paramDocu.getEnumValues().getEnumValue()
                                : restrictedSet;
                    }
                };
                break;
            default:
                throw new IllegalStateException();
        }
        result.initComponents();
        return result;
    }

    private static List<String> getRestrictedSet(DmxmlBlockParameterDocumentation paramDocu) {
        final DmxmlBlockParameterDocumentation.Validators validators = paramDocu.getValidators();
        if (validators == null || validators.getValidator() == null || validators.getValidator().isEmpty()) {
            return null;
        }
        for (MmValidator validator : validators.getValidator()) {
            if ("RestrictedSet".equals(validator.getValidatorType())) {  // $NON-NLS$
                for (MmValidator.Parameters.Parameter parameter : validator.getParameters().getParameter()) {
                    if ("value".equals(parameter.getName())) { // $NON-NLS$
                        final String value = parameter.getValue().replace("\"", ""); // $NON-NLS$
                        return Arrays.asList(value.split(","));
                    }
                }
            }
        }
        return null;
    }

}
