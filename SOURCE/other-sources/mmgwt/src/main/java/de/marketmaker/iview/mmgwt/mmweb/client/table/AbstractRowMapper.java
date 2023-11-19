/*
 * AbstractRowMapper.java
 *
 * Created on 24.07.2008 13:45:29
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class AbstractRowMapper<V> implements RowMapper<V> {
    private String[] rowClasses = null;

    public String getFlipId(V v) {
        return null;
    }

    public String getRowClass(int row, V v) {
        if (this.rowClasses != null) {
            return this.rowClasses[row % this.rowClasses.length];
        }
        return null;
    }

    public RowMapper<V> withAlternatingRowClasses(String[] rowClasses) {
        this.rowClasses = rowClasses;
        return this;
    }
}
