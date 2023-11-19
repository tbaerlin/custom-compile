/*
 * PlainTextSupport.java
 *
 * Created on 11.04.14 14:59
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Reads a file with text extracted from a pdf document, tries its best to join words
 * that had been split in two parts due to formatting.
 *
 * @author oflege
 */
public class PlainTextSupport {
    static String getText(final File f) throws IOException {
        try (Scanner s = new Scanner(f, StandardCharsets.UTF_8.name())) {
            return joinSplitWords(s, (int) f.length() + 128);
        }
    }

    public static String joinSplitWords(Scanner s, int stringBuilderSize) {
        StringBuilder sb = new StringBuilder(stringBuilderSize);
        boolean previousLineEndsWithDash = false;
        while (s.hasNextLine()) {
            String line = s.nextLine().trim();
/*
            if ("IMPRESSUM".equals(line)) {
                return sb.toString();
            }
*/
            if (previousLineEndsWithDash) {
                if (!startWithNewWord(line)) {
                    sb.setLength(sb.length() - 1);
                }
                else {
                    sb.append('\n');
                }
            }
            else if (sb.length() > 0) {
                sb.append('\n');
            }
            sb.append(line);
            previousLineEndsWithDash = line.endsWith("-");
        }
        return sb.toString();
    }

    private static boolean startWithNewWord(String line) {
        return line.startsWith("und") || line.startsWith("oder") || line.startsWith("/");
    }
}
