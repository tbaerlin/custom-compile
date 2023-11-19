/*
 * BondDataProvider.java
 *
 * Created on 13.09.2006 10:59:09
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.merger.provider.ProviderPreference;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.certificatedata")
public interface CertificateDataProvider extends EdgDataProvider {
    MasterDataCertificate getMasterData(long instrumentid, ProviderPreference preference);

    @Deprecated
    MasterDataCertificate getMasterData(long instrumentid, boolean smf);

    @Deprecated
    MasterDataCertificate getMasterData(long instrumentid);
}
