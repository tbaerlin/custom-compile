/*
 * StatisticsView.java
 *
 * Created on 13.04.2010 14:34:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.DateWrapper;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.iview.dmxml.Sort;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.NeedsScrollLayout;

/**
 * @author oflege
 */
abstract class StatisticsView extends SimplePanel implements NeedsScrollLayout {
    private static int instanceCount = 0;

    private int id = ++instanceCount;

    protected static final String SELECTOR_NAME = "selectorfields"; // $NON-NLS-0$

    private final ContentPanel content;

    private ListBox intervals = new ListBox();

    private RadioButton[] selectorRadios;

    private ListBox[] selectorBoxes;

    private DateBox from;

    private DateBox to;

    protected ListBox modules;
    private final HTMLTable.CellFormatter formatter;

    public StatisticsView() {
        this.content = new ContentPanel();
        this.content.setStyleName("mm-snippet"); // $NON-NLS-0$
        this.content.setHeading(I18n.I.evaluation()); 

        final Grid grid = new Grid(2, 1);
        grid.setStyleName("mm-gridSnippets"); // $NON-NLS-0$
        grid.setWidth("100%"); // $NON-NLS-0$
        grid.setWidget(0, 0, createForm());
        grid.setWidget(1, 0, this.content);
        this.formatter = grid.getCellFormatter();
        this.formatter.setStyleName(0, 0, "mm-gridSnippets"); // $NON-NLS-0$
        this.formatter.setStyleName(1, 0, "mm-gridSnippets"); // $NON-NLS-0$
        setContent(null);

        setWidget(grid);
    }

    protected void setContent(Widget w) {
        this.content.removeAll();
        if (w == null) {
            this.formatter.setVisible(1, 0, false);
        }
        else {
            this.formatter.setVisible(1, 0, true);
            this.content.add(w);
        }
        this.content.layout();
    }

    protected StatisticsMetaData getMetaData() {
        return StatsController.metaData;
    }

    protected Widget createForm() {
        final ContentPanel panel = new ContentPanel();
        panel.setStyleName("mm-snippet"); // $NON-NLS-0$
        panel.setHeading(I18n.I.settings()); 

        final ToolBar toolbar = new ToolBar();
        toolbar.add(new Button(I18n.I.evaluate(), new SelectionListener<ButtonEvent>(){ 
            @Override
            public void componentSelected(ButtonEvent ce) {
                onCompute();
            }
        }));
        panel.setBottomComponent(toolbar);

        final FlexTable g = new FlexTable();
        g.setStyleName("mm-contentPadding"); // $NON-NLS-0$
        final HTMLTable.CellFormatter formatter = g.getCellFormatter();
        int row = 0;
        int col = 0;

        final Selectors selectors = getMetaData().getSelectors();
        selectorBoxes = new ListBox[selectors.getElements().size() + 1];
        selectorRadios = new RadioButton[selectorBoxes.length];

        // todo: nur, wenn User alles sehen/auswerten darf
        selectorRadios[0] = new RadioButton(SELECTOR_NAME + id, I18n.I.all()); 
        selectorRadios[0].setValue(true);
        g.setWidget(row++, col, selectorRadios[0]);

        for (SelectorElement element : selectors.getElements()) {
            selectorBoxes[row] = new ListBox();
            selectorBoxes[row].setWidth("100%"); // $NON-NLS-0$
            selectorBoxes[row].setVisibleItemCount(1);
            for (String s : element.getValues()) {
                selectorBoxes[row].addItem(s);
            }
            selectorRadios[row] = new RadioButton(SELECTOR_NAME + id, " " + element.getName()); // $NON-NLS-0$
            g.setWidget(row, col, selectorRadios[row]);
            g.setWidget(row, col + 1, selectorBoxes[row]);
            row++;
        }

        row = 0;
        col += 2;
        formatter.setWidth(row, col, "30px"); // $NON-NLS-0$

        col++;

        this.intervals.addItem(I18n.I.dailyMaximum14Days(), StatsCommand.IntervalType.DAILY.toString()); 
        this.intervals.addItem(I18n.I.weeklyMaximum12Weeks(), StatsCommand.IntervalType.WEEKLY.toString()); 
        this.intervals.addItem(I18n.I.monthly(), StatsCommand.IntervalType.MONTHLY.toString()); 
        this.intervals.setSelectedIndex(1);
//        this.intervals.setWidth("100%");
        this.intervals.addChangeHandler(new ChangeHandler(){
            public void onChange(ChangeEvent event) {
                adaptMinDate();
            }
        });
        g.setText(row, col, I18n.I.timeAggregation()); 
        g.setWidget(row, col + 1, intervals);

        row++;
        final MmJsDate dateFrom = new MmJsDate();
        dateFrom.addDays(-12 * 7);
        final MmJsDate dateTo = new MmJsDate();
        this.from = new DateBox(null, dateFrom);
        this.from.setWidth("100%"); // $NON-NLS-0$
        this.from.setMinDate(dateFrom);
        this.from.setMaxDate(dateTo);
        g.setText(row, col, I18n.I.fromUpperCase());
        g.setWidget(row, col + 1, this.from);

        row++;
        this.to = new DateBox(null, new MmJsDate());
        this.to.setWidth("100%"); // $NON-NLS-0$
        this.to.setMinDate(dateFrom);
        this.to.setMaxDate(dateTo);
        g.setText(row, col, I18n.I.toUpperCase());
        g.setWidget(row, col + 1, this.to);

        row = 0;
        col += 2;
        formatter.setWidth(row, col, "30px"); // $NON-NLS-0$

        col++;

        this.modules = initModules();
//        this.modules.setWidth("100%");
        g.setText(row, col, this.modules.getTitle());
        g.setWidget(row, col + 1, this.modules);

        panel.add(g);
        return panel;
    }

