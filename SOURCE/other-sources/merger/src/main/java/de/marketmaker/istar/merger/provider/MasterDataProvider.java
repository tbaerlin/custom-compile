/*
 * MasterDataProvider.java
 *
 * Created on 6/12/14 5:34 PM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.data.MasterData;

/**
 * @author Stefan Willenbrock
 */
public interface MasterDataProvider<T extends MasterData> {
    T getMasterData(long instrumentid);
}
