/*
 * OpraDiff.java
 *
 * Created on 15.04.14 14:50
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.opra;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import org.springframework.util.StringUtils;

/**
 * Compares two opra files.
 * @author oflege
 */
public class OpraDiff {
    private static class ValuationPrice {
        final String price;
        final int date;

        private ValuationPrice(String price, int date) {
            if (!price.endsWith("0") || !price.contains(".")) {
                this.price = price;
            }
            else {
                int n = price.length();
                while (n > 0 && price.charAt(n - 1) == '0') {
                    n--;
                }
                this.price = price.substring(0, n);
            }
            this.date = date;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            ValuationPrice that = (ValuationPrice) o;

            if (date != that.date) return false;
            if (!price.equals(that.price)) return false;

            return true;
        }

        @Override
        public String toString() {
            return this.price + "/" + this.date;
        }

        @Override
        public int hashCode() {
            int result = price.hashCode();
            result = 31 * result + date;
            return result;
        }
    }

    private static final ValuationPrice NULL = new ValuationPrice("0", 0);

    private final Map<String, ValuationPrice> m = new HashMap<>(1 << 23);

    public OpraDiff(File f1, File f2) throws IOException {
        try (Scanner sc = new Scanner(new GZIPInputStream(new FileInputStream(f1)))) {
            sc.nextLine();
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(";");

                if (tokens.length > 3 && !"0".equals(tokens[3])) {
                    m.put(tokens[0], new ValuationPrice(tokens[2], Integer.parseInt(tokens[3])));
                }
                else {
                    m.put(tokens[0], NULL);
                }
            }
        }
        System.out.println("finished reading " + f1.getAbsolutePath() + " " + m.size());

        int numNotFound = 0;
        int numDiff = 0;
        int numEqual = 0;
        int numSamePrice = 0;

        try (Scanner sc = new Scanner(new GZIPInputStream(new FileInputStream(f2)))) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] tokens = line.split(";");

                ValuationPrice vp = m.remove(tokens[7]);
                if (vp == null) {
                    System.err.printf("not found in f1(%d): %s%n", ++numNotFound, tokens[7]);
                }
                else {
                    ValuationPrice vp2 = getValuationPrice(tokens);
                    if (vp.equals(vp2)) {
                        numEqual++;
                    }
                    else if (vp.price.equals(vp2.price)) {
                        numSamePrice++;
                    }
                    else if (vp2 != NULL) {
//                        System.out.printf("%s %s %s %n", tokens[7], vp, vp2);
                        numDiff++;
                    }
                }
            }
        }

        System.out.println(m.size());
        System.out.println(numEqual + numSamePrice);
        System.out.println(numDiff);
    }

    private ValuationPrice getValuationPrice(String[] tokens) {
        for (int i = 8; i < 13; i += 2) {
            if (hasSettlement(tokens, i)) {
                return new ValuationPrice(tokens[i], asDate(tokens[i + 1]));
            }
        }
        return NULL;
    }

    private int asDate(String s) {
        return Integer.parseInt(s.substring(6, 10)) * 10000
                + Integer.parseInt(s.substring(3, 5)) * 100
                + Integer.parseInt(s.substring(0, 2))
                ;
    }

    private boolean hasSettlement(String[] s, int i) {
        return s.length > (i + 1)
                && StringUtils.hasText(s[i])
                && !"0.0".equals(s[i])
                && StringUtils.hasText(s[i + 1]) && !"0".equals(s[i + 1]);
    }


    public static void main(String[] args) throws IOException {
        // args[0]: Name of file created by OpraWriter (opra-yyyy...)
        // args[1]: Name of file downloaded by tools (opra-static...)
        new OpraDiff(new File(args[0]), new File(args[1]));
    }
}
