/*
 * DTTableUtils.java
 *
 * Created on 23.04.13 09:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTRowBlock;
import de.marketmaker.iview.pmxml.DTRowGroup;
import de.marketmaker.iview.pmxml.DTSingleRow;
import de.marketmaker.iview.pmxml.DTSortSpec;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

/**
 * @author oflege
 */
public class DTTableUtils {
    public final static CellComparator CELL_COMPARATOR = new CellComparator();

    public static String stripOffBlanksAndLineBreaks(String caption) {
        if(!StringUtil.hasText(caption)) {
            return null;
        }
        return caption.replaceAll("\\s+", " ").replaceAll("-\\s+", "").replaceAll("/\\s+", "/"); //$NON-NLS$
    }

    private interface ExtractContextStrategy<T> {
        boolean exists(T activityInstanceInfo);
        boolean isAcceptable(T activityInstanceInfo, DTSingleRow dtSingleRow);
        T extract(DTSingleRow row);
    }

    private final static ExtractContextStrategy<ActivityInstanceInfo> EXTRACT_ACTIVITY_CONTEXT_STRATEGY =
            new ExtractContextStrategy<ActivityInstanceInfo>() {
                @Override
                public boolean exists(ActivityInstanceInfo activityInstanceInfo) {
                    return activityInstanceInfo != null &&
                            activityInstanceInfo.getDefinition() != null &&
                            activityInstanceInfo.getId() != null &&
                            activityInstanceInfo.getDefinition().getName() != null;
                }

                @Override
                public boolean isAcceptable(ActivityInstanceInfo activityInstanceInfo, DTSingleRow row) {
                    return row.getActivity() != null;
                }

                @Override
                public ActivityInstanceInfo extract(DTSingleRow row) {
                    return row.getActivity();
                }
            };

    private final static ExtractContextStrategy<ShellMMInfo> EXTRACT_SHELL_MM_CONTEXT_STRATEGY =
            new ExtractContextStrategy<ShellMMInfo>() {
                @Override
                public boolean exists(ShellMMInfo shellMMInfo) {
                    return shellMMInfo != null && shellMMInfo.getTyp() != null && shellMMInfo.getId() != null;
                }

                @Override
                public boolean isAcceptable(ShellMMInfo shellMMInfo, DTSingleRow row) {
                    final ShellMMType type = shellMMInfo.getTyp();
                    final ShellMMInfo objectInfo = row.getObjectInfo();

                    return objectInfo != null && (type.equals(objectInfo.getTyp()) ||
                            (ShellMMTypeUtil.isSecurity(type) && ShellMMTypeUtil.isSecurity(objectInfo.getTyp())));
                }

                @Override
                public ShellMMInfo extract(DTSingleRow row) {
                    return row.getObjectInfo();
                }
            };

    public static void collect(DTTable table, Collector<?>... collectors) {
        addValues(table.getToplevelGroup(), Arrays.asList(collectors));
    }

    public static void collect(DTRowGroup group, Collector<?>... collectors) {
        addValues(group, Arrays.asList(collectors));
    }

    private static void addValues(DTRowGroup group, Collection<? extends Collector> collectors) {
        for (DTRowBlock grouped : group.getGroupedRows()) {
            addValues(grouped, collectors);
        }
    }

    private static void addValues(DTRowBlock block, Collection<? extends Collector> collectors) {
        if (block instanceof DTSingleRow) {
            for(Collector<?> collector : collectors) {
                collector.accept((DTSingleRow)block);
            }
        }
        else if (block instanceof DTRowGroup) {
            addValues((DTRowGroup)block, collectors);
        }
    }

    public interface Collector<T> extends Consumer<DTSingleRow>{
        List<T> getValues();
    }

    @SuppressWarnings("unused")
    public static class DistinctColumnValuesCollector implements Collector<DTCell> {
        private final int colIndex;
        protected final HashMap<String, DTCell> distinctValues = new HashMap<>();

        public DistinctColumnValuesCollector(int colIndex) {
            this.colIndex = colIndex;
        }

        @Override
        public List<DTCell> getValues() {
            return new ArrayList<>(this.distinctValues.values());
        }

        @Override
        public void accept(DTSingleRow row) {
            final DTCell dtCell = row.getCells().get(this.colIndex);
            this.distinctValues.put(dtCell.getContent(), dtCell);
        }
    }

    public static class RowCountCollector implements Collector<Integer> {
        int count = 0;

        public int getCount() {
            return this.count;
        }

        @Override
        public List<Integer> getValues() {
            return Collections.singletonList(this.count);
        }

        @Override
        public void accept(DTSingleRow dtSingleRow) {
            if(dtSingleRow == null) {
                return;
            }
            this.count++;
        }
    }

    public static DTRowGroup getFilteredCopy(DTRowGroup group, DTTableRenderer.ColumnFilter filter) {
        final DTRowGroup copy = getFlatCopy(group);
        final List<DTRowBlock> filteredGroupedRows = copy.getGroupedRows();
        for (DTRowBlock groupedRow : group.getGroupedRows()) {
            final DTRowBlock filteredGroupedRow = filter != null ? applyFilter(groupedRow, filter) : groupedRow;
            if(filteredGroupedRow != null) {
                filteredGroupedRows.add(filteredGroupedRow);
            }
        }

        return copy;
    }

    private static DTRowBlock applyFilter(DTRowBlock block, DTTableRenderer.ColumnFilter filter) {
        if(block instanceof DTSingleRow) {
            if(filter.isAcceptable((DTSingleRow)block)) {
                return block;
            }
            return null;
        }
        if(block instanceof DTRowGroup) {
            return getFilteredCopy((DTRowGroup) block, filter);
        }
        return null;
    }

