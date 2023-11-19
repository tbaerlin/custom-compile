/*
 * MdpsTypeConverter.java
 *
 * Created on 10.11.2008 13:41:39
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.mdps;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Utility to convert types in DelayRules and EntitlementRules to numeric type.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MdpsTypeConverter {
    // iff true, rules for types EF and C will be duplicated as 1 and 18, or 8 and 17 respectively
    // can be removed as soon as the old type mappings are no longer used
    private static final boolean ISTAR_435_HACK = Boolean.getBoolean("istar.435.hack");

    private int tokensPerLine = 6;

    private File infile;

    private String inCharset = System.getProperty("file.encoding");

    private File out;

    private String outCharset = System.getProperty("file.encoding");

    private boolean verbose;

    private boolean killComments;

    private int minNumRules = 0;

    public MdpsTypeConverter(String[] args) {
        int i = 0;
        while (i < (args.length) && args[i].startsWith("-")) {
            if ("-t".equals(args[i])) {
                this.tokensPerLine = Integer.parseInt(args[++i]);
            }
            if ("-m".equals(args[i])) {
                this.minNumRules = Integer.parseInt(args[++i]);
            }
            else if ("-i".equals(args[i])) {
                this.infile = new File(args[++i]);
            }
            else if ("-ic".equals(args[i])) {
                this.inCharset = args[++i];
            }
            else if ("-o".equals(args[i])) {
                this.out = new File(args[++i]);
            }
            else if ("-oc".equals(args[i])) {
                this.outCharset = args[++i];
            }
            else if ("-kc".equals(args[i])) {
                this.killComments = true;
            }
            else if ("-v".equals(args[i])) {
                this.verbose = true;
            }
            i++;
        }
        if (this.infile == null) {
            throw new IllegalStateException("input file undefined");
        }
        if (this.verbose) {
            System.err.println("infile : " + this.infile.getAbsolutePath());
            System.err.println("in csn : " + this.inCharset);
            System.err.println("outfile: "
                    + ((this.out != null) ? this.out.getAbsolutePath() : "System.out"));
            System.err.println("out csn: " + this.outCharset);
            if (this.minNumRules > 0) {
                System.err.println("require at least " + this.minNumRules + " rules in infile");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        try {
            new MdpsTypeConverter(args).convert();
        } catch (IllegalStateException e) {
            System.err.println("Usage: MdpsTypeConverter [options]");
            System.err.println(" Options:");
            System.err.println(" -t  <num>       : required tokens per line");
            System.err.println(" -m  <num>       : minimum number of rules expected in input file");
            System.err.println(" -i  <inputfile> : name of input file");
            System.err.println(" -ic <csn>       : charset name for input file");
            System.err.println(" -o  <outputfile>: name of output file");
            System.err.println(" -oc <csn>       : charset name for output file");
            System.err.println(" -kc             : kill comments");
            System.err.println(" -v              : verbose");
            System.exit(2);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
    }

    private void convert() throws Exception {
        final List<String[]> lines = new ArrayList<>();
        final Scanner s = new Scanner(this.infile, this.inCharset);
        int n = 0;
        int numRules = 0;
        while (s.hasNextLine()) {
            final String line = s.nextLine();
            n++;
            if (line.startsWith("#") || "".equals(line.trim())) {
                if (!this.killComments) lines.add(new String[]{line});
                continue;
            }
            final String[] tokens = line.trim().split("\\s+");
            if (!isValidLine(tokens)) {
                throw new IllegalArgumentException("line " + n + ": '" + line + "'");
            }
            if (this.tokensPerLine == 0) {
                this.tokensPerLine = tokens.length;
            }
            String mdpsSecType = tokens[2];

            tokens[2] = MdpsTypeMappings.toNumericType(mdpsSecType);
            if (tokens[2] == null) {
                throw new IllegalArgumentException("unknown type in line " + n + ": '" + line + "'");
            }
            lines.add(tokens);
            if (ISTAR_435_HACK) {
                if ("EF".equals(mdpsSecType)) {
                    lines.add(cloneWithType(tokens, "18".equals(tokens[2]) ? "1" : "18"));
                }
                else if ("C".equals(mdpsSecType)) {
                    lines.add(cloneWithType(tokens, "17".equals(tokens[2]) ? "8" : "17"));
                }
            }
            numRules++;
        }
        s.close();

        if (this.minNumRules > 0 && this.minNumRules > numRules) {
            throw new IllegalArgumentException("converted only " + numRules
                    + " rules, required " + this.minNumRules);
        }

        if (this.verbose) {
            System.err.println("converted " + numRules + " rules");
        }

        writeResult(lines);
    }

    private String[] cloneWithType(String[] tokens, final String type) {
        String[] result = Arrays.copyOf(tokens, tokens.length);
        result[2] = type;
        return result;
    }

    private void writeResult(List<String[]> lines) throws Exception {
        try (final PrintWriter pw = getPrintWriter()) {
            final String fmt = getFormatString(lines);
            for (String[] line : lines) {
                final String lineFmt = (line.length == this.tokensPerLine) ? fmt : "%s%n";
                pw.printf(lineFmt, line);
            }
        }
    }

    private PrintWriter getPrintWriter() throws IOException {
        if (this.out != null) {
            return new PrintWriter(this.out, this.outCharset);
        }
        return new PrintWriter(System.out);
    }

    private boolean isValidLine(String[] tokens) {
        if (this.tokensPerLine > 0) {
            return (tokens.length == this.tokensPerLine);
        }
        return (tokens.length > 2);
    }

    private String getFormatString(List<String[]> lines) {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < this.tokensPerLine; i++) {
            result.append("%-").append(getMaxWidth(lines, i)).append("s ");
        }
        return result.append("%n").toString();
    }

    private int getMaxWidth(List<String[]> lines, int i) {
        int n = 0;
        for (String[] line : lines) {
            if (line.length == this.tokensPerLine) {
                n = Math.max(n, line[i].length());
            }
        }
        return n + 2;
    }
}
