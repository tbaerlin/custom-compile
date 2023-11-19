package de.marketmaker.itools.gwtutil.client.widgets.datepicker;

import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.PopupPanelFix;

/**
 * @author umaurer
 */
public class DatePickerPopup {
    private final Element element;
    private DatePicker datePicker = null;
    private PopupPanel popupPanel = null;


    public DatePickerPopup(Element element) {
        this(null, element);
    }

    public DatePickerPopup(Widget widget) {
        this(null, widget.getElement());
    }

    public DatePickerPopup(DatePicker datePicker, Element element) {
        this.datePicker = datePicker;
        this.element = element;
    }

    public DatePickerPopup(DatePicker datePicker, Widget widget) {
        this(datePicker, widget.getElement());
    }

    public void show() {
        final DatePicker datePicker = getDatePicker();
        if (this.popupPanel == null) {
            this.popupPanel = new PopupPanel(true, true);
            PopupPanelFix.addFrameDummy(this.popupPanel);
            this.popupPanel.setWidget(datePicker);
            //noinspection GWTStyleCheck
            this.popupPanel.setStyleName("mm-menu mm-menu-plain mm-date-menu");
            this.popupPanel.setAnimationEnabled(true);
            this.datePicker.addListener(new DateListener(){
                public void setDate(MmJsDate date) {
                    if (popupPanel.isVisible()) {
                        popupPanel.hide(false);
                    }
                }
            });
        }
        this.popupPanel.addCloseHandler(new CloseHandler<PopupPanel>(){
            public void onClose(CloseEvent closeEvent) {
                if (closeEvent.isAutoClosed()) {
                    datePicker.hideSubPanels();
                }
            }
        });
        this.popupPanel.setPopupPositionAndShow(new PopupPositionCallback(this.element, this.popupPanel));
    }

    public DatePicker getDatePicker() {
        if (this.datePicker == null) {
            this.datePicker = new DatePicker();
        }
        return this.datePicker;
    }

    public static void show(DatePicker datePicker, Element element) {
        new DatePickerPopup(datePicker, element).show();
    }
}
