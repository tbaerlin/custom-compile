/*
 * MasterDataStock.java
 *
 * Created on 12.07.2006 14:56:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface MasterDataStock extends MasterData {
    long getInstrumentid();

    BigDecimal getDividend();

    String getDividendCurrency();

    LocalDate getDividendExDay();

    BigDecimal getDividendLastYear();

    LocalizedString getSector();

    String getGicsSectorKey();

    String getGicsIndustryGroupKey();

    String getGicsIndustryKey();

    String getGicsSubIndustryKey();

    LocalizedString getGicsSector();

    LocalizedString getGicsIndustryGroup();

    LocalizedString getGicsIndustry();

    LocalizedString getGicsSubIndustry();

    String getMostLiquidMarket();

    MasterDataStock merge(MasterDataStock other);
}
