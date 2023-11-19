/*
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.common.util;

import static de.marketmaker.istar.common.util.EntitlementsVwd.MAX_ENTITLEMENT_VALUE;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Utility class for {@code resource-profiles.properties}.
 *
 * @author Stefan Willenbrock
 */
public class ResourceProfilesSorter {

    class SelectorRange {

        private int start;
        private int end;

        public SelectorRange(int i) {
            this.start = i;
            this.end = i;
        }

        public SelectorRange merge(SelectorRange next) {
            this.end = next.end;
            return this;
        }

        @Override
        public String toString() {
            return this.end - this.start < 1 ? EntitlementsVwd.toEntitlement(this.start) : String.format("%s-%s", EntitlementsVwd.toEntitlement(this.start), EntitlementsVwd.toEntitlement(this.end));
        }
    }

    public void sort(Path input, Path output) throws IOException {
        try (final BufferedWriter bw = Files.newBufferedWriter(output, StandardCharsets.UTF_8)) {
            final Scanner s = new Scanner(input);
            while (s.hasNextLine()) {
                final String line = s.nextLine().trim();
                final int index = line.indexOf('=');
                if (index > -1) {
                    String key = line.substring(0, index);
                    if (isSelectorProperty(key) && index < line.length() - 1) {
                        String[] selectors = line.substring(index + 1, line.length()).split(",");
                        bw.write(String.format("%s=%s", key, join(sortSelectorsMerge(selectors)).stream().collect(Collectors.joining(","))));
                        bw.newLine();
                        continue;
                    }
                }
                bw.write(line);
                bw.newLine();
            }
        }
    }

    private boolean isSelectorProperty(String key) {
        return key.endsWith(".PRICES_REALTIME") || key.endsWith(".PRICES_DELAY") || key.endsWith(".PRICES_EOD") || key.endsWith(".NEWS_REALTIME");
    }

    private SelectorRange[] sortSelectorsMerge(String[] selectors) {
        return Arrays.stream(selectors).flatMapToInt(s -> expandRanges(s)).sorted().distinct().mapToObj(SelectorRange::new).toArray(SelectorRange[]::new);
    }

    private IntStream expandRanges(String selectorOrRange) {
        final int index = selectorOrRange.indexOf('-');
        if (index > -1) {
            final int from = Integer.valueOf(EntitlementsVwd.toNumericSelector(selectorOrRange.substring(0, index)));
            final int to = Integer.valueOf(EntitlementsVwd.toNumericSelector(selectorOrRange.substring(index + 1, selectorOrRange.length())));
            return IntStream.range(from, to + 1);
        } else {
            return IntStream.of(Integer.valueOf(EntitlementsVwd.toNumericSelector(selectorOrRange.trim())));
        }
    }

    private List<String> join(SelectorRange[] ranges) {
        final List<SelectorRange> result = new ArrayList<>();
        SelectorRange current = ranges[0];

        for (int i = 1; i < ranges.length; i++) {
            if (ranges[i].start - current.end < 2 && ranges[i].end <= MAX_ENTITLEMENT_VALUE) {
                current.end = ranges[i].end;
            } else {
                result.add(current);
                current = ranges[i];
            }
        }
        result.add(current);

        return result.stream().map(r -> r.toString()).collect(Collectors.toList());
    }

    public static void main(String[] args) throws IOException {
        final Path inputPath = Paths.get(System.getProperty("user.dir"), "domain/src/main/java/de/marketmaker/istar/domain/resources/resource-profiles.properties");
        final Path outputPath = inputPath.getParent().resolve("resource-profiles-new.properties");
        new ResourceProfilesSorter().sort(inputPath, outputPath);
    }
}