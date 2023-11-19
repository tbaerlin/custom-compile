/*
 * AbstractSnapField.java
 *
 * Created on 04.07.2006 08:23:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public abstract class AbstractSnapField implements SnapField, Serializable {
    private final Object value;
    private final int id;

    protected AbstractSnapField(int id, Object value) {
        this.id = id;
        this.value = value;
    }

    public int getId() {
        return this.id;
    }

    public Object getValue() {
        return this.value;
    }

    @Override
    public BigDecimal getLastPrice() {
        return getPrice();
    }

    public boolean isDefined() {
        return this.value != null;
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (o.getClass() != this.getClass()) {
            return false;
        }

        final SnapField sf = (SnapField) o;

        if (!this.isDefined() || !sf.isDefined()) {
            return false; // undefined fields are like undefined values in ternary logic
        }

        if (this.id != sf.getId()) {
            return false;
        }

        if (this.value != null) {
            return this.value.equals(sf.getValue());
        }
        else {
            return sf.getValue() == null;
        }
    }

    public int hashCode() {
        int result = id;
        result = 29 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
