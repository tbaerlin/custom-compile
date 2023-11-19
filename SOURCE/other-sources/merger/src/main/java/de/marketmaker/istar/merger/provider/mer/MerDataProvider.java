/*
 * MerDataProvider.java
 *
 * Created on 26.09.2008 07:36:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.mer;

import de.marketmaker.istar.common.amqp.AmqpAddress;
import de.marketmaker.istar.domain.profile.Profile;

import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.provider.merData")
public interface MerDataProvider {
    Map<String, List<String>> getMetadata(Profile profile);

    List<MerItem> getItems(Profile profile, List<String> type, List<String> country);
}
