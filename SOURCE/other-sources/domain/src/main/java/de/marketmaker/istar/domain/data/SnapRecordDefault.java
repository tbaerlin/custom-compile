/*
 * SnapRecordDefault.java
 *
 * Created on 18.07.2005 16:59:47
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapRecordDefault implements SnapRecord {
    private final SnapField[] fields;

    // with lazy init, not needed in most cases
    private Map<String, SnapField> fieldsByName;

    private int[] fieldids;

    private final int nominalDelayInSeconds;

    public SnapRecordDefault(Collection<SnapField> fields) {
        this(fields, 0);
    }

    public SnapRecordDefault(Collection<SnapField> fields, int nominalDelayInSeconds) {
        this.fields = fields.toArray(new SnapField[fields.size()]);
        Arrays.sort(this.fields, SnapFieldComparators.BY_ID);

        int n = fields.size();
        this.fieldids = new int[n];
        for (int j = 0; j < n; j++) {
            this.fieldids[j] = this.fields[j].getId();
        }
        this.nominalDelayInSeconds = nominalDelayInSeconds;
    }

    public SnapField getField(int fieldId) {
        final int n = Arrays.binarySearch(this.fieldids, fieldId);
        return (n < 0) ? NullSnapField.INSTANCE : this.fields[n];
    }

    public SnapField getField(String fieldname) {
        ensureFieldsByName();
        final SnapField result = this.fieldsByName.get(fieldname);
        return (result != null) ? result : NullSnapField.INSTANCE;
    }

    @Override
    public int getNominalDelayInSeconds() {
        return 0;
    }

    public Collection<SnapField> getSnapFields() {
        return Arrays.asList(this.fields);
    }

    public String toString() {
        return "SnapRecordDefault" + getSnapFields();
    }

    private void ensureFieldsByName() {
        if (this.fieldsByName == null) {
            this.fieldsByName = mapFieldsByName();
        }
    }

    private Map<String, SnapField> mapFieldsByName() {
        final Map<String, SnapField> result = new HashMap<>();
        for (int i = 0; i < fields.length; i++) {
            result.put(fields[i].getName(), fields[i]);
        }
        return result;
    }
}
