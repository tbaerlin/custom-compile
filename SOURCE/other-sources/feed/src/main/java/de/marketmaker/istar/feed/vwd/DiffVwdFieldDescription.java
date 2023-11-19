/*
 * DiffVwdFieldDescription.java
 *
 * Created on 21.03.2006 12:44:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Show difference between VwdFieldDescription and VwdFieldDescriptionOld
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class DiffVwdFieldDescription {
    private static String formatFlags(final int flags) {
        StringBuilder sb = new StringBuilder(4);
        if ((flags & VwdFieldDescription.FLAG_DYNAMIC) != 0) sb.append("D");
        if ((flags & VwdFieldDescription.FLAG_STATIC) != 0) sb.append("S");
        if ((flags & VwdFieldDescription.FLAG_RATIO) != 0) sb.append("R");
        if ((flags & VwdFieldDescription.FLAG_NEWS) != 0) sb.append("N");
        return sb.length() > 0 ? sb.toString() : "-";
    }

    private static String formatType(VwdFieldDescription.Field f) {
        final String s = f.mdpsType().toString().toLowerCase();
        return (s.endsWith("string"))
                ? (s + "(" + f.length() + ")")
                : s;
    }

    public static void main(String[] args) throws Exception {
        int max = Math.max(VwdFieldDescription.length(), VwdFieldDescriptionOld.length());
        int numDiffs = 0;
        for (int i = 0; i < max; i++) {
            VwdFieldDescription.Field f = VwdFieldDescription.getFieldIds().get(i) ? VwdFieldDescription.getField(i) : null;
            VwdFieldDescription.Field o = VwdFieldDescriptionOld.getFieldIds().get(i) ? VwdFieldDescriptionOld.getField(i) : null;
            if (f == null) {
                if (o != null) {
                    System.out.printf("%4d: %s was REMOVED%n", ++numDiffs, o);
                }
                continue;
            }
            if (o == null) {
                System.out.printf("%4d: %s is NEW  %s%n", ++numDiffs, f, formatFlags(f.flags()));
                continue;
            }
            if (equal(f, o)) {
                continue;
            }

            List<String> diffs = new ArrayList<>();

            if (!f.name().equals(o.name())) {
                diffs.add("name was '" + o.name() + "'");
            }

            if (f.length() != o.length()) {
                if (f.mdpsType() == VwdFieldDescription.MdpsType.FLSTRING) {
                    diffs.add("length is " + f.length() + " was " + o.length() + " !!!!! CRITICAL !!!!!!");
                }
                else {
                    diffs.add("length is " + f.length() + " was " + o.length());
                }
            }

            if (f.type() != o.type()) {
                diffs.add("type is " + f.type() + " was " + o.type());
            }

            if (f.mdpsType() != o.mdpsType()) {
                diffs.add("mdpsType is " + f.mdpsType() + " was " + o.mdpsType());
            }

            if (f.flags() != o.flags()) {
                diffs.add("flags is " + formatFlags(f.flags()) + "(" + f.flags()
                        + ") was " + formatFlags(o.flags()) + "(" + o.flags() + ")");
            }

            if (!diffs.isEmpty()) {
                System.out.printf("%4d: %s %s %s  CHANGED: %s%n",
                        ++numDiffs, f, formatType(f), formatFlags(f.flags()), diffs);
            }
        }
    }

    private static boolean hasFlag(VwdFieldDescription.Field f, int flag) {
        return (f.flags() & flag) != 0;
    }

    private static boolean equal(VwdFieldDescription.Field f1, VwdFieldDescription.Field f2) {
        return f1.length() == f2.length()
                && f1.name().equals(f2.name())
                && f1.type() == f2.type()
                && f1.mdpsType() == f2.mdpsType()
                && (f1.flags() & 0xFE) == (f2.flags() & 0xFE);
    }
}
