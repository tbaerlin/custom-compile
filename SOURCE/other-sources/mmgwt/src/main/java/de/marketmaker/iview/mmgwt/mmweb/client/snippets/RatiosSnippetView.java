/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DatePickerHeaderRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class RatiosSnippetView extends SnippetView<RatiosSnippet> {
    private TableColumnModel columnModel;

    private SnippetTableWidget tw;

    private final SimplePanel panel;

    private final TableColumn lastColumn;

    public RatiosSnippetView(final RatiosSnippet snippet) {
        super(snippet);
        super.setTitle(I18n.I.ratios()); 

        final SnippetConfiguration config = snippet.getConfiguration();
        this.panel = new SimplePanel();

        final List<String> periodNames = config.getList("periodNames"); // $NON-NLS$
        final TableColumn[] columns = new TableColumn[periodNames.size() + 1];
        columns[0] = new TableColumn("", 0.3f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")); // $NON-NLS$
        int col = 1;
        final float colWidth = 0.7f / periodNames.size();
        for (String periodName : periodNames) {
            columns[col] = new TableColumn(periodName, colWidth, TableCellRenderers.DEFAULT_RIGHT);
            col++;
        }

        this.lastColumn = columns[columns.length - 1];
        this.lastColumn.setHeaderRenderer(DatePickerHeaderRenderer.create(new DatePickerHeaderRenderer.ValueChangeListener() {
            @Override
            public void onValueChange(String period, String title) {
                lastColumn.setTitle(title);
                snippet.setLastPeriod(period);
                snippet.ackParametersChanged();
            }
        }));

        this.columnModel = new DefaultTableColumnModel(columns);
    }

    @Override
    protected void onContainerAvailable() {
        this.container.addHeaderTool(SessionData.isAsDesign() ? "x-tool-btn-reset" : "x-tool-gear", I18n.I.reset(), new ContentPanelIfc.HeaderToolCommand() { // $NON-NLS$
            @Override
            public void execute(Widget headerToolWidget) {
                restore();
            }
        });
        this.container.setContentWidget(this.panel);
    }

    private void restore() {
        final ArrayList<String> names = getConfiguration().getList("periodNames"); // $NON-NLS$
        final ArrayList<String> values = getConfiguration().getList("periodValues"); // $NON-NLS$
        this.lastColumn.setTitle(names.get(names.size() - 1));
        this.snippet.setLastPeriod(values.get(values.size() - 1));
        this.snippet.ackParametersChanged();
    }

    public void setTitle(String title) {
        setTitleForNextUpdate(title);
    }

    void update(TableDataModel dtm) {
        updateTitle();
        if (this.tw == null) {
            this.tw = SnippetTableWidget.create(this.columnModel);
            this.panel.add(this.tw);
        }
        this.tw.updateData(dtm);
    }
}
