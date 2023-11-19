/*
* CurrentTrendBar.java
*
* Created on 27.08.2008 11:50:45
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.data;

/**
 * @author Michael Lösch
 */
public class CurrentTrendBar {

    private final String changeOfCurrentPosition;
    private final TrendBarData trendBarData;

    public CurrentTrendBar(String changeOfCurrentPosition, TrendBarData trendBarData) {
        this.changeOfCurrentPosition = changeOfCurrentPosition;
        this.trendBarData = trendBarData;
    }

    public String getNegativeSideWidth() {
        return trendBarData.getNegativeSideWidth();
    }

    public String getPositiveSideWidth() {
        return trendBarData.getPositiveSideWidth();
    }

    public String getNegativeWidth() {
        return trendBarData.getNegativeWidth(this.changeOfCurrentPosition);
    }

    public String getPositiveWidth() {
        return trendBarData.getPositiveWidth(this.changeOfCurrentPosition);        
    }

    public boolean isValid() {
        return trendBarData.isValid(this.changeOfCurrentPosition);
    }

}
