package de.marketmaker.istar.merger.provider.stockdata;

import java.util.List;
import java.util.Locale;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class StockDataRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final List<Long> instrumentids;

    private final Profile profile;

    // TODO: refactor to carry priorities, move up to AbstractIstarRequest
    private List<Locale> locales;

    private boolean withCompanyProfile = false;

    private boolean withAnnualReportData = false;

    public StockDataRequest(List<Long> instrumentids, Profile profile, List<Locale> locales) {
        this.instrumentids = instrumentids;
        this.profile = profile;
        this.locales = locales;
    }

    public StockDataRequest withCompanyProfile() {
        this.withCompanyProfile = true;
        return this;
    }

    public StockDataRequest withAnnualReportData() {
        this.withAnnualReportData = true;
        return this;
    }

    public List<Long> getInstrumentids() {
        return instrumentids;
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public boolean isWithCompanyProfile() {
        return withCompanyProfile;
    }

    public boolean isWithAnnualReportData() {
        return withAnnualReportData;
    }

    @Override
    public String toString() {
        return "StockDataRequest{" +
                "instrumentids=" + instrumentids +
                ", profile=" + profile +
                ", locales=" + locales +
                ", withCompanyProfile=" + withCompanyProfile +
                ", withAnnualReportData=" + withAnnualReportData +
                '}';
    }
}