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
public class PushChangeRequest implements Serializable {
    static final long serialVersionUID = 5292725358934367544L;

    public final static String ORDERBOOK_PREFIX = "#"; // $NON-NLS$

    private String sessionId;

    private HashSet<String> toRegister;

    private HashSet<String> toUnregister;

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public HashSet<String> getToRegister() {
        return toRegister;
    }

    public void setToRegister(HashSet<String> toRegister) {
        this.toRegister = toRegister;
    }

    public HashSet<String> getToUnregister() {
        return toUnregister;
    }

    public void setToUnregister(HashSet<String> toUnregister) {
        this.toUnregister = toUnregister;
    }
}
