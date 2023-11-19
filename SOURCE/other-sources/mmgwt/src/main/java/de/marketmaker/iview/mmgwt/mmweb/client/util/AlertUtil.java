/*
 * AlertUtil.java
 *
 * Created on 05.10.2009 11:38:31
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import de.marketmaker.iview.dmxml.MSCPriceData;
import de.marketmaker.iview.dmxml.QuoteData;
import de.marketmaker.iview.dmxml.Alert;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class AlertUtil {

    public static Set<String> FUND_MARKETS = new HashSet<>(Arrays.asList("FONDS", "FONDIT")); // $NON-NLS$

    public static final int ADF_Ruecknahme = 414;

    public static final int ADF_Brief = 28;

    public static final int ADF_Geld = 30;

    public static final int ADF_Bezahlt = 80;

    public static final int ADF_Interpo_Closing = 235;

    public static final int ADF_Prov_Evaluation = 236;

    public static final int ADF_Official_Ask = 1168;

    public static final int ADF_Official_Bid = 1169;

    public static final int ADF_Unofficial_Ask = 1170;

    public static final int ADF_Unofficial_Bid = 1171;

    public static final Comparator<Alert> COMPARE_BY_NAME = new Comparator<Alert>() {
        public int compare(Alert o1, Alert o2) {
            return o1.getName().compareToIgnoreCase(o2.getName());
        }
    };

    public static final Comparator<Alert> COMPARE_BY_DATE = new Comparator<Alert>() {
        public int compare(Alert o1, Alert o2) {
            return o1.getCreated().compareTo(o2.getCreated());
        }
    };

    public static final Comparator<Alert> COMPARE_BY_INSTRUMENTNAME_AND_DATE = new Comparator<Alert>() {
        public int compare(Alert o1, Alert o2) {
            final int p = o1.getInstrumentdata().getName().compareTo(o2.getInstrumentdata().getName());
            if (p != 0) {
                return p;
            }
            return o1.getCreated().compareTo(o2.getCreated());
        }
    };

    public static int getDefaultFieldId(QuoteData qd) {
        if (FUND_MARKETS.contains(qd.getMarketVwd())) {
            return ADF_Ruecknahme;
        }
        return ADF_Bezahlt;
    }

    public static String getLimitFieldName(Alert a) {
        return getLimitFieldName(a.getFieldId());        
    }

    public static String getLimitFieldName(int n) {
        switch (n) {
            case ADF_Brief:
                return I18n.I.ask(); 
            case ADF_Geld:
                return I18n.I.bid(); 
            case ADF_Bezahlt:
                return I18n.I.paid(); 
            case ADF_Ruecknahme:
                return I18n.I.redemption();
            case ADF_Interpo_Closing:
                return I18n.I.interpoClosing();
            case ADF_Prov_Evaluation:
                return I18n.I.provEvaluation();
            case ADF_Official_Ask:
                return I18n.I.ask() + I18n.I.officialSuffix();
            case ADF_Official_Bid:
                return I18n.I.bid() + I18n.I.officialSuffix();
            case ADF_Unofficial_Ask:
                return I18n.I.ask() + I18n.I.unOfficialSuffix();
            case ADF_Unofficial_Bid:
                return I18n.I.bid() + I18n.I.unOfficialSuffix();
            default:
                return "???"; // $NON-NLS-0$
        }
    }

    public static String pickFieldValue(int n, MSCPriceData result) {
        final Price price = Price.create(result);

        switch (n) {
            case AlertUtil.ADF_Brief:
                return getPriceValue(price.getAsk());
            case AlertUtil.ADF_Geld:
                return getPriceValue(price.getBid());
            case AlertUtil.ADF_Ruecknahme:
                return getPriceValue(price.getLastPrice().getPrice());
            case AlertUtil.ADF_Bezahlt:
                return getPriceValue(price.getLastPrice().getPrice());
            case AlertUtil.ADF_Official_Ask:
                return getPriceValue(result.getLmepricedata().getOfficialAsk());
            case AlertUtil.ADF_Official_Bid:
                return getPriceValue(result.getLmepricedata().getOfficialAsk());
            case AlertUtil.ADF_Unofficial_Ask:
                return getPriceValue(result.getLmepricedata().getUnofficialAsk());
            case AlertUtil.ADF_Unofficial_Bid:
                return getPriceValue(result.getLmepricedata().getUnofficialAsk());
            default:
                return "";
        }
    }

    private static String getPriceValue(final String price) {
        if (price == null) {
            return ""; // $NON-NLS-0$
        }
        return Renderer.PRICE.render(price);
    }

}
