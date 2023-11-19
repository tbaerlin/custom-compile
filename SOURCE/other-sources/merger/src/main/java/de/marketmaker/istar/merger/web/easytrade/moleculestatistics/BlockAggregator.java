package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import de.marketmaker.istar.merger.util.MoleculeRequestReplayer;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class BlockAggregator extends Aggregator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String csvFilename;

    private AppidLookup appidLookup;

    private final Map<DayAggregationKey, Long> aggregateData;

    /**
     * Create new and empty aggregator.
     *
     * @param csvFilename The file to aggregate into.
     */
    public BlockAggregator(String csvFilename, AppidLookup appidLookup) {
        aggregateData = new HashMap<>();
        this.csvFilename = csvFilename;
        this.appidLookup = appidLookup;
    }

    /**
     * Read previously aggregated data.
     */
    public void start() {
        aggregateData.clear();

        try {
            File inputFile = new File(csvFilename + ".json");
            if (inputFile.exists()) {
                InputStream is = MoleculeRequestReplayer.getInputStream(inputFile);

                try (Scanner sc = new Scanner(is, "utf8")) {
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();

                        try {
                            BlockAggregator.JsonPojo jsonPojo = GsonUtil.fromJson(line, BlockAggregator.JsonPojo.class);
                            aggregateData.put(jsonPojo.dayAggregationKey, jsonPojo.aggregate);
                        } catch (Exception e) {
                            logger.error("Can't parse aggregate line with due to " + e.getMessage());
                        }
                    }
                }

                is.close();
            }

        } catch (Exception e) {
            logger.error("Can't read aggregation file due to Exception: " + e.getMessage());
        }
    }

    /**
     * Add one MoleculeRequest to the aggregation.
     *
     * @param lineNumber      The line number of the read log file.
     * @param moleculeRequest The MoleculeRequest to aggregate.
     * @param date            The data in the form yyyy-mm-dd.
     * @param hour            The hour in the form hh.
     * @param zone            The zone the request was sent to.
     */
    public void aggregateRequest(int lineNumber, MoleculeRequest moleculeRequest, String date, String hour, String zone) {
        try {
            DayAggregationKey key = new DayAggregationKey(moleculeRequest.getAuthentication(), date);

            Long aggregate = aggregateData.getOrDefault(key, 0L);
            aggregate += moleculeRequest.getAtomRequests().size();
            aggregateData.put(key, aggregate);

        } catch (Exception e) {
            logger.error("Can't aggregate line " + lineNumber + " due to Exception: " + e.getClass() + " " + e.getMessage());
        }

    }

    /**
     * Write the aggregated data to the given file.
     */
    public void close() {
        try {
            Writer writer = new FileWriter(csvFilename, false);

            List<String> days = aggregateData.keySet().stream().map(DayAggregationKey::getDate).sorted().distinct().collect(Collectors.toList());
            List<String> vwdIds = aggregateData.keySet().stream().map(DayAggregationKey::getVwdId).sorted().distinct().collect(Collectors.toList());
            List<String> customerNames = vwdIds.stream().map(appidLookup::shortNameOrId).collect(Collectors.toList());

            String headerLine = HEADER_NAME_DATE + "," + String.join(",", customerNames) + ",totals\n";
            writer.write(headerLine);

            for (String day : days) {
                long total = 0;
                writer.write(day);
                for (String vwdId : vwdIds) {
                    DayAggregationKey key = new DayAggregationKey(vwdId, day);
                    Long aggregate = aggregateData.getOrDefault(key, 0L);
                    total += aggregate;
                    writer.write("," + aggregate);
                }
                writer.write("," + total);
                writer.write("\n");

            }

            writer.close();

            String aggregateJson = aggregateData.entrySet().stream().map(BlockAggregator.JsonPojo::new).map(GsonUtil::toJson).collect(Collectors.joining("\n"));

            Writer writer2 = new FileWriter(csvFilename + ".json", false);
            writer2.write(aggregateJson);
            writer2.close();
        } catch (Exception e) {
            logger.error("Can't write aggregation file due to Exception: " + e.getMessage());
            e.printStackTrace();
        }

    }

    /**
     * Pojo for serializing aggregated data to JSON
     */
    private static class JsonPojo {
        public DayAggregationKey dayAggregationKey;
        public Long aggregate;

        public JsonPojo(Map.Entry<DayAggregationKey, Long> e) {
            this.dayAggregationKey = e.getKey();
            this.aggregate = e.getValue();
        }
    }

}
