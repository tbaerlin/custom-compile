package de.marketmaker.itools.gwtutil.client.util.date;

import de.marketmaker.itools.gwtutil.client.i18n.GwtUtilI18n;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * DateParser functionality that can be used on server side as well as in GWT client.
 *
 * Author: umaurer
 * Created: 24.04.14
 */
public class CsDateParser {
    static final int ID_YEAR = 0;
    static final int ID_MONTH = 1;
    static final int ID_DAY = 2;
    static final int ID_HOURS = 3;
    static final int ID_MINUTES = 4;
    static final int ID_SECONDS = 5;
    static final int ID_MILLIS = 6;
    static final int ID_ZONE_HOURS = 7;
    static final int ID_ZONE_MINUTES = 8;

    private static DateSpec dateSpecInstance = null;

    public interface Msg {
        String invalidDateFormat(String invalidDate);
    }
    public static final Msg MSG = new Msg() {
        @Override
        public String invalidDateFormat(String invalidDate) {
            return GwtUtilI18n.I.invalidFormat(invalidDate);
        }
    };
    private final Msg msg;

    private String[] ymdHms;

    enum Mode {
        NUMBER, DIVIDER
    }

    CsDateParser(String s, Msg msg, String localizedDateSeparator, String... ymdHms) {
        this.msg = msg;
        if (ymdHms.length == 9) {
            this.ymdHms = ymdHms;
        }
        else {
            this.ymdHms = new String[]{"2000", "1", "1", "0", "0", "0", "0", "0", "0"};
            final int len = ymdHms.length < 9 ? ymdHms.length : 9;
            System.arraycopy(ymdHms, 0, this.ymdHms, 0, len);
        }

        final ArrayList<String> list = split(s);
        evaluate(s, list, localizedDateSeparator);
    }

    private ArrayList<String> split(String s) {
        final ArrayList<String> list = new ArrayList<>();

        Mode mode = Mode.NUMBER;
        int start = 0;
        for (int pos = 0; pos < s.length(); pos++) {
            final char c = s.charAt(pos);
            if (mode == Mode.NUMBER) {
                if (!isNumber(c)) {
                    list.add(s.substring(start, pos));
                    mode = Mode.DIVIDER;
                    start = pos;
                }
            }
            else {
                if (isNumber(c)) {
                    list.add(s.substring(start, pos));
                    mode = Mode.NUMBER;
                    start = pos;
                }
            }
        }
        list.add(s.substring(start, s.length()));
        return list;
    }

    private boolean isNumber(char c) {
        return c >= '0' && c <='9';
    }

    static class DateSpec {
        private int[] ymdHmsIndex = null;
        private HashMap<String, DateSpec> mapDateSpecs;

        DateSpec getOrCreateDateSpec(String divider) {
            if (this.mapDateSpecs == null) {
                this.mapDateSpecs = new HashMap<>();
            }
            else {
                final DateSpec dateSpec = mapDateSpecs.get(divider);
                if (dateSpec != null) {
                    return dateSpec;
                }
            }
            final DateSpec dateSpec = new DateSpec();
            this.mapDateSpecs.put(divider, dateSpec);
            return dateSpec;
        }

        DateSpec getDateSpec(String divider) {
            if (this.mapDateSpecs == null) {
                return null;
            }
            return this.mapDateSpecs.get(divider);
        }

        void setYmdHmsIndex(int... ymdHmsIndex) {
            this.ymdHmsIndex = ymdHmsIndex;
        }

        public int[] getYmdHmsIndex() {
            return ymdHmsIndex;
        }
    }

