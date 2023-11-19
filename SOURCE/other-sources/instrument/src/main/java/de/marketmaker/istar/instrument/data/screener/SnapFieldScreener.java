/*
 * SnapFieldScreener.java
 *
 * Created on 26.04.2005 09:21:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.data.screener;

import java.math.BigDecimal;

import de.marketmaker.istar.domain.data.FieldTypeEnum;
import de.marketmaker.istar.domain.data.AbstractSnapField;
import de.marketmaker.istar.common.util.PriceCoder;
import net.jcip.annotations.Immutable;

/**
 * A SnapField representing the value of a {@link ScreenerFieldDescription} field.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
public class SnapFieldScreener extends AbstractSnapField {
    static final long serialVersionUID = 1L;

    public SnapFieldScreener(int id, Object value) {
        super(id, value);
    }

    public String getName() {
        return ScreenerFieldDescription.NAMES[getId()];
    }

    public FieldTypeEnum getType() {
        switch (ScreenerFieldDescription.TYPES[getId()]) {
            case ScreenerFieldDescription.TYPE_DATE:
//            case ScreenerFieldDescription.TYPE_UNUM2:
            case ScreenerFieldDescription.TYPE_UNUM4:
//            case ScreenerFieldDescription.TYPE_TIME:
                return FieldTypeEnum.NUMBER;
            case ScreenerFieldDescription.TYPE_UCHAR:
//            case ScreenerFieldDescription.TYPE_CHARV:
                return FieldTypeEnum.STRING;
            case ScreenerFieldDescription.TYPE_PRICE:
                return FieldTypeEnum.PRICE;
            default:
                throw new IllegalArgumentException("unknown field type: this should never happen");
        }
    }

    public int getFieldDescriptionType() {
        return ScreenerFieldDescription.TYPES[getId()];
    }

    public int getFormattingHint() {
        return ScreenerFieldDescription.FORMATTING_HINTS[getId()];
    }

    public BigDecimal getPrice() {
        if (getType() != FieldTypeEnum.PRICE) {
            return null;
        }
        return BigDecimal.valueOf((Long) getValue(), PriceCoder.DEFAULT_FRACTION);
    }

    public String toString() {
        return "SnapFieldScreener[" + getId() + "=>" + getValue() + "]";
    }
}
