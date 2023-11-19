/*
 * SelectIpoSymbolFormBHLKGS.java
 *
 * Created on 28.01.14 15:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.search;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolForm;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SelectSymbolFormControllerInterface;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

import java.util.Map;

/**
 * @author Markus Dick
 */
public class SelectIpoSymbolFormBHLKGS extends SelectSymbolForm {

    public SelectIpoSymbolFormBHLKGS(Map<String, String> params,
                                 final SelectSymbolFormControllerInterface c) {

        super(params, null, null, false, c, SnippetConfigurationView.SymbolParameterType.ISIN, false);

        final TableColumnModel tableColumnModel = new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.option(), 0.05f).withRenderer(this.renderer),
                new TableColumn(I18n.I.name(), 0.6f).withRenderer(TableCellRenderers.STRING),
                new TableColumn("ISIN", 0.1f).withRenderer(TableCellRenderers.STRING_CENTER), //$NON-NLS$
                new TableColumn("WKN", 0.1f).withRenderer(TableCellRenderers.STRING_CENTER)   //$NON-NLS$
        });

        setTableColumnModel(tableColumnModel);
        setToolbarVisible(false);
    }
}
