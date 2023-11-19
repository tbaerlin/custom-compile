package de.marketmaker.istar.analyses.analyzer.collect;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

import de.marketmaker.istar.analyses.analyzer.Analysis;
import de.marketmaker.istar.analyses.analyzer.PriceCache;
import de.marketmaker.istar.analyses.analyzer.collect.PerformanceCollector.PerformanceValues;

/**
 *  calculate and collect performance values from analyses (and any associated securities)
 *
 *  performance for a single analyses is defined as:
 *     performance =  ((analysis.target / security.previousClose) - 1)
 *
 *  aggregation (e.g. for a agency) is done by summing up the performance values and dividing by the number of analyses
 */
public class PerformanceCollector<K> implements Collector<Entry<K, Analysis>, PerformanceValues, PerformanceValues> {
    private static final int SCALE = 3;

    // not concurrent, not unordered
    private static final Set<Characteristics> CH_ID = Collections.unmodifiableSet(EnumSet.of(Characteristics.IDENTITY_FINISH));

    private final int yyyyMmDd;
    private final PriceCache priceCache;

    /**
     * TODO: we probably need the profile here
     * @param yyyyMmDd the date we want to evaluate the performance on
     * @param priceCache backend data storage for price data to calculate the performance
     */
    public PerformanceCollector(int yyyyMmDd, PriceCache priceCache) {
        this.yyyyMmDd = yyyyMmDd;
        this.priceCache = priceCache;
    }

    // return a new mutable container for performance data
    @Override
    public Supplier<PerformanceValues> supplier() {
        return PerformanceValues::new;
    }

    // fold analysis, previous close data into a performance data container
    @Override
    public BiConsumer<PerformanceValues, Entry<K, Analysis>> accumulator() {
        return (collected, entry) -> {
            // add values for entry to be collected
            Analysis analysis = entry.getValue();

            collected.previousClose = priceCache
                    .getPreviousClose(null, // profile
                            analysis.getTargetCurrency(),
                            analysis.getSecurity(),
                            yyyyMmDd)
                    .orElse(null);

            BigDecimal target = analysis.getTarget();
            // the performance...
            if (target != null && collected.previousClose != null) {
                collected.sum = collected.sum.add(
                        target.divide(collected.previousClose, SCALE, RoundingMode.HALF_UP)
                                .subtract(BigDecimal.ONE));
                collected.count = collected.count.add(BigDecimal.ONE);
            } else {
                collected.sum = null;
                collected.count = null;
            }
            // the count
        };
    }

    // combine two data container by adding the values
    @Override
    public BinaryOperator<PerformanceValues> combiner() {
        return (left, right) -> {
            left.sum = left.sum.add(right.sum);
            left.count = left.count.add(right.count);
            return left;
        };
    }

    // identity transformation
    @Override
    public Function<PerformanceValues, PerformanceValues> finisher() {
        return  identity -> identity;
    }

    // identity transformation
    @Override
    public Set<Characteristics> characteristics() {
        return CH_ID;
    }


    // data container for calculating and aggregating performance values
    // (expected-)performance is a feature of the analysis, might be aggregated and
    // depends on the previous day price value (previous clse)
    public static class PerformanceValues {

        // success rate defined for a specific analysis,
        private BigDecimal sum = BigDecimal.ZERO;  //  ((target / previousClose) - 1)

        private BigDecimal count = BigDecimal.ZERO;  // to calculate average value e.g. for agencies

        private BigDecimal previousClose = BigDecimal.ZERO;

        // might return null if the value is undefined
        public BigDecimal getAverageValue() { // == value for count == 1
            if (sum == null || count == null) {
                return null;
            } else {
                return sum.divide(count, SCALE, RoundingMode.HALF_UP);
            }
        }

        public BigDecimal getCount() {
            return count;
        }

        public BigDecimal getSum() {
            return sum;
        }

        public BigDecimal getPreviousClose() {
            return previousClose;
        }

        @Override
        public String toString() {
            return "Performance{" +
                    " sum=" + sum +
                    ", count=" + count +
                    '}';
        }

    }

}
