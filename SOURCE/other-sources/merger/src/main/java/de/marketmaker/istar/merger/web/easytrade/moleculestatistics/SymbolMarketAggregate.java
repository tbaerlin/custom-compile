package de.marketmaker.istar.merger.web.easytrade.moleculestatistics;

import de.marketmaker.istar.domain.Market;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class SymbolMarketAggregate {

    private final HashMap<String, Long> symbolCounts;
    private final HashMap<String, Long> marketCounts;

    public SymbolMarketAggregate() {
        this.symbolCounts = new HashMap<>();
        this.marketCounts = new HashMap<>();
    }

    public void countSymbol(String symbol, String marketName) {
        Long symbolCount = symbolCounts.getOrDefault(symbol, 0L);
        symbolCount++;
        symbolCounts.put(symbol, symbolCount);

        Long marketCount = marketCounts.getOrDefault(marketName, 0L);
        marketCount++;
        marketCounts.put(marketName, marketCount);
    }
    
    public Map<String, Long> getMarketCounts() {
        return marketCounts;
    }

    public Map<String, Long> getSymbolCounts() {
        return symbolCounts;
    }
}
