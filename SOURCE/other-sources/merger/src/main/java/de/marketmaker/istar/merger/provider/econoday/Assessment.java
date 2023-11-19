/*
 * Value.java
 *
 * Created on 15.03.12 16:07
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author zzhao
 */
public class Assessment implements Serializable {

    private static final long serialVersionUID = -7429352488954113928L;

    private final AssessmentType type;

    private final Map<ValueTypeEnum, String> values = new TreeMap<>();


    Assessment(AssessmentType type) {
        this.type = type;
    }

    public AssessmentType getType() {
        return type;
    }

    void addValue(ValueTypeEnum valueType, String val) {
        this.values.put(valueType, val);
    }

    public Map<ValueTypeEnum, String> getValues() {
        return values;
    }

    @Override
    public String toString() {
        return "Assessment{" +
                "type=" + type +
                ",values=" + values +
                '}';
    }
}
