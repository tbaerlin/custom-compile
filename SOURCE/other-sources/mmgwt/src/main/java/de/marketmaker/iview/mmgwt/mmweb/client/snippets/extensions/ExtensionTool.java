/*
 * ExtensionTool.java
 *
 * Created on 23.10.2012 12:43:09
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.extensions;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Markus Dick
 */
public class ExtensionTool {
    public static void destroy(List<? extends Extension> extensions) {
        for (Extension extension : extensions) {
            extension.destroy();
        }
    }

    public static <T extends Extension> List<T> createExtensions(Class<T> extensionType, SnippetConfiguration config, String arrayConfigParamName, DmxmlContext context) {
        final String[] extensionNames = config.getArray(arrayConfigParamName);

        if (extensionNames != null) {
            return createExtensions(extensionType, config, extensionNames, context);
        }
        return Collections.emptyList();
    }

    public static <T extends Extension> List<T> createExtensions(Class<T> extensionType, SnippetConfiguration config, String[] extensionNames, DmxmlContext context) {
        final List<T> extensions = new ArrayList<T>();

        if (extensionNames != null) {
            for (String extensionName : extensionNames) {
                final T extension = ExtensionClass.createExtension(extensionType, extensionName, context, config);

                if (extension != null && extension.isAllowed()) {
                    extensions.add(extension);
                }
            }
        }
        return extensions;
    }

    public static <T extends Extension> T createExtension(Class<T> extensionType, SnippetConfiguration config, String extensionName, DmxmlContext context) {
        if (StringUtil.hasText(extensionName)) {
            final T extension = ExtensionClass.createExtension(extensionType, extensionName, context, config);
            if(extension.isAllowed()) {
                return extension;
            }
        }
        return null;
    }
}
