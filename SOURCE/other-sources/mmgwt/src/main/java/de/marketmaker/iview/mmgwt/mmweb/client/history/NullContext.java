package de.marketmaker.iview.mmgwt.mmweb.client.history;

/**
 * Created on 07.07.2014 11:24
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 *         <p/>
 *         Use NullContext if you need to reset the HistoryContext to null
 */

public class NullContext implements HistoryContext<Object, NullContext> {

    private static NullContext INSTANCE;

    public static HistoryContext getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new NullContext();
        }
        return INSTANCE;
    }

    @Override
    public Object getValue() {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public String getName() {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public boolean isBreadCrumb() {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public void action() {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public String putProperty(String key, String value) {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public String getProperty(String key) {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public NullContext withoutBreadCrumb() {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public String getIconKey() {
        throw new IllegalStateException("not allowed!"); // $NON-NLS$
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}