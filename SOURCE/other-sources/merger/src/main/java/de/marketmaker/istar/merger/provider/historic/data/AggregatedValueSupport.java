/*
 * AggregatedPriceSupport.java
 *
 * Created on 07.08.13 16:38
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.marketmaker.istar.domain.data.AggregatedValue;
import de.marketmaker.istar.domain.data.LiteralSnapField;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.TickImpl;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.historic.Aggregation;
import de.marketmaker.istar.merger.provider.historic.HistoricRequestImpl;
import de.marketmaker.istar.merger.provider.historic.HistoricTerm;
import de.marketmaker.istar.merger.provider.historic.HistoricTimeseries;
import de.marketmaker.istar.merger.provider.historic.PriceType;
import de.marketmaker.istar.merger.web.easytrade.TickDataCommand;

/**
 * @author zzhao
 */
public class AggregatedValueSupport {

    private static final Logger log = LoggerFactory.getLogger(AggregatedValueSupport.class);

    private static final Pattern P_SIMPLE_TERM = Pattern.compile("([OHLCV])=(\\w+)");

    private final TickDataCommand.ElementDataType resultType;

    private final TickImpl.Type tickType;

    private final Map<PriceType, VwdFieldDescription.Field> baseFields = new EnumMap<>(PriceType.class);

    private final List<VwdFieldDescription.Field> additionalFields;

    private int valueSlots;

    private String modelKey = "trades";

    public AggregatedValueSupport(TickDataCommand.ElementDataType resultType, String baseField,
            TickImpl.Type tickType, String[] additionalFields) {
        this.resultType = resultType;
        this.tickType = tickType;
        this.additionalFields = lookupAdditionalFields(additionalFields);
        fillBaseFields(baseField);
    }

    private static final EnumSet<VwdFieldDescription.Type> allowedTypes = EnumSet.of(
            VwdFieldDescription.Type.PRICE,
            VwdFieldDescription.Type.UINT,
            VwdFieldDescription.Type.USHORT
    );

    private List<VwdFieldDescription.Field> lookupAdditionalFields(String[] additionalFields) {
        if (null != additionalFields) {
            final ArrayList<VwdFieldDescription.Field> ret = new ArrayList<>(additionalFields.length);
            for (String field : additionalFields) {
                final VwdFieldDescription.Field vwdField = toVwdField(field);
                if (null != vwdField && allowedTypes.contains(vwdField.type())) {
                    ret.add(vwdField);
                }
                else {
                    log.warn("<lookupAdditionalFields> ignore field: {}", field);
                }
            }
            return ret;
        }
        return Collections.emptyList();
    }

    private void fillBaseFields(String baseField) {
        if (StringUtils.isNotBlank(baseField)) {
            final Matcher matcher = P_SIMPLE_TERM.matcher(baseField);
            if (matcher.find()) {
                do {
                    putBaseField(matcher.group(1), matcher.group(2));
                } while (matcher.find());
            }
            else {
                putBaseField(PriceType.OPEN, baseField);
                putBaseField(PriceType.HIGH, baseField);
                putBaseField(PriceType.LOW, baseField);
                putBaseField(PriceType.CLOSE, baseField);
            }
        }
    }

    private void putBaseField(String priceType, String field) {
        switch (priceType) {
            case "O":
                putBaseField(PriceType.OPEN, field);
                break;
            case "H":
                putBaseField(PriceType.HIGH, field);
                break;
            case "L":
                putBaseField(PriceType.LOW, field);
                break;
            case "C":
                putBaseField(PriceType.CLOSE, field);
                break;
            case "V":
                putBaseField(PriceType.VOLUME, field);
                break;
            default:
                throw new UnsupportedOperationException("no support for type: " + priceType);
        }
    }

    private void putBaseField(PriceType priceType, String baseField) {
        final VwdFieldDescription.Field vwdField = toVwdField(baseField);
        if (null != vwdField) {
            this.baseFields.put(priceType, vwdField);
        }
    }

