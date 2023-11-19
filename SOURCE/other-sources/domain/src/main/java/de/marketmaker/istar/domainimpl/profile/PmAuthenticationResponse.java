/*
 * PmAuthenticationResponse.java
 *
 * Created on 12.07.12 15:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * @author oflege
 */
public class PmAuthenticationResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 2L;

    private final HashSet<String> abos;

    private final String kennung;

    private PmAuthenticationResponse() {
        this.abos = null;
        this.kennung = null;
        setInvalid();
    }

    public PmAuthenticationResponse(String kennung, Collection<String> abos) {
        this.abos = new HashSet<>(abos);
        this.kennung = kennung;
    }

    public static PmAuthenticationResponse invalid() {
        return new PmAuthenticationResponse();
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", kennung=").append(this.kennung).append(", abos=").append(this.abos);
    }

    public Collection<String> getAbos() {
        return Collections.unmodifiableSet(this.abos);
    }

    public String getKennung() {
        return this.kennung;
    }
}
