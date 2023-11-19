/*
 * HistoryReader.java
 *
 * Created on 20.08.12 14:42
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.Closeable;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public class HistoryDataCorrector implements Closeable {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HistoryUnit unit;

    private File file;

    private DataFile dataFile;

    private ItemExtractor<ByteString> extractor;

    public HistoryDataCorrector(HistoryUnit unit) {
        this.unit = unit;
    }

    public void setFile(File file) throws IOException {
        final TimeTaker tt = new TimeTaker();
        if (null == file) {
            close();
        }
        else if (!file.equals(this.file)) {
            close();
            this.logger.info("<setFile> {} {}", this.unit, file.getAbsolutePath());
            this.file = file;
            this.dataFile = new DataFile(this.file, true);
            if (this.dataFile.size() <= 0) {
                this.logger.warn("<setFile> empty history file: " + this.file.getAbsolutePath());
                return;
            }
            this.extractor = new ItemExtractor<>(ByteString.class, this.dataFile);
        }
        this.logger.info("<setFile> took: {}", tt);
    }

    @Override
    public void close() throws IOException {
        if (null != this.file) {
            this.logger.info("<close> {} {}", this.unit, this.file.getAbsolutePath());
            this.dataFile.close();
            this.dataFile = null;
            this.file = null;
        }
    }

    public static void main(String[] args) throws IOException {
        if (args.length < 4) {
            System.err.println("Usage: genesis history_dir repair_dir output");
            System.exit(1);
        }

        LocalDate genesis = HistoryUtil.DTF_DAY.parseLocalDate(args[0]);
        final File dir = new File(args[1]);
        final File repairDir = new File(args[2]);
        final boolean output = Boolean.parseBoolean(args[3]);

        if (dir.isFile()) {
            repairFile(genesis, repairDir, dir, output);
        }
        else {
            final File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    try {
                        HistoryUnit.fromExt(pathname);
                        return true;
                    } catch (IllegalArgumentException e) {
                        return false;
                    }
                }
            });
            if (null == files) {
                System.exit(0);
            }

            for (File file : files) {
                repairFile(genesis, repairDir, file, output);
            }
        }
    }

    private static void repairFile(LocalDate genesis, File repairDir, File file, boolean output)
            throws IOException {
        final File repairFile = new File(repairDir, file.getName() + ".cr");
        if (!repairFile.exists()) {
            return;
        }
        final HistoryUnit unit = HistoryUnit.fromExt(file);
        final TimeTaker tt = new TimeTaker();
        final List<ByteString> itemsToCheck;
        try (
                final HistoryDataCorrector repairData = new HistoryDataCorrector(unit)
        ) {
            repairData.setFile(repairFile);
            final List<Item<ByteString>> items = new ArrayList<>();
            repairData.extractor.iterator().forEachRemaining(items::add);
            verifyOrder(repairFile, items);

            final DataFile rdf = repairData.dataFile;
            try (final DataFile df = new DataFile(file, false)) {
                rdf.seek(0);
                final int blocks = rdf.readInt();
                for (int i = 0; i < blocks; i++) {
                    final long offset = rdf.readLong();
                    final ByteBuffer bb = ByteBuffer.allocate(rdf.readInt());
                    rdf.read(bb);
                    bb.flip();
                    System.out.println(offset + ";" + bb.limit() + ";" + bb.array().length);
                    df.seek(offset);
                    df.write(bb);
                }

                final long to = repairData.extractor.getIndexOffsetStart();
                final ByteBuffer itemsBuf = ByteBuffer.allocate((int) (to - rdf.position()));
                rdf.read(itemsBuf);
                itemsBuf.flip();
                final int count = itemsBuf.getInt();
                itemsToCheck = new ArrayList<>(count);
                for (int i = 0; i < count; i++) {
                    itemsToCheck.add(ByteString.readFrom(itemsBuf, ByteString.LENGTH_ENCODING_BYTE));
                }

                final long indexOffsetStart = ItemExtractor.indexOffsetStart(df);
                df.seek(indexOffsetStart);
                final OneLevelBsTree<ByteString> bsTree = new OneLevelBsTree<>(ByteString.class);
                for (Item<ByteString> item : items) {
                    bsTree.addItem(item);
                }
                bsTree.finish(df, df.size());
            }
        }

        try (final HistoryReader<ByteString> reader = new HistoryReader<>(
                ByteString.class, unit, false)) {
            reader.setFile(file);
            final TickType tickType = TickType.valueOf(HistoryUnit.getContentType(file));

            final ByteBuffer bb = ByteBuffer.allocate(8 * 1024 * 1024);
            for (ByteString key : itemsToCheck) {
                bb.clear();
                reader.loadData(key, bb);
                bb.flip();
                if (output) {
                    System.out.println(key + "#" + tickType);
                }
                TickHistoryReaderCli.emitTickHistory(genesis, tickType, false, bb, output);
            }
        } finally {
            System.out.println("repair: " + file.getName() + " took: " + tt);
        }
    }

    private static void verifyOrder(File repairFile, List<Item<ByteString>> items) {
        for (int i = 0; i < items.size(); i++) {
            if (i > 0) {
                final Item<ByteString> pre = items.get(i - 1);
                final Item<ByteString> cur = items.get(i);
                if (cur.compareTo(pre) <= 0) {
                    throw new IllegalStateException("incorrect repair file: "
                            + repairFile.getAbsolutePath());
                }
            }
        }
    }
}