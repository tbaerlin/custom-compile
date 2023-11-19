/*
 * EstimatesFields.java
 *
 * Created on 10.09.2008 07:15:55
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.Interval;
import org.joda.time.LocalDate;

import de.marketmaker.istar.domainimpl.data.StockRevenueSummaryImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class EstimatesFields {
    enum Fieldname {
        EPS("Earning per Share"), EPS_STDDEV("Earning per Share, Standard-Abweichung"),
        EBG("EPS before Goodwill"), EAG("EPS after Goodwill"),
        BVPS("Book Value per Share"), CFPS("Cashflow per Share"), NET_DIVIDEND("Dividende"),
        EBIT("EBIT"), EBITDA("EBITDA"), NET_DEBT("Net Debt"), NET_PROFIT("Net Profit"),
        P_E("Price Earning Ratio"), NET_DIV_YIELD("Dividendenrendite"),
        GOODWILL("Goodwill"), PRE_TAX_PROFIT("Pre Tax Profit"), SALES("Sales"),
        POST_EV__EPS("Post Event Consensus"),
        POST_EV_EPS(POST_EV__EPS),
        BVAPS("Book Value per Share (adjusted)"),
        NAPS("Net Assets per Share"), EPS_GROWTH("EPS Growth"), NUMBER_OF_SHARES("Number of Shares"),
        FISCALYEAR_END("Geschäftsjahresende"), FISCALYEAR("Geschäftsjahr"),
        FCF("Free Cash Flow"), ROE("Return on Equity"), ROIC("Return on Invested Capital");

        private final String displayname;

        private final Fieldname resultingFieldname;

        Fieldname(String displayname) {
            this.displayname = displayname;
            this.resultingFieldname = null;
        }

        Fieldname(Fieldname resultingFieldname) {
            this.resultingFieldname = resultingFieldname;
            this.displayname = null;
        }

        public String getDisplayname() {
            return displayname;
        }

        static Fieldname parse(String text) {
            final Fieldname fieldname = Fieldname.valueOf(text);
            if (fieldname.resultingFieldname != null) {
                return fieldname.resultingFieldname;
            }
            return fieldname;
        }
    }

    private final long instrumentid;

    private final int increment;

    private final Map<Fieldname, EstimateField> values = new EnumMap<>(Fieldname.class);

    private DateTime fiscalYearEnd;

    public EstimatesFields(long instrumentid, int increment) {
        this.instrumentid = instrumentid;
        this.increment = increment;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public int getIncrement() {
        return increment;
    }

    public DateTime getFiscalYearEnd() {
        return fiscalYearEnd;
    }

    public void setFiscalYearEnd(DateTime fiscalYearEnd) {
        this.fiscalYearEnd = fiscalYearEnd;
    }

    public EstimateField getField(Fieldname fieldname) {
        return this.values.get(fieldname);
    }

    public void setValue(Fieldname fieldname, BigDecimal value) {
        final EstimateField field = getOrCreateField(fieldname);
        field.setValue(value);
    }

    public void setNumberOfAnalysts(Fieldname fieldname, int value) {
        final EstimateField field = getOrCreateField(fieldname);
        field.setNumberOfAnalysts(value);
    }

    public void setUpRevisions(Fieldname fieldname, int value) {
        final EstimateField field = getOrCreateField(fieldname);
        field.setUpRevisions(value);
    }

    public void setDownRevisions(Fieldname fieldname, int value) {
        final EstimateField field = getOrCreateField(fieldname);
        field.setDownRevisions(value);
    }

    public void setUnchangedRevisions(Fieldname fieldname, int value) {
        final EstimateField field = getOrCreateField(fieldname);
        field.setUnchangedRevisions(value);
    }

    private EstimateField getOrCreateField(Fieldname f) {
        EstimateField field = this.values.get(f);
        if (field == null) {
            field = new EstimateField();
            this.values.put(f, field);
        }
        return field;
    }

    StockRevenueSummaryImpl toStockRevenueSummary(String currency, Interval interval,
            Integer fiscalYear, BigDecimal priceTarget, Integer numPriceTarget,
            BigDecimal longTermGrowth, Integer numLongTermGrowth, BigDecimal price,
            BigDecimal recommendation, Integer numBuy, Integer numOverweight, Integer numHold,
            Integer numUnderweight, Integer numSell, Integer numTotal, List<String> brokerNames,
            LocalDate referenceDate, DateTime dmlDate) {
        return new StockRevenueSummaryImpl(interval,
                currency,
                fiscalYear,
                price,
                recommendation,
                numBuy,
                numOverweight,
                numHold,
                numUnderweight,
                numSell,   // 10
                numTotal,
                brokerNames,
                referenceDate,
                dmlDate,
                toField(Fieldname.EPS), // 15
                toField(Fieldname.P_E),
                toField(Fieldname.CFPS),
                toField(Fieldname.NET_DIVIDEND),
                toField(Fieldname.NET_DIV_YIELD),
                toField(Fieldname.NET_PROFIT),
                toField(Fieldname.PRE_TAX_PROFIT),
                toField(Fieldname.EAG),
                toField(Fieldname.EBG),
                StockRevenueSummaryImpl.Field.create(longTermGrowth, numLongTermGrowth, null, null, null),
                toField(Fieldname.BVPS),
                toField(Fieldname.BVAPS),
                toField(Fieldname.EBIT),
                toField(Fieldname.EBITDA),
                toField(Fieldname.GOODWILL),
                toField(Fieldname.NET_DEBT),
                StockRevenueSummaryImpl.Field.create(priceTarget, numPriceTarget, null, null, null),
                toField(Fieldname.SALES),
                toField(Fieldname.POST_EV__EPS),
                toField(Fieldname.EPS_STDDEV),
                toField(Fieldname.NAPS),
                toField(Fieldname.EPS_GROWTH),
                toField(Fieldname.NUMBER_OF_SHARES),
                toField(Fieldname.FCF),
                toField(Fieldname.ROE),
                toField(Fieldname.ROIC));
    }


    private StockRevenueSummaryImpl.Field toField(Fieldname fn) {
        final EstimateField f = this.values.get(fn);
        if (f == null) {
            return StockRevenueSummaryImpl.EMPTY_FIELD;
        }
        return StockRevenueSummaryImpl.Field.create(f.getValue(), f.getNumberOfAnalysts(),
                f.getUpRevisions(), f.getDownRevisions(), f.getUnchangedRevisions());
    }
}
