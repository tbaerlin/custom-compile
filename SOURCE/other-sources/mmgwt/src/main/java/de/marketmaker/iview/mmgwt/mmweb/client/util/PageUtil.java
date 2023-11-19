/*
 * PageUtil.java
 *
 * Created on 16.08.2012 13:29
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.iview.dmxml.MSCPageDisplay;

/**
 * @author Markus Dick
 */
public class PageUtil {
    /**
     * Appends the date/time string of the last update to the text of the page.
     * @throws IllegalArgumentException if display is null.
     */
    public static String toPageText(MSCPageDisplay display) {
        if(display == null) {
            throw new IllegalArgumentException("<toPageText> MSCPageDisplay parameter must not be null!"); //$NON-NLS$
        }

        String text = display.getText();
        if(text == null) {
            text = display.getFormattedText();
        }
        if(StringUtil.hasText(text)) {
            text += toLastUpdateDateTimeString(display);
        }

        return text;
    }

    private static String toLastUpdateDateTimeString(MSCPageDisplay display) {
        if(display == null || display.getLastUpdate() == null) {
            return "";
        }

        final JsDate date = GwtDateParser.getMmJsDate(display.getLastUpdate().trim());
        return "\n\n " + JsDateFormatter.formatDdmmyyyyHhmmss(date); // $NON-NLS$
    }
}
