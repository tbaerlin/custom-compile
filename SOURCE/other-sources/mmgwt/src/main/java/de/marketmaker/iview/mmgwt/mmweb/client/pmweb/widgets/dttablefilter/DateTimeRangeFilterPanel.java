/*
 * DateTimeRangeFilterPanel.java
 *
 * Created on 23.03.2015 12:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.HasValue;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.DateTimeRange;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.DateTimeRangeBox;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTSingleRow;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMDateTime;

/**
 * @author mdick
 */
public class DateTimeRangeFilterPanel extends AbstractFilterPanel<DateTimeRange, DTCell> {
    @Override
    protected HasValue<DateTimeRange> createEditorWidget() {
        final DateTimeRangeBox dateTimeRangeBox = new DateTimeRangeBox();
        dateTimeRangeBox.addValueChangeHandler(new ValueChangeHandler<DateTimeRange>() {
            @Override
            public void onValueChange(ValueChangeEvent<DateTimeRange> event) {
                final DateTimeRange value = event.getValue();
                if(isValueAddable(value) && !isEditorValueEnabled()) {
                    setEditorValueEnabled(true);
                }
                fireChangeEvent();
            }
        });
        return dateTimeRangeBox;
    }

    @Override
    protected IsWidget createValueWidget(DateTimeRange value) {
        final String label = (value.hasBegin()
                ? I18n.I.from() + " " + JsDateFormatter.format(value.getBegin(), JsDateFormatter.Format.DMY)
                : "")
                + (value.hasBegin() && value.hasEnd() ? " " : "")
                + (value.hasEnd()
                ? I18n.I.to() + " " + JsDateFormatter.format(value.getEnd(), JsDateFormatter.Format.DMY)
                : "");

        return new Label(label);
    }

    @Override
    protected DTTableRenderer.ColumnFilter createFilter(DateTimeRange value) {
        return new DateTimeRangeFilter(getMetadata().getColumnIndex(), value);
    }

    @Override
    protected boolean isValueAddable(DateTimeRange value) {
        return value != null && (
                (value.hasBegin() && !value.hasEnd())
                        || (value.hasEnd() && !value.hasBegin())
                        || (value.hasBegin() && value.hasEnd() && value.getBegin().isBefore(value.getEnd())));
    }

    private static class DateTimeRangeFilter implements DTTableRenderer.ColumnFilter {
        private final int columnIndex;
        private final DateTimeRange value;
        private final long startTime;
        private final long endTime;

        public DateTimeRangeFilter(int columnIndex, DateTimeRange value) {
            this.columnIndex = columnIndex;
            this.value = value;
            if(value != null) {
                this.startTime = value.hasBegin() ? value.getBegin().getTime() : Long.MIN_VALUE;
                this.endTime = value.hasEnd() ? value.getEnd().getTime() : Long.MAX_VALUE;
            }
            else {
                this.startTime = 0;
                this.endTime = 0;
            }
        }

        @Override
        public boolean isAcceptable(DTSingleRow row) {
            if(this.value == null) {
                return true;
            }

            final DTCell cell = row.getCells().get(this.columnIndex);
            final MM item = cell.getItem();
            final MMDateTime mmDateTime = item instanceof MMDateTime ? (MMDateTime) item : null;
            final MmJsDate mmJsDate = mmDateTime != null ? JsDateFormatter.parseDdmmyyyy(mmDateTime.getValue()) : null;

            if(mmJsDate == null) {
                return false;
            }

            final long time = mmJsDate.getTime();
            return this.startTime <= time && time <= this.endTime;
        }
    }
}
