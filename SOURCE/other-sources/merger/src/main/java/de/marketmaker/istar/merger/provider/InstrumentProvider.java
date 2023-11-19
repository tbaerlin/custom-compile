/*
 * InstrumentProvider.java
 *
 * Created on 05.07.2006 13:49:54
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.instrument.search.SearchMetaResponse;
import de.marketmaker.istar.instrument.search.SearchResponse;
import de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum;
import de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand;

/**
 * Provides instrument and quote related query methods.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentProvider {
    List<Instrument> getDerivates(long iid, int maxNumResults, InstrumentTypeEnum... type);

    List<Instrument> getByVwdcodePrefix(String vwdcodePrefix, int maxNumResults, InstrumentTypeEnum... type);

    /**
     * Strategy applied to search methods.
     */
    enum StrategyEnum {
        DEFAULT, EXACT, TOLERANT
    }

    /**
     * @return this instrument provider's meta data.
     * @see de.marketmaker.istar.instrument.search.SearchMetaResponse
     */
    SearchMetaResponse getMetadata();

    /**
     * Identifies an instrument by one of its quote ids.
     *
     * @param qid a quote id
     * @return an instrument, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Instrument identifyByQid(Long qid);

    /**
     * Identifies an instrument by its ISIN (International Securities Identification Number).
     *
     * @param isin an ISIN.
     * @return an instrument, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Instrument identifyByIsin(String isin);

    /**
     * Identifies an instrument by its WKN (Wertpapierkennunmmer).
     *
     * @param wkn a WKN
     * @return an instrument, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Instrument identifyByWkn(String wkn);

    /**
     * Identifies an instrument by its instrument id.
     *
     * @param iid an instrument id.
     * @return an instrument, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Instrument identifyByIid(Long iid);

    /**
     * Identifies an instrument by a symbol, which could be an ISIN or a WKN.
     *
     * @param symbol a symbol, either ISIN or WKN
     * @return an instrument, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Instrument identifyByIsinOrWkn(String symbol);

    /**
     * XXX: needs to be verified. Could the return type be a map of instruments keyed by its instrument
     * id. If no instrument for a given id found, leave that keyed value null. Additionally input
     * parameter type could be of type set.
     *
     * @param iids a list of instrument ids against which instrument should be identified.
     * @return see XXX
     */
    List<Instrument> identifyInstruments(List<Long> iids);

    /**
     * Returns a map that associates the given symbols to their belonging instruments.
     * <p>
     * XXX: input parameter could be of type set.
     *
     * @param symbols a list of instrument symbols
     * @param symbolStrategy specifies how symbols are to be interpreted, must not be null and not
     * be {@link de.marketmaker.istar.merger.web.easytrade.SymbolStrategyEnum#AUTO}
     * @return map that may be empty and|or unmodifiable
     */
    Map<String, Instrument> identifyInstruments(List<String> symbols,
            SymbolStrategyEnum symbolStrategy);

    /**
     * Identifies a quote by its VWD feed (vendor key).
     *
     * @param vendorkey VWD feed, also known as vendor key.
     * @return a quote, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Quote identifyByVwdfeed(String vendorkey);

    /**
     * Identifies a quote by its VWD code.
     *
     * @param vwdcode VWD code
     * @return a quote, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Quote identifyByVwdcode(String vwdcode);

    /**
     * Identifies a quote by its InfrontID.
     *
     * @param symbol an InfrontID
     * @return a quote, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Quote identifyByInfrontId(String symbol);

    /**
     * Identifies a quote by its bis key.
     *
     * @param biskey bis key
     * @return a quote, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Quote identifyByBisKey(String biskey);

    /**
     * Identifies a quote by its MMWKN.
     *
     * @param mmwkn MMWKN
     * @return a quote, throws {@link de.marketmaker.istar.merger.provider.UnknownSymbolException}
     * if not found.
     */
    Quote identifyByMmwkn(String mmwkn);

    /**
     * Queries instruments or quotes according to criteria specified in the given search command. This
     * query supports results paging.
     * <p>
     * Usage:
     * <pre>
     * InstrumentProvider instrumentProvider = ...
     * ...
     * SimpleSearchCommand ssCmd = new SimpleSearchCommand("amazon", 0, 10, 50, true);
     * SearchResponse resp = instrumentProvider.simpleSearch(ssCmd);
     * ...
     * </pre>
     * For more information on building SimpleSearchCommand, please refer to {@link de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand}.
     *
     * @param command a SimpleSearchCommand.
     * @return a search response which if valid contains the search results a/o other info depending
     *         on the search command.
     * @see de.marketmaker.istar.merger.web.easytrade.block.SimpleSearchCommand
     * @see de.marketmaker.istar.instrument.search.SearchResponse
     */
    SearchResponse simpleSearch(SimpleSearchCommand command);

    /**
     * Returns suggestions for instruments a user may be looking for. Usually, this method will be
     * called with a very short query string (even 1 character is possible) and is expected to
     * return the "best" suggestions, usually the most "well known" securities.
     * <p>
     * The query string normally is a sub-string of an instrument name.
     *
     * @param query what the user has typed so far
     * @param limit number of results requested
     * @param strategy selects a specific order of the results (prefer german instruments,
     * prefer largest companies, ...); null selects default strategy. Values could be <tt>de, us</tt>
     * @return a list of instrument suggestions, may be empty
     * @see de.marketmaker.istar.domain.data.SuggestedInstrument
     */
    List<SuggestedInstrument> getSuggestions(String query, int limit, String strategy);

    /**
     * Used to identify invalid iids. If a service maintains data by iid but cannot be sure for which
     * iids Instrument objects are actually available from the InstrumentServer, this method should
     * be used to check the validity of the iids. Iid availability should never be checked with
     * search requests as those would take too much time.
     * @param iids List of iids to be validated
     * @return List of invalid iids
     */
    Collection<Long> validate(Collection<Long> iids);
}
