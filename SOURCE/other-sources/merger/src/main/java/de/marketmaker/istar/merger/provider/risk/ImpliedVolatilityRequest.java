/*
 * ImpliedVolatilityRequest.java
 *
 * Created on 14.12.11 13:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.risk;

import de.marketmaker.istar.common.request.AbstractIstarRequest;
import org.joda.time.LocalDate;

/**
 * @author oflege
 */
public class ImpliedVolatilityRequest extends AbstractIstarRequest {
    static final long serialVersionUID = 1L;

    private final long underlyingid;
    
    private final LocalDate from;

    public ImpliedVolatilityRequest(long underlyingid, LocalDate from) {
        this.underlyingid = underlyingid;
        this.from = from;
    }

    public long getUnderlyingid() {
        return underlyingid;
    }

    public LocalDate getFrom() {
        return from;
    }

    @Override
    protected void appendToString(StringBuilder sb) {
        sb.append(", underlyingid=").append(this.underlyingid);
        if (this.from != null) {
            sb.append(", from=").append(this.from);
        }
    }
}
