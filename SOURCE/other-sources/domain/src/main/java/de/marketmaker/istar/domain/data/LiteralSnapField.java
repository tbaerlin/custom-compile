/*
 * LiteralSnapField.java
 *
 * Created on 04.07.2006 07:47:28
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import net.jcip.annotations.Immutable;

/**
 * A self-contained SnapField implementation. Does not rely on any external classes, provides
 * four static factory methods to create instances of a specific type.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class LiteralSnapField extends AbstractSnapField {
    private final FieldTypeEnum type;

    private LiteralSnapField(int id, Object value, FieldTypeEnum type) {
        super(id, value);
        this.type = type;
    }

    /**
     * @return a new price field
     */
    public static SnapField createPrice(int id, BigDecimal value) {
        return new LiteralSnapField(id, value, FieldTypeEnum.PRICE);
    }

    /**
     * @return a new numeric field
     */
    public static SnapField createNumber(int id, Number value) {
        return new LiteralSnapField(id, value, FieldTypeEnum.NUMBER);
    }

    /**
     * @return a new time field
     */
    public static SnapField createTime(int id, Number value) {
        return new LiteralSnapField(id, value, FieldTypeEnum.TIME);
    }

    /**
     * @return a new date field
     */
    public static SnapField createDate(int id, Number value) {
        return new LiteralSnapField(id, value, FieldTypeEnum.DATE);
    }

    /**
     * @return a new string field
     */
    public static SnapField createString(int id, String value) {
        return new LiteralSnapField(id, value, FieldTypeEnum.STRING);
    }

    public String getName() {
        return Integer.toString(getId());
    }

    public BigDecimal getPrice() {
        return (this.type == FieldTypeEnum.PRICE) ? (BigDecimal) getValue() : null;
    }

    public FieldTypeEnum getType() {
        return this.type;
    }

    public String toString() {
        return getId() + "=" + getValue();
    }
}
