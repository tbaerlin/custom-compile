/*
 * QuotedefMapper.java
 *
 * Created on 24.02.12 07:37
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.domain.data.Price;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.PriceRecordFund;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.Constants;

/**
 * XML configuration based on export of
 *
 * select *
 * from quotedefpricetypename
 * order by quotedefid, pos
 *
 * AND
 *
 * MDP-View PRICE2VWDS
 * @author tkiesgen
 */
public class QuotedefMapper implements InitializingBean {
    private static final ClassPathResource RESOURCE = new ClassPathResource("/de/marketmaker/istar/merger/provider/historic/quotedefpricetype.xml");

    private static BigDecimal TWO = BigDecimal.valueOf(2);

    private final Logger logger = LoggerFactory.getLogger(getClass());

    static class QuotedefToPos {
        private final int quotedefid;

        private final String quotedef;

        private final Map<Integer, String> pos2pricetype = new TreeMap<>();
        private final Map<Integer, String> pos2field = new TreeMap<>();

        QuotedefToPos(int quotedefid, String quotedef) {
            this.quotedefid = quotedefid;
            this.quotedef = quotedef;
        }

        public void add(int position, String pricetype, String fieldname) {
            this.pos2pricetype.put(position, pricetype);
            this.pos2field.put(position, fieldname);
        }

        public String getFieldname(int position) {
            return this.pos2field.get(position);
        }

        public String getPricetype(int position) {
            return this.pos2pricetype.get(position);
        }

        @Override
        public String toString() {
            return "QuotedefToPos{" +
                    "quotedefid=" + quotedefid +
                    ", quotedef='" + quotedef + '\'' +
                    ", pos2pricetype=" + pos2pricetype +
                    ", pos2field=" + pos2field +
                    '}';
        }
    }

    private final Map<Integer, QuotedefToPos> quotedefs = new HashMap<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        final SAXBuilder builder = new SAXBuilder();
        final InputStream is = RESOURCE.getInputStream();
        final Document document = builder.build(is);
        is.close();

