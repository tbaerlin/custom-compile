/*
 * LiteralSnapRecord.java
 *
 * Created on 12.11.12 10:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

/**
 * @author tkiesgen
 */
public class LiteralSnapRecord implements SnapRecord {
    private final Map<Integer, SnapField> fields;

    public LiteralSnapRecord(Map<Integer, SnapField> fields) {
        this.fields = fields;
    }

    @Override
    public Collection<SnapField> getSnapFields() {
        final ArrayList<SnapField> result = new ArrayList<>(this.fields.values());
        Collections.sort(result,new Comparator<SnapField>() {
            public int compare(SnapField o1, SnapField o2) {
                return o2.getId() - o1.getId();
            }
        });
        return result;
    }

    @Override
    public SnapField getField(int fieldId) {
        final SnapField field = this.fields.get(fieldId);
        return field != null ? field : NullSnapField.INSTANCE;
    }

    @Override
    public SnapField getField(String fieldname) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }
}
