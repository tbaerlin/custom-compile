/*
 * NLSClearer.java
 *
 * Created on 16.08.2010 09:36:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.tools.i18n;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.TimeTaker;

/**
 * @author zzhao
 */
public class NLSCleaner {

    /**
     * group 1: NON-NLS
     * group 2: index
     */
    private static final Pattern PATTERN_LS_COMMENT = Pattern.compile(
            "\\$(NON-)?NLS-(\\d+)\\$" // $NON-NLS$
    );

    private static final Pattern PATTERN_NON_NLS_WITHOUT_ENDING = Pattern.compile(
            "\\$NON-NLS-(\\d+)(\\s|$)"  // $NON-NLS$
    );

    private static final NLSLineCleaner NLS_LINE_CLEANER = new NLSLineCleaner();

    private static final NonNLSCorrector NON_NLS_CORRECTOR = new NonNLSCorrector();

    public static void main(String[] args) throws Exception {
        final TimeTaker tt = new TimeTaker();
        final String fromDir =
                "D:\\dev\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client"; // $NON-NLS$
        final File from = new File(fromDir);
        cleanNLS(from);
        System.out.println("NLS cleaned, took: " + tt); // $NON-NLS$
    }

    private static void cleanNLS(File from) throws IOException {
        if (from.isDirectory()) {
            final File[] files = from.listFiles(I18nUtil.INSTANCE_NON_I18N_JAVA_FILE);
            if (null != files) {
                for (final File file : files) {
                    cleanNLSInJavaFile(file);
                }
            }

            final File[] dirs = from.listFiles(I18nUtil.INSTANCE_DIR_JAVA_SOURCE);
            if (null != dirs) {
                for (final File dir : dirs) {
                    cleanNLS(dir);
                }
            }
        }
        else {
            cleanNLSInJavaFile(from);
        }
    }

    private static void cleanNLSInJavaFile(File file) throws IOException {
        JavaFile jf = JavaFile.from(file);
        jf.process(NON_NLS_CORRECTOR);
//        jf.process(NLS_LINE_CLEANER);
        jf.applyProcessResults();
    }

    private static class NLSLineCleaner implements JavaFile.JavaLineProcessor {

        public String processLine(File file, int idx, String theLine) {
            final String comment = I18nUtil.getNLSOneLineComment(theLine);
            if (!StringUtils.hasText(comment)) {
                return theLine;
            }

            final Matcher matcher = PATTERN_LS_COMMENT.matcher(theLine);
            int nlsFound = 0;

            final StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                String nonNLSPrefix = matcher.group(1);
                if (null == nonNLSPrefix) {
                    // NLS
                    ++nlsFound;
                }
                else {
                    // NON-NLS
                    sb.append(" ").append("$NON-NLS-") // $NON-NLS$
                            .append(Integer.parseInt(matcher.group(2)) - nlsFound)
                            .append("$"); // $NON-NLS$
                }
            }

            String ret;
            final String nonComment = theLine.substring(0, theLine.length() - comment.length());
            if (sb.length() == 0) {
                ret = nonComment;
            }
            else {
                ret = nonComment + " //" + sb.toString();   // $NON-NLS$
            }

            return ret;
        }
    }

    private static class NonNLSCorrector implements JavaFile.JavaLineProcessor {
        public String processLine(File file, int idx, String theLine) {
            final String comment = I18nUtil.getLineCommentAfterCode(theLine);
            if (!StringUtils.hasText(comment)) {
                return theLine;
            }

            final Matcher matcher = PATTERN_NON_NLS_WITHOUT_ENDING.matcher(comment);
            final StringBuilder sb = new StringBuilder();

            while (matcher.find()) {
                sb.append(" $NON-NLS-").append(matcher.group(1)).append("$"); // $NON-NLS$
            }

            if (sb.length() == 0) {
                return theLine;
            }

            final String nonComment = theLine.substring(0, theLine.length() - comment.length());
            final String ret = nonComment + sb.toString();

            return ret;
        }
    }
}
