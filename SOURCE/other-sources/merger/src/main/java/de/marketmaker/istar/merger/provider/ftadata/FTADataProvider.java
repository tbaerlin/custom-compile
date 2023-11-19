/*
 * CLASS.java
 *
 * Created on 6/13/13 9:16 AM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.ftadata;

/**
 * @author Stefan Willenbrock
 */
public interface FTADataProvider {
    FTADataResponse getFTAData(FTADataRequest request);
}
