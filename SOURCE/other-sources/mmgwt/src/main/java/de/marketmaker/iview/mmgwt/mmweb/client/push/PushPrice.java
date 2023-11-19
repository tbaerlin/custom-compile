/*
 * PushPrice.java
 *
 * Created on 04.02.2010 11:42:42
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.push;

import java.io.Serializable;

/**
 * @author Michael Lösch
 */
public class PushPrice implements Serializable {
    private String vwdCode;

    private String price;

    private String bid;

    private String ask;

    private String high;

    private String low;

    private Long askVolume;

    private Long bidVolume;

    private Long volumeDay;

    private String turnoverDay;

    private String date;

    private Long volumePrice;

    private Long numberOfTrades;

    private String supplement;

    private String bidAskDate;

    private String changeNet;

    private String changePercent;

    private String spreadNet;

    private String spreadPercent;

    private String open;

    private String close;

    private String previousClose;

    private String previousCloseDate;

    private String previousCloseSupplement;

    private String lowYear;

    private String highYear;

    private String lowDay;

    private String highDay;

    private Long previousVolumeDay;

    private String settlement;

    private String issueprice;

    private Long openInterest;

    private String interpolatedClosing;

    private String provisionalEvaluation;

    private String officialAsk;

    private String officialBid;

    private String unofficialAsk;

    private String unofficialBid;

    private String previousOfficialAsk;

    private String previousOfficialBid;

    private String previousUnofficialAsk;

    private String previousUnofficialBid;

    public String getPrice() {
        return price;
    }

    public void setPrice(String value) {
        this.price = value;
    }


    public String getVwdCode() {
        return vwdCode;
    }

    public void setVwdCode(String vwdCode) {
        this.vwdCode = vwdCode;
    }

