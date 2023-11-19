/*
 * SnapFieldComparators.java
 *
 * Created on 28.04.2005 12:54:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Comparator;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnapFieldComparators {
    private SnapFieldComparators() {
    }

    public static final Comparator<SnapField> BY_ID = new Comparator<SnapField>() {
        public int compare(SnapField snapField, SnapField snapField1) {
            if (snapField == NullSnapField.INSTANCE) {
                return snapField1 == NullSnapField.INSTANCE ? 0 : 1;
            }
            if (snapField1 == NullSnapField.INSTANCE) {
                return -1;
            }
            return snapField.getId() - snapField1.getId();
        }
    };

    public static final Comparator<SnapField> BY_NAME = new Comparator<SnapField>() {
        public int compare(SnapField snapField, SnapField snapField1) {
            if (!snapField.isDefined()) {
                return snapField1.isDefined() ? 1 : 0;
            }
            if (!snapField1.isDefined()) {
                return -1;
            }
            return snapField.getName().compareToIgnoreCase(snapField1.getName());
        }
    };
}
