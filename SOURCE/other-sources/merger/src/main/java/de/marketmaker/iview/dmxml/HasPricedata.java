/*
 * WithPricedata.java
 *
 * Created on 26.03.2010 09:21:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.dmxml;

/**
 * @author oflege
 */
public interface HasPricedata extends HasQuote {
    String getPricedatatype();
    PriceData getPricedata();
    FundPriceData getFundpricedata();
    ContractPriceData getContractpricedata();
    LMEPriceData getLmepricedata();
}