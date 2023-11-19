/*
 * MasterDataStock.java
 *
 * Created on 12.07.2006 14:56:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MasterDataWarrant extends MasterData {
    long getInstrumentid();
    BigDecimal getIssuePrice();
    YearMonthDay getIssueDate();
    String getIssuerName();
    String getCurrencyStrike();
    Boolean getAmerican();
    LocalDate getFirstTradingDate();
    LocalDate getLastTradingDate();
}
