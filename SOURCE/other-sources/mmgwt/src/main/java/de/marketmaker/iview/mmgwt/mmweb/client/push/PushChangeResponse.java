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
public class PushChangeResponse implements Serializable {
    public enum State {
        OK,
        NO_SESSION,
        INTERNAL_ERROR
    }

    private HashSet<String> registered;
    
    private HashSet<String> invalid;

    private State state = State.OK;

    public State getState() {
        return this.state;
    }

    public PushChangeResponse withState(State state) {
        this.state = state;
        return this;
    }

    public HashSet<String> getRegistered() {
        return this.registered;
    }

    public void setRegistered(HashSet<String> registered) {
        this.registered = registered;
    }

    public HashSet<String> getInvalid() {
        return this.invalid;
    }

    public void setInvalid(HashSet<String> invalid) {
        this.invalid = invalid;
    }
}
