/*
 * CerComparisonRenderer.java
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.data.CerDataSection;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model.CerColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.cercomparison.model.CerTableModel;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.CellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.QwiCellData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.RendererContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkManager;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HTMLWithLinks;

/**
 * @author umaurer
 */
public class CerComparisonRenderer {
    private final CerTableModel model;
    private final FlexTable table;
    private final HTMLTable.RowFormatter rowFormatter;
    private final FlexTable.FlexCellFormatter cellFormatter;
    private final boolean withDnd;

    public CerComparisonRenderer(CerTableModel model, boolean withDnd) {
        this.model = model;
        this.withDnd = withDnd;

        if (this.model.getColumnCount() == 0) {
            this.table = null;
            this.rowFormatter = null;
            this.cellFormatter = null;
        }
        else {
            this.table = new FlexTable();
            this.table.setStyleName("mm-snippetTable");
            this.table.setCellPadding(0);
            this.table.setCellSpacing(0);
            this.rowFormatter = this.table.getRowFormatter();
            this.cellFormatter = this.table.getFlexCellFormatter();
            this.table.getColumnFormatter().setWidth(0, "180px"); // $NON-NLS$
            renderTitles();
            renderData();
        }
    }

    public Widget getWidget() {
        if (this.table == null) {
            return this.withDnd ? getDefaultWidget() : getDefaultWidgetNoDnd();
        }
        return this.table;
    }

    private void renderTitles() {
        int row = 0;
        this.table.setText(row, 0, ""); // row of delete buttons
        row++;
        final int columnCount = this.model.getColumnCount();
        for (CerDataSection section : CerDataSection.values()) {
            final String[] sectionTitles = this.model.getSectionTitles(section);
            final LabelSectionTitle labelSectionTitle = new LabelSectionTitle(section.getName(), row + 1, row + sectionTitles.length);
            this.table.setWidget(row, 0, labelSectionTitle);
            this.cellFormatter.setColSpan(row, 0, columnCount + 1);
            row++;

            if (section == CerDataSection.COMPARE_CHART) {
                this.table.setHTML(row, 0, "<img class=\"mm-middle\" src=\"" + this.model.getCompareChart().getChartUrl() + "\"></img>"); // $NON-NLS$
                this.cellFormatter.setColSpan(row, 0, columnCount + 1);
                this.cellFormatter.setStyleName(row, 0, "mm-certcomparison-singlefield"); // $NON-NLS$
                row++;
            }
            else {
                int sectionRow = 0;
                for (String sectionTitle : sectionTitles) {
                    this.table.setText(row, 0, sectionTitle);
                    if (sectionRow % 2 > 0) {
                        this.rowFormatter.setStyleName(row, "uneven-row"); // $NON-NLS$
                    }
                    sectionRow++;
                    row++;
                }
            }

            labelSectionTitle.setExpanded(section.isExpanded());
        }
    }

    public static Widget getDefaultWidget() {
        final HTML html = new HTML(I18n.I.certCompareDescription());
        html.setStyleName("mm-certcomparison-default-text"); // $NON-NLS$
        return html;
    }

    public static Widget getDefaultWidgetNoDnd() {
        final HTML html = new HTML(I18n.I.certCompareDescriptionNoDnd());
        html.setStyleName("mm-certcomparison-default-text"); // $NON-NLS$
        return html;
    }

    class LabelSectionTitle extends Label {
        private boolean expanded = true;
        private final int expandFrom;
        private final int expandTo;

        LabelSectionTitle(String text, int expandFrom, int expandTo) {
            super(text);
            this.expandFrom = expandFrom;
            this.expandTo = expandTo;
            addClickHandler(event -> setExpanded(!expanded));
            setStyleName("mm-certcomparison-section"); // $NON-NLS$
        }

        public void setExpanded(boolean expanded) {
            this.expanded = expanded;
            if (this.expanded) {
                setStyleName("mm-certcomparison-section"); // $NON-NLS$
            }
            else {
                setStyleName("mm-certcomparison-section hidden"); // $NON-NLS$
            }
            for (int row = this.expandFrom; row <= expandTo; row++) {
                rowFormatter.setVisible(row, expanded);
            }
        }
    }

    private void renderData() {
        final LinkManager linkManager = new LinkManager();
        final RendererContext context = new RendererContext(linkManager, null);

        this.model.computeRanking();
        for (int colId = 1, columnCount = this.model.getColumnCount(); colId <= columnCount; colId++) {
            final int modelColId = colId - 1;
            final CerColumnModel column = this.model.getColumn(modelColId);
            int row = 0;

            final Button buttonDelete = Button.icon(SessionData.isAsDesign() ? "as-minus-24" : "mm-icon-comparison-delete") // $NON-NLS$
                    .clickHandler(event -> model.removeColumn(modelColId))
                    .build();
            this.table.setWidget(row, colId, buttonDelete);
            this.rowFormatter.addStyleName(row, "mm-printHidden"); // $NON-NLS$
            row++;

            for (CerDataSection section : CerDataSection.values()) {
                row++; // sectionTitle is spanned over all columns => dont render row

                if (section == CerDataSection.COMPARE_CHART) {
                    row++; // compareChart is spanned over all columns => dont render row
                }
                else {
                    for (CellData cellData : column.getCellDatas(section)) {
                        if (cellData instanceof QwiCellData) {
                            this.table.setWidget(row, colId, new HTMLWithLinks(((QwiCellData) cellData).getRenderedValue(context), linkManager));
                        }
                        else if (cellData.isHtml()) {
                            this.table.setHTML(row, colId, cellData.getRenderedValue());
                        }
                        else {
                            this.table.setText(row, colId, cellData.getRenderedValue());
                        }
                        this.cellFormatter.setStyleName(row, colId, "mm-certcomparison-table-" + cellData.getRank());
                        row++;
                    }
                }
            }
        }
    }
}
