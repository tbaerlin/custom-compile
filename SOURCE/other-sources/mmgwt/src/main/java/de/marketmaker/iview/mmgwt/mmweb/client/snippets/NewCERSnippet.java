/*
 * FinderGroupsSnippet.java
 *
 * Created on 28.03.2008 16:11:58
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.dom.client.Element;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.dmxml.FinderGroupCell;
import de.marketmaker.iview.dmxml.FinderGroupRow;
import de.marketmaker.iview.dmxml.FinderGroupTable;
import de.marketmaker.iview.dmxml.MinMaxAvgElement;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderController;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderControllerRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormKeys;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderSection;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.LiveFinderCER;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DateTimeUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewCERSnippet extends
        AbstractSnippet<NewCERSnippet, SnippetTableView<NewCERSnippet>> {

    public static class Class extends SnippetClass {
        public Class() {
            super("NewCER"); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new NewCERSnippet(context, config);
        }
    }

    private DmxmlContext.Block<FinderGroupTable> block;
    private int maxColumnCount;

    private NewCERSnippet(DmxmlContext context, SnippetConfiguration config) {
        super(context, config);

        this.block = createBlock("MSC_FinderGroups"); // $NON-NLS-0$
        this.block.setParameter("type", "CER"); // $NON-NLS$
        this.block.setParameter("query", config.getString("query")); // $NON-NLS$
        this.block.setParameter("primaryField", "certificateType"); // $NON-NLS$
        this.block.setParameter("secondaryField", "issuername"); // $NON-NLS$
        this.block.setParameter("disablePaging", "true"); // $NON-NLS$
        this.block.setParameter("sortBy", "fieldname"); // $NON-NLS$
        this.block.setParameter("ascending", "true"); // $NON-NLS$

        this.maxColumnCount = config.getInt("maxColumnCount", 5); // $NON-NLS-0$

        this.setView(new SnippetTableView<>(this,
                new DefaultTableColumnModel(new TableColumn[]{
                        new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.STRING) 
                }))
        );
    }

    public void destroy() {
        destroyBlock(this.block);
    }

    class FinderLinkListener implements LinkListener<Link> {
        final String type;
        private final String issuer;

        FinderLinkListener(final String type, final String issuer) {
            this.type = type;
            this.issuer = issuer;
        }

        public void onClick(LinkContext<Link> linkLinkContext, Element e) {
            final FinderController controller = FinderControllerRegistry.get(LiveFinderCER.CER_FINDER_ID);
            final FinderFormConfig ffc = new FinderFormConfig("temp", LiveFinderCER.CER_FINDER_ID); // $NON-NLS-0$
            FinderSection.enableBaseSection(ffc, "CER"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.ISSUER_NAME, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.ISSUER_NAME + "-item", this.issuer); // $NON-NLS-0$
            ffc.put(FinderFormKeys.TYPE, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.TYPE + "-item", this.type); // $NON-NLS-0$
            final MmJsDate today = new MmJsDate();
            ffc.put(FinderFormKeys.ISSUE_DATE, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.ISSUE_DATE + "-checked", "1"); // $NON-NLS-0$ $NON-NLS-1$
            ffc.put(FinderFormKeys.ISSUE_DATE + "-from", createTimePoint(today, "1m", DateTimeUtil.PeriodMode.PAST)); // $NON-NLS-0$ $NON-NLS-1$
            ffc.put(FinderFormKeys.ISSUE_DATE + "-to", JsDateFormatter.formatDdmmyyyy(today)); // $NON-NLS-0$
            ffc.put(FinderFormKeys.SORT, "true"); // $NON-NLS-0$
            ffc.put(FinderFormKeys.SORT + "-item", "issueDate"); // $NON-NLS-0$ $NON-NLS-1$
            controller.prepareFind(ffc);
            PlaceUtil.goTo("M_LF_CER"); // $NON-NLS-0$

        }
    }

    public void updateView() {
        if (!block.isResponseOk()) {
            getView().update(DefaultTableDataModel.NULL);
            return;
        }

        final FinderGroupTable result = this.block.getResult();

        // create TableColumnModel
        int columnCount = result.getColumn().size() + 1;
        if (columnCount > this.maxColumnCount) {
            Firebug.log("too many columns in NewCERSnippet"); // $NON-NLS-0$
            DebugUtil.logToServer("too many columns in NewCERSnippet"); // $NON-NLS-0$
            columnCount = this.maxColumnCount;
        }
        final TableColumn[] tableColumns = new TableColumn[columnCount];
        tableColumns[0] = new TableColumn(I18n.I.type(), 0.3f, TableCellRenderers.STRING); 
        for (int i = 0; i < columnCount - 1; i++) {
            final String column = result.getColumn().get(i);
            tableColumns[i + 1] = new TableColumn(column, 0.1f, TableCellRenderers.DEFAULT).withCellClass("mm-center"); // $NON-NLS-0$
        }
        final TableColumnModel tcm = new DefaultTableColumnModel(tableColumns);

        // create TableDataModel
        final List<Object[]> list = new ArrayList<>();
        for (FinderGroupRow row : result.getRow()) {
            Object[] values = new Object[columnCount];
            values[0] = Renderer.CERTIFICATE_CATEGORY.render(row.getKey());
            for (int i = 0; i < columnCount - 1; i++) {
                final FinderGroupCell cell = row.getColumn().get(i);
                if (cell != null && cell.getItem() != null && !cell.getItem().isEmpty()) {
                    final MinMaxAvgElement item = (MinMaxAvgElement) cell.getItem().get(0);
                    final FinderLinkListener listener = new FinderLinkListener(row.getKey(), cell.getKey());
                    values[i + 1] = new Link(listener, item.getCount(), I18n.I.gotoFinder()); 
                }
            }
            list.add(values);
        }

        final TableDataModel tdm = DefaultTableDataModel.create(list);
        getView().update(tcm, tdm);
    }

    private static String createTimePoint(final MmJsDate date, final String period, final DateTimeUtil.PeriodMode mode) {
        return JsDateFormatter.formatDdmmyyyy(FinderFormElements.add(date, period, mode));
    }
}
