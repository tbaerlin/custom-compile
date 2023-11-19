/*
 * PageDefinition.java
 *
 * Created on 08.01.2010 16:03:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.statistics;

import java.util.regex.Pattern;

/**
 * Identifies a page based on its history token by matching that token against a regular
 * expression. Since multiple PageDefinitions may match the same token, these objects can be
 * ordered bases on their priority.
 * @author oflege
 */
public class PageDefinition {
    private int id;

    private String module;

    private String name;

    private Pattern pattern;

    private int priority;

    @Override
    public String toString() {
        return this.id + "/" + this.name + " (" + this.module + "):" + this.pattern;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPattern(String regex) {
        this.pattern = Pattern.compile(regex);
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean matches(String page) {
        return this.pattern.matcher(page).matches();
    }
}