        @SuppressWarnings("unchecked")
        final List<Element> rows = document.getRootElement().getChildren("ROW");
        for (final Element row : rows) {
            @SuppressWarnings("unchecked")
            final List<Element> columns = row.getChildren("COLUMN");
            final int quotedefid = Integer.parseInt(getString(columns, "QUOTEDEFID"));
            final String quotedef = getString(columns, "QUOTEDEF");
            final String pricetype = getString(columns, "PRICETYPE");
            final String pricetypeADFName = getString(columns, "PRICETYPE_ADFNAME");
            final int pos = Integer.parseInt(getString(columns, "POS"));

            QuotedefToPos map = this.quotedefs.get(quotedefid);
            if (map == null) {
                map = new QuotedefToPos(quotedefid, quotedef);
                this.quotedefs.put(quotedefid, map);
            }

            map.add(pos, pricetype, pricetypeADFName);
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<afterPropertiesSet> quotedefs: " + quotedefs);
        }
    }

    private String getString(List<Element> elements, String name) {
        for (final Element item : elements) {
            if (name.equals(item.getAttributeValue("NAME"))) {
                return item.getTextTrim();
            }
        }
        return null;
    }

    public BigDecimal getKassa(int quotedef, PriceRecord pr) {
        if (quotedef == 3) {
            // funds
            return getKassaRaw(quotedef, pr);
        }

        return getSettlement(pr);
    }

    public BigDecimal getKassaRaw(int quotedef, PriceRecord pr) {
        final BigDecimal f2 = getPrice(quotedef, pr, 2);
        final BigDecimal f4 = getPrice(quotedef, pr, 4);
        final BigDecimal f5 = getPrice(quotedef, pr, 5);
        final BigDecimal f6 = getPrice(quotedef, pr, 6);
        final BigDecimal f7 = getPrice(quotedef, pr, 7);
        switch (quotedef) {
            case 1:
            case 22:
            case 123:
            case 322:
                return nvl(f5, f2);
            case 2:
            case 6:
            case 12:
            case 15:
            case 16:
            case 17:
            case 19:
            case 1122:
            case 1522:
                return f5;
            case 3:
                return nvl(f5, f4);
            case 4:
            case 1423:
            case 1922:
                if (f6 == null && f7 == null) {
                    return null;
                }
                return nvl(f6, f7).add(nvl(f7, f6)).divide(TWO, Constants.MC);
            case 8:
            case 13:
            case 21:
            case 922:
                return f2;
            case 1024:
                return price(pr.getSettlement());
            case 1322:
                return null;
        }

        return null;
    }

    public BigDecimal getOpen(int quotedef, PriceRecord pr) {
        final BigDecimal f1 = getPrice(quotedef, pr, 1);
        switch (quotedef) {
            case 1:
            case 2:
            case 3:
            case 5:
            case 8:
            case 15:
            case 17:
            case 21:
            case 22:
            case 123:
            case 322:
            case 722:
            case 1024:
            case 1822:
            case 1922:
            case 2022:
                return f1;
            case 1322:
                return null;
        }

        return null;
    }

    public BigDecimal getHigh(int quotedef, PriceRecord pr) {
        final BigDecimal f3 = getPrice(quotedef, pr, 3);
        final BigDecimal f7 = getPrice(quotedef, pr, 7);
        switch (quotedef) {
            case 1:
            case 2:
            case 3:
            case 5:
            case 8:
            case 15:
            case 17:
            case 22:
            case 123:
            case 322:
            case 722:
            case 1024:
            case 1822:
            case 2022:
                return f3;
            case 4:
            case 7:
            case 1423:
            case 1922:
                return f7;
            case 1322:
                return null;
        }

        return null;
    }

    public BigDecimal getLow(int quotedef, PriceRecord pr) {
        final BigDecimal f2 = getPrice(quotedef, pr, 2);
        final BigDecimal f4 = getPrice(quotedef, pr, 4);
        final BigDecimal f6 = getPrice(quotedef, pr, 6);
        switch (quotedef) {
            case 1:
            case 2:
            case 3:
            case 5:
            case 8:
            case 15:
            case 17:
            case 22:
            case 123:
            case 322:
            case 722:
            case 1024:
            case 1822:
            case 2022:
                return f4;
            case 4:
            case 7:
            case 1423:
            case 1922:
                return f6;
            case 1322:
                return null;
            case 1522:
                return f2;
        }

        return null;
    }

    public BigDecimal getClose(int quotedef, PriceRecord pr) {
        final BigDecimal close = getCloseRaw(quotedef, pr);

        if (quotedef == 3) {
            return close;
        }

        if (close != null && close.compareTo(BigDecimal.ZERO) != 0) {
            return close;
        }

        final BigDecimal kassa = getKassaRaw(quotedef, pr);
        if (kassa != null && kassa.compareTo(BigDecimal.ZERO) != 0) {
            return kassa;
        }

        return null;
    }

    public BigDecimal getCloseRaw(int quotedef, PriceRecord pr) {
        final BigDecimal f2 = getPrice(quotedef, pr, 2);
        final BigDecimal f3 = getPrice(quotedef, pr, 3);
        final BigDecimal f5 = getPrice(quotedef, pr, 5);
        final BigDecimal f8 = getPrice(quotedef, pr, 8);
        switch (quotedef) {
            case 1:
            case 2:
            case 5:
            case 8:
            case 13:
            case 15:
            case 17:
            case 22:
            case 123:
            case 322:
            case 423:
            case 722:
            case 1024:
            case 1423:
            case 1622:
            case 1722:
            case 1822:
            case 1922:
            case 2022:
                return f2;
            case 3:
                return f5;
            case 21:
                return f3;
            case 823:
            case 1322:
                return f8;
        }

        return null;
    }

    public BigDecimal getVolume(int quotedef, PriceRecord pr) {
        final BigDecimal f6 = getPrice(quotedef, pr, 6);
        final BigDecimal f8 = getPrice(quotedef, pr, 8);
        switch (quotedef) {
            case 1:
            case 2:
            case 5:
            case 8:
            case 22:
            case 123:
            case 322:
            case 722:
            case 1024:
            case 1822:
                return f6;
            case 3:
                return f8;
            case 1322:
                return null;
        }

        return null;
    }

    public BigDecimal getContracts(int quotedef, PriceRecord pr) {
        final BigDecimal f5 = getPrice(quotedef, pr, 5);
        final BigDecimal f7 = getPrice(quotedef, pr, 7);
        switch (quotedef) {
            case 1:
            case 8:
            case 123:
            case 322:
            case 722:
                return f7;
            case 1822:
                return f5;
            case 1024:
                return price(pr.getNumberOfTrades());
        }

        return null;
    }

    public BigDecimal getOpenInterest(int quotedef, PriceRecord pr) {
        final BigDecimal f4 = getPrice(quotedef, pr, 4);
        final BigDecimal f7 = getPrice(quotedef, pr, 7);
        switch (quotedef) {
            case 2:
                return f7;
            case 21:
                return f4;
            case 1322:
                return pr.getBidYield();
        }

        return price(pr.getYieldPrice());
    }

    private BigDecimal getSettlement(PriceRecord pr) {
        return price(pr.getSettlement());
    }

    private BigDecimal nvl(BigDecimal f1, BigDecimal f2) {
        return f1 != null ? f1 : f2;
    }

    private BigDecimal getPrice(int quotedef, PriceRecord pr, int position) {
        final QuotedefToPos quotedefToPos = getQuotedefToPos(quotedef);
        final String fieldname = getFieldname(position, quotedefToPos);

        if (!StringUtils.hasText(fieldname)) {
            return null;
        }

        return getPrice(pr, fieldname, quotedef, position);
    }

    private QuotedefToPos getQuotedefToPos(int quotedef) {
        final QuotedefToPos result = this.quotedefs.get(quotedef);
        if (result == null) {
            this.logger.error("<getQuotedefToPos> unknown quotedef " + quotedef + " => FIX");
            return this.quotedefs.get(1); // default
        }
        return result;
    }

    private String getFieldname(int position, QuotedefToPos quotedefToPos) {
        final String fieldname = quotedefToPos.getFieldname(position);

        if (StringUtils.hasText(fieldname)) {
            return fieldname;
        }

        if (position == 6 && "BOEGA UMSATZ".equals(quotedefToPos.getPricetype(position))) {
            // special handling for Dt. Börse-specific Boega Umsatz
            return "ADF_Umsatz_gesamt";
        }

        return null;
    }

    private BigDecimal getPrice(PriceRecord pr, String fieldname, int quotedef, int position) {
        if (!StringUtils.hasText(fieldname)) {
            final Long qid = pr instanceof PriceRecordVwd ? ((PriceRecordVwd) pr).getQuoteid() : null;
            this.logger.warn("<getPrice> unknown fieldname: '"
                    + fieldname + "' for quotedef/position " + quotedef + "/" + position
                    + (qid != null ? " for " + qid + ".qid" : ""));
            return null;
        }

        if ("ADF_Aktiengewinn".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Aktiengewinn);
        }
        if ("ADF_Anfang".equals(fieldname)) {
            return price(pr.getOpen());
        }
        if ("ADF_Anzahl_Handel".equals(fieldname)) {
            return price(pr.getNumberOfTrades());
        }
        if ("ADF_ATE".equals(fieldname)) {
            return null;
        }
        if ("ADF_Ausgabe".equals(fieldname)) {
            if (pr instanceof PriceRecordFund) {
                return price(((PriceRecordFund) pr).getIssuePrice());
            }
            return null;
        }
        if ("ADF_Bezahlt".equals(fieldname)) {
            return price(pr.getPrice());
        }
        if ("ADF_Brief".equals(fieldname)) {
            return price(pr.getAsk(), pr.getPreviousAsk());
        }
        if ("ADF_Brief_Tageshoch".equals(fieldname)) {
            return null;
        }
        /*  ADF_FAZ* are deprecated in
            fieldmap for release 17.01.0 at 21.01.2017
            we map to the new fieldname with the old FieldIds */
        if ("ADF_FAZ1".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_304);
        }
        if ("ADF_FAZ10".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_305);
        }
        if ("ADF_FAZ2".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_306);
        }
        if ("ADF_FAZ3".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_307);
        }
        if ("ADF_FAZ4".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_308);
        }
        if ("ADF_FAZ5".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_309);
        }
        if ("ADF_FAZ6".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_310);
        }
        if ("ADF_FAZ7".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_311);
        }
        if ("ADF_FAZ8".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_312);
        }
        if ("ADF_FAZ9".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_DUMMY_313);
        }
        if ("ADF_Geld".equals(fieldname)) {
            return price(pr.getBid(), pr.getPreviousBid());
        }
        if ("ADF_Geld_Tagestief".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Geld_Tagestief);
        }
        if ("ADF_Immobiliengewinn".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Immobiliengewinn);
        }
        if ("ADF_Imp_Vola_Brief".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Imp_Vola_Brief);
        }
        if ("ADF_Imp_Vola_Geld".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Imp_Vola_Geld);
        }
        if ("ADF_Kassa".equals(fieldname)) {
            return price(pr.getKassa());
        }
        if ("ADF_Mittelkurs".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Mittelkurs);
        }
        if ("ADF_NAV".equals(fieldname)) {
            if (pr instanceof PriceRecordFund) {
                return price(((PriceRecordFund) pr).getNetAssetValue());
            }
            return null;
        }
        if ("ADF_Open_Interest".equals(fieldname)) {
            return price(pr.getOpenInterest());
        }
        if ("ADF_Prozentuale_Veraenderung".equals(fieldname)) {
            return pr.getChangePercent();
        }
        if ("ADF_Rendite".equals(fieldname)) {
            return pr.getYield();
        }
        if ("ADF_Rendite_Geld".equals(fieldname)) {
            return pr.getBidYield();
        }
        if ("ADF_Ruecknahme".equals(fieldname)) {
            if (pr instanceof PriceRecordFund) {
                return price(((PriceRecordFund) pr).getRedemptionPrice());
            }
            return null;
        }
        if ("ADF_Schluss".equals(fieldname)) {
            return price(pr.getPrice());
        }
        if ("ADF_Schluss_Kurszusatz".equals(fieldname)) {
            return null;
        }
        if ("ADF_Settlement".equals(fieldname)) {
            return price(pr.getSettlement());
        }
        if ("ADF_Tageshoch".equals(fieldname)) {
            return price(pr.getHighDay());
        }
        if ("ADF_Tagestief".equals(fieldname)) {
            return price(pr.getLowDay());
        }
        if ("ADF_TID".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_TID);
        }
        if ("ADF_TIS".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_TIS);
        }
        if ("ADF_Umsatz_gesamt".equals(fieldname)) {
            return price(pr.getVolumeDay());
        }
        if ("ADF_Umsatz_gesamt_in_Whrg".equals(fieldname)) {
            return pr.getTurnoverDay();
        }
        if ("ADF_Volatility".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Volatility);
        }
        if ("ADF_Zwischengewinn".equals(fieldname)) {
            return price(pr, VwdFieldDescription.ADF_Zwischengewinn);
        }

        this.logger.warn("<getPrice> unmapped fieldname: '"
                + fieldname + "' for quotedef/position " + quotedef + "/" + position);

        return null;
    }

    private BigDecimal price(PriceRecord priceRecord, VwdFieldDescription.Field field) {
        if (!(priceRecord instanceof PriceRecordVwd)) {
            return null;
        }

        final PriceRecordVwd pr = (PriceRecordVwd) priceRecord;
        final SnapRecord sr = pr.getSnapRecord();

        if (sr == null) {
            return null;
        }

        final SnapField sf = sr.getField(field.id());
        return sf.isDefined() ? sf.getPrice() : null;
    }

    private BigDecimal price(final Long value) {
        return value == null ? null : new BigDecimal(value);
    }

    private BigDecimal price(final Price price) {
        return price == null ? null : price.getValue();
    }

    private BigDecimal price(final Price price, final Price previousPrice) {
        return price != null && price.getValue() != null
                ? price.getValue()
                : previousPrice != null ? previousPrice.getValue() : null;
    }


    public static void main(String[] args) throws Exception {
        final QuotedefMapper mapper = new QuotedefMapper();
        mapper.afterPropertiesSet();
    }
}