    protected ListBox initModules() {
        return null;
    }

    private void adaptMinDate() {
        final int index = this.intervals.getSelectedIndex();
        final MmJsDate minDate;
        switch (index) {
            case 0:
                minDate = new MmJsDate().addDays(-14);
                break;
            case 1:
                minDate = new MmJsDate().addDays(-12 * 7);
                break;
            default:
                minDate = null;
                break;
        }
        this.from.setMinDate(minDate);
        this.to.setMinDate(minDate);
        if (this.from.getDate().isBefore(minDate)) {
            this.from.setDate(minDate);
        }
        if (this.to.getDate().isBefore(minDate)) {
            this.to.setDate(minDate);
        }
    }

    protected abstract void onCompute();

    protected void fillCommand(StatsCommand cmd) {
        cmd.setFrom(this.from.getDate().getJavaDate());
        cmd.setTo(this.to.getDate().getJavaDate());
        cmd.setSelector1(getSelector(1));
        cmd.setSelector2(getSelector(2));
        cmd.setSelector3(getSelector(3));
        cmd.setSelector4(getSelector(4));
        cmd.setIntervalType(getIntervalType());
    }

    private String getSelector(int i) {
        if (i < this.selectorRadios.length && this.selectorRadios[i].getValue()) {
            return this.selectorBoxes[i].getItemText(this.selectorBoxes[i].getSelectedIndex());
        }
        return null;
    }

    private StatsCommand.IntervalType getIntervalType() {
        return StatsCommand.IntervalType.valueOf(this.intervals.getValue(this.intervals.getSelectedIndex()));
    }

    public void showStats(StatsResult stats) {
        if (stats == null) {
            setContent(new Label(I18n.I.error())); 
            return;
        }

        final ArrayList<StatsResult.Item> items = stats.getItems();
        final ArrayList<Integer> periods = getPeriods(items);
        final ArrayList<Object[]> rows = createRows(items, periods);
        final Sort sort = new StatisticsSort(periods);

        final SnippetTableWidget stw = new SnippetTableWidget(createColumnModel(periods));
        stw.setSortLinkListener(new LinkListener<String>(){
            public void onClick(LinkContext<String> context, Element e) {
                final Sort.SortedBy sortedBy = sort.getSortedBy();
                final String sortBy = context.getData();
                if (sortBy.equals(sortedBy.getValue())) {
                    sortedBy.setAscending(!sortedBy.isAscending());
                }
                else {
                    sortedBy.setValue(sortBy);
                    sortedBy.setAscending(sortBy.startsWith("s-")); // $NON-NLS-0$
                }
                displaySortedRows(stw, rows, sort);
            }
        });

        setContent(stw);
        displaySortedRows(stw, rows, sort);
    }

