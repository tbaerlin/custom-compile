/*
 * GisIpoInstrumentsView.java
 *
 * Created on 24.10.2008 14:44:19
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class GisIpoInstrumentsView extends ContentPanel {
    private final GisIpoInstrumentsController controller;

    private SnippetTableWidget table;

    private SnippetTableWidget tableCurrent;

    private SnippetTableWidget tableFixed;

    public GisIpoInstrumentsView(GisIpoInstrumentsController controller) {
        addStyleName("mm-contentData"); // $NON-NLS-0$
        setHeaderVisible(false);
        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        this.controller = controller;

        final TableColumnModel columnModel = this.controller.createTableColumnModel();

        if (this.controller.wgzDev()) {
            this.tableCurrent = SnippetTableWidget.create(columnModel, "mm-snippetTable"); // $NON-NLS-0$
            this.tableCurrent.setSortLinkListener(controller.getSortLinkListener());
            this.tableFixed = SnippetTableWidget.create(columnModel, "mm-snippetTable"); // $NON-NLS-0$
            this.tableFixed.setSortLinkListener(controller.getSortLinkListener());

            addContentPanel(this.tableCurrent, I18n.I.currentCertIPO());
            addContentPanel(this.tableFixed, I18n.I.futureCertIPO());
        }
        else {
            this.table = SnippetTableWidget.create(columnModel, "mm-snippetTable"); // $NON-NLS-0$
            this.table.setSortLinkListener(controller.getSortLinkListener());
            add(this.table);
        }
    }

    private void addContentPanel(SnippetTableWidget tableWidget, String headerText) {
        final ContentPanel current = new ContentPanel();
        setHeader(current, headerText);
        current.add(tableWidget);
        add(current);
    }

    private static void setHeader(ContentPanelIfc contentPanelIfc, String headerText) {
        if (SessionData.isAsDesign()) {
            contentPanelIfc.setHeading(headerText);
        }
        else {
            final String headerSeparator = AbstractMainController.INSTANCE.getHeaderSeparator();
            contentPanelIfc.setHeading(I18n.I.wgzBank() + headerSeparator
                    + I18n.I.certificateIPO() + headerSeparator
                    + I18n.I.currentCertIPO());
        }
    }

    public void show(TableDataModel tdm) {
        this.table.updateData(tdm);
        this.controller.getContentContainer().setContent(this);
    }

    public void show(TableDataModel tdmCurrent, TableDataModel tdmFixed) {
        this.tableCurrent.updateData(tdmCurrent);
        this.tableFixed.updateData(tdmFixed);
        this.controller.getContentContainer().setContent(this);
    }
}