/*
 * ProfileProvider.java
 *
 * Created on 04.02.2008 13:41:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;

import de.marketmaker.istar.domainimpl.profile.PmAuthentcationProvider;
import de.marketmaker.istar.domainimpl.profile.PmAuthenticationRequest;
import de.marketmaker.istar.domainimpl.profile.PmAuthenticationResponse;
import de.marketmaker.istar.domainimpl.profile.ProfileProvider;
import de.marketmaker.istar.domainimpl.profile.ProfileRequest;
import de.marketmaker.istar.domainimpl.profile.ProfileResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@ManagedResource
public class DefaultProfileProvider implements ProfileProvider, PmAuthentcationProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, ProfileProvider> mappings = new ConcurrentHashMap<>();

    private Map<String, ProfileProvider> disabled = new ConcurrentHashMap<>();

    private Map<String, ProfileRequest> resourceIdToProfileRequest = new ConcurrentHashMap<>();

    private PmAuthentcationProvider pmAuthentcationProvider;

    public void setPmAuthentcationProvider(PmAuthentcationProvider pmAuthentcationProvider) {
        this.pmAuthentcationProvider = pmAuthentcationProvider;
    }

    public void setMappings(Map<String, ProfileProvider> mappings) {
        this.mappings.putAll(mappings);
    }

    public void setResourceIdToProfileRequest(Map<String, ProfileRequest> resourceIdToProfileRequest) {
        this.resourceIdToProfileRequest.putAll(resourceIdToProfileRequest);
    }

    @ManagedOperation
    public void disableMapping(String name) {
        final ProfileProvider provider = this.mappings.remove(name);
        if (provider != null) {
            this.disabled.put(name, provider);
        }
    }

    @ManagedOperation
    public void enableMapping(String name) {
        final ProfileProvider provider = this.disabled.remove(name);
        if (provider != null) {
            this.mappings.put(name, provider);
        }
    }

    @ManagedAttribute
    public String getDisabledMappings() {
        return this.disabled.keySet().toString();
    }

    public PmAuthenticationResponse getPmAuthentication(PmAuthenticationRequest request) {
        if (this.pmAuthentcationProvider != null) {
            return this.pmAuthentcationProvider.getPmAuthentication(request);
        }
        return PmAuthenticationResponse.invalid();
    }

    private ProfileRequest rewrite(ProfileRequest request) {
        if ("resource".equals(request.getAuthenticationType())) {
            ProfileRequest override = this.resourceIdToProfileRequest.get(request.getAuthentication());
            if (override != null) {
                this.logger.info("<rewrite> " + request.getAuthentication() + " => " + override);
                return override;
            }
        }
        return request;
    }

    public ProfileResponse getProfile(ProfileRequest request) {
        return doGetProfile(rewrite(request));
    }

    private ProfileResponse doGetProfile(ProfileRequest request) {
        final ProfileProvider provider = this.mappings.get(request.getAuthenticationType());
        if (provider == null) {
            this.logger.warn("<getProfile> no mapping for " + request.getAuthenticationType());
            return ProfileResponse.invalid();
        }

        final ProfileResponse response = provider.getProfile(request);
        if (response != null) {
            return response;
        }

        this.logger.warn("<getProfile> provider returned null for " + request);
        return ProfileResponse.invalid();
    }
}