    private static DateSpec getDateSpec() {
        if (dateSpecInstance != null) {
            return dateSpecInstance;
        }

        dateSpecInstance = new DateSpec();

        // ISO formats
        addDateSpec(new String[]{"-", "-", "T", ":", ":", ".", "+", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_MILLIS, ID_ZONE_HOURS, ID_ZONE_MINUTES});
        addDateSpec(new String[]{"-", "-", "T", ":", ":", ".", "-", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_MILLIS, ID_ZONE_HOURS, ID_ZONE_MINUTES});
        addDateSpec(new String[]{"-", "-", "T", ":", ":", ".", "+"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_MILLIS, ID_ZONE_HOURS});
        addDateSpec(new String[]{"-", "-", "T", ":", ":", "+", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_ZONE_HOURS, ID_ZONE_MINUTES});
        addDateSpec(new String[]{"-", "-", "T", ":", ":", "-", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_ZONE_HOURS, ID_ZONE_MINUTES});
        addDateSpec(new String[]{"-", "-", "T", ":", ":", "+"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_ZONE_HOURS});
        addDateSpec(new String[]{"-", "-", "T", ":", ":", "-"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS, ID_ZONE_HOURS});
        addDateSpec(new String[]{"-", "-", "T", ":", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS});
        addDateSpec(new String[]{"-", "-", " ", ":", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS});
        addDateSpec(new String[]{"-", "-", "T", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES});
        addDateSpec(new String[]{"-", "-", " ", ":"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES});
        addDateSpec(new String[]{"-", "-"}, new int[]{ID_YEAR, ID_MONTH, ID_DAY});

        // english formats
        addDateSpec(new String[]{"/", "/", " ", ":", ":"}, new int[]{ID_MONTH, ID_DAY, ID_YEAR, ID_HOURS, ID_MINUTES, ID_SECONDS});
        addDateSpec(new String[]{"/", "/", " ", ":"}, new int[]{ID_MONTH, ID_DAY, ID_YEAR, ID_HOURS, ID_MINUTES});
        addDateSpec(new String[]{"/", "/"}, new int[]{ID_MONTH, ID_DAY, ID_YEAR});
        addDateSpec(new String[]{"/", " ", ":", ":"}, new int[]{ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES, ID_SECONDS});
        addDateSpec(new String[]{"/", " ", ":"}, new int[]{ID_MONTH, ID_DAY, ID_HOURS, ID_MINUTES});
        addDateSpec(new String[]{"/"}, new int[]{ID_MONTH, ID_DAY});

        // german formats
        addDateSpec(new String[]{".", ".", " ", ":", ":"}, new int[]{ID_DAY, ID_MONTH, ID_YEAR, ID_HOURS, ID_MINUTES, ID_SECONDS});
        addDateSpec(new String[]{".", ".", " ", ":"}, new int[]{ID_DAY, ID_MONTH, ID_YEAR, ID_HOURS, ID_MINUTES});
        addDateSpec(new String[]{".", "."}, new int[]{ID_DAY, ID_MONTH, ID_YEAR});
        addDateSpec(new String[]{".", ". ", ":", ":"}, new int[]{ID_DAY, ID_MONTH, ID_HOURS, ID_MINUTES, ID_SECONDS});
        addDateSpec(new String[]{".", ". ", ":"}, new int[]{ID_DAY, ID_MONTH, ID_HOURS, ID_MINUTES});
        return dateSpecInstance;
    }

    private static void addDateSpec(String[] dividers, int[] ids) {
        assert (dividers.length + 1 == ids.length);

        DateSpec dateSpec = dateSpecInstance;
        for (String divider : dividers) {
            dateSpec = dateSpec.getOrCreateDateSpec(divider);
        }
        dateSpec.setYmdHmsIndex(ids);
    }

