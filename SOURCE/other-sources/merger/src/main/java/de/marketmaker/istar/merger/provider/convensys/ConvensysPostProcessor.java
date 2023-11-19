/*
 * ConvensysPostProcessor.java
 *
 * Created on 10.09.2008 15:31:56
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.convensys;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;

import de.marketmaker.istar.common.util.I18nFactory;
import de.marketmaker.istar.common.util.I18nMap;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.util.IsinUtil;

/**
 * Batch process that creates files derived from convensys data.<p>
 * Files are generated using rules from a file. currently, each rule has to look like this:
 * <pre>
 * sourceDir=profiles;targetDir=dzbank/profiles;template=dzprofile.vm;suffix=.txt;sourceLanguage=en;targetLanguage=de
 * </pre>
 * <dl>
 * <dt>sourceDir
 * <dd>directory below {@link #inputBaseDir} with input files
 * <dt>targetDir
 * <dd>directory below {@link #outputBaseDir} for output files
 * <dt>template
 * <dd>velocity template to be used to process each input file
 * <dt>suffix
 * <dd>file suffix for output files (optional, default is ".txt")
 * <dt>sourceLanguage</dt>
 * <dd>ISO language code of the language that determines the
 * decimal format symbols, which are used to parse numbers in source files</dd>
 * <dt>targetLanguage</dt>
 * <dd>ISO language code that 1. determines the decimal format symbols, which are used to
 * format numbers in target files, and 2. determines the language for static labels
 * in the target files</dd>
 * </dl>
 *
 * An output file will be generated if there is no such file that is more recent than the respective
 * input file. For each file to be generated, a {@link org.w3c.dom.Document} will be created
 * from the respective convensys file, wrapped into an {@link de.marketmaker.istar.merger.provider.convensys.XmlTool}
 * instance, and that instance will be available in the velocity templates as "$xml".
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Markus Dick
 */
public class ConvensysPostProcessor implements InitializingBean {
    /**
     * Abstract base class for all Rule implementations
     */
    private abstract class AbstractRule implements Rule {
        final String sourceDir;

        final String suffix;

        final String targetDir;

        final String sourceLanguage;

        final String targetLanguage;

        private final Set<String> toBeGenerated = new HashSet<>();

        private AbstractRule(String sourceDir, String targetDir, String suffix,
                String sourceLanguage, String targetLanguage) {
            this.sourceDir = sourceDir;
            this.targetDir = targetDir;
            this.suffix = suffix;
            this.sourceLanguage = sourceLanguage;
            this.targetLanguage = targetLanguage;
        }

        public final void apply(String name, Document d) throws Exception {
            if (!isToBeGenerated(name)) {
                return;
            }
            doApply(name, d);
        }

        public Set<String> init() {
            File dir = getOutputDir();
            if (!dir.isDirectory() && !dir.mkdirs()) {
                logger.error("<init> output dir not available: " + dir.getAbsolutePath());
                return this.toBeGenerated;
            }
            final Set<String> upToDate = new HashSet<>();
            final Map<String, Long> sourceFileTimestamps = sourceFiles.get(this.sourceDir);

            try {
                Files.list(Paths.get(dir.getAbsolutePath()))
                        .filter(p -> Files.isRegularFile(p))
                        .map(Path::toFile)
                        .filter(f -> f.getName().endsWith(suffix))
                        .forEach(file -> {
                            final String name = getFileNameWithoutSuffix(file, suffix);
                            final Long ts = sourceFileTimestamps.get(name);
                            if (ts != null && file.lastModified() > ts) {
                                upToDate.add(name);
                            }
                        });
            } catch (IOException ignored) {
            }

            this.toBeGenerated.addAll(sourceFileTimestamps.keySet());
            this.toBeGenerated.removeAll(upToDate);
            logger.info("<init> for " + this.toString() + " found " + this.toBeGenerated.size()
                    + " files to be generated");
            return this.toBeGenerated;
        }

        protected File getOutputDir() {
            return new File(outputBaseDir, this.targetDir);
        }

        protected boolean isToBeGenerated(String name) {
            return this.toBeGenerated.contains(name);
        }

        abstract void doApply(String name, Document d) throws Exception;
    }

    private interface Rule {
        /**
         * For the given name and document, apply this rule.
         * @param name name of input/output file (without suffix)
         * @param d content of input file
         * @throws Exception on failure
         */
        void apply(String name, Document d) throws Exception;

        /**
         * Determines which files need to be used for generating content and returns a set with
         * the names of those files (without the .xml suffix)
         * @return names of files need to generate
         */
        Set<String> init();
    }

