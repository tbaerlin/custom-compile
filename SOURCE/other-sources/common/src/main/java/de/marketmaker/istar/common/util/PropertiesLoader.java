/*
 * PropertiesLoader.java
 *
 * Created on 07.07.2008 09:41:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.util.Properties;
import java.io.File;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

/**
 * Makes sure Properties are loaded from files using UTF-8 encoding, defaults to ISO-8859-1.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PropertiesLoader {
    public static Properties load(File f) throws IOException {
        return load(new FileInputStream(f));
    }

    public static Properties load(InputStream is) throws IOException {
        final Properties result = new Properties();

        try (Reader reader = new InputStreamReader(is, "UTF-8")) {
            result.load(reader);
            return result;
        }
    }

    public static Properties load(Resource resource) throws IOException {
        return load(resource.getInputStream());
    }

    private PropertiesLoader() {
    }
}
