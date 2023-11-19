package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.List;

/**
 * Created on 16.05.13 09:15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class AbstractContextRowMapper<V> extends AbstractRowMapper<V> {

    private List<V> vs;
    private String contextName;

    @Override
    public Object[] mapRow(V v) {
        if (this.vs == null) {
            throw new IllegalStateException("list of all elements needed!"); // $NON-NLS$
        }
        return mapRow(v, this.vs);
    }

    public abstract Object[] mapRow(V v, List<V> list);

    public AbstractContextRowMapper<V> withAllElements(List<V> list) {
        this.vs = list;
        return this;
    }

    public AbstractContextRowMapper<V> withContextName(String name) {
        this.contextName = name;
        return this;
    }

    public String getContextName() {
        return this.contextName;
    }
}
