/*
 * RatingDataProvider.java
 *
 * Created on 03.11.11 18:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

/**
 * @author tkiesgen
 */
public interface RatingDataProvider {
    RatingDataResponse getData(RatingDataRequest request);
}
