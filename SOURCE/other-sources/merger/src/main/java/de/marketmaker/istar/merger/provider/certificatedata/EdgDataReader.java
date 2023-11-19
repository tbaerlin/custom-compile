/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domainimpl.data.EdgDataImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EdgDataReader extends AbstractSaxReader {
    private final static DateTimeFormatter DTF_DATE = DateTimeFormat.forPattern("dd.MM.yyyy");

    // scores (3 bits each, 0 as null, each value +1), order: edgScore1-5, topClass, ddvRiskclass10d

    private long instrumentid = -1;
    private String isin;
    private int edgRatingDate;
    private int scores;

    private int ddvDate;
    private double ddvVar10d;
    private double ddvPriceRisk10d;
    private double ddvInterestRisk10d;
    private double ddvCurrencyRisk10d;
    private double ddvIssuerRisk10d;
    private double ddvVolatilityRisk10d;
    private double ddvDiversificationRisk10d;
    private double ddvTimevalue10d;
    private double ddvVar250d;
    private double ddvPriceRisk250d;
    private double ddvInterestRisk250d;
    private double ddvCurrencyRisk250d;
    private double ddvIssuerRisk250d;
    private double ddvVolatilityRisk250d;
    private double ddvDiversificationRisk250d;
    private double ddvTimevalue250d;

    private InstrumentBasedUpdatable<EdgData> provider;

    public EdgDataReader(InstrumentBasedUpdatable<EdgData> provider) {
        this.provider = provider;
    }

    public void endElement(String uri, String localName, String tagName) throws SAXException {
        try {
            if (tagName.equals("ROW")) {
                // i have finished a new row => process
                storeFields();
            }
            else if (tagName.equals("IID")) {
                this.instrumentid = getCurrentLong();
            }
            else if (tagName.equals("ISIN")) {
                this.isin = getCurrentString();
            }
            else if (tagName.equals("EDGRATINGDATE")) {
                this.edgRatingDate = getDate();
            }
            else if (tagName.equals("DDVDATE")) {
                this.ddvDate = getDate();
            }
            else if (tagName.equals("EDGSCORE1")) {
                addScore(1);
            }
            else if (tagName.equals("EDGSCORE2")) {
                addScore(2);
            }
            else if (tagName.equals("EDGSCORE3")) {
                addScore(3);
            }
            else if (tagName.equals("EDGSCORE4")) {
                addScore(4);
            }
            else if (tagName.equals("EDGSCORE5")) {
                addScore(5);
            }
            else if (tagName.equals("EDGTOPCLASS")) {
                addScore(6);
            }
            else if (tagName.equals("DDVRISKCLASS10D")) {
                addScore(7);
            }
            else if (tagName.equals("DDVVAR10D")) {
                this.ddvVar10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVPRICERISK10D")) {
                this.ddvPriceRisk10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVINTERESTRISK10D")) {
                this.ddvInterestRisk10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVCURRENCYRISK10D")) {
                this.ddvCurrencyRisk10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVISSUERRISK10D")) {
                this.ddvIssuerRisk10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVVOLATILITYRISK10D")) {
                this.ddvVolatilityRisk10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVDIVERSIFICATIONRISK10D")) {
                this.ddvDiversificationRisk10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVTIMEVALUE10D")) {
                this.ddvTimevalue10d = getCurrentDouble();
            }
            else if (tagName.equals("DDVVAR250D")) {
                this.ddvVar250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVPRICERISK250D")) {
                this.ddvPriceRisk250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVINTERESTRISK250D")) {
                this.ddvInterestRisk250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVCURRENCYRISK250D")) {
                this.ddvCurrencyRisk250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVISSUERRISK250D")) {
                this.ddvIssuerRisk250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVVOLATILITYRISK250D")) {
                this.ddvVolatilityRisk250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVDIVERSIFICATIONRISK250D")) {
                this.ddvDiversificationRisk250d = getCurrentDouble();
            }
            else if (tagName.equals("DDVTIMEVALUE250D")) {
                this.ddvTimevalue250d = getCurrentDouble();
            }
            else if (tagName.equals("ROWS")) {
                //ignored
            }
            else {
                notParsed(tagName);
            }
        }
        catch (Exception e) {
            this.logger.error("<endElement> error in " + tagName, e);
            this.errorOccured = true;
        }
    }

    private void addScore(int index) {
        final int value = getCurrentInt();
        this.scores |= (value + 1) << (index * 3);
    }

    private int getDate() {
        final String s = getCurrentString(false);
        if (s == null || s.startsWith("31.12.9999")) {
            return 0;
        }
        return DateUtil.dateToYyyyMmDd(DTF_DATE.parseDateTime(s).toDate());
    }

    private void storeFields() {
        this.limiter.ackAction();

        if (this.errorOccured) {
            reset();
            return;
        }

        if (this.instrumentid < 0) {
            reset();
            return;
        }

        final EdgDataImpl data = new EdgDataImpl(instrumentid, isin, edgRatingDate, scores, ddvDate, ddvVar10d, ddvPriceRisk10d, ddvInterestRisk10d,
                ddvCurrencyRisk10d, ddvIssuerRisk10d, ddvVolatilityRisk10d, ddvDiversificationRisk10d, ddvTimevalue10d,
                ddvVar250d, ddvPriceRisk250d, ddvInterestRisk250d, ddvCurrencyRisk250d, ddvIssuerRisk250d, ddvVolatilityRisk250d,
                ddvDiversificationRisk250d, ddvTimevalue250d);
        this.provider.addOrReplace(this.instrumentid, data);

        reset();
    }

    protected void reset() {
        this.instrumentid = -1;

        this.isin=null;
        this.edgRatingDate = 0;
        this.scores = 0;

        this.ddvDate = 0;
        this.ddvVar10d = Double.NaN;
        this.ddvPriceRisk10d = Double.NaN;
        this.ddvInterestRisk10d = Double.NaN;
        this.ddvCurrencyRisk10d = Double.NaN;
        this.ddvIssuerRisk10d = Double.NaN;
        this.ddvVolatilityRisk10d = Double.NaN;
        this.ddvDiversificationRisk10d = Double.NaN;
        this.ddvTimevalue10d = Double.NaN;
        this.ddvVar250d = Double.NaN;
        this.ddvPriceRisk250d = Double.NaN;
        this.ddvInterestRisk250d = Double.NaN;
        this.ddvCurrencyRisk250d = Double.NaN;
        this.ddvIssuerRisk250d = Double.NaN;
        this.ddvVolatilityRisk250d = Double.NaN;
        this.ddvDiversificationRisk250d = Double.NaN;
        this.ddvTimevalue250d = Double.NaN;

        this.errorOccured = false;
    }

}