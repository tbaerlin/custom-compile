/*
 * ListCommand.java
 *
 * Created on 01.08.2006 13:03:11
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import de.marketmaker.istar.common.validator.RestrictedSet;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.validator.Min;
import de.marketmaker.istar.common.validator.Range;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListCommand {

    private static final boolean DEFAULT_ASCENDING = true;

    private int offset;

    private int count;

    private String sortBy;

    private boolean ascending = DEFAULT_ASCENDING;

    public ListCommand() {
        this(10);
    }

    public ListCommand(int anzahl) {
        this.count = anzahl;
    }

    @MmInternal
    public int getAnzahl() {
        return getCount();
    }

    public void setAnzahl(int anzahl) {
        setCount(anzahl);
    }

    /**
     * @return The number of results that should be returned.
     */
    @Range(min = 0, max = 1000)
    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return Whether to sort the result in ascending order. Default is {@value #DEFAULT_ASCENDING}.
     */
    @RestrictedSet("true,false")
    public boolean isAscending() {
        return ascending;
    }

    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    public void setAscending(String ascending) {
        if (StringUtils.hasText(ascending)) {
            this.ascending = Boolean.parseBoolean(ascending);
        }
    }

    /**
     * Specifies the index of the first result that will be returned.
     *
     * @return the index of the first result.
     */
    @Min(0)
    public int getOffset() {
        return this.offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    /**
     * Specifies how the result should be sorted.
     * Format is <pre>sortfield( asc|desc)?[, sortfield( asc|desc)?]*</pre>
     * that is: multiple sort fields can be separated by commas and each fieldname can have
     * an " asc" or " desc" suffix that indicates the sort order for that field. If no sort order
     * is specified, the value of {@link #isAscending()} will be used for that field.
     *
     * @return expression that defines how to sort
     */
    public String getSortBy() {
        return sortBy;
    }

    /**
     * Specifies how the result should be sorted.
     * Format is <pre>sortfield( asc|desc)?[, sortfield( asc|desc)?]*</pre>
     * that is: multiple sort fields can be separated by commas and each fieldname can have
     * an " asc" or " desc" suffix that indicates the sort order for that field. If no sort order
     * is specified, the value of {@link #isAscending()} will be used for that field.
     *
     * @param sortBy expression that defines how to sort
     */
    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    /**
     * Readonly. Sum of offset and count.
     */
    @MmInternal
    public int getEndOffset() {
        // call methods, may have been overridden!
        return getOffset() + getCount();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ListCommand that = (ListCommand) o;

        if (ascending != that.ascending) return false;
        if (count != that.count) return false;
        if (offset != that.offset) return false;
        if (sortBy != null ? !sortBy.equals(that.sortBy) : that.sortBy != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = offset;
        result = 31 * result + count;
        result = 31 * result + (sortBy != null ? sortBy.hashCode() : 0);
        result = 31 * result + (ascending ? 1 : 0);
        return result;
    }
}
