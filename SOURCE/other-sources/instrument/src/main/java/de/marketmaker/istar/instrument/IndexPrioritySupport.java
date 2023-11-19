/*
 * IndexPrioritySupport.java
 *
 * Created on 10.12.13 14:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.instrument.Quote;

/**
 * @author oflege
 */
public class IndexPrioritySupport {
    private static final Map<Long, Integer> PRIOS = new HashMap<>();

    static {
        Properties props;
        try {
            props = PropertiesLoader.load(IndexPrioritySupport.class.getResourceAsStream("index-priorities.properties"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (String s : props.stringPropertyNames()) {
            PRIOS.put(Long.parseLong(s), Integer.parseInt(props.getProperty(s)));
        }
    }

    public static final Comparator<Quote> DESCENDING_PRIORITY = new Comparator<Quote>() {
        @Override
        public int compare(Quote o1, Quote o2) {
            return Integer.compare(getPriority(o2.getId()), getPriority(o1.getId()));
        }
    };

    public static Long getWithMaxWeightFrom(Collection<Long> values) {
        Long result = null;
        int maxWeight = -1;
        int weight;
        for (Long value : values) {
            if ((weight = getPriority(value)) > maxWeight) {
                maxWeight = weight;
                result = value;
            }
        }
        return result;
    }

    public static int getPriority(Long qid) {
        final Integer result = PRIOS.get(qid);
        return (result != null) ? result : 0;
    }

    public static void main(String[] args) {
        System.out.println(getWithMaxWeightFrom(Arrays.asList(1694349L)));
    }
}
