/*
 * FieldAlias.java
 *
 * Created on 20.11.2003 12:29:43
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.dp;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.feed.vwd.VwdFieldOrder;

import net.jcip.annotations.Immutable;

import java.util.Comparator;


/**
 * Specification for a field in a DpFile
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@Immutable
class DpField {
    final static Comparator<DpField> COMPARATOR_BY_ID = new Comparator<DpField>() {
        public int compare(DpField o1, DpField o2) {
            return o1.fieldId - o2.fieldId;
        }
    };

    final static Comparator<DpField> COMPARATOR_BY_ORDER = new Comparator<DpField>() {
        public int compare(DpField o1, DpField o2) {
            return o1.orderId - o2.orderId;
        }
    };

    static final int OID_TIMEOFARR = -4;

    static final int OID_DATEOFARR = -3;

    static final int OID_CREATED = -2;

    static final int OID_UPDATED = -1;

    private final int fieldId;

    private final int orderId;

    private final String alias;

    private final VwdFieldDescription.Type type;

    DpField(int fieldId, String alias) {
        this.alias = alias;
        this.fieldId = fieldId;
        this.type = VwdFieldDescription.getField(this.fieldId).type();

        if (fieldId == VwdFieldDescription.ADF_TIMEOFARR.id()) {
            this.orderId = OID_TIMEOFARR;
        }
        else if (fieldId == VwdFieldDescription.ADF_DATEOFARR.id()) {
            this.orderId = OID_DATEOFARR;
        }
        else if (fieldId == VwdFieldDescription.MMF_Created.id()) {
            this.orderId = OID_CREATED;
        }
        else if (fieldId == VwdFieldDescription.MMF_Updated.id()) {
            this.orderId = OID_UPDATED;
        }
        else {
            this.orderId = VwdFieldOrder.getOrder(this.fieldId);
        }
    }

    public String toString() {
        return "FieldAlias["
                + VwdFieldDescription.getField(this.fieldId).name()
                + "=" + this.alias
                + "/" + this.orderId
                + "]";
    }

    String getAlias() {
        return this.alias;
    }

    int getFieldId() {
        return this.fieldId;
    }

    int getOrderId() {
        return this.orderId;
    }

    VwdFieldDescription.Type getType() {
        return this.type;
    }
}
