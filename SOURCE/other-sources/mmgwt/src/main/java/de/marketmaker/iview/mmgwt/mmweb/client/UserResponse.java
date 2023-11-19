/*
 * MmwebRequest.java
 *
 * Created on 07.08.2008 10:25:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import de.marketmaker.iview.mmgwt.mmweb.client.data.User;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class UserResponse implements Serializable {
    public enum State {
        OK,
        WRONG_PASSWORD,
        WRONG_INITIAL_PASSWORD,
        UNKNOWN_USER, 
        INACTIVE_USER,
        INVALID_PRODUCT,
        INTERNAL_ERROR,
        LICENSE_INVALID,
        PASSWORD_EXPIRED,
        INVALID_VWDID
    }

    private State state = State.OK;

    private User user;

    public State getState() {
        return state;
    }

    public User getUser() {
        return user;
    }

    public UserResponse withState(State state) {
        this.state = state;
        return this;
    }

    public UserResponse withUser(User user) {
        this.user = user;
        return this;
    }
}
