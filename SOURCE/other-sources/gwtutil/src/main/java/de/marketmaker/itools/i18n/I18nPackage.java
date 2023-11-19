/*
 * I18nPackage.java
 *
 * Created on 21.07.2010 17:13:25
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * @author zzhao
 */
public class I18nPackage {

    private final Properties properties;

    private final Set<Lines> fileLinesSet;

    public I18nPackage() {
        this.properties = new Properties();
        this.fileLinesSet = new HashSet<Lines>();
    }

    public Properties getProperties() {
        return properties;
    }

    public Set<Lines> getFileLinesSet() {
        return fileLinesSet;
    }
}