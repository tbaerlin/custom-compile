package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import de.marketmaker.istar.merger.util.MoleculeRequestReplayer;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * This aggregator collects request durations per customer and day/hour.
 */
class DurationAggregator extends Aggregator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<HourAggregationKey, DurationAggregate> aggregateData;

    private AppidLookup appidLookup;

    private final String csvFilename;

    /**
     * Create new and empty aggregator.
     *
     * @param csvFilename The file to aggregate into.
     */
    public DurationAggregator(String csvFilename, AppidLookup appidLookup) {
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
                            JsonPojo jsonPojo = GsonUtil.fromJson(line, JsonPojo.class);
                            aggregateData.put(jsonPojo.hourAggregationKey, jsonPojo.durationAggregate);
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
            HourAggregationKey key = new HourAggregationKey(moleculeRequest.getAuthentication(), date, hour);

            DurationAggregate aggregate = aggregateData.getOrDefault(key, new DurationAggregate());
            aggregate.increaseRequestCount();

            Integer totalDuration = moleculeRequest.getMs();
            aggregate.addTotalDuration(totalDuration);

            List<MoleculeRequest.AtomRequest> atomRequests = moleculeRequest.getAtomRequests();
            for (MoleculeRequest.AtomRequest atomRequest : atomRequests) {
                Integer blockDuration = atomRequest.getMs();
                aggregate.addBlockDuration(blockDuration);
                aggregate.increaseBlockCount();
            }

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
            String csvData = aggregateData.entrySet().stream().map(e -> e.getKey().getDate() + "," + e.getKey().getHour() + "," + e.getKey().getVwdId()
                    + "," + appidLookup.getAppName(e.getKey().getVwdId()).name + "," + appidLookup.getAppName(e.getKey().getVwdId()).shortName + "," + e.getValue().getTotalDuration() + ","
                    + e.getValue().getBlockDuration() + "," + e.getValue().getRequestCount() + "," + e.getValue().getBlockCount()).collect(Collectors.joining("\n"));

            Writer writer = new FileWriter(csvFilename, false);
            writer.write(HEADER_NAME_DATE + "," + HEADER_NAME_HOUR + "," + HEADER_NAME_AUTHENTICATION + "," + HEADER_NAME_APP_NAME + "," + HEADER_NAME_APP_NAME_SHORT + "," + "sumTotalDuration,sumBlockDuration,requestCount,blockCount\n");
            writer.write(csvData);
            writer.close();

            String aggregateJson = aggregateData.entrySet().stream().map(JsonPojo::new).map(GsonUtil::toJson).collect(Collectors.joining("\n"));

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
        public HourAggregationKey hourAggregationKey;
        public DurationAggregate durationAggregate;

        public JsonPojo(Map.Entry<HourAggregationKey, DurationAggregate> e) {
            this.hourAggregationKey = e.getKey();
            this.durationAggregate = e.getValue();
        }
    }
}
