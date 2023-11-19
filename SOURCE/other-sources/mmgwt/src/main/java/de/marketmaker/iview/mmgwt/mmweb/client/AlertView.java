/*
 * AlertView.java
 *
 * Created on 24.09.2008 13:57:50
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.ActionRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * @author Oliver Flege
 */
public class AlertView extends ContentPanel {

    private SnippetTableWidget stw;

    public AlertView() {
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        this.stw = SnippetTableWidget.create(createColumnModel(), "mm-snippetTable"); // $NON-NLS-0$
        add(this.stw);
    }

    private TableColumnModel createColumnModel() {
        final DefaultTableColumnModel result = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.limitActions(), 0.05f, new ActionRenderer<>(AlertController.ALERT_ACTION_HANDLER)),
                new TableColumn(I18n.I.numberAbbr(), 0.02f, TableCellRenderers.STRING), 
                new TableColumn(I18n.I.limitName(), 0.1f, TableCellRenderers.STRING), 
                new TableColumn(I18n.I.instrument(), 0.18f, TableCellRenderers.QUOTELINK_22), 
                new TableColumn(I18n.I.marketName(), 0.05f, TableCellRenderers.STRING_CENTER), 
                new TableColumn(I18n.I.limitFieldName(), 0.05f, TableCellRenderers.STRING), 
                new TableColumn(I18n.I.absolute(), 0.05f, TableCellRenderers.PRICE23), 
                new TableColumn(I18n.I.percentalAbbr(), 0.05f, TableCellRenderers.PRICE_PERCENT), 
                new TableColumn(I18n.I.absolute(), 0.05f, TableCellRenderers.PRICE23),
                new TableColumn(I18n.I.percentalAbbr(), 0.05f, TableCellRenderers.PRICE_PERCENT), 
                new TableColumn(I18n.I.referenceValue(), 0.05f, TableCellRenderers.PRICE), 
                new TableColumn(I18n.I.limitCreationTime(), 0.10f, TableCellRenderers.LOCAL_TZ_DATE_TIME),
                new TableColumn(I18n.I.limitTriggered(), 0.15f, TableCellRenderers.ALERT_STATUS) 
        });
        result.groupColumns(6, 8, I18n.I.lowerLimit()); 
        result.groupColumns(8, 10, I18n.I.upperLimit()); 
        return result;
    }

    void show(TableDataModel dtm) {
        this.stw.updateData(dtm);
    }
}
