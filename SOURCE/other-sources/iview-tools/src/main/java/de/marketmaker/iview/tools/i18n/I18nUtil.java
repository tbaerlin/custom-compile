/*
 * JavaFileFilter.java
 *
 * Created on 16.08.2010 11:04:50
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.tools.i18n;

import org.apache.commons.lang.SystemUtils;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zzhao
 */
public final class I18nUtil {
    private enum State {
        NORMAL, IN_STRING, BACKSLASH, COMMENT
    }

    private static final Pattern PATTERN_LITERAL_STRING = Pattern.compile(
            "\"(([^\"]*?(\\\\\")*)+)\""  // $NON-NLS$
    );

    private static final Pattern PATTERN_JS_WITH_NLS_LINE_COMMENT = Pattern.compile(
            "^\\s*[^/][^/].*(//.*(\\$NLS-\\d+\\$)+.*)$" // $NON-NLS-0$
    );

    private static final Pattern PATTERN_NON_NLS = Pattern.compile(
            ".*\\$NON-NLS\\$.*" // $NON-NLS$
    );

    /**
     * group 1: index
     */
    private static final Pattern PATTERN_NON_NLS_INDEX = Pattern.compile(
            "\\$NON-NLS-(-?\\d+)\\$" // $NON-NLS$
    );