    /**
     * Rule implementation that uses velocity to create derived files.
     */
    private class TemplateRule extends AbstractRule {
        final String templateName;

        final I18nFactory i18nFactory = new I18nFactory();

        final Locale sourceLanguageLocale;

        final Locale targetLanguageLocale;

        private TemplateRule(String sourceDir, String targetDir, String templateName, String suffix,
                String sourceLanguage, String targetLanguage) {
            super(sourceDir, targetDir, suffix, sourceLanguage, targetLanguage);

            sourceLanguageLocale = new Locale(sourceLanguage);
            targetLanguageLocale = new Locale(targetLanguage);

            if (DecimalFormatSymbols.getInstance(sourceLanguageLocale) == null) {
                throw new IllegalArgumentException("DecimalFormatSymbols for language " + sourceLanguage + " source not supported!");
            }

            if (DecimalFormatSymbols.getInstance(targetLanguageLocale) == null) {
                throw new IllegalArgumentException("DecimalFormatSymbols for language " + targetLanguage + " source not supported!");
            }

            this.templateName = templateName;
        }

        public String toString() {
            return new StringBuilder(200).append("TemplateRule[")
                    .append("sourceDir=").append(this.sourceDir)
                    .append(", targetDir=").append(this.targetDir)
                    .append(", template=").append(this.templateName)
                    .append(", suffix=").append(this.suffix)
                    .append(", sourceLanguage=").append(this.sourceLanguage)
                    .append(", targetLanguage=").append(this.targetLanguage)
                    .toString();
        }

        void doApply(String name, Document d) throws Exception {
            final File dir = getOutputDir();
            final File f = new File(dir, name + this.suffix);
            final File tmp = new File(dir, name + this.suffix + ".tmp");

            final VelocityContext context = new VelocityContext();
            context.put("xml", new XmlTool(d));
            context.put("counterTool", new CounterTool());
            context.put("shaper", new ConvensysContentShaper(sourceLanguageLocale, targetLanguageLocale));
            context.put("stringTool", new StringTool());
            context.put("language", targetLanguage);
            context.put("i18n", new EscapeHtmlI18nMap(i18nFactory.getI18n(targetLanguage)));
            try (Writer writer = new BufferedWriter(new FileWriter(tmp))) {
                velocityEngine.getTemplate(this.templateName).merge(context, writer);
            }

            if (f.exists() && !f.delete()) {
                throw new Exception("Could not delete " + f.getAbsolutePath());
            }

            if (!tmp.renameTo(f)) {
                throw new Exception("Could not rename " + tmp.getAbsolutePath() + " to " + f.getAbsolutePath());
            }
        }
    }

    public static void main(String[] args) {
        //noinspection resource,IOResourceOpenedButNotSafelyClosed
        new FileSystemXmlApplicationContext(args);
    }

    private File inputBaseDir;

    private File conversionRules;

    private DocumentBuilder documentBuilder;

    private int limit = 0; // set to small value != 0 for testing purposes

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File outputBaseDir;

    /**
     * Contains a list of rules for each subdirectory of {@link #inputBaseDir}.
     */
    private final Map<String, List<Rule>> rules = new HashMap<>();

    /**
     * this map's key denotes a subdirectory of {@link #inputBaseDir}. The value is a map that
     * contains the timestamp for each file, where the key is the original filename without
     * the suffix (.xml)
     */
    private final Map<String, Map<String, Long>> sourceFiles = new HashMap<>();

    private VelocityEngine velocityEngine;

    public void afterPropertiesSet() throws Exception {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        this.documentBuilder = factory.newDocumentBuilder();

        readRules();
        scanSourceFiles();
        applyRules();
    }

    public void setInputBaseDir(File inputBaseDir) {
        this.inputBaseDir = inputBaseDir;
    }

    public void setConversionRules(File conversionRules) {
        this.conversionRules = conversionRules;
    }

    public void setLimit(int limit) {
        this.limit = limit;
        this.logger.info("<setLimit> at most " + this.limit + " files in each sourceDir will be processed");
    }

    public void setOutputBaseDir(File outputBaseDir) {
        this.outputBaseDir = outputBaseDir;
    }

    public void setVelocityEngine(VelocityEngine velocityEngine) {
        this.velocityEngine = velocityEngine;
    }

    private void applyRules() {
        for (Map.Entry<String, List<Rule>> entry : rules.entrySet()) {
            applyRules(entry.getKey(), entry.getValue());
        }
    }

