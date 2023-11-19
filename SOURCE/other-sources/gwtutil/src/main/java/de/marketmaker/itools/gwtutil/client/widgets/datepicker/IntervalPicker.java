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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.i18n.GwtUtilI18n;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;

import java.util.ArrayList;
import java.util.List;

/**
 * @author umaurer
 */
public class IntervalPicker extends Composite implements EventListener, ClickHandler {
    private static final String LID_ATTR = "mm:interval";

    public enum IntervalMode {
        PAST, FUTURE
    }

    private final IntervalMode intervalMode;

    final SimplePanel panel;
    final FlexTable table;
    private final FlexTable.FlexCellFormatter formatter;
    private List<DateListener> listeners = new ArrayList<DateListener>();

    public IntervalPicker(IntervalMode intervalMode, boolean alltimeEnabled) {
        this.intervalMode = intervalMode;
        this.panel = new SimplePanel();
        this.panel.setStyleName("mm-date-mp");
        this.panel.setWidth("175px");
        this.table = new FlexTable();
        this.table.setCellSpacing(0);
        this.table.setWidth("175px");
        this.formatter = this.table.getFlexCellFormatter();

        int row = 0;

//        this.table.setHTML(++row, 0, "&nbsp;");

        ++row;
        final DateLink dateLinkToday = new DateLink(GwtUtilI18n.I.today());
        dateLinkToday.getElement().setAttribute(LID_ATTR, "0d");
        dateLinkToday.addClickHandler(this);
        this.table.setWidget(row, 1, dateLinkToday);
        this.formatter.setColSpan(row, 1, 3);
        this.formatter.setStyleName(row, 1, "mm-date-mp-month");

        addIntervalRow(++row, GwtUtilI18n.I.days(), "d", "1", "2", "5");
        addIntervalRow(++row, GwtUtilI18n.I.weeks(), "w", "1", "2", "4");
        addIntervalRow(++row, GwtUtilI18n.I.months(), "m", "1", "3", "6");
        addIntervalRow(++row, GwtUtilI18n.I.years(), "y", "1", "2", "3");
        addIntervalRow(++row, "", "y", "5", "7", "10");

        if (alltimeEnabled) {
            ++row;
            final DateLink dateLinkAlltime = new DateLink(GwtUtilI18n.I.alltime());
            dateLinkAlltime.getElement().setAttribute(LID_ATTR, "a");
            dateLinkAlltime.addClickHandler(this);
            this.table.setWidget(row, 1, dateLinkAlltime);
            this.formatter.setColSpan(row, 1, 3);
            this.formatter.setStyleName(row, 1, "mm-date-mp-month");
        }
        
//        this.table.setHTML(++row, 0, "&nbsp;");

        // buttons
        this.table.setHTML(++row, 0, "<button class=\"mm-date-mp-cancel\" type=\"button\">" + GwtUtilI18n.I.cancel() + "</button>");
        this.table.getRowFormatter().setStyleName(row, "mm-date-mp-btns");
        this.formatter.setColSpan(row, 0, 4);
        launderButtons(this.formatter.getElement(row, 0));

        panel.setWidget(this.table);
        initWidget(this.panel);
    }

    private void addIntervalRow(int row, String title, String cInterval, String... iInterval) {
        this.table.setText(row, 0, title);
        int col = 0;
        for (String ii : iInterval) {
            col++;
            final DateLink dateLink = new DateLink(ii);
            dateLink.getElement().setAttribute(LID_ATTR, ii + cInterval);
            dateLink.addClickHandler(this);
            this.table.setWidget(row, col, dateLink);
            this.formatter.setStyleName(row, col, "mm-date-mp-month");
        }
    }

    public void onClick(ClickEvent event) {
        final String interval = ((Widget)event.getSource()).getElement().getAttribute(LID_ATTR);
        final MmJsDate date = getDate(interval);
        fireDate(date);
        hide();
    }

    private MmJsDate getDate(String interval) {
        final int factor = this.intervalMode == IntervalMode.PAST ? -1 : 1;
        if ("a".equals(interval)) {
            return null;
        }
        final MmJsDate date = new MmJsDate().getMidnight();
        if ("0d".equals(interval)) {
            return date;
        }

        final int iInterval = Integer.valueOf(interval.substring(0, interval.length() - 1));
        final char cInterval = interval.charAt(interval.length() - 1);
        switch (cInterval) {
            case 'd':
                date.addDays(iInterval * factor);
                break;
            case 'w':
                date.addDays(iInterval * factor * 7);
                break;
            case 'm':
                date.addMonths(iInterval * factor);
                break;
            case 'y':
                date.addMonths(iInterval * factor * 12);
                break;
            default:
                return null;
        }
        return date;
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
        hide();
        DOM.eventPreventDefault(event);
    }


    public void show() {
        final Element element = this.panel.getElement();
        final String height = element.getParentElement().getPropertyInt("offsetHeight") + "px";
        element.getStyle().setProperty("height", height);
        this.table.getElement().getStyle().setProperty("height", height);

        element.getStyle().setProperty("display", "block");
        element.getStyle().setProperty("visibility", "visible");
    }

    public void hide() {
        this.panel.getElement().getStyle().setProperty("display", "none");
        this.panel.getElement().getStyle().setProperty("visibility", "hidden");
    }


    public void addListener(DateListener listener) {
        this.listeners.add(listener);
    }

    public void removeListener(DateListener listener) {
        this.listeners.remove(listener);
    }

    private void fireDate(MmJsDate date) {
        for (final DateListener listener : listeners) {
            listener.setDate(date);
        }
    }
}