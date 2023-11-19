package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import de.marketmaker.istar.common.util.ClassUtil;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.util.MoleculeRequestReplayer;
import de.marketmaker.istar.merger.web.GsonUtil;
import de.marketmaker.istar.merger.web.easytrade.MoleculeRequest;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategy;
import de.marketmaker.istar.merger.web.easytrade.block.MarketStrategyFactory;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.Writer;
import java.util.*;
import java.util.stream.Collectors;

public class CustomerMarketAggregator extends Aggregator {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String marketCsvFilename;
    private final String symbolCsvFilename;

    private final AppidLookup appidLookup;
    
    private final Map<String, Properties> zones;

    private final Map<CustomerBlockAggregationKey, SymbolMarketAggregate> aggregateData;
    
    private final EasytradeInstrumentProvider instrumentProvider;

    private final static String DEFAULT_MARKETSTRATEGY_PROPERTYNAME = "marketStrategy";

    private final static SymbolStrategyEnum DEFAULT_SYMBOLSTRATEGY = SymbolStrategyEnum.AUTO;
    
    private final static String MARKET_NOT_FOUND = "unknown";
    
    /**
     * Create new and empty aggregator.
     *
     * @param marketCsvFilename The file to aggregate per market into.
     * @param symbolCsvFilename The file to aggregate per symbol into.
     */
    public CustomerMarketAggregator(String marketCsvFilename, String symbolCsvFilename, AppidLookup appidLookup, Map<String, Properties> zones, EasytradeInstrumentProvider instrumentProvider) {
        aggregateData = new HashMap<>();
        this.marketCsvFilename = marketCsvFilename;
        this.symbolCsvFilename = symbolCsvFilename;
        this.appidLookup = appidLookup;
        this.zones = zones;
        this.instrumentProvider = instrumentProvider;
    }

