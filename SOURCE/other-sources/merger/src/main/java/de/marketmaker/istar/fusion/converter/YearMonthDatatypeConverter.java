package de.marketmaker.istar.fusion.converter;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class YearMonthDatatypeConverter {
    private static final DateTimeFormatter XML_FORMAT = DateTimeFormat.forPattern("yyyy-MM");

    public static String marshal(LocalDate value) {
        throw new UnsupportedOperationException("not needed");
    }

    public static LocalDate unmarshal(String value) {
        return XML_FORMAT.parseDateTime(value).toLocalDate();
    }
}
