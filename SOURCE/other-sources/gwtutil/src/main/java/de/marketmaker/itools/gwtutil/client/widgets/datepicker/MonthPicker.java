package de.marketmaker.itools.gwtutil.client.widgets.datepicker;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.EventListener;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.i18n.GwtUtilI18n;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class MonthPicker extends Composite implements EventListener {
    final SimplePanel panel;
    final FlexTable table;
    final DateLink[] dateLinkMonths = new DateLink[12];
    final DateLink[] dateLinkYears = new DateLink[10];
    private final FlexTable.FlexCellFormatter formatter;
    private int currentDay = 1;
    private int currentMonth = -1;
    private int firstYear = Integer.MIN_VALUE;
    private int currentYearId = -1;
    private List<DateListener> listeners = new ArrayList<DateListener>();

    public MonthPicker() {
        this.panel = new SimplePanel();
        this.panel.setStyleName("mm-date-mp");
        this.panel.setWidth("175px");
        this.table = new FlexTable();
        this.table.setCellSpacing(0);
        this.table.setWidth("175px");
        this.formatter = this.table.getFlexCellFormatter();

        // months
        for (int i = 0; i < 6; i++) {
            final int m1 = i;
            this.dateLinkMonths[m1] = new DateLink(JsDateFormatter.MONTH_NAMES_SHORT[m1]);
            this.dateLinkMonths[m1].addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    selectMonth(m1);
                }
            });
            this.table.setWidget(i, 0, this.dateLinkMonths[m1]);
            this.formatter.setStyleName(i, 0, "mm-date-mp-month");

            // second month column
            final int m2 = i + 6;
            this.dateLinkMonths[m2] = new DateLink(JsDateFormatter.MONTH_NAMES_SHORT[m2]);
            this.dateLinkMonths[m2].addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    selectMonth(m2);
                }
            });
            this.table.setWidget(i, 1, this.dateLinkMonths[m2]);
            final String style = "mm-date-mp-month mm-date-mp-sep";
            this.formatter.setStyleName(i, 1, style);
        }

        // years prev
        final DateLink dateLinkYearPrev = new DateLink();
        dateLinkYearPrev.setStyleName("mm-date-mp-prev");
        dateLinkYearPrev.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                addYears(-10);
            }
        });
        this.table.setWidget(0, 2, dateLinkYearPrev);
        this.formatter.setStyleName(0, 2, "mm-date-mp-ybtn");

        // years next
        final DateLink dateLinkYearNext = new DateLink();
        dateLinkYearNext.setStyleName("mm-date-mp-next");
        dateLinkYearNext.addClickHandler(new ClickHandler(){
            public void onClick(ClickEvent event) {
                addYears(10);
            }
        });
        this.table.setWidget(0, 3, dateLinkYearNext);
        this.formatter.setStyleName(0, 3, "mm-date-mp-ybtn");

        this.firstYear = new MmJsDate().getFullYear() - 4;
        // years
        for (int i = 0; i < 5; i++) {
            final int yearId1 = i;
            this.dateLinkYears[yearId1] = new DateLink(String.valueOf(this.firstYear + yearId1));
            this.dateLinkYears[yearId1].addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    selectYear(yearId1);
                }
            });
            this.table.setWidget(i + 1, 2, this.dateLinkYears[yearId1]);
            this.formatter.setStyleName(i + 1, 2, "mm-date-mp-year");

            // second year column
            final int yearId2 = i + 5;
            this.dateLinkYears[yearId2] = new DateLink(String.valueOf(this.firstYear + yearId2));
            this.dateLinkYears[yearId2].addClickHandler(new ClickHandler(){
                public void onClick(ClickEvent event) {
                    selectYear(yearId2);
                }
            });
            this.table.setWidget(i + 1, 3, this.dateLinkYears[yearId2]);
            this.formatter.setStyleName(i + 1, 3, "mm-date-mp-year");
        }

        // buttons
        this.table.setHTML(6, 0, "<button class=\"mm-date-mp-ok\" type=\"button\">" + GwtUtilI18n.I.ok() + "</button>\n" +
                "<button class=\"mm-date-mp-cancel\" type=\"button\">" + GwtUtilI18n.I.cancel() + "</button>");
        this.table.getRowFormatter().setStyleName(6, "mm-date-mp-btns");
        this.formatter.setColSpan(6, 0, 4);
        launderButtons(this.formatter.getElement(6, 0));

        this.panel.setWidget(this.table);
        initWidget(this.panel);
    }

    private void launderButtons(final Element e) {
        final JavaScriptObject list = DOMUtil.getElementsByTagNameImpl(e, "button");
        final int n = DOMUtil.getLength(list);
        for (int i = 0; i < n; i++) {
            final Element button = DOMUtil.getElementFromList(i, list);
            DOM.sinkEvents(button, Event.ONCLICK | DOM.getEventsSunk(button));
            DOM.setEventListener(button, this);
        }
    }

    @Override
    public void onBrowserEvent(Event event) {
        if (DOM.eventGetType(event) != Event.ONCLICK) {
            return;
        }
        final Element e = DOM.eventGetCurrentTarget(event);
        handleButtonClick(e);
        DOM.eventPreventDefault(event);
    }

    private void handleButtonClick(Element e) {
        if ("mm-date-mp-ok".equals(e.getClassName())) {
            fireDate();
        }
        hide();
    }

    public void setDate(MmJsDate date) {
        this.currentDay = date.getDate();
        final int month = date.getMonth();
        final int year = date.getFullYear();

        selectMonth(month);
        this.firstYear = year - 4;
        for (int i = 0; i < 10; i++) {
            this.dateLinkYears[i].setText(String.valueOf(this.firstYear + i));
        }
        selectYear(4);
    }

    private void selectMonth(int month) {
        if (this.currentMonth >= 0) {
            this.formatter.removeStyleName(this.currentMonth % 6, this.currentMonth / 6, "mm-date-mp-sel");
        }
        this.currentMonth = month;
        this.formatter.addStyleName(this.currentMonth % 6, this.currentMonth / 6, "mm-date-mp-sel");
    }

    private void addYears(int years) {
        this.firstYear += years;
        for (int i = 0; i < 10; i++) {
            this.dateLinkYears[i].setText(String.valueOf(this.firstYear + i));
        }
    }

    private void selectYear(int yearId) {
        if (this.currentYearId >= 0) {
            this.formatter.removeStyleName(this.currentYearId % 5 + 1, this.currentYearId / 5 + 2, "mm-date-mp-sel");
        }
        this.currentYearId = yearId;
        this.formatter.addStyleName(this.currentYearId % 5 + 1, this.currentYearId / 5 + 2, "mm-date-mp-sel");
    }


    public void show(MmJsDate date) {
        setDate(date);
        final Element element = this.panel.getElement();
        final String height = element.getParentElement().getPropertyInt("offsetHeight") + "px";
        element.getStyle().setProperty("height", height);
        this.table.getElement().getStyle().setProperty("height", height);

        element.getStyle().setProperty("display", "block");
        element.getStyle().setProperty("visibility", "visible");
    }

    public void hide() {
        final Style style = this.panel.getElement().getStyle();
        style.setProperty("display", "none");
        style.setProperty("visibility", "hidden");
    }


    public void addListener(DateListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(DateListener listener) {
        this.listeners.remove(listener);
    }

    private void fireDate() {
        final MmJsDate date = new MmJsDate(this.firstYear + this.currentYearId, this.currentMonth, 1);
        date.setDateOrLastOfMonth(this.currentDay);
        for (final DateListener listener : listeners) {
            listener.setDate(date);
        }
    }
}
