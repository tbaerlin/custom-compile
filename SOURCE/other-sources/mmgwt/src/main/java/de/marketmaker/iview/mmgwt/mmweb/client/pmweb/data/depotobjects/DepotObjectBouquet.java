package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects;

import java.util.List;

/**
 * Created on 19.12.12 10:59
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 *
 *
 * The idea of this class is to be a container for a depotobject and its referenced depotobjects.
 * E.g. an investor is the root object and it has references of portfolios, accounts and depots.
 * An other case might be the portfolio as the rootobject and references the other ones as lists of a, b and c.
 *
 */

public class DepotObjectBouquet<R, A, B, C> {

    private final R rootObject;
    private final List<A> as;
    private final List<B> bs;
    private final List<C> cs;

    public DepotObjectBouquet(R rootObject, List<A> as, List<B> bs, List<C> cs) {
        this.rootObject = rootObject;
        this.as = as;
        this.bs = bs;
        this.cs = cs;
    }

    public R getRootObject() {
        return rootObject;
    }

    public List<A> getAs() {
        return this.as;
    }

    public List<B> getBs() {
        return this.bs;
    }

    public List<C> getCs() {
        return this.cs;
    }

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DepotObjectBouquet)) return false;

        final DepotObjectBouquet that = (DepotObjectBouquet) o;

        if (as != null ? !as.equals(that.as) : that.as != null) return false;
        if (bs != null ? !bs.equals(that.bs) : that.bs != null) return false;
        if (cs != null ? !cs.equals(that.cs) : that.cs != null) return false;
        if (rootObject != null ? !rootObject.equals(that.rootObject) : that.rootObject != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = rootObject != null ? rootObject.hashCode() : 0;
        result = 31 * result + (as != null ? as.hashCode() : 0);
        result = 31 * result + (bs != null ? bs.hashCode() : 0);
        result = 31 * result + (cs != null ? cs.hashCode() : 0);
        return result;
    }
}