    private void applyRules(String sourceDir, List<Rule> rules) {
        this.logger.info("<applyRules> for sourceDir " + sourceDir);
        final TimeTaker tt = new TimeTaker();

        final Set<String> toBeGenerated = new HashSet<>();
        for (Rule rule : rules) {
            toBeGenerated.addAll(rule.init());
        }

        this.logger.info("<applyRules> needs to create " + toBeGenerated.size() + " documents");

        final File dir = new File(this.inputBaseDir, sourceDir);
        int n = 0;
        for (String name : toBeGenerated) {
            final File f = new File(dir, name + ".xml");
            final Document d;
            try {
                d = this.documentBuilder.parse(f);
            } catch (Exception e) {
                this.logger.error("failed to parse xml document: " + f.getAbsolutePath(), e);
                continue;
            }

            for (Rule rule : rules) {
                try {
                    rule.apply(name, d);
                } catch (Exception e) {
                    this.logger.error("<applyRules> failed for " + f.getAbsolutePath() + " and rule " + rule, e);
                }
            }

            if (this.limit > 0 && ++n == limit) {
                this.logger.info("<applyRules> reached limit, stopping");
                break;
            }
        }
        this.logger.info("<applyRules> finished for sourceDir " + sourceDir + ", took " + tt);
    }

    private String getFileNameWithoutSuffix(File file, String suffix) {
        final String name = file.getName();
        return name.substring(0, name.length() - suffix.length());
    }

    private void readRules() throws FileNotFoundException {
        try (Scanner s = new Scanner(this.conversionRules)) {
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                final Properties p = StringUtils.splitArrayElementsIntoProperties(line.split(";"), "=");
                final String sourceDir = p.getProperty("sourceDir");
                if (sourceDir == null) {
                    this.logger.error("<afterPropertiesSet> missing sourceDir in line '" + line + "'");
                    continue;
                }
                final String targetDir = p.getProperty("targetDir");
                if (targetDir == null) {
                    this.logger.error("<afterPropertiesSet> missing targetDir in line '" + line + "'");
                    continue;
                }
                if (!this.sourceFiles.containsKey(sourceDir)) {
                    this.sourceFiles.put(sourceDir, new HashMap<>());
                }
                if (p.getProperty("template") != null) {
                    List<Rule> list = rules.get(sourceDir);
                    if (list == null) {
                        list = new ArrayList<>();
                        rules.put(sourceDir, list);
                    }
                    list.add(new TemplateRule(sourceDir, targetDir, p.getProperty("template"), p.getProperty("suffix", ".txt"), p.getProperty("sourceLanguage", "en"), p.getProperty("targetLanguage", "de")));
                }
                else {
                    this.logger.warn("<afterPropertiesSet> unknown rule in line '" + line + "'");
                }
            }
        }
    }

    private void scanSourceFiles() {
        for (Map.Entry<String, Map<String, Long>> entry : this.sourceFiles.entrySet()) {
            File dir = new File(this.inputBaseDir, entry.getKey());
            if (!dir.isDirectory()) {
                this.logger.error("<scanSourceFiles> not found: " + dir.getAbsolutePath());
                continue;
            }
            final File[] files = dir.listFiles(f -> {
                //check filename starts with isin
                return f.isFile() && f.getName().endsWith(".xml") && IsinUtil.isIsin(getFileNameWithoutSuffix(f, ".xml"));
            });
            final Map<String, Long> map = entry.getValue();
            for (File file : files) {
                map.put(getFileNameWithoutSuffix(file, ".xml"), file.lastModified());
            }
            this.logger.info("<scanSourceFiles> found " + files.length + " files in " + dir.getAbsolutePath());
        }
    }

    public static class EscapeHtmlI18nMap extends I18nMap {
        private final I18nMap i18nMap;

        public EscapeHtmlI18nMap(I18nMap i18nMap) {
            this.i18nMap = i18nMap;
        }

        @Override
        public String getNWithParams(String key, int n, Object... params) {
            return StringEscapeUtils.escapeHtml4(i18nMap.getNWithParams(key, n, params));
        }

        @Override
        public String getN(String key, int n) {
            return StringEscapeUtils.escapeHtml4(i18nMap.getN(key, n));
        }

        @Override
        public String getWithParams(String key, Object... params) {
            return StringEscapeUtils.escapeHtml4(i18nMap.getWithParams(key, params));
        }

        @Override
        public String get(String key) {
            return StringEscapeUtils.escapeHtml4(i18nMap.get(key));
        }

        @Override
        public String put(String key, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<String> getInvalidKeys() {
            throw new UnsupportedOperationException();
        }
    }
}
