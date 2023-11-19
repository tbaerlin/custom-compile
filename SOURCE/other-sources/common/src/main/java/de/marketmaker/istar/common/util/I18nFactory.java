package de.marketmaker.istar.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Read mmgwt I18n.properties and give access to them for fusion velocity files.
 * @author umaurer
 */
public class I18nFactory {
    private static final String I18N_PROPERTIES_FILE = "/de/marketmaker/iview/mmgwt/mmweb/client/I18n{locale}.properties";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Map<String, I18nMap> cache = Collections.synchronizedMap(new HashMap<>());

    private String i18nLocation = I18N_PROPERTIES_FILE;

    public void setI18nLocation(String i18nLocation) {
        this.i18nLocation = i18nLocation;
    }

    public I18nMap getI18n(Locale locale, boolean useCache) {
        return getI18n(locale.toString(), useCache);
    }

    public I18nMap getI18n(String locale, boolean useCache) {
        if (locale == null) {
            return getI18n("", useCache);
        }

        if (useCache) {
            final I18nMap existing = getCachedMap(locale);
            if (existing != null) {
                return existing;
            }
        }

        I18nMap result = new I18nMap();
        final String localeWithSuffix = locale.isEmpty() ? "" : ("_" + locale);
        read(localeWithSuffix, result);

        if (useCache) {
            this.cache.put(locale, result);
        }
        return result;
    }

    protected I18nMap getCachedMap(String locale) {
        return this.cache.get(locale);
    }

    public I18nMap getI18n(String locale) {
        return getI18n(locale, true);
    }

    private void read(String localeSuffix, I18nMap map) {
        if (!localeSuffix.isEmpty()) { // first read I18n file with less specified locale
            read(localeSuffix.substring(0, localeSuffix.lastIndexOf('_')), map);
        }

        final String i18nFilename = this.i18nLocation.replace("{locale}", localeSuffix);
        try {
            Properties p = PropertiesLoader.load(getInputStream(i18nFilename));
            addProperties(p, map);
        } catch (IOException e) {
            logger.warn("failed to read " + i18nFilename, e);
        }
    }

    protected InputStream getInputStream(String i18nFilename) throws FileNotFoundException {
        final File f = new File(i18nFilename);
        if (f.canRead()) {
            return new FileInputStream(f);
        }
        final InputStream is = getClass().getResourceAsStream(i18nFilename);
        if (is != null) {
            return is;
        }
        throw new FileNotFoundException("cannot load " + i18nFilename);
    }

    private void addProperties(Properties properties, I18nMap map) throws IOException {
        for (final String key : properties.stringPropertyNames()) {
            map.put(key, properties.getProperty(key));
        }
    }
}
