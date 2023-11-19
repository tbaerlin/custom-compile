/*
 * HistoryThreadLogWindow.java
 *
 * Created on 19.11.2012 13:41:17
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.history;

import com.google.gwt.dom.client.Document;
import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.HistoryThreadHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ActiveWindow;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Michael Lösch
 */
@NonNLS
public class HistoryThreadLogWindow implements HistoryThreadHandler, PlaceChangeHandler {
    private final ActiveWindow window;
    private final HistoryThreadManager historyThreadManager;

    private static HistoryThreadLogWindow htlw = null;

    @SuppressWarnings("JSUnusedLocalSymbols")
    private static native Document createWindow() /*-{
        var win = $wnd.open("", "logwindow", "dependent=yes,width=800,height=800,location=no,menubar=no,resizable=yes,scrollbars=yes,status=no,toolbar=no"); // $NON-NLS-0$ $NON-NLS-1$ $NON-NLS-2$
        var doc = win.document;
        doc.open();
        doc.write("<html><body onunload=\"\"></body></html>"); // $NON-NLS-0$
        doc.close();
        win.onunload = function (evt) {
            @de.marketmaker.iview.mmgwt.mmweb.client.history.HistoryThreadLogWindow::onUnload()();
        };
        return doc;
    }-*/;

    public static void onUnload() {
        EventBusRegistry.get().removeHandler(HistoryThreadEvent.getType(), htlw);
    }

    public static HistoryThreadLogWindow createHTLogWindow(HistoryThreadManager historyThreadManager) {
        htlw = new HistoryThreadLogWindow(historyThreadManager);
        return htlw;
    }

    private HistoryThreadLogWindow(HistoryThreadManager historyThreadManager) {
        this.historyThreadManager = historyThreadManager;
        this.window = new ActiveWindow(createWindow());
        this.window.loadStylesheet("logwindow.css");
        this.window.add(createThreadTable(), true);
        EventBusRegistry.get().addHandler(HistoryThreadEvent.getType(), this);
        EventBusRegistry.get().addHandler(PlaceChangeEvent.getType(), this);
    }

    @Override
    public void onHistoryThreadChange(HistoryThreadEvent event) {
        this.window.clear();
        this.window.add(createThreadTable(), true);
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        onHistoryThreadChange(null);
    }

    private String createThreadTable() {
        final StringBuilder sb = new StringBuilder();
        final HistoryThread activeThread = this.historyThreadManager.getActiveThread();
        final List<Integer> allHistoryThreadIds = this.historyThreadManager.getAllHistoryThreadIds();
        final HashMap<Integer, List<HistoryItem>> threadsAndItems = new LinkedHashMap<>();

        for (Integer threadId : allHistoryThreadIds) {
            threadsAndItems.put(threadId, this.historyThreadManager.getHistoryThread(threadId).getItems());
        }
        final int maxItems = getMaxLength(threadsAndItems.values());
        final String[][] rowsAndCells = new String[allHistoryThreadIds.size()][maxItems + 1];

        for (int m = 0, allHistoryThreadNamesSize = allHistoryThreadIds.size(); m < allHistoryThreadNamesSize; m++) {
            final int threadId = allHistoryThreadIds.get(m);
            final StringBuilder head = new StringBuilder();
            head.append("<u>");
            if (threadId == activeThread.getId()) {
                head.append("<b>").append(threadId).append("</b>");
            }
            else {
                head.append(threadId);
            }
            head.append("</u>");

            final SafeHtml threadTitle = this.historyThreadManager.getThreadTitle(threadId);
            if(threadTitle != null) {
                head.append("<br/>").append(threadTitle.asString());
            }

            rowsAndCells[m][0] = head.toString();
        }
        for (int i = 0; i < maxItems; i++) {
            for (int n = 0; n < allHistoryThreadIds.size(); n++) {
                final int threadId = allHistoryThreadIds.get(n);
                final List<HistoryItem> items = threadsAndItems.get(threadId);
                final HistoryItem currentItem = this.historyThreadManager.getHistoryThread(threadId).getActiveItem();
                if (items.size() > i) {
                    final HistoryItem item = items.get(i);
                    final PlaceChangeEvent placeChangeEvent = item.getPlaceChangeEvent();
                    final String historyToken = (item.isBreadCrumb() ? "[BC] " : "") +
                            " hid: " + placeChangeEvent.getHistoryToken().getHistoryId() + " " +
                            placeChangeEvent.getHistoryToken() +
                            (placeChangeEvent.isExplicitHistoryNullContext() ? " NullContext" : "") +
                            (placeChangeEvent.getHistoryContext() == null
                                    ? ""
                                    : (" HC: " + placeChangeEvent.getHistoryContext().getClass().getSimpleName() +
                                    " \"" + placeChangeEvent.getHistoryContext().getName()) +
                                    " \" [" + placeChangeEvent.getHistoryContext().getIconKey() + "] " +
                                    " \"" + placeChangeEvent.toDebugString()
                            );

                    final SafeHtml debugHint = item.getDebugHint();
                    final String debugHintTextRaw = debugHint != null ?
                            historyToken + " hint: \"" + debugHint.asString() + "\"": historyToken;

                    rowsAndCells[n][i + 1] = CompareUtil.equals(currentItem.getHid(), item.getHid()) ?
                            "<b>" + debugHintTextRaw + "</b>" : debugHintTextRaw;
                }
            }
        }

        sb.append("<table style=\"width: 100%;\" cellpadding=\"0\" cellspacing=\"0\" border=\"1px solid\">");

        for (int n = 0; n < maxItems + 1; n++) {
            sb.append("<tr>");
            for (final String[] cells : rowsAndCells) {
                if (cells.length > n) {
                    sb.append("<td>").append(cells[n]).append("</td>");
                }
                else {
                    sb.append("<td>&nbsp;</td>");
                }
            }
            sb.append("</tr>");
        }

        sb.append("</table>");
        return sb.toString();
    }

    private int getMaxLength(Collection<List<HistoryItem>> values) {
        int max = 0;
        for (List<HistoryItem> value : values) {
            if (value.size() > max) {
                max = value.size();
            }
        }
        return max;
    }
}
