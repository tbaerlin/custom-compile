/*
 * IsoCurrencySourceReader.java
 *
 * Created on 06.05.2010 13:32:40
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import de.marketmaker.istar.common.util.PropertiesLoader;

/**
 * @author oflege
 */
class IsoCurrencySourceReader {

    private final Map<String, IsoCurrencySource> sources = new HashMap<>();


    Map<String, IsoCurrencySource> read(Resource config) throws Exception {
        Pattern isoMapping = Pattern.compile("iso\\.([A-Z]{3})\\.([A-Z]{3})");

        Properties properties = PropertiesLoader.load(config);
        for (String name : properties.stringPropertyNames()) {
            final Matcher m = isoMapping.matcher(name);
            if (m.matches()) {
                final IsoCurrencySource source = this.sources.computeIfAbsent(m.group(1), IsoCurrencySource::new);
                source.addResult(IsoCurrencyResult.create(m.group(2), properties.getProperty(name).trim()));
            }
        }

        return this.sources;
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new IsoCurrencySourceReader().read(new ClassPathResource("currencyconversions.properties",
                IsoCurrencySourceReader.class)));
    }
}
