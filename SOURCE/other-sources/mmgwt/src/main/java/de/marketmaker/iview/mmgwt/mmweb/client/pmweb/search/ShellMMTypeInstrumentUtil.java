/*
 * ShellMMTypeInstrumentUtil.java
 *
 * Created on 19.02.13 12:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.search;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author Markus Dick
 */
public class ShellMMTypeInstrumentUtil {
    private final static HashMap<String, String> NAMES = new HashMap<String, String>();

    public static final String TYPE_ALL = "xALL"; //$NON-NLS$
    public static final String TYPE_OTHER = "xOTHER"; //$NON-NLS$

    static {
        for(ShellMMType type : ShellMMType.values()) {
            NAMES.put(type.name(), PmRenderers.SHELL_MM_TYPE_PLURAL.render(type));
        }

        NAMES.put(TYPE_ALL, I18n.I.all());
        NAMES.put(TYPE_OTHER, I18n.I.other());
    }

    private ShellMMTypeInstrumentUtil() {
        /* do nothing */
    }

    public static String getLabel(String typeName) {
        final String s = NAMES.get(typeName);
        return (s != null) ? s : typeName;
    }

    public static String getLabel(ShellMMType type) {
        final String s = NAMES.get(type.name());
        return (s != null) ? s : type.value().substring(2);
    }
}
