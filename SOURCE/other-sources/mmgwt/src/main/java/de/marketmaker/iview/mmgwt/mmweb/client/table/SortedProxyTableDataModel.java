package de.marketmaker.iview.mmgwt.mmweb.client.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.Command;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ulrich Maurer
 *         Date: 20.01.12
 */
public class SortedProxyTableDataModel implements TableDataModel, LinkListener<String> {
    private final Comparator<SortMapEntry> comparatorAsc;
    private final Comparator<SortMapEntry> comparatorDesc;

    private static final ValueMapping DEFAULT_VALUE_MAPPING = new ValueMapping() {
        public String toString(Object value, String columnKey) {
            if (value instanceof String) {
                return (String) value;
            }
            if (value instanceof QuoteWithInstrument) {
                return ((QuoteWithInstrument)value).getName();
            }
            return value.toString();
        }
    };

    private TableDataModel delegate = null;
    private String sortedBy = null;
    private boolean ascending = true;
    private final Command onSorted;
    private SortMapEntry[] sortMapping;
    private final Map<String, Integer> mapSortColumns;
    private final ValueMapping valueMapping;
    
    public static interface ValueMapping {
        String toString(Object value, String columnKey);
    }
    
    public class SortMapEntry {
        private int row;

        SortMapEntry(int row) {
            this.row = row;
        }

        @Override
        public String toString() {
            if (sortedBy == null) {
                return String.valueOf(this.row);
            }
            return getValueAt(this.row, sortedBy);  
        }
    }

    public static SortedProxyTableDataModel create(TableColumnModel model, Command onSorted, ValueMapping valueMapping) {
        final Map<String, Integer> mapSortColumns = getSortColumnsMap(model);
        return new SortedProxyTableDataModel(mapSortColumns, onSorted, valueMapping);
    }

    public static SortedProxyTableDataModel create(TableColumnModel model, Command onSorted, ValueMapping valueMapping,
                                                   Comparator<SortMapEntry> ascComparator,
                                                   Comparator<SortMapEntry> descComparator) {
        final Map<String, Integer> mapSortColumns = getSortColumnsMap(model);
        return new SortedProxyTableDataModel(mapSortColumns, onSorted, valueMapping, ascComparator, descComparator);
    }

    private static Map<String, Integer> getSortColumnsMap(TableColumnModel model) {
        Map<String, Integer> mapSortColumns = new HashMap<>();
        for (int i = 0; i < model.getColumnCount(); i++) {
            final String sortKey = model.getTableColumn(i).getSortKey();
            if (sortKey != null) {
                mapSortColumns.put(sortKey, i);
            }
        }
        return mapSortColumns;
    }

    public static SortedProxyTableDataModel create(TableColumn[] tableColumns, Command onSorted, ValueMapping valueMapping) {
        Map<String, Integer> mapSortColumns = new HashMap<>();
        for (int row = 0; row < tableColumns.length; row++) {
            final String sortKey = tableColumns[row].getSortKey();
            if (sortKey != null) {
                mapSortColumns.put(sortKey, row);
            }
        }
        return new SortedProxyTableDataModel(mapSortColumns, onSorted, valueMapping);
    }

    public SortedProxyTableDataModel(Map<String, Integer> mapSortColumns, Command onSorted, ValueMapping valueMapping) {
        this(mapSortColumns, onSorted, valueMapping,
                NaturalComparator.<SortMapEntry>createDefault(),
                NaturalComparator.<SortMapEntry>createDescending());
    }

    public SortedProxyTableDataModel(Map<String, Integer> mapSortColumns, Command onSorted, ValueMapping valueMapping, Comparator<SortMapEntry> asc, Comparator<SortMapEntry> desc) {
        this.comparatorAsc = asc;
        this.comparatorDesc = desc;

        this.onSorted = onSorted;
        this.mapSortColumns = mapSortColumns;
        this.valueMapping = valueMapping;
    }
    
    public void setDelegate(TableDataModel delegate) {
        this.delegate = delegate;
        final int rowCount = delegate.getRowCount();
        this.sortMapping = new SortMapEntry[rowCount];
        for (int row = 0; row < rowCount; row++) {
            this.sortMapping[row] = new SortMapEntry(row);
        }
        setSortedBy(null, true);
    }

    public int getColumnCount() {
        return this.delegate.getColumnCount();
    }

    public int getRowCount() {
        return this.delegate.getRowCount();
    }
    
    private int getDelegateRow(int row) {
        return this.sortMapping[row].row;
    }

    public Object getValueAt(int row, int column) {
        return this.delegate.getValueAt(getDelegateRow(row), column);
    }
    
    public String getValueAt(int row, String columnKey) {
        int column = this.mapSortColumns.get(columnKey);
        final Object value = this.delegate.getValueAt(row, column);
        if (value == null) {
            return null;
        }
        if (this.valueMapping != null) {
            final String result = this.valueMapping.toString(value, this.sortedBy);
            if (result != null) {
                return result;
            }
        }
        return DEFAULT_VALUE_MAPPING.toString(value, this.sortedBy);
    }

    public CellMetaData getMetaData(int row, int column) {
        return this.delegate.getMetaData(getDelegateRow(row), column);
    }

    public String getFlipId(int row) {
        return this.delegate.getFlipId(getDelegateRow(row));
    }

    public String getRowClass(int row) {
        return this.delegate.getRowClass(getDelegateRow(row));
    }

    public TableColumn.Sort getSort(String columnKey) {
        if (columnKey == null) {
            return TableColumn.Sort.NOTSORTABLE; 
        }
        if (columnKey.equals(this.sortedBy)) {
            return this.ascending ? TableColumn.Sort.SORTED_ASC : TableColumn.Sort.SORTED_DESC;
        }
        return this.mapSortColumns.containsKey(columnKey) ? TableColumn.Sort.SORTABLE : TableColumn.Sort.NOTSORTABLE;
    }

    public void setSortedBy(String sortedBy, boolean ascending) {
        this.sortedBy = sortedBy;
        this.ascending = ascending;
        Arrays.sort(this.sortMapping, ascending ? this.comparatorAsc : this.comparatorDesc);
    }
    
    public int[] getColumnOrder() {
        return this.delegate.getColumnOrder();
    }

    @Override
    public String getMessage() {
        return this.delegate.getMessage();
    }

    public void onClick(LinkContext<String> context, Element e) {
        this.ascending = !(context.data.equals(this.sortedBy) && this.ascending);
        this.sortedBy = context.data;
        setSortedBy(this.sortedBy, this.ascending);
        this.onSorted.execute();
    }
}
