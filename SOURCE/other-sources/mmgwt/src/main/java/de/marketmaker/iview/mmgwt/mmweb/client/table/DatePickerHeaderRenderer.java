package de.marketmaker.iview.mmgwt.mmweb.client.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;

import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DatePicker;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DatePickerPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.Link;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;

/**
 * @author umaurer
 */
public class DatePickerHeaderRenderer implements TableCellRenderer, HasValueChangeHandlers<MmJsDate> {
    private String linkStyle = null;
    private String linkHtml = "<img class=\"mm-calendarIcon\" src=\"clear.cache.gif\"/>"; // $NON-NLS-0$
    private String linkQuickTip = null;
    private MmJsDate value = null;
    private MmJsDate minDate = null;
    private MmJsDate maxDate = null;
    private boolean alltimeEnabled = false;

    private final HandlerManager handlers = new HandlerManager(null);

    public void setLinkStyle(String linkStyle) {
        this.linkStyle = linkStyle;
    }

    public void setLinkHtml(String linkHtml) {
        this.linkHtml = linkHtml;
    }

    public void setLinkQuickTip(String linkQuickTip) {
        this.linkQuickTip = linkQuickTip;
    }

    public void setValue(MmJsDate value) {
        this.value = value;
    }

    public void setMinDate(MmJsDate minDate) {
        this.minDate = minDate;
    }

    public void setMaxDate(MmJsDate maxDate) {
        this.maxDate = maxDate;
    }

    public void setAlltimeEnabled(boolean alltimeEnabled) {
        this.alltimeEnabled = alltimeEnabled;
    }

    public void fireEvent(GwtEvent<?> event) {
        this.handlers.fireEvent(event);
    }

    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MmJsDate> handler) {
        return this.handlers.addHandler(ValueChangeEvent.getType(), handler);
    }

    public void render(Object data, StringBuffer sb, Context context) {
        sb.append((String) data);
        final Link link = new Link(new LinkListener<Link>() {
            public void onClick(LinkContext<Link> linkLinkContext, Element e) {
                showDatePicker(e);
            }
        }, this.linkHtml, this.linkQuickTip);
        if (this.linkStyle != null) {
            link.withStyle(this.linkStyle);
        }
        context.appendLink(new LinkContext<>(link.getListener(), link), link.getText(), link.getTooltip(), sb);
    }

    public String getContentClass() {
        return null;
    }

    private void showDatePicker(Element e) {
        final DatePicker datePicker = new DatePicker(this.alltimeEnabled);
        datePicker.addListener(new DateListener(){
            public void setDate(MmJsDate date) {
                value = date;
                ValueChangeEvent.fire(DatePickerHeaderRenderer.this, value);
            }
        });
        if (this.value != null) {
            datePicker.setDate(this.value);
        }
        if (this.minDate != null) {
            datePicker.setMinDate(this.minDate);
        }
        if (this.maxDate != null) {
            datePicker.setMaxDate(this.maxDate);
        }
        DatePickerPopup.show(datePicker, e);
    }

    public boolean isPushRenderer() {
        return false;
    }

    public static interface ValueChangeListener {
        void onValueChange(String period, String title);
    }

    public static DatePickerHeaderRenderer create(final ValueChangeListener listener) {
        final DatePickerHeaderRenderer result = new DatePickerHeaderRenderer();
        result.setLinkQuickTip(I18n.I.selectComparisonPeriod());
        result.setValue(new MmJsDate().addYears(-1));
        result.setMaxDate(new MmJsDate().addDays(-1));
        result.setAlltimeEnabled(true);
        result.addValueChangeHandler(new ValueChangeHandler<MmJsDate>() {
            public void onValueChange(ValueChangeEvent<MmJsDate> e) {
                final String period;
                final MmJsDate date = e.getValue();
                final String title;
                if (date == null) {
                    period = "P1000Y"; // $NON-NLS-0$
                    title = I18n.I.alltime();
                }
                else {
                    final long days = date.getDiffDays(new MmJsDate().getMidnight());
                    if (days > 1) {
                        period = "P" + days + "D"; // $NON-NLS-0$ $NON-NLS-1$
                        title = JsDateFormatter.formatDdmmyyyy(date);
                    }
                    else if (days == 1) {
                        period = "P1D"; // $NON-NLS-0$
                        title = I18n.I.previousDay();
                    }
                    else {
                        period = "P1000Y"; // $NON-NLS-0$
                        title = I18n.I.alltime();
                    }
                }
                listener.onValueChange(period, title);
            }
        });
        return result;
    }

}
