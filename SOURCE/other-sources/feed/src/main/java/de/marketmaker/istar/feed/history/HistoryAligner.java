/*
 * HistoryAligner.java
 *
 * Created on 27.06.2014 10:15
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.history;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Iterator;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.io.DataFile;
import de.marketmaker.istar.common.io.OffsetLengthCoder;
import de.marketmaker.istar.common.util.ByteString;
import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.data.TickType;

/**
 * @author zzhao
 */
public class HistoryAligner {

    public static void main(String[] args) throws IOException {
        if (args.length != 3) {
            System.err.println("Usage: {genesis} {history_file} {from_date}");
            System.exit(1);
        }

        LocalDate genesis = HistoryUtil.DTF_DAY.parseLocalDate(args[0]);
        File histFile = Paths.get(args[1]).toFile();
        HistoryUnit unit = HistoryUnit.fromExt(histFile);
        int fromDate = unit.getFromDate(histFile);
        int fdNew = Integer.parseInt(args[2]);

        if (fdNew <= fromDate) {
            System.out.println("not necessary to align since from date is: " + fromDate);
            System.exit(0);
        }

        int days = HistoryUtil.daysFromBegin(genesis, DateUtil.yyyyMmDdToLocalDate(fdNew));
        OffsetLengthCoder coder = new OffsetLengthCoder(HistoryReader.fromHistoryFile(histFile));
        File tmpFile = unit.createTmpFile(TickType.TRADE.name(), histFile.getParentFile(), fdNew,
                unit.getToDate(histFile));
        try (
                DataFile dfBase = new DataFile(histFile, true);
                HistoryWriter<ByteString> writer = new HistoryWriter<>(tmpFile, coder, ByteString.class)
        ) {
            EntryMergerCompact<MutableTickEntry> compacter = new EntryMergerCompact<>(days,
                    MutableTickEntry.class);
            final Iterator<Item<ByteString>> itBase = new ItemExtractor<>(ByteString.class, dfBase).iterator();
            final BufferedBytesTransporter tranBase = new BufferedBytesTransporter(dfBase, coder.maxLength());

            final ByteArrayTarget targetBase = new ByteArrayTarget();
            while (itBase.hasNext()) {
                Item<ByteString> item = itBase.next();
                tranBase.transferTo(item.getOffset(), item.getLength(), targetBase);
                writer.withEntry(item.getKey(), compacter.merge(targetBase.data(), null));
            }
        }

        HistoryUtil.replaceFile(unit.convert(tmpFile, unit), tmpFile);
    }
}
