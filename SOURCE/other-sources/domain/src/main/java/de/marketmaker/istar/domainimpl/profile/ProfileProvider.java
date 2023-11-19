/*
 * ProfileService.java
 *
 * Created on 04.02.2008 11:34:38
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.amqp.AmqpAddress;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@AmqpAddress(queue = "istar.merger.profile")
public interface ProfileProvider {
    ProfileResponse getProfile(ProfileRequest request);
}
