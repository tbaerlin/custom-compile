/*
 * HistoricTerm.java
 *
 * Created on 06.08.13 12:14
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.historic;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.feed.vwd.VwdFieldDescription;

/**
 * @author zzhao
 */
public final class HistoricTerm {

    public static enum Type {ADF, FunctionalPrice, MmFormula}

    private final Type type;

    private final String content;

    private final Aggregation aggregation;

    public HistoricTerm(Type type, String content, Aggregation aggregation) {
        if (null == type) {
            throw new IllegalArgumentException("proper type required");
        }
        if (!StringUtils.hasText(content)) {
            throw new IllegalArgumentException("non-empty content required");
        }
        this.type = type;
        this.content = content;
        this.aggregation = aggregation;
    }

    public static HistoricTerm fromVwdField(VwdFieldDescription.Field vwdField) {
        return new HistoricTerm(Type.ADF, String.valueOf(vwdField.id()), null);
    }

    public static HistoricTerm fromVwdField(VwdFieldDescription.Field vwdField, Aggregation agg) {
        return new HistoricTerm(Type.ADF, String.valueOf(vwdField.id()), agg);
    }

    public static HistoricTerm fromFunctionalPrice(PriceType priceType) {
        return new HistoricTerm(Type.FunctionalPrice, priceType.name(), null);
    }

    public static HistoricTerm fromFunctionalPrice(PriceType priceType, Aggregation aggregation) {
        return new HistoricTerm(Type.FunctionalPrice, priceType.name(), aggregation);
    }

    public static HistoricTerm fromMmTalk(String mmTalk) {
        return new HistoricTerm(Type.MmFormula, mmTalk, null);
    }

    public String getContent() {
        return content;
    }

    public Type getType() {
        return type;
    }

    public Aggregation getAggregation() {
        return aggregation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        HistoricTerm that = (HistoricTerm) o;

        if (!content.equals(that.content)) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + content.hashCode();
        return result;
    }
}
