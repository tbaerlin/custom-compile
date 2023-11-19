/*
 * MyspaceConfiguration.java
 *
 * Created on 29.04.2008 10:27:00
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.data;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
@NonNLS
public class DashboardConfig implements Serializable {
    protected static final long serialVersionUID = 1L;

    /**
     * The order matters!
     * @see de.marketmaker.iview.mmgwt.mmweb.client.dashboard.ConfigComparator
     */
    public enum Access implements Serializable {
        PUBLIC, PRIVATE, MYSPACE
    }

    private List<String> roles;

    private String id;

    private String name;

    private Access access;

    private final ArrayList<SnippetConfiguration> snippetConfigs = new ArrayList<>();

    public List<String> getRoles() {
        return this.roles;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public DashboardConfig createEmptyCopy() {
        final DashboardConfig dc = new DashboardConfig();
        dc.roles = roles;
        dc.id = id;
        dc.name = name;
        dc.access = access;
        return dc;
    }

    public DashboardConfig createCopy(List<SnippetConfiguration> snippetConfigs) {
        final DashboardConfig dc = createEmptyCopy();
        dc.snippetConfigs.addAll(snippetConfigs);
        return dc;
    }

    public DashboardConfig createCopy() {
        return createCopy(this.snippetConfigs);
    }

    public ArrayList<SnippetConfiguration> getSnippetConfigs() {
        return snippetConfigs;
    }

    public void addSnippet(SnippetConfiguration sc) {
        this.snippetConfigs.add(sc);
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(500);
        sb.append("DashboardConfig[<b>").append(this.roles).append("</b> ")
                .append(this.access)
                .append(" (").append(this.id).append(": ").append(this.name).append(")");
        for (SnippetConfiguration snippetConfig : this.snippetConfigs) {
            sb.append(",<br/>&nbsp;&nbsp;&nbsp;").append(snippetConfig);
        }
        return sb.append("]").toString();
    }
}
