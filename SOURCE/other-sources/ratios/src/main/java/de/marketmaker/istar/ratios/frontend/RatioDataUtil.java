/*
 * RatioDataUtil.java
 *
 * Created on 08.11.11 14:01
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.ratios.RatioFieldDescription.Field;
import de.marketmaker.istar.ratios.backend.RatioFieldTypeVisitor;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.BitSet;
import java.util.Locale;

import java.util.Objects;
import java.util.function.Predicate;
import org.joda.time.format.ISODateTimeFormat;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.common.util.TimeFormatter;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author oflege
 */
class RatioDataUtil implements RatioFieldTypeVisitor {

    private static final DecimalFormat FORMATTER = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    static {
        FORMATTER.applyLocalizedPattern("0.#####");
    }

    private Selectable ps;
    private String result;

    public void visit(Field field, Selectable propertySupport) {
        this.ps = propertySupport;
        visit(field);
    }

    private void setResult(String value) {
        this.result = value;
    }

    public String getResult() {
        return this.result;
    }

    static String toDebugString(TypeData data, RatioData ratioData) {
        return toDebugString(data, ratioData, i -> true);
    }

    static String toDebugString(TypeData data, RatioData ratioData, Predicate<Integer> fieldIdFilter) {
        final DecimalFormat format = (DecimalFormat) NumberFormat.getInstance(Locale.US);
        format.applyLocalizedPattern("0.#####");

        final InstrumentTypeEnum type = data.getType();
        final InstrumentRatios ir = ratioData.getInstrumentRatios();

        final StringBuilder sb = new StringBuilder(8192);
        sb.append("Instrument [").append("\n\t").append(ir.getId());

        RatioFieldDescription.getInstrumentFieldIdsSortedByName()
            .stream().filter(fieldIdFilter)
            .map(RatioFieldDescription::getFieldById)
            .filter(Objects::nonNull)
            .forEach(field -> {
                if (!field.isApplicableFor(type)) {
                    return;
                }
                sb.append(", ").append(field.name()).append("=").append(getFieldContent(field, ir));
            });

        for (final QuoteRatios quoteRatios : ratioData.getQuoteRatios()) {
            sb.append("\n\tQuote [").append("\n\t\t").append(quoteRatios.getId());

            RatioFieldDescription.getQuoteRatiosFieldIdsSortedByName()
                .stream()
                .filter(fieldIdFilter)
                .map(RatioFieldDescription::getFieldById)
                .filter(Objects::nonNull)
                .forEach(field -> {
                    if (!field.isApplicableFor(type)) {
                        return;
                    }
                    sb.append(", ").append(field.name()).append("=").append(getFieldContent(field, quoteRatios));
                });
            sb.append("\n\t").append("]");
        }

        sb.append("\n").append("]");

        return sb.toString();
    }

    private static String getFieldContent(RatioFieldDescription.Field field, Selectable ps) {
        final RatioDataUtil util = new RatioDataUtil();
        util.visit(field, ps);
        return util.getResult();
    }

    @Override
    public void visitNumber(Field field) {
        final Long n = this.ps.getLong(field.id());
        if (n == null || n == Long.MIN_VALUE) {
            setResult(null);
        } else {
            setResult(Long.toString(n));
        }
    }

    @Override
    public void visitDecimal(Field field) {
        final Long l = this.ps.getLong(field.id());
        if (l == null || l == Long.MIN_VALUE) {
            setResult(null);
        } else {
            final BigDecimal bd = new BigDecimal(l);
            setResult(FORMATTER.format(bd.movePointLeft(field.isPercent() ? 3 : 5))
                + (field.isPercent() ? "%" : ""));
        }
    }

    @Override
    public void visitString(Field field) {
        setResult(this.ps.getString(field.id()));
    }

    @Override
    public void visitStringLocalized(Field field, Locale[] locales) {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < locales.length; i++) {
            final Locale locale = field.getLocales()[i];
            if (sb.length() > 0) {
                sb.append("//");
            }
            sb.append(locale.getLanguage()).append("=").append(this.ps.getString(field.id(), i));
        }
        setResult(sb.toString());
    }

    @Override
    public void visitDate(Field field) {
        final Integer date = this.ps.getInt(field.id());
        if (date == null || date == Integer.MIN_VALUE) {
            setResult(null);
        } else {
            setResult(date.toString());
        }
    }

    @Override
    public void visitTime(Field field) {
        final Integer time = this.ps.getInt(field.id());
        if (time == null || time == Integer.MIN_VALUE) {
            setResult(null);
        } else {
            setResult(TimeFormatter.formatHHMMSS(time));
        }
    }

    @Override
    public void visitTimestamp(Field field) {
        final Long value = this.ps.getLong(field.id());
        if (value == null || value == Long.MIN_VALUE) {
            setResult(null);
        } else {
            final int yyyymmdd = (int) (value / 100000L);
            final int secondsInDay = (int) (value % 100000L);
            setResult(ISODateTimeFormat.dateTimeNoMillis().print(DateUtil.toDateTime(yyyymmdd, secondsInDay)));
        }
    }

    @Override
    public void visitBoolean(Field field) {
        final Boolean b = this.ps.getBoolean(field.id());
        setResult(b == null ? null : b.toString());
    }

    @Override
    public void visitEnumset(Field field) {
        final BitSet es = this.ps.getBitSet(field.id());
        if (es == null || es.isEmpty()) {
            setResult(null);
        } else {
            setResult(RatioEnumSetFactory.fromBits(field.id(), es));
        }
    }
}
