/*
 * CalcDataPartitionStrategyImpl.java
 *
 * Created on 12.05.2010 13:41:58
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.util.CollectionUtils;

/**
 * @author zzhao
 */
public class CalcDataPartitioner {

    private QuoteCategorizer categorizer;

    public void setCategorizer(QuoteCategorizer categorizer) {
        this.categorizer = categorizer;
    }

    /**
     * Divides the given list of calculation data into partitions according to category of the quote
     * contained in one calculation data.
     *
     * @param cds a list of calculation data.
     * @return a calculation data list partition.
     */
    public Map<QuoteCategory, List<CalcData>> partition(List<CalcData> cds) {
        if (CollectionUtils.isEmpty(cds)) {
            return Collections.emptyMap();
        }

        final Map<QuoteCategory, List<CalcData>> ret = new EnumMap<>(QuoteCategory.class);

        for (final CalcData cd : cds) {
            final QuoteCategory qc = categorizer.categorize(cd.getQuote());
            List<CalcData> tmp = ret.get(qc);
            if (null == tmp) {
                tmp = new LinkedList<>();
                ret.put(qc, tmp);
            }
            tmp.add(cd);
        }

        return ret;
    }
}
