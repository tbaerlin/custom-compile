/*
 * SymbolMapping.java
 *
 * Created on 26.07.2010 16:04:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.core.io.ClassPathResource;

import de.marketmaker.istar.common.util.PropertiesLoader;

/**
 * @author oflege
 */
class SymbolMapping {

    private static Map<String, SymbolMapping> MAPPINGS = new HashMap<>();

    static {
        final Properties properties = loadProperties();
        for (String s : properties.stringPropertyNames()) {
            MAPPINGS.put(s, create(properties.getProperty(s)));
        }
    }

    private static Properties loadProperties() {
        try {
            return PropertiesLoader.load(new ClassPathResource("symbolmappings.properties", SymbolMapping.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static SymbolMapping create(String value) {
        final String[] strings = value.split(";");
        return new SymbolMapping(strings[0], strings.length > 1 ? Integer.parseInt(strings[1]) : -1);
    }

    static SymbolMapping getMapping(String s) {
        return MAPPINGS.get(s);
    }

    private final String vwdcode;

    private final int field;

    SymbolMapping(String vwdcode, int field) {
        this.vwdcode = vwdcode;
        this.field = field;
    }

    String getVwdcode() {
        return this.vwdcode;
    }

    int getField() {
        return this.field;
    }
}
