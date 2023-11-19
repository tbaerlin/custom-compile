package de.marketmaker.istar.merger.provider;

import java.util.List;
import java.util.Locale;

import de.marketmaker.istar.domain.data.DownloadableItem;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ReportProvider {
    String PREFIX = "reportprovider.";

    enum Description {
        MONTHLY_REPORT(PREFIX + "descMonthlyReport"),
        SEMINANNUAL_REPORT(PREFIX + "descSeminannualReport"),
        ANNUAL_REPORT(PREFIX + "descAnnualReport"),
        ACCOUNTS(PREFIX + "descAccounts"),
        PROSPECTUS(PREFIX + "descProspectus"),
        /**
         * Reports of this type should not be shown in results, should be filtered by MDP in the export view.
         */
        @Deprecated
        SHORT_PROSPECTUS(PREFIX + "descShortProspectus"),
        FACT_SHEET(PREFIX + "descFactSheet"),
        CONSTITUTIVE_CRITERIA(PREFIX + "descConstitutiveCriteria"),
        UNFINISHED_PROSPECTUS(PREFIX + "descUnfinishedProspectus"),
        ADDENDUM(PREFIX + "descAddendum"),
        KIID(PREFIX + "descKiid");

        Description(String message) {
            this.message = message;
        }

        public final String message;
    }

    DownloadableItem.Source getSource();

    List<DownloadableItem> getReports(long instrumentid);

    List<DownloadableItem> getReports(long instrumentid, Locale locale);
}
