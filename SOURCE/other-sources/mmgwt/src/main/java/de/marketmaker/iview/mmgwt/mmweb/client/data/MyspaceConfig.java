/*
 * MyspaceConfiguration.java
 *
 * Created on 29.04.2008 10:27:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import java.io.Serializable;
import java.util.ArrayList;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
@NonNLS
public class MyspaceConfig implements Serializable {
    protected static final long serialVersionUID = 1L;

    private String id;

    private String name;

    private ArrayList<SnippetConfiguration> snippetConfigs = new ArrayList<>();

    private AppConfig appConfig;

    public MyspaceConfig() {
    }

    void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(500);
        sb.append("MyspaceConfig[").append(this.id).append(", ").append(this.name);
        for (SnippetConfiguration snippetConfig : snippetConfigs) {
            sb.append(", ").append(snippetConfig);
        }
        return sb.append("]").toString();
    }

    public String getId() {
        return id;
    }

    void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public ArrayList<SnippetConfiguration> getSnippetConfigs() {
        return snippetConfigs;
    }

    public void addSnippet(SnippetConfiguration config) {
        this.snippetConfigs.add(config);
        if (this.appConfig != null) {
            config.setAppConfig(this.appConfig);
            this.appConfig.firePropertyChange("myspace.addsnippet", null, config);
        }
    }

    public boolean removeSnippet(SnippetConfiguration config) {
        final boolean result = this.snippetConfigs.remove(config);
        if (this.appConfig != null) {
            this.appConfig.firePropertyChange("myspace.removesnippet", null, config);
        }
        return result;
    }
}
