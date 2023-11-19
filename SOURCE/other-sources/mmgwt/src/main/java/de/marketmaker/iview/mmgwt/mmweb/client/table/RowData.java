/*
 * RowData.java
 *
 * Created on 15.09.2008 15:10:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.table;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Ulrich Maurer
 */
public class RowData {
    private final Object[] data;
    private String flipId = null;

    public RowData(Object... data) {
        this.data = data;
    }

    public RowData withFlipId(String flipId) {
        this.flipId = flipId;
        return this;
    }

    public Object[] getData() {
        return this.data;
    }

    public int getColumnCount() {
        return this.data.length;
    }

    public String getFlipId() {
        return this.flipId;
    }


    public static ArrayList<RowData> combineRowData(RowData[]... cols) {
        // make sure, all columns have the same size
        final int size = cols[0].length;
        for (int i = 1; i < cols.length; i++) {
            if (cols[i].length != size) {
                throw new RuntimeException("invalid column size: " + i); // $NON-NLS$
            }
        }

        final ArrayList<RowData> list = new ArrayList<RowData>(cols[0].length);
        for (int i = 0; i < size; i++) {
            final List<Object> objects = new ArrayList<Object>();
            boolean addDivider = false;
            for (RowData[] col : cols) {
                if (addDivider) {
                    objects.add(null);
                }
                addDivider = true;
                for (int n = 0; n < col[i].getColumnCount(); n++) {
                    objects.add(col[i].getData()[n]);
                }
            }
            list.add(new RowData(objects.toArray(new Object[objects.size()])));
        }
        return list;
    }

    public static RowData combineRowData(RowData... rowData) {
        List<Object> objects = new ArrayList<Object>();
        boolean addDivider = false;
        for (RowData aRowData : rowData) {
            if (addDivider) {
                objects.add(null);
            }
            addDivider = true;
            for (int n = 0; n < aRowData.getColumnCount(); n++) {
                objects.add(aRowData.getData()[n]);
            }
        }
        return new RowData(objects.toArray(new Object[objects.size()]));
    }

}
