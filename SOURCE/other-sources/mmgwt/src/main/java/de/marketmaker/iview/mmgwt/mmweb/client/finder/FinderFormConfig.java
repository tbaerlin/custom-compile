/*
 * FinderFormConfig.java
 *
 * Created on 10.06.2008 13:32:19
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class FinderFormConfig implements Serializable {
    protected static final long serialVersionUID = 1L;

    private String finderFormId;

    private String name;

    private HashMap<String, String> config = new HashMap<String, String>();

    public FinderFormConfig() {
        // need empty constructor for gwt serialization
    }

    public FinderFormConfig(String name, String finderFormId) {
        this.name = name;
        this.finderFormId = finderFormId;
    }

    public String getFinderFormId() {
        return finderFormId;
    }

    public String getName() {
        return name;
    }

    public String put(String key, String value) {
        return config.put(key, value);
    }

    public String get(String key) {
        return config.get(key);
    }

    public Set<String> getKeySet() {
        return this.config.keySet();
    }

    public void copyParameters(FinderFormConfig other) {
        this.config = other.config;
    }

    public String toString() {
        return new StringBuilder(200)
                .append("FinderFormConfig[").append(this.finderFormId) // $NON-NLS-0$
                .append(", ").append(this.name) // $NON-NLS-0$
                .append(", config=").append(this.config).append("]") // $NON-NLS-0$ $NON-NLS-1$
                .toString();
    }

    public boolean contains(String key) {
        return this.config.containsKey(key);
    }

    public List<String> getCloneIds(FinderFormElement originalElement) {
        final ArrayList<String> result = new ArrayList<String>();
        for (String id : this.config.keySet()) {
            if (id.matches("^.*-[0-9]*$") && originalElement.getId().equals(FinderFormUtils.getOriginalId(id))) { // $NON-NLS$
                result.add(id);
            }
        }
        return result;
    }
}
