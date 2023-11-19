/*
* StringTool.java
*
* Created on 03.09.2012 15:06:23
*
* Copyright (c) vwd AG. All Rights Reserved.
*/
package de.marketmaker.istar.merger.provider.convensys;

import org.springframework.util.StringUtils;

import java.util.Date;

/**
 * @author Markus Dick
 */
public class StringTool {
    private final static Date NOW = new Date();

    /**
     * This method is used e.g. during Il Sole 24 Ore AMF processing,
     * to check if a data entry may be a table header.
     */
    public boolean isAllUpperCase(String value) {
        if(value == null) return false;

        for(int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            if(!Character.isUpperCase(c) && !Character.isWhitespace(c)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Interprets a number between NOW - 10 and NOW + 10 as a year.
     * A good guess for years e.g. in a balance sheet.
     */
    public boolean isYear(String value) {
        if(!StringUtils.hasText(value)) {
            return false;
        }

        if(value.length() == 4) {
            try {
                int i = Integer.parseInt(value);
                final int now = 1900 + NOW.getYear();
                if(now - 10 < i && i < now + 10) {
                    return true;
                }
            }
            catch(NumberFormatException e) {
                return false;
            }
        }
        return false;
    }

    public boolean isNumeric(String value) {
        if(!StringUtils.hasText(value)) {
            return false;
        }
        value = value.trim();

        boolean hadNumbers = false;

        for(int i = 0; i < value.length(); i++) {
            final char c = value.charAt(i);
            final boolean isDigit = Character.isDigit(c);

            hadNumbers = hadNumbers || isDigit;

            if(!(isDigit
                    || Character.isWhitespace(c)
                    || c == '.'
                    || c == ','
                    || c == '%'
                    || (i == 0 && c == '-' && value.length() > 1))) {
                return false;
            }
        }
        return hadNumbers;
    }

    public boolean isNumericButNotYear(String value) {
        return isNumeric(value) && !isYear(value);
    }
}
