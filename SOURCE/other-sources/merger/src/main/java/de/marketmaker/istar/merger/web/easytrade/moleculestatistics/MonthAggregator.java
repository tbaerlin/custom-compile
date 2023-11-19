package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;
import java.util.Map;

/**
 * This aggregator simply collects all requests into on big csv file for a month.
 */
public class MonthAggregator extends Aggregator {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final AppidLookup appidLookup;

    private Writer writer;

    private final String csvFilename;

    public MonthAggregator(String csvFilename, AppidLookup appidLookup) {
        this.csvFilename = csvFilename;
        this.appidLookup = appidLookup;
    }

    public void start() {
        try {
            File csvFile = new File(csvFilename);
            boolean addHeader = !csvFile.exists();
            writer = new FileWriter(csvFile, true);

            if (addHeader) {
                writer.write(HEADER_NAME_DATE + "," + HEADER_NAME_HOUR + "," + HEADER_NAME_AUTHENTICATION + ","
                        + HEADER_NAME_APP_NAME + "," + HEADER_NAME_APP_NAME_SHORT + "," + HEADER_NAME_AUTHENTICATION_TYPE + ","
                        + HEADER_NAME_ZONE + "," + HEADER_NAME_BLOCK_COUNT + "," + HEADER_NAME_BLOCK_NUMBER + ","
                        + HEADER_NAME_BLOCK_TYPE + "," + HEADER_NAME_SYMBOL_COUNT + "," + HEADER_NAME_SYMBOL + ","
                        + HEADER_NAME_TOTAL_DURATION + "," + HEADER_NAME_BLOCK_DURATION + "\n");
            }
        } catch (Exception e) {
            logger.error("Can't start aggregate file due to " + e.getMessage());
        }
    }

    public void aggregateRequest(int lineNumber, MoleculeRequest moleculeRequest, String date, String hour, String zone) {
        try {
            String authentication = moleculeRequest.getAuthentication();
            String authenticationType = moleculeRequest.getAuthenticationType();
            int atomCount = moleculeRequest.getAtomRequests().size();
            int duration = moleculeRequest.getMs();

            List<MoleculeRequest.AtomRequest> atomRequests = moleculeRequest.getAtomRequests();
            for (int i = 0; i < atomRequests.size(); i++) {
                MoleculeRequest.AtomRequest atomRequest = atomRequests.get(i);

                String blockName = atomRequest.getName();
                Map<String, String[]> parameters = atomRequest.getParameterMap();
                String[] symbols = parameters.get("symbol");
                String symbol = "none";
                int symbolCount = 0;
                if (symbols != null && symbols.length > 0) {
                    symbol = String.join(";", symbols);
                    symbolCount = symbols.length;
                }
                int blockDuration = atomRequest.getMs();
                AppidLookup.AppName appName = appidLookup.getAppName(authentication);
                writer.write(date + "," + hour + "," + commaSafe(authentication) + "," + commaSafe(appName.name)
                        + "," + commaSafe(appName.shortName) + "," + commaSafe(authenticationType)
                        + "," + commaSafe(zone) + "," + atomCount + "," + (i + 1) + "," + commaSafe(blockName)
                        + "," + symbolCount + "," + commaSafe(symbol) + "," + duration + "," + blockDuration + "\n");
            }
        } catch (Exception e) {
            logger.error("Can't aggregate line " + lineNumber + " due to Exception: " + e.getClass() + " " + e.getMessage());
            e.printStackTrace();
        }

    }

    public void close() {
        try {
            if (writer != null) {
                writer.close();
            }
        } catch (IOException e) {
            logger.error("Can't close writer due to " + e.getMessage());
        }
    }
}
