/*
 * HistoryLabel.java
 *
 * Created on 26.04.13 11:51
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.HistoryItem;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.UserFieldDeclarationDesc;

import java.util.List;

/**
 * @author Markus Dick
 */
public class HistoryLabel extends AbstractLabelWithPopup<List<HistoryItem>> {
    private final SnippetTableWidget table;

    public HistoryLabel(List<HistoryItem> historyItems, Renderer<MM> valueRenderer, boolean isNumber) {
        this(historyItems, new TableCellRenderers.DelegateRenderer<MM>(valueRenderer, isNumber ? "mm-right" : null)); //$NON-NLS$
    }

    public HistoryLabel(List<HistoryItem> historyItems, UserFieldDeclarationDesc userFieldDeclarationDesc) {
        this(historyItems, new PmRenderers.DataItemTableCellRenderer(userFieldDeclarationDesc));
    }

    public HistoryLabel(List<HistoryItem> historyItems, TableCellRenderer dataItemCellRenderer) {
        super(historyItems);

        this.updateWidget();

        final PopupPanel p = getPopupPanel();

        this.table = new SnippetTableWidget(new DefaultTableColumnModel(
                new TableColumn[] {
                        new TableColumn(I18n.I.date(), 0.25f),
                        new TableColumn(I18n.I.value(), -1f, dataItemCellRenderer)
                })
        );
        p.add(this.table);
    }

    public HistoryLabel(SafeHtml safeHtml, List<HistoryItem> historyItems, Renderer<MM> valueRenderer, boolean isNumber) {
        this(historyItems, valueRenderer, isNumber);
        setHTML(safeHtml);
    }

    public HistoryLabel(String label, List<HistoryItem> historyItems, Renderer<MM> valueRenderer, boolean isNumber) {
        this(historyItems, valueRenderer, isNumber);
        setText(label);
        setTitle(label);
    }

    @Override
    protected void updatePopupPanel() {
        final List<HistoryItem> historyList = getPopupData();

        if(historyList == null || historyList.isEmpty()) {
            return;
        }

        final TableDataModel tdm = DefaultTableDataModel.create(historyList,
                new AbstractRowMapper<HistoryItem>() {
                    @Override
                    public Object[] mapRow(HistoryItem historyItem) {
                        return new Object[]{
                                PmRenderers.DATE_TIME_STRING.render(historyItem.getDate()),
                                historyItem.getDataItem()
                        };
                    }
                });

        this.table.updateData(tdm);
    }
}
