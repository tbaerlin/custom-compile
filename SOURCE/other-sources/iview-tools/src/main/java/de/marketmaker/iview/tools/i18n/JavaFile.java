/*
 * JavaFile.java
 *
 * Created on 16.08.2010 10:49:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.tools.i18n;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.SystemUtils;
import org.springframework.util.Assert;

/**
 * @author zzhao
 */
public class JavaFile {

    private final File file;

    private final List<String> lines;

    private final Set<Integer> irrelevantLineNumbers;

    private String qualifiedClassName;

    private boolean nonNLSAnnotated;

    private JavaFile(File file) {
        Assert.isTrue(null != file && file.canRead(), "Invalid java file"); // $NON-NLS$
        this.file = file;
        this.lines = new ArrayList<String>();
        this.irrelevantLineNumbers = new HashSet<Integer>();
    }

    public static JavaFile from(File file) throws IOException {
        final JavaFile ret = new JavaFile(file);
        ret.init();

        return ret;
    }

    private void init() throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.file));

            String str;
            boolean isComment = false;
            int line = 0;
            while ((str = br.readLine()) != null) {
                this.lines.add(str);
                if (I18nUtil.isStartMultiLineComment(str)) {
                    isComment = true;
                }

                if (!this.nonNLSAnnotated && I18nUtil.isAnnotationLine(str)) {
                    this.nonNLSAnnotated = str.trim().equals("@" + NonNLS.class.getSimpleName());
                }

                if (isComment || I18nUtil.isIrrelevantLine(str)) {
                    this.irrelevantLineNumbers.add(line);
                }

                if (!I18nUtil.isOneLineComment(str) && I18nUtil.isEndMultiLineComment(str)) {
                    isComment = false;
                }

                ++line;
            }
        } finally {
            if (null != br) {
                br.close();
            }
        }
    }

    public String getQualifiedClassName() {
        return qualifiedClassName;
    }

    public boolean isRelevantLine(int idx) {
        return !this.irrelevantLineNumbers.contains(idx);
    }

    public void applyProcessResults() throws IOException {
        BufferedWriter bw = null;
        try {
            bw = new BufferedWriter(new FileWriter(this.file));
            for (final String line : this.lines) {
                if (null != line) {
                    bw.write(line);
                    bw.write(SystemUtils.LINE_SEPARATOR);
                }
            }
        } finally {
            if (null != bw) {
                bw.close();
            }
        }
    }

    public void process(JavaLineProcessor jlp) {
        for (int i = 0; i < this.lines.size(); i++) {
            if (this.irrelevantLineNumbers.contains(new Integer(i))) {
                continue;
            }
            final String oneLine = this.lines.get(i);
            String line = jlp.processLine(this.file, i, oneLine);
            if (!oneLine.equals(line)) {
                this.lines.set(i, line);
            }
        }
    }

    public void processReadOnly(JavaLineProcessor jlp) {
        for (int i = 0; i < this.lines.size(); i++) {
            if (this.irrelevantLineNumbers.contains(new Integer(i))) {
                continue;
            }
            final String oneLine = this.lines.get(i);
            jlp.processLine(this.file, i, oneLine);
        }
    }

    public boolean isAnnotatedNonNLS() {
        return this.nonNLSAnnotated;
    }

    public interface JavaLineProcessor {
        String processLine(File file, int idx, String theLine);
    }
}
