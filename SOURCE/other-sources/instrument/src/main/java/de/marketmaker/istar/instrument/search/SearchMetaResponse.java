/*
 * SearchMetaResponse.java
 *
 * Created on 26.01.2005 16:50:12
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.io.Serializable;
import java.util.List;

import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Currency;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.common.request.AbstractIstarResponse;

/**
 * An i-star response which contains meta-data for an instrument server.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SearchMetaResponse extends AbstractIstarResponse {
    static final long serialVersionUID = 213128L;

    private List<Country> countries;

    private List<Currency> currencies;

    private List<Market> markets;

    private List<InstrumentTypeEnum> instrumentTypes;

    public SearchMetaResponse() {
    }

    /**
     * @return a list of countries supported by this instrument server.
     * @see de.marketmaker.istar.domain.Country
     */
    public List<Country> getCountries() {
        return countries;
    }

    public void setCountries(List<Country> countries) {
        this.countries = countries;
    }

    /**
     * @return a list of currencies supported by this instrument server
     * @see de.marketmaker.istar.domain.Currency
     */
    public List<Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(List<Currency> currencies) {
        this.currencies = currencies;
    }

    /**
     * @return a list of markets supported by this instrument server
     * @see de.marketmaker.istar.domain.Market
     */
    public List<Market> getMarkets() {
        return markets;
    }

    public void setMarkets(List<Market> markets) {
        this.markets = markets;
    }

    /**
     * @return a list of instrument types supported by this instrument server.
     * @see de.marketmaker.istar.domain.instrument.InstrumentTypeEnum
     */
    public List<InstrumentTypeEnum> getInstrumentTypes() {
        return instrumentTypes;
    }

    public void setInstrumentTypes(List<InstrumentTypeEnum> instrumentTypes) {
        this.instrumentTypes = instrumentTypes;
    }
}
