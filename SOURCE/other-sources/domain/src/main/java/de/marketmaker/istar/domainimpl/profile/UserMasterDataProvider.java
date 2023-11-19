/*
 * UserMasterDataProvider.java
 *
 * Created on 14.07.2008 14:29:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.merger.usermasterdata")
public interface UserMasterDataProvider {
    UserMasterDataResponse getUserMasterData(UserMasterDataRequest request);
}
