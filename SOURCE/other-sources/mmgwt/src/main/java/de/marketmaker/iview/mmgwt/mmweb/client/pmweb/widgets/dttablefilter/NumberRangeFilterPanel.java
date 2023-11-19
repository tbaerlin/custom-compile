/*
 * NumberRangeFilterPanel.java
 *
 * Created on 11.08.2014 14:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidatingBigDecimalBox;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.NumberRange;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.NumberRangeBox;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTSingleRow;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.tools.i18n.NonNLS;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * @author mdick
 */
public class NumberRangeFilterPanel extends AbstractFilterPanel<NumberRange, DTCell> {
    @Override
    public HasValue<NumberRange> createEditorWidget() {
        final NumberRangeBox numberRangeBox = new NumberRangeBox().withPercent(getMetadata().isPercent());
        numberRangeBox.addValueChangeHandler(new ValueChangeHandler<NumberRange>() {
            @Override
            public void onValueChange(ValueChangeEvent<NumberRange> event) {
                final NumberRange value = event.getValue();
                if(value != null && value.isComplete() && !isEditorValueEnabled()) {
                    setEditorValueEnabled(true);
                }
                fireChangeEvent();
            }
        });
        return numberRangeBox;
    }

    @Override
    public IsWidget createValueWidget(NumberRange value) {
        final String label = (value.hasMin() ? I18n.I.from() + " " + toString(value.getMin()) : "")
                + (value.isComplete() ? " " : "")
                + (value.hasMax() ? I18n.I.to() + " " + toString(value.getMax()) : "");

        return new Label(label);
    }

    private String toString(BigDecimal value) {
        final BigDecimal min = getMetadata().isPercent() ? value.movePointRight(2) : value;
        return ValidatingBigDecimalBox.BigDecimalRenderer.instance().render(min);
    }

    @Override
    public DTTableRenderer.ColumnFilter createColumnFilter() {
        final List<Boolean> valuesEnabled = getValuesEnabled();
        final ArrayList<NumberRangeFilter> valueFilters = new ArrayList<>();

        for(int i = 0; i < valuesEnabled.size(); i++) {
            if(!valuesEnabled.get(i)) {
                continue;
            }
            final NumberRange value = getValues().get(i);
            final NumberRangeFilter filter = createSimpleNumberRangeFilter(value);
            if(filter != null) {
                valueFilters.add(filter);
            }
        }
        if(isEditorValueEnabled()) {
            final NumberRange value = getEditorWidget().getValue();
            if(isValueAddable(value)) {
                final NumberRangeFilter filter = createSimpleNumberRangeFilter(value);
                if (filter != null) {
                    valueFilters.add(filter);
                }
            }
        }

        if(valueFilters.isEmpty()) {
            return null;
        }

        final FilterMetadata<DTCell> metadata = getMetadata();
        return new InclusiveNumberRangeFilter(metadata.getColumnIndex(), metadata.isPercent(), valueFilters);
    }

    @Override
    public DTTableRenderer.ColumnFilter createFilter(NumberRange value) {
        throw new UnsupportedOperationException();
    }

    public NumberRangeFilter createSimpleNumberRangeFilter(NumberRange value) {
        if(!isValueAddable(value)) {
            return null;
        }

        return new NumberRangeFilter(value);
    }

    @Override
    protected boolean isValueAddable(NumberRange value) {
        return value != null && (
                (value.hasMin() && !value.hasMax())
                        || (value.hasMax() && !value.hasMin())
                        || (value.isComplete() && value.getMin().compareTo(value.getMax()) <= 0));
    }

    /**
     * This filter implementation avoids parsing the BigDecimal number several times.
     */
    @NonNLS
    private static class InclusiveNumberRangeFilter implements DTTableRenderer.ColumnFilter {
        private final int columnIndex;
        private final boolean percent;
        private final List<NumberRangeFilter> filters;

        private InclusiveNumberRangeFilter(int colIndex, boolean percent, List<NumberRangeFilter> filters) {
            this.columnIndex = colIndex;
            this.percent = percent;
            this.filters = filters;
        }

        /**
         * Remark: this filter works directly with MM values as the filters in PM also do.
         * Hence, it reproduces PM's current behaviour with rounded values: if a number is formatted to show 2 decimal
         * digits, e.g. 2,32 (in German format) but the original MM value is 2.315, a min value of the number range of
         * of 2.32 will not cover the formatted value that is shown to the users, because filtering is done on the
         * original MM value of 2.315, which is indeed less than 2.32.
         */
        @Override
        public boolean isAcceptable(DTSingleRow row) {
            final MM mm = row.getCells().get(this.columnIndex).getItem();
            final String mmValue = mm instanceof de.marketmaker.iview.pmxml.HasValue
                    ? ((de.marketmaker.iview.pmxml.HasValue) mm).getValue()
                    : null;

            try {
                final BigDecimal value = mmValue != null ? new BigDecimal(mmValue) : null;

                for (NumberRangeFilter filter : this.filters) {
                    if (filter.isAcceptable(value)) {
                        return true;
                    }
                }
                return false;
            }
            catch(NumberFormatException nfe) {
                Firebug.warn("<InclusiveNumberRangeFilter.isAcceptable> failed to parse BigDecimal: \"" + mmValue + "\"", nfe);
                return false;
            }
        }

        @Override
        public String toString() {
            return "InclusiveNumberRangeFilter{" +
                    "columnIndex=" + columnIndex +
                    ", percent=" + percent +
                    ", filters=" + filters +
                    '}';
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof InclusiveNumberRangeFilter)) return false;

            final InclusiveNumberRangeFilter that = (InclusiveNumberRangeFilter) o;

            if (columnIndex != that.columnIndex) return false;
            if (percent != that.percent) return false;
            if (filters != null ? !filters.equals(that.filters) : that.filters != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = columnIndex;
            result = 31 * result + (percent ? 1 : 0);
            result = 31 * result + (filters != null ? filters.hashCode() : 0);
            return result;
        }
    }

    @NonNLS
    private static class NumberRangeFilter {
        private final BigDecimal min;
        private final BigDecimal max;

        private NumberRangeFilter(NumberRange numberRange) {
            this.min = numberRange.getMin();
            this.max = numberRange.getMax();
        }

        public boolean isAcceptable(BigDecimal value) {
            if(this.min == null && this.max == null) {
                return true;
            }
            else if(this.min == null) {
                return value.compareTo(this.max) <= 0;
            }
            else if(this.max == null) {
                return this.min.compareTo(value) <= 0;
            }
            else {
                return this.min.compareTo(value) <= 0 && value.compareTo(this.max) <= 0;
            }
        }

        @Override
        public String toString() {
            return "NumberRangeFilter{" +
                    "min=" + min +
                    ", max=" + max +
                    '}';
        }

        @SuppressWarnings("RedundantIfStatement")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof NumberRangeFilter)) return false;

            final NumberRangeFilter that = (NumberRangeFilter) o;

            if (max != null ? !max.equals(that.max) : that.max != null) return false;
            if (min != null ? !min.equals(that.min) : that.min != null) return false;

            return true;
        }

        @Override
        public int hashCode() {
            int result = min != null ? min.hashCode() : 0;
            result = 31 * result + (max != null ? max.hashCode() : 0);
            return result;
        }
    }
}
