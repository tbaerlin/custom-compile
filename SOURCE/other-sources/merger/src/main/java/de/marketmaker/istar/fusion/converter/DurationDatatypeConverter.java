package de.marketmaker.istar.fusion.converter;

import de.marketmaker.istar.common.util.PeriodEditor;
import org.joda.time.Period;
import org.joda.time.format.ISOPeriodFormat;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DurationDatatypeConverter {
    public static String marshal(Period value) {
        throw new UnsupportedOperationException("not needed");
    }

    public static Period unmarshal(String value) {
        return ISOPeriodFormat.standard().parsePeriod(value);
    }
}