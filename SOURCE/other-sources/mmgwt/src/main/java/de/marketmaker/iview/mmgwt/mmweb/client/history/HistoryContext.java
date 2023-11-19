package de.marketmaker.iview.mmgwt.mmweb.client.history;

import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;

/**
 * Created on 29.11.12 11:20
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface HistoryContext<T, HC extends HistoryContext<T, HC>> {
    public static final String USER_DEFAULT_KEY = "ud"; // $NON-NLS$

    public static class Util {
        public static HistoryContext getWithoutBreadcrumb() {
            final HistoryItem activeHistoryItem = AbstractMainController.INSTANCE.getHistoryThreadManager().getActiveThreadHistoryItem();
            if (activeHistoryItem == null) {
                return null;
            }
            return getWithoutBreadcrumb(activeHistoryItem.getPlaceChangeEvent().getHistoryContext());
        }

        public static <C extends HistoryContext<T, C>, T> C getWithoutBreadcrumb(C context) {
            if (context == null) {
                return null;
            }
            if (context.isBreadCrumb()) {
                return context.withoutBreadCrumb();
            }
            return context;
        }
    }

    T getValue();
    String getName();
    boolean isBreadCrumb();
    void action();
    String putProperty(String key, String value);
    String getProperty(String key);
    HC withoutBreadCrumb();
    String getIconKey();
}