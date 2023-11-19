package de.marketmaker.iview.dmxml.converter;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateDatatypeConverter {
    private static final DateTimeFormatter XML_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TARGET_FORMAT = DateTimeFormat.forPattern("dd.MM.yyyy");

    public static String marshal(String value) {
        return value;
    }

    public static String unmarshal(String value) {
        final DateTime dt = XML_FORMAT.parseDateTime(value);
        return TARGET_FORMAT.print(dt);
    }
}
