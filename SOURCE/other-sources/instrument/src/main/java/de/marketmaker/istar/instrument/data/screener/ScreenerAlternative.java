package de.marketmaker.istar.instrument.data.screener;

import java.io.Serializable;

/**
 * @author umaurer
 */
public class ScreenerAlternative implements Serializable {
    static final long serialVersionUID = 16121969L;

    private final long instrumentId;
    private String isin;
    private final String name;
    private String currency;
    private String country;

    public ScreenerAlternative(long instrumentId, String isin, String name, String currency, String country) {
        this.instrumentId = instrumentId;
        this.isin = isin;
        this.name = name;
        this.currency = currency;
        this.country = country;
    }

    public long getInstrumentId() {
        return instrumentId;
    }

    public String getIsin() {
        return isin;
    }

    public String getName() {
        return name;
    }

    public String getCurrency() {
        return currency;
    }

    public String getCountry() {
        return country;
    }

    public String toString() {
        return "ScreenerAlternative[" + this.instrumentId + ", " +
                this.isin + ", " + this.name + ", " + this.currency + ", " + this.country + "]";
    }
}
