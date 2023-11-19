package de.marketmaker.iview.mmgwt.mmweb.client.history;

/**
 * Created on 29.11.12 11:24
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class EmptyContext implements HistoryContext<Object, EmptyContext> {
    private final String contextName;
    private final boolean breadCrumb;
    private String iconKey;

    public static EmptyContext create(String contextName) {
        return contextName == null ? null : new EmptyContext(contextName, true, null);
    }

    private EmptyContext(String contextName, boolean breadCrumb, String iconKey) {
        this.contextName = contextName;
        this.breadCrumb = breadCrumb;
        this.iconKey = iconKey;
    }

    @Override
    public String getValue() {
        return null;
    }

    @Override
    public void action() {
        //nothing
    }

    public String getName() {
        return this.contextName;
    }

    @Override
    public boolean isBreadCrumb() {
        return this.breadCrumb;
    }

    @Override
    public EmptyContext withoutBreadCrumb() {
        if (this.breadCrumb) {
            return new EmptyContext(getName(), false, getIconKey());
        }
        return this;
    }

    @Override
    public String putProperty(String key, String value) {
        return null;
    }

    @Override
    public String getProperty(String key) {
        return null;
    }

    @Override
    public String getIconKey() {
        return this.iconKey;
    }

    public EmptyContext withIconKey(String iconKey) {
        this.iconKey = iconKey;
        return this;
    }
}