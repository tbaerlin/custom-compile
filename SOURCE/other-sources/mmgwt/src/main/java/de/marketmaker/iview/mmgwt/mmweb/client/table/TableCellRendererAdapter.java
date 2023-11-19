/*
 * TableCellRendererAdapter.java
 *
 * Created on 05.06.2008 15:19:35
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.table;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class TableCellRendererAdapter implements TableCellRenderer {
    public void render(Object data, StringBuffer sb, Context context) {
        // empty
    }

    public String getContentClass() {
        return null;
    }

    public boolean isPushRenderer() {
        return false;
    }        
}
