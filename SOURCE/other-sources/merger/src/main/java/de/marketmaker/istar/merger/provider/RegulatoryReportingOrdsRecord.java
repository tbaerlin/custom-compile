/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

public class RegulatoryReportingOrdsRecord {

    private String iid;

    private List<RegulatoryReportingOrdsRowset> rowset;

    public String getIid() {
        return iid;
    }

    public void setIid(String iid) {
        this.iid = iid;
    }

    public List<RegulatoryReportingOrdsRowset> getRowset() {
        return rowset;
    }

    public void setRowset(List<RegulatoryReportingOrdsRowset> rowset) {
        this.rowset = rowset;
    }

    @Override
    public String toString() {
        return "RegulatoryReportingOrdsRecord{" +
            "iid='" + iid + '\'' +
            ", rowset=" + rowset +
            '}';
    }
}
