package de.marketmaker.iview.pmxml.internaltypes;


import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.Sort;
import de.marketmaker.iview.pmxml.DocumentMetadata;

import java.io.Serializable;
import java.util.List;

/**
 * Created on 20.04.2015
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */

public class DMSSearchResult extends BlockListType implements Serializable {

    private List<DocumentMetadata> metaData;
    private String fromDate;
    private String toDate;
    private String pagingHandle;

    public DMSSearchResult() {
    }

    public DMSSearchResult(List<DocumentMetadata> metaData, String fromDate, String toDate,
                           String count, String offset, String total, Sort sort, String pagingHandle) {
        this.metaData = metaData;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.pagingHandle = pagingHandle;
        setCount(String.valueOf(count));
        setOffset(String.valueOf(offset));
        setTotal(String.valueOf(total));
        setSort(sort);
    }

    public List<DocumentMetadata> getMetaData() {
        return this.metaData;
    }

    public String getFromDate() {
        return this.fromDate;
    }

    public String getToDate() {
        return this.toDate;
    }

    public String getPagingHandle() {
        return this.pagingHandle;
    }
}