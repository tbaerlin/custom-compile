/*
 * PushOrderbook.java
 *
 * Created on 08.03.2010 15:01:00
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.io.Serializable;

/**
 * @author oflege
 */
public class PushOrderbook implements Serializable {
    private String vwdCode;
    private String[] bidPrices;
    private String[] bidVolumes;
    private String[] askPrices;
    private String[] askVolumes;
    private String date;

    public String getVwdCode() {
        return vwdCode;
    }

    public void setVwdCode(String vwdCode) {
        this.vwdCode = vwdCode;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String[] getAskVolumes() {
        return askVolumes;
    }

    public String[] getAskPrices() {
        return askPrices;
    }

    public String[] getBidVolumes() {
        return bidVolumes;
    }

    public String[] getBidPrices() {
        return bidPrices;
    }

    public void setBidPrices(String[] bidPrices) {
        this.bidPrices = bidPrices;
    }

    public void setBidVolumes(String[] bidVolumes) {
        this.bidVolumes = bidVolumes;
    }

    public void setAskPrices(String[] askPrices) {
        this.askPrices = askPrices;
    }

    public void setAskVolumes(String[] askVolumes) {
        this.askVolumes = askVolumes;
    }
}
