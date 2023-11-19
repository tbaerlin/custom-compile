/*
 * ScreenerUtil.java
 *
 * Created on 25.03.2010 11:37:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock.screener;

import de.marketmaker.iview.mmgwt.mmweb.client.data.Image;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author oflege
 */
class ScreenerUtil {
    static Image getImage(String imageName) {
        if (imageName == null) {
            return null;
        }
        final int slashPos = imageName.lastIndexOf('/');
        final int dotPos = imageName.lastIndexOf('.');
        final String styleName = imageName.substring(slashPos + 1, dotPos).replace('.', '-');
        return new Image("mm-screener " + styleName); // $NON-NLS-0$
    }

    static TableColumnModel createColumnModel() {
        return new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.type(), 0.15f, new TableCellRenderers.StringRenderer("--", "mm-snippetTable-label")),  // $NON-NLS-0$ $NON-NLS-1$
                new TableColumn(I18n.I.value(), 0.05f, TableCellRenderers.DEFAULT_RIGHT), 
                new TableColumn(I18n.I.star(), 0.05f, TableCellRenderers.DEFAULT_RIGHT), 
                new TableColumn(I18n.I.shortText(), 0.2f, TableCellRenderers.DEFAULT), 
                new TableColumn(I18n.I.longText(), 0.55f, TableCellRenderers.DEFAULT) 
        }, false);
    }

}
