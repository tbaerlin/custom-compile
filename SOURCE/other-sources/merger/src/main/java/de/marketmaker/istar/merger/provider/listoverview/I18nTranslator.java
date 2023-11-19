package de.marketmaker.istar.merger.provider.listoverview;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.log.CommonsLogLogChute;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.I18nFactory;
import de.marketmaker.istar.common.util.I18nMap;
import de.marketmaker.istar.common.util.LocalConfigProvider;

/**
 * @author Ulrich Maurer
 *         Date: 20.12.11
 */
class I18nTranslator {
    private final I18nFactory I18N_FACTORY = new I18nFactory(){
        @Override
        protected InputStream getInputStream(String i18nFilename) throws FileNotFoundException {
            return new FileInputStream(new File(LocalConfigProvider.getIviewSrcDir() + "/mmgwt/src/main/java", i18nFilename));
        }
    };

    public void translate(File sourceFile, File destinationFile, String locale, String country) throws Exception {
        final I18nMap i18nMap = I18N_FACTORY.getI18n(locale);

        final VelocityEngine ve = initializeVelocity(sourceFile.getParentFile().getAbsolutePath());
        final VelocityContext vc = new VelocityContext();
        vc.put("locale", locale);
        vc.put("i18n", i18nMap);
        vc.put("dateTime", new DateTime());
        vc.put("country", country);

        final Writer writer = new BufferedWriter(new FileWriter(destinationFile));
        final Template template = ve.getTemplate(sourceFile.getName());
        template.merge(vc, writer);
        writer.close();

        final Set<String> invalidKeys = i18nMap.getInvalidKeys();
        if (!invalidKeys.isEmpty()) {
            final int amount = invalidKeys.size();
            System.out.println(invalidKeys);
            throw new RuntimeException("I18nTranslator didn't find " + amount + (amount == 1 ? " key" : " keys") + " in " + sourceFile.getName() + " for locale " + locale);
        }
    }

    private VelocityEngine initializeVelocity(String templatePath) throws Exception {
        final VelocityEngine ve = new VelocityEngine();
        ve.setProperty("input.encoding", "UTF-8");
        ve.setProperty("output.encoding", "UTF-8");
        ve.setProperty("resource.loader", "file");
        ve.setProperty("file.resource.loader.class", "org.apache.velocity.runtime.resource.loader.FileResourceLoader");
        ve.setProperty("file.resource.loader.path", templatePath);
        ve.setProperty("file.resource.loader.cache", true);
        ve.setProperty("file.resource.loader.modificationCheckInterval", "0");
        ve.setProperty("runtime.references.strict", true);
        ve.setProperty(Velocity.RUNTIME_LOG_LOGSYSTEM, new CommonsLogLogChute());
        ve.init();
        return ve;
    }
}
