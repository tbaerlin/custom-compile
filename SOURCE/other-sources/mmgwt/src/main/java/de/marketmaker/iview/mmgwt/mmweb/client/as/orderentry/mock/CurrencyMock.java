/*
 * CurrencyMock.java
 *
 * Created on 12.08.13 17:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.mock;

import de.marketmaker.iview.pmxml.CurrencyAnnotated;
import de.marketmaker.iview.pmxml.OrderCurrency;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Markus Dick
 */
@NonNLS
public class CurrencyMock {
    public static final OrderCurrency EUR = new OrderCurrency();
    public static final CurrencyAnnotated EUR_CA = new CurrencyAnnotated();
    public static final CurrencyAnnotated EUR_CA_DEFAULT = new CurrencyAnnotated();
    public static final CurrencyAnnotated EUR_CA_EXT_INFO_DEFAULT = new CurrencyAnnotated();

    public static final OrderCurrency USD = new OrderCurrency();
    public static final CurrencyAnnotated USD_CA = new CurrencyAnnotated();

    public static final OrderCurrency HKD = new OrderCurrency();
    public static final CurrencyAnnotated HKD_CA = new CurrencyAnnotated();

    public static final OrderCurrency CHF = new OrderCurrency();
    public static final CurrencyAnnotated CHF_CA = new CurrencyAnnotated();

    public static final OrderCurrency XXE = new OrderCurrency();
    public static final CurrencyAnnotated XXE_CA = new CurrencyAnnotated();
    public static final CurrencyAnnotated XXE_CA_EXT_INFO_DEFAULT = new CurrencyAnnotated();

    public static final OrderCurrency AUD = new OrderCurrency();
    public static final CurrencyAnnotated AUD_CA = new CurrencyAnnotated();

    public static final OrderCurrency OOODEPWHR_ST = new OrderCurrency();
    public static final CurrencyAnnotated OOODEPWHR_ST_CA = new CurrencyAnnotated();

    static {
        EUR.setId("eur");
        EUR.setBez("Europäischer EURO");
        EUR.setKuerzel("EUR");
        EUR.setIsPMObject(true);
        EUR_CA.setCurrency(EUR);
        EUR_CA_DEFAULT.setCurrency(EUR);
        EUR_CA_DEFAULT.setIsDefault(true);
        EUR_CA_EXT_INFO_DEFAULT.setCurrency(EUR);
        EUR_CA_EXT_INFO_DEFAULT.setIsDefault(true);
        EUR_CA_EXT_INFO_DEFAULT.setExtInfo("This is an ExtInfo");

        USD.setId("usd");
        USD.setBez("US-Amerikanischer EURO");
        USD.setKuerzel("USD");
        USD_CA.setCurrency(USD);

        HKD.setId("hkd");
        HKD.setBez("Honkong-Dollar");
        HKD.setKuerzel("HKD");
        HKD_CA.setCurrency(HKD);

        CHF.setId("chf");
        CHF.setBez("Schweizerischer EURO");
        CHF.setKuerzel("CHF");
        CHF_CA.setCurrency(CHF);

        AUD.setId("aud");
        AUD.setBez("Australischer EURO");
        AUD.setKuerzel("AUD");
        AUD_CA.setCurrency(AUD);

        XXE.setId("951340");
        XXE.setBez("Antarktischer Euro");
        XXE.setKuerzel("XXE");
        XXE_CA.setCurrency(XXE);

        XXE_CA_EXT_INFO_DEFAULT.setCurrency(XXE);
        XXE_CA_EXT_INFO_DEFAULT.setExtInfo("This is an ExtInfo");
        XXE_CA_EXT_INFO_DEFAULT.setIsDefault(true);

        OOODEPWHR_ST.setId("0");
        OOODEPWHR_ST.setBez("DEPOT_WÄHRUNG_STÜCK");
        OOODEPWHR_ST.setKuerzel("ST");
        OOODEPWHR_ST_CA.setCurrency(OOODEPWHR_ST);
    }
}
