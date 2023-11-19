package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import com.google.common.base.Strings;
import de.marketmaker.istar.merger.util.MoleculeRequestReplayer;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.ZonePropertiesReader;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.io.*;
import java.util.*;

public class MoleculeStatistics extends Thread implements InitializingBean {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private AppidLookup appidLookup;
    
    private MonthAggregator monthAggregator;
    private DurationAggregator durationAggregator;
    private BlockAggregator blockAggregator;
    private CustomerMarketAggregator customerMarketAggregator;
    private EasytradeInstrumentProvider instrumentProvider;
    private Map<String, Properties> zones;
    private String zonesBaseDir;
    private String zonesProperties;
    private String inputDir;
    private String outputDir;

    /**
     * Create a new instance. Please use the setters to provide
     * - AppId lookup
     * - an instrument provider
     * - the info on where to find the zone definitions.
     */
    public MoleculeStatistics() {}

    /**
     * Set the folder in which we expect the molecule logfiles to be.
     * @param inputDir The folder path.
     */
    public void setInputDir(String inputDir) {
        this.inputDir = inputDir;
    }

    /**
     * Set the folder into which we write generated statistics.
     * @param outputDir
     */
    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    /**
     * Set the path to the appid lookup file.
     * It is expected to be a csv file containing AppId,KurzBezeichnung,Bezeichnung.
     * @param appidLookupFile The path to the file.
     */
    public void setAppidLookupFile(String appidLookupFile) {
        this.appidLookup = new AppidLookup(appidLookupFile);
    }

    /**
     * Set the instrument provider to be used for looking up markets from symbols.
     * @param instrumentProvider The instance of EasytradeInstrumentProvider.
     */
    public void setInstrumentProvider(EasytradeInstrumentProvider instrumentProvider) {
        this.instrumentProvider = instrumentProvider;
    }

    /**
     * Set the path to the base directory in which the zone definitions reside.
     * @param zonesBaseDir The directory.
     */
    public void setZonesBaseDir(String zonesBaseDir) {
        this.zonesBaseDir = zonesBaseDir;
    }

    /**
     * Set the path to the zones.properties file relative to the zones base dir.
     * @param zonesProperties The path of the file.
     */
    public void setZonesProperties( String zonesProperties) {
        this.zonesProperties = zonesProperties;
    }

    /**
     * Check that all needed properties have been set and init the MoleculeStatistics.
     */
    public void init() {
        StringBuilder error = new StringBuilder();
        if (Strings.isNullOrEmpty(zonesBaseDir)) {
            error.append("\t- set zonesBaseDir.\n");
        }
        if (Strings.isNullOrEmpty(zonesProperties)) {
            error.append("\t- set zonesProperties.\n");
        }

        if (instrumentProvider == null) {
            error.append("\t- provide an EasyTradeProvider.\n");
        }

        if (appidLookup == null) {
            logger.error("\t- provide path to AppId lookup file.\n");
        }

        if (error.length() > 0) {
            logger.error("MoleculeStatistics is missing required setup. Please ...\n" + error.toString());
            System.exit(-1);
        } else {
            ZonePropertiesReader zonePropertiesReader = new ZonePropertiesReader(zonesBaseDir, zonesProperties);
            this.zones = zonePropertiesReader.loadZones();
        }

        logger.info("MoleculeStatistics has been initialized.");
    }

