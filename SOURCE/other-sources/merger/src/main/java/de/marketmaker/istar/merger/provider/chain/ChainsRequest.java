package de.marketmaker.istar.merger.provider.chain;

import de.marketmaker.istar.common.request.AbstractIstarRequest;

/**
 *
 */
public class ChainsRequest extends AbstractIstarRequest {
    protected static final long serialVersionUID = 1L;

    private String query;
    private int offset = 0;
    private int count = 10;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

}
