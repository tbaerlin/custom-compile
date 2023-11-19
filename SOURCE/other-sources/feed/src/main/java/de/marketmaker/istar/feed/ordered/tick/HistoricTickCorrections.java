/*
 * HistoricTickCorrections.java
 *
 * Created on 04.08.14 09:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.ordered.tick;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.springframework.jmx.export.annotation.ManagedOperation;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.vwd.VendorkeyVwd;

import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_TICK_CORRECTION_DELETE;
import static de.marketmaker.istar.feed.FeedUpdateFlags.FLAG_TICK_CORRECTION_INSERT;
import static java.nio.file.StandardOpenOption.*;
import static java.util.stream.Collectors.toList;

/**
 * Can be used to apply corrections from a file to symbols in tick files for a given day.
 * Appends data to <tt>corrections-yyyyMMdd.td[xz]</tt> as described for
 * {@link de.marketmaker.istar.feed.ordered.tick.TickDirectory}.
 * <p>
 * Adding corrections will never modify any data previously written: data will always be appended.
 * @author oflege
 */
@ManagedResource
public class HistoricTickCorrections {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<File> baseDirectories;

    private FileTickStore store;

    public void setStore(FileTickStore store) {
        this.store = store;
    }

    public void setBaseDirectory(String[] baseDirectory) {
        this.baseDirectories = new LinkedHashSet<>();
        for (String s : baseDirectory) {
            this.baseDirectories.add(new File(s));
        }
    }

    public void setBaseDirectories(Set<File> baseDirectories) {
        this.baseDirectories = baseDirectories;
    }

    @ManagedOperation
    public void applyCorrection(int day, String filename) throws IOException {
        applyCorrection(day, new File(filename));
    }

    public void applyCorrection(int day, File f) throws IOException {
        if (!f.canRead()) {
            throw new IllegalArgumentException("Cannot read '" + f.getAbsolutePath() + "'");
        }

        DateTime now = new DateTime();

        Map<String, TickCorrections> map = parseCorrections(f);

        File dir = findDirectory(Integer.toString(day));
        this.logger.info("<applyCorrection> using " + dir.getAbsolutePath());

        File patch = cpInputToPatchesDir(f, dir);

        TickDirectory td = TickDirectory.open(dir);

        List<String> indexEntries = new ArrayList<>();
        StringBuilder sb = new StringBuilder(80)
                .append(ISODateTimeFormat.dateHourMinuteSecond().print(now)).append(';');
        int sbPos = sb.length();

        File cf = td.getCorrectionsFile();
        try (RandomAccessFile raf = new RandomAccessFile(cf, "rw");
             FileChannel ch = raf.getChannel()) {
            ch.position(cf.length());
            TickDeflater deflater = new TickDeflater(ch, ch.position());

            for (Map.Entry<String, TickCorrections> e : map.entrySet()) {
                sb.setLength(sbPos);
                sb.append(e.getKey());

                byte[] bytes = td.readTicks(store, e.getKey());
                if (bytes == null) {
                    this.logger.warn("<applyCorrection> no ticks for " + e.getKey());
                    continue;
                }

                deflater.reset();
                e.getValue().applyTo(new TickDecompressor(bytes, td.getEncoding()), deflater);
                long fileAddress = deflater.getFileAddress();
                int numTickBytes = deflater.getNumTickBytes();

                sb.append(';').append(Long.toHexString(fileAddress ^ TickWriter.FILE_ADDRESS_FLAG))
                        .append(';').append(Integer.toHexString(numTickBytes))
                        .append(';').append(patch.getName());
                indexEntries.add(sb.toString());
                this.logger.info("<applyCorrection> " + sb.substring(sbPos));
            }
        }

        if (!indexEntries.isEmpty()) {
            Files.write(td.getCorrectionsIndexFile().toPath(), indexEntries, StandardCharsets.UTF_8,
                    CREATE, WRITE, APPEND);
        }
    }

