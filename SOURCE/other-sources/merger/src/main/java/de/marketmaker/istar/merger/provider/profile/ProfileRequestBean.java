package de.marketmaker.istar.merger.provider.profile;

import de.marketmaker.istar.domainimpl.profile.ProfileRequest;

/**
 * A special purpose bean with overridable authentication properties.
 *
 * Keeps {@link ProfileRequest} unchanged, to leave its property immutability intact.
 */
public class ProfileRequestBean extends ProfileRequest {

    private String authenticationType;

    private String authentication;

    public ProfileRequestBean(String authenticationType, String authentication) {
        super(authenticationType, authentication); // properties hidden
        this.authenticationType = authenticationType;
        this.authentication = authentication;
    }

    @Override
    public String getAuthenticationType() {
        return authenticationType;
    }

    public void setAuthenticationType(String authenticationType) {
        this.authenticationType = authenticationType;
    }

    @Override
    public String getAuthentication() {
        return authentication;
    }

    public void setAuthentication(String authentication) {
        this.authentication = authentication;
    }
}
