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

import java.util.List;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class YieldsSnippetView extends SnippetTableView<YieldsSnippet> {

    public YieldsSnippetView(YieldsSnippet yieldsSnippet) {
        super(yieldsSnippet, createColumnModel(yieldsSnippet));
        setTitle(I18n.I.yields()); 
    }

    private static DefaultTableColumnModel createColumnModel(YieldsSnippet yieldsSnippet) {
        final List<String> names = yieldsSnippet.getNames();

        final TableColumn[] columns = new TableColumn[names.size() + 1];
//        float width = Float.parseFloat("0." + (80 / (columns.length - 1)));
        float width = 0.65f / (columns.length - 1);
        columns[0] = new TableColumn("", 0.35f); // $NON-NLS-0$

        for (int i = 0; i < names.size(); i++) {
            columns[i + 1] = new TableColumn(names.get(i), width, TableCellRenderers.DEFAULT).alignRight();
        }

        return new DefaultTableColumnModel(columns);
    }
}
