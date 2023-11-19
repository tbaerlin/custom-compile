package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface IntradayReportingDao {
    void insertAccess(Quote quote, Profile profile);

    int getCount(String selector, DateTime from, DateTime to);
}
