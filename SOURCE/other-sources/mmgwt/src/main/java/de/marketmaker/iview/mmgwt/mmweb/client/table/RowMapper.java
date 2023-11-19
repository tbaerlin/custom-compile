/*
 * RowMapper.java
 *
 * Created on 24.07.2008 13:12:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface RowMapper<V> {
    Object[] mapRow(V v);
    String getFlipId(V v);
    String getRowClass(int row, V v);
}
