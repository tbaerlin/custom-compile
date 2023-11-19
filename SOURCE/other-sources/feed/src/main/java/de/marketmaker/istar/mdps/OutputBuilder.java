/*
 * OutputBuilder.java
 *
 * Created on 28.06.2010 15:15:35
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.mdps;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.management.ManagementFactory;
import java.util.Arrays;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author oflege
 */
class OutputBuilder {
    protected final DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    private final StringWriter sw = new StringWriter(8192);

    private final PrintWriter pw = new PrintWriter(sw);

    OutputBuilder() {
    }

    OutputBuilder printHeader(int mdpsPid) {
        line(20).print(" ").print(MdpsMain.getMdpsName())
                .print(" [Version ").print(System.getProperty("istar.version", "1.0")).print("] ")
                .line(10).println().println()
                .print("MDPS Process ID: ").println(mdpsPid)
                .print("Start Time: ").print(DTF.print(ManagementFactory.getRuntimeMXBean().getStartTime())).print("     ")
                .print("Current Time: ").println(DTF.print(new DateTime())).println();
        return this;
    }

    OutputBuilder line(int s) {
        print('-', s);
        return this;
    }

    OutputBuilder print(DateTime dt) {
        print(DTF.print(dt));
        return this;
    }

    OutputBuilder print(char c, int s) {
        final char[] chars = new char[s];
        Arrays.fill(chars, c);
        print(new String(chars));
        return this;
    }

    OutputBuilder printf(String fmt, Object... args) {
        this.pw.printf(fmt, args);
        return this;
    }

    OutputBuilder print(Object s) {
        this.pw.print(String.valueOf(s));
        return this;
    }

    OutputBuilder println(Object s) {
        this.pw.println(String.valueOf(s));
        return this;
    }

    OutputBuilder println() {
        this.pw.println();
        return this;
    }

    String build() {
        return sw.toString();
    }
}
