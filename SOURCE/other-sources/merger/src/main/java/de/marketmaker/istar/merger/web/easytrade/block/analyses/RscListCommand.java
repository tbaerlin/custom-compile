/*
 * RscListCommand.java
 *
 * Created on 29.03.12 11:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block.analyses;

import de.marketmaker.istar.common.validator.RestrictedSet;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;

/**
 * @author oflege
 */
public class RscListCommand extends ListCommand {
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

    public void setProviderId(String providerId) {
        this.providerId = providerId;
    }

    Selector getSelector() {
        return RscCommand.getSelector(this.providerId);
    }

    /**
     * @return whether the result should include analyses without a sell/hold/buy recommendation
     */
    public boolean isIgnoreAnalysesWithoutRating() {
        return ignoreAnalysesWithoutRating;
    }

    public void setIgnoreAnalysesWithoutRating(boolean ignoreAnalysesWithoutRating) {
        this.ignoreAnalysesWithoutRating = ignoreAnalysesWithoutRating;
    }
}
