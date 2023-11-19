package de.marketmaker.istar.feed.history;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.IoUtils;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

/**
 * Purges history tick file for specific date or date range for specific symbols a/o markets
 *
 * <p>
 * Consider extension with input file which describes all candidates to be purged and purge
 * them in one go.
 * </p>
 * <p>
 * Purge result will have the same file name and put into the given purge result folder.
 * </p>
 * @author zzhao
 */
public class HistoryTickFilePurger {

    public static void main(String[] args) throws Exception {
        if (args.length != 4) {
            usage();
            System.exit(1);
        }
        final TimeTaker tt = new TimeTaker();

        final Path histFilePath = Paths.get(args[0]);
        if (!Files.exists(histFilePath)) {
            System.out.println("file: " + args[0] + " not found");
            System.exit(2);
        }

        final File histFile = histFilePath.toFile();
        final HistoryUnit histUnit = HistoryUnit.fromExt(histFile);
        final int fromDate = histUnit.getFromDate(histFile);
        final int toDate = histUnit.getToDate(histFile);

        final Path purgeResultDirPath = Paths.get(args[1]);
        final File purgeResultFile = histUnit.createFile(HistoryUnit.getContentType(histFile),
                purgeResultDirPath.toFile(), fromDate, toDate);
        if (Files.exists(purgeResultFile.toPath())) {
            System.out.println("take care of " + purgeResultFile.getPath() + " first");
            System.exit(3);
        }

        final Purger purger = new Purger(args[2], fromDate, args[3], toDate);
        if (!purger.isPurgeNecessary()) {
            System.out.println("purge not necessary for given parameters: " + tt);
            System.exit(0);
        }

        DataFile histDatFile = null;
        HistoryWriter<ByteString> histWriter = null;
        try {
            System.out.println("purging " + args[0]);
            histDatFile = new DataFile(histFile, true);
            final ItemExtractor<ByteString> itemExtractor = new ItemExtractor<>(ByteString.class, histDatFile);
            final OffsetLengthCoder olCoder = itemExtractor.getOffsetLengthCoder();
            histWriter = new HistoryWriter<>(purgeResultFile, olCoder, ByteString.class);

            final BufferedBytesTransporter bufferedTransporter =
                    new BufferedBytesTransporter(histDatFile, olCoder.maxLength());
            final ByteArrayTarget buffer = new ByteArrayTarget();
            for (Item<ByteString> item : itemExtractor) {
                bufferedTransporter.transferTo(item.getOffset(), item.getLength(), buffer);
                if (purger.isCandidate(item.getKey())) {
                    histWriter.withEntry(item.getKey(), purger.purge(item.getKey(), buffer.data()));
                }
                else {
                    histWriter.withEntry(item.getKey(), buffer.data());
                }
            }
        } finally {
            IoUtils.close(histDatFile);
            IoUtils.close(histWriter);
        }

        System.out.println("purged into " + args[1] + " in: " + tt);
    }

    private static void usage() {
        System.out.println("HistoryTickFilePurger {file to be purged}" +
                " {purge result dir} {date(s)} {symbols a/o markets}");
        System.out.println("- date in format yyyyMMdd, multiple dates separated with ','");
        System.out.println("- multiple symbols and markets separated with ','");
    }

    private static class Purger {
        // values in the sense of days from genesis
        private Map<Integer, Integer> dayByYmd;

        // symbols categorized by markets. If symbols is empty, the whole market is to be pured
        // otherwise only those symbols within that market are to be purged
        private Map<ByteString, Set<ByteString>> symbolsByMarket;

        private Purger(String dates, int fromDate, String symbolsAndMarkets, int toDate)
                throws Exception {
            final TickHistoryContext ctx = TickHistoryContextImpl.fromEnv();
            this.dayByYmd = Arrays.stream(dates.split(","))
                    .map(Integer::parseInt)
                    .filter(ymd -> ymd >= fromDate && ymd <= toDate)
                    .collect(Collectors.toMap(
                            ymd -> HistoryUtil.daysFromBegin(ctx.getGenesis(), DateUtil.yyyyMmDdToLocalDate(ymd)),
                            Function.identity()));
            final String[] split = symbolsAndMarkets.split(",");
            this.symbolsByMarket = new TreeMap<>();
            for (String str : split) {
                if (!str.contains(".")) { // symbol
                    this.symbolsByMarket.put(new ByteString(str), Collections.emptySet());
                }
                else {
                    final ByteString key = getKey(str);
                    final ByteString market = getMarket(key);
                    this.symbolsByMarket.computeIfAbsent(market, k -> new HashSet<>()).add(key);
                }
            }
        }

        boolean isPurgeNecessary() {
            return !this.dayByYmd.isEmpty() && !this.symbolsByMarket.isEmpty();
        }

        static ByteString getMarket(ByteString key) {
            return key.substring(0, key.indexOf('.'));
        }

        static ByteString getKey(String arg) {
            final ByteString symbol = new ByteString(arg);
            if (VendorkeyVwd.isKeyWithTypePrefix(symbol)) {
                return HistoryUtil.removeTypeAndGetKey(symbol);
            }
            else {
                return HistoryUtil.getKey(symbol);
            }
        }

        boolean isCandidate(ByteString symbol) {
            final Set<ByteString> keys = this.symbolsByMarket.get(getMarket(symbol));
            return keys != null && (keys.isEmpty() || keys.contains(symbol));
        }

        byte[] purge(ByteString key, byte[] data) {
            if (null == data || data.length == 0) {
                return data;
            }

            final ByteBuffer bb = ByteBuffer.allocate(data.length);
            final MutableEntryIterator<MutableTickEntry> histTickEntryIt =
                    new MutableEntryIterator<>(ByteBuffer.wrap(data), MutableTickEntry.class);
            for (MutableTickEntry histTickEntry : histTickEntryIt) {
                if (this.dayByYmd.containsKey(histTickEntry.getDays())) {
                    System.out.println(key + " " + this.dayByYmd.get(histTickEntry.getDays()) + " purged");
                    continue;
                }
                EntryFactory.toBuffer(bb, histTickEntry);
            }

            return Arrays.copyOfRange(bb.array(), 0, bb.position());
        }
    }
}
