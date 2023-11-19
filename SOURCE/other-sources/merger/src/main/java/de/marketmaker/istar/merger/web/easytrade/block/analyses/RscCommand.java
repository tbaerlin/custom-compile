/*
 * RscCommand.java
 *
 * Created on 29.03.12 07:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import java.util.LinkedHashMap;
import java.util.Map;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.provider.NoDataException;

/**
 * @author oflege
 */
public class RscCommand {
    // linked to preserve insertion order
    private static final Map<String, Selector> PROVIDER_TO_SELECTOR = new LinkedHashMap<>();
    static {
        // order will be used to select default provider if none is specified
        PROVIDER_TO_SELECTOR.put("aktiencheck", Selector.SMARTHOUSE_ANALYSES);
        PROVIDER_TO_SELECTOR.put("dpaafx", Selector.DPA_AFX_ANALYSES);
        PROVIDER_TO_SELECTOR.put("shm", Selector.SHM_ANALYSES);
        PROVIDER_TO_SELECTOR.put("websim", Selector.WEB_SIM_ANALYSES);
        PROVIDER_TO_SELECTOR.put("awp", Selector.AWP_ANALYSER);
    }
    
    private String providerId;

    private boolean ignoreAnalysesWithoutRating = true;

    /**
     * Select the analyses provider; can only be used by clients that have permissions to use
     * multiple analyses providers (for others, the default value for this parameter will be set to
     * the only allowed provider, so there is no need to specify this parameter explicitly)
     */
    @RestrictedSet("aktiencheck,dpaafx,shm,websim,awp")
    public String getProviderId() {
        return this.providerId;
    }

    public void setIgnoreAnalysesWithoutRating(boolean ignoreAnalysesWithoutRating) {
        this.ignoreAnalysesWithoutRating = ignoreAnalysesWithoutRating;
    }

    /**
     * @return whether the result should include analyses without a sell/hold/buy recommendation
     */
    public boolean isIgnoreAnalysesWithoutRating() {
        return this.ignoreAnalysesWithoutRating;
    }

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    Selector getSelector() {
        return getSelector(this.providerId);
    }
    
    public static String getProviderId(Selector s) {
        for (Map.Entry<String, Selector> entry : PROVIDER_TO_SELECTOR.entrySet()) {
            if (entry.getValue() == s) {
                return entry.getKey();
            }
        }
        throw new NoDataException(s.toString());
    }

    // throws NoDataException if the user is not allowed to access the provider
    // he wants to retrieve data from
    public static Selector getSelector(String providerId) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();
        if (providerId != null) {
            final Selector requiredSelector = PROVIDER_TO_SELECTOR.get(providerId);
            if (!profile.isAllowed(requiredSelector)) {
                throw new NoDataException("provider not allowed");
            }
            return requiredSelector;
        }
        return getSelectorForProfile(profile);
    }
    
    private static Selector getSelectorForProfile(Profile profile) {
        for (Selector s: PROVIDER_TO_SELECTOR.values()) {
            if (profile.isAllowed(s)) {
                return s;
            }
        }
        throw new NoDataException("no provider available");
    }
}
