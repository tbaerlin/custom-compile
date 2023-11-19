/*
 * Research2Document.java
 *
 * Created on 19.03.14 10:43
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.gisresearch;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.NumericField;
import org.joda.time.DateTime;

import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.news.analysis.NewsAnalyzer;

import static de.marketmaker.istar.merger.provider.gisresearch.GisResearchIndexConstants.*;
import static org.joda.time.DateTimeConstants.MILLIS_PER_SECOND;

/**
 * @author oflege
 */
public class Research2Document {

    static final Analyzer ANALYZER = new NewsAnalyzer();

    public static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);

    static final String ALLOW_ALL_SELECTOR = "0";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Map<String, GisResearchIndexer.InstrumentInfo> isinToInstrument;

    Research2Document(Map<String, GisResearchIndexer.InstrumentInfo> isinToInstrument) {
        this.isinToInstrument = isinToInstrument;
    }

    Document toDocument(ControlFile cf, String text) throws IOException {
        Document d = new Document();
        d.add(new Field(FIELD_ID, cf.researchId, Field.Store.YES, Field.Index.NO));

        d.add(new NumericField(FIELD_DATE).setIntValue(
                encodeTimestamp(cf.publicationDate)));

        for (String entitlement : cf.productEntitlements) {
            Selector s = getSelector(entitlement);
            if (s != null) {
                d.add(keyword(FIELD_SELECTOR, Integer.toString(s.getId())));
            }
        }
        if (cf.productEntitlements.isEmpty() && cf.documentType == DocumentType.ATA_DAILY_KURZ) {
            d.add(keyword(FIELD_SELECTOR, ALLOW_ALL_SELECTOR));
        }

        for (String isin : cf.isins) {
            GisResearchIndexer.InstrumentInfo instrument = this.isinToInstrument.get(isin);
            if (instrument != null) {
                d.add(keyword(FIELD_ISIN, isin));
                d.add(new Field(FIELD_NAME, instrument.name, Field.Store.NO, Field.Index.ANALYZED));
                if (isin.equals(cf.getPrimaryIsin())) {
                    d.add(keyword(FIELD_PRIMARY_ISIN, isin));
                    d.add(new Field(FIELD_PRIMARY_NAME, instrument.name, Field.Store.NO, Field.Index.ANALYZED));
                    if (instrument.sector != null) {
                        d.add(keyword(FIELD_SECTOR, instrument.sector));
                    }
                }
            }
            d.add(new Field(FIELD_TEXT, isin, Field.Store.NO, Field.Index.ANALYZED));
        }

        d.add(keyword(FIELD_TYPE, cf.documentType.name()));
        if (cf.documentType.getAssetClass() != null) {
            d.add(keyword(FIELD_ASSET_CLASS, cf.documentType.getAssetClass()));
        }

        for (String number : cf.issuerNumbers()) {
            d.add(keyword(FIELD_ISSUER, number));
            if (number.equals(cf.getPrimaryIssuer().number)) {
                d.add(keyword(FIELD_PRIMARY_ISSUER, number));
            }
        }

        for (String country : cf.countries) {
            d.add(keyword(FIELD_COUNTRY, country));
        }
        if (cf.recommendation != null) {
            d.add(keyword(FIELD_RECOMM, cf.recommendation));
        }
        if (cf.riskGroup != null) {
            d.add(keyword(FIELD_RISK, cf.riskGroup));
        }

        d.add(new Field(FIELD_TITLE, cf.title, Field.Store.NO, Field.Index.ANALYZED));

        d.add(new Field(FIELD_TEXT, cf.title, Field.Store.NO, Field.Index.ANALYZED));
        d.add(new Field(FIELD_TEXT, text, Field.Store.NO, Field.Index.ANALYZED));
        return d;
    }

    private static Field keyword(String name, String value) {
        return new Field(name, value, Field.Store.NO, Field.Index.NOT_ANALYZED_NO_NORMS);
    }

    public static int encodeTimestamp(DateTime timestamp) {
        return (int) (timestamp.getMillis() / MILLIS_PER_SECOND);
    }

    private static Selector getSelector(String entitlement) {
        switch (entitlement) {
            case "HM1":
                return Selector.DZ_HM1;
            case "HM2":
                return Selector.DZ_HM2;
            case "HM3":
                return Selector.DZ_HM3;
            case "FP4":
                return Selector.DZ_FP4;
        }
        return null;
    }
}
