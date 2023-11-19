/*
 * SnapFieldVwd.java
 *
 * Created on 02.03.2005 16:39:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.math.BigDecimal;

import net.jcip.annotations.Immutable;

import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.domain.data.AbstractSnapField;
import de.marketmaker.istar.domain.data.FieldTypeEnum;

/**
 * A SnapField representing the value of a {@link VwdFieldDescription} field.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class SnapFieldVwd extends AbstractSnapField {

    /**
     * Creates new snap field based on {@link VwdFieldDescription} field.
     * @throws ArrayIndexOutOfBoundsException if field id is invalid
     * @throws IllegalArgumentException if no field with that id exists
     */
    public SnapFieldVwd(int id, Object value) {
        super(id, value);
        if (VwdFieldDescription.getField(id) == null) {
            throw new IllegalArgumentException("no such field: " + id);
        }
    }

    public String getName() {
        return VwdFieldDescription.getField(getId()).name();
    }

    public FieldTypeEnum getType() {
        return VwdFieldDescription.getField(getId()).type().getFieldType();
    }

    public VwdFieldDescription.Type getPlainType() {
        final VwdFieldDescription.Field f = VwdFieldDescription.getField(getId());
        return f.type();
    }

    public Object getValue() {
        if (isDefined()) {
            switch (getPlainType()) {
                case PRICE:
                    // default behaviour: price is long with scale 5
                    return PriceCoder.toDefaultEncoding(((Number) super.getValue()).longValue());
                case UINT:
                    return Integer.toUnsignedLong(((Number)super.getValue()).intValue());
            }
        }
        return super.getValue();
    }

    public BigDecimal getPrice() {
        if (isDefined() && getType() == FieldTypeEnum.PRICE) {
            return PriceCoder.decode((Long) super.getValue());
        }
        return null;
    }

    public String toString() {
        return getName() + "(" + getId() + ")=" + getValue();
    }
}
