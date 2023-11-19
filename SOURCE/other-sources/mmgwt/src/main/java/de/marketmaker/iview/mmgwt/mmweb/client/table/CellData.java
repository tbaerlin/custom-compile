package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.List;

/**
 * Created on 22.09.2010 09:36:06
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public abstract class CellData<T> {

    public enum Sorting {
        ASC,
        DESC,
        NONE
    }

    protected final T value;
    private final Renderer<T> renderer;
    private int rank = -1;
    protected final Sorting sorting;
    private final String nullValue;
    private final boolean asHtml;

    protected CellData(Renderer<T> renderer, T value, String nullValue, Sorting sorting, boolean asHtml) {
        this.renderer = renderer;
        this.value = value;
        this.sorting = sorting;
        this.nullValue = nullValue;
        this.asHtml = asHtml;
    }

    protected CellData(Renderer<T> renderer, T value, Sorting sorting, boolean asHtml) {
        this(renderer, value, "", sorting, asHtml);
    }

    protected CellData(Renderer<T> renderer, T value, String nullValue, Sorting sorting) {
        this(renderer, value, nullValue, sorting, false);
    }

    protected CellData(Renderer<T> renderer, T value, Sorting sorting) {
        this(renderer, value, "", sorting, false);
    }

    public String getRenderedValue() {
        if (this.renderer != null) {
            return this.renderer.render(this.value);
        } else if (this.value != null) {
            return this.value.toString();
        } else {
            return this.nullValue;
        }
    }

    public void setRank(int rank) {        
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public Sorting getSorting() {
        return sorting;
    }

    public abstract void computeRanking(List<? extends CellData> data);

    @Override
    public String toString() {
        return this.getRenderedValue();
    }

    @Override
    public boolean equals(Object o) {          
        if (this == o) return true;
        if (!(o instanceof CellData)) return false;

        CellData cellData = (CellData) o;

        return value.equals(cellData.value);

    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public boolean isHtml() {
        return this.asHtml;
    }
}
