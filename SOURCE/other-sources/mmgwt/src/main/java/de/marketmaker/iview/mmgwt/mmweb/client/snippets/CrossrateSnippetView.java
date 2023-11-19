/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CrossrateSnippetView extends SnippetTableView<CrossrateSnippet> {

    public CrossrateSnippetView(CrossrateSnippet snippet) {
        super(snippet, createColumnModel(snippet));
        setTitle(I18n.I.crossRates()); 
    }

    private static DefaultTableColumnModel createColumnModel(CrossrateSnippet snippet) {
        final int numCols = snippet.size() + 1;
        float width = 1f / numCols;
        final TableColumn[] columns = new TableColumn[numCols];
        columns[0] = new TableColumn(I18n.I.fromToWithBackslash(), width, TableCellRenderers.STRING); 

        for (int i = 1; i < numCols; i++) {
            columns[i] = new TableColumn(snippet.getIsoCode(i - 1), width, TableCellRenderers.PRICE);
        }
        return new DefaultTableColumnModel(columns);
    }
}
