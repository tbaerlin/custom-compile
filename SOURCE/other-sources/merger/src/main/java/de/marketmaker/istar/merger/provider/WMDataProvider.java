/*
 * WMDataProvider.java
 *
 * Created on 02.11.11 18:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

/**
 * @author oflege
 */
public interface WMDataProvider {
    WMDataResponse getData(WMDataRequest request);
}
