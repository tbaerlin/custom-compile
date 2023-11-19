/*
 * InstrumentDp2.java
 *
 * Created on 19.12.2004 17:05:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.instrument;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.joda.time.DateTime;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.domain.Country;
import de.marketmaker.istar.domain.Issuer;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Market;
import de.marketmaker.istar.domain.Sector;
import de.marketmaker.istar.domain.instrument.DetailedInstrumentType;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.MmInstrumentclass;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.ItemWithSymbolsDp2;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public abstract class InstrumentDp2 extends ItemWithSymbolsDp2 implements Instrument, Serializable {
    static final long serialVersionUID = -108L;

    private Market homeExchange;

    private boolean homeExchangeByHeuristic = false;

    private String name;

    private String aliases; // istar-136

    private String lei;

    private Country country;

    private int expirationDate = Integer.MIN_VALUE;

    private List<QuoteDp2> quotes = new ArrayList<>(4);

    private Issuer issuer;

    private Sector sector;

    private DetailedInstrumentTypeDp2 detailedInstrumentType;

    private MmInstrumentclass mmInstrumentclass;

    protected InstrumentDp2() {
    }

    public InstrumentDp2(long id) {
        super(id);
    }

    public InstrumentDp2(long id, Map<KeysystemEnum, String> symbols) {
        super(id, symbols);
    }

    public List<Quote> getQuotes() {
        return Collections.<Quote>unmodifiableList(this.quotes);
    }

    public QuoteDp2[] getQuotesDp2() {
        return this.quotes.toArray(new QuoteDp2[quotes.size()]);
    }

    public Quote getQuote(long quoteid) {
        for (final Quote quote : this.quotes) {
            if (quote.getId() == quoteid) {
                return quote;
            }
        }
        return null;
    }

    public String getSymbolFww() {
        return getSymbol(KeysystemEnum.FWW);
    }

    public String getSymbolCusip() {
        return getSymbol(KeysystemEnum.CUSIP);
    }

    public String getSymbolIsin() {
        return getSymbol(KeysystemEnum.ISIN);
    }

    public String getSymbolSedol() {
        return getSymbol(KeysystemEnum.SEDOL);
    }

    public String getSymbolValor() {
        return getSymbol(KeysystemEnum.VALOR);
    }

    public String getSymbolValorsymbol() {
        return getSymbol(KeysystemEnum.VALORSYMBOL);
    }

    public String getSymbolWkn() {
        return getSymbol(KeysystemEnum.WKN);
    }

    public String getSymbolOeWkn() {
        return getSymbol(KeysystemEnum.OEWKN);
    }

    public String getSymbolWmGd195Id() {
        return getSymbol(KeysystemEnum.WM_GD195_ID);
    }

    public String getSymbolWmGd195Name() {
        return getSymbol(KeysystemEnum.WM_GD195_NAME);
    }

    public String getSymbolTicker() {
        return getSymbol(KeysystemEnum.TICKER);
    }

    public String getSymbolEurexTicker() {
        return getSymbol(KeysystemEnum.EUREXTICKER);
    }

    public String getSymbolWmGd664() {
        return getSymbol(KeysystemEnum.GD664);
    }

    public String getSymbolWmGd260() {
        return getSymbol(KeysystemEnum.GD260);
    }

    public String getSymbolMmName() {
        return getSymbol(KeysystemEnum.MMNAME);
    }

    public abstract InstrumentTypeEnum getInstrumentType();

    public MmInstrumentclass getMmInstrumentclass() {
        return this.mmInstrumentclass;
    }

    public void setMmInstrumentclass(MmInstrumentclass mmInstrumentclass) {
        this.mmInstrumentclass = mmInstrumentclass;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public Market getHomeExchange() {
        return homeExchange;
    }

    public void setHomeExchange(Market homeExchange) {
        this.homeExchange = homeExchange;
    }

    public boolean isHomeExchangeByHeuristic() {
        return homeExchangeByHeuristic;
    }

    public void setHomeExchangeByHeuristic(boolean homeExchangeByHeuristic) {
        this.homeExchangeByHeuristic = homeExchangeByHeuristic;
    }

    public Market getHomeExchangeWithoutHeuristic() {
        return isHomeExchangeByHeuristic() ? DomainContextImpl.UNKNOWN_MARKET : getHomeExchange();
    }

    public Issuer getIssuer() {
        return issuer;
    }

    public void setIssuer(Issuer issuer) {
        this.issuer = issuer;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAliases() {
        return aliases;
    }

    public void setAliases(String aliases) {
        this.aliases = aliases;
    }

    @Override
    public String getLei() {
        return lei;
    }

    public void setLei(String lei) {
        this.lei = lei;
    }

    @Override
    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    @Deprecated
    public int getExpirationDate() {
        return expirationDate;
    }

    public DateTime getExpiration() {
        if (this.expirationDate == Integer.MIN_VALUE) {
            return null;
        }
        return DateUtil.yyyymmddToDateTime(this.expirationDate);
    }

    public void setExpirationDate(int expirationDate) {
        this.expirationDate = expirationDate;
    }

    public DetailedInstrumentType getDetailedInstrumentType() {
        return detailedInstrumentType;
    }

    public void setDetailedInstrumentType(DetailedInstrumentType detailedInstrumentType) {
        this.detailedInstrumentType = (DetailedInstrumentTypeDp2) detailedInstrumentType;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + getId() + ".iid, '" + getName() + "'}";
    }

    public String toDebugString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public void addQuote(QuoteDp2 quote) {
        this.quotes.add(quote);
        quote.setInstrument(this);
    }

    public void replaceOrAdd(QuoteDp2 quote) {
        for (int i = 0; i < this.quotes.size(); i++) {
            if (this.quotes.get(i).getId() == quote.getId()) {
                this.quotes.set(i, quote);
                quote.setInstrument(this);
                return;
            }
        }
        addQuote(quote);
    }

    public boolean equals(Object o) {
        if (!super.equals(o)) return false;

        final InstrumentDp2 that = (InstrumentDp2) o;

        if (getInstrumentType() != that.getInstrumentType()) return false;
        if (expirationDate != that.expirationDate) return false;
        if (!equalsById(this.country, that.country)) return false;
        if (detailedInstrumentType != null ? !detailedInstrumentType.equals(that.detailedInstrumentType) : that.detailedInstrumentType != null)
            return false;
        if (!equalsById(this.homeExchange, that.homeExchange)) return false;
        if (issuer != null ? !issuer.equals(that.issuer) : that.issuer != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (quotes != null ? !quotesEquals(that) : that.quotes != null) return false;
        if (!equalsById(this.sector, that.sector)) return false;
        // TODO: lei?

        return true;
    }

    private boolean quotesEquals(InstrumentDp2 that) {
        if (this.quotes.size() != that.getQuotes().size()) {
            return false;
        }
        for (QuoteDp2 quote : this.quotes) {
            final Quote other = that.getQuote(quote.getId());
            if (other == null || !quote.equals(other)) {
                return false;
            }
        }
        return true;
    }
}
