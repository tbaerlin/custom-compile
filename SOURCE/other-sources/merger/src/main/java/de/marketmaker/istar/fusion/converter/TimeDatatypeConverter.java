package de.marketmaker.istar.fusion.converter;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.DateTimeFormatterBuilder;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TimeDatatypeConverter {
    private static final DateTimeFormatter XML_FORMAT = new DateTimeFormatterBuilder()
            .append(DateTimeFormat.forPattern("HH:mm:ss"))
            .appendOptional(DateTimeFormat.forPattern("Z").getParser())
            .toFormatter()
            .withOffsetParsed();

    public static String marshal(LocalTime value) {
        throw new UnsupportedOperationException("not needed");
    }

    public static LocalTime unmarshal(String value) {
        return XML_FORMAT.parseDateTime(value).toLocalTime();
    }
}