    public void start() {
        aggregateData.clear();

        try {
            File inputFile = new File(marketCsvFilename + ".json");
            if (inputFile.exists()) {
                InputStream is = MoleculeRequestReplayer.getInputStream(inputFile);

                try (Scanner sc = new Scanner(is, "utf8")) {
                    while (sc.hasNextLine()) {
                        String line = sc.nextLine();

                        try {
                            CustomerMarketAggregator.JsonPojo jsonPojo = GsonUtil.fromJson(line, CustomerMarketAggregator.JsonPojo.class);
                            aggregateData.put(jsonPojo.customerBlockAggregationKey, jsonPojo.aggregate);
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
     * Resolve a MarketStrategy either by the name of a predefined MarketStrategy instance or by a sting based definiton
     * of a strategy. The fallback is used if it is neither a predefined strategy or when parsing the string base definiton
     * fails.
     *
     * @param marketStrategyName The instance name or definition of the MarketStrategy.
     * @param fallbackMarketStrategy The MarketStrategy to fall back to.
     * @return A MarketStrategy
     */
    private MarketStrategy resolveMarketStrategy(String marketStrategyName, MarketStrategy fallbackMarketStrategy) {
        MarketStrategy marketStrategy = fallbackMarketStrategy;

        try {
            marketStrategy = ClassUtil.getObject(marketStrategyName);
        } catch (Exception e1) {
            try {
                marketStrategy = MarketStrategyFactory.byStrategy(marketStrategyName, fallbackMarketStrategy);
            } catch (Exception e2) {
                logger.error("Unable to get MarketStrategy: " + marketStrategyName + " due to " + e2.getMessage());
                e2.printStackTrace();
            }
        }
        
        return marketStrategy;
    }

    /**
     * Cut the name of a block out of the property name as it is defined in the zone properties. 
     * @param propertyName The name of the property.
     * @return The name of the block.
     */
    public String propertyNameToBlockName(String propertyName) {
        if (propertyName != null) {
            return propertyName.replace(".default.marketStrategy", "");
        } else {
            return null;
        }
        
    }
    
    /**
     * Get the MarketStrategy that is configured for the given zone.
     * In case none is configured but the zone extends another the parent zone will be searched for the MarketStrategy.
     *
     * @param zone The zone the request was initially made to.
     * @return The configured MarketStrategy.
     */
    public Map<String, MarketStrategy> getMarketStrategies(String zone) {

        Map<String, MarketStrategy> marketStrategies = new HashMap<>();
        Properties zoneProperties = zones.get(zone);

        if (zoneProperties != null) {

            MarketStrategy fallbackMarketStrategy = RequestContext.DEFAULT_MARKET_STRATEGY;

            List<String> marketStrategyKeys = zoneProperties.stringPropertyNames().stream().filter(s -> s.endsWith("marketStrategy")).collect(Collectors.toList());

            if (marketStrategyKeys.contains(DEFAULT_MARKETSTRATEGY_PROPERTYNAME)) {
                String marketStrategyName = zoneProperties.getProperty(DEFAULT_MARKETSTRATEGY_PROPERTYNAME);
                MarketStrategy marketStrategy = resolveMarketStrategy(marketStrategyName, fallbackMarketStrategy);
                fallbackMarketStrategy = marketStrategy;
                marketStrategies.put(DEFAULT_MARKETSTRATEGY_PROPERTYNAME, marketStrategy);
                marketStrategyKeys.remove(DEFAULT_MARKETSTRATEGY_PROPERTYNAME);
            }

            for (String key : marketStrategyKeys) {
                String marketStrategyName = zoneProperties.getProperty(key);
                MarketStrategy marketStrategy = resolveMarketStrategy(marketStrategyName, fallbackMarketStrategy);
                marketStrategies.put(propertyNameToBlockName(key), marketStrategy);
            }
        }

        return marketStrategies;
    }

    /**
     * Find the appropriate MarketStrategy for a block.
     * @param marketStrategies The map of market strategies
     * @param blockName The name of the requested block
     * @return The MarketStrategy
     */
    public MarketStrategy getMarketStrategy(Map<String, MarketStrategy> marketStrategies, String blockName) {
        MarketStrategy marketStrategy = marketStrategies.get(blockName);
        if (marketStrategy == null) {
            marketStrategy = marketStrategies.get(DEFAULT_MARKETSTRATEGY_PROPERTYNAME);
        }
        return marketStrategy;
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
            
            Map<String, MarketStrategy> marketStrategies = getMarketStrategies(zone);
            
            for (MoleculeRequest.AtomRequest atomRequest : moleculeRequest.getAtomRequests()) {
                                
                CustomerBlockAggregationKey key = new CustomerBlockAggregationKey(zone, moleculeRequest.getAuthentication(), moleculeRequest.getAuthenticationType(), atomRequest.getName());
                SymbolMarketAggregate aggregate = aggregateData.getOrDefault(key, new SymbolMarketAggregate());

                MarketStrategy marketStrategy = getMarketStrategy(marketStrategies, atomRequest.getName());
                
                Map<String, String[]> parameters = atomRequest.getParameterMap();
                String[] symbols = parameters.get("symbol");
                if (symbols != null) {
                    for (String symbol : symbols) {
                        String market = MARKET_NOT_FOUND;
                        try {
                            Quote quote = instrumentProvider.identifyQuote(symbol, DEFAULT_SYMBOLSTRATEGY, marketStrategy);
                            market = quote.getMarket().getName();
                        } catch (Exception e) {}
                        aggregate.countSymbol(symbol, market);
                    }

                    aggregateData.put(key, aggregate);
                }

            }

        } catch (Exception e) {;
            logger.error("Can't aggregate line " + lineNumber + " due to Exception: " + ExceptionUtils.getStackTrace(e));
        }

    }

    /**
     * Write the aggregated data to the given file.
     */
    public void close() {
        try {
            StringBuilder csvMarketData = new StringBuilder();
            StringBuilder csvSymbolData = new StringBuilder();

            for (Map.Entry<CustomerBlockAggregationKey, SymbolMarketAggregate> e : aggregateData.entrySet()) {
                String vwdId = e.getKey().getAuthentication();
                String shortName = appidLookup.shortNameOrId(vwdId);
                String name = appidLookup.nameOrId(vwdId);
                String keyCsv = e.getKey().getZone() + "," + vwdId + "," + e.getKey().getAuthenticationType() + "," + name + "," + shortName + "," + e.getKey().getBlock();
                
                Map<String, Long> marketData = e.getValue().getMarketCounts();
                String entryMarketCsv = marketData.entrySet().stream().map(me -> keyCsv + "," + me.getKey() + "," + me.getValue()).collect(Collectors.joining("\n"));

                Map<String, Long> symbolData = e.getValue().getSymbolCounts();
                String entrySymbolCsv = symbolData.entrySet().stream().map(me -> keyCsv + "," + me.getKey() + "," + me.getValue()).collect(Collectors.joining("\n"));

                csvMarketData.append(entryMarketCsv).append("\n");
                csvSymbolData.append(entrySymbolCsv).append("\n");
            }

            Writer writer1 = new FileWriter(marketCsvFilename, false);
            writer1.write(HEADER_NAME_ZONE + "," + HEADER_NAME_AUTHENTICATION + "," + HEADER_NAME_AUTHENTICATION_TYPE + "," + HEADER_NAME_APP_NAME + "," + HEADER_NAME_APP_NAME_SHORT + "," + "block,market,blockCount\n");
            writer1.write(csvMarketData.toString());
            writer1.close();

            Writer writer2 = new FileWriter(symbolCsvFilename, false);
            writer2.write(HEADER_NAME_ZONE + "," + HEADER_NAME_AUTHENTICATION + "," + HEADER_NAME_AUTHENTICATION_TYPE + "," + HEADER_NAME_APP_NAME + "," + HEADER_NAME_APP_NAME_SHORT + "," + "block,symbol,blockCount\n");
            writer2.write(csvSymbolData.toString());
            writer2.close();

            String aggregateJson = aggregateData.entrySet().stream().map(CustomerMarketAggregator.JsonPojo::new).map(GsonUtil::toJson).collect(Collectors.joining("\n"));

            Writer writer3 = new FileWriter(marketCsvFilename + ".json", false);
            writer3.write(aggregateJson);
            writer3.close();
        } catch (Exception e) {
            logger.error("Can't write aggregation file due to Exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Pojo for serializing aggregated data to JSON
     */
    private static class JsonPojo {
        public CustomerBlockAggregationKey customerBlockAggregationKey;
        public SymbolMarketAggregate aggregate;

        public JsonPojo(Map.Entry<CustomerBlockAggregationKey, SymbolMarketAggregate> e) {
            this.customerBlockAggregationKey = e.getKey();
            this.aggregate = e.getValue();
        }
    }

}
