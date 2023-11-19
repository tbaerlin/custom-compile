/*
 * PushPriceFactory.java
 *
 * Created on 10.02.2010 08:52:37
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.server.push;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.iview.mmgwt.mmweb.client.prices.PriceDataType;
import de.marketmaker.iview.mmgwt.mmweb.client.push.PushPrice;

/**
 * @author oflege
 */
class PushPriceFactory {
    private static final Log logger = LogFactory.getLog(PushPriceFactory.class);

    private static final DecimalFormat DF = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        DF.applyLocalizedPattern("0.#####");
    }

    private PushPriceFactory() {
    }

    static PushPrice create(PriceRecord pr, PriceDataType type) {
        switch (type) {
            case CONTRACT_EXCHANGE:
                return contractPrice(pr);
            case FUND_OTC:
                return fundPrice((PriceRecordFund) pr);
            case LME:
                return lmePrice(pr);
            default:
                return standardPrice(pr);
        }
    }

    private static PushPrice standardPrice(PriceRecord pr) {
        final PushPrice pp = new PushPrice();
        pp.setDate(datetime(pr.getPrice().getDate()));
        pp.setPrice(price(pr.getPrice().getValue()));
        pp.setVolumePrice(pr.getPrice().getVolume());
        pp.setVolumeDay(pr.getVolumeDay());
        pp.setTurnoverDay(price(pr.getTurnoverDay()));
        pp.setNumberOfTrades(pr.getNumberOfTrades());
        pp.setSupplement(pr.getPrice().getSupplement());
        pp.setBidAskDate(datetime(pr.getBid().getDate()));
        pp.setBid(price(pr.getBid().getValue()));
        pp.setAsk(price(pr.getAsk().getValue()));
        pp.setBidVolume(pr.getBid().getVolume());
        pp.setAskVolume(pr.getAsk().getVolume());
        pp.setChangeNet(price(pr.getChangeNet()));
        pp.setChangePercent(price(pr.getChangePercent()));
        pp.setSpreadNet(price(pr.getSpreadNet()));
        pp.setSpreadPercent(price(pr.getSpreadPercent()));
        pp.setOpen(price(pr.getOpen().getValue()));
        pp.setClose(price(pr.getClose().getValue()));
        pp.setPreviousClose(price(pr.getPreviousClose().getValue()));
        pp.setPreviousCloseDate(datetime(pr.getPreviousClose().getDate()));
        pp.setPreviousCloseSupplement(pr.getPreviousClose().getSupplement());
        pp.setPreviousVolumeDay(pr.getPreviousVolumeDay());
        pp.setHighDay(price(pr.getHighDay().getValue()));
        pp.setLowDay(price(pr.getLowDay().getValue()));
        pp.setHighYear(price(pr.getHighYear().getValue()));
        pp.setLowYear(price(pr.getLowYear().getValue()));
        return pp;
    }

    private static PushPrice fundPrice(PriceRecordFund pr) {
        final PushPrice pp = new PushPrice();
        pp.setDate(datetime(pr.getPrice().getDate()));
        pp.setPrice(price(pr.getRedemptionPrice().getValue())); // repurchasingprice
        pp.setPreviousClose(price(pr.getPreviousRedemptionPrice().getValue()));
        pp.setPreviousCloseDate(datetime(pr.getPreviousRedemptionPrice().getDate()));
        pp.setIssueprice(price(pr.getIssuePrice().getValue()));
        pp.setChangeNet(price(pr.getChangeNet()));
        pp.setChangePercent(price(pr.getChangePercent()));
        pp.setHighYear(price(pr.getHighYear().getValue()));
        pp.setLowYear(price(pr.getLowYear().getValue()));
        return pp;
    }

    private static PushPrice contractPrice(PriceRecord pr) {
        final PushPrice pp = standardPrice(pr);
        pp.setOpenInterest(pr.getOpenInterest().getVolume());
        pp.setSettlement(price(pr.getSettlement().getValue()));
        return pp;
    }

    private static PushPrice lmePrice(PriceRecord pr) {
        final PushPrice pp = standardPrice(pr);
        pp.setInterpolatedClosing(price(pr.getInterpolatedClosing()));
        pp.setProvisionalEvaluation(price(pr.getProvisionalEvaluation()));
        pp.setOfficialAsk(price(pr.getOfficialAsk().getValue()));
        pp.setOfficialBid(price(pr.getOfficialBid().getValue()));
        pp.setUnofficialAsk(price(pr.getUnofficialAsk().getValue()));
        pp.setUnofficialBid(price(pr.getUnofficialBid().getValue()));
        pp.setPreviousOfficialAsk(price(pr.getPreviousOfficialAsk().getValue()));
        pp.setPreviousOfficialBid(price(pr.getPreviousOfficialBid().getValue()));
        pp.setPreviousUnofficialAsk(price(pr.getPreviousUnofficialAsk().getValue()));
        pp.setPreviousUnofficialBid(price(pr.getPreviousUnofficialBid().getValue()));
        return pp;
    }
    
    private static String price(BigDecimal bd) {
        if (bd == null) {
            return null;
        }
        synchronized (DF) {
            return DF.format(bd);
        }
    }

    private static String datetime(DateTime dt) {
        if (dt == null) {
            return null;
        }
        return ISODateTimeFormat.dateTimeNoMillis().print(dt);
    }

    public static PushPrice createDiff(PushPrice p1, PushPrice p2, boolean b) {
        if (p2 == null) {
            return p1;
        }
        final StringBuilder sb = b && logger.isDebugEnabled() ? new StringBuilder(100) : null;
        final PushPrice result = new PushPrice();
        result.setVwdCode(p1.getVwdCode());
        if (isDifferent(p1.getPrice(), p2.getPrice())) {
            if (sb != null) sb.append(" ,price=").append(p1.getPrice());
            result.setPrice(p1.getPrice());
        }
        if (isDifferent(p1.getDate(), p2.getDate())) {
            if (sb != null) sb.append(" ,date=").append(p1.getDate());
            result.setDate(p1.getDate());
        }
        if (isDifferent(p1.getVolumePrice(), p2.getVolumePrice())) {
            if (sb != null) sb.append(" ,volP=").append(p1.getVolumePrice());
            result.setVolumePrice(p1.getVolumePrice());
        }
        if (isDifferent(p1.getSupplement(), p2.getSupplement())) {
            if (sb != null) sb.append(" ,supp=").append(p1.getSupplement());
            result.setSupplement(p1.getSupplement());
        }
        if (isDifferent(p1.getVolumeDay(), p2.getVolumeDay())) {
            if (sb != null) sb.append(" ,volD=").append(p1.getVolumeDay());
            result.setVolumeDay(p1.getVolumeDay());
        }
        if (isDifferent(p1.getPreviousVolumeDay(), p2.getPreviousVolumeDay())) {
            if (sb != null) sb.append(" ,pvolD=").append(p1.getPreviousVolumeDay());
            result.setPreviousVolumeDay(p1.getPreviousVolumeDay());
        }
        if (isDifferent(p1.getTurnoverDay(), p2.getTurnoverDay())) {
            if (sb != null) sb.append(" ,turnover=").append(p1.getTurnoverDay());
            result.setTurnoverDay(p1.getTurnoverDay());
        }
        if (isDifferent(p1.getNumberOfTrades(), p2.getNumberOfTrades())) {
            if (sb != null) sb.append(" ,numT=").append(p1.getNumberOfTrades());
            result.setNumberOfTrades(p1.getNumberOfTrades());
        }
        if (isDifferent(p1.getBid(), p2.getBid())) {
            if (sb != null) sb.append(" ,bid=").append(p1.getBid());
            result.setBid(p1.getBid());
        }
        if (isDifferent(p1.getBidAskDate(), p2.getBidAskDate())) {
            if (sb != null) sb.append(" ,baD=").append(p1.getBidAskDate());
            result.setBidAskDate(p1.getBidAskDate());
        }
        if (isDifferent(p1.getAsk(), p2.getAsk())) {
            if (sb != null) sb.append(" ,ask=").append(p1.getAsk());
            result.setAsk(p1.getAsk());
        }
        if (isDifferent(p1.getBidVolume(), p2.getBidVolume())) {
            if (sb != null) sb.append(" ,#bid=").append(p1.getBidVolume());
            result.setBidVolume(p1.getBidVolume());
        }
        if (isDifferent(p1.getAskVolume(), p2.getAskVolume())) {
            if (sb != null) sb.append(" ,#ask=").append(p1.getAskVolume());
            result.setAskVolume(p1.getAskVolume());
        }
        if (isDifferent(p1.getChangeNet(), p2.getChangeNet())) {
            if (sb != null) sb.append(" ,chg=").append(p1.getChangeNet());
            result.setChangeNet(p1.getChangeNet());
        }
        if (isDifferent(p1.getChangePercent(), p2.getChangePercent())) {
            if (sb != null) sb.append(" ,chg%=").append(p1.getChangePercent());
            result.setChangePercent(p1.getChangePercent());
        }
        if (isDifferent(p1.getSpreadNet(), p2.getSpreadNet())) {
            if (sb != null) sb.append(" ,spred=").append(p1.getSpreadNet());
            result.setSpreadNet(p1.getSpreadNet());
        }
        if (isDifferent(p1.getSpreadPercent(), p2.getSpreadPercent())) {
            if (sb != null) sb.append(" ,spread%=").append(p1.getSpreadPercent());
            result.setSpreadPercent(p1.getSpreadPercent());
        }
        if (isDifferent(p1.getOpen(), p2.getOpen())) {
            if (sb != null) sb.append(" ,open=").append(p1.getOpen());
            result.setOpen(p1.getOpen());
        }
        if (isDifferent(p1.getClose(), p2.getClose())) {
            if (sb != null) sb.append(" ,close=").append(p1.getClose());
            result.setClose(p1.getClose());
        }
        if (isDifferent(p1.getPreviousClose(), p2.getPreviousClose())) {
            if (sb != null) sb.append(" ,pClose=").append(p1.getPreviousClose());
            result.setPreviousClose(p1.getPreviousClose());
        }
        if (isDifferent(p1.getPreviousCloseDate(), p2.getPreviousCloseDate())) {
            if (sb != null) sb.append(" ,pCloseDate=").append(p1.getPreviousCloseDate());
            result.setPreviousCloseDate(p1.getPreviousCloseDate());
        }
        if (isDifferent(p1.getPreviousCloseSupplement(), p2.getPreviousCloseSupplement())) {
            if (sb != null) sb.append(" ,pCloseSupp=").append(p1.getPreviousCloseSupplement());
            result.setPreviousCloseSupplement(p1.getPreviousCloseSupplement());
        }
        if (isDifferent(p1.getHighDay(), p2.getHighDay())) {
            if (sb != null) sb.append(" ,hDay=").append(p1.getHighDay());
            result.setHighDay(p1.getHighDay());
        }
        if (isDifferent(p1.getLowDay(), p2.getLowDay())) {
            if (sb != null) sb.append(" ,lDay=").append(p1.getLowDay());
            result.setLowDay(p1.getLowDay());
        }
        if (isDifferent(p1.getHighYear(), p2.getHighYear())) {
            if (sb != null) sb.append(" ,hYear=").append(p1.getHighYear());
            result.setHighYear(p1.getHighYear());
        }
        if (isDifferent(p1.getLowYear(), p2.getLowYear())) {
            if (sb != null) sb.append(" ,lYear=").append(p1.getLowYear());
            result.setLowYear(p1.getLowYear());
        }
        if (isDifferent(p1.getIssueprice(), p2.getIssueprice())) {
            if (sb != null) sb.append(" ,issue=").append(p1.getIssueprice());
            result.setIssueprice(p1.getIssueprice());
        }
        if (isDifferent(p1.getOpenInterest(), p2.getOpenInterest())) {
            if (sb != null) sb.append(" ,openIn=").append(p1.getOpenInterest());
            result.setOpenInterest(p1.getOpenInterest());
        }
        if (isDifferent(p1.getSettlement(), p2.getSettlement())) {
            if (sb != null) sb.append(" ,settle=").append(p1.getSettlement());
            result.setSettlement(p1.getSettlement());
        }
        if (sb != null) {
            logger.debug("diff: " + sb.toString());
        }
        return result;
    }

    private static boolean isDifferent(final Object o1, final Object o2) {
        return o1 == null ? (o2 != null) : !o1.equals(o2);
    }
}