    private void displaySortedRows(SnippetTableWidget stw, ArrayList<Object[]> rows, Sort sort) {
        sortRows(rows, sort.getSortedBy());
        final DefaultTableDataModel tdm;
        if (rows.isEmpty()) {
            tdm = DefaultTableDataModel.NULL;
        }
        else {
            tdm = new DefaultTableDataModel(rows.size(), rows.get(0).length, sort);
            for (int i = 0; i < rows.size(); i++) {
                tdm.setValuesAt(i, rows.get(i));
            }
        }
        stw.updateData(tdm);
    }

    private void sortRows(ArrayList<Object[]> rows, Sort.SortedBy sortedBy) {
        final String sortBy = sortedBy.getValue();
        final int index = Integer.parseInt(sortBy.substring(2));
        final Comparator<Object[]> comparator = sortBy.startsWith("s-") // $NON-NLS-0$
                ? new StringRowComparator(index, sortedBy.isAscending())
                : new NumberRowComparator(index, sortedBy.isAscending());
        Collections.sort(rows, comparator);
    }

    abstract class RowComparator implements Comparator<Object[]> {
        protected final int index;
        private final boolean ascending;

        RowComparator(int index, boolean ascending) {
            this.index = index;
            this.ascending = ascending;
        }

        public int compare(Object[] o1, Object[] o2) {
            final String s1 = (String) o1[this.index];
            final String s2 = (String) o2[this.index];
            if (StringUtil.hasText(s1)) {
                if (StringUtil.hasText(s2)) {
                    final int result = _compare(s1, s2);
                    return this.ascending ? result : -result;
                }
                else {
                    return -1;
                }
            }
            return StringUtil.hasText(s2) ? 1 : 0;
        }

        abstract int _compare(String s1, String s2);
    }

    class StringRowComparator extends RowComparator {
        StringRowComparator(int index, boolean ascending) {
            super(index, ascending);
        }

        public int _compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }

    class NumberRowComparator extends RowComparator {
        NumberRowComparator(int index, boolean ascending) {
            super(index, ascending);
        }

        public int _compare(String s1, String s2) {
            final int lenResult = s1.length() - s2.length();
            if (lenResult != 0) {
                return lenResult;
            }
            return s1.compareTo(s2);
        }
    }

    abstract ArrayList<Object[]> createRows(ArrayList<StatsResult.Item> items, ArrayList<Integer> periods);

    protected abstract DefaultTableColumnModel createColumnModel(ArrayList<Integer> periods);

    protected ArrayList<Integer> getPeriods(ArrayList<StatsResult.Item> items) {
        final HashSet<Integer> dates = new HashSet<Integer>();
        for (StatsResult.Item item : items) {
            dates.add(item.getPeriod());
        }
        final ArrayList<Integer> result = new ArrayList<Integer>(dates);
        Collections.sort(result);
        return result;
    }

    protected String formatPeriod(int period) {
        switch (getIntervalType()) {
            case DAILY:
                return formatDay(period / 1000, period % 1000);
            case WEEKLY:
                return I18n.I.calendarWeekAbbr() + (period % 100) + ", " + (period / 100);  // $NON-NLS-0$
            case MONTHLY:
                return formatMonth(period / 100, period % 100);
        }
        return Integer.toString(period);
    }

    private String formatMonth(int year, int monthOfYear) {
        final Date date = new DateWrapper(year, 0, 1).addMonths(monthOfYear - 1).asDate();
        return Formatter.FORMAT_MONTH_YEAR.format(date);
    }

    private String formatDay(int year, int dayInYear) {
        final Date date = new DateWrapper(year, 0, 1).addDays(dayInYear - 1).asDate();
        return Formatter.LF.formatDate(date);
    }

    protected abstract List<String> getSortFields(ArrayList<Integer> periods);

    protected class StatisticsSort extends Sort {
        public StatisticsSort(ArrayList<Integer> periods) {
            this.field = getSortFields(periods);
            this.sortedBy = new SortedBy();
            this.sortedBy.setValue("s-0"); // $NON-NLS-0$
            this.sortedBy.setAscending(true);
        }
    }
}
