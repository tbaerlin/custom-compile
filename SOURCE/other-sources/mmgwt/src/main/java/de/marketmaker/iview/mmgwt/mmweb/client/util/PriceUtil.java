/*
 * PriceUtil.java
 *
 * Created on 08.05.2009 14:21:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.ContractPriceData;
import de.marketmaker.iview.dmxml.FundPriceData;
import de.marketmaker.iview.dmxml.HasPricedata;
import de.marketmaker.iview.dmxml.MSCPriceDatas;
import de.marketmaker.iview.dmxml.MSCPriceDatasElement;
import de.marketmaker.iview.dmxml.PriceData;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.Price;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceUtil {
    public static void update(HasPricedata e, Price p) {
        if (e.getPricedata() != null) {
            update(e.getPricedata(), p);
        }
        else if (e.getContractpricedata() != null) {
            update(e.getContractpricedata(), p);
        }
        else if (e.getFundpricedata() != null) {
            update(e.getFundpricedata(), p);
        }
    }

    private static void update(PriceData priceData, Price p) {
        priceData.setChangePercent(p.getChangePercent());
        priceData.setPrice(p.getLastPrice().getPrice());
        priceData.setDate(p.getDate());
    }

    private static void update(ContractPriceData priceData, Price p) {
        priceData.setChangePercent(p.getChangePercent());
        priceData.setPrice(p.getLastPrice().getPrice());
        priceData.setDate(p.getDate());
    }

    private static void update(FundPriceData priceData, Price p) {
        priceData.setChangePercent(p.getChangePercent());
        priceData.setRepurchasingprice(p.getLastPrice().getPrice());
        priceData.setDate(p.getDate());
    }

    public static boolean isEndOfDay(MSCPriceDatasElement data) {
        if (data == null) {
            return false;
        }
        if (data.getFundpricedata() != null) {
            return true;
        }
        if (data.getPricedata() == null) {
            return false;
        }
        return "END_OF_DAY".equals(data.getPricedata().getPriceQuality()); // $NON-NLS-0$
    }

    public static MSCPriceDatasElement getSelectedElement(DmxmlContext.Block<MSCPriceDatas> block) {
        if (!block.isResponseOk()) {
            return null;
        }
        return getSelectedElement(block.getResult());
    }

    public static MSCPriceDatasElement getSelectedElement(MSCPriceDatas datas) {
        for (MSCPriceDatasElement element : datas.getElement()) {
            if (element.isSelected()) {
                return element;
            }
        }
        return null;
    }


}
