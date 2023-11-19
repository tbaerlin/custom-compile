/*
 * IpoReportWriter.java
 *
 * Created on 22.08.13 08:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.ipomonitor;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;

/**
 * @author oflege
 */
class IpoReportWriter {
    final TreeMap<String, List<Item>> items;

    private final File outputDir;

    IpoReportWriter(Map<String, Item> items, File outputDir) throws IOException {
        this.items = partition(items);
        this.outputDir = outputDir;
        if (!this.outputDir.isDirectory() && !this.outputDir.mkdirs()) {
            throw new IOException("Could not create directory " + this.outputDir.getAbsolutePath());
        }
    }

    public void createReports() throws IOException {
        createSummaryReport();
        for (Map.Entry<String, List<Item>> e : items.entrySet()) {
            final String market = e.getKey();
            createReport(market, e.getValue());
        }
    }

    private void createSummaryReport() throws IOException {
        TreeMap<String, MarketStats> marketStats = new TreeMap<>();
        for (Map.Entry<String, List<Item>> e : items.entrySet()) {
            marketStats.put(e.getKey(), new MarketStats(e.getValue()));
        }
        TreeSet<LocalDate> dates = new TreeSet<>();
        for (MarketStats stats : marketStats.values()) {
            dates.addAll(stats.getDates());
        }
        int n = 0;
        try (PrintWriter pw = getPrintWriter("index")) {
            title(pw, "Summary");
            pw.println("For each market and date, the table shows three numbers:<ol>");
            pw.println("<li>new keys received on that day");
            pw.println("<li>quotes in the instrument index with that key");
            pw.println("<li>~ and with a defined biskey");
            pw.println("</ol><p><table>");

            for (Map.Entry<String, MarketStats> e : marketStats.entrySet()) {
                if (n++ % 24 == 0) {
                    addSummaryHeader(pw, dates);
                }
                pw.append("<tr");
                if (n % 2 == 0) {
                    pw.append(" bgcolor=\"#EEEEEE\"");
                }
                pw.append("><td>").append(e.getKey()).append("</td>");
                for (LocalDate date : dates) {
                    final MarketStats.Daily stats = e.getValue().getStats(date);
                    if (stats == null) {
                        pw.println("<td colspan=\"3\">&nbsp;</td>");
                    }
                    else {
                        td(pw, "<a href=\"" + e.getKey() + "-" + date + ".html\">" + stats.numCreated + "</a>");
                        td(pw, stats.numIndexed, stats.numCreated);
                        td(pw, stats.numComplete, stats.numCreated);
                    }
                }
                pw.println("</tr>");
            }


            pw.println("</table>");
        }
    }

    private void addSummaryHeader(PrintWriter pw, TreeSet<LocalDate> dates) {
        pw.println("<tr><th>market</th>");
        for (LocalDate date : dates) {
            pw.append("<th colspan=\"3\">").append(date.toString()).append("</th>").println();
        }
        pw.println("</tr>");
    }

    private void createReport(String market, List<Item> values) throws IOException {
        LocalDate ld = values.get(0).created().toLocalDate();
        int from = 0;
        for (int i = 1; i < values.size(); i++) {
            if (!ld.equals(values.get(i).created().toLocalDate())) {
                createReport(market, ld, values.subList(from, i));
                from = i;
                ld = values.get(i).created().toLocalDate();
            }
        }
        createReport(market, ld, values.subList(from, values.size()));
    }

    private void createReport(String market, LocalDate ld, List<Item> values) throws IOException {
        try (PrintWriter pw = getPrintWriter(market + "-" + ld)) {
            title(pw, market + " " + ld);
            pw.println("<table>");
            pw.println("<tr><th>vwdcode</th><th>created</th><th>indexed</th><th>biskey</th></tr>");
            for (Item item : values) {
                pw.append("<tr>");
                td(pw, item.vwdcode);
                tdDate(pw, item.created);
                tdDate(pw, item.indexed);
                tdDate(pw, item.biskeyed);
                pw.println("</tr>");
            }
            pw.println("</table>");
            pw.println("</body>");
        }
    }

    private void td(PrintWriter pw, int i, int total) {
        if (i == total) {
            td(pw, i);
        }
        else {
            int x = (i * 255 / total) & 0xF8;
            pw.append("<td class=\"r" + x + "\">").append(Integer.toString(i)).append("</td>");
        }
    }

    private void td(PrintWriter pw, int i) {
        td(pw, Integer.toString(i));
    }

    private void tdDate(PrintWriter pw, int i) {
        // .replace is nasty (I know) but easy to have more readable output
        td(pw, i > 0 ? ISODateTimeFormat.dateHourMinuteSecond().print(new DateTime(i * 1000L)).replace('T',' ') : null);
    }

    private void td(PrintWriter pw, Object o) {
        pw.append("<td>").append(o != null ? o.toString() : "&nbsp;").append("</td>");
    }

    private void title(PrintWriter pw, String title) {
        pw.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01//EN\" \"http://www.w3.org/TR/html4/strict.dtd\">");
        pw.println("<html>");
        pw.println("<head>");
        pw.println("<meta httpEquiv=\"Content-Type\" content=\"text/html; charset=utf-8\"/>");
        pw.append("<title>").append(title).append("</title>").println();
        pw.println("<link href=\"style.css\" rel=\"stylesheet\" type=\"text/css\"/>");
        pw.println("</head>");
        pw.println("<body>");
        pw.append("<h2>").append(title).append("</h2>").println();
    }

    private PrintWriter getPrintWriter(String name) throws IOException {
        return new PrintWriter(new File(this.outputDir, name + ".html"), "utf-8");
    }

    private static TreeMap<String, List<Item>> partition(Map<String, Item> items) {
        final TreeMap<String, List<Item>> result = new TreeMap<>();
        for (Item item : items.values()) {
            List<Item> list = result.get(item.market);
            if (list == null) {
                result.put(item.market, list = new ArrayList<>());
            }
            list.add(item);
        }
        for (List<Item> list : result.values()) {
            list.sort(Item.BY_CREATED);
        }
        return result;
    }
}
