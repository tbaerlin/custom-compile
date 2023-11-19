package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.IndexPosition;

import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexPositionImpl implements Serializable, IndexPosition{
    protected static final long serialVersionUID = 1L;

    private final long quoteid;
    private final String itemname;

    public IndexPositionImpl(long quoteid, String itemname) {
        this.quoteid = quoteid;
        this.itemname = itemname;
    }

    public long getQuoteid() {
        return quoteid;
    }

    public String getItemname() {
        return itemname;
    }

    public String toString() {
        return "IndexPostionImpl[quoteid=" + quoteid
                + ", itemname=" + itemname
                + "]";
    }
}
