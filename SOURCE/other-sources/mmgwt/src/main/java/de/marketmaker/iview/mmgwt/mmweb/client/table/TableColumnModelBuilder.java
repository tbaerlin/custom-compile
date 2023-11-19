package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: umaurer
 * Date: 22.07.13
 * Time: 13:14
 */
public class TableColumnModelBuilder {
    class GroupSpec {
        final String label;
        final int start;
        final int end;

        GroupSpec(String label, int start, int end) {
            this.label = label;
            this.start = start;
            this.end = end;
        }

        String getLabel() {
            return label;
        }

        int getStart() {
            return start;
        }

        int getEnd() {
            return end;
        }
    }

    final List<TableColumn> listColumns = new ArrayList<TableColumn>();
    final List<GroupSpec> listGroups = new ArrayList<GroupSpec>();

    public TableColumnModelBuilder addColumns(TableColumn... tableColumns) {
        this.listColumns.addAll(Arrays.asList(tableColumns));
        return this;
    }

    public TableColumnModelBuilder addGroup(String label, TableColumn... tableColumns) {
        final int start = this.listColumns.size();
        final int end = start + tableColumns.length;
        this.listGroups.add(new GroupSpec(label, start, end));
        addColumns(tableColumns);
        return this;
    }

    public DefaultTableColumnModel asTableColumnModel() {
        final DefaultTableColumnModel tcm = new DefaultTableColumnModel(this.listColumns.toArray(new TableColumn[this.listColumns.size()]));
        for (GroupSpec group : this.listGroups) {
            tcm.groupColumns(group.getStart(), group.getEnd(), group.getLabel());
        }
        return tcm;
    }
}
