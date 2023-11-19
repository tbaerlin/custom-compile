/*
 * NamedIdSetImpl.java
 *
 * Created on 22.06.2007 13:09:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.NamedIdSet;
import net.jcip.annotations.Immutable;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @deprecated use {@link de.marketmaker.istar.domainimpl.data.IndexComposition}
 */
public class NamedIdSetImpl implements NamedIdSet, Serializable {
    protected static final long serialVersionUID = 1L;

    private final String id;
    private String marketStrategy;
    private final String name;
    private final Set<Long> ids;
    private Map<Long, String> namesByQuoteid;

    public NamedIdSetImpl(String id, String name, Set<Long> ids, Map<Long, String> namesByQuoteid) {
        this.id = id;
        this.name = name;
        this.ids = Collections.unmodifiableSet(ids);
        this.namesByQuoteid= namesByQuoteid;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Set<Long> getIds() {
        return ids;
    }

    @Override
    public Set<Long> getQids() {
        return ids;
    }

    @Override
    public Set<Long> getIids() {
        return null;
    }

    @Override
    public String getConstituentsGroup() {
        return null;
    }

    public String getName(long id) {
        return this.namesByQuoteid.get(id);
    }

    public NamedIdSetImpl withMarketStrategy(String marketStrategy) {
        this.marketStrategy = marketStrategy;
        return this;
    }

    public String getMarketStrategy() {
        return marketStrategy;
    }

    public String toString() {
        return "NamedIdSetImpl[id=" + id
                + ", marketStrategy=" + marketStrategy
                + ", name=" + name
                + ", ids=" + ids
                + "]";
    }
}