    private VwdFieldDescription.Field toVwdField(String field) {
        final VwdFieldDescription.Field vwdField;
        if (StringUtils.isNumeric(field)) {
            vwdField = VwdFieldDescription.getField(Integer.parseInt(field));
        }
        else {
            vwdField = VwdFieldDescription.getFieldByName(field);
        }
        return vwdField;
    }

    private void addHistoricTerm(HistoricRequestImpl req, HistoricTerm term) {
        req.addHistoricTerm(term);
        this.valueSlots++;
    }

    public String getModelKey() {
        return modelKey;
    }

    public void addHistoricTerms(HistoricRequestImpl req) {
        final boolean isFund = req.getQuote().getQuotedef() == 3;
        switch (this.tickType) {
            case TRADE:
                switch (this.resultType) {
                    case PERFORMANCE:
                    case CLOSE:
                        addHistoricTerm(req, getTerm(PriceType.CLOSE, isFund, Aggregation.LAST));
                        break;
                    case OHLC:
                        addHistoricTerm(req, getTerm(PriceType.OPEN, isFund, Aggregation.FIRST));
                        addHistoricTerm(req, getTerm(PriceType.HIGH, isFund, Aggregation.MAX));
                        addHistoricTerm(req, getTerm(PriceType.LOW, isFund, Aggregation.MIN));
                        addHistoricTerm(req, getTerm(PriceType.CLOSE, isFund, Aggregation.LAST));
                        break;
                    case OHLCV:
                        addHistoricTerm(req, getTerm(PriceType.OPEN, isFund, Aggregation.FIRST));
                        addHistoricTerm(req, getTerm(PriceType.HIGH, isFund, Aggregation.MAX));
                        addHistoricTerm(req, getTerm(PriceType.LOW, isFund, Aggregation.MIN));
                        addHistoricTerm(req, getTerm(PriceType.CLOSE, isFund, Aggregation.LAST));
                        addHistoricTerm(req, getTerm(PriceType.VOLUME, false, Aggregation.SUM));
                        addHistoricTerm(req, getTerm(PriceType.CONTRACT, false, Aggregation.SUM));
                        break;
                    case FUND:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Ausgabe));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Ruecknahme));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_NAV));
                        addHistoricTerm(req, HistoricTerm.fromFunctionalPrice(PriceType.VOLUME));
                        this.modelKey = "fundTs";
                        break;
                    case VOLUME_AGGREGATION:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Umsatz_Gesamt_Call));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Umsatz_Gesamt_Put));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Call));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Put));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Umsatz_Gesamt_Futures));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Block_Tr_Umsatz_Gesamt_Fut));
                        this.modelKey = "volumes";
                        break;
                }
                break;
            case BID:
                switch (this.resultType) {
                    case OHLC:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Eroeffnung));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Tageshoch));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Tagestief));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Schluss));
                        break;
                    case OHLCV:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Eroeffnung));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Tageshoch));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Tagestief));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Schluss));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Umsatz_in_Whrg));
                        break;
                    default:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Geld_Schluss));
                        break;
                }
                break;
            case ASK:
                switch (this.resultType) {
                    case OHLC:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Eroeffnung));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Tageshoch));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Tagestief));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Schluss));
                        break;
                    case OHLCV:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Eroeffnung));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Tageshoch));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Tagestief));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Schluss));
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Umsatz_in_Whrg));
                        break;
                    default:
                        addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Brief_Schluss));
                        break;
                }
                break;
            case YIELD:
                addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Rendite));
                break;
            case SETTLEMENT:
                addHistoricTerm(req, HistoricTerm.fromVwdField(VwdFieldDescription.ADF_Settlement));
                break;
            default:
                break;
        }

        for (VwdFieldDescription.Field field : this.additionalFields) {
            req.addHistoricTerm(HistoricTerm.fromVwdField(field));
        }
    }

    private HistoricTerm getTerm(PriceType priceType, boolean isFund, Aggregation agg) {
        if (this.baseFields.containsKey(priceType)) {
            // todo handle functionalFill
            return HistoricTerm.fromVwdField(this.baseFields.get(priceType), agg);
        }
        else {
            return HistoricTerm.fromFunctionalPrice(isFund ? PriceType.KASSA : priceType, agg);
        }
    }

    public List<AggregatedValue> extractPrices(List<HistoricTimeseries> htList) {
        final int size = getSize(htList, this.valueSlots + this.additionalFields.size());

        if (size == 0) {
            return Collections.emptyList();
        }

        final ArrayList<AggregatedValue> ret = new ArrayList<>();
        final List<HistoricTimeseries> hts;
        if (htList.size() > this.valueSlots + this.additionalFields.size()) {
            hts = htList.subList(0, this.valueSlots + this.additionalFields.size());
        }
        else {
            hts = htList;
        }

        int k = getNextDefinedPosition(hts, 0, size);
        final LocalDate startDay = hts.get(0).getStartDay();
        while (k >= 0 && k < size) {
            final AggregatedValueImpl ap = new AggregatedValueImpl(startDay.plusDays(k).toInterval(),
                    this.tickType, this.resultType, this.valueSlots, this.additionalFields.size());
            for (int i = 0; i < this.valueSlots; i++) {
                ap.addValue(getValue(hts.get(i), k));
            }
            for (int i = 0; i < this.additionalFields.size(); i++) {
                final SnapField snapField = getSnapField(this.additionalFields.get(i),
                        hts.get(i + this.valueSlots), k);
                if (null != snapField.getValue()) {
                    ap.addAdditionalField(snapField);
                }
            }

            ret.add(ap);
            k = getNextDefinedPosition(hts, k + 1, size);
        }

        if (this.resultType == TickDataCommand.ElementDataType.PERFORMANCE) {
            final BigDecimal firstValue = getFirstNonZeroValue(ret);
            if (BigDecimal.ZERO.equals(firstValue)) {
                return Collections.emptyList();
            }
            else {
                final ArrayList<AggregatedValue> performances = new ArrayList<>(ret.size());
                for (AggregatedValue av : ret) {
                    performances.add(av.divide(firstValue));
                }
                return performances;
            }
        }

        return ret;
    }

    private BigDecimal getFirstNonZeroValue(ArrayList<AggregatedValue> ret) {
        for (AggregatedValue av : ret) {
            if (!BigDecimal.ZERO.equals(av.getClose())) {
                return av.getClose();
            }
        }
        return BigDecimal.ZERO;
    }

    private SnapField getSnapField(VwdFieldDescription.Field field,
            HistoricTimeseries ht, int idx) {
        switch (field.type()) {
            case PRICE:
                return LiteralSnapField.createPrice(field.id(), getValue(ht, idx));
            case UINT:
            case USHORT:
                return LiteralSnapField.createNumber(field.id(), getNumber(ht, idx));
            default:
                throw new UnsupportedOperationException("no support for: " + field);
        }
    }

    private Number getNumber(HistoricTimeseries ht, int idx) {
        final BigDecimal value = getValue(ht, idx);
        return null == value ? null : value.longValue();
    }


    private BigDecimal getValue(HistoricTimeseries ht, int idx) {
        return null == ht || Double.isNaN(ht.getValue(idx))
                ? null
                : BigDecimal.valueOf(ht.getValue(idx));
    }

    private int getNextDefinedPosition(List<HistoricTimeseries> lts, int from, int to) {
        for (int i = from; i < to; i++) {
            for (int j = 0; j < lts.size(); j++) {
                final HistoricTimeseries ht = lts.get(j);
                if (null != ht && !Double.isNaN(ht.getValue(i))) {
                    return i;
                }
            }
        }
        return to;
    }

    private int getSize(List<HistoricTimeseries> lts, int to) {
        int ret = 0;
        if (null != lts) {
            for (int i = 0; i < to; i++) {
                final HistoricTimeseries ht = lts.get(i);
                ret = Math.max(ret, null == ht ? 0 : ht.size());
            }
        }

        return ret;
    }
}
