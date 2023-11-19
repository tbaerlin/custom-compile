/*
 * PriceRecordFactory.java
 *
 * Created on 08.11.2006 12:56:16
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import de.marketmaker.istar.domain.data.NullSnapField;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.instrument.Derivative;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.PriceRecordFundVwd;
import de.marketmaker.istar.feed.vwd.PriceRecordIMO;
import de.marketmaker.istar.feed.vwd.PriceRecordLME;
import de.marketmaker.istar.feed.vwd.PriceRecordVwd;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.InstrumentUtil;

import static de.marketmaker.istar.feed.vwd.VwdFieldDescription.*;

/**
 * Factory to create PriceRecords.
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PriceRecordFactory {
    private PriceRecordFactory() {
    }

    private static final Pattern GOV_BONDS = Pattern.compile("5.(.+)GB(.+)J.BONDS");

    private static final Pattern FORWARD = Pattern.compile("10\\.FN?\\w{6}\\.TFI\\.\\w+");

    /**
     * Builder pattern for creating prices; allows to create a PriceRecord without having
     * a Quote object at hand (used in iview push).
     */
    public static class Builder {
        private final Quote quote;

        private String market;

        private String symbolVwdfeed;

        private BigDecimal subscriptionRatio;

        private final SnapRecord sr;

        private final PriceQuality priceQuality;

        private boolean pushAllowed = false;

        // needed in iview for push, don't remove
        public Builder(long qid, SnapRecord sr, PriceQuality priceQuality) {
            this.quote = null;
            this.sr = sr;
            this.priceQuality = priceQuality;
        }

        public Builder(Quote q, SnapRecord sr, PriceQuality priceQuality, boolean pushAllowed) {
            this.quote = q;
            this.sr = sr;
            this.priceQuality = priceQuality;
            this.market = q.getSymbolVwdfeedMarket();
            this.symbolVwdfeed = q.getSymbolVwdfeed();
            this.subscriptionRatio = getSubscriptionRatio(q.getInstrument());
            this.pushAllowed = pushAllowed;
        }

        public Builder withSymbolVwdfeed(String s) {
            this.symbolVwdfeed = s;
            return this;
        }

        public Builder withMarket(String s) {
            this.market = s;
            return this;
        }

        private SnapRecord resolveSnapRecord() {
            if ("EZB".equals(market) && !sr.getField(ADF_Bezahlt.id()).isDefined()) {
                return new EzbWrapperSnapRecord(sr);
            }
            if ("AMI".equals(market)) {
                return new AmiWrapperSnapRecord(sr);
            }
            if (matches(GOV_BONDS)) {
                return new GovBondsWrapperSnapRecord(sr);
            }
            return sr;
        }

        private boolean matches(final Pattern pattern) {
            return symbolVwdfeed != null && pattern.matcher(symbolVwdfeed).matches();
        }

        public PriceRecord build() {
            if (getInstrumentType() == InstrumentTypeEnum.IMO) {
                return new PriceRecordIMO(getQuoteId(), this.sr, this.priceQuality,
                        this.pushAllowed);
            }
            if (InstrumentUtil.isVwdFundFeedsymbol(this.symbolVwdfeed)) {
                return new PriceRecordFundVwd(getQuoteId(), this.sr, this.priceQuality, this.pushAllowed);
            }
            final boolean isForward = isForward();

            if (InstrumentUtil.isLMEMarket(this.market)) {
                return new PriceRecordLME(getQuoteId(), this.sr, this.priceQuality, this.pushAllowed, this.subscriptionRatio, isForward);
            }

            if (InstrumentUtil.isEZBMarket(this.market)) {
                return new PriceRecordVwd(getQuoteId(), resolveSnapRecord(), this.priceQuality,
                        this.pushAllowed, this.subscriptionRatio, isForward) {
                    @Override
                    protected boolean getCurrentDayDefined() {
                        return super.getCurrentDayDefined() || (isDefined(ADF_Bezahlt) && isDefinedAndNotZero(ADF_Bezahlt_Datum));
                    }
                };
            }

            return new PriceRecordVwd(getQuoteId(), resolveSnapRecord(), this.priceQuality,
                    this.pushAllowed, this.subscriptionRatio, isForward);
        }

        private InstrumentTypeEnum getInstrumentType() {
            return (this.quote != null) ? this.quote.getInstrument().getInstrumentType() : null;
        }

        private long getQuoteId() {
            return this.quote != null ? this.quote.getId() : 0L;
        }

        private boolean isForward() {
            if (matches(FORWARD)) {
                return true;
            }

            // Mail by OStroehmann@vwd.com, 2014-03-21
            final String type = SnapRecordUtils.getString(this.sr.getField(VwdFieldDescription.ADF_Instrumententyp.id()));
            return "Carry Future".equals(type);
        }
    }

    public static PriceRecord create(Quote quote, SnapRecord sr, PriceQuality priceQuality,
            boolean pushAllowed) {
        return new Builder(quote, sr, priceQuality, pushAllowed).build();
    }

    private static BigDecimal getSubscriptionRatio(Instrument instrument) {
        if (instrument instanceof Derivative) {
            return ((Derivative) instrument).getSubscriptionRatio();
        }
        return null;
    }

    /**
     * A base class for a SnapRecord that maps some fields to some other fields.
     */
    private abstract static class WrapperSnapRecord implements SnapRecord, Serializable {
        protected static final long serialVersionUID = 1L;

        private final SnapRecord sr;

        protected final Map<Integer, Integer> mappings;

        WrapperSnapRecord(SnapRecord sr, Map<Integer, Integer> mappings) {
            this.sr = sr;
            this.mappings = mappings;
        }

        @Override
        public SnapField getField(int fieldId) {
            final Integer mappedId = this.mappings.get(fieldId);
            if (mappedId != null) {
                return doGetField(mappedId);
            }
            return doGetField(fieldId);
        }

        protected SnapField doGetField(Integer targetId) {
            return this.sr.getField(targetId);
        }

        @Override
        public Collection<SnapField> getSnapFields() {
            return this.sr.getSnapFields();
        }

        @Override
        public SnapField getField(String fieldname) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getNominalDelayInSeconds() {
            return this.sr.getNominalDelayInSeconds();
        }
    }

    private final static class AmiWrapperSnapRecord extends WrapperSnapRecord {

        private static final Map<Integer, Integer> MAPPINGS = new HashMap<>();

        static {
            MAPPINGS.put(ADF_Veraenderung.id(), ADF_Veraenderung_VW.id());
            MAPPINGS.put(ADF_Schluss_Vortag.id(), ADF_Bezahlt_2.id());
        }

        private AmiWrapperSnapRecord(SnapRecord sr) {
            super(sr, MAPPINGS);
        }
    }

    private final static class GovBondsWrapperSnapRecord extends WrapperSnapRecord {
        private static final Map<Integer, Integer> MAPPINGS = new HashMap<>();

        static {
            MAPPINGS.put(ADF_Bezahlt.id(), ADF_Rendite.id());
            MAPPINGS.put(ADF_Schluss_Vortag.id(), ADF_Rendite_Vortag.id());
        }

        private GovBondsWrapperSnapRecord(SnapRecord sr) {
            super(sr, MAPPINGS);
        }
    }

    private final static class EzbWrapperSnapRecord extends WrapperSnapRecord {
        private static final Map<Integer, Integer> MAPPINGS = new HashMap<>();

        static {
            MAPPINGS.put(ADF_Bezahlt.id(), ADF_Geld.id());
            MAPPINGS.put(ADF_Tageshoch.id(), ADF_Geld.id());
            MAPPINGS.put(ADF_Tagestief.id(), ADF_Geld.id());
            MAPPINGS.put(ADF_Schluss_Datum.id(), ADF_Handelsdatum.id());
            MAPPINGS.put(ADF_Schluss_Vortag.id(), ADF_Geld_Vortag.id());
            MAPPINGS.put(ADF_Schluss_Vortagesdatum.id(), ADF_Vorheriges_Datum.id());
        }

        private EzbWrapperSnapRecord(SnapRecord sr) {
            super(sr, MAPPINGS);
        }

        @Override
        public SnapField getField(int fieldId) {
            if (fieldId == MMF_Bezahlt_Datum.id()) {
                // HACK because of feed error
                final SnapField field = doGetField(ADF_Handelsdatum.id());
                if (field.isDefined() && ((Number) field.getValue()).intValue() != 0) {
                    return field;
                }
                return NullSnapField.INSTANCE;
            }
            return super.getField(fieldId);
        }
    }

    public static void main(String[] args) {
        System.out.println(FORWARD.matcher("10.FEURJPY.TFI.TN").matches());
    }
}
