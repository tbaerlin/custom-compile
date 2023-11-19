/*
 * PmAuthenticationRequest.java
 *
 * Created on 12.07.12 15:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 * @author oflege
 */
public class PmAuthenticationRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final String credentials;

    public PmAuthenticationRequest(String credentials) {
        this.credentials = credentials;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", credentials='").append(this.credentials).append("'");
    }

    public String getCredentials() {
        return this.credentials;
    }
}
