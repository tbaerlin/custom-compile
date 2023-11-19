/*
 * PageStatisticsView.java
 *
 * Created on 15.01.2010 10:02:39
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import com.google.gwt.user.client.ui.ListBox;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;

import java.util.ArrayList;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author oflege
 */
public class PageStatisticsView extends StatisticsView {

    private final PageStatisticsController controller;

    public PageStatisticsView(PageStatisticsController controller) {
        this.controller = controller;
    }

    @Override
    protected void onCompute() {
        final PageStatsCommand cmd = new PageStatsCommand();
        cmd.setClient(SessionData.INSTANCE.getUser().getClient());
        if (modules.getSelectedIndex() > 0) {
            cmd.setModule(modules.getItemText(modules.getSelectedIndex()));
        }
        fillCommand(cmd);
        this.controller.loadData(cmd);
    }

    @Override
    protected ListBox initModules() {
        final ListBox result = new ListBox();
        result.setVisibleItemCount(1);
        result.addItem(I18n.I.all()); 
        for (String s : getMetaData().getPages().getModuleNames()) {
            result.addItem(s);
        }
        return result;
    }

    ArrayList<Object[]> createRows(ArrayList<StatsResult.Item> items, ArrayList<Integer> periods) {
        final ArrayList<Object[]> result = new ArrayList<Object[]>();
        for (int i = 0; i < items.size(); i++) {
            final Object[] row = new Object[2 + periods.size()];
            StatsResult.Item item = items.get(i);
            final Page page = StatsController.metaData.getPages().getPage(item.getName());
            row[0] = (page != null) ? page.getName() : "???"; // $NON-NLS-0$
            row[1] = (page != null) ? getModule(page) : "???"; // $NON-NLS-0$
            for (int p = 0; p < periods.size(); p++) {
                if (periods.get(p) == item.getPeriod()) {
                    row[2 + p] = Integer.toString(item.getNum());
                    int j = i + 1;
                    if (j < items.size() && item.getName().equals(items.get(j).getName())) {
                        item = items.get(j);
                        i = j;
                    }
                }
            }
            result.add(row);
        }
        return result;
    }

    @Override
    protected List<String> getSortFields(ArrayList<Integer> periods) {
        final int sortCount = 2 + periods.size();
        final List<String> list = new ArrayList<String>(sortCount);
        list.add("s-0"); // $NON-NLS-0$
        list.add("s-1"); // $NON-NLS-0$
        for (int i = list.size(); i < sortCount; i++) {
            list.add("i-" + i); // $NON-NLS-0$
        }
        return list;
    }

    protected DefaultTableColumnModel createColumnModel(ArrayList<Integer> periods) {
        final TableColumn[] columns = new TableColumn[2 + periods.size()];
        columns[0] = new TableColumn(I18n.I.page(), 300, TableCellRenderers.STRING, "s-0");  // $NON-NLS-0$
        columns[1] = new TableColumn(I18n.I.module(), 100, TableCellRenderers.STRING, "s-1");  // $NON-NLS-0$
        for (int i = 0; i < periods.size(); i++) {
            final int colId = 2 + i;
            final String sortKey = "i-" + colId; // $NON-NLS-0$
            columns[colId] = new TableColumn(formatPeriod(periods.get(i)), 60,
                    TableCellRenderers.STRING_RIGHT, sortKey);
        }

        return new DefaultTableColumnModel(columns);
    }

    private String getModule(Page page) {
        return page.getModule() != null ? page.getModule() : ""; // $NON-NLS-0$
    }
}
