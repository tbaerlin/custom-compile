/*
 * I18nChecker.java
 *
 * Created on 16.08.2010 09:37:02
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.tools.i18n;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * @author zzhao
 */
public class I18nChecker implements JavaFile.JavaLineProcessor {

    private final List<Problem> problems;

    public I18nChecker() {
        this.problems = new ArrayList<Problem>();
    }

    public List<Problem> getProblems() {
        return Collections.unmodifiableList(this.problems);
    }

    public int getProblemCount() {
        return this.problems.size();
    }

    public String processLine(File file, int idx, String theLine) {
        final String packageName = I18nUtil.getQualifiedName(file, "de"); // $NON-NLS$
        final String fileName = file.getName();
        final String comment = I18nUtil.getLineCommentAfterCode(theLine);
        final String justCode = StringUtils.removeEnd(theLine, comment);
        final List<LiteralStringMatch> lsMatches = I18nUtil.getLiteralStrings(justCode);
        final boolean withNonNLS = I18nUtil.containsNonNLS(comment);
        final List<Integer> indexes = I18nUtil.getNonNLSIndexes(comment);

        // 1. no string literals, no non-nls comment: OK
        if (CollectionUtils.isEmpty(lsMatches) && !withNonNLS && CollectionUtils.isEmpty(indexes)) {
            return theLine;
        }
        // 2. no string literals, with non-nls or non-nls-n comment
        if (CollectionUtils.isEmpty(lsMatches) && (withNonNLS || !CollectionUtils.isEmpty(indexes))) {
            this.problems.add(new Problem(Problem.Kind.NON_NLS_Comment_Unnecessary,
                    packageName, fileName, idx + 1));
        }
        else if (!CollectionUtils.isEmpty(lsMatches)) {
            // 3. without non-nls or non-nls-n comment
            if (!withNonNLS && CollectionUtils.isEmpty(indexes)) {
                if (!I18nUtil.isIgnored(theLine) && !I18nUtil.isIgnored(lsMatches)) {
                    this.problems.add(new Problem(Problem.Kind.NON_NLS_Comment_Required,
                            packageName, fileName, idx + 1));
                }
            }
            else {
                // 4. with non-nls comments: OK
                if (withNonNLS) {
                    return theLine;
                }
                else {
                    // 5. without non-nls, with non-nls-n
                    if (indexesMatched(lsMatches, indexes)) {
                        return theLine;
                    }
                    else {
                        this.problems.add(new Problem(Problem.Kind.NON_NLS_Comment_Not_Match,
                                packageName, fileName, idx + 1));
                    }
                }
            }
        }

        return theLine;
    }

    private boolean indexesMatched(List<LiteralStringMatch> lsMatches, List<Integer> indexes) {
        final List<Integer> indexesExpected = new ArrayList<Integer>(lsMatches.size());
        for (int i = 0; i < lsMatches.size(); i++) {
            indexesExpected.add(i);
        }

        if (indexesExpected.size() != indexes.size()) {
            return false;
        }

        for (Integer i : indexesExpected) {
            if (!indexes.contains(i)) {
                return false;
            }
        }

        return true;
    }

    public static void main(String[] args) throws Exception {
        final File f = new File("D:\\dev\\iview\\trunk\\mmgwt\\src\\main\\java\\de\\marketmaker\\iview\\mmgwt\\mmweb\\client\\util\\DOMUtil.java"); // $NON-NLS$
        JavaFile jf = JavaFile.from(f);

        final I18nChecker checker = new I18nChecker();
        jf.processReadOnly(checker);

        for (Problem p : checker.problems) {
            System.out.println(p);
        }
    }

    public static class Problem {
        public enum Kind {
            NON_NLS_Comment_Unnecessary,
            NON_NLS_Comment_Required,
            NON_NLS_Comment_Not_Match
        }

        private final Kind kind;

        private final String qualifiedName;

        private final String fileName;

        private final int lineNumber;

        public Problem(Kind kind, String qualifiedName, String fileName,
                int lineNumber) {
            this.kind = kind;
            this.qualifiedName = qualifiedName;
            this.fileName = fileName;
            this.lineNumber = lineNumber;
        }

        public Kind getKind() {
            return kind;
        }

        public String getQualifiedName() {
            return qualifiedName;
        }

        public String getFileName() {
            return fileName;
        }

        public int getLineNumber() {
            return lineNumber;
        }

        @Override
        public String toString() {
            return this.qualifiedName + "(" + this.fileName + ":" + this.lineNumber + ") -> " + this.kind; // $NON-NLS$
        }
    }
}
