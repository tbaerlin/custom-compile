package de.marketmaker.istar.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

/**
 * @author umaurer
 */
public class I18nFactoryDev extends I18nFactory {
    @Override
    protected I18nMap getCachedMap(String locale) {
        return null;
    }

    @Override
    protected InputStream getInputStream(String i18nFilename) throws FileNotFoundException {
        return new FileInputStream(new File(LocalConfigProvider.getIviewSrcDir() + "/mmgwt/src/main/java", i18nFilename));
    }
}
