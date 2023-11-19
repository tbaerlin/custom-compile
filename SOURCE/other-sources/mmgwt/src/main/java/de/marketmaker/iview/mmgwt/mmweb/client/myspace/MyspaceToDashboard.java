package de.marketmaker.iview.mmgwt.mmweb.client.myspace;

import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.TableStructureModel;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MyspaceConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Author: umaurer
 * Created: 29.10.15
 */
public class MyspaceToDashboard {
    public static void convertSnippetConfigs(MyspaceConfig mc, DashboardConfig dc) {
        final TableStructureModel<SnippetConfiguration> tsm = new TableStructureModel<>();

        final ArrayList<SnippetConfiguration> list = mc.getSnippetConfigs();
        Collections.sort(list, SnippetConfigPositionComparator.INSTANCE);
        int col = 0;
        int row = 0;
        int lastX = 0;
        int lastY = 0;
        for (SnippetConfiguration sc : list) {
            final int x = sc.getInt("x", 1000); // $NON-NLS$
            final int y = sc.getInt("y", 1000); // $NON-NLS$
            if (x < lastX || y > lastY + 100) {
                row++;
                col = 0;
            }
            lastX = x;
            lastY = y;
            final int colSpan = getSpan(sc, "w"); // $NON-NLS$
            final int rowSpan = getSpan(sc, "h"); // $NON-NLS$
            while (!tsm.isFree(row, col, rowSpan, colSpan)) {
                col++;
            }
            final SnippetConfiguration scCopy = removeParameters(sc.copy(), "x", "y", "w", "h", "z"); // $NON-NLS$
            setDashboardParams(scCopy, row, col, rowSpan, colSpan);
            tsm.add(scCopy, row, col, rowSpan, colSpan);
            dc.addSnippet(scCopy);
            col++;
        }
    }

    private static int getSpan(SnippetConfiguration sc, String key) {
        final int span = (sc.getInt(key, 300) - 150) / 300 + 1;
        return span < 1 ? 1 : span;
    }

    private static SnippetConfiguration removeParameters(SnippetConfiguration sc, String... keys) {
        for (String key : keys) {
            sc.put(key, null);
        }
        return sc;
    }

    private static void setDashboardParams(SnippetConfiguration sc, int row, int col, int rowSpan, int colSpan) {
        sc.put("context", Snippet.CONFIG_CONTEXT_DASHBOARD); // $NON-NLS$
        sc.put("row", row); // $NON-NLS$
        sc.put("col", col); // $NON-NLS$
        if (rowSpan != 1 || colSpan != 1) {
            sc.put("rowSpan", rowSpan); // $NON-NLS$
            sc.put("colSpan", colSpan); // $NON-NLS$
        }
    }
}
