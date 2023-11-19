/*
 * UserMasterDataResponse.java
 *
 * Created on 14.07.2008 14:11:33
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.request.AbstractIstarResponse;
import de.marketmaker.istar.domain.profile.UserMasterData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserMasterDataResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 1L;

    private UserMasterData masterData;

    @SuppressWarnings("WeakerAccess") // cannot be package private, because it is used for Mocking etc. by several not-i* consumers
    public UserMasterDataResponse(UserMasterData data) {
        this.masterData = data;
    }

    private UserMasterDataResponse() {
    }

    public static UserMasterDataResponse createInvalid() {
        final UserMasterDataResponse result = new UserMasterDataResponse();
        result.setInvalid();
        return result;
    }

    public UserMasterData getMasterData() {
        return masterData;
    }
}
