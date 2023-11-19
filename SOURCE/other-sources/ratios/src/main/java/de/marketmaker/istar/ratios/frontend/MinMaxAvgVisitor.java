/*
 * MinMaxAvgVisitor.java
 *
 * Created on 03.08.2006 18:35:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import java.io.File;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PriceCoder;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.ratios.RatioFieldDescription;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MinMaxAvgVisitor implements MergeableSearchEngineVisitor<MinMaxAvgVisitor> {
    public static final String KEY_GROUP_BY = "mma:groupBy";

    public static final String KEY_SOURCE = "mma:source";

    private RatioFieldDescription.Field[] groupByField;

    private RatioFieldDescription.Field[] sourceFields;

    private int[] localeIndexes;

    private boolean[] sourceIsInstrumentField;

    private boolean[] groupByIsInstrumentField;

    private final MinMaxAvgRatioSearchResponse response = new MinMaxAvgRatioSearchResponse();

    public void init(SearchParameterParser spp) {
        final String groupBy = spp.getParameterValue(KEY_GROUP_BY);

        final String[] groupBys = groupBy.split(",");
        this.groupByField = new RatioFieldDescription.Field[groupBys.length];
        this.groupByIsInstrumentField = new boolean[groupBys.length];
        this.localeIndexes = new int[groupBys.length];
        for (int i = 0; i < groupBys.length; i++) {
            this.groupByField[i] = RatioFieldDescription.getFieldByName(groupBys[i]);
            this.groupByIsInstrumentField[i] = this.groupByField[i].isInstrumentField();
            this.localeIndexes[i] = RatioFieldDescription.getLocaleIndex(this.groupByField[i], spp.getLocales());
        }

        final String source = spp.getParameterValue(KEY_SOURCE);
        if (StringUtils.hasText(source)) {
            final String[] sources = source.split(",");
            this.sourceFields = new RatioFieldDescription.Field[sources.length];
            this.sourceIsInstrumentField = new boolean[sources.length];
            for (int i = 0; i < sources.length; i++) {
                this.sourceFields[i] = RatioFieldDescription.getFieldByName(sources[i]);
                this.sourceIsInstrumentField[i] = this.sourceFields[i].isInstrumentField();
            }
        }
        else {
            this.sourceFields = new RatioFieldDescription.Field[0];
            this.sourceIsInstrumentField = new boolean[0];
        }
    }

    @Override
    public MinMaxAvgVisitor merge(MinMaxAvgVisitor v) {
        this.response.merge(v.response);
        return this;
    }

    @Override
    public void visit(RatioData data) {
        QuoteRatios qr = data.getSearchResult();
        final Selectable srGroupBy0 = this.groupByIsInstrumentField[0] ? qr.getInstrumentRatios() : qr;
        final String groupBy0 = getString(srGroupBy0, this.groupByField[0], this.localeIndexes[0]);

        if (groupBy0 == null) {
            return;
        }

        final String groupBy1;
        if (this.groupByField.length > 1) {
            final Selectable srGroupBy1 = this.groupByIsInstrumentField[1] ? qr.getInstrumentRatios() : qr;
            groupBy1 = getString(srGroupBy1, this.groupByField[1], this.localeIndexes[1]);
            if (groupBy1 == null) {
                return;
            }
        }
        else {
            groupBy1 = "default";
        }

        if (this.sourceFields.length == 0) {
            this.response.add(groupBy0, groupBy1, 0, 0);
        }
        else {
            for (int i = 0; i < this.sourceFields.length; i++) {
                final Selectable s = this.sourceIsInstrumentField[i] ? qr.getInstrumentRatios() : qr;

                final Long v;
                switch (this.sourceFields[i].type()) {
                    case DECIMAL:
                    case NUMBER:
                        v = s.getLong(this.sourceFields[i].id());
                        if (v == null || v == Long.MAX_VALUE || v == Long.MIN_VALUE) {
                            continue;
                        }
                        break;
                    default:
                        continue;
                }

                double source = (this.sourceFields[i].type() == RatioFieldDescription.Type.DECIMAL)
                        ? PriceCoder.decodeAsDouble(v) : v.doubleValue();

                this.response.add(groupBy0, groupBy1, this.sourceFields[i].id(), source);
            }
        }
    }

    public static String getString(Selectable selectable, RatioFieldDescription.Field field,
            int localeIndex) {
        switch (field.type()) {
            case BOOLEAN:
                final Boolean aBoolean = selectable.getBoolean(field.id());
                return aBoolean == null ? null : Boolean.toString(aBoolean);
            case DATE:
                final Integer anInt = selectable.getInt(field.id());
                return anInt == null ? null : Integer.toString(anInt);
            case ENUMSET:
                final BitSet esVal = selectable.getBitSet(field.id());
                return null != esVal && !esVal.isEmpty() ? RatioEnumSetFactory.fromBits(field.id(), esVal) : null;
            case NUMBER:
                final Long aLong = selectable.getLong(field.id());
                return aLong == null ? null : Long.toString(aLong);
            case DECIMAL:
                final Long aDecimal = selectable.getLong(field.id());
                return aDecimal == null ? null : Double.toString(PriceCoder.decodeAsDouble(aDecimal));
            case STRING:
                final String s = selectable.getString(field.id(), localeIndex);
                if (s != null) {
                    return s;
                }

                if (field == RatioFieldDescription.gatrixxMultiassetName) {
                    final Long uiid = selectable.getLong(RatioFieldDescription.underlyingIid.id());
                    return uiid == null ? null : Long.toString(uiid);
                }
                return null;
        }
        return null;
    }

    public MinMaxAvgRatioSearchResponse getResponse() {
        return this.response;
    }

    public static void main(String[] args) throws Exception {
        final FileRatioDataStore store = new FileRatioDataStore();
        store.setBaseDir(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/ratios"));
        final TypeData data = new TypeData(InstrumentTypeEnum.FND);
        store.restore(data.getType(), data);

        final TimeTaker tt = new TimeTaker();
        final RatioSearchRequest mmaRequest = new RatioSearchRequest(ProfileFactory.valueOf(true), null);
        mmaRequest.setType(InstrumentTypeEnum.FND);
        mmaRequest.setDataRecordStrategyClass(DefaultDataRecordStrategy.class);
        mmaRequest.setVisitorClass(MinMaxAvgVisitor.class);
        final Map<String, String> mmaParameters = new HashMap<>();
        mmaRequest.setParameters(mmaParameters);
        mmaParameters.put("vwdMarket", "FONDS");
        mmaParameters.put(MinMaxAvgVisitor.KEY_GROUP_BY, "msInvestmentFocus");
        mmaParameters.put(MinMaxAvgVisitor.KEY_SOURCE, "bviperformancecurrentyear,bviperformance1m,bviperformance3m,bviperformance6m," +
                "bviperformance1y,bviperformance3y,bviperformance5y,bviperformance10y,bviperformanceAlltime,volatility3y");

        final SearchParameterParser spp = new SearchParameterParser(mmaRequest, null);
        final ForkJoinPool pool = new ForkJoinPool(2);
        final RatioSearchResponse response = data.searchAndVisit(spp, pool);

        final Map<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> result = ((MinMaxAvgRatioSearchResponse) response).getResult();
        System.out.println(result);

    }
}
