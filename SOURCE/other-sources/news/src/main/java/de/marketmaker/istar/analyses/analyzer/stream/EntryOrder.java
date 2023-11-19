package de.marketmaker.istar.analyses.analyzer.stream;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Map.Entry;

import org.springframework.util.StringUtils;

import de.marketmaker.istar.analyses.analyzer.Security;
import de.marketmaker.istar.analyses.analyzer.collect.PerformanceCollector.PerformanceValues;
import de.marketmaker.istar.analyses.analyzer.collect.RatingCollector.RatingValues;
import de.marketmaker.istar.analyses.analyzer.collect.SuccessCollector.SuccessValues;

/**
 * a set of comparators to order entry streams
 */
public interface EntryOrder<K, V> extends Comparator<Entry<K, V>> {

    int LEFT_FIRST = -1;
    int RIGHT_FIRST = +1;
    int EQUAL = 0;

    class TotalCountOrder<K> implements EntryOrder<K, RatingValues> {
        @Override
        public int compare(Entry<K, RatingValues> left, Entry<K, RatingValues> right) {
            return Integer.compare(right.getValue().getTotal(), left.getValue().getTotal());
        }
    }

    class BuyCountOrder<K> implements EntryOrder<K, RatingValues> {
        @Override
        public int compare(Entry<K, RatingValues> left, Entry<K, RatingValues> right) {
            return Integer.compare(right.getValue().getBuy(), left.getValue().getBuy());
        }
    }

    class SellCountOrder<K> implements EntryOrder<K, RatingValues> {
        @Override
        public int compare(Entry<K, RatingValues> left, Entry<K, RatingValues> right) {
            return Integer.compare(right.getValue().getSell(), left.getValue().getSell());
        }
    }

    class HoldCountOrder<K> implements EntryOrder<K, RatingValues> {
        @Override
        public int compare(Entry<K, RatingValues> left, Entry<K, RatingValues> right) {
            return Integer.compare(right.getValue().getHold(), left.getValue().getHold());
        }
    }

    class BuySellRatioOrder<K> implements EntryOrder<K, RatingValues> {
        @Override
        public int compare(Entry<K, RatingValues> left, Entry<K, RatingValues> right) {
            int leftBuy = left.getValue().getBuy();
            int rightBuy = right.getValue().getBuy();
            int leftSell = left.getValue().getSell();
            int rightSell = right.getValue().getSell();

            if (leftSell == 0 && rightSell == 0) {
                return Integer.compare(rightBuy, leftBuy);
            }

            float leftRatio = leftSell == 0 ? Integer.MAX_VALUE : ((float)leftBuy / (float)leftSell);
            float rightRatio = rightSell == 0 ? Integer.MAX_VALUE : ((float)rightBuy / (float)rightSell);

            return Float.compare(leftRatio, rightRatio);
        }
    }

    class PerformanceCompare<K> implements EntryOrder<K, PerformanceValues> {
        private final boolean invert;

        public PerformanceCompare(boolean invert) {
            this.invert = invert;
        }

        private int invert() {
            return invert?-1:+1;
        }

        @Override
        public int compare(Entry<K, PerformanceValues> left, Entry<K, PerformanceValues> right) {
            final BigDecimal rightPerformance = right.getValue().getAverageValue();
            final BigDecimal leftPerformance = left.getValue().getAverageValue();

            if (rightPerformance == null && leftPerformance == null) {
                return EQUAL;
            }
            if (rightPerformance != null && leftPerformance != null) {
                return rightPerformance.compareTo(leftPerformance) * invert();
            }
            return (rightPerformance != null)? RIGHT_FIRST : LEFT_FIRST;
        }
    }

    class SuccessCompare<K> implements EntryOrder<K, SuccessValues> {
        @Override
        public int compare(Entry<K, SuccessValues> left, Entry<K, SuccessValues> right) {
            return Integer.compare(right.getValue().getCount(), left.getValue().getCount());
        }
    }

    class TotalCompare<K> implements EntryOrder<K, SuccessValues> {
        @Override
        public int compare(Entry<K, SuccessValues> left, Entry<K, SuccessValues> right) {
            return Integer.compare(right.getValue().getTotal(), left.getValue().getTotal());
        }
    }


    // invert order
    class Invert<K, V> implements EntryOrder<K, V> {

        private final EntryOrder<K, V> delegate;

        public Invert(EntryOrder<K, V> delegate) {
            this.delegate = delegate;
        }

        @Override
        public int compare(Entry<K, V> left, Entry<K, V> right) {
            return delegate.compare(right, left);
        }
    }

    abstract class InvertableStringCompare<V> implements EntryOrder<Security, V> {
        private final boolean invert;

        public InvertableStringCompare(boolean invert) {
            this.invert = invert;
        }

        private int invert() {
            return invert?-1:+1;
        }

        int doCompare(String left, String right) {

            if (!StringUtils.isEmpty(left) && !StringUtils.isEmpty(right)) {
                return left.compareToIgnoreCase(right) * invert();
            }
            if (StringUtils.isEmpty(left) && StringUtils.isEmpty(right)) {
                return EQUAL;
            }
            // always sort empty to the bottom
            return StringUtils.isEmpty(right)?LEFT_FIRST:RIGHT_FIRST;
        }
    }


    class SecurityNameCompare<V> extends InvertableStringCompare<V> {

        public SecurityNameCompare(boolean invert) {
            super(invert);
        }

        @Override
        public int compare(Entry<Security, V> left, Entry<Security, V> right) {
            return doCompare(left.getKey().getName(), right.getKey().getName());
        }
    }

    class SecuritySymbolCompare<V> extends InvertableStringCompare<V> {

        public SecuritySymbolCompare(boolean invert) {
            super(invert);
        }

        @Override
        public int compare(Entry<Security, V> left, Entry<Security, V> right) {
            return doCompare(left.getKey().getSymbol(), right.getKey().getSymbol());
        }
    }

}
