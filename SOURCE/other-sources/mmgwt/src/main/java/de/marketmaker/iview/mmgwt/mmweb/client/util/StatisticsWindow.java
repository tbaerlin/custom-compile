/*
 * LogWindow.java
 *
 * Created on 09.10.2008 11:21:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

import com.google.gwt.dom.client.Document;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import de.marketmaker.iview.mmgwt.mmweb.client.statistics.ServiceStatistics;

/**
 * @author Ulrich Maurer
 */
@SuppressWarnings({"GWTStyleCheck"})
public class StatisticsWindow {
    private static final int INTERVAL = 300;
    private static StatisticsWindow instance = null;

    protected Grid timingGrid;
    private HTMLTable.CellFormatter cellFormatter;

    private final ActiveWindow window;
    private final FlowPanel panel = new FlowPanel();

    private StatisticsWindow() {
        this.window = new ActiveWindow(createWindow());
        this.window.loadStylesheet("statswindow.css"); // $NON-NLS-0$
    }

    private native Document createWindow() /*-{
        var win = $wnd.open("", "statwindow", "dependent=yes,width=800,height=800,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        var doc = win.document;
        doc.open();
        doc.write("<html><body onunload=\"\"></body></html>"); // $NON-NLS-0$
        doc.close();

        win.onunload = function(evt){ @de.marketmaker.iview.mmgwt.mmweb.client.util.StatisticsWindow::onUnload()(); };
        return doc;
    }-*/;

    public static void onUnload() {
        instance = null;
    }

    public static void show() {
        if (instance == null) {
            instance = new StatisticsWindow();
            instance.addGrid();
        }
        updateStats();
    }

    public static void updateStats() {
        if (instance == null) {
            return;
        }
        instance.doUpdateStats();
    }

    private void doUpdateStats() {
        final ServiceStatistics ss = ServiceStatistics.getOrCreate();
        final int[] rq = ss.getAvgRequestTimes();
        final int[] pr = ss.getAvgProcessingTimes();
        final int[] to = ss.getAvgTotalTimes();
        for (int i = 0; i < rq.length; i++) {
            updateRow(i + 1, rq[i], pr[i], to[i]);
        }
        updateRow(rq.length + 1, ss.getMeanRequestTime(), ss.getMeanProcessingTime(), ss.getMeanTotalTime());
        updateRow(rq.length + 2, ss.getLastRequestTime(), ss.getLastProcessingTime(), ss.getLastTotalTime());
        updateWindow();
    }

    private void updateRow(int row, int v1, int v2, int v3) {
        cellFormatter.setStyleName(row, 0, v1 == 0 && v2 == 0 && v3 == 0 ? "zero" : "more");
        updateCellValue(row, 1, v1);
        updateCellValue(row, 2, v2);
        updateCellValue(row, 3, v3);
    }

    private void updateCellValue(int row, int col, int value) {
        timingGrid.setText(row, col, Integer.toString(value));
        cellFormatter.setStyleName(row, col, value == 0 ? "zero" : "more");
    }

    private void addGrid() {
        final ServiceStatistics ss = ServiceStatistics.getOrCreate();
        final int[] rq = ss.getAvgRequestTimes();
        timingGrid = new Grid(rq.length + 3, 4);
        cellFormatter = timingGrid.getCellFormatter();
        timingGrid.setText(0, 1, "Request"); // $NON-NLS-0$
        timingGrid.setText(0, 2, "Processing"); // $NON-NLS-0$
        timingGrid.setText(0, 3, "Total"); // $NON-NLS-0$
        int m = 0;
        for (int i = 0; i < rq.length - 1; i++) {
            final String label = m + " - " + (m + (INTERVAL - 1)) + "ms"; // $NON-NLS-0$ $NON-NLS-1$
            timingGrid.setText(i + 1, 0, label);
            m += INTERVAL;
        }
        timingGrid.setText(rq.length, 0, "> " + m + "ms"); // $NON-NLS-0$ $NON-NLS-1$
        timingGrid.setText(rq.length + 1, 0, "Mean"); // $NON-NLS-0$
        timingGrid.setText(rq.length + 2, 0, "Last"); // $NON-NLS-0$
        panel.add(timingGrid);
        updateWindow();
    }

    private void updateWindow() {
        window.clear();
        window.add(panel.getElement().getInnerHTML(), true);
    }
}