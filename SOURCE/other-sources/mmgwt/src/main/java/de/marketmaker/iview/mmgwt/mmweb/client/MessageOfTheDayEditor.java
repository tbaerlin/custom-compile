package de.marketmaker.iview.mmgwt.mmweb.client;

import java.util.Date;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasVerticalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RichTextArea;
import com.google.gwt.user.client.ui.SimplePanel;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.DateBox;
import de.marketmaker.itools.gwtutil.client.widgets.datepicker.IntervalPicker;
import de.marketmaker.iview.mmgwt.mmweb.client.data.MessageOfTheDay;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SimpleRichTextToolbar;

/**
 * @author Ulrich Maurer
 *         Date: 14.05.12
 */
public class MessageOfTheDayEditor extends AbstractPageController {
    private static boolean active = false;

    private final SimplePanel panel = new SimplePanel();

    public MessageOfTheDayEditor() {
        panel.setStyleName("message-of-the-day-editor");
    }

    public static void setActive(boolean _active) {
        active = _active;
    }

    public static boolean isActive() {
        return active;
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        loadMessageOfTheDay();
    }

    private void loadMessageOfTheDay() {
        showMessage(I18n.I.motdLoading());
        UserServiceAsync.App.getInstance().getMessageOfTheDay(new AsyncCallback<MessageOfTheDay>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("cannot load message of the day", caught);
                showError(caught.getMessage());
            }

            @Override
            public void onSuccess(MessageOfTheDay motd) {
                showMessageOfTheDay(motd);
            }
        });
    }

    private void saveMessageOfTheDay(MmJsDate firstDate, MmJsDate lastDate, String message) {
        final MessageOfTheDay motd = new MessageOfTheDay();
        motd.setFirstDate(firstDate == null ? new Date() : firstDate.getJavaDate());
        motd.setLastDate(lastDate == null ? null : lastDate.getJavaDate());
        motd.setMessage(message);
        UserServiceAsync.App.getInstance().setMessageOfTheDay(motd, new AsyncCallback<Void>() {
            @Override
            public void onFailure(Throwable caught) {
                Firebug.error("cannot save message of the day", caught);
                showError(caught.getMessage());
            }

            @Override
            public void onSuccess(Void xVoid) {
                AbstractMainController.INSTANCE.showMessage(I18n.I.motdSaved());
            }
        });
    }

    private void showMessage(final String message) {
        this.panel.setWidget(new Label(message));
        getContentContainer().setContent(this.panel);
    }

    private void showError(final String message) {
        final Label label = new Label(message);
        label.setStyleName("mm-error");
        this.panel.setWidget(label);
        getContentContainer().setContent(this.panel);
    }

    private void showMessageOfTheDay(final MessageOfTheDay motd) {
        if (motd == null) {
            showMessageOfTheDay(new MmJsDate(), null, "");
            return;
        }

        final MmJsDate firstDate = motd.getFirstDate() == null ? new MmJsDate() : new MmJsDate(motd.getFirstDate());
        final MmJsDate lastDate = motd.getLastDate() == null ? null : new MmJsDate(motd.getLastDate());
        final String message = motd.getMessage();

        showMessageOfTheDay(firstDate, lastDate, message);
    }

    private void showMessageOfTheDay(MmJsDate firstDate, MmJsDate lastDate, String message) {
        final DateBox dbFirstDate = new DateBox(firstDate);
        dbFirstDate.setIntervalMode(IntervalPicker.IntervalMode.FUTURE);
        final DateBox dbLastDate = new DateBox(true);
        dbLastDate.setIntervalMode(IntervalPicker.IntervalMode.FUTURE);
        dbLastDate.setDate(lastDate);
        final RichTextArea textArea = new RichTextArea();
        textArea.setStyleName("text-area");
        textArea.setHTML(message);

        final FlexTable table = new FlexTable();
        table.setCellSpacing(5);
        table.setText(0, 0, I18n.I.motdValidFirstDay());
        table.setWidget(0, 1, dbFirstDate);
        table.setText(1, 0, I18n.I.motdValidLastDay());
        table.setWidget(1, 1, dbLastDate);

        final FlexTable tableButtons = new FlexTable();
        tableButtons.setWidth("800px"); // $NON-NLS$
        tableButtons.setCellPadding(10);
        tableButtons.setWidget(0, 0, Button.text(I18n.I.motdAccept())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        saveMessageOfTheDay(dbFirstDate.getDate(), dbLastDate.getDate(), textArea.getHTML());
                    }
                })
                .build());
        tableButtons.setWidget(0, 1, Button.text(I18n.I.motdReset())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        loadMessageOfTheDay();
                    }
                })
                .build());
        final FlexTable.FlexCellFormatter btnsFormatter = tableButtons.getFlexCellFormatter();
        btnsFormatter.setAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT, HasVerticalAlignment.ALIGN_MIDDLE);
        btnsFormatter.setAlignment(0, 1, HasHorizontalAlignment.ALIGN_RIGHT, HasVerticalAlignment.ALIGN_MIDDLE);

        final FlowPanel flowPanel = new FlowPanel();
        flowPanel.getElement().getStyle().setMargin(10, Style.Unit.PX);
        flowPanel.add(table);
        flowPanel.add(new HTML("&nbsp;")); // $NON-NLS$
        flowPanel.add(new SimpleRichTextToolbar(textArea));
        flowPanel.add(textArea);
        flowPanel.add(new HTML("&nbsp;")); // $NON-NLS$
        flowPanel.add(tableButtons);

        this.panel.setWidget(flowPanel);
        getContentContainer().setContent(this.panel);
    }
}
