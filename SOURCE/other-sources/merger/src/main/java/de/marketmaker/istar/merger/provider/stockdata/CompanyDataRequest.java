package de.marketmaker.istar.merger.provider.stockdata;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;

import java.util.List;
import java.util.Locale;

/**
 * Company data for a single instrument but multiple locales.
 */
public class CompanyDataRequest extends AbstractIstarRequest {

    static final long serialVersionUID = 1L;

    private final long instrumentId;

    private Profile profile;

    private List<Locale> locales;

    public CompanyDataRequest(long instrumentId, Profile profile, List<Locale> locales) {
        this.instrumentId = instrumentId;
        this.profile = profile;
        this.locales = locales;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public Profile getProfile() {
        return profile;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    @Override
    public String toString() {
        return "CompanyDataRequest{" +
                "instrumentId=" + instrumentId +
                ", profile=" + profile +
                ", locales=" + locales +
                '}';
    }
}
