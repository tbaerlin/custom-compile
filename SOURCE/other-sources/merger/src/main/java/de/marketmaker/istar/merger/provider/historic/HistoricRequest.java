/*
 * HistoricRequest.java
 *
 * Created on 08.08.13 17:21
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.joda.time.Period;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author zzhao
 */
public interface HistoricRequest {

    Quote getQuote();

    LocalDate getFrom();

    LocalDate getTo();

    boolean isAlignStartWithAggregationPeriod();

    boolean isAlignEndWithAggregationPeriod();

    Period getAggregationPeriod();

    boolean isWithSplit();

    boolean isWithDividend();

    /**
     * @return the date where price factor is ONE. Price factors before this date are applied
     * normally. Price factors after this date are reversed and then applied.
     */
    LocalDate getBaseDate();

    /**
     * @return the date till when corporate actions are queried and price factors are calculated.
     */
    LocalDate getCorporateActionReferenceDate();

    BigDecimal getFactor();

    String getCurrency();

    PriceRecord getPriceRecord();

    HistoricRequest withSplit(boolean kap);

    HistoricRequest withDividend(boolean div);

    @Deprecated
    HistoricRequest withFactor(BigDecimal factor);

    @Deprecated
    void addMmTalk(String mmtalk);

    @Deprecated
    void addBviPerformance(LocalDate referencedate);

    @Deprecated
    void addOpen(LocalDate basisdatum);

    @Deprecated
    void addHigh(LocalDate basisdatum);

    @Deprecated
    void addLow(LocalDate basisdatum);

    @Deprecated
    void addClose(LocalDate basisdatum);

    @Deprecated
    boolean isYieldBased();

    @Deprecated
    boolean isSettlementBased();

    @Deprecated
    void addVolume(LocalDate basisdatum);

    @Deprecated
    void addKassa(LocalDate basisdatum, Aggregation aggregation);

    @Deprecated
    void addOpenInterest(LocalDate basisdatum);

    @Deprecated
    void addKontrakt(LocalDate basisdatum);

}
