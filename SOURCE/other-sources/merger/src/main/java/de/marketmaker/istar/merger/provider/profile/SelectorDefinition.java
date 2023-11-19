/*
 * SelectorDefinition.java
 *
 * Created on 20.07.12 15:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.profile;

import java.io.Serializable;

/**
 * @author Markus Dick
 */
public class SelectorDefinition implements Serializable {
    protected static final long serialVersionUID = 1L;

    private int id;
    private String selector;
    private String shortname;
    private String service;
    private String type;
    private String subtype;
    private String description;

    public SelectorDefinition() {
        super();
    }

    public SelectorDefinition(int id, String selector, String shortname, String service, String type, String subtype, String description) {
        this.id = id;
        this.selector = selector;
        this.shortname = shortname;
        this.service = service;
        this.type = type;
        this.subtype = subtype;
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getSelector() {
        return selector;
    }

    public void setSelector(String selector) {
        this.selector = selector;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubtype() {
        return subtype;
    }

    public void setSubtype(String subtype) {
        this.subtype = subtype;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SelectorDefinition)) return false;

        SelectorDefinition that = (SelectorDefinition) o;

        if (id != that.id) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (selector != null ? !selector.equals(that.selector) : that.selector != null) return false;
        if (service != null ? !service.equals(that.service) : that.service != null) return false;
        if (shortname != null ? !shortname.equals(that.shortname) : that.shortname != null) return false;
        if (subtype != null ? !subtype.equals(that.subtype) : that.subtype != null) return false;
        if (type != null ? !type.equals(that.type) : that.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (selector != null ? selector.hashCode() : 0);
        result = 31 * result + (shortname != null ? shortname.hashCode() : 0);
        result = 31 * result + (service != null ? service.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (subtype != null ? subtype.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "SelectorDefinition{" +
                "id=" + id +
                ", selector='" + selector + '\'' +
                ", shortname='" + shortname + '\'' +
                ", service='" + service + '\'' +
                ", type='" + type + '\'' +
                ", subtype='" + subtype + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
