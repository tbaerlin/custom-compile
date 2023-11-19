/*
 * PageContentEvaluator.java
 *
 * Created on 25.02.2008 11:56:24
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.pages;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.BitSet;
import java.util.Map;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.data.SnapField;
import de.marketmaker.istar.domain.data.SnapRecord;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.data.PriceRecord;
import de.marketmaker.istar.feed.snap.SnapRecordUtils;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.merger.provider.IntradayData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class PageContentEvaluator {
    static final BigDecimal NO_CURRENCY_CONVERSION = BigDecimal.valueOf(1L);

    private static final BitSet FIELDS_TO_CONVERT = new BitSet();

    static {
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Anfang_Vortag.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Anfang.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Bezahlt.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Brief.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Geld.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Jahreshoch.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Jahrestief.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Kassa_Vortag.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Kassa.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Mittelkurs.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Rueckzahlungs_Kurs.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Schluss_Vortag.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Schluss.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Settlement.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Tageshoch_Vortag.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Tageshoch.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Tagestief_Vortag.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Tagestief.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.ADF_Veraenderung.id());
        FIELDS_TO_CONVERT.set(VwdFieldDescription.MMF_Schluss_Vorvortag.id());
    }

    private PdlPage page;

    private Map<String, IntradayData> records;

    private DecimalFormat[] decimalFormat;

    private DateTimeFormatter[] dateTimeFormatter;

    private Map<String, BigDecimal> currencyFactors;

    public void setDateTimeFormatter(DateTimeFormatter[] dateTimeFormatter) {
        this.dateTimeFormatter = dateTimeFormatter;
    }

    public void setDecimalFormat(DecimalFormat[] decimalFormat) {
        this.decimalFormat = decimalFormat;
    }

    public void setPage(PdlPage page) {
        this.page = page;
    }

    public void setRecords(Map<String, IntradayData> records) {
        this.records = records;
    }

    public void setCurrencyFactors(Map<String, BigDecimal> currencyFactors) {
        this.currencyFactors = currencyFactors;
    }

    DateTime getMaxUpdateTimestamp() {
        int maxUpdatedDate = 0;
        int maxUpdatedTime = 0;

        for (IntradayData data : records.values()) {
            final SnapField sfDate = getField(VwdFieldDescription.ADF_DATEOFARR.id(), data);
            final SnapField sfTime = getField(VwdFieldDescription.ADF_TIMEOFARR.id(), data);

            if (sfDate != null && sfTime != null
                    && SnapRecordUtils.getInt(sfDate) >= maxUpdatedDate
                    && SnapRecordUtils.getInt(sfTime) > maxUpdatedTime) {
                maxUpdatedDate = SnapRecordUtils.getInt(sfDate);
                maxUpdatedTime = SnapRecordUtils.getInt(sfTime);
            }
        }

        if (maxUpdatedDate > 0) {
            return DateUtil.toDateTime(maxUpdatedDate, maxUpdatedTime);
        }
        return null;
    }

    void evaluateDynamicContent() {
        for (PdlObject po : page.getObjects()) {
            if (po.getType() != PdlObject.TYPE_DATA) {
                continue;
            }

            final PdlDataObject pdo = (PdlDataObject) po;

            final String rc3key = pdo.getRequestObject();
            final IntradayData data = records.get(rc3key);
            if (data == null) {
                continue;
            }

            final SnapField sf = getField(pdo.getFieldId(), data);
            if (sf == null || !sf.isDefined()) {
                continue;
            }

            BigDecimal factor = NO_CURRENCY_CONVERSION;
            String content;
            final VwdFieldDescription.Field field = VwdFieldDescription.getField(sf.getId());
            if (field.type() == VwdFieldDescription.Type.PRICE) {
                if (this.currencyFactors != null && FIELDS_TO_CONVERT.get(sf.getId())) {
                    final SnapField sfCurrency = getField(VwdFieldDescription.ADF_Waehrung.id(), data);
                    if (sfCurrency != null) {
                        factor = this.currencyFactors.get(sfCurrency.getValue().toString());
                    }
                }

                content = priceFieldToString(sf, pdo, factor);
                pdo.setPriceQuality(getPriceQuality(data.getPrice()));
            }
            else {
                content = fieldToString(sf, pdo);

            }

            pdo.setContent(content);
        }
    }

    private PriceQuality getPriceQuality(PriceRecord pr) {
        return (pr != null) ? pr.getPriceQuality() : PriceQuality.NONE;
    }

    private SnapField getField(int fieldId, IntradayData item) {
        final SnapField field = getField(fieldId, item.getSnap());
        return (field != null) ? field : getField(fieldId, item.getSnap());
    }

    private SnapField getField(int fieldId, SnapRecord snapRecord) {
        if (snapRecord == null) {
            return null;
        }
        final SnapField sf = snapRecord.getField(fieldId);
        return (sf != null && sf.isDefined()) ? sf : null;
    }

    private String priceFieldToString(SnapField sf, PdlDataObject pdo,
            BigDecimal currencyConversionFactor) {
        BigDecimal value = sf.getPrice();

        if (value.signum() == 0 || currencyConversionFactor == null) {
            return "";
        }

        if (currencyConversionFactor != NO_CURRENCY_CONVERSION) {
            value = value.multiply(currencyConversionFactor);
            pdo.setPriceConverted(true);
        }

        if (pdo.getScalingFactor() != 1) {
            value = value.multiply(BigDecimal.valueOf(pdo.getScalingFactor(), 0));
        }

        final int i = Math.max(0, Math.min(pdo.getDecimalPlaces(), VwdPageProvider.MAX_FRACTION_DIGITS));

        synchronized (this.decimalFormat[i]) {
            return this.decimalFormat[i].format(value);
        }
    }

    private String fieldToString(SnapField sf, PdlDataObject pdo) {
        final VwdFieldDescription.Field field = VwdFieldDescription.getField(sf.getId());
        switch (field.type()) {
            case STRING:
                return sf.getValue().toString();

            case UINT:
            case USHORT:
                return sf.getValue().toString();

            case DATE:
                final int yyyymmdd = ((Number) sf.getValue()).intValue();
                if (yyyymmdd == 0) {
                    return "";
                }
                final DateTime date = DateUtil.yyyymmddToDateTime(yyyymmdd);
                switch (pdo.getDecimalPlaces()) {
                    case 4:
                        return this.dateTimeFormatter[0].print(date);
                    case 6:
                        return this.dateTimeFormatter[1].print(date);
                    case 8:
                        return this.dateTimeFormatter[2].print(date);
                    case 10:
                    default:
                        return this.dateTimeFormatter[3].print(date);
                }

            case TIME:
                boolean withSeconds = pdo.getDecimalPlaces() == 8;
                return formatTime(((Number) sf.getValue()).intValue(), withSeconds);

            default:
                return "";
        }
    }

    private String formatTime(int secondsInDay, boolean withSeconds) {
        if (secondsInDay < 0 || secondsInDay >= 86400) {
            return withSeconds ? "--:--:--" : "--:--";
        }
        final String s = TimeFormatter.formatSecondsInDay(secondsInDay);
        return (withSeconds) ? s : s.substring(0, 5);
    }

}
