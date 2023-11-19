/*
 * BewFunction.java
 *
 * Created on 15.12.2010 07:16:29
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.bew;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.springframework.util.StringUtils;

import de.marketmaker.istar.common.util.DateUtil;
import de.marketmaker.istar.merger.Constants;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
abstract class BewFunction {
    private static final String DATE_PREFIX = "DATE:";

    private static final String FILTER_PREFIX = "FILTER:";

    private BewFunction() {
    }

    static BewFunction create(String definition, boolean isDateField) {
        if ("LAST".equals(definition)) {
            return new LastItem();
        }
        else if ("AVG".equals(definition)) {
            return new AvgItem();
        }
        else if ("MIN".equals(definition)) {
            return isDateField ? new MinDateItem() : new MinItem();
        }
        else if ("MAX".equals(definition)) {
            return isDateField ? new MaxDateItem() :new MaxItem();
        }
        else if ("LIST".equals(definition)) {
            return new ListItem();
        }
        else if (definition.startsWith(DATE_PREFIX)) {
            return new DateItem(definition.substring(DATE_PREFIX.length()));
        }
        else if (definition.startsWith(FILTER_PREFIX)) {
            return new FilterItem(definition.substring(FILTER_PREFIX.length()));
        }

        return new EmptyItem();
    }

    void handle(String item) {
    }

    abstract String getResult();

    void reset() {
    }

    boolean isFilter() {
        return false;
    }

    boolean isValid(String item) {
        return true;
    }

    private static class EmptyItem extends BewFunction {
        @Override
        String getResult() {
            return "";
        }
    }

    private static class LastItem extends BewFunction {
        private String result;

        @Override
        void handle(String item) {
            this.result = item;
        }

        @Override
        String getResult() {
            return this.result != null ? this.result : "";
        }

        @Override
        void reset() {
            this.result = null;
        }
    }

    private static class ListItem extends BewFunction {
        private List<String> items = new ArrayList<>();

        @Override
        void handle(String item) {
            this.items.add(item);
        }

        @Override
        String getResult() {
            return this.items.toString();
        }

        @Override
        void reset() {
            this.items.clear();
        }
    }

    private static class FilterItem extends ListItem {
        private Set<String> validItems = new HashSet<>();

        private FilterItem(String definition) {
            this.validItems.addAll(Arrays.asList(
                    definition.substring(1, definition.length() - 1).split(Pattern.quote(","))));
        }

        @Override
        boolean isFilter() {
            return true;
        }

        @Override
        boolean isValid(String item) {
            return this.validItems.contains(item);
        }
    }

    private static class DateItem extends ListItem {
        private DateTime mininumDate;

        private DateItem(String period) {
            final Period p = DateUtil.getPeriod(period);
            this.mininumDate = new DateTime().withTimeAtStartOfDay().minus(p);
        }

        @Override
        boolean isFilter() {
            return true;
        }

        @Override
        boolean isValid(String item) {
            return StringUtils.hasText(item)
                    && BewFields.DATE_FMT.parseDateTime(item).isAfter(this.mininumDate);
        }
    }

    private static class AvgItem extends BewFunction {
        private List<BigDecimal> items = new ArrayList<>();

        @Override
        void handle(String item) {
            if (StringUtils.hasText(item)) {
                this.items.add(new BigDecimal(item));
            }
        }

        @Override
        String getResult() {
            if (this.items.isEmpty()) {
                return "";
            }
            BigDecimal sum = BigDecimal.ZERO;
            for (final BigDecimal item : items) {
                sum = sum.add(item);
            }
            return sum.divide(BigDecimal.valueOf(this.items.size()), Constants.MC).toPlainString();
        }

        @Override
        void reset() {
            this.items.clear();
        }
    }

    private static class MaxDateItem extends BewFunction {
        private DateTime max = null;

        @Override
        void handle(String item) {
            if (StringUtils.hasText(item)) {
                final DateTime value = BewFields.DATE_FMT.parseDateTime(item);
                if (this.max == null) {
                    this.max = value;
                }
                this.max = this.max.isAfter(value) ? this.max : value;
            }
        }

        @Override
        String getResult() {
            if (this.max == null) {
                return "";
            }
            return BewFields.DATE_FMT.print(this.max);
        }

        @Override
        void reset() {
            this.max = null;
        }
    }

    private static class MinDateItem extends BewFunction {
        private DateTime min = null;

        @Override
        void handle(String item) {
            if (StringUtils.hasText(item)) {
                final DateTime value = BewFields.DATE_FMT.parseDateTime(item);
                if (this.min == null) {
                    this.min = value;
                }
                this.min = this.min.isBefore(value) ? this.min : value;
            }
        }

        @Override
        String getResult() {
            if (this.min == null) {
                return "";
            }
            return BewFields.DATE_FMT.print(this.min);
        }

        @Override
        void reset() {
            this.min = null;
        }
    }

    private static class MaxItem extends BewFunction {
        private BigDecimal max = null;

        @Override
        void handle(String item) {
            if (StringUtils.hasText(item)) {
                final BigDecimal value = new BigDecimal(item);
                if (this.max == null) {
                    this.max = value;
                }
                this.max = this.max.compareTo(value) > 0 ? this.max : value;
            }
        }

        @Override
        String getResult() {
            if (this.max == null) {
                return "";
            }
            return this.max.toPlainString();
        }

        @Override
        void reset() {
            this.max = null;
        }
    }

    private static class MinItem extends BewFunction {
        private BigDecimal min = null;

        @Override
        void handle(String item) {
            if (StringUtils.hasText(item)) {
                final BigDecimal value = new BigDecimal(item);
                if (this.min == null) {
                    this.min = value;
                }
                this.min = this.min.compareTo(value) < 0 ? this.min : value;
            }
        }

        @Override
        String getResult() {
            if (this.min == null) {
                return "";
            }
            return this.min.toPlainString();
        }

        @Override
        void reset() {
            this.min = null;
        }
    }
}
