/*
 * PropertySupported.java
 *
 * Created on 22.10.2007 14:20:23
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PropertySupported {
    PropertySupport propertySupport(int fid);

    void setTimestamp(long timestamp);

    long getTimestamp();
}
