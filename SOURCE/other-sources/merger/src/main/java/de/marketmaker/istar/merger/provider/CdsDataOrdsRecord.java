/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class CdsDataOrdsRecord {

    @JsonProperty("vwdkey")
    private String vwdKey;

    private List<CdsDataOrdsRowset> rowset;

    public String getVwdKey() {
        return vwdKey;
    }

    public void setVwdKey(String vwdKey) {
        this.vwdKey = vwdKey;
    }

    public List<CdsDataOrdsRowset> getRowset() {
        return rowset;
    }

    public void setRowset(List<CdsDataOrdsRowset> rowset) {
        this.rowset = rowset;
    }

    @Override
    public String toString() {
        return "CdsDataOrdsRecord{" +
            "vwdKey='" + vwdKey + '\'' +
            ", rowset=" + rowset +
            '}';
    }
}
