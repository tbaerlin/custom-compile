/*
 * SortLinkUtil.java
 *
 * Created on 04.03.2009 13:33:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.user.client.Command;
import com.google.gwt.dom.client.Element;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SortLinkSupport implements LinkListener<String> {
    private DmxmlContext.Block block;

    private final boolean resetOffset;

    private final boolean defaultAscending;

    private final Command onSortChange;

    public SortLinkSupport(DmxmlContext.Block block, Command onSortChange, boolean resetOffset,
            boolean defaultAscending) {
        this.block = block;
        this.onSortChange = onSortChange;
        this.resetOffset = resetOffset;
        this.defaultAscending = defaultAscending;
    }

    public SortLinkSupport(DmxmlContext.Block block, Command onSortChange, boolean resetOffset) {
        this(block, onSortChange, resetOffset, true);
    }

    public SortLinkSupport(DmxmlContext.Block block, Command onSortChange) {
        this(block, onSortChange, false, true);
    }

    public void setBlock(DmxmlContext.Block block) {
        this.block = block;
    }

    public void onClick(LinkContext<String> context, Element e) {
        //addSort(context, this.block, this.defaultAscending);
        final String sortBy = context.data;
        final String current = this.block.getParameter("sortBy"); // $NON-NLS$
        if (current.equals(sortBy)) {
            final boolean ascending = !"true".equals(this.block.getParameter("ascending")); // flip ascending value // $NON-NLS$
            this.block.setParameter("ascending", ascending); // $NON-NLS$
        }
        else {
            this.block.setParameter("sortBy", sortBy); // $NON-NLS$
            this.block.setParameter("ascending", this.defaultAscending); // $NON-NLS$
        }
        if (this.resetOffset) {
            this.block.setParameter("offset", "0"); // $NON-NLS$
        }
        this.onSortChange.execute();
    }

    private static boolean isCtrlKey(LinkContext<String> context, DmxmlContext.Block block) {
//        return context.isCtrlKey();
//        return context.isCtrlKey() && block.getKey().equals("CDS_Finder"); // $NON-NLS$
        return false; // disable for now
    }

    public static final String PARAM_KEY_SORT = "sortBy";   // $NON-NLS$

    public static final String PARAM_KEY_ORDER = "ascending";  // $NON-NLS$

    public static final String SEP_SORTS = ",";  // $NON-NLS$

    public static final String SEP_SORT_ORDER = " ";  // $NON-NLS$

    public static void addSort(LinkContext<String> context, DmxmlContext.Block block,
            boolean defaultAscending) {
        final String sortByReq = context.getData();
        final String sortByNow = block.getParameter(PARAM_KEY_SORT);
        final boolean orderNow = Boolean.parseBoolean(block.getParameter(PARAM_KEY_ORDER));
        if (null == sortByNow || sortByNow.trim().length() == 0) {
            block.setParameter(PARAM_KEY_SORT, sortByReq);
            block.setParameter(PARAM_KEY_ORDER, defaultAscending);
        }
        else {
            final String[] sortFields = sortByNow.split(SEP_SORTS);
            for (int i = 0; i < sortFields.length; i++) {
                final String sortField = sortFields[i];
                final String[] split = sortField.split(SEP_SORT_ORDER);
                final String fieldName = split[0];
                final boolean order = split.length == 2 ? getOrder(split[1]) : orderNow;
                if (fieldName.equals(sortByReq)) {
                    final String pre = joinSortFields(sortFields, 0, i);
                    final String changed = createSort(fieldName, !order);
                    final String suf = joinSortFields(sortFields, i + 1, sortFields.length);

                    block.setParameter(PARAM_KEY_SORT, isCtrlKey(context, block)
                            ? joinSortFields(pre, changed, suf)
                            : changed);
                    block.setParameter(PARAM_KEY_ORDER, !order);
                    return;
                }
            }

            final String sortByNew = isCtrlKey(context, block)
                    // multiple sort fields
                    ? join(sortFields, 0, sortFields.length, createSort(sortByReq, defaultAscending))
                    // replace sort fields
                    : createSort(sortByReq, defaultAscending);
            block.setParameter(PARAM_KEY_SORT, sortByNew);
            block.setParameter(PARAM_KEY_ORDER, defaultAscending);
        }
    }

    private static String createSort(String sortField, boolean order) {
        return sortField + SEP_SORT_ORDER + fromOrder(order);
    }

    private static String joinSortFields(String[] sortBys, int from, int to) {
        return join(sortBys, from, to, null);
    }

    private static String join(String[] sortBys, int from, int to, String last) {
        if (from >= to) {
            return last;
        }

        final StringBuilder sb = new StringBuilder();
        for (int i = from; i < to; i++) {
            sb.append(sortBys[i]).append(SEP_SORTS);
        }

        if (null != last && last.trim().length() > 0) {
            sb.append(last);
        }
        else {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private static String joinSortFields(String... parts) {
        final StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (null != part && part.trim().length() > 0) {
                sb.append(part).append(SEP_SORTS);
            }
        }

        if (sb.length() > 1) {
            sb.setLength(sb.length() - 1);
        }

        return sb.toString();
    }

    private static String fromOrder(boolean order) {
        return order ? "asc" : "desc"; // $NON-NLS$
    }

    private static boolean getOrder(String orderSpec) {
        return "asc".equals(orderSpec.toLowerCase()); // $NON-NLS$
    }
}
