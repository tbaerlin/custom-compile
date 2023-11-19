/*
 * HasPriceQuality.java
 *
 * Created on 05.03.2010 14:12:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author oflege
 */
public interface HasPriceQuality {
    PriceQuality getPriceQuality();

    boolean isPushAllowed();
}
