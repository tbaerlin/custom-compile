/*
 * MmauthProvider.java
 *
 * Created on 12.07.12 15:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

/**
 * @author oflege
 */
public interface PmAuthentcationProvider {
    PmAuthenticationResponse getPmAuthentication(PmAuthenticationRequest request);
}
