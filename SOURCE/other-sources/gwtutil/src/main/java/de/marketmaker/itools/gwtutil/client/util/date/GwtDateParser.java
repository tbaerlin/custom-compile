package de.marketmaker.itools.gwtutil.client.util.date;

import de.marketmaker.itools.gwtutil.client.i18n.LocalizedFormatter;

/**
 * @author Ulrich Maurer
 *         Date: 20.02.12
 */
public class GwtDateParser extends CsDateParser {
    private static final int CENTURY = (new JsDate().getFullYear() / 100) * 100;
    private static final String DATE_SEPARATOR = String.valueOf(LocalizedFormatter.getInstance().getDateSeparator());

    private GwtDateParser(String s, Msg msg) {
        super(s, msg, DATE_SEPARATOR, initializeYmdHms());
    }

    private static String[] initializeYmdHms() {
        final MmJsDate date = new MmJsDate();
        return new String[]{String.valueOf(date.getFullYear()), String.valueOf(date.getMonth() + 1), String.valueOf(date.getDate()), "0", "0", "0", "0", "0", "0"};
    }

    public static String[] getYmdHms(String s) {
        return getYmdHms(s, MSG);
    }

    public static String[] getYmdHms(String s, Msg msg) {
        return new GwtDateParser(s, msg).getYmdHms();
    }

    public static int[] getYmdHmsInt(String s) {
        return getYmdHmsInt(s, MSG);
    }

    public static int[] getYmdHmsInt(String s, Msg msg) {
        return getYmdHmsInt(new GwtDateParser(s, msg).getYmdHms());
    }

    public static MmJsDate getMmJsDate(String s) {
        return getMmJsDate(s, MSG);
    }

    public static MmJsDate getMmJsDate(String s, Msg msg) {
        final int[] ymdHmsInt = getYmdHmsInt(s, msg);
        return new MmJsDate(
                ymdHmsInt[ID_YEAR] < 100 ? (ymdHmsInt[ID_YEAR] + CENTURY) : ymdHmsInt[ID_YEAR],
                ymdHmsInt[ID_MONTH] - 1,
                ymdHmsInt[ID_DAY],
                ymdHmsInt[ID_HOURS],
                ymdHmsInt[ID_MINUTES],
                ymdHmsInt[ID_SECONDS]);
    }

    public static String formatIso8601(String s) {
        return JsDateFormatter.formatIso8601(getMmJsDate(s));
    }
}