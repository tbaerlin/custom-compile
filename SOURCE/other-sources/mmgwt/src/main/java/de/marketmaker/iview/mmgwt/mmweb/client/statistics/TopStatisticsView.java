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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author oflege
 */
public class TopStatisticsView extends StatisticsView {

    private final TopStatisticsController controller;

    public TopStatisticsView(TopStatisticsController controller) {
        super();
        this.controller = controller;
    }

    @Override
    protected ListBox initModules() {
        final ListBox result = new ListBox();
        result.setVisibleItemCount(1);
        result.addItem(I18n.I.browser()); 
        result.addItem(I18n.I.ipAddress()); 
        result.addItem(I18n.I.page()); 
        return result;
    }

    private ArrayList<String> getNames(ArrayList<StatsResult.Item> items) {
        final HashSet<String> names = new HashSet<String>();
        for (StatsResult.Item item : items) {
            names.add(item.getName());
        }
        final ArrayList<String> result = new ArrayList<String>(names);
        Collections.sort(result);
        return result;
    }

    ArrayList<Object[]> createRows(ArrayList<StatsResult.Item> items, ArrayList<Integer> periods) {
        final ArrayList<String> names = getNames(items);
        final ArrayList<Object[]> result = new ArrayList<Object[]>(names.size());
        for (String name : names) {
            final Object[] data = new Object[1 + periods.size()];
            result.add(data);
            data[0] = resolveName(name);
        }

        for (StatsResult.Item item : items) {
            final int row = names.indexOf(item.getName());
            final int col = 1 + periods.indexOf(item.getPeriod());
            result.get(row)[col] = Integer.toString(item.getNum());
        }
        return result;
    }

    private String resolveName(String name) {
        if (modules.getSelectedIndex() == 2) {
            return StatsController.metaData.getPages().getPage(name).getName();    
        }
        return name;
    }

    @Override
    protected List<String> getSortFields(ArrayList<Integer> periods) {
        final int sortCount = 1 + periods.size();
        final List<String> list = new ArrayList<String>(sortCount);
        list.add("s-0"); // $NON-NLS-0$
        for (int i = list.size(); i < sortCount; i++) {
            list.add("i-" + i); // $NON-NLS-0$
        }
        return list;
    }

    @Override
    protected DefaultTableColumnModel createColumnModel(ArrayList<Integer> periods) {
        final TableColumn[] columns = new TableColumn[1 + periods.size()];
        columns[0] = new TableColumn(I18n.I.name(), 300, TableCellRenderers.STRING, "s-0");  // $NON-NLS-0$
        for (int i = 0; i < periods.size(); i++) {
            final int colId = 1 + i;
            final String sortKey = "i-" + colId; // $NON-NLS-0$
            columns[colId] = new TableColumn(formatPeriod(periods.get(i)), 60,
                    TableCellRenderers.STRING_RIGHT, sortKey);
        }

        return new DefaultTableColumnModel(columns);
    }

    @Override
    protected void onCompute() {
        final TopStatsCommand cmd = new TopStatsCommand();
        cmd.setClient(SessionData.INSTANCE.getUser().getClient());
        fillCommand(cmd);
        cmd.setSubject(TopStatsCommand.Subject.values()[modules.getSelectedIndex()]);
        this.controller.loadData(cmd);
    }
}
