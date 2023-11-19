package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.RadioButtonRenderer;

/**
 * EditWatchlistPositionView.java
 * Created on 06.07.2009 10:38:01
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

class EditWatchlistPositionView {
    private final SnippetTableWidget tw;
    private final DialogIfc dialog;
    private TableColumnModel tableColumnModel = null;
    private final RadioButtonRenderer renderer = new RadioButtonRenderer("marketSelect"); // $NON-NLS-0$

    EditWatchlistPositionView(final EditWatchlistPosition controller) {
        this.tableColumnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn("", 0.02f, this.renderer), // $NON-NLS-0$
                new TableColumn(I18n.I.market(), 0.13f, TableCellRenderers.STRING_CENTER),
                new TableColumn(I18n.I.price(), 0.07f, TableCellRenderers.PRICE_WITH_SUPPLEMENT),
                new TableColumn("+/-", 0.07f, TableCellRenderers.CHANGE_NET), // $NON-NLS-0$
                new TableColumn(I18n.I.trend(), 0.07f, TableCellRenderers.TRENDBAR),
                new TableColumn(I18n.I.volume(), 0.05f, TableCellRenderers.VOLUME_LONG),
        });

        this.tw = SnippetTableWidget.create(this.tableColumnModel, "mm-snippetTable"); // $NON-NLS-0$
        final SimplePanel panel = new SimplePanel(this.tw);
        final Style style = panel.getElement().getStyle();
        style.setPropertyPx("maxHeight", 500); // $NON-NLS$
        style.setPropertyPx("maxWidth", 500); // $NON-NLS$
        style.setOverflow(Style.Overflow.AUTO);
        this.renderer.setElement(this.tw.getElement());
        this.dialog = Dialog.getImpl().createDialog()
                .withTitle(I18n.I.selectExchange())
                .withWidget(panel)
                .withDefaultButton(I18n.I.ok(), new Command() {
                    @Override
                    public void execute() {
                        controller.updateWatchlistPosition(renderer.getSelected());
                    }
                })
                .withButton(I18n.I.cancel());
    }

    public void show() {
        this.dialog.show();
    }

    public void update(TableDataModel tdm) {
        this.tw.update(this.tableColumnModel, tdm);
    }
}