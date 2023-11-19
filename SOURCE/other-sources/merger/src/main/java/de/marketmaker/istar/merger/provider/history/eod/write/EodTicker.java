/*
 * EodTicker.java
 *
 * Created on 14.03.13 13:27
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.history.eod.write;

import java.io.Closeable;
import java.io.File;
import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.IOFileFilter;

/**
 * @author zzhao
 */
public interface EodTicker extends Iterator<EodTick>, Closeable {
    public static enum Type {
        EOD_I, EOD_E, EOD_A, EOD_C, EOD_P, EOD_S;

        public static Type fromName(String name) {
            if (name.toUpperCase().startsWith("EOD_I")) {
                return EOD_I;
            }
            if (name.toUpperCase().startsWith("EOD_A")) {
                return EOD_A;
            }
            if (name.toUpperCase().startsWith("EOD_C")) {
                return EOD_C;
            }
            if (name.toUpperCase().startsWith("EOD_S")) {
                return EOD_S;
            }
            if (name.toUpperCase().startsWith("EOD_E")) {
                return EOD_E;
            }
            if (name.toUpperCase().startsWith("EOD_P")) {
                return EOD_P;
            }
            throw new IllegalArgumentException("no EOD type found: " + name);
        }

        public static final IOFileFilter FILTER = FileFilterUtils.makeFileOnly(
                FileFilterUtils.prefixFileFilter("EOD_", IOCase.INSENSITIVE));

        private static final Matcher FM = Pattern.compile("(EOD_[IACSEP])_([0-9_]+).*").matcher("");

        public static final Comparator<File> COMPARATOR = new Comparator<File>() {
            @Override
            public int compare(File fa, File fb) {
                boolean flag = FM.reset(fa.getName().toUpperCase()).find();
                if (!flag) {
                    throw new IllegalStateException("not valid EoD history file: " + fa.getAbsolutePath());
                }
                final String fa1 = FM.group(1);
                final String fa2 = FM.group(2);

                flag = FM.reset(fb.getName().toUpperCase()).find();
                if (!flag) {
                    throw new IllegalStateException("not valid EoD history file: " + fb.getAbsolutePath());
                }
                final String fb1 = FM.group(1);
                final String fb2 = FM.group(2);

                if (fa1.equals(fb1)) {
                    return fa2.compareTo(fb2);
                }
                else {
                    return EodTickerProtobuf.Type.fromName(fa1).compareTo(
                            EodTickerProtobuf.Type.fromName(fb1));
                }
            }
        };
    }
}
