/*
* SpsDateTimeEdit.java
*
* Created on 02.06.2014 10:15
*
* Copyright (c) vwd AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.MultiWidgetFocusSupport;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidationFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.TimeBox;
import de.marketmaker.iview.pmxml.MMDateTime;
import de.marketmaker.iview.pmxml.TiDateKind;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
public class SpsDateTimeEdit extends SpsBoundWidget<Widget, SpsLeafProperty> implements HasCaption,
        RequiresPropertyUpdateBeforeSave, HasFocusHandlers, HasBlurHandlers {
    static final JsDateFormatter.Format JS_DATE_FORMAT = getJsDateFormat();
    public static final DateBox.Msg MSG = new DateBox.Msg() {
        @Override
        public String beforeMin(MmJsDate minDate, JsDateFormatter.Format format) {
            return I18n.I.spsDateFormatBeforeMin(JsDateFormatter.format(minDate, format));
        }

        @Override
        public String afterMax(MmJsDate maxDate, JsDateFormatter.Format format) {
            return I18n.I.spsDateFormatAfterMax(JsDateFormatter.format(maxDate, format));
        }

        @Override
        public String invalidDateFormat(String invalidDate) {
            return I18n.I.spsDateFormatDescription();
        }
    };

    private TiDateKind dateKind = TiDateKind.DK_DATE;
    private boolean withSeconds = false;

    private DateBox dateBox = null;
    private TimeBox timeBox = null;

    private static JsDateFormatter.Format getJsDateFormat() {
        try {
            return JsDateFormatter.Format.valueOf(I18n.I.spsDateFormat());
        }
        catch (Exception e) {
            return JsDateFormatter.Format.ISO_DAY;
        }
    }

    @Override
    public SpsDateTimeEdit withCaption(String caption) {
        setCaption(caption);
        return this;
    }

    public void setDateKind(TiDateKind dateKind) {
        if (dateKind == null) {
            this.dateKind = TiDateKind.DK_DATE;
        }
        this.dateKind = dateKind;
    }

    public SpsDateTimeEdit withDateKind(TiDateKind dateKind) {
        setDateKind(dateKind);
        return this;
    }

    public SpsDateTimeEdit withSeconds(boolean withSeconds) {
        this.withSeconds = withSeconds;
        return this;
    }

    private void updatePropertyDate(MmJsDate date) {
        if (date == null) {
            getBindFeature().getSpsProperty().setNullValue();
            return;
        }

        switch (this.dateKind) {
            case DK_DATE:
                date = date.atMidnight();
                break;

            case DK_DATE_TIME:
            default:
                MmJsDate time = getPropertyValue();
                if (time == null) {
                    time = new MmJsDate();
                }
                applyTime(date, time);
                break;
        }

        updateProperty(date);
    }

    protected void updatePropertyTime(MmJsDate time) {
        MmJsDate date = getPropertyValue();

        switch (this.dateKind) {
            case DK_DATE_TIME:
                if (date == null && time == null) {
                    getBindFeature().getSpsProperty().setNullValue();
                    return;
                }

                if (date == null) {
                    date = new MmJsDate();
                }
                if (time != null) {
                    applyTime(date, time);
                }
                else {
                    date = date.atMidnight();
                }
                break;

            case DK_TIME:
                if (time == null) {
                    getBindFeature().getSpsProperty().setNullValue();
                    return;
                }
                else {
                    if(date == null) {
                        date = time;
                    }
                    else {
                        //if we do not apply the time, we may change the date time value,
                        //if someone changes the time value and sets it then back to the initial value.
                        //Visually the time is the same, but the underlying date time will be the
                        //date of today which is likely not the same value that was initially set.
                        applyTime(date, time);
                    }
                }
                break;
        }

        updateProperty(date);
    }

    private void updateProperty(MmJsDate date) {
        if(!CompareUtil.equals(date, getPropertyValue())) {
            getBindFeature().getSpsProperty().setValue(MmTalkHelper.asMMDateTime(date));
        }
    }

    private void applyTime(MmJsDate target, MmJsDate time) {
        target.setHours(time.getHours());
        target.setMinutes(time.getMinutes());
        target.setSeconds(time.getSeconds());
        target.setMilliseconds(time.getMilliseconds());
    }

    @Override
    public void onPropertyChange() {
        updateValue();
    }

    private void updateValue() {
        final MmJsDate date = getPropertyValue();
        if (this.dateBox != null) {
            this.dateBox.setDate(date, false);
        }
        if (this.timeBox != null) {
            this.timeBox.setValue(date, false);
            this.timeBox.getValidationFeature().validate();
        }
    }

    protected MmJsDate getPropertyValue() {
        final MMDateTime diDateTime = getBindFeature().getSpsProperty().getDate();
        return diDateTime == null ? null : JsDateFormatter.parseDdmmyyyy(diDateTime.getValue(), true, MSG);
    }

    @Override
    public void updatePropertyBeforeSave() {
        if (this.dateBox != null) {
            updatePropertyDate(this.dateBox.getDate());
        }
        if (this.timeBox != null) {
            updatePropertyTime(this.timeBox.getValue());
        }
    }

    @Override
    protected Widget createWidget() {
        final MessagePopup messagePopupImpl = createMessagePopupImpl();

        if (this.dateKind == TiDateKind.DK_DATE || this.dateKind == TiDateKind.DK_DATE_TIME) {
            this.dateBox = DateBox.factory()
                    .withAllowNull()
                    .withMsg(MSG)
                    .withFormat(JS_DATE_FORMAT)
                    .withIconWidet(IconImage.getIcon("sps-calendar")) // $NON-NLS$
                    .withMessagePopup(messagePopupImpl)
                    .build();
            this.dateBox.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
                @Override
                public void onValueChange(ValueChangeEvent<MmJsDate> event) {
                    updatePropertyDate(event.getValue());
                }
            });
        }

        if (this.dateKind == TiDateKind.DK_TIME || this.dateKind == TiDateKind.DK_DATE_TIME) {
            final ValidationFeature.FormatDescriptionProvider formatDescriptionProvider = new ValidationFeature.FormatDescriptionProvider() {
                @Override
                public String getFormatDescription() {
                    return withSeconds ? I18n.I.spsTimeFormatDescription(TimeBox.TimeFormat.HHMMSS.getPlaceholder()) : I18n.I.spsTimeFormatDescription(TimeBox.TimeFormat.HHMM.getPlaceholder());
                }
            };
            this.timeBox = new TimeBox(withSeconds ? TimeBox.TimeFormat.HHMMSS : TimeBox.TimeFormat.HHMM, formatDescriptionProvider, messagePopupImpl);
            this.timeBox.setStyleName("sps-edit");  // $NON-NLS$
            this.timeBox.addStyleName("time");  // $NON-NLS$
            this.timeBox.setMandatory(false); // see DateBox.factory().withAllowNull()
            this.timeBox.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
                @Override
                public void onValueChange(ValueChangeEvent<MmJsDate> event) {
                    final MmJsDate value = event.getValue();
                    updatePropertyTime(value);
                    formatTimeBox(value);
                }
            });
        }

        if (this.dateKind == TiDateKind.DK_DATE_TIME) {
            final HorizontalPanel panel = new HorizontalPanel();
            panel.add(this.dateBox);
            final Label timeLabel = new Label(I18n.I.time());
            timeLabel.setStyleName("sps-time-label");
            panel.add(timeLabel);
            panel.add(this.timeBox);
            panel.addStyleName("sps-dateTime");
            return panel;
        }
        else {
            return this.dateBox != null ? this.dateBox : this.timeBox;
        }
    }

    private void formatTimeBox(MmJsDate time) {
        final MmJsDate date = getPropertyValue();

        if (date == null && time == null) {
            return;
        }

        if (time == null && this.dateKind == TiDateKind.DK_DATE_TIME) {
            this.timeBox.setValue(date, false);
        }
    }

    @Override
    protected Focusable getFocusable() {
        return this.dateBox != null ? this.dateBox : this.timeBox;
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return MultiWidgetFocusSupport.addBlurHandler(handler, this.dateBox, this.timeBox);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return MultiWidgetFocusSupport.addFocusHandler(handler, this.dateBox, this.timeBox);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        if (this.dateBox != null) {
            this.dateBox.fireEvent(event);
        }
        else {
            this.timeBox.fireEvent(event);
        }
    }
}