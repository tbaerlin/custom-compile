/*
 * NullSnapField.java
 *
 * Created on 18.07.2005 17:15:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.io.Serializable;
import java.math.BigDecimal;

import net.jcip.annotations.Immutable;

/**
 * A SnapField that is entirely undefined. Invoking {@link #isDefined()} always returns <code>false</code>,
 * invoking any other SnapField method always results in an {@link java.lang.IllegalStateException}
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class NullSnapField implements SnapField, Serializable {
    static final long serialVersionUID = 0L;

    public static final NullSnapField INSTANCE = new NullSnapField();

    private NullSnapField() {
    }

    private Object readResolve() {
        return INSTANCE;
    }

    public boolean isDefined() {
        return false;
    }

    public String toString() {
        return "NullSnapField[]";
    }

    public int getId() {
        throw new IllegalStateException("not defined");
    }

    public String getName() {
        throw new IllegalStateException("not defined");
    }

    public FieldTypeEnum getType() {
        throw new IllegalStateException("not defined");
    }

    public Object getValue() {
        throw new IllegalStateException("not defined");
    }

    public BigDecimal getPrice() {
        throw new IllegalStateException("not defined");
    }

    public BigDecimal getLastPrice() {
        return getPrice();
    }
}
