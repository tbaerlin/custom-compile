/*
 * I18nConstantsGen.java
 *
 * Created on 21.07.2010 13:51:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;

/**
 * @author zzhao
 */
public class I18nMessagesGen {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final int MAX_DISTANCE = 2;

    private final Map<String, I18nPackage> reg;

    private final Properties propHistory;
    private static final String ARTIFACTS_DATE_TIME = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());

    public I18nMessagesGen() {
        this.reg = new HashMap<>();
        this.propHistory = new Properties();
        try {
            tryToLoadPropKeyHistory();
        }
        catch (IOException e) {
            this.logger.error("<I18nMessagesGen> cannot load property history");
        }
    }

    private void tryToLoadPropKeyHistory() throws IOException {
        final File propHistoryFile = new File(getMyTempDir(), "gwt-i18n-history.properties");
        if (propHistoryFile.exists()) {
            this.propHistory.load(new FileReader(propHistoryFile));
        }
    }

    private void persistPropKeyHistory() throws IOException {
        if (this.propHistory.isEmpty()) {
            return;
        }
        File propHistoryFile = new File(getMyTempDir(), "gwt-i18n-history.properties");
        this.propHistory.store(new FileWriter(propHistoryFile), "GWT i18n property history");
    }

    public void registerProperty(File dir, String key, String value) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Only dir allowed");
        }
        String path = dir.getAbsolutePath();
        if (!reg.containsKey(path)) {
            reg.put(path, new I18nPackage());
        }

        reg.get(path).getProperties().put(key, value);
        this.propHistory.put(key, value);
    }

    public void registerHistoricValue(File dir, String key) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Only dir allowed");
        }
        String path = dir.getAbsolutePath();
        final I18nPackage i18nPackage;
        if (reg.containsKey(path)) {
            i18nPackage = reg.get(path);
        }
        else {
            i18nPackage = new I18nPackage();
            reg.put(path, i18nPackage);
        }

        i18nPackage.getProperties().put(key, this.propHistory.get(key));
    }

    public String getProperty(File dir, String key) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("Only dir allowed");
        }

        final String historicValue = this.propHistory.getProperty(key);

        final String path = dir.getAbsolutePath();
        if (!reg.containsKey(path)) {
            return historicValue;
        }

        final String value = reg.get(path).getProperties().getProperty(key);
        return value == null ? historicValue : value;
    }

    public void addFileToGenerate(File dir, Lines lines) {
        String path = dir.getAbsolutePath();
        if (!reg.containsKey(path)) {
            reg.put(path, new I18nPackage());
        }
        this.reg.get(path).getFileLinesSet().add(lines);
    }

    public List<KeyValueWithDistance> getRecommendation(String value) {
        final List<KeyValueWithDistance> hits = new ArrayList<>();
        for (Map.Entry<Object, Object> entry : this.propHistory.entrySet()) {
            final int distance = StringUtils.getLevenshteinDistance((String) entry.getValue(), value);
            if (distance <= MAX_DISTANCE) {
                hits.add(new KeyValueWithDistance((String) entry.getKey(), (String) entry.getValue(), distance));
            }
        }

        if (hits.isEmpty()) {
            return Collections.emptyList();
        }

        Collections.sort(hits);

        return hits;
    }

    public static String getPackageName(String path, String pkgStartStr) {
        final String replacedStr = path.replace(SystemUtils.FILE_SEPARATOR, ".");
        if (StringUtils.isNotBlank(pkgStartStr)) {
            final int idx = replacedStr.indexOf("." + pkgStartStr + ".");
            if (idx != -1) {
                return replacedStr.substring(idx + 1);
            }
        }
        return replacedStr.substring(replacedStr.indexOf(".") + 1);
    }

    public void generateArtifacts(String packageNameStartString, Lines lines) throws IOException {
        final File nonNLSSummaryFile = getNonNLSSummaryFile(ARTIFACTS_DATE_TIME);
        BufferedWriter bw = null;
        try {
            if (nonNLSSummaryFile.exists()) {
                bw = new BufferedWriter(new FileWriter(nonNLSSummaryFile, true));
            }
            else {
                bw = new BufferedWriter(new FileWriter(nonNLSSummaryFile));
                outputSummaryStart(bw, ARTIFACTS_DATE_TIME);
            }

            if (!reg.isEmpty()) {
                writeLine(bw, "<table>");
                for (Map.Entry<String, I18nPackage> entry : reg.entrySet()) {
                    String i18nPkgName = getPackageName(entry.getKey(), packageNameStartString);
                    final String backUpDirName = i18nPkgName + System.currentTimeMillis();
                    final I18nPackage i18nPkg = entry.getValue();
                    generateI18nConstantsFile(entry.getKey(), i18nPkgName, i18nPkg.getProperties(), null);
                    generateI18nPropertiesFile(entry.getKey(), i18nPkg.getProperties(), null);

                    if (lines.existChanges()) {
                        generateFile(lines, backUpDirName);
                    }
                    final String nonNlsLinesHtml = lines.getNonNLSLinesHtml(i18nPkgName);
                    if (StringUtils.isNotBlank(nonNlsLinesHtml)) {
                        writeLine(bw, nonNlsLinesHtml);
                    }
                }
                try {
                    persistPropKeyHistory();
                }
                catch (IOException e) {
                    this.logger.error("<generateArtifacts> cannot persist property history", e);
                }
            }
        }
        finally {
            if (null != bw) {
                bw.close();
            }
        }
    }

    public void finishArtifacts() {
        try {
            final File nonNLSSummaryFile = getNonNLSSummaryFile(ARTIFACTS_DATE_TIME);
            final BufferedWriter bw = new BufferedWriter(new FileWriter(nonNLSSummaryFile, true));
            writeLine(bw, "</table>");
            outputSummaryEnd(bw);
            bw.close();
        }
        catch (IOException e) {
            this.logger.error("cannot finish artifacts", e);
        }

    }

    private void outputSummaryEnd(BufferedWriter bw) throws IOException {
        writeLine(bw, "</body>\n" +
                "</html>");
    }

    private void outputSummaryStart(BufferedWriter bw, String runDateTime) throws IOException {
        writeLine(bw, "<html>\n" +
                "<head>\n" +
                "<title id=\"title\">I18n Non-NLS Summary: " + runDateTime + "</title>\n" +
                "<meta content=\"\">\n" +
                "<style type=\"text/css\">\n" +
                "     .code { color: #aaaaaa; white-space: nowrap; font-family: monospace; }\n" +
                "     .mystring { color: #006600; font-weight: bold; }\n" +
                "     .packageName { font-weight: bold; }" +
                "</style>\n" +
                "</head>\n" +
                "<body>");
    }

    private File getNonNLSSummaryFile(String runDateTime) {
        File ret = new File(getMyTempDir(), "i18n-non-nls-sum-" + runDateTime + ".html");
        if (ret.exists() && !ret.delete()) {
            throw new IllegalStateException("cannot get Non-NLS summary file: " + ret.getAbsolutePath());
        }

        return ret;
    }

    private void generateI18nPropertiesFile(String dir, Properties prop,
                                            String backUpDirName) throws IOException {
        if (prop.size() == 0) {
            // won't generate empty property file
            return;
        }
        File i18nPropFile = getFile(dir, "I18nMessages.properties", backUpDirName);
        prop.store(new FileWriter(i18nPropFile), "Generated by I18nMessagesGen.java");
    }

    private void generateFile(Lines lines, String backUpDirName) throws IOException {
        final File theFile = lines.getFile();
        File file = getFile(theFile.getParent(), theFile.getName(), backUpDirName);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(file));
            for (Line line : lines.getLines()) {
                writeLine(bw, line.getAdjustedLine());
            }
        }
        finally {
            if (null != bw) {
                bw.close();
            }
        }
    }


    private void generateI18nConstantsFile(String dir, String i18nPkgName, Properties prop,
                                           String backUpDirName) throws IOException {
        if (prop.size() == 0) {
            // won't generate empty constants file
            return;
        }
        File i18nFile = getFile(dir, "I18nMessages.java", backUpDirName);

        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(i18nFile));
            writeLine(bw, "// generated by I18nConstantsGen");
            writeLine(bw, "package " + i18nPkgName + ";");
            writeLine(bw, "import com.google.gwt.core.client.GWT;");
            writeLine(bw, "import com.google.gwt.i18n.client.Messages;");
            writeLine(bw, "public interface I18nMessages extends Messages{");
            writeLine(bw, "\t\tI18nMessages INSTANCE = (I18nMessages) GWT.create(I18nMessages.class);");

            for (String key : new TreeSet<>(prop.stringPropertyNames())) {
                writeLine(bw, "\t\tString " + key + "();");
            }

            writeLine(bw, "}");
        }
        finally {
            if (null != bw) {
                bw.close();
            }
        }
    }

    private void writeLine(BufferedWriter bw, String str) throws IOException {
        bw.write(str);
        bw.write(SystemUtils.LINE_SEPARATOR);
    }

    private File getFile(String dir, String fileName, String backUpDirName) throws IOException {
        File file = new File(dir, fileName);
        if (backUpDirName == null) {
            return file;
        }

        if (file.exists()) {
            File tmpDir = new File(getMyTempDir(), backUpDirName);
            if (!tmpDir.exists() && !tmpDir.mkdir()) {
                throw new IllegalStateException("cannot create tmp dir: " + tmpDir.getAbsolutePath());
            }
            final File dest = new File(tmpDir, fileName);
            FileUtils.copyFile(file, dest, true);
        }

        return new File(dir, fileName);
    }

    private static File getMyTempDir() {
        final String h = System.getProperty("gwt.i18n.myTempDir");
        if (h == null) {
            return SystemUtils.getJavaIoTmpDir();
        }
        else {
            return new File(h);
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, I18nPackage> entry : reg.entrySet()) {
            sb.append(getPackageName(entry.getKey(), "")).append(SystemUtils.LINE_SEPARATOR);
            final Properties prop = entry.getValue().getProperties();
            for (String key : prop.stringPropertyNames()) {
                sb.append("\t").append(key).append("=").append(prop.getProperty(key));
                sb.append(SystemUtils.LINE_SEPARATOR);
            }
        }

        return sb.toString();
    }
}
