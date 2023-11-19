/*
 * InstrumentDp2.java
 *
 * Created on 19.12.2004 17:05:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.Derivative;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class DerivativeDp2 extends InstrumentDp2 implements Derivative, Serializable {
    static final long serialVersionUID = -104L;

    private long underlyingId;

    private BigDecimal subscriptionRatio;

    protected DerivativeDp2() {
    }

    public DerivativeDp2(long id) {
        super(id);
    }

    public DerivativeDp2(long id, Map<KeysystemEnum, String> symbols) {
        super(id, symbols);
    }

    public long getUnderlyingId() {
        return underlyingId;
    }

    public void setUnderlyingId(long underlyingId) {
        this.underlyingId = underlyingId;
    }

    public BigDecimal getSubscriptionRatio() {
        return this.subscriptionRatio;
    }

    public void setSubscriptionRatio(BigDecimal subscriptionRatio) {
        this.subscriptionRatio = subscriptionRatio;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        DerivativeDp2 that = (DerivativeDp2) o;
        if (subscriptionRatio != null ? !subscriptionRatio.equals(that.subscriptionRatio) : that.subscriptionRatio != null)
            return false;
        return this.underlyingId == that.underlyingId;
    }
}
