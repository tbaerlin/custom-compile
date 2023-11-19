/*
 * Vendorkey.java
 *
 * Created on 08.08.2002 09:36:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed;

import de.marketmaker.istar.common.util.ByteString;

/**
 * Interface for a vendorkey. There is no "getVendorkeyString" because a Vendorkey
 * object is supposed to return that by its {@link Vendorkey#toString}.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface Vendorkey extends Comparable<Vendorkey> {
    /** max. length of a string representing a vendorkey */
    int MAX_VENDORKEY_LEN = 40;
    int MIN_VENDORKEY_LEN = 3;

    /**
     * Returns the numeric type of this key, e.g., 1 for 1.710000.ETR
     * @return this object's type
     */
    int getType();

    int getMdpsType();

    /**
     * Returns the vendorkey string WITH the type, e.g., 1.710000.ETR
     * @return vendorkey with type
     */
    ByteString toByteString();

    /**
     * Returns the symbol of the vendorkey. They symbol is the part
     * of the vendokey before the market, e.g.,
     * for "123456.ETR.1.1A" the symbol would be "123456".
     * @return the symbol of the vendorkey
     */
    ByteString getSymbol();

    /**
     * Returns the name of the market the vendorkey belongs to. For example, the market
     * for '1.710000.ETR' would be 'ETR'.
     * @return market
     */
    ByteString getMarketName();

    /**
     * Returns the vendorkey string WITHOUT the type, e.g., 710000.ETR
     * @return vendorkey without type
     */
    ByteString toVwdcode();
}
