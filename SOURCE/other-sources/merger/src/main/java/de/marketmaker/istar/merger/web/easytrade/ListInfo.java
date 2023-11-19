package de.marketmaker.istar.merger.web.easytrade;


import de.marketmaker.istar.merger.provider.IstarListRequest;

import java.io.Serializable;
import java.util.Collections;
import java.util.Set;

public class ListInfo implements Serializable {
    protected static final long serialVersionUID = 1L;

    private final int offset;

    private final int count;

    private final int totalCount;

    private final int requestedCount;

    private final Set<String> sortFields;

    private final boolean ascending;

    private final String sortedBy;


    public ListInfo(int offset, int count, int totalCount, int requestedCount, Set<String> sortFields,
                    boolean ascending, String sortedBy) {
        this.offset = offset;
        this.count = count;
        this.totalCount = totalCount;
        this.requestedCount = requestedCount;
        this.sortFields = sortFields;
        this.ascending = ascending;
        this.sortedBy = sortedBy;
    }

    @Override
    public String toString() {
        return '[' + offset + ".." + (count + offset) + '/' + totalCount + ']';
    }

    public static ListInfo createEmptyInfo(IstarListRequest req) {
        return new ListInfo(req.getOffset(), 0, 0, req.getCount(),
                Collections.<String>emptySet(), req.isAscending(), req.getSortBy());
    }

    public static ListInfo createEmptyInfo() {
        return new ListInfo(0, 0, 0, 0,
                Collections.<String>emptySet(), false, "");
    }

    public int getOffset() {
        return offset;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getCount() {
        return count;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public boolean getAscending() {
        return ascending;
    }

    public String getSortedBy() {
        return sortedBy;
    }

    public Set<String> getSortFields() {
        return sortFields;
    }

}
