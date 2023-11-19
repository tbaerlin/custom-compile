package de.marketmaker.itools.gwtutil.client.i18n;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Messages;

/**
 * @author Ulrich Maurer
 *         Date: 09.06.11
 */
public interface GwtUtilI18n extends Messages {
    GwtUtilI18n I = (GwtUtilI18n) GWT.create(GwtUtilI18n.class);

    String monthNames();
    String monthNamesShort();
    String dayNames1();
    String today();
    String days();
    String weeks();
    String months();
    String years();
    String alltime();
    String ok();
    String cancel();
    String period();
    String beforeEarliestDate(String p0);
    String afterLatestDate(String p0);
    String invalidFormat(String p0);
}
