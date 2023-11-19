package de.marketmaker.iview.mmgwt.mmweb.client.table;

import de.marketmaker.iview.mmgwt.mmweb.client.util.Renderer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created on 22.09.2010 09:42:14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class DecimalCellData extends CellData<String> {

    private final Double doubleValue;
    private final String additionalString;

    public DecimalCellData(Renderer<String> renderer, String value, String additionalString, Sorting sorting) {
        this(renderer, value, additionalString, sorting, false);
    }

    public DecimalCellData(Renderer<String> renderer, String value, String additionalString, Sorting sorting, boolean asHtml) {
        super(renderer, value, sorting, asHtml);
        this.additionalString = additionalString;
        try {
            if (value != null) {
                this.doubleValue = Double.valueOf(value);
            }
            else {
                this.doubleValue = null;
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(this.getClass().getName() + ": wrong value format! \n" + e.getMessage()); // $NON-NLS$
        }
    }

    public DecimalCellData(Renderer<String> renderer, String value, Sorting sorting) {
        this(renderer, value, "", sorting, false);
    }

    public DecimalCellData(Renderer<String> renderer, String value, Sorting sorting, boolean asHtml) {
        this(renderer, value, "", sorting, asHtml);
    }

    protected Comparator<DecimalCellData> getComparator() {
        return new Comparator<DecimalCellData>() {
            public int compare(DecimalCellData o1, DecimalCellData o2) {
                if (o1.doubleValue == null || o2.doubleValue == null) {
                    return 0;
                }
                if (getSorting() == CellData.Sorting.ASC) {
                    return o2.doubleValue.compareTo(o1.doubleValue);
                }
                else if (getSorting() == CellData.Sorting.DESC) {
                    return o1.doubleValue.compareTo(o2.doubleValue);
                }
                else {
                    return 0;
                }
            }
        };
    }

    public void computeRanking(List<? extends CellData> allFields) {
        if (allFields.size() <= 1) {
            return;
        }

        if (!allFields.get(0).getClass().getName().equals(this.getClass().getName())) {
            throw new IllegalArgumentException(this.getClass().getName() + ".computeRanking: wrong type in list!"); // $NON-NLS$
        }

        List<DecimalCellData> decimalFields = new ArrayList<>();

        for (CellData cellData : allFields) {
            final DecimalCellData data = (DecimalCellData) cellData;
            if (data.doubleValue != null) {
                decimalFields.add(data);
            }
        }
        Collections.sort(decimalFields, getComparator());

        for (int i = 0, size = decimalFields.size(); i < size; i++) {
            final DecimalCellData data = decimalFields.get(i);
            if (data.sorting == Sorting.NONE) {
                data.setRank(-1);
            } else {
                data.setRank(i);
            }
        }
    }


    @Override
    public String toString() {
        return super.toString() + this.additionalString;
    }
}
