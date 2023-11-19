/*
 * Internationalizer.java
 *
 * Created on 20.07.2010 15:04:12
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.itools.i18n;

import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import org.apache.commons.lang.math.NumberUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author zzhao
 */
public class GWTAppInternationalizer {

    private static final BufferedReader STD_IN = new BufferedReader(new InputStreamReader(System.in));

    private static final int ADDITIONAL_LINES_TO_DISPLAY = 1;

    private static final Pattern LITERAL_STRING = Pattern.compile("\"(([^\"]*?(\\\\\")*)+)\"");

    /**
     * The group 1 contains the literal string without double quote
     * if the regex is changed, this group number has to be checked
     * if what theMatch holds is still what we want here
     */
    private static final int GROUP_NUM_4_LITERAL_STR_WO_QUOTE = 1;

    private static final Pattern[] STRINGS_TO_IGNORE = {
            Pattern.compile("[0-9]+(%|px)"),
            Pattern.compile("((<[^>]+>)*[^\\p{javaLetter}]*(\\\\n)*)*(<[^>]+){0,1}"),
            Pattern.compile("P[0-9]+[DWMY]")
    };

    private static final String[] KEY_WORDS = {
            "query", "symbol", "vwdSymbol", "@", "'", "/", "true", "false", "pdf", "PDF", "csv", "html",
            "url", "name", "id", "type", "date", "wkn", "isin", "&nbsp;", "-", "--", "title", "WKN",
            "ISIN", "(", ")", "{", "}", "[", "]", "%", ",", "size", "phone", "email", "\"",
            "xls", "doc", "\\n", "\\n\\n", "children", "root", "::", ":", ".", ">", "<", "</",
            "width", "height", "+/-", "+/- %", "|"
    };

    private static final String[] SPECIAL_WORDS = {
            "_", "==", "="
    };

    private static final String[] SPECIAL_START_WORDS = {
            "mm", "http://", "x-"
    };

    private static final String[] SPECIAL_END_WORDS = {
            ".html", ".pdf", ".gif", ".png", ".jsp"
    };

    private static final String[] SPECIAL_LINE_START_WORDS = {
            "throw new", "DebugUtil.logTo"
    };

    private static final String[] SPECIAL_STATEMENT_WORDS = {
            ".setParameter", ".addBlock", ".addStyleName(", ".getStyle().setProperty(",
            ".setStyleName(", ".removeStyleName("
    };

    private static final FileFilter JAVA_FILE_FILTER = new FileFilter() {
        public boolean accept(File pathname) {
            final String fn = pathname.getName();
            return pathname.isFile() && fn.endsWith(".java") && !fn.startsWith("I18n");
        }
    };

    private static I18nMessagesGen gen = new I18nMessagesGen();

    public static void main(String[] args) throws IOException {
        final File file;
        if (args.length == 0) {
//        String an = "D:\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client\\customer";
//        String an = "D:\\mike\\prog\\iint";
//        String an = "D:\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client\\snippets\\fund";
//        String an = "D:\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client\\snippets\\stock\\estimates";
            String an = "D:\\dev\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client\\NewsDetailController.java";
            file = new File(an);
        }
        else {
            file = new File(args[0]);
        }
        if (!file.canRead()) {
            throw new IllegalArgumentException("a valid dir or file required");
        }
        System.out.println("# start with: " + file.getAbsolutePath());

        if (!file.isDirectory()) {
            processFile(file.getParentFile(), file, 1, 1);
        }
        else {
            processDir(file);
        }

//        gen.generateArtifacts("de");
        gen.finishArtifacts();
    }

