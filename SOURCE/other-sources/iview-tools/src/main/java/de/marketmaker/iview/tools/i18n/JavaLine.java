/*
 * JavaLine.java
 *
 * Created on 16.08.2010 11:24:08
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.tools.i18n;

/**
 * @author zzhao
 */
public class JavaLine {

    private final String line;

    private JavaLine(String line) {
        this.line = line;
    }

    public static JavaLine from(String line) {
        final JavaLine ret = new JavaLine(line);
        ret.init();

        return ret;
    }

    private void init() {
        throw new UnsupportedOperationException("not implemented"); // $NON-NLS-0$
    }
}
