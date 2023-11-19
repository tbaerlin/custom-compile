/*
 * CerFindersuchergebnis.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.util.ArraysUtil;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.merger.web.HttpRequestUtil;
import de.marketmaker.istar.merger.web.easytrade.ListCommand;
import de.marketmaker.istar.merger.web.easytrade.ListResult;
import de.marketmaker.istar.ratios.RatioFieldDescription;
import de.marketmaker.istar.ratios.frontend.DefaultRatioSearchResponse;
import de.marketmaker.istar.ratios.frontend.RatioSearchRequest;
import de.marketmaker.istar.ratios.frontend.RatioSearchResponse;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CerFindersuchergebnis extends AbstractFindersuchergebnis {
    private final static DateTimeFormatter DTF = DateTimeFormat.forPattern("yyyy-MM-dd");

    private final static DateTimeFormatter DTF_RATIOTOOL = DateTimeFormat.forPattern("dd.MM.yyyy");

    private boolean countTypes = false;

    public CerFindersuchergebnis() {
        super(Command.class);
    }

    public static class Command extends ListCommand {
        private String[] rawparam;

        private String[] searchstring;

        private String[] symbol;

        private String underlying;

        private String issuer;

        private String maturity;

        private String maturityMin;

        private String maturityMax;

        private String type;

        private String strikeMin;

        private String strikeMax;

        private String pricetype;

        private String couponMin;

        private String couponMax;

        private String capMin;

        private String capMax;

        private String capLevelMin;

        private String capLevelMax;

        private String knockinMin;

        private String knockinMax;

        private String bonuslevelMin;

        private String bonuslevelMax;

        private String barrierMin;

        private String barrierMax;

        private String discountMin;

        private String discountMax;

        private String guaranteetype;

        private String leveragetype;

        public String[] getRawparam() {
            return rawparam;
        }

        public void setRawparam(String[] rawparam) {
            this.rawparam = rawparam;
        }

        public String[] getSymbol() {
            return symbol;
        }

        public void setSymbol(String[] symbol) {
            this.symbol = HttpRequestUtil.filterParametersWithText(symbol);
        }

        public String[] getSearchstring() {
            return ArraysUtil.copyOf(searchstring);
        }

        public void setSearchstring(String[] searchstring) {
            this.searchstring = ArraysUtil.copyOf(searchstring);
        }

        public void setBasiswert(String underlying) {
            this.underlying = underlying;
        }

        public void setEmittent(String issuer) {
            this.issuer = issuer;
        }

        public void setFaelligkeit(String maturity) {
            this.maturity = maturity;
        }

        public void setFaelligkeitMin(String maturityMin) {
            this.maturityMin = maturityMin;
        }

        public void setFaelligkeitMax(String maturityMax) {
            this.maturityMax = maturityMax;
        }

        public void setZertifikatetyp(String type) {
            this.type = type;
        }

        public void setBasispreisMin(String strikeMin) {
            this.strikeMin = strikeMin;
        }

        public void setBasispreisMax(String strikeMax) {
            this.strikeMax = strikeMax;
        }

        public void setKurstyp(String pricetype) {
            this.pricetype = pricetype;
        }

        public void setKuponMin(String couponMin) {
            this.couponMin = couponMin;
        }

        public void setKuponMax(String couponMax) {
            this.couponMax = couponMax;
        }

        public String getCapMin() {
            return capMin;
        }

        public void setCapMin(String capMin) {
            this.capMin = capMin;
        }

        public String getCapMax() {
            return capMax;
        }

        public void setCapMax(String capMax) {
            this.capMax = capMax;
        }

        public String getCapLevelMin() {
            return capLevelMin;
        }

        public void setCapLevelMin(String capLevelMin) {
            this.capLevelMin = capLevelMin;
        }

        public String getCapLevelMax() {
            return capLevelMax;
        }

        public void setCapLevelMax(String capLevelMax) {
            this.capLevelMax = capLevelMax;
        }

        public String getKnockinMin() {
            return knockinMin;
        }

        public void setKnockinMin(String knockinMin) {
            this.knockinMin = knockinMin;
        }

        public String getKnockinMax() {
            return knockinMax;
        }

        public void setKnockinMax(String knockinMax) {
            this.knockinMax = knockinMax;
        }

        public String getBonuslevelMin() {
            return bonuslevelMin;
        }

        public void setBonuslevelMin(String bonuslevelMin) {
            this.bonuslevelMin = bonuslevelMin;
        }

        public String getBonuslevelMax() {
            return bonuslevelMax;
        }

        public void setBonuslevelMax(String bonuslevelMax) {
            this.bonuslevelMax = bonuslevelMax;
        }

        public void setBarriereMin(String barrierMin) {
            this.barrierMin = barrierMin;
        }


        public void setBarriereMax(String barrierMax) {
            this.barrierMax = barrierMax;
        }

        public String getDiscountMin() {
            return discountMin;
        }

        public void setDiscountMin(String discountMin) {
            this.discountMin = discountMin;
        }

        public String getDiscountMax() {
            return discountMax;
        }

        public void setDiscountMax(String discountMax) {
            this.discountMax = discountMax;
        }

        public void setGarantietyp(String guaranteetype) {
            this.guaranteetype = guaranteetype;
        }

        public void setHebeltyp(String leveragetype) {
            this.leveragetype = leveragetype;
        }

        public String getUnderlying() {
            return underlying;
        }

        public void setUnderlying(String underlying) {
            this.underlying = underlying;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getMaturity() {
            return maturity;
        }

        public void setMaturity(String maturity) {
            this.maturity = maturity;
        }

        public String getMaturityMin() {
            return maturityMin;
        }

        public void setMaturityMin(String maturityMin) {
            this.maturityMin = maturityMin;
        }

        public String getMaturityMax() {
            return maturityMax;
        }

        public void setMaturityMax(String maturityMax) {
            this.maturityMax = maturityMax;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getStrikeMin() {
            return strikeMin;
        }

        public void setStrikeMin(String strikeMin) {
            this.strikeMin = strikeMin;
        }

        public String getStrikeMax() {
            return strikeMax;
        }

        public void setStrikeMax(String strikeMax) {
            this.strikeMax = strikeMax;
        }

        public String getPricetype() {
            return pricetype;
        }

        public void setPricetype(String pricetype) {
            this.pricetype = pricetype;
        }

        public String getCouponMin() {
            return couponMin;
        }

        public void setCouponMin(String couponMin) {
            this.couponMin = couponMin;
        }

        public String getCouponMax() {
            return couponMax;
        }

        public void setCouponMax(String couponMax) {
            this.couponMax = couponMax;
        }

        public String getBarrierMin() {
            return barrierMin;
        }

        public void setBarrierMin(String barrierMin) {
            this.barrierMin = barrierMin;
        }

        public String getBarrierMax() {
            return barrierMax;
        }

        public void setBarrierMax(String barrierMax) {
            this.barrierMax = barrierMax;
        }

        public String getGuaranteetype() {
            return guaranteetype;
        }

        public void setGuaranteetype(String guaranteetype) {
            this.guaranteetype = guaranteetype;
        }

        public String getLeveragetype() {
            return leveragetype;
        }

        public void setLeveragetype(String leveragetype) {
            this.leveragetype = leveragetype;
        }
    }

    private static final Map<String, String> SORTFIELDS = new HashMap<>();
    static {
        SORTFIELDS.put("name", "postbankname");
        SORTFIELDS.put("nameBasiswert", "underlyingname");
        SORTFIELDS.put("emittent", "issuername");
        SORTFIELDS.put("faelligkeit", "expires");
        SORTFIELDS.put("volumen", "totalvolume");
        SORTFIELDS.put("performance3Monate", "performance3m");
    }

    private static Map<String, String> SORT_DMXML_CONVERSIONS = new HashMap<>();
    private static final List<String> QID_SORT_FIELDS_DMXML;

    static {
        SORT_DMXML_CONVERSIONS.put("name", "name");
        SORT_DMXML_CONVERSIONS.put("underlyingname", "underlying");
        SORT_DMXML_CONVERSIONS.put("faelligkeit", "maturity");
        SORT_DMXML_CONVERSIONS.put("volumen", "volume");
        SORT_DMXML_CONVERSIONS.put("datum", "date");
        SORT_DMXML_CONVERSIONS.put("performance3Monate", "performance3m");

        QID_SORT_FIELDS_DMXML = Collections.unmodifiableList(new ArrayList<>(SORT_DMXML_CONVERSIONS.values()));
    }


    public void setCountTypes(boolean countTypes) {
        this.countTypes = countTypes;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
                                    Object o, BindException errors) {
        final Command cmd = (Command) o;

        checkSortBy(cmd);

        final RatioSearchRequest rsr = createRequest(InstrumentTypeEnum.CER, cmd.getSearchstring(), null);

        if (cmd.getSymbol() != null) {
            rsr.setInstrumentIds(this.instrumentProvider.identifyInstrumentIds(cmd.getSymbol(), null));
        }

        if (this.countTypes) {
            rsr.setFieldidForResultCount(RatioFieldDescription.gatrixxType.id());
        }

        if (StringUtils.hasText(cmd.getType())) {
            if (this.countTypes) {
                rsr.setFilterForResultCount(cmd.getType());
            } else {
                rsr.addParameter("gatrixxtype", cmd.getType());
            }
        }

        rsr.addParameter("performance3m:U", "100000");

        addParameter(rsr, "underlyingisin@underlyingwkn@underlyingname", cmd.getUnderlying());
        addParameter(rsr, "issuername", cmd.getIssuer());
        if (addParameter(rsr, "expires:R", cmd.getMaturity())) {
            rsr.addParameter("expires:ISR", "true");
        } else {
            if (StringUtils.hasText(cmd.getMaturityMin())) {
                final String expiresL = getDateStr(cmd.getMaturityMin());
                if (StringUtils.hasText(expiresL)) {
                    rsr.addParameter("expires:L", expiresL);
                } else {
                    this.logger.warn("<doHandle> illegal faelligkeitMin: " + cmd.getMaturityMin());
                }
            }
            if (StringUtils.hasText(cmd.getMaturityMax())) {
                final String expiresU = getDateStr(cmd.getMaturityMax());
                if (StringUtils.hasText(expiresU)) {
                    rsr.addParameter("expires:U", expiresU);
                } else {
                    this.logger.warn("<doHandle> illegal faelligkeitMax: " + cmd.getMaturityMax());
                }
            }
        }

        addParameter(rsr, "gatrixxstrikeprice:L", cmd.getStrikeMin());
        addParameter(rsr, "gatrixxstrikeprice:U", cmd.getStrikeMax());

        addParameter(rsr, "gatrixxcoupon:L", cmd.getCouponMin());
        addParameter(rsr, "gatrixxcoupon:U", cmd.getCouponMax());

        addParameter(rsr, "gatrixxcap:L", cmd.getCapMin());
        addParameter(rsr, "gatrixxcap:U", cmd.getCapMax());

        addParameter(rsr, "caplevel:L", cmd.getCapLevelMin());
        addParameter(rsr, "caplevel:U", cmd.getCapLevelMax());

        addParameter(rsr, "gatrixxknockin:L", cmd.getKnockinMin());
        addParameter(rsr, "gatrixxknockin:U", cmd.getKnockinMax());

        addParameter(rsr, "gatrixxbonuslevel:L", cmd.getBonuslevelMin());
        addParameter(rsr, "gatrixxbonuslevel:U", cmd.getBonuslevelMax());

        addParameter(rsr, "gatrixxbarrier:L", cmd.getBarrierMin());
        addParameter(rsr, "gatrixxbarrier:U", cmd.getBarrierMax());

        addParameter(rsr, "discountpricerelative:L", cmd.getDiscountMin());
        addParameter(rsr, "discountpricerelative:U", cmd.getDiscountMax());

        addParameter(rsr, "gatrixxguaranteetype", cmd.getGuaranteetype());

        addParameter(rsr, "gatrixxleveragetype", cmd.getLeveragetype());


        if (StringUtils.hasText(cmd.getPricetype())) {
            if ("pari".equals(cmd.getPricetype())) {
                rsr.addParameter("lastprice", "100");
            } else if ("unterpari".equals(cmd.getPricetype())) {
                rsr.addParameter("lastprice:L", "100");
                rsr.addParameter("lastprice:IO", "false");
            } else if ("überpari".equals(cmd.getPricetype())) {
                rsr.addParameter("lastprice:U", "100");
                rsr.addParameter("lastprice:IO", "false");
            }
        }

        final ListResult listResult = ListResult.create(cmd, new ArrayList<>(SORTFIELDS.keySet()), "name", 0);

        final String sortedBy = listResult.getSortedBy();

        rsr.addParameter("i", Integer.toString(cmd.getOffset()));
        rsr.addParameter("n", Integer.toString(cmd.getAnzahl()));
        rsr.addParameter("sort1", SORTFIELDS.get(listResult.getSortedBy()));
        rsr.addParameter("sort1:D", Boolean.toString(!listResult.isAscending()));

        if (cmd.getRawparam() != null) {
            for (String s : cmd.getRawparam()) {
                final String[] strings = s.split("\\|");
                rsr.addParameter(strings[0], strings[1]);
            }
        }

        final RatioSearchResponse sr = this.ratiosProvider.search(rsr);

        if (!sr.isValid()) {
            errors.reject("ratios.searchfailed", "invalid search response");
            return null;
        }

        final Map<String, Object> model = createResultModel(InstrumentTypeEnum.CER, sr, listResult);

        final Map<Object, Integer> counts = ((DefaultRatioSearchResponse) sr).getResultGroupCount();
        final int aktienanleihenCount = getCount(counts, "AKTIENANLEIHE");
        final int sprinterCount = getCount(counts, "SPRINTER");
        final int bonusCount = getCount(counts, "BONUS");
        final int discountCount = getCount(counts, "DISCOUNT");
        final int knockoutCount = getCount(counts, "KNOCKOUT");
        final int indexCount = getCount(counts, "INDEX");
        final int garantieCount = getCount(counts, "GARANTIE");
//        final int sonstigCount = drsr.getNumTotal() - aktienanleihenCount - sprinterCount - bonusCount - discountCount - knockoutCount - indexCount;
        final int sonstigCount = getCount(counts, "SONSTIG");

        model.put("aktienanleihenCount", aktienanleihenCount);
        model.put("sprinterCount", sprinterCount);
        model.put("bonusCount", bonusCount);
        model.put("discountCount", discountCount);
        model.put("knockoutCount", knockoutCount);
        model.put("indexCount", indexCount);
        model.put("garantieCount", garantieCount);
        model.put("sonstigCount", sonstigCount);

        model.put("dmxmlsortedby", SORT_DMXML_CONVERSIONS.get(sortedBy));
        model.put("dmxmlfields", QID_SORT_FIELDS_DMXML);

        return new ModelAndView("cerfindersuchergebnis", model);
    }

    private int getCount(Map<Object, Integer> counts, String key) {
        if (counts == null) {
            return 0;
        }
        final Integer count = counts.get(key);
        if (count == null) {
            return 0;
        }
        return count;
    }

    private String getDateStr(String dateXml) {
        if (!StringUtils.hasText(dateXml)) {
            return null;
        }
        return parseAndFormat(dateXml, DTF_RATIOTOOL, DTF, DTF_RATIOTOOL);
    }

    private String parseAndFormat(String s, DateTimeFormatter outputFormat,
                                  DateTimeFormatter... inputFormats) {
        for (DateTimeFormatter dateTimeFormatter : inputFormats) {
            final DateTime dateTime;
            try {
                dateTime = dateTimeFormatter.parseDateTime(s);
            } catch (IllegalArgumentException e) {
                continue;
            }
            return outputFormat.print(dateTime);
        }
        return null;
    }

    /**
     * helper for converting possibly english sorting column names (from dmxml) to postbank slang german column names.
     */
    private void checkSortBy(Command cmd) {
        if (SORT_DMXML_CONVERSIONS.containsKey(cmd.getSortBy())) {
            return;
        }

        for (final Map.Entry<String, String> entry : SORT_DMXML_CONVERSIONS.entrySet()) {
            if (entry.getValue().equals(cmd.getSortBy())) {
                cmd.setSortBy(entry.getKey());
                return;
            }
        }
    }
}
