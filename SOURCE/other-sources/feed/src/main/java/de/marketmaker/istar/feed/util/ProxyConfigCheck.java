package de.marketmaker.istar.feed.util;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import de.marketmaker.istar.feed.VendorkeyFilter;
import de.marketmaker.istar.feed.VendorkeyFilterFactory;

public class ProxyConfigCheck {
    public static void main(String[] args) throws IOException {

        Map<Boolean, Map<ConfigFile, List<String>>> configsByFallback = readConfigFiles(args);
        System.out.println(configsByFallback);


        Map<ConfigFile, List<String>> nonFallbacks = configsByFallback.get(Boolean.FALSE);

    }

    private static Map<Boolean, Map<ConfigFile, List<String>>> readConfigFiles(String[] filenames) throws IOException {
        Map<ConfigFile, List<String>> configFiles = new HashMap<>();

        for (String filename : filenames) {
            Properties p = new Properties();
            p.load(new FileReader(new File(filename)));

            ConfigFile c = new ConfigFile();
            c.fallback = Boolean.parseBoolean(p.getProperty("sharding.fallback", "false"));

            String vendorkeyFilterSpec = p.getProperty("vendorkeyFilter.spec", "");
            extractMarketFilter(VendorkeyFilterFactory.create(vendorkeyFilterSpec), c);

            configFiles
                    .computeIfAbsent(c, b -> new ArrayList<>())
                    .add(filename);
        }

        return configFiles.entrySet()
                .stream()
                .collect(
                        Collectors.groupingBy(
                                e -> e.getKey().fallback,
                                Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                        )
                );
    }

    private static void extractMarketFilter(VendorkeyFilter f, ConfigFile c) {
        if (f instanceof VendorkeyFilterFactory.NotFilter) {
            c.not = !c.not;
            extractMarketFilter(((VendorkeyFilterFactory.NotFilter) f).getFilter(), c);
        } else if (f instanceof VendorkeyFilterFactory.MarketSetFilter) {
            c.markets = ((VendorkeyFilterFactory.MarketSetFilter) f).toSet();
        } else if (f instanceof VendorkeyFilterFactory.MarketFilter) {
            c.markets = Collections.singleton(((VendorkeyFilterFactory.MarketFilter) f).getMarket());
        } else if (f == VendorkeyFilterFactory.ACCEPT_ALL) {
            c.markets = Collections.emptySet();
            c.fallback = true;
        } else if (f == VendorkeyFilterFactory.ACCEPT_NONE) {
            c.markets = Collections.emptySet();
        } else {
            System.err.println("Only market filter and negations are allowed for sharding. Found: " + f.getClass() + " - " + f.toString());
        }
    }

    private static class ConfigFile {
        boolean fallback;
        boolean not;
        Set<String> markets;

        @Override
        public String toString() {
            return "ConfigFile{" +
                    "fallback=" + fallback +
                    ", not=" + not +
                    ", markets=" + markets +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ConfigFile that = (ConfigFile) o;
            return fallback == that.fallback &&
                    not == that.not &&
                    Objects.equals(markets, that.markets);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fallback, not, markets);
        }
    }
}
