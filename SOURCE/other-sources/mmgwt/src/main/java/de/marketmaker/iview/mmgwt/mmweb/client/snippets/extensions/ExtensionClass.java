/*
 * ExtensionClass.java
 *
 * Created on 23.10.2012 14:04:23
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;

import java.util.HashMap;

/**
 * @author Markus Dick
 */
abstract class ExtensionClass {
    private static final HashMap<String, ExtensionClass> classes = new HashMap<String, ExtensionClass>();

    static {
        addClass(new GisStaticDataExtension.Class());
    }

    @SuppressWarnings("unchecked")
    public static <T extends Extension> T createExtension(Class<T> clazz, String extensionClassName, DmxmlContext context, SnippetConfiguration config) {
        ExtensionClass extensionClass = classes.get(extensionClassName);
        if(extensionClass != null && extensionClass.isImplementationOf(clazz)) {
            return (T)extensionClass.newExtension(context, config);
        }

        Firebug.log("Extension not found: " + extensionClassName); //$NON-NLS$
        return null;
    }

    public static void addClass(ExtensionClass extensionClass) {
        final ExtensionClass existing = classes.put(extensionClass.extensionClassName, extensionClass);
        assert existing == null;
    }

    private final String extensionClassName;

    protected ExtensionClass(String extensionClassName) {
        this.extensionClassName = extensionClassName;
    }

    public String getExtensionClassName() {
        return extensionClassName;
    }

    /**
     * Checks if this ExtensionClass is an implementation of the given Extension interface.
     * If the ExtensionClass claims to implement also super interfaces of the given
     * Extension interface, depends solely on the impl. of this method.
     */
    public abstract boolean isImplementationOf(Class<? extends Extension> clazz);

    public abstract Extension newExtension(DmxmlContext context, SnippetConfiguration config);
}
