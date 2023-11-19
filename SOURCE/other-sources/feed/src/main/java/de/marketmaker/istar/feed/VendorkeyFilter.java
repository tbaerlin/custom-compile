/*
 * VendorkeyFilter.java
 *
 * Created on 25.10.2004 15:29:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import java.util.function.Predicate;

import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * Filter based on a given vendor key.
 *
 * <p>
 * Concrete implementations could be obtained through {@link VendorkeyFilterFactory} with a
 * specification.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @see de.marketmaker.istar.feed.VendorkeyFilterFactory
 */
public interface VendorkeyFilter extends Predicate<Vendorkey> {

    /**
     * Alternative test method that avoids the creation of a vendorkey object, especially
     * suitable if performance is a concern. Not all filters can support this method, e.g.,
     * filters that use the vwd key type won't find the type in the vwdcode.
     * @param bytes contains a vwdcode
     * @param from where the vwdcode starts
     * @param to position of first byte after vwdcode
     * @return true iff the vwdcode in bytes is acceptable
     */
    default boolean test(byte[] bytes, int from, int to) {
        return test(VendorkeyVwd.getInstance(new ByteString(bytes, from, to)));
    }
}