    private static void processDir(File file) throws IOException {
        final String dirName = file.getName();
        System.out.format(">Process dir '%s' (y/n)", dirName);
        String decision = STD_IN.readLine();
        if ("y".equalsIgnoreCase(decision.trim())) {
            System.out.println("# start with: " + dirName);
        }
        else {
            System.out.println("# file: " + dirName + " bypassed");
            return;
        }


        final File[] files = file.listFiles(JAVA_FILE_FILTER);
        Arrays.sort(files, new FileComparator());
        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
            processFile(file, files[i], i + 1, filesLength);
        }
    }

    static class FileComparator implements Comparator<File> {
        public int compare(File f1, File f2) {
            return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
        }
    }

    private static void processFile(File parentDir, File file, int fileIdx, int fileCounts) throws IOException {
        final Lines lines = new Lines(file);
        if (lines.getLines().isEmpty()) {
            System.out.println("# no lines available in: " + file.getAbsolutePath());
        }
        else {
            final String fn = file.getName();
            System.out.format(">Process [%d/%d] '%s' (y/n)", fileIdx, fileCounts, fn);
            String decision = STD_IN.readLine();
            if ("y".equalsIgnoreCase(decision.trim())) {
                System.out.println("# start with: " + fn);
                processFileLines(parentDir, lines);
                gen.addFileToGenerate(parentDir, lines);
                gen.generateArtifacts("de", lines);
            }
            else {
                System.out.println("# file: " + fn + " bypassed");
            }
        }

    }

    private static void processFileLines(File parentDir, Lines lines) throws IOException {
        final List<Line> theLines = lines.getLines();
        for (int i = 0; i < theLines.size(); i++) {
            final Line line = theLines.get(i);
            final String theLine = line.getTheLine();

            if (isAnnotationLine(theLine) || isOneLineComment(theLine)) {
                continue;
            }

            final Matcher matcher = LITERAL_STRING.matcher(theLine);
            boolean found = false;
            while (matcher.find()) {
                final String theMatch = matcher.group(GROUP_NUM_4_LITERAL_STR_WO_QUOTE);
                line.addLiteralStringMatch(new LiteralStringMatch(removeHtmlUmlauts(theMatch), matcher.start(), matcher.end()));
                found = true;
            }
            if (isRelevantLine(theLine) && found) {
                processOneMatchLine(parentDir, lines, i);
            }
        }
    }

    private static String removeHtmlUmlauts(String s) {
        return s.replace("&auml;", "ä")
                .replace("&ouml;", "ö")
                .replace("&uuml;", "ü")
                .replace("&Auml;", "A")
                .replace("&Ouml;", "Ö")
                .replace("&Uuml;", "Ü")
                .replace("&szlig;", "ß");
    }

    private static void processOneMatchLine(File parentDir, Lines lines, int lineIndex) throws IOException {
        boolean relevantPartDisplayed = false;

        final List<Line> theLines = lines.getLines();
        final Line theLine = theLines.get(lineIndex);

        for (LiteralStringMatch match : theLine.getMatches()) {
            if (!isRelevantMatch(match.getTheMatch())) {
                continue;
            }

            if (!relevantPartDisplayed) {
                displayRelevantPart(lineIndex, lines);
                relevantPartDisplayed = true;
            }

            List<String> keys = promptGetRecommendation(lineIndex, theLine, match);
            final String key = readKeyFromUser(parentDir, match, keys);
            if (StringUtility.hasText(key)) {
                match.setPropertyKey(key);
            }
        }
    }

    private static String readKeyFromUser(File parentDir, LiteralStringMatch match, List<String> keys) throws IOException {
        String key = null;
        String message = "# Replace? (yes: [m:]key / no: enter) >";
        do {
            System.out.format(message);
            String cmd = STD_IN.readLine().trim();
            if (cmd.startsWith("m:")) {
                match.setIsMessage(true);
                cmd = cmd.substring(2);
            }

            if (cmd.isEmpty()) {
                if (StringUtility.hasText(key)) {
                    gen.registerHistoricValue(parentDir, key);
                }
                return key;
            }

            final int recIdx = getNumber(cmd) - 1;  // subtract by one, since keys range from 1 to keys.size()
            if (recIdx >= keys.size()) {
                System.out.println("-> invalid choice, must be between 1 and " + keys.size());
            }
            else if (recIdx >= 0) {
                // one of the recommendations was chosen
                key = keys.get(recIdx);
                gen.registerHistoricValue(parentDir, key);
                return key;
            }
            else {
                // new key was entered
                final String value = gen.getProperty(parentDir, cmd);
                if (value == null) {
                    // key does not yet exist
                    gen.registerProperty(parentDir, cmd, match.getTheMatch());
                    return cmd;
                }
                else {
                    // key already exists in repository
                    key = cmd;
                    message = "# Reuse '" + key + "=" + value + "'? (yes: enter / no: another key) >";
                }
            }

        } while (true);
    }

    private static int getNumber(String s) {
        try {
            return Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            return -1;
        }
    }

    private static List<String> promptGetRecommendation(int lineIndex, Line line, LiteralStringMatch match) {
        displayReplacePart(lineIndex, line, match);
        final List<KeyValueWithDistance> recs = gen.getRecommendation(match.getTheMatch());
        List<String> keys = new ArrayList<String>();

        if (!recs.isEmpty()) {
            System.out.format("# Recommendation(s):%n");
            System.out.println("--------------------------------------------------------");
            int idx = 0;
            for (KeyValueWithDistance kvwd : recs) {
                keys.add(kvwd.getKey());
                System.out.format("%4d) %s = %s%n", ++idx, kvwd.getKey(), kvwd.getValue());
            }
            System.out.println("--------------------------------------------------------");
        }
        return keys;
    }

    private static boolean isAnnotationLine(String line) {
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

    private static boolean isOneLineComment(String line) {
        return line.trim().startsWith("//");
    }

    private static boolean isRelevantMatch(String theMatchParam) {
        String theMatch = theMatchParam.trim();

        if (!StringUtility.hasText(theMatch)) {
            return false;
        }

        if (org.apache.commons.lang.StringUtils.isNumericSpace(theMatch)
                || NumberUtils.isNumber(theMatch)) {
            return false;
        }

        for (int i = 0; i < KEY_WORDS.length; i++) {
            if (KEY_WORDS[i].equals(theMatch)) {
                return false;
            }
        }

        for (int i = 0; i < SPECIAL_START_WORDS.length; i++) {
            if (theMatch.startsWith(SPECIAL_START_WORDS[i])) {
                return false;
            }
        }

        for (int i = 0; i < SPECIAL_END_WORDS.length; i++) {
            if (theMatch.endsWith(SPECIAL_END_WORDS[i])) {
                return false;
            }
        }

        for (int i = 0; i < SPECIAL_WORDS.length; i++) {
            if (theMatch.contains(SPECIAL_WORDS[i])) {
                return false;
            }
        }

        if (Character.isLowerCase(theMatch.charAt(0)) && theMatch.indexOf(" ") == -1
                && theMatch.indexOf("-") == -1 && isMixedCases(theMatch)) {
            return false;
        }

        if (isToIgnore(theMatch)) {
            return false;
        }

        return true;
    }

    static boolean isToIgnore(String str) {
        for (Pattern pattern : STRINGS_TO_IGNORE) {
            if (pattern.matcher(str).matches()) {
                return true;
            }
        }

        return false;
    }

    private static boolean isMixedCases(String theMatch) {
        String lowerCase = theMatch.toLowerCase();
        String upperCase = theMatch.toUpperCase();

        return !theMatch.equals(lowerCase) && !theMatch.equals(upperCase);
    }

    private static boolean isRelevantLine(String line) {
        for (int i = 0; i < SPECIAL_LINE_START_WORDS.length; i++) {
            if (line.trim().startsWith(SPECIAL_LINE_START_WORDS[i])) {
                return false;
            }
        }

        for (int i = 0; i < SPECIAL_STATEMENT_WORDS.length; i++) {
            if (line.indexOf(SPECIAL_STATEMENT_WORDS[i]) != -1) {
                return false;
            }
        }

        return true;
    }

    private static void displayReplacePart(int idx, Line line, LiteralStringMatch match) {
        final String theLine = line.getTheLine();
        System.out.format("%4d|%s%n", idx + 1, theLine);
        System.out.print("     ");
        for (int i = 0; i < theLine.length(); i++) {
            if (i >= match.getStartIndex() && i < match.getEndIndex()) {
                System.out.print("~");
            }
            else {
                System.out.print(" ");
            }
        }
        System.out.println();
    }

    private static void displayRelevantPart(int idx, Lines lines) {
        final List<Line> theLines = lines.getLines();
        Integer[] indexes = getIndexesToDisplay(idx, theLines.size());

        final int length = Math.max(80, getMaxLineLength(theLines, indexes) + 8);
        final String fn = lines.getFile().getName();

        System.out.println(org.apache.commons.lang.StringUtils.center("> " + fn + " <", length, "="));
        for (int i = 0; i < indexes.length; i++) {
            System.out.format("%4d|%s%n", indexes[i] + 1, theLines.get(indexes[i]).getTheLine());
        }
        System.out.println(org.apache.commons.lang.StringUtils.center("", length, "="));
    }

    private static int getMaxLineLength(List<Line> theLines, Integer[] indexes) {
        int ret = 0;
        for (int i = 0; i < indexes.length; i++) {
            ret = Math.max(ret, theLines.get(indexes[i]).getTheLine().length());
        }

        return ret;
    }

    private static Integer[] getIndexesToDisplay(int idx, int size) {
        List<Integer> ret = new ArrayList<Integer>(Math.min(2 * ADDITIONAL_LINES_TO_DISPLAY + 1, size));

        for (int i = idx - ADDITIONAL_LINES_TO_DISPLAY; i < idx; i++) {
            if (i >= 0 && i < size) {
                ret.add(i);
            }
        }

        for (int i = idx; i < idx + ADDITIONAL_LINES_TO_DISPLAY + 1; i++) {
            if (i >= 0 && i < size) {
                ret.add(i);
            }
        }

        return ret.toArray(new Integer[ret.size()]);
    }

    private static int resolveRecIdx(String key, List<String> keys) {
        try {
            final int idx = Integer.parseInt(key);
            return (idx > 0 && idx <= keys.size()) ? (idx - 1) : -2; // idx ranges from 1 to keys.size()
        }
        catch (NumberFormatException e) {
            return findKey(key, keys);
        }
    }

    private static int findKey(String key, List<String> keys) {
        for (int i = 0; i < keys.size(); i++) {
            if (key.equalsIgnoreCase(keys.get(i))) {
                return i;
            }
        }
        return -1;
    }
}