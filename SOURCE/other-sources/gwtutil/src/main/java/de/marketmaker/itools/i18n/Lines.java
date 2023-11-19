/*
 * Lines.java
 *
 * Created on 20.07.2010 16:08:24
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zzhao
 */
public class Lines {

    private final File file;

    private final List<Line> lines;

    public Lines(File file) throws IOException {
        this.file = file;
        this.lines = new ArrayList<Line>();
        extractLines();
    }

    public File getFile() {
        return file;
    }

    public boolean existChanges() {
        for (Line line : this.lines) {
            final List<LiteralStringMatch> matches = line.getMatches();
            if (!matches.isEmpty()) {
                return true;
            }
        }

        return false;
    }

    private boolean existNonNLSLines() {
        for (Line line : this.lines) {
            if (line.isNonNLSLine()) {
                return true;
            }
        }

        return false;
    }

    public String getNonNLSLinesHtml(String pkgName) {
        if (!existNonNLSLines()) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<tr><td class=\"packageName\" colspan=\"3\">").append(pkgName).append(".");
        sb.append(this.file.getName().substring(0, this.file.getName().lastIndexOf(".")));
        sb.append("</td></tr>");
        for (int i = 0; i < this.lines.size(); i++) {
            Line line = this.lines.get(i);
            if (line.isNonNLSLine()) {
                appendOneNonNLSLine(i + 1, sb, line.getNonNLSHtmlLine());
            }
        }

        return sb.toString();
    }

    private void appendOneNonNLSLine(int lineNum, StringBuilder sb, String nonNLSHtmlLine) {
        sb.append("<tr>\n" +
                "<td>&nbsp;</td>\n" +
                "<td>").append(lineNum).append("</td>\n" +
                "<td class=\"code\">").append(nonNLSHtmlLine);
        sb.append("</td></tr>");
    }

    private void extractLines() throws IOException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(this.file));
//            StringBuilder sb = new StringBuilder();
            String str;
            while ((str = br.readLine()) != null) {
//                if (sb.length() > 0) {
//                    sb.append(SystemUtils.LINE_SEPARATOR);
//                }
//                sb.append(str);
//                String tmp = str.trim();
//                if (isEndOfLine(tmp)) {
//                    this.lines.add(new Line(sb.toString()));
//                    sb.setLength(0);
//                }
                this.lines.add(new Line(str));
            }
//            if (sb.length() != 0) {
//                this.lines.add(new Line(sb.toString()));
//            }
        } finally {
            if (null != br) {
                br.close();
            }
        }
    }

    private boolean isEndOfLine(String str) {
        return str.endsWith("{")
                || str.endsWith("}")
                || str.endsWith(";");
    }

    public List<Line> getLines() {
        return this.lines;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Lines lines = (Lines) o;

        if (file != null ? !file.equals(lines.file) : lines.file != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return file != null ? file.hashCode() : 0;
    }
}