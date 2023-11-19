/*
 * PushChangeRequest.java
 *
 * Created on 10.02.2010 11:40:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.io.Serializable;
import java.util.HashSet;

/**
 * @author oflege
 */
public class PushSessionResponse implements Serializable {
    public enum State {
        OK,
        PUSH_NOT_ALLOWED,
        INTERNAL_ERROR
    }

    private String sessionId;

    private State state = State.OK;

    public PushSessionResponse() {
    }

    public PushSessionResponse withSession(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public PushSessionResponse withState(State s) {
        this.state = s;
        return this;
    }

    public String getSessionId() {
        return this.sessionId;
    }

    public State getState() {
        return this.state;
    }
}