    private File cpInputToPatchesDir(File f, File dir) {
        File patches = new File(dir, "patches");
        if (!patches.isDirectory() && !patches.mkdirs()) {
            return f;
        }
        File dst = new File(patches, f.getName());
        if (dst.equals(f)) {
            return f;
        }
        int i = 0;
        while (dst.exists()) {
            dst = new File(patches, f.getName() + "-" + ++i);
        }
        try {
            Files.copy(f.toPath(), dst.toPath());
            return dst;
        } catch (IOException e) {
            this.logger.error("<cpInputToPatchesDir> failed for " + f.getAbsolutePath(), e);
            return f;
        }
    }


    private File findDirectory(String yyyymmdd) {
        for (File baseDir : this.baseDirectories) {
            final File d = new File(baseDir, yyyymmdd);
            if (d.isDirectory()) {
                return d;
            }
        }
        throw new IllegalArgumentException("No directory for day " + yyyymmdd);
    }

    private Map<String, TickCorrections> parseCorrections(File f) throws IOException {
        List<String> allLines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
        return parseTickCorrections(allLines);
    }

    Map<String, TickCorrections> parseTickCorrections(List<String> allLines) {
        HashMap<String, TickCorrections> result = new HashMap<>();

        TickCli.LineParser parser = new TickCli.LineParser();
        String vwdcode = null;
        List<TickCorrections.Correction> corrections = new ArrayList<>();

        List<String> lines = allLines.stream().filter(StringUtils::hasText).collect(toList());

        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.startsWith("#")) {
                if (!corrections.isEmpty()) {
                    result.put(vwdcode, new TickCorrections(corrections));
                    corrections = new ArrayList<>();
                }
                vwdcode = line.substring(1).trim();
                if (!VendorkeyVwd.KEY_PATTERN.matcher(vwdcode).matches()) {
                    continue;
                }
                VendorkeyVwd vkey = VendorkeyVwd.getInstance("1." + vwdcode);
                if (vkey == VendorkeyVwd.ERROR) {
                    throw new IllegalArgumentException("Illegal vendorkey '" + vwdcode + "'");
                }
            }
            else {
                if (vwdcode == null) {
                    continue;
                }
                parser.parse(line);
                if (parser.matchCount > Byte.MAX_VALUE || parser.matchCount < Byte.MIN_VALUE) {
                    throw new IllegalStateException(parser.matchCount
                            + " not in [-128..127] for '" + line + "'");
                }
                if ((parser.flags & FLAG_TICK_CORRECTION_INSERT) != 0) {
                    throw new IllegalStateException("corrections cannot start with insert '" + line + "'");
                }
                int flags = parser.flags;
                int matchCount = parser.matchCount;
                byte[] reference = parser.builder.asArray();

                int numInserts = 0;
                while (i + 1 + numInserts < lines.size() && lines.get(i + 1 + numInserts).startsWith("+")) {
                    numInserts++;
                }
                if (numInserts == 0 && (parser.flags & FLAG_TICK_CORRECTION_DELETE) == 0) {
                    this.logger.warn("<read> void correction: no delete and no insert(s) '" + line + "'");
                    continue;
                }
                byte[][] inserts = new byte[numInserts][];
                for (int j = 0; j < numInserts; j++) {
                    i++;
                    parser.parse(lines.get(i));
                    inserts[j] = asInsert(parser);
                }

                corrections.add(new TickCorrections.Correction(matchCount, flags,
                        reference, inserts));
            }
        }
        if (vwdcode != null) {
            result.put(vwdcode, new TickCorrections(corrections));
        }

        return result;
    }

    private byte[] asInsert(TickCli.LineParser parser) {
        byte[] bytes = parser.builder.asArray();
        byte[] result = new byte[1 + bytes.length];
        result[0] = (byte) parser.flags;
        System.arraycopy(bytes, 0, result, 1, bytes.length);
        return result;
    }

    public static void main(String[] args) throws IOException {
        HistoricTickCorrections htc = new HistoricTickCorrections();
        htc.setBaseDirectories(Collections.singleton(new File("/Users/oflege/produktion/var/data/chicago")));
        FileTickStore fts = new FileTickStore();
        htc.setStore(fts);
        htc.applyCorrection(20140801, "/Users/oflege/tmp/corr.txt");
    }
}
