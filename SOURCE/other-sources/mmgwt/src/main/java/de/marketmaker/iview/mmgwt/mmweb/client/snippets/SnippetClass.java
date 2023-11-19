/*
 * SnippetClass.java
 *
 * Created on 02.04.2008 14:54:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.Arrays;
import java.util.HashMap;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class SnippetClass {
    private static final HashMap<String, SnippetClass> classes = new HashMap<>();


    public static void addClass(SnippetClass sc) {
        final SnippetClass existing = classes.put(sc.name, sc);
        assert existing == null : "Snippet class already added: " + sc.name;
    }

    public static Snippet create(DmxmlContext context, SnippetConfiguration config) {
        if (config.getName() == null && config.getString("columnWidths", null) != null) { // $NON-NLS$
            return createSimpleHtml(context, config);
        }

        final SnippetClass sc = classes.get(config.getName());
        if (sc == null) {
            Firebug.log("snippet class not registered: " + config.getName()); // $NON-NLS$
            if (config.getString("columnWidths", null) != null) { // $NON-NLS$
                return createSimpleHtml(context, config);
            }
            return null;
        }
        return sc.newSnippet(context, config.withDefaults(sc.getDefaultConfig()));
    }

    public static SnippetConfiguration getDefaultConfig(String name) {
        return classes.get(name).getDefaultConfig();
    }

    private static Snippet createSimpleHtml(DmxmlContext context, SnippetConfiguration config) {
        final SnippetClass scSimpleHtml = classes.get("SimpleHtml"); // $NON-NLS$
        if (scSimpleHtml != null) {
            return scSimpleHtml.newSnippet(context, config.withDefaults(scSimpleHtml.getDefaultConfig()));
        }
        Firebug.log("snippet class not registered: SimpleHtml");
        assert false;
        return null;
    }

    private final String name;
    private final String title;

    protected SnippetClass(String name) {
        this(name, null);
    }

    protected SnippetClass(String name, String title) {
        this.name = name;
        this.title = title;
    }

    public String getSnippetClassName() {
        return name;
    }

    public String getTitle() {
        return this.title;
    }

    public final SnippetConfiguration getDefaultConfig() {
        final SnippetConfiguration result = new SnippetConfiguration(this.name);
        if (this.title != null) {
            result.put("title", this.title); // $NON-NLS$
        }
        addDefaultParameters(result);
        return result;
    }

    public abstract Snippet newSnippet(DmxmlContext context, SnippetConfiguration config);

    protected void addDefaultParameters(SnippetConfiguration config) {
        // subclasses can override this method to provide default parameters
    }

    public void addDashboardParameters(SnippetConfiguration config) {
        // subclasses can override this method to provide default parameters for usage in Dashboards
    }
}
