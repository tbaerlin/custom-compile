package de.marketmaker.itools.gwtutiltest.client.util.date;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RootPanel;
import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

/**
 * @author Ulrich Maurer
 *         Date: 20.02.12
 */
public class DateParserTest {
    private static final String[] DIVIDERS = {"", "-", "-", " ", ":", ":", "?", "?", "?"};
    public static final String COLOR_ERROR = "#ffdddd";
    public static final String COLOR_OK = "#ddffdd";

    private final FlexTable flexTable = new FlexTable();
    private final FlexTable.FlexCellFormatter formatter = flexTable.getFlexCellFormatter();


    public DateParserTest() {
        removeAllWidgets();
        RootPanel.get().add(flexTable);
        flexTable.setCellPadding(5);

        test("2011-03-12", 2011, 3, 12, 0, 0, 0, 0, 0, 0);
        test("2011-01-1", 2011, 1, 1, 0, 0, 0, 0, 0, 0);
        test("Mar/12/2011", 2011, 3, 12, 0, 0, 0, 0, 0, 0);
        test("3/12/2011", 2011, 3, 12, 0, 0, 0, 0, 0, 0);
        test("12.3.2011", 2011, 3, 12, 0, 0, 0, 0, 0, 0);
        test("12.03.2011", 2011, 3, 12, 0, 0, 0, 0, 0, 0);
        test("Mar/12/2011 12:13", 2011, 3, 12, 12, 13, 0, 0, 0, 0);
        test("Mar/12/2011 12:13:14", 2011, 3, 12, 12, 13, 14, 0, 0, 0);
        test("12.03.2011 12:13", 2011, 3, 12, 12, 13, 0, 0, 0, 0);
        test("12.03.2011 12:13:14", 2011, 3, 12, 12, 13, 14, 0, 0, 0);
        test("2011-03-12T12:13:14", 2011, 3, 12, 12, 13, 14, 0, 0, 0);
        test("2011-03-12T12:13:14+01:00", 2011, 3, 12, 12, 13, 14, 0, 1, 0);
        test("2011-03-12T12:13:14.123+01:00", 2011, 3, 12, 12, 13, 14, 123, 1, 0);
        test("2011-03-12T12:13:14+0100", 2011, 3, 12, 12, 13, 14, 0, 100, 0);
        test("3/12/2011 12:13", 2011, 3, 12, 12, 13, 0, 0, 0, 0);
        test("3/12/2011 12:13:14", 2011, 3, 12, 12, 13, 14, 0, 0, 0);
        test("12.03. 12:13", new MmJsDate().getFullYear(), 3, 12, 12, 13, 0, 0, 0, 0);
    }
    
    private void test(String s, int... ymdHmsExpected) {
        final int[] ymdHms;
        try {
            ymdHms = GwtDateParser.getYmdHmsInt(s);
        }
        catch (Exception e) {
            addRow(s, formatYmdHms(ymdHmsExpected), e.getClass().getName(), e.getMessage(), COLOR_ERROR);
            return;
        }
        if (ymdHms == null) {
            addRow(s, formatYmdHms(ymdHmsExpected), "null", "invalid return value: null", COLOR_ERROR);
            return;
        }
        if (ymdHms.length != ymdHmsExpected.length) {
            addRow(s, formatYmdHms(ymdHmsExpected), formatYmdHms(ymdHms), "invalid element count", COLOR_ERROR);
            return;
        }
        for (int i = 0, ymdHmsLength = ymdHms.length; i < ymdHmsLength; i++) {
            if (ymdHms[i] != ymdHmsExpected[i]) {
                addRow(s, formatYmdHms(ymdHmsExpected), formatYmdHms(ymdHms), "invalid element " + (i + 1) + ": " + ymdHms[i] + "!=" + ymdHmsExpected[i], COLOR_ERROR);
                return;
            }
        }
        addRow(s, formatYmdHms(ymdHmsExpected), formatYmdHms(ymdHms), "ok", COLOR_OK);
    }
    
    private void addRow(String... values) {
        int row = flexTable.getRowCount();
        for (int i = 0, valuesLength = values.length - 1; i < valuesLength; i++) {
            this.flexTable.setText(row, i, values[i]);
        }
        this.formatter.getElement(row, values.length - 2).getStyle().setBackgroundColor(values[values.length - 1]);
    }
    
    private String formatYmdHms(int[] ymdHms) {
        final StringBuilder sb = new StringBuilder();
        appendWithZeroPrefix(sb, ymdHms[0], 4);
        for (int i = 1; i < ymdHms.length; i++) {
            sb.append(DIVIDERS[i]);
            appendWithZeroPrefix(sb, ymdHms[i], 2);
        }
        return sb.toString();
    }
    
    private void appendWithZeroPrefix(StringBuilder sb, int nr, int count) {
        final String s = String.valueOf(nr);
        for (int i = s.length(); i < count; i++) {
            sb.append('0');
        }
        sb.append(s);
    }

    private void removeAllWidgets() {
        final RootPanel rootPanel = RootPanel.get();
        while (rootPanel.getWidgetCount() > 0) {
            rootPanel.remove(0);
        }
    }

}
