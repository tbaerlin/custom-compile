/*
 * ValueType.java
 *
 * Created on 15.03.12 16:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

/**
 * @author zzhao
 */
public class AssessmentType extends Version {

    private final String name;

    private final String prefix;

    private final String suffix;

    AssessmentType(long revision, String name, String prefix, String suffix) {
        super(revision);
        this.name = name;
        this.prefix = prefix;
        this.suffix = suffix;
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    @Override
    public String toString() {
        return "ValueType{" +
                "name='" + name + '\'' +
                ", prefix='" + prefix + '\'' +
                ", suffix='" + suffix + '\'' +
                "} " + super.toString();
    }
}
