package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import org.apache.commons.lang3.StringUtils;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;

public abstract class Aggregator {

    public static final String HEADER_NAME_DATE = "date";
    public static final String HEADER_NAME_HOUR = "hour";
    public static final String HEADER_NAME_AUTHENTICATION = "authentication";
    public static final String HEADER_NAME_AUTHENTICATION_TYPE = "authenticationType";
    public static final String HEADER_NAME_APP_NAME = "app name";
    public static final String HEADER_NAME_APP_NAME_SHORT = "app name short";
    public static final String HEADER_NAME_ZONE = "zone";
    public static final String HEADER_NAME_BLOCK_COUNT = "blockCount";
    public static final String HEADER_NAME_BLOCK_NUMBER = "blockNumber";
    public static final String HEADER_NAME_BLOCK_TYPE = "blockType";
    public static final String HEADER_NAME_SYMBOL = "symbol";
    public static final String HEADER_NAME_SYMBOL_COUNT = HEADER_NAME_SYMBOL + "Count";
    public static final String HEADER_NAME_TOTAL_DURATION = "totalDuration";
    public static final String HEADER_NAME_BLOCK_DURATION = "blockDuration";

    /**
     * Replace commas with COMMA.
     *
     * @param input The input string
     * @return The result
     */
    public String commaSafe(String input) {
        if (StringUtils.isEmpty(input)) {
            return input;
        } else {
            return input.replaceAll(",", "COMMA");
        }
    }

    public abstract void start();

    public abstract void aggregateRequest(int lineNumber, MoleculeRequest moleculeRequest, String date, String hour, String zone);

    public abstract void close();
}
