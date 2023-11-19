/*
 * LocalConfigProvider.java
 *
 * Created on 17.05.2007 11:04:56
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Provides directories for various projects and other locations.
 *
 * DO NOT USE IN PRODUCTION ENVIRONMENT.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class LocalConfigProvider {
    private static final Properties s_properties;

    private static final File HOME = new File(System.getProperty("user.home"));

    private static final File TMP = new File(System.getProperty("java.io.tmpdir"));

    static {
        try {
            final File configFile1 = new File(HOME, "mm-dev-local-config.prop");
            final File configFile2 = new File(HOME, "conf/mm-dev-local-config.prop");

            if (configFile1.exists()) {
                s_properties = PropertiesLoader.load(configFile1);
            }
            else if (configFile2.exists()) {
                s_properties = PropertiesLoader.load(configFile2);
            }
            else {
                s_properties = new Properties();
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File getProductionDir(String path) {
        return new File(getProductionBaseDir(), path);
    }

    public static File getProductionBaseDir() {
        return getDir("productionBaseDir", new File(HOME, "produktion"));
    }                    

    public static File getProjectsBaseDir() {
        return getDir("projectsBaseDir", HOME);
    }

    private static File getProjectDir(String name) {
        return getDir(name + "SrcDir", new File(getProjectsBaseDir(), name));
    }

    public static File getTempDir() {
        return getDir("tempDir", TMP);
    }

    public static File getIstarSrcDir() {
        return getProjectDir("istar");
    }

    public static File getIviewSrcDir() {
        return getProjectDir("iview");
    }

    public static File getItoolsSrcDir() {
        return getProjectDir("itools");
    }

    public static File getIdocSrcDir() {
        return getProjectDir("idoc");
    }

    private static File getDir(String key, File defaultValue) {
        final File result = doGetDir(key, defaultValue);
        if (!result.isDirectory()) {
            throw new IllegalStateException(key + " = " + result.getAbsolutePath());
        }
        return result;
    }

    private static File doGetDir(String key, File defaultValue) {
        if (s_properties.containsKey(key)) {
            return new File(s_properties.getProperty(key));
        }
        return defaultValue;
    }
}