    private void evaluate(String s, final ArrayList<String> alist, String localizedDateSeparator) {
        final ArrayList<String> list = new ArrayList<>(alist);

        //Special handling for PM dateFormat behaviour if a date is given without separators.
        if(list.size() == 1 && list.get(0).length() == 6 || list.get(0).length() == 8) {

            final String firstEntry = list.get(0);
            list.clear();
            list.add(firstEntry.substring(0, 2));
            list.add(localizedDateSeparator);
            list.add(firstEntry.substring(2,4));
            list.add(localizedDateSeparator);
            String yearString = firstEntry.substring(4, firstEntry.length());
            if(yearString.length() == 2) {
                final int currentCenturyPrefix = new MmJsDate().getFullYear() / 100;
                if('5' <= yearString.charAt(0)) { //decade of year >= 50 e.g. 19xx
                    yearString = Integer.toString(currentCenturyPrefix - 1) + yearString;
                }
                else { //decade of year < 50 e.g. 20xx
                    yearString = Integer.toString(currentCenturyPrefix) + yearString;
                }
            }
            list.add(yearString);
        }
        else if (list.size() < 3) {
            throw new IllegalArgumentException(this.msg.invalidDateFormat(s));
        }
        final String s1 = list.get(1);
        // special handling for english format with short month name: Mar/15/2012
        if (list.get(0).isEmpty() && s1.length() == 4 && s1.endsWith("/")) {
            list.set(0, String.valueOf(JsDateFormatter.getMonthNumberEn(s1.substring(0, s1.length() - 1))));
            list.set(1, "/");
        }

        // special handling for german format dd.MM.
        if (list.size() == 4 && ".".equals(list.get(1)) && ".".equals(list.get(3))) {
            list.add(String.valueOf(ymdHms[ID_YEAR]));
        }

        if (list.size() % 2 != 1) {
            if (!"Z".equals(list.get(list.size()-1))) {
                throw new IllegalArgumentException(this.msg.invalidDateFormat(s));
            } else {
                // Chapter 5.4 of ISO 8601, syntax is [-]CCYY-MM-DDThh:mm:ss[Z|(+|-)hh:mm]
                // let's rewrite 'Z' as '+00:00'
                list.set(list.size() - 1, "+");
                list.add("00");
                list.add(":");
                list.add("00");
            }
        }
        DateSpec dateSpec = getDateSpec();
        for (int i = 1; i < list.size(); i += 2) {
            dateSpec = dateSpec.getDateSpec(list.get(i));
            if (dateSpec == null) {
                throw new IllegalArgumentException(this.msg.invalidDateFormat(s));
            }
        }
        final int[] ymdHmsIndex = dateSpec.getYmdHmsIndex();
        if (ymdHmsIndex == null) {
            throw new IllegalArgumentException(this.msg.invalidDateFormat(s));
        }
        assert list.size() == ymdHmsIndex.length * 2 - 1 : "invalid list size: " + list.size();
        for (int i = 0; i < ymdHmsIndex.length; i++) {
            this.ymdHms[ymdHmsIndex[i]] = list.get(i * 2);
        }
        if (this.ymdHms[ID_ZONE_HOURS].length() > 2) { // if zone part is like "+0100" instead of "+01:00"
            this.ymdHms[ID_ZONE_MINUTES] = this.ymdHms[ID_ZONE_HOURS].substring(this.ymdHms[ID_ZONE_HOURS].length() - 2);
            this.ymdHms[ID_ZONE_HOURS] = this.ymdHms[ID_ZONE_HOURS].substring(0, this.ymdHms[ID_ZONE_HOURS].length() - 2);
        }
    }

    String[] getYmdHms() {
        return this.ymdHms;
    }

    public static int[] getYmdHmsInt(String[] ymdHms) {
        final int[] ymdHmsInt = new int[ymdHms.length];
        for (int i = 0, length = ymdHms.length; i < length; i++) {
            ymdHmsInt[i] = Integer.parseInt(ymdHms[i]);

        }
        return ymdHmsInt;
    }

    public int[] getYmdHmsInt() {
        return getYmdHmsInt(this.ymdHms);
    }

    public static boolean isIso8601(String date) {
        return date.matches("(-?(?:[1-9][0-9]*)?[0-9]{4})-(1[0-2]|0[1-9])-(3[0-1]|0[1-9]|[1-2][0-9])T(2[0-3]|[0-1][0-9]):([0-5][0-9]):([0-5][0-9])(\\.[0-9]+)?(Z|[+-](?:2[0-3]|[0-1][0-9]):[0-5][0-9])?");
    }
}
