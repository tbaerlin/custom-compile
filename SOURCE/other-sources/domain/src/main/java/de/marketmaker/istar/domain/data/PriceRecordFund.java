/*
 * PriceRecordFund.java
 *
 * Created on 12.07.2006 10:33:53
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PriceRecordFund extends PriceRecord {

    Price getIssuePrice();

    Price getPreviousIssuePrice();

    Price getRedemptionPrice();

    Price getPreviousRedemptionPrice();

    Price getPreviousNetAssetValue();

    Price getNetAssetValue();
}
