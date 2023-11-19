/*
 * DomainContextReader.java
 *
 * Created on 27.02.12 09:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import oracle.jdbc.OracleTypes;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.object.StoredProcedure;

import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.MarketcategoryEnum;
import de.marketmaker.istar.domainimpl.CountryDp2;
import de.marketmaker.istar.domainimpl.CurrencyDp2;
import de.marketmaker.istar.domainimpl.DomainContextImpl;
import de.marketmaker.istar.domainimpl.MarketDp2;
import de.marketmaker.istar.domainimpl.SectorDp2;

/**
 * Reads DomainContext data from mdp ResultSets.
 * @author oflege
 */
class DomainContextReader extends AbstractDp2Reader {

    private class SectorsProcedure extends StoredProcedure {
        public SectorsProcedure() {
            super(dataSource, "begin ? := DPADFInterface.SectorsForIstar; end;");
            setFetchSize(fetchSize);
            setSqlReadyForUse(true);
            declareParameter(new SqlOutParameter("result", OracleTypes.CURSOR, new ResultSetExtractor() {
                public Object extractData(ResultSet rs) throws DataAccessException {
                    try {
                        readSectorsResultSet(rs);
                    } catch (Exception e) {
                        throw new DataRetrievalFailureException("SectorsProcedure failed", e);
                    }
                    return null;

                }
            }));
            compile();
        }
    }

    private class CurrenciesProcedure extends StoredProcedure {
        public CurrenciesProcedure() {
            super(dataSource, "begin ? := DPADFInterface.CurrenciesForIstar; end;");
            setFetchSize(fetchSize);
            setSqlReadyForUse(true);
            declareParameter(new SqlOutParameter("result", OracleTypes.CURSOR, new ResultSetExtractor() {
                public Object extractData(ResultSet rs) throws DataAccessException {
                    try {
                        readCurrenciesResultSet(rs);
                    } catch (Exception e) {
                        throw new DataRetrievalFailureException("CurrenciesProcedure failed", e);
                    }
                    return null;

                }
            }));
            compile();
        }
    }

    private class CountriesProcedure extends StoredProcedure {
        public CountriesProcedure() {
            super(dataSource, "begin ? := DPADFInterface.CountriesForIstar; end;");
            setFetchSize(fetchSize);
            setSqlReadyForUse(true);
            declareParameter(new SqlOutParameter("result", OracleTypes.CURSOR, new ResultSetExtractor() {
                public Object extractData(ResultSet rs) throws DataAccessException {
                    try {
                        readCountriesResultSet(rs);
                    } catch (Exception e) {
                        throw new DataRetrievalFailureException("CountriesProcedure failed", e);
                    }
                    return null;

                }
            }));
            compile();
        }
    }

    private class MarketsProcedure extends StoredProcedure {
        public MarketsProcedure() {
            super(dataSource, "begin ? := DPADFInterface.MarketsForIstar; end;");
            setFetchSize(fetchSize);
            setSqlReadyForUse(true);
            declareParameter(new SqlOutParameter("result", OracleTypes.CURSOR, new ResultSetExtractor() {
                public Object extractData(ResultSet rs) throws DataAccessException {
                    try {
                        readMarketsResultSet(rs);
                    } catch (Exception e) {
                        throw new DataRetrievalFailureException("MarketsProcedure failed", e);
                    }
                    return null;

                }
            }));
            compile();
        }
    }

    DomainContextReader(DataSource dataSource, DomainContextImpl domainContext, int fetchSize) {
        super(dataSource, domainContext, false, fetchSize);
    }

    void read() throws Exception {
        readSectors();
        readCurrencies();
        readCountries();
        readMarkets();
    }

    private void readSectors() {
        new SectorsProcedure().execute(Collections.emptyMap());
    }

    private void readCurrencies() {
        new CurrenciesProcedure().execute(Collections.emptyMap());
    }

    private void readCountries() {
        new CountriesProcedure().execute(Collections.emptyMap());
    }

