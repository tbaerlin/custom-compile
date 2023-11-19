/*
 * ResultComparer.java
 *
 * Created on 03.08.2010 15:04:06
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ResultComparer {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormat.forPattern("dd/MM/yy");

    private File bewAltFile;

    private File unknownFile;

    private Map<String, LocalDate> lastDateBySymbol = new HashMap<>();

    private Set<String> noResultSymbols = new HashSet<>();

    public void setBewAltFile(File bewAltFile) {
        this.bewAltFile = bewAltFile;
    }

    public void setUnknownFile(File unknownFile) {
        this.unknownFile = unknownFile;
    }

    public void compare() throws Exception {
        readBewAlt();

        int countNoResult = 0;
        int countOldResult = 0;
        int countOther = 0;

        final LocalDate cmp = new LocalDate(2010,7,29);

        final Scanner scanner = new Scanner(this.unknownFile);
        scanner.nextLine(); // consume header
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] tokens = line.split("[; ]");
            final String symbol = tokens[0] + ";" + tokens[1];

            final LocalDate date = this.lastDateBySymbol.get(symbol);

            if (this.noResultSymbols.contains(symbol)) {
                countNoResult++;
            }
            else if (date != null && date.isBefore(cmp)) {
                countOldResult++;
            }
            else {
                System.out.println(line);
                countOther++;
            }
        }
        scanner.close();

        System.out.println("countNoResult = " + countNoResult);
        System.out.println("countOldResult = " + countOldResult);
        System.out.println("countOther = " + countOther);
    }

    private void readBewAlt() throws FileNotFoundException {
        final Scanner scanner = new Scanner(this.bewAltFile, "ISO-8859-1");
        scanner.nextLine(); // consume header
        while (scanner.hasNextLine()) {
            final String line = scanner.nextLine();
            final String[] tokens = line.split(Pattern.quote(";"));
            final String symbol = tokens[0] + ";" + tokens[1];
            if (tokens.length < 10 || "".equals(tokens[12])) {
                this.noResultSymbols.add(symbol);
                continue;
            }
            final LocalDate date = DATE_FMT.parseDateTime(tokens[12]).toLocalDate();
            this.lastDateBySymbol.put(symbol, date);
        }
        scanner.close();
    }

    public static void main(String[] args) throws Exception {
        final ResultComparer rc = new ResultComparer();
        rc.setBewAltFile(new File("d:/temp/bew/current/pcs/FID20100803.prx"));
        rc.setUnknownFile(new File("d:/temp/bew/current/pcs/messages-console.txt"));
        rc.compare();
    }
}