    /**
     * Find all molecule logfiles in the input folder.
     * @return An array of filenames
     */
    private String[] scanInputDir() {
        File inputDirectory = new File(this.inputDir);
        return inputDirectory.list(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.startsWith("molecule.");
            }
        });
    }
    
    /**
     * Start the import of logfiles.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        init();
        
        this.start();
    }

    @Override
    public void run() {

        try {
            Thread.sleep(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String[] inputFiles = scanInputDir();

        for (String f : inputFiles) {
            addLog(outputDir, inputDir + File.separator + f);
        }

		System.exit(0);
    }
    
    /**
     * Extract the date as a string from the molecule logfile.
     *
     * @param filename The name of the molecule logfile.
     * @return The date of the file.
     */
    public String extractDate(String filename) {
        String date = null;

        try {
            int start = filename.indexOf(".log.") + 5;
            int end = filename.indexOf(".gz", start);
            if (start > 0 && end > 0 && end > start) {
                date = filename.substring(start, end);
            }
        } catch (Exception e) {
            logger.warn("Unable to get date from molecule logfile: " + filename);
        }

        return date;
    }

    /**
     * Shorten the given date to yyyy-mm
     *
     * @param date The date extracted from the filename.
     * @return The year and month part of the date.
     */
    public String getYearMonth(String date) {
        if (date == null) {
            return null;
        } else {
            return date.substring(0, date.lastIndexOf("-"));
        }
    }

    /**
     * Extract the zone from a moleculue log line.
     *
     * @param line The log line.
     * @return The zone the logged request was made to.
     */
    public String extractZone(String line) {
        String zone = null;

        try {
            int jsonStart = line.indexOf('{');

            int zoneStart = jsonStart - 2;
            while (line.charAt(zoneStart) != ' ') {
                zoneStart--;
            }
            zone = line.substring(zoneStart + 1, jsonStart - 1);
        } catch (Exception e) {
            logger.warn("Can't extract zone from log line: " + e.getMessage());
        }

        return zone;
    }

    /**
     * Get the hour in which a log line has been written.
     *
     * @param line The log line.
     * @return The hour.
     */
    public String extractHour(String line) {
        String hour = null;

        try {
            hour = line.substring(0, 2);
        } catch (Exception e) {
            logger.warn("Can't extract hour from log line: " + e.getMessage());
        }

        return hour;
    }

    /**
     * Open the aggregators for the the given year and month.
     * @param outputDir The folder in which the csv files reside.
     * @param yearMonth The year and month to open.
     */
    public void openAggregators(String outputDir, String yearMonth) {
        String csvFilename = outputDir + File.separator + "molecule-" + yearMonth + ".csv";
        monthAggregator = new MonthAggregator(csvFilename, appidLookup);
        monthAggregator.start();

        String durationFilename = outputDir + File.separator + "durations-" + yearMonth + ".csv";
        durationAggregator = new DurationAggregator(durationFilename, appidLookup);
        durationAggregator.start();

        String blockFilename = outputDir + File.separator + "blocks-" + yearMonth + ".csv";
        blockAggregator = new BlockAggregator(blockFilename, appidLookup);
        blockAggregator.start();

        String customerMarketFilename = outputDir + File.separator + "customerMarket-" + yearMonth + ".csv";
        String customerSymbolFilename = outputDir + File.separator + "customerSymbol-" + yearMonth + ".csv";
        customerMarketAggregator = new CustomerMarketAggregator(customerMarketFilename, customerSymbolFilename, appidLookup, zones, instrumentProvider);
        customerMarketAggregator.start();
    }

    /**
     * Close all open aggregators.
     */
    public void closeAggregators() {
        monthAggregator.close();
        durationAggregator.close();
        blockAggregator.close();
        customerMarketAggregator.close();
    }
    
    /**
     * Collect a molecule log file as csv into one file per month.
     *
     * @param outputDir The folder in which the csv resides.
     * @param filename  The molecule logfile name.
     */
    public void addLog(String outputDir, String filename) {

        logger.info("==========================================================================================");
        logger.info(" Adding statistics for " + filename);
        long start = System.currentTimeMillis();

        try {
            File moleculeFile = new File(filename);
            String date = extractDate(filename);
            String yearMonth = getYearMonth(date);
            InputStream is = MoleculeRequestReplayer.getInputStream(moleculeFile);
            int lineNumber = 0;

            openAggregators(outputDir, yearMonth);

            try (Scanner sc = new Scanner(is, "utf8")) {
                while (sc.hasNextLine()) {
                    String line = sc.nextLine();
                    lineNumber++;

                    if (line.isEmpty()) {
                        continue;
                    }

                    String zone = extractZone(line);
                    String hour = extractHour(line);

                    MoleculeRequest moleculeRequest;

                    int jsonStart = line.indexOf('{');
                    int jsonEnd = line.lastIndexOf("}") + 1;
                    String json = line.substring(jsonStart, jsonEnd);
                    try {
                        moleculeRequest = GsonUtil.fromJson(json, MoleculeRequest.class);
                    } catch (Exception e) {
                        logger.error("Can't parse MoleculeRequest due to: " + e.getMessage());
                        logger.error("JSON was: " + json);
                        continue;
                    }

                    monthAggregator.aggregateRequest(lineNumber, moleculeRequest, date, hour, zone);
                    durationAggregator.aggregateRequest(lineNumber, moleculeRequest, date, hour, zone);
                    blockAggregator.aggregateRequest(lineNumber, moleculeRequest, date, hour, zone);
                    customerMarketAggregator.aggregateRequest(lineNumber, moleculeRequest, date, hour, zone);

                }
            }

            closeAggregators();

        } catch (Exception e) {
            logger.error("Can't read molecule log <" + filename + "> due to error:", e);
        }

        long end = System.currentTimeMillis();
        long secondTotal = (end - start) / 1000;
        long minutes = secondTotal / 60;
        long seconds = secondTotal - (minutes * 60);
        logger.info(" Adding logfile took " + minutes + "m" + seconds + "s");
        logger.info("==========================================================================================");
    }

}
