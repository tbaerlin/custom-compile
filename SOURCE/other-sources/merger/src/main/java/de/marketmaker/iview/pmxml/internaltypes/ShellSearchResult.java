package de.marketmaker.iview.pmxml.internaltypes;


import de.marketmaker.iview.dmxml.BlockListType;
import de.marketmaker.iview.dmxml.SearchTypeCount;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ZoneDesc;

import java.util.List;

/**
 * Created on 23.07.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class ShellSearchResult extends BlockListType {

    private List<ShellMMInfo> objects;
    private List<ZoneDesc> zones;
    private List<SearchTypeCount> typeCounts;
    private String pagingHandle;

    public ShellSearchResult() {
    }

    public ShellSearchResult(List<ShellMMInfo> objects, List<ZoneDesc> zones, List<SearchTypeCount> typeCounts,
                             String count, String offset, String total, String pagingHandle) {
        this.objects = objects;
        this.zones = zones;
        this.typeCounts = typeCounts;
        this.pagingHandle = pagingHandle;
        setCount(String.valueOf(count));
        setOffset(String.valueOf(offset));
        setTotal(String.valueOf(total));
    }

    public List<ShellMMInfo> getObjects() {
        return objects;
    }

    public List<ZoneDesc> getZones() {
        return zones;
    }

    public List<SearchTypeCount> getTypeCounts() {
        return typeCounts;
    }

    public String getPagingHandle() {
        return pagingHandle;
    }
}
