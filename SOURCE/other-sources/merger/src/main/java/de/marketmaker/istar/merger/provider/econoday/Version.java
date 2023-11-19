/*
 * Version.java
 *
 * Created on 25.03.11 09:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.econoday;

/**
 * @author zzhao
 */
public abstract class Version {

    private final long revision;

    protected Version(long revision) {
        this.revision = revision;
    }

    public long getRevision() {
        return revision;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Version version = (Version) o;
        return revision == version.revision;
    }

    @Override
    public int hashCode() {
        return (int) (revision ^ (revision >>> 32));
    }
}
