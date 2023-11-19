/*
 * CrossrateFinder.java
 *
 * Created on 24.08.2006 11:28:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.feed.vwd;

import de.marketmaker.istar.common.rmi.RmiProxyFactory;
import de.marketmaker.istar.common.rmi.RmiServiceDescriptor;
import de.marketmaker.istar.common.rmi.RmiServiceDescriptorEditor;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.api.IntradayRequest;
import de.marketmaker.istar.feed.api.IntradayResponse;
import de.marketmaker.istar.feed.api.IstarFeedConnector;
import de.marketmaker.istar.feed.api.FeedConnector;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CrossrateFinder {
    private final static String[] ISO_CODES = new String[]{
            "AED", "AUD", "BHD", "BND", "BOB", "BRL", "CAD",
            "CHF", "CLP", "CNY", "COP", "CYP", "CZK", "DKK", "DOP", "DZD", "ECS", "EEK", "EGP",
            "ETB", "EUR", "FJD", "GBP", "GHC", "HKD", "HUF", "IDR", "ILS", "INR",
            "ISK", "JMD", "JOD", "JPY", "KES", "KRW", "KWD", "LBP", "LKR", "LSL", "LTL",
            "LVL", "MAD", "MMK", "MTL", "MUR", "MWK", "MXN", "MYR", "MZN", "NGN", "NOK",
            "NZD", "OMR", "PEN", "PHP", "PLN", "PYG", "QAR", "RON", "RUB", "SAR", "SCR", "SEK",
            "SGD", "SIT", "SKK", "SZL", "THB", "TND", "TWD", "TZS",
            "UGX", "USD", "VEB", "VND", "VUV", "XAF", "YER", "ZAR", "ZMK", "ZWD"
    };

    private final static String[] NO_XRATES = new String[]{
            "ANG", "AON", "ARS", "BDT", "BGN", "BWP", "CRC", "GTQ", "HNL", "HRK",
            "KMF", "KZT", "NAD", "NIO", "PGK", "SVC", "THP", "TRY", "TTD", "UAH", "UYP"
    };


    private IstarFeedConnector feedConnector;

    public void setFeedConnector(IstarFeedConnector feedConnector) {
        this.feedConnector = feedConnector;
    }

    private void generate() {
        // print header
        for (final String isocode : ISO_CODES) {
            System.out.print(";" + isocode);
        }
        System.out.println();

        for (final String isocode1 : ISO_CODES) {
            System.out.print(isocode1);
            for (final String isocode2 : ISO_CODES) {
                System.out.print(";" + getFeedsymbol(isocode1, isocode2));
            }
            System.out.println();
        }
    }

    private String getFeedsymbol(String isocode1, String isocode2) {
        if (isocode1.equals(isocode2)) {
            return "-";
        }

        final String keyXrate = "10." + isocode1 + isocode2 + ".XRATE.SPOT";
        if (getFeedsymbol(keyXrate) != null) {
            return keyXrate;
        }

        if ("USD".equals(isocode1)) {
            final String keyUsd = "10." + isocode2 + ".FX";
            if (getFeedsymbol(keyUsd) != null) {
                return keyUsd;
            }
        }

        if ("USD".equals(isocode2)) {
            final String keyUsd = "10." + isocode1 + ".FX";
            if (getFeedsymbol(keyUsd) != null) {
                return keyUsd;
            }
        }

        return "";
    }

    private String getFeedsymbol(String vendorkey) {
        final IntradayRequest ir = new IntradayRequest();
        final IntradayRequest.Item ri = new IntradayRequest.Item(vendorkey, true);

        ir.add(ri);
        final IntradayResponse data = this.feedConnector.getIntradayData(ir);
        final IntradayResponse.Item item = data.getItem(vendorkey);
        if (item != null) {
            final SnapRecord sr = item.getPriceSnapRecord();
            if (sr.getField(VwdFieldDescription.ADF_Bezahlt.id()).isDefined()) {
                return vendorkey;
            }
        }
        return null;
    }

    public static void main(String[] args) throws Exception {
        final RmiServiceDescriptorEditor editor = new RmiServiceDescriptorEditor();

        editor.setAsText("1,rmi://techi1:1199/priceserver@techi1");
        final RmiProxyFactory intradayProxy = new RmiProxyFactory();
        intradayProxy.setRmiServices((RmiServiceDescriptor[]) editor.getValue());
        intradayProxy.setServiceInterface(FeedConnector.class);
        intradayProxy.afterPropertiesSet();

        final IstarFeedConnector feedconnector = new IstarFeedConnector();
        feedconnector.setChicagoServer((FeedConnector) intradayProxy.getObject());


        final CrossrateFinder cf = new CrossrateFinder();
        cf.setFeedConnector(feedconnector);
        cf.generate();
    }
}