    private void readMarkets() {
        new MarketsProcedure().execute(Collections.emptyMap());
    }

    private void readSectorsResultSet(ResultSet rs) throws Exception {
        while (rs.next()) {
            final long sectorid = rs.getLong("sectorid");
            final String name = rs.getString("name");
            final String nameEn = rs.getString("name_en");
//            final String isoSymbol = rs.getString("iso");
//            final String vwdSymbol = rs.getString("vwd");
//            final String wmSymbol = rs.getString("wm");
//            final String mmSymbol = rs.getString("mm");
            final String dpteamSymbol = rs.getString("dp");

            final SectorDp2 sector = new SectorDp2(sectorid, name);
            sector.setNames(Language.de, name);
            sector.setNames(Language.en, nameEn);
            addSymbol(sector, KeysystemEnum.DP_TEAM, dpteamSymbol);
            this.domainContext.addSector(sector);
        }
    }

    private void readCurrenciesResultSet(ResultSet rs) throws Exception {
        while (rs.next()) {
            final long currencyid = rs.getLong("currencyid");
            final String name = rs.getString("name");
            final String symbol = rs.getString("symbol");
            final String nameEn = rs.getString("name_en");

            final CurrencyDp2 currency = new CurrencyDp2(currencyid, name);
            currency.setNames(Language.de, name);
            currency.setNames(Language.en, nameEn);
            addSymbol(currency, KeysystemEnum.ISO, symbol);
            this.domainContext.addCurrency(currency);
        }
    }

    private void readCountriesResultSet(ResultSet rs) throws Exception {
        while (rs.next()) {
            final long countryid = rs.getLong("countryid");
            final String name = rs.getString("name");
            final long currencyid = rs.getLong("currencyid");
            final String nameEn = rs.getString("name_en");

            final CountryDp2 country = new CountryDp2(countryid, name);
            country.setNames(Language.de, name);
            country.setNames(Language.en, nameEn);
            country.setCurrency(this.domainContext.getCurrency(currencyid));
            addSymbol(rs, country, "symbol", KeysystemEnum.ISO);

            this.domainContext.addCountry(country);
        }
    }

    private void readMarketsResultSet(ResultSet rs) throws Exception {
        Set<String> columnNames = getColumnNames(rs);
        final boolean nameItAvailable = columnNames.contains("name_it");
        while (rs.next()) {
            final long marketid = rs.getLong("marketid");
            final String name = rs.getString("name");
            final String nameEn = rs.getString("name_en");
            final String nameIt = nameItAvailable ? rs.getString("name_it") : null;

            final MarketDp2 market = new MarketDp2(marketid, name);
            market.setNames(Language.de, name);
            if (nameEn != null) {
                market.setNames(Language.en, nameEn);
            }
            if (nameIt != null && !nameIt.equals(nameEn)) {
                market.setNames(Language.it, nameIt);
            }
            final long countryid = rs.getLong("countryid");
            market.setCountry(this.domainContext.getCountry(countryid));
            final int marketcategoryid = rs.getInt("marketcategoryid");
            market.setMarketcategory(MarketcategoryEnum.valueOf(marketcategoryid));

            addSymbol(rs, market, "iso", KeysystemEnum.ISO);
            addSymbol(rs, market, "vwd", KeysystemEnum.VWDFEED);
            addSymbol(rs, market, "wm", KeysystemEnum.WM);
            addSymbol(rs, market, "mm", KeysystemEnum.MM);
            addSymbol(rs, market, "dp", KeysystemEnum.DP_TEAM);

            this.domainContext.addMarket(market);
        }
    }

    private Set<String> getColumnNames(ResultSet rs) throws SQLException {
        final Set<String> result = new HashSet<>();
        ResultSetMetaData md = rs.getMetaData();
        for (int i = 1; i <= md.getColumnCount(); i++) {
            result.add(md.getColumnName(i).toLowerCase());
        }
        return result;
    }

}
