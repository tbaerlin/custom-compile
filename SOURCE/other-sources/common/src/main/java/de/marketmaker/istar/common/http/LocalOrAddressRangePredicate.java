/*
 * LocalOrAddressRangePredicate.java
 *
 * Created on 30.11.15 17:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.http;

import java.net.InetAddress;

/**
 * @author tkiesgen
 */
public class LocalOrAddressRangePredicate extends IPv4AddressRangePredicate {
    @Override
    public boolean test(InetAddress inetAddress) {
        return inetAddress.isLoopbackAddress()
                || inetAddress.isSiteLocalAddress()
                || super.test(inetAddress);
    }
}
