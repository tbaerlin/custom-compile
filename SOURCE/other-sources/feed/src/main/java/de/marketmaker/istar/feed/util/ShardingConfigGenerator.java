package de.marketmaker.istar.feed.util;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;

import de.marketmaker.istar.common.util.NumberUtil;

public class ShardingConfigGenerator {

    /** The following markets will be available on every instance */
    private static final Set<String> FIXED_MARKETS = ImmutableSet.of("VWD", "VWD2", "VWD3", "VWD4", "VWD6", "DPH");

    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage(args);
            System.exit(1);
        }

        int clusterMemberCount = Integer.parseInt(args[0]);
        String fileEnding = args[1];
        String[] folderNames = Arrays.copyOfRange(args, 2, args.length);

        List<MarketAndAverage> marketSizeAverages = collectStatistics(folderNames, fileEnding);
        marketSizeAverages.forEach(System.out::println);

        List<Shard> shards = new ArrayList<>();
        for (int i = 0; i < clusterMemberCount; i++) {
            shards.add(new Shard());
        }

        for (MarketAndAverage maa : marketSizeAverages) {
            shards.stream()
                    .min(Comparator.comparingDouble(Shard::getSum))
                    .ifPresent(min -> min.addMarket(maa));
        }

        // Sort by size ascending
        shards.sort(Comparator.comparingDouble(Shard::getSum));

        // Generate fallback config
        Shard smallest = shards.remove(0);
        smallest.markets.clear();
        smallest.fallback = true;
        shards.stream()
                .map(s -> s.markets)
                .forEach(smallest.markets::addAll);

        System.out.println(NumberUtil.humanReadableByteCount((long) smallest.getSum()) + " by excluding " + smallest.getMarketCount() + " markets:");
        System.out.println("sharding.markets=" + smallest.getVendorkeyFilterSpec());
        shards.forEach(s -> {
            System.out.println(NumberUtil.humanReadableByteCount((long) s.getSum()) + " from " + s.getMarketCount() + " markets:");
            System.out.println("sharding.markets=" + s.getVendorkeyFilterSpec());
        });

    }

    private static List<MarketAndAverage> collectStatistics(String[] folderNames, String fileEnding) {
        return Stream.of(folderNames)
                .map(File::new)
                .filter(File::isDirectory)
                .flatMap(f -> {
                    File[] tdzFiles = f.listFiles((dir, name) ->
                            (name.endsWith(fileEnding) || name.endsWith(".dph"))
                            && !(name.startsWith("OPRA-") || name.startsWith("XCBO-")));
                    return tdzFiles != null ? Stream.of(tdzFiles) : Stream.empty();
                })
                .map(f -> {
                    String[] split = f.getName().replace(fileEnding, "").replace(".dph", "").split("-");
                    return new MarketAndSize(
                            split[0],
                            f.length(),
                            LocalDate.parse(split[1], DateTimeFormatter.BASIC_ISO_DATE)
                    );
                })
                .collect(Collectors.groupingBy(m -> m.market, Collectors.summarizingLong(m -> m.size)))
                .entrySet().stream()
                .sorted(Comparator
                        .<Map.Entry<String, LongSummaryStatistics>>comparingDouble(o -> o.getValue().getAverage())
                        .reversed()
                )
                .map(e -> new MarketAndAverage(e.getKey(), e.getValue().getAverage()))
                .collect(Collectors.toList());
    }

    private static void printUsage(String[] args) {
        System.out.println("Usage: ShardingConfigGenerator <clusterMemberCount> <file-ending> <historical-data-dir>...");
        System.out.println("    clusterMemberCount: Count of chicago instances to split all markets between");
        System.out.println("    filename extension: e.g. .td3, .tdz, etc. Note that this MUST have the dot in front of the extension");
        System.out.println("    historical-data-dir: folder containing files with the above file-ending");
    }

    private static class MarketAndSize {
        final String market;
        final long size;
        final LocalDate date;

        private MarketAndSize(String market, long size, LocalDate date) {
            this.market = market;
            this.size = size;
            this.date = date;
        }
    }

    private static class MarketAndAverage {
        final String market;
        final double average;

        private MarketAndAverage(String market, double average) {
            this.market = market;
            this.average = average;
        }

        @Override
        public String toString() {
            return String.format("%-6s: %7s", market, NumberUtil.humanReadableByteCount((long)average));
        }
    }

    private static class Shard {
        final List<String> markets = new ArrayList<>();
        final DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        boolean fallback;

        public void addMarket(MarketAndAverage marketAndAverage) {
            this.markets.add(marketAndAverage.market);
            this.stats.accept(marketAndAverage.average);
        }

        public int getMarketCount() {
            return markets.size();
        }

        public double getSum() {
            return this.stats.getSum();
        }

        public String getVendorkeyFilterSpec() {
            StringJoiner stringJoiner = new StringJoiner(",", "^m:", "$");
            this.markets.stream()
                .filter(m -> !FIXED_MARKETS.contains(m))
                .forEach(stringJoiner::add);
            if (!this.fallback) {
                FIXED_MARKETS.forEach(stringJoiner::add);
            }
            String vendorkeyFilterSpec = stringJoiner.toString();
            return this.fallback ? "!(" + vendorkeyFilterSpec + ")"  : vendorkeyFilterSpec;
        }
    }
}