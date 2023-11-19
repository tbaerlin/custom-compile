/*
 * FieldProviderMBean.java
 *
 * Created on 13.01.2006 12:29:17
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface FieldProviderVwdMBean {
    /**
     * Returns array of fieldids for all allowed price fields
     */
    int[] getAllowedPriceFields();

    /**
     * Returns array of fieldids for all allowed static fields
     */
    int[] getAllowedStaticFields();
}
