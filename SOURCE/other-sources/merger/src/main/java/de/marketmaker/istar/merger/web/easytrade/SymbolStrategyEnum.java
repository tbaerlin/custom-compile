package de.marketmaker.istar.merger.web.easytrade;

import java.util.EnumSet;
import java.util.Set;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public enum SymbolStrategyEnum {
    AUTO, WKN, ISIN, VWDCODE, IID, QID, VWDCODE_PREFIX, MMWKN, BIS_KEY, INFRONT_ID;

    private static final Set<SymbolStrategyEnum> INSTRUMENT_STRATEGIES = EnumSet.of(WKN, ISIN, IID);

    public boolean isInstrumentStrategy() {
        return INSTRUMENT_STRATEGIES.contains(this);
    }
}
