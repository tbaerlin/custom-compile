package de.marketmaker.itools.gwtutil.client.widgets.datepicker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.itools.gwtutil.client.i18n.GwtUtilI18n;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.SimpleButton;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class DatePicker extends Composite implements EventListener {
    private static final String LID_ATTR = "mm:datemillis";
    private final Element elementHtml;
    private MmJsDate currentDate;
    private MmJsDate minDate;
    private MmJsDate maxDate;
    private final SimpleButton buttonMonth;
    private final MonthPicker monthPicker;
    private final IntervalPicker intervalPicker;
    private List<DateListener> listeners = new ArrayList<DateListener>();

    public DatePicker() {
        this(IntervalPicker.IntervalMode.PAST, false);
    }

    public DatePicker(boolean alltimeEnabled) {
        this(IntervalPicker.IntervalMode.PAST, alltimeEnabled);
    }

    public DatePicker(IntervalPicker.IntervalMode intervalMode) {
        this(intervalMode, false);
    }

    public DatePicker(IntervalPicker.IntervalMode intervalMode, boolean alltimeEnabled) {
        final FlowPanel panel = new FlowPanel();
        //noinspection GWTStyleCheck
        panel.setStyleName("mm-date-picker mm-unselectable");
        panel.setWidth("175px");

        final FlexTable table = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();

        final DateLink aDateLeft = new DateLink();
        aDateLeft.setStyleName("mm-unselectable");
        aDateLeft.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                setDate(currentDate.getPreviousMonth());
            }
        });

        this.buttonMonth = new SimpleButton("");
        this.buttonMonth.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showMonthPicker();
            }
        });

        final DateLink aDateRight = new DateLink();
        aDateRight.setStyleName("mm-unselectable");
        aDateRight.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                setDate(currentDate.getNextMonth());
            }
        });

        final SimpleButton buttonInterval = new SimpleButton(GwtUtilI18n.I.period());
        buttonInterval.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                showIntervalPicker();
            }
        });

        table.setCellSpacing(0);
        table.setCellPadding(0);
        table.setWidth("175px");
        table.setWidget(0, 0, aDateLeft);
        formatter.setStyleName(0, 0, "mm-date-left");

        table.setWidget(0, 1, this.buttonMonth);
        formatter.setStyleName(0, 1, "mm-date-middle");
        formatter.getElement(0, 1).setAttribute("align", "center");

        table.setWidget(0, 2, aDateRight);
        formatter.setStyleName(0, 2, "mm-date-right");

        formatter.setColSpan(1, 0, 3);
        table.setHTML(1, 0, "&nbsp;");
        this.elementHtml = formatter.getElement(1, 0);

        formatter.setColSpan(2, 0, 3);
        table.setWidget(2, 0, buttonInterval);
        formatter.setStyleName(2, 0, "mm-date-middle");
        formatter.getElement(2, 0).setAttribute("align", "center");

        panel.add(table);

        final MmJsDate now = new MmJsDate();
        setDate(now);

        this.monthPicker = new MonthPicker();
        this.monthPicker.addListener(new DateListener(){
            public void setDate(MmJsDate date) {
                DatePicker.this.setDate(date);
            }
        });
        panel.add(this.monthPicker);

        this.intervalPicker = new IntervalPicker(intervalMode, alltimeEnabled);
        this.intervalPicker.addListener(new DateListener(){
            public void setDate(MmJsDate date) {
                fireDate(date);
            }
        });
        panel.add(this.intervalPicker);

        initWidget(panel);
    }


    public void setMinDate(MmJsDate minDate) {
        this.minDate = minDate == null ? null : minDate.getMidnight();
        refresh();
    }

    public void setMaxDate(MmJsDate maxDate) {
        this.maxDate = maxDate == null ? null : maxDate.getMidnight();
        refresh();
    }


    void hideSubPanels() {
        this.monthPicker.hide();
        this.intervalPicker.hide();
    }


    private void showMonthPicker() {
        this.monthPicker.show(this.currentDate);
    }


    private void showIntervalPicker() {
        this.intervalPicker.show();
    }


    public void refresh() {
        setDate(this.currentDate);
    }


    public void setDate(final MmJsDate mmJsDate) {
        final StringBuilder sb = new StringBuilder();

        if (mmJsDate == null) {
            this.currentDate = new MmJsDate();
            return;
        }
        
        this.currentDate = mmJsDate;
        final int currentDayOfMonth = mmJsDate.getDate();
        final MmJsDate firstOfMonth = mmJsDate.getFirstOfMonth();
        final int month = mmJsDate.getMonth();
        final MmJsDate currentDay = firstOfMonth.getPreviousMonday();
        final MmJsDate today = new MmJsDate();

        this.buttonMonth.setText(JsDateFormatter.MONTH_NAMES[firstOfMonth.getMonth()] + " " + firstOfMonth.getFullYear());

        sb.append("<table class=\"mm-date-inner\" cellspacing=\"0\">");
        sb.append("<thead><tr>");
        for (String dayName1 : JsDateFormatter.DAY_NAMES_1) {
            sb.append("<th><span>").append(dayName1).append("</span></th>");
        }
        sb.append("</tr></thead>");
        sb.append("<tbody>");
        for (int week = 0; week < 6; week++) {
            sb.append("<tr>");
            for (int day = 0; day < 7; day++) {
                sb.append("<td class=\"");
                final int currentMonth = currentDay.getMonth();
                if (this.minDate != null && currentDay.isBefore(this.minDate)) {
                    sb.append("mm-date-disabled");
                }
                else if (this.maxDate != null && currentDay.isAfter(this.maxDate)) {
                    sb.append("mm-date-disabled");
                }
                else if (currentMonth == month) {
                    sb.append("mm-date-active");
                    if (currentDay.getDate() == currentDayOfMonth) {
                        sb.append(" mm-date-selected");
                    }
                }
                else if (currentMonth < month) {
                    sb.append("mm-date-prevday");
                }
                else {
                    sb.append("mm-date-nextday");
                }
                if (currentDay.isSameDay(today)) {
                    sb.append(" mm-date-today");
                }
                sb.append("\"><a class=\"mm-date-date\" href=\"#\" ").append(LID_ATTR).append("=\"").append(currentDay.getTime()).append("\"><em><span>");
                sb.append(currentDay.getDate());
                sb.append("</span></em></a></td>");

                currentDay.addDays(1);
            }
            sb.append("</tr>");
        }
        sb.append("</tbody>");
        sb.append("</table>");

        this.elementHtml.setInnerHTML(sb.toString());
        launderLinks(this.elementHtml);
    }

    private void fireDate(MmJsDate date) {
        setDate(date);
        if (date != null) {
            if (this.minDate != null && date.isBefore(this.minDate)) {
                return;
            }
            if (this.maxDate != null && date.isAfter(this.maxDate)) {
                return;
            }
        }
        for (DateListener listener : listeners) {
            listener.setDate(date);
        }
    }


    private void launderLinks(final Element e) {
        final JavaScriptObject list = DOMUtil.getElementsByTagNameImpl(e, "a");
        final int n = DOMUtil.getLength(list);
        for (int i = 0; i < n; i++) {
            final Element a = DOMUtil.getElementFromList(i, list);
            final String type = DOM.getElementAttribute(a, LID_ATTR);
            if (type == null) {
                continue;
            }
            DOM.sinkEvents(a, Event.ONCLICK | DOM.getEventsSunk(a));
            DOM.setEventListener(a, this);
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) != Event.ONCLICK) {
            return;
        }
        final Element e = DOM.eventGetCurrentTarget(event);
        fireDate(new MmJsDate(Long.valueOf(DOM.getElementAttribute(e, LID_ATTR))));
        DOM.eventPreventDefault(event);
    }

    public void addListener(DateListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(DateListener listener) {
        this.listeners.remove(listener);
    }
}
