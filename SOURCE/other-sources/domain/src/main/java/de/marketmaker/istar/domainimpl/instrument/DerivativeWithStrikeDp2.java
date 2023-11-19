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

import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.instrument.DerivativeTypeEnum;
import de.marketmaker.istar.domain.instrument.DerivativeWithStrike;

/**
 * @author Martin Wilke
 */

public abstract class DerivativeWithStrikeDp2 extends DerivativeDp2 implements DerivativeWithStrike, Serializable {
    static final long serialVersionUID = 54940316532977089L;

    private BigDecimal strike = null;

    private Currency strikeCurrency = null;

    private DerivativeTypeEnum type = DerivativeTypeEnum.NONE;

    protected DerivativeWithStrikeDp2() {
    }

    public DerivativeWithStrikeDp2(long id) {
        super(id);
    }

    public DerivativeWithStrikeDp2(long id, Map<KeysystemEnum, String> symbols) {
        super(id, symbols);
    }

    public void setStrike(BigDecimal strike) {
        this.strike = strike;
    }

    public void setStrikeCurrency(Currency strikeCurrency) {
        this.strikeCurrency = strikeCurrency;
    }

    public void setType(DerivativeTypeEnum type) {
        this.type = type;
    }

    public BigDecimal getStrike() {
        return strike;
    }

    public Currency getStrikeCurrency() {
        return strikeCurrency;
    }

    public DerivativeTypeEnum getType() {
        return type;
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;
        DerivativeWithStrikeDp2 that = (DerivativeWithStrikeDp2) o;
        if (type != that.type) return false;
        if (strike != null ? !strike.equals(that.strike) : that.strike != null) return false;
        return true;
    }
}
