/*
 * DmxmlDataUtil.java
 *
 * Created on 12.06.2008 15:39:20
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Ulrich Maurer
 */
public class DmxmlDataUtil {
    public enum FundDistributionStrategy {
        DISTRIBUTING, RETAINING, INVALID

    }

    public static FundDistributionStrategy getFundDistributionStrategy(String strategy) {
        if (strategy == null) {
            return FundDistributionStrategy.INVALID;
        }
        else if (strategy.startsWith("aus")) { // $NON-NLS-0$
            return FundDistributionStrategy.DISTRIBUTING;
        }
        else if (strategy.startsWith("thes")) { // $NON-NLS-0$
            return FundDistributionStrategy.RETAINING;
        }
        return FundDistributionStrategy.INVALID;
    }
}
