/*
 * ListRequestResult.java
 *
 * Created on 13.07.2006 11:59:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListResult {
    public static class Sort {
        private final String name;

        private final boolean ascending;

        public Sort(String name, boolean ascending) {
            this.name = name;
            this.ascending = ascending;
        }

        public String getName() {
            return this.name;
        }

        public boolean isAscending() {
            return this.ascending;
        }
    }

    private static final Pattern SORT_PATTERN
            = Pattern.compile("(\\w+)( +(asc|desc))?", Pattern.CASE_INSENSITIVE);
    
    private int offset;

    private int totalCount;

    private int count;

    private int requestedCount;

    private List<Sort> sorts;

    private List<String> sortFields;

    public static ListResult create(ListCommand lc, List<String> sortFields, String defaultSortBy, int totalCount) {
        final ListResult r = new ListResult();
        r.setSortFields(sortFields);
        r.setOffset(lc.getOffset());
        r.setRequestedCount(lc.getCount());
        r.setTotalCount(totalCount);
        if (StringUtils.hasText(lc.getSortBy())) {
            r.sorts = parseSortBy(lc, sortFields);
        }
        if (r.sorts == null || r.sorts.isEmpty()) {
            r.setSort(defaultSortBy, lc.isAscending());
        }
        return r;
    }

    public static List<Sort> parseSortBy(ListCommand lc, List<String> sortFields) {
        return parseSortBy(lc.getSortBy(), lc.isAscending(), sortFields);
    }

    public static List<Sort> parseSortBy(String sortBy, boolean ascending, List<String> sortFields) {
        if (!StringUtils.hasText(sortBy)) {
            return Collections.emptyList();
        }
        
        final List<Sort> sorts = new ArrayList<>(4);
        final String[] subsorts = sortBy.split(",");
        for (String subsort : subsorts) {
            final Sort s = createSort(subsort.trim(), ascending);
            if (s != null && sortFields.contains(s.getName())) {
                sorts.add(s);
            }
        }
        return sorts;
    }

    private static Sort createSort(String sortBy, boolean ascending) {
        final Matcher matcher = SORT_PATTERN.matcher(sortBy);
        if (matcher.matches()) {
            final String direction = matcher.group(2);
            return new Sort(matcher.group(1),
                    (direction != null) ? direction.toLowerCase().endsWith("asc") : ascending);
        }
        return null;
    }

    public boolean isAscending() {
        return this.sorts.get(0).isAscending();
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public String getSortedBy() {
        return this.sorts.get(0).getName();
    }

    public void setSort(String sortedBy, boolean ascending) {
        this.sorts = Collections.singletonList(new Sort(sortedBy, ascending));
    }

    public List<Sort> getSorts() {
        return this.sorts;
    }

    public List<String> getSortFields() {
        return sortFields;
    }

    public void setSortFields(List<String> sortFields) {
        this.sortFields = sortFields;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getRequestedCount() {
        return requestedCount;
    }

    public void setRequestedCount(int requestedCount) {
        this.requestedCount = requestedCount;
    }
}
