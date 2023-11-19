/* FileTickIOReader.java
 *
 * Created on 26.05.2008 15:31:06
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.exporttools.boersestg;


import java.io.File;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.Period;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.feed.ordered.tick.FileTickStore;
import de.marketmaker.istar.feed.tick.TickFiles;
import de.marketmaker.istar.feed.tick.TickRecordImpl;

/**
 * @author Thomas Kiesgen
 */

public class FileTickExporterBoerseStg {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File symbolsFile;

    private File tickDir;

    private File outDir;

    private final int date;

    private String market;

    private Interval interval;

    private FileTickStore fileTickStore = new FileTickStore();

    public FileTickExporterBoerseStg() {
        this.date = Integer.getInteger("exportDay", DateUtil.toYyyyMmDd(new DateTime().minusDays(1)));
        this.interval = new Interval(DateUtil.yyyymmddToDateTime(date), Period.days(1));
    }

    public void setSymbolsFile(File symbolsFile) {
        this.symbolsFile = symbolsFile;
    }

    public void setTickDir(File tickDir) {
        this.tickDir = tickDir;
    }

    public void setOutDir(File outDir) {
        this.outDir = outDir;
    }

    private File findInFile() {
        for (String suffix : new String[]{".tdz", ".td3"}) {
            File inFile = new File(this.tickDir + "/", market + "-" + date + suffix);
            if (inFile.canRead()) {
                return inFile;
            }
        }
        return null;
    }

    private void export() throws Exception {
        logger.info("<export> start export FileTickExporterBoerseStg");
        final List<String> symbols
                = Files.readAllLines(this.symbolsFile.toPath(), Charset.defaultCharset());

        logger.info("<export> export for day " + date);
        this.market = "FFMST";
        logger.info("<export> start for derivatemarket " + market);
        final File file = new File(this.outDir, market + "-" + date + ".csv");

        final File inFile = findInFile();
        if (inFile == null) {
            this.logger.error("<export> no infile for market " + market);
            return;
        }

        this.logger.info("<export> reading " + inFile);
        try (FileTickOutputBoerseStg output = new FileTickOutputBoerseStg(file)) {
            output.init("#WKN;market;bid;bidVolume;ask;askVolume;trade;tradeVolume;time");
            exportBySymbol(output, symbols, inFile);
            logger.info("<export> end export FileTickExporterBoerseStg");
        }
    }

    private void exportBySymbol(FileTickOutputBoerseStg output, List<String> symbolList,
            File inFile) throws Exception {
        for (final String symbol : symbolList) {
            process(inFile, symbol, output, symbol + "." + this.market);
        }
    }

    public void process(File file, final String symbol, FileTickOutputBoerseStg output,
            String vwdcode) throws Exception {
        final byte[] bytes;
        try {
            bytes = this.fileTickStore.readTicks(file, vwdcode);
        } catch (Exception e) {
            this.logger.warn("<process> read ticks failed for " + vwdcode, e);
            return;
        }

        if (bytes == null) {
            logger.warn("no element for " + vwdcode + " in " + file.getAbsolutePath());
            return;
        }

        TickRecordImpl tr = new TickRecordImpl();
        tr.add(date, bytes, TickFiles.getItemType(file));
        output.process(symbol, market, tr.getTimeseries(interval));
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("usage: FileTickExporterBoerseStg <config-file>");
            System.exit(1);
        }

        final FileSystemXmlApplicationContext ac = new FileSystemXmlApplicationContext(args[0]);
        final FileTickExporterBoerseStg e = ac.getBean("exporter", FileTickExporterBoerseStg.class);
        e.export();
        ac.destroy();
    }
}
