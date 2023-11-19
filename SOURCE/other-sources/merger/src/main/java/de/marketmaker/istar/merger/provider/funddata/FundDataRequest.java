package de.marketmaker.istar.merger.provider.funddata;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.merger.context.RequestContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FundDataRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final Profile profile;

    private final List<Long> instrumentids;

    // TODO: refactor to carry priorities, move up to AbstractIstarRequest
    private final List<Locale> locales;

    private boolean withMasterData = false;

    private boolean withAllocations = false;

    private boolean withConsolidatedAllocations = false;

    private boolean withMorningstarRating = false;

    private boolean withMorningstarRatingDz = false;

    private boolean withFeriRating = false;

    private boolean withBenchmarks = false;

    private boolean withFeriPerformances = false;

    private String providerPreference;

    public FundDataRequest(Instrument instrument) {
        this(Collections.singletonList(instrument.getId()));
    }

    public FundDataRequest(List<Long> instrumentids) {
        this(instrumentids,
                RequestContextHolder.getRequestContext().getProfile(),
                RequestContextHolder.getRequestContext().getLocales());
    }

    public FundDataRequest(List<Long> instrumentids, Profile profile, List<Locale> locales) {
        this.instrumentids = instrumentids;

        this.profile = profile;
        this.locales = locales;
    }

    public FundDataRequest withMasterData() {
        this.withMasterData = true;
        return this;
    }

    public FundDataRequest withAllocations() {
        this.withAllocations = true;
        return this;
    }

    public FundDataRequest withConsolidatedAllocations() {
        this.withConsolidatedAllocations = true;
        return this;
    }

    public FundDataRequest withMorningstarRating() {
        this.withMorningstarRating = true;
        return this;
    }

    public FundDataRequest withMorningstarRatingDz() {
        this.withMorningstarRatingDz = true;
        return this;
    }

    public FundDataRequest withFeriRating() {
        this.withFeriRating = true;
        return this;
    }

    public FundDataRequest withBenchmarks() {
        this.withBenchmarks = true;
        return this;
    }

    public FundDataRequest withFeriPerformances() {
        this.withFeriPerformances = true;
        return this;
    }

    public Profile getProfile() { return profile; }

    public List<Long> getInstrumentids() {
        return instrumentids;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public boolean isWithMasterData() {
        return withMasterData;
    }

    public boolean isWithAllocations() {
        return withAllocations;
    }

    public boolean isWithConsolidatedAllocations() {
        return withConsolidatedAllocations;
    }

    public boolean isWithMorningstarRating() {
        return withMorningstarRating;
    }

    public boolean isWithMorningstarRatingDz() {
        return withMorningstarRatingDz;
    }

    public boolean isWithFeriRating() {
        return withFeriRating;
    }

    public boolean isWithBenchmarks() {
        return withBenchmarks;
    }

    public boolean isWithFeriPerformances() {
        return withFeriPerformances;
    }

    public String getProviderPreference() {
        return providerPreference;
    }

    public void setProviderPreference(String providerPreference) {
        this.providerPreference = providerPreference;
    }

    public boolean onlyInstrumentAllocations() {
        return (withAllocations || withConsolidatedAllocations)
                && !withMasterData
                && !withMorningstarRating
                && !withMorningstarRatingDz
                && !withFeriRating
                && !withBenchmarks
                && !withFeriPerformances;
    }

    public String toString() {
        return "FundDataRequest["
                + "profile=" + profile
                + ", instrumentids=" + instrumentids
                + ", withMasterData=" + withMasterData
                + ", withAllocations=" + withAllocations
                + ", withConsolidatedAllocations=" + withConsolidatedAllocations
                + ", withMorningstarRating=" + withMorningstarRating
                + ", withMorningstarRatingDz=" + withMorningstarRatingDz
                + ", withFeriRating=" + withFeriRating
                + ", withBenchmarks=" + withBenchmarks
                + ", withFeriPerformances=" + withFeriPerformances
                + ", locales=" + locales
                + ", providerPreference=" + providerPreference
                + "/" + getClientInfo() + "]";
    }
}
