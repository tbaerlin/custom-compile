package de.marketmaker.iview.mmgwt.mmweb.client.util;

import java.util.Date;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * Created on 23.02.2010 14:16:34
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */
public class WatchlistPortfolioUtil {

    public static String getPrintHeadWithDate(String headText) {
        StringBuilder sb = new StringBuilder();
        final String today = Formatter.LF.formatDate(new Date());
        sb.append("<div ").append("class=\"mm-printHeader\"").append(">") // $NON-NLS$
                .append(headText).append(" ").append(I18n.I.toDativ()).append(" ").append(today)  // $NON-NLS$
                .append("</div>"); // $NON-NLS$
        return sb.toString();
    }

}
