/*
 * BondDataProvider.java
 *
 * Created on 13.09.2006 10:59:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.MasterDataWarrant;
import de.marketmaker.istar.merger.provider.ProviderPreference;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.warrantdata")
public interface WarrantDataProvider extends EdgDataProvider {
    MasterDataWarrant getMasterData(long instrumentid, ProviderPreference preference);

    @Deprecated
    MasterDataWarrant getMasterData(long instrumentid);
}