    private static DTRowGroup getFlatCopy(DTRowGroup group) {
        final DTRowGroup copy = new DTRowGroup();
        copy.setHead(group.getHead());
        copy.setAggregatedRow(group.getAggregatedRow());
        return copy;
    }


    public static List<ShellMMInfo> extractContextShellMMInfoItemsFor(ShellMMInfo shellMMInfo, DTTable table,
                                                                      DTTableRenderer.Options options) {
        return extractContextItemsFor(shellMMInfo, table, options, EXTRACT_SHELL_MM_CONTEXT_STRATEGY
        );
    }

    public static List<ActivityInstanceInfo> extractContextActivityItemsFor(ActivityInstanceInfo aai, DTTable table,
                                                                            DTTableRenderer.Options options) {
        return extractContextItemsFor(aai, table, options, EXTRACT_ACTIVITY_CONTEXT_STRATEGY);
    }

    public static <T> List<T> extractContextItemsFor(T t, DTTable table, DTTableRenderer.Options options,
                                                     ExtractContextStrategy<T> strategy) {
        if (!strategy.exists(t)) {
            return Collections.singletonList(t);
        }

        final DTRowGroup currentGroup = table.getToplevelGroup();
        final List<DTSingleRow> rows = new ArrayList<>();

        final ArrayList<DTRowBlock> toExpand = new ArrayList<DTRowBlock>(Collections.singletonList(currentGroup));
        while (!toExpand.isEmpty()) {
            final DTRowBlock block = toExpand.remove(0);
            if (block instanceof DTSingleRow) {
                final DTSingleRow row = (DTSingleRow) block;

                if(isAcceptable(options.getFilter(), row) && strategy.isAcceptable(t, row)) {
                    rows.add(row);
                }
            }
            else if (block instanceof DTRowGroup) {
                for (DTRowBlock toAdd : ((DTRowGroup) block).getGroupedRows()) {
                    toExpand.add(toAdd); //in principle a breadth first expand
                }
            }
        }

        if(options.isWithCustomSort()) {
            Collections.sort(rows, new RowComparator(options.getCustomSort()));
        }

        final List<T> out = new ArrayList<>(rows.size());
        for(DTSingleRow row : rows) {
            out.add(strategy.extract(row));
        }

        return out;
    }

    protected static List<DTRowBlock> expandAllDTRowGroups(List<DTRowBlock> in) {
        final List<DTRowBlock> out = new ArrayList<>();
        final ArrayList<DTRowBlock> toExpand = new ArrayList<>(in);

        while (!toExpand.isEmpty()) {
            final DTRowBlock block = toExpand.remove(0);
            if (block instanceof DTSingleRow) {
                out.add(block);
            }
            else if (block instanceof DTRowGroup) {
                for (DTRowBlock toAdd : ((DTRowGroup) block).getGroupedRows()) {
                    toExpand.add(toAdd);
                }
            }
        }

        return out;
    }

    public static ShellMMInfo findShellMMInfo(String id, DTRowBlock block) {
        if(block == null) {
            return null;
        }

        if (block instanceof DTSingleRow) {
            final ShellMMInfo shellMMInfo = ((DTSingleRow) block).getObjectInfo();
            if(shellMMInfo != null && id.equals(shellMMInfo.getId())) {
                return shellMMInfo;
            }
        }
        else if (block instanceof DTRowGroup) {
            for (DTRowBlock row : ((DTRowGroup) block).getGroupedRows()) {
                final ShellMMInfo found = findShellMMInfo(id, row);
                if(found != null) {
                    return found;
                }
            }
        }

        return null;
    }

    private static boolean isAcceptable(DTTableRenderer.ColumnFilter filter, DTSingleRow row) {
        return filter == null || filter.isAcceptable(row);
    }

    public static class RowComparator implements Comparator<DTRowBlock> {
        private List<DTSortSpec> customSort;

        public RowComparator(List<DTSortSpec> customSort) {
            this.customSort = customSort;
        }

        @Override
        public int compare(DTRowBlock o1, DTRowBlock o2) {
            try {
                final DTSingleRow r1 = (DTSingleRow) o1;
                final DTSingleRow r2 = (DTSingleRow) o2;

                for (DTSortSpec spec : this.customSort) {
                    final int colIndex = Integer.parseInt(spec.getColIndex());
                    final int cmp = compareOrders(
                            r1.getCells().get(colIndex).getOrder(),
                            r2.getCells().get(colIndex).getOrder());
                    if (cmp != 0) {
                        return spec.isAscending() ? cmp : -cmp;
                    }
                }
            } catch (ClassCastException cce) {
                Firebug.warn("ClassCastException while comparing DTSingleRow", cce);
                Firebug.warn("o1 class? " + o1.getClass().getName());
                Firebug.warn("o2 class? " + o2.getClass().getName());
            }
            return 0;
        }
    }

    public static class CellComparator implements Comparator<DTCell> {
        @Override
        public int compare(DTCell o1, DTCell o2) {
            if (o1 == o2) return 0;
            return compareOrders(o1.getOrder(), o2.getOrder());
        }
    }

    private static int compareOrders(String o1, String o2) {
        // orders are always positive integers
        final int cmp = o1.length() - o2.length();
        return (cmp != 0) ? cmp : o1.compareTo(o2);
    }

    public static int getInitialDiagramIndex(DTTable dtTable) {
        if(dtTable == null) {
            return -1;
        }

        return StringUtility.hasText(dtTable.getInitialDiagramIndex())
                ? Integer.parseInt(dtTable.getInitialDiagramIndex())
                : -1;
    }
}
