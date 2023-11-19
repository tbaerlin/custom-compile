package de.marketmaker.istar.analyses.analyzer;

import com.google.common.collect.ComparisonChain;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 * data model for an index list
 */
public class Index {

    private final long qid;

    private final String name;

    private final Set<Security> securities = new TreeSet<>(Security.IID_COMPARATOR);

    public static final Comparator<Index> QID_COMPARATOR
            = (left, right) -> ComparisonChain.start().compare(left.qid, right.qid).result();

    public Index(long qid, String name) {
        assert name != null : "name for index must not be null";
        this.qid = qid;
        this.name = name;
    }

    public long getId() {
        return qid;
    }

    public String getName() {
        return name;
    }

    public void put(Security security) {
        securities.add(security);
    }

    public Set<Security> getSecurities() {
        return securities;
    }

    @Override
    public String toString() {
        return "Index [" + qid + "/" + "'" + name + "'"
                + " has: " + securities.size() + " securities"
                + "]";
    }

}