    public String getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = bid;
    }

    public String getAsk() {
        return ask;
    }

    public void setAsk(String ask) {
        this.ask = ask;
    }

    public String getHigh() {
        return high;
    }

    public void setHigh(String high) {
        this.high = high;
    }

    public String getLow() {
        return low;
    }

    public void setLow(String low) {
        this.low = low;
    }

    public Long getAskVolume() {
        return askVolume;
    }

    public void setAskVolume(Long askVolume) {
        this.askVolume = askVolume;
    }

    public Long getBidVolume() {
        return bidVolume;
    }

    public void setBidVolume(Long bidVolume) {
        this.bidVolume = bidVolume;
    }

    public Long getVolumeDay() {
        return volumeDay;
    }

    public void setVolumeDay(Long volumeDay) {
        this.volumeDay = volumeDay;
    }

    public String getTurnoverDay() {
        return turnoverDay;
    }

    public void setTurnoverDay(String turnoverDay) {
        this.turnoverDay = turnoverDay;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDate() {
        return date;
    }

    public void setVolumePrice(Long volumePrice) {
        this.volumePrice = volumePrice;
    }

    public Long getVolumePrice() {
        return volumePrice;
    }

    public void setNumberOfTrades(Long numberOfTrades) {
        this.numberOfTrades = numberOfTrades;
    }

    public Long getNumberOfTrades() {
        return numberOfTrades;
    }

    public void setSupplement(String supplement) {
        this.supplement = supplement;
    }

    public String getSupplement() {
        return supplement;
    }

    public void setBidAskDate(String bidAskDate) {
        this.bidAskDate = bidAskDate;
    }

    public String getBidAskDate() {
        return bidAskDate;
    }

    public void setChangeNet(String changeNet) {
        this.changeNet = changeNet;
    }

    public String getChangeNet() {
        return changeNet;
    }

    public void setChangePercent(String changePercent) {
        this.changePercent = changePercent;
    }

    public String getChangePercent() {
        return changePercent;
    }

    public void setSpreadNet(String spreadNet) {
        this.spreadNet = spreadNet;
    }

    public String getSpreadNet() {
        return spreadNet;
    }

    public void setSpreadPercent(String spreadPercent) {
        this.spreadPercent = spreadPercent;
    }

    public String getSpreadPercent() {
        return spreadPercent;
    }

    public void setOpen(String open) {
        this.open = open;
    }

    public String getOpen() {
        return open;
    }

    public void setClose(String close) {
        this.close = close;
    }

    public String getClose() {
        return close;
    }

    public String getPreviousClose() {
        return previousClose;
    }

    public void setPreviousClose(String previousClose) {
        this.previousClose = previousClose;
    }

    public String getPreviousCloseDate() {
        return previousCloseDate;
    }

    public void setPreviousCloseDate(String previousCloseDate) {
        this.previousCloseDate = previousCloseDate;
    }

    public String getPreviousCloseSupplement() {
        return previousCloseSupplement;
    }

    public void setPreviousCloseSupplement(String previousCloseSupplement) {
        this.previousCloseSupplement = previousCloseSupplement;
    }

    public String getLowYear() {
        return lowYear;
    }

    public void setLowYear(String lowYear) {
        this.lowYear = lowYear;
    }

    public String getHighYear() {
        return highYear;
    }

    public void setHighYear(String highYear) {
        this.highYear = highYear;
    }

    public String getLowDay() {
        return lowDay;
    }

    public void setLowDay(String lowDay) {
        this.lowDay = lowDay;
    }

    public String getHighDay() {
        return highDay;
    }

    public void setHighDay(String highDay) {
        this.highDay = highDay;
    }

    public Long getPreviousVolumeDay() {
        return previousVolumeDay;
    }

    public void setPreviousVolumeDay(Long previousVolumeDay) {
        this.previousVolumeDay = previousVolumeDay;
    }

    public String getSettlement() {
        return settlement;
    }

    public void setSettlement(String settlement) {
        this.settlement = settlement;
    }

    public String getIssueprice() {
        return issueprice;
    }

    public void setIssueprice(String issueprice) {
        this.issueprice = issueprice;
    }

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public String getInterpolatedClosing() {
        return interpolatedClosing;
    }

    public void setInterpolatedClosing(String interpolatedClosing) {
        this.interpolatedClosing = interpolatedClosing;
    }

    public String getProvisionalEvaluation() {
        return provisionalEvaluation;
    }

    public void setProvisionalEvaluation(String provisionalEvaluation) {
        this.provisionalEvaluation = provisionalEvaluation;
    }

    public String getOfficialAsk() {
        return officialAsk;
    }

    public void setOfficialAsk(String officialAsk) {
        this.officialAsk = officialAsk;
    }

    public String getOfficialBid() {
        return officialBid;
    }

    public void setOfficialBid(String officialBid) {
        this.officialBid = officialBid;
    }

    public String getUnofficialAsk() {
        return unofficialAsk;
    }

    public void setUnofficialAsk(String unofficialAsk) {
        this.unofficialAsk = unofficialAsk;
    }

    public String getUnofficialBid() {
        return unofficialBid;
    }

    public void setUnofficialBid(String unofficialBid) {
        this.unofficialBid = unofficialBid;
    }

    public String getPreviousOfficialAsk() {
        return previousOfficialAsk;
    }

    public void setPreviousOfficialAsk(String previousOfficialAsk) {
        this.previousOfficialAsk = previousOfficialAsk;
    }

    public String getPreviousOfficialBid() {
        return previousOfficialBid;
    }

    public void setPreviousOfficialBid(String previousOfficialBid) {
        this.previousOfficialBid = previousOfficialBid;
    }

    public String getPreviousUnofficialAsk() {
        return previousUnofficialAsk;
    }

    public void setPreviousUnofficialAsk(String previousUnofficialAsk) {
        this.previousUnofficialAsk = previousUnofficialAsk;
    }

    public String getPreviousUnofficialBid() {
        return previousUnofficialBid;
    }

    public void setPreviousUnofficialBid(String getPreviousUnofficialBid) {
        this.previousUnofficialBid = getPreviousUnofficialBid;
    }
}
