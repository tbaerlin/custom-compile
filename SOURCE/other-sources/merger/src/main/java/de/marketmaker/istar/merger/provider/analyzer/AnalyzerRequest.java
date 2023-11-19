package de.marketmaker.istar.merger.provider.analyzer;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.profile.Profile;


public class AnalyzerRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final Profile profile;

    private final String providerId;

    private final String query;


    public AnalyzerRequest(Profile profile, String providerId, String query) {
        this.profile = profile;
        this.providerId = providerId;
        this.query = query;
    }

    public Profile getProfile() {
        return profile;
    }

    public String getProviderId() {
        return providerId;
    }

    public String getQuery() {
        return query;
    }

    @Override
    public String toString() {
        return "AnalyzerRequest{"
                + " profile.name=" + profile.getName()
                + " providerId=" + providerId
                + " query=" + query
                + '}';
    }
}
