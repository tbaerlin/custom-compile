/*
 * MmwebRequest.java
 *
 * Created on 07.08.2008 10:25:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.pmxml.ServerLoginResult;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PmUserResponse extends UserResponse implements Serializable {
    private ServerLoginResult pmState;

    public PmUserResponse() {
    }

    public PmUserResponse(UserResponse response) {
        withState(response.getState());
        withUser(response.getUser());
    }

    public PmUserResponse withState(ServerLoginResult state) {
        this.pmState = state;

        //map the pm-types to userresponse states
        switch (this.pmState) {
            case SLR_OK:
                break;
            case SLR_OK_BUT_PASSWORD_EXPIRED:
                withState(State.PASSWORD_EXPIRED);
                break;
            case SLR_FEATURE_DEACTIVATED:
                withState(State.INVALID_PRODUCT);
                break;
            case SLR_MODULE_DEACTIVATED:
                withState(State.INVALID_PRODUCT);
                break;
            case SLR_LICENSE_INVALID:
                withState(State.LICENSE_INVALID);
                break;
            case SLR_UNKNOWN_USER_OR_PASSWORD:
                withState(State.WRONG_PASSWORD);
                break;
            case SLR_USER_DEACTIVATED:
                withState(State.INACTIVE_USER);
                break;
            default:
                withState(State.INTERNAL_ERROR);
        }
        return this;
    }

    public ServerLoginResult getPmState() {
        return this.pmState;
    }
}