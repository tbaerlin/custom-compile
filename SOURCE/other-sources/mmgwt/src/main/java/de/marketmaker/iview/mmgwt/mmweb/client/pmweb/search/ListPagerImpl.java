/*
 * ListPagerImpl.java
 *
 * Created on 28.01.14 16:40
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingFeature;

import java.util.List;

/**
 * @author Markus Dick
 */
public class ListPagerImpl<T> implements PagingFeature.Pager {
    private int offset;
    private int count;
    private List<T> list;

    public ListPagerImpl() {
        this.offset = 0;
        this.count = 0;
    }

    @Override
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public int getTotal() {
        return this.list.size();
    }

    @Override
    public int getOffset() {
        return this.offset;
    }

    @Override
    public int getCount() {
        final int endIndex = this.offset + this.count;
        final int total = getTotal();
        return (total <= endIndex ? (total - this.offset) : this.count);
    }

    @Override
    public boolean isResponseOk() {
        return this.list != null;
    }

    public void setList(List<T> list) {
        this.list = list;
    }

    public List<T> getSublist() {
        return this.list.subList(this.offset, this.offset + getCount());
    }
}
