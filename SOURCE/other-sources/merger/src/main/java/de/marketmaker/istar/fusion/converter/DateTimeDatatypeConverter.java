package de.marketmaker.istar.fusion.converter;

import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DateTimeDatatypeConverter {
    public static String marshal(DateTime value) {
        throw new UnsupportedOperationException("not needed");
    }

    public static DateTime unmarshal(String value) {
        return ISODateTimeFormat.dateTimeNoMillis().parseDateTime(value);
    }
}