/*
 * TypeMapping.java
 *
 * Created on 22.06.2010 14:56:44
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
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class TypeMapping {
    private static Map<InstrumentTypeEnum, String> MAPPINGS = new HashMap<>();

    static {
        final Properties properties = loadProperties();
        for (final String s : properties.stringPropertyNames()) {
            MAPPINGS.put(InstrumentTypeEnum.valueOf(s), properties.getProperty(s));
        }
    }

    private static Properties loadProperties() {
        try {
            return PropertiesLoader.load(new ClassPathResource("typemappings.properties", TypeMapping.class));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static String getMapping(InstrumentTypeEnum type) {
        final String s = MAPPINGS.get(type);
        return s != null ? s : "XX"; // XX is type Unknown
    }

    public static void main(String[] args) {
        System.out.println(TypeMapping.getMapping(InstrumentTypeEnum.STK));
        System.out.println(TypeMapping.getMapping(InstrumentTypeEnum.UND));
        System.out.println(TypeMapping.getMapping(null));
    }
}
