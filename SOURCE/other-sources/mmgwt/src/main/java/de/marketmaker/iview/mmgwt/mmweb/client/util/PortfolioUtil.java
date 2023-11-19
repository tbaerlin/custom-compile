/*
 * PortfolioUtil.java
 *
 * Created on 17.03.2009 10:26:02
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

import de.marketmaker.iview.dmxml.PortfolioElement;
import de.marketmaker.iview.dmxml.WatchlistElement;

import java.util.List;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PortfolioUtil {
    public static boolean isWatchlistIdOk(List<WatchlistElement> list, String id) {
        if (list == null || list.isEmpty()) {
            return false;

        }
        for (WatchlistElement element : list) {
            if (element.getWatchlistid().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean watchlistsDiffer(List<WatchlistElement> list1, List<WatchlistElement> list2) {
        if (list1 == null && list2 == null) return false;
        if (list1 == null ||  list2 == null) return true;
        if (list1.size() != list2.size()) return true;
        for (int i = 0; i < list1.size(); i++) {
            final WatchlistElement we1 = list1.get(i);
            final WatchlistElement we2 = list2.get(i);
            if (!we1.getName().equals(we2.getName()) || !we1.getWatchlistid().equals(we2.getWatchlistid())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isPortfolioIdOk(List<PortfolioElement> list, String id) {
        for (PortfolioElement element : list) {
            if (element.getPortfolioid().equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean portfoliosDiffer(List<PortfolioElement> list1, List<PortfolioElement> list2) {
        if (list1 == null && list2 == null) return false;
        if (list1 == null ||  list2 == null) return true;
        if (list1.size() != list2.size()) return true;
        for (int i = 0; i < list1.size(); i++) {
            final PortfolioElement pe1 = list1.get(i);
            final PortfolioElement pe2 = list2.get(i);
            if (!pe1.getName().equals(pe2.getName()) || !pe1.getPortfolioid().equals(pe2.getPortfolioid())) {
                return true;
            }
        }
        return false;
    }
}
