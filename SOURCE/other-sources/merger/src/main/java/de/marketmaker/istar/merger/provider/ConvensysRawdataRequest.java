/*
 * ConvensysRawdataRequest.java
 *
 * Created on 09.01.12 14:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Profile;

/**
 * @author tkiesgen
 */
public class ConvensysRawdataRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final Profile profile;

    private final Instrument instrument;

    private final boolean keydata;

    private final boolean transformed;

    private final boolean metadata;

    public ConvensysRawdataRequest(Profile profile, Instrument instrument, boolean keydata,
            boolean transformed, boolean metadata) {
        this.profile = profile;
        this.instrument = instrument;
        this.keydata = keydata;
        this.transformed = transformed;
        this.metadata = metadata;
    }

    public Profile getProfile() {
        return profile;
    }

    public Instrument getInstrument() {
        return instrument;
    }

    public boolean isKeydata() {
        return keydata;
    }

    public boolean isTransformed() {
        return transformed;
    }

    public boolean isMetadata() {
        return metadata;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", profile=").append(this.profile);
        sb.append(", instrument=").append(this.instrument);
        sb.append(", keydata").append(this.keydata);
        sb.append(", transformed").append(this.transformed);
        sb.append(", metadata").append(this.metadata);

    }
}