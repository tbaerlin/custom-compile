/*
 * SnippetConfiguration.java
 *
 * Created on 01.04.2008 11:26:16
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.FeatureFlags;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContainerConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Formatter;
import de.marketmaker.iview.mmgwt.mmweb.client.util.JSONWrapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SnippetConfiguration implements ContainerConfig, Serializable {
    protected static final long serialVersionUID = 1L;

    private String name;

    private HashMap<String, String> params = new HashMap<>();

    private AppConfig appConfig;

    public static SnippetConfiguration createFrom(String def) {
        final int n = def.indexOf('(');
        final SnippetConfiguration result = new SnippetConfiguration(def.substring(0, n));
        final ArrayList<String> pairs = StringUtil.split(def.substring(n + 1, def.length() - 1), ';');
        for (String s : pairs) {
            final int p = s.indexOf('=');
            result.put(s.substring(0, p), s.substring(p + 1));
        }
        return result;
    }

    public static SnippetConfiguration createFrom(JSONWrapper def) {
        final String name = def.get("name").stringValue(); // $NON-NLS$
        if (def.get("ifFeatureFlag").isString()) { // $NON-NLS$
            final String ifFeatureFlag = def.getString("ifFeatureFlag"); // $NON-NLS$

            if (ifFeatureFlag.startsWith("!")) { // $NON-NLS$
                if (FeatureFlags.isEnabled(ifFeatureFlag.substring(1))) {
                    Firebug.log("snippet not available (ifFeatureFlag=" + ifFeatureFlag + "): " + name);
                    return null;
                }
            }
            else {
                if (!FeatureFlags.isEnabled(ifFeatureFlag)) {
                    Firebug.log("snippet not available (ifFeatureFlag=" + ifFeatureFlag + "): " + name);
                    return null;
                }
            }
        }
        if (def.get("ifSelector").isString()) { // $NON-NLS$
            final String ifSelector = def.getString("ifSelector"); // $NON-NLS$

            if (ifSelector.startsWith("!")) { // $NON-NLS$
                if (Selector.valueOf(ifSelector.substring(1)).isAllowed()) {
                    Firebug.log("snippet not allowed (ifSelector=" + ifSelector + "): " + name);
                    return null;
                }
            }
            else {
                if (!Selector.valueOf(ifSelector).isAllowed()) {
                    Firebug.log("snippet not allowed (ifSelector=" + ifSelector + "): " + name);
                    return null;
                }
            }
        }
        final SnippetConfiguration result = new SnippetConfiguration(name);
        for (String s : def.keySet()) {
            final JSONWrapper wrapper = def.get(s);
            if (!"name".equals(s) && wrapper.isValid()) { // $NON-NLS-0$
                result.put(s, toString(wrapper));
            }
        }
        return result;
    }

    private static String toString(JSONWrapper w) {
        if (w.isString()) {
            return w.stringValue();
        }
        if (w.isNumber()) {
            return Formatter.FORMAT_NUMBER.format(w.numberValue());
        }
        if (w.isArray()) {
            final StringBuilder sb = new StringBuilder();
            sb.append("["); // $NON-NLS-0$
            for (int i = 0; i < w.size(); i++) {
                if (i > 0) {
                    sb.append(","); // $NON-NLS-0$
                }
                sb.append(toString(w.get(i)));
            }
            sb.append("]"); // $NON-NLS-0$
            return sb.toString();
        }
        Firebug.log("ERROR Object found: " + w.getValue().toString()); // $NON-NLS-0$
        return null;
    }

    public SnippetConfiguration() {
    }

    public SnippetConfiguration(String name) {
        this.name = name;
    }

    public void setAppConfig(AppConfig appConfig) {
        this.appConfig = appConfig;
    }

    public String toString() {
        final StringBuilder sb = new StringBuilder(500);
        sb.append("SnippetConfig[<b>").append(this.name).append("</b>"); // $NON-NLS$
        final String id = this.params.get("id"); // $NON-NLS$
        if (id != null) {
            sb.append("(").append(id).append(")"); // $NON-NLS$
        }
        for (Map.Entry<String, String> entry : this.params.entrySet()) {
            if (!"id".equals(entry.getKey())) { // $NON-NLS$
                sb.append(", ").append(entry.getKey()).append('=').append(entry.getValue()); // $NON-NLS$
            }
        }
        return sb.append("]").toString(); // $NON-NLS-0$
    }

    public boolean containsKey(String key) {
        return this.params.containsKey(key);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.params.containsKey(key) ? Boolean.valueOf(this.params.get(key)) : defaultValue;
    }

    public float getFloat(String key, float defaultValue) {
        return this.params.containsKey(key) ? Float.parseFloat(this.params.get(key)) : defaultValue;
    }

    public BigDecimal getBigDecimal(String key, BigDecimal defaultValue) {
        if (this.params.containsKey(key)) {
            try {
                final String value = this.params.get(key);
                if (StringUtil.hasText(value)) {
                    return this.params.containsKey(key) ? new BigDecimal(value) : defaultValue;
                }
            } catch (NumberFormatException nfe) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public int getInt(String key, int defaultValue) {
        return this.params.containsKey(key) ? Integer.parseInt(this.params.get(key)) : defaultValue;
    }

    public String[] getArray(String key) {
        return getArray(key, null);
    }

    public String[] getArray(String key, String[] defaultValue) {
        final ArrayList<String> list = getList(key);
        return (list != null) ? list.toArray(new String[list.size()]) : defaultValue;
    }

    public ArrayList<String> getList(String key) {
        final String s = getString(key);
        if (s == null) {
            return null;
        }
        if (s.startsWith("[") && s.endsWith("]")) { // $NON-NLS$
            return StringUtil.split(s.substring(1, s.length() - 1), ',');
        }
        final ArrayList<String> result = new ArrayList<>(1);
        result.add(s);
        return result;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, String> getCopyOfParameters() {
        return new HashMap<>(this.params);
    }

    public void setParameters(HashMap<String, String> params) {
        this.params = params;
        onChange();
    }

    public SnippetConfiguration with(HashMap<String, String> params) {
        this.params.putAll(params);
        onChange();
        return this;
    }

    public String getString(String key, String defaultValue) {
        return this.params.containsKey(key) ? this.params.get(key) : defaultValue;
    }

    public String getString(String key) {
        return this.params.get(key);
    }

    public void setName(String name) {
        this.name = name;
    }

    public String remove(String key) {
        return put(key, null);
    }

    public String put(String key, String value) {
        String old;
        if (value != null) {
            old = this.params.put(key, value);
        }
        else {
            old = this.params.remove(key);
        }
        if (!StringUtil.equals(old, value)) {
            onChange();
        }
        return old;
    }

    public void put(String key, int value) {
        put(key, Integer.toString(value));
    }

    public void put(String key, boolean value) {
        put(key, Boolean.valueOf(value).toString());
    }

    public void putDefault(String key, String value) {
        if (!this.params.containsKey(key)) {
            this.params.put(key, value);
            onChange();
        }
    }

    public SnippetConfiguration withDefaults(SnippetConfiguration sc) {
        for (Map.Entry<String, String> entry : sc.params.entrySet()) {
            if (!this.params.containsKey(entry.getKey())) {
                this.params.put(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public SnippetConfiguration with(String key, String value) {
        put(key, value);
        return this;
    }

    public SnippetConfiguration copy() {
        return new SnippetConfiguration(this.name).with(this.params);
    }

    private void onChange() {
        if (this.appConfig != null) {
            this.appConfig.firePropertyChange("snippet.config", null, this); // $NON-NLS-0$
        }
    }
}
