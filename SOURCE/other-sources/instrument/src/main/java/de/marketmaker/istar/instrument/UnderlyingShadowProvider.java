/*
 * UnderlyingShadowProvider.java
 *
 * Created on 14.08.2008 20:15:56
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface UnderlyingShadowProvider {
    /**
     * Returns the iids of shadow instruments that act as underlyings.
     * e.g., for 132358 (856900.ETR), this method would return the iids of
     * ODAX.DTB and possibly others as well that are referenced with their respective
     * iid as underlyings in derivatives.
     * @param shadowIid source iid
     * @return iids of shadow instruments
     */
    List<Long> getInstrumentids(long shadowIid);

    /**
     * Returns the iid for a shadow instrument. Reverses {@link #getInstrumentids(long)}, so that
     * for any value in the list returned by that method this method will return the shadowIid
     * @param iid underlying iid
     * @return shadow instrument id
     */
    Long getShadowInstrumentId(long iid);
}
