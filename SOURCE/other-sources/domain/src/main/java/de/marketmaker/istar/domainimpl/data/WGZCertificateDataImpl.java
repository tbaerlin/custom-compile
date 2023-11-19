/*
 * IpoDataImpl.java
 *
 * Created on 15.09.2006 14:08:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.WGZCertificateData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WGZCertificateDataImpl implements WGZCertificateData, Serializable {
    protected static final long serialVersionUID = 1L;

    private final Integer cell;
    private final String name;
    private final String listid;
    private final List<String> columnList;
    private final String sortColumn;
    private final List<Long> instrumentids = new ArrayList<>();

    public WGZCertificateDataImpl(Integer cell, String listid, String name, List<String> columnList, String sortColumn) {
        this.cell = cell;
        this.listid = listid;
        this.name = name;
        this.columnList = columnList;
        this.sortColumn = sortColumn;
    }

    public Integer getCell() {
        return cell;
    }

    public String getListid() {
        return listid;
    }

    public String getName() {
        return name;
    }

    public List<String> getColumnList() {
        return columnList;
    }

    public String getSortColumn() {
        return sortColumn;
    }

    public void add(Long iid) {
        this.instrumentids.add(iid);
    }

    public List<Long> getInstrumentids() {
        return instrumentids;
    }

    @Override
    public String toString() {
        return "WGZCertificateDataImpl{" +
                "cell=" + cell +
                ", listid='" + listid + '\'' +
                ", name='" + name + '\'' +
                ", columnList=" + columnList +
                ", sortColumn='" + sortColumn + '\'' +
                ", instrumentids=" + instrumentids +
                '}';
    }
}