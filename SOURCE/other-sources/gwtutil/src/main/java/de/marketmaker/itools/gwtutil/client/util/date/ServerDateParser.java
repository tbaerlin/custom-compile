package de.marketmaker.itools.gwtutil.client.util.date;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Author: umaurer
 * Created: 24.04.14
 */
public class ServerDateParser extends CsDateParser {
    private static final int CENTURY = (LocalDateTime.now().getYear() / 100) * 100;

    ServerDateParser(String s, Msg msg) {
        super(s, msg, ".", initializeYmdHms());
    }

    private static String[] initializeYmdHms() {
        final LocalDateTime dateTime = LocalDateTime.now();
        return new String[]{String.valueOf(dateTime.getYear()), String.valueOf(dateTime.getMonthValue()), String.valueOf(dateTime.getDayOfMonth()), "0", "0", "0", "0", "0", "0"};
    }

    public static int[] getYmdHmsInt(String s, Msg msg) {
        return getYmdHmsInt(new ServerDateParser(s, msg).getYmdHms());
    }

    public static LocalDateTime getDateTime(String s) {
        final int[] ymdHmsInt = getYmdHmsInt(s, MSG);
        return LocalDateTime.of(
                ymdHmsInt[ID_YEAR] < 100 ? (ymdHmsInt[ID_YEAR] + CENTURY) : ymdHmsInt[ID_YEAR],
                ymdHmsInt[ID_MONTH],
                ymdHmsInt[ID_DAY],
                ymdHmsInt[ID_HOURS],
                ymdHmsInt[ID_MINUTES],
                ymdHmsInt[ID_SECONDS]);
    }

    public static String formatIso8601(String s) {
        final LocalDateTime dateTime = getDateTime(s);
        return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(dateTime);
    }
}