    private static final Pattern[] PATTERNS_IGNORE = {
            Pattern.compile("\\s*DebugUtil\\.logToFirebugConsole\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile("\\s*DebugUtil\\.logToServer\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile("\\s*DebugUtil\\.showDeveloperNotification\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile(".*\\.(log|debug|info|warn|error|group|groupCollapsed|logAsGroup|groupStart)\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile("\\s*il\\.log\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile("\\s*assert.*:.*;\\s*"), // $NON-NLS$
            Pattern.compile(".*\\.setStyleName\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile(".*\\.addStyleName\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile(".*\\.removeStyleName\\(.*\\);\\s*"), // $NON-NLS$
            Pattern.compile(".*\\.appendHtmlConstant\\(.*\\);\\s*") // $NON-NLS$
    };

    private static final Pattern[] PATTERNS_LS_IGNORE = {
            Pattern.compile("\\W*") // $NON-NLS$
    };

    private static final String POINT = "."; // $NON-NLS$

    private I18nUtil() {
        throw new AssertionError("not for instantiation or inheritance"); // $NON-NLS$
    }

    public static final FileFilter INSTANCE_DIR_JAVA_SOURCE = new FileFilter() {
        public boolean accept(File pathname) {
            return pathname.isDirectory() && !pathname.getName().startsWith(POINT);
        }
    };

    public static final FileFilter INSTANCE_NON_I18N_JAVA_FILE = new FileFilter() {
        public boolean accept(File pathname) {
            final String fileName = pathname.getName();
            return pathname.isFile() && fileName.endsWith(".java") // $NON-NLS$
                    && !"I18nConstants.java".equals(fileName) // $NON-NLS$
                    && !"I18nMessages.java".equals(fileName); // $NON-NLS$
        }
    };

    public static boolean isAnnotationLine(String line) {
        boolean seenLeftQuote = false;
        for (int i = 0; i < line.length(); i++) {
            final char c = line.charAt(i);
            if (c == '\"') {
                seenLeftQuote = !seenLeftQuote;
            }
            else if (c == '@' && !seenLeftQuote) {
                return true;
            }
        }

        return false;
    }

    public static boolean isOneLineComment(String line) {
        return line.trim().startsWith("//"); // $NON-NLS$
    }

    public static String getNLSOneLineComment(String line) {
        final Matcher matcher = PATTERN_JS_WITH_NLS_LINE_COMMENT.matcher(line);
        return matcher.matches() ? matcher.group(1) : ""; // $NON-NLS$
    }

    /**
     * Finds the line comment after java code.
     *
     * @param line a java code line
     * @return the line comment if found, null otherwise.
     */
    public static String getLineCommentAfterCode(String line) {
        State state = State.NORMAL;
        for (int i = 0, lineLength = line.length(); i < lineLength; i++) {
            char c = line.charAt(i);
            switch (state) {
                case IN_STRING:
                    switch (c) {
                        case '"':
                            state = State.NORMAL;
                            break;
                        case '\\':
                            state = State.BACKSLASH;
                            break;
                    }
                    break;
                case BACKSLASH:
                    state = State.IN_STRING;
                    break;
                case COMMENT:
                    if (c == '/') {
                        return line.substring(i + 1);
                    }
                    state = State.NORMAL;
                    // falls through
                case NORMAL:
                    if (c == '"') {
                        state = State.IN_STRING;
                    }
                    else if (c == '/') {
                        state = State.COMMENT;
                    }
                    break;
            }
        }

        return null;
    }

    public static boolean isStartMultiLineComment(String line) {
        return line.trim().startsWith("/*"); // $NON-NLS$
    }

    public static boolean isEndMultiLineComment(String line) {
        return line.trim().endsWith("*/"); // $NON-NLS$
    }

    public static boolean isPackageStatement(String line) {
        return line.trim().startsWith("package "); // $NON-NLS$
    }

    public static boolean isImportStatement(String line) {
        return line.trim().startsWith("import "); // $NON-NLS$
    }

    public static boolean isIrrelevantLine(String line) {
        return !StringUtils.hasText(line) || isOneLineComment(line) || isAnnotationLine(line)
                || isPackageStatement(line) || isImportStatement(line);
    }

    /**
     * Finds all literal string matches on a java code line without comment.
     *
     * @param line java code line without comment
     * @return a list of literal string matches found, empty if none found.
     */
    public static List<LiteralStringMatch> getLiteralStrings(String line) {
        final Matcher matcher = PATTERN_LITERAL_STRING.matcher(line);
        final List<LiteralStringMatch> ret = new ArrayList<LiteralStringMatch>(line.length());

        while (matcher.find()) {
            final LiteralStringMatch ls = new LiteralStringMatch(matcher.group(1),
                    matcher.start(), matcher.end());
            ret.add(ls);
        }

        return ret;
    }

    public static boolean containsNonNLS(String comment) {
        if (!StringUtils.hasText(comment)) {
            return false;
        }
        final Matcher matcher = PATTERN_NON_NLS.matcher(comment);
        return matcher.matches();
    }

    public static List<Integer> getNonNLSIndexes(String comment) {
        if (!StringUtils.hasText(comment)) {
            return Collections.emptyList();
        }
        final Matcher matcher = PATTERN_NON_NLS_INDEX.matcher(comment);
        final List<Integer> ret = new ArrayList<Integer>(comment.length());

        while (matcher.find()) {
            ret.add(Integer.parseInt(matcher.group(1)));
        }

        return ret;
    }

    public static String getPackageName(String path, String pkgStartStr) {
        final String replacedStr = path.replace(SystemUtils.FILE_SEPARATOR, POINT);
        if (org.apache.commons.lang.StringUtils.isNotBlank(pkgStartStr)) {
            final int idx = replacedStr.indexOf(POINT + pkgStartStr + POINT);
            if (idx != -1) {
                return replacedStr.substring(idx + 1);
            }
        }
        return replacedStr.substring(replacedStr.indexOf(POINT) + 1);
    }

    public static String getQualifiedName(File file, String pkgStartStr) {
        final String fileName = file.getName();
        return getPackageName(file.getParentFile(), pkgStartStr) + POINT
                + fileName.substring(0, fileName.lastIndexOf(POINT));
    }

    public static String getPackageName(File file, String pkgStartStr) {
        return getPackageName(file.getAbsolutePath(), pkgStartStr);
    }

    public static boolean isIgnored(String line) {
        for (Pattern pattern : PATTERNS_IGNORE) {
            if (pattern.matcher(line).matches()) {
                return true;
            }
        }
        return false;
    }

    public static boolean isIgnored(List<LiteralStringMatch> lsMatches) {
        for (LiteralStringMatch lsMatch : lsMatches) {
            for (Pattern pattern : PATTERNS_LS_IGNORE) {
                if (!pattern.matcher(lsMatch.getTheMatch()).matches()) {
                    return false;
                }
            }
        }
        return true;
    }
}
