/*
 * InstrumentAllocation.java
 *
 * Created on 12.08.2006 14:51:21
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.DateTime;

import java.math.BigDecimal;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface InstrumentAllocation {
    enum Type {
        ASSET,
        COUNTRY,
        EXCHANGE,
        DURATION,
        SECTOR,
        CURRENCY,
        REAL_ESTATE,
        BONDS,
        RATING,
        INSTRUMENT,
        FUNDMANAGEMENT_STRATEGY,
        RISK_COUNTRY,
        ISSUER,
        STANDARDIZED_COUNTRY,
        FUND,
        ASSET_COUNTRY,
        STANDARDIZED_SECTOR,
        EXPOSURE,
        NA,
        POOL,
        RISK,
        BASIC,
        THEME,
        GEOGRAPHICAL
    }

    public enum ShareType {
        CONSOLIDATED, STANDARDIZED
    }

    Long getId();

    String getCategory();

    BigDecimal getShare();

    Type getType();

    ShareType getShareType();

    BigDecimal getLongPosition();

    BigDecimal getShortPosition();

    String getIsin();

    String getElementCode();

    DateTime getLastUpdate();

    MasterDataFund.Source getSource();
}
