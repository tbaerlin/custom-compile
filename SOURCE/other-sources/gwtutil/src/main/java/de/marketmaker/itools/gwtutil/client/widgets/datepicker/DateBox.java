package de.marketmaker.itools.gwtutil.client.widgets.datepicker;

import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.HasBlurHandlers;
import com.google.gwt.event.dom.client.HasFocusHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.i18n.GwtUtilI18n;
import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.util.date.DateListener;
import de.marketmaker.itools.gwtutil.client.util.date.GwtDateParser;
import de.marketmaker.itools.gwtutil.client.util.date.JsDate;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;

/**
 * @author umaurer
 *         <p/>
 *         A date input field with validation and a popup for picking a date.
 *         <p/>
 *         <h3>CSS Style Rules</h3>
 *         <ul>
 *         <li>.mm-dateBox { primary style }</li>
 *         <li>.mm-dateBox img { style of the button }</li>
 *         <li>.mm-dateBox img.hover { dependent style added when the button is hovered }</li>
 *         <li>.mm-dateBox input { style of the textbox }</li>
 *         <li>.mm-dateBox.focus { dependent style added when the textbox has focus }</li>
 *         <li>.mm-dateBox.focus img { dependent style of the button when the textbox has focus }</li>
 *         <li>.mm-dateBox.focus img.hover { dependent style of the button when the textbox has focus and button is hovered}</li>
 *         <li>.mm-dateBox.focus input { dependent style of the textbox when it has focus }</li>
 *         <li>.mm-dateBox.disabled { dependent style added when DateBox is disabled }</li>
 *         <li>.mm-dateBox.disabled img { dependent style of the button when DateBox is disabled }</li>
 *         <li>.mm-dateBox.disabled img.hover { dependent style of the button when DateBox is disabled and button is hovered }</li>
 *         <li>.mm-dateBox.disabled input { dependent style of the textbox when DateBox is disabled }</li>
 *         <li>.mm-dateBox .mm-spanLabel { style of the label }</li>
 *         </ul>
 *         <p/>
 *         <h3>Examples</h3>
 *         <h4>DateBox with Label</h4>
 *         <pre>{@code
 *                 <div class="mm-dateBox">
 *                      <span class="mm-spanLabel">Lorem Ipsum</span>
 *                      <input type="text" class="gwt-TextBox" maxlength="10" />
 *                      <img src="clear.cache.gif" class="gwt-Image" />
 *                 </div>}</pre>
 *         <h4>Hovered button</h4>
 *         <pre>{@code
 *                 <div class="mm-dateBox">
 *                      <input type="text" class="gwt-TextBox" maxlength="10">
 *                      <img src="clear.cache.gif" class="gwt-Image hover">
 *                 </div>}</pre>
 *         <h4>Focused textbox</h4>
 *         <pre>{@code
 *                 <div class="mm-dateBox focus">
 *                      <input type="text" class="gwt-TextBox" maxlength="10">
 *                      <img src="clear.cache.gif" class="gwt-Image">
 *                 </div>}</pre>
 *         <h4>Disabled DateBox</h4>
 *         <pre>{@code
 *                 <div class="mm-dateBox disabled">
 *                      <input type="text" class="gwt-TextBox" maxlength="10">
 *                      <img src="clear.cache.gif" class="gwt-Image">
 *                 </div>}</pre>
 */


@SuppressWarnings({"GWTStyleCheck", "UnusedDeclaration"})
public class DateBox extends Composite implements HasValueChangeHandlers<MmJsDate>, Focusable, HasFocusHandlers, HasBlurHandlers {
    private final TextBox textBox;
    private MmJsDate lastDate;
    private MmJsDate currentDate;
    private MmJsDate minDate;
    private MmJsDate maxDate;
    private DatePickerPopup datePickerPopup;
    private String errorMessage = null;
    private boolean hasFocus = false;
    private final boolean allowNull;
    private IntervalPicker.IntervalMode intervalMode;
    private MessagePopup messagePopup = new MessagePopup.DefaultMessagePopup();

    private JsDateFormatter.Format jsDateFormat = JsDateFormatter.Format.DMY;

    public interface Msg extends GwtDateParser.Msg {
        String beforeMin(MmJsDate minDate, JsDateFormatter.Format format);
        String afterMax(MmJsDate maxDate, JsDateFormatter.Format format);
    }
    public static final Msg MSG = new Msg() {
        @Override
        public String beforeMin(MmJsDate minDate, JsDateFormatter.Format format) {
            return GwtUtilI18n.I.beforeEarliestDate(JsDateFormatter.format(minDate, format));
        }

        @Override
        public String afterMax(MmJsDate maxDate, JsDateFormatter.Format format) {
            return GwtUtilI18n.I.afterLatestDate(JsDateFormatter.format(maxDate, format));
        }

        @Override
        public String invalidDateFormat(String invalidDate) {
            return GwtDateParser.MSG.invalidDateFormat(invalidDate);
        }
    };
    private Msg msg = MSG;

    public static class Factory {
        private String prefix = null;
        private MmJsDate date = null;
        private TextBox textBox = null;
        private Widget iconWidget = null;
        private boolean allowNull = false;
        private Msg msg = null;
        private JsDateFormatter.Format format = null;
        private MessagePopup messagePopup = null;

        public Factory withPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public Factory withDate(MmJsDate date) {
            this.date = date;
            return this;
        }

        public Factory withTextBox(TextBox textBox) {
            this.textBox = textBox;
            return this;
        }

        public Factory withIconWidet(Widget iconWidget) {
            this.iconWidget = iconWidget;
            return this;
        }

        public Factory withAllowNull() {
            this.allowNull = true;
            return this;
        }

        public Factory withMsg(Msg msg) {
            this.msg = msg;
            return this;
        }

        public Factory withFormat(JsDateFormatter.Format format) {
            this.format = format;
            return this;
        }

        public Factory withMessagePopup(MessagePopup messagePopup) {
            this.messagePopup = messagePopup;
            return this;
        }

        public DateBox build() {
            final DateBox dateBox = new DateBox(this.prefix, this.date, this.textBox == null ? new TextBox() : this.textBox, this.iconWidget, this.allowNull);
            if (this.msg != null) {
                dateBox.withMsg(this.msg);
            }
            if (this.format != null) {
                dateBox.withFormat(this.format);
            }
            if (this.messagePopup != null) {
                dateBox.withMessagePopup(this.messagePopup);
            }
            return dateBox;
        }
    }

    /**
     * Creates an DateBox with current Date.
     */
    public DateBox() {
        this(null, null, new TextBox(), null, false);
    }

    public DateBox(boolean allowNull) {
        this(null, null, new TextBox(), null, allowNull);
    }

    public DateBox(MmJsDate date) {
        this(null, date, new TextBox(), null, false);
    }

    public DateBox(MmJsDate date, final TextBox textBox) {
        this(null, date, textBox, null, false);
    }

    public DateBox(String prefix, MmJsDate date) {
        this(prefix, date, new TextBox(), null, false);
    }

    public DateBox(String prefix, MmJsDate date, final TextBox textBox) {
        this(prefix, date, textBox, null, false);
    }

    public DateBox(String prefix, MmJsDate date, final TextBox textBox, final Widget iconWidget, final boolean allowNull) {
        this.allowNull = allowNull;
        if (date == null && !allowNull) {
            date = new MmJsDate();
        }

        final FlowPanel panel = new FlowPanel();

        this.currentDate = date;
        this.lastDate = date;

        this.textBox = textBox;
        this.textBox.setText(format(this.currentDate));
        this.textBox.setMaxLength(10);
        this.textBox.getElement().setAttribute("placeholder", this.jsDateFormat.getPlaceholder());
        this.textBox.addChangeHandler(new com.google.gwt.event.dom.client.ChangeHandler() {
            public void onChange(ChangeEvent event) {
                currentDate = null;
                validate();
                if (currentDate != null || allowNull && isValid()) {
                    setDate(currentDate);
                }
            }
        });
        this.textBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (handleKeyDown(event.getNativeKeyCode(), event.isControlKeyDown())) {
                    event.preventDefault();
                }
            }
        });
        this.textBox.addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                currentDate = null;
                validate();
            }
        });
        this.textBox.addMouseWheelHandler(new MouseWheelHandler() {
            public void onMouseWheel(MouseWheelEvent event) {
                if (!WidgetUtil.hasFocus(textBox.getElement())) {
                    return;
                }
                addToDate(-event.getDeltaY() / 3, false);
                event.preventDefault();
                validate();
            }
        });
        this.textBox.addFocusHandler(new FocusHandler() {
            public void onFocus(FocusEvent event) {
                DateBox.this.onFocus();
                panel.addStyleName("focus");
            }
        });
        this.textBox.addBlurHandler(new BlurHandler() {
            public void onBlur(BlurEvent event) {
                DateBox.this.onLostFocus();
                panel.removeStyleName("focus");
            }
        });
        panel.setStyleName("mm-dateBox");
        if (prefix != null) {
            panel.add(new InlineLabel(prefix));
        }
        panel.add(this.textBox);

        final ClickHandler iconClickHandler = new ClickHandler() {
            public void onClick(ClickEvent event) {
                textBox.setFocus(true);
                showDatePicker();
            }
        };
        if (iconWidget == null) {
            final Image calendarButton = new Image("clear.cache.gif");
            calendarButton.addMouseOutHandler(new MouseOutHandler() {
                public void onMouseOut(MouseOutEvent event) {
                    calendarButton.removeStyleName("hover");
                }
            });
            calendarButton.addMouseOverHandler(new MouseOverHandler() {
                public void onMouseOver(MouseOverEvent event) {
                    calendarButton.addStyleName("hover");
                }
            });
            calendarButton.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(ClickEvent event) {
                    calendarButton.removeStyleName("hover");
                }
            });
            calendarButton.addClickHandler(iconClickHandler);
            panel.add(calendarButton);
        }
        else {
            iconWidget.addHandler(iconClickHandler, ClickEvent.getType());
            panel.add(iconWidget);
        }

        initWidget(panel);

    }

    public static Factory factory() {
        return new Factory();
    }

    public DateBox withMsg(Msg msg) {
        this.msg = msg;
        return this;
    }

    public DateBox withFormat(JsDateFormatter.Format format) {
        this.jsDateFormat = format;
        this.textBox.getElement().setAttribute("placeholder", this.jsDateFormat.getPlaceholder());
        return this;
    }

    private String format(JsDate date) {
        return JsDateFormatter.format(date, this.allowNull, this.jsDateFormat);
    }

    public MmJsDate getMinDate() {
        return minDate;
    }

    public void setMinDate(MmJsDate minDate) {
        this.minDate = minDate;
        isValid();
    }

    public MmJsDate getMaxDate() {
        return maxDate;
    }

    public void setMaxDate(MmJsDate maxDate) {
        this.maxDate = maxDate;
        isValid();
    }

    private boolean checkInMinMax(MmJsDate date) {
        if(date == null && this.allowNull) {
            return true;
        }

        if(date == null) return false;

        if (this.minDate != null && date.isBefore(this.minDate)) {
            throw new RuntimeException(this.msg.beforeMin(this.minDate, this.jsDateFormat));
        }
        if (this.maxDate != null && date.isAfter(this.maxDate)) {
            throw new RuntimeException(this.msg.afterMax(this.maxDate, this.jsDateFormat));
        }
        return true;
    }


    public void setDate(MmJsDate date) {
        setDate(date, true);
    }

    public void setDate(MmJsDate date, boolean fireEvent) {
        this.textBox.setText(format(date));
        this.currentDate = date;
        if (fireEvent) {
            fireValueChangeEvent();
        }
        else {
            this.lastDate = this.currentDate;
        }
    }

    public MmJsDate getDate() {
        if (this.currentDate == null) {
            try {
                parseDate();
            }
            catch (Exception e) {
                // null is returned
            }
        }
        return this.currentDate;
    }

    private MmJsDate parseDate() {
        this.currentDate = JsDateFormatter.parseDdmmyyyy(this.textBox.getText(), this.allowNull, this.msg);
        return this.currentDate;
    }


    public boolean isValid() {
        setError(null);
        try {
            return checkInMinMax(parseDate());
        }
        catch (Exception e) {
            setError(e.getMessage());
            return false;
        }
    }

    public void setValid(boolean valid) {
        if (valid) {
            this.textBox.removeStyleName("mm-form-invalid");
        }
        else {
            this.textBox.addStyleName("mm-form-invalid");
        }
    }

    public void validate() {
        setValid(isValid());
    }

    private boolean handleKeyDown(int keyCode, boolean ctrlKeyDown) {
        final int direction;
        if (keyCode == KeyCodes.KEY_UP) {
            direction = 1;
        }
        else if (keyCode == KeyCodes.KEY_DOWN) {
            direction = -1;
        }
        else {
            return false;
        }

        addToDate(direction, ctrlKeyDown);
        return true;
    }

    private void addToDate(int direction, boolean ctrlKeyDown) {
        final MmJsDate date = getDate();
        if (date == null) {
            if (this.allowNull) {
                setDateAndCursorPos(new MmJsDate(), 0);
            }
            return;
        }

        final String s = this.textBox.getText();
        if (s.matches("[0-9]{1,2}\\.[0-9]{1,2}\\.[0-9]{1,4}")) {
            addToDate(s, date, direction, ctrlKeyDown, '.', new AddScale[]{AddScale.DAY, AddScale.MONTH, AddScale.YEAR});
        }
        else if (s.matches("[0-9]{1,2}/[0-9]{1,2}/[0-9]{1,4}")) {
            addToDate(s, date, direction, ctrlKeyDown, '/', new AddScale[]{AddScale.MONTH, AddScale.DAY, AddScale.YEAR});
        }
        else if (s.matches("[0-9]{1,4}-[0-9]{1,2}-[0-9]{1,2}")) {
            addToDate(s, date, direction, ctrlKeyDown, '-', new AddScale[]{AddScale.YEAR, AddScale.MONTH, AddScale.DAY});
        }
    }

    enum AddScale {
        DAY(7), MONTH(3), YEAR(10);

        private int multiply;

        AddScale(int multiply) {
            this.multiply = multiply;
        }

        public int getMultiply() {
            return multiply;
        }
    }

    private void addToDate(String s, MmJsDate date, int direction, boolean ctrlKeyDown, char delimiter, AddScale[] asSequence) {
        final int pos1 = s.indexOf(delimiter);
        final int pos2 = s.lastIndexOf(delimiter);
        final int cursorPos = this.textBox.getCursorPos();
        final MmJsDate newDate = new MmJsDate(date);
        final int selectedBlock = cursorPos <= pos1 ? 0 : cursorPos <= pos2 ? 1 : 2;
        final AddScale addScale = asSequence[selectedBlock];
        final int multiply = ctrlKeyDown ? addScale.getMultiply() : 1;
        switch (asSequence[selectedBlock]) {
            case DAY:
                newDate.addDays(direction * multiply);
                break;
            case MONTH:
                newDate.addMonths(direction * multiply);
                break;
            case YEAR:
                newDate.addYears(direction * multiply);
                break;
        }
        setDateAndSelection(newDate, delimiter, selectedBlock);
    }



    private void setDateAndCursorPos(MmJsDate date, int cursorPos) {
        try {
            checkInMinMax(date);
            setDate(date);
            this.textBox.setCursorPos(cursorPos);
        }
        catch (Exception e) {
            // ignore
        }
    }

    private void setDateAndSelection(MmJsDate date, char delimiter, int selectedBlock) {
        try {
            checkInMinMax(date);
            setDate(date);
            final String s = this.textBox.getText();
            final int pos1 = s.indexOf(delimiter);
            if (selectedBlock == 0) {
                this.textBox.setSelectionRange(0, pos1);
            }
            else {
                final int pos2 = s.lastIndexOf(delimiter);
                if (selectedBlock == 1) {
                    this.textBox.setSelectionRange(pos1 + 1, pos2 - pos1 - 1);
                }
                else {
                    this.textBox.setSelectionRange(pos2 + 1, s.length() - pos2 -1);
                }
            }
        }
        catch (Exception e) {
            // ignore
        }
    }

    private void onFocus() {
        this.hasFocus = true;
        if (this.errorMessage != null) {
            showErrorMessage();
        }
    }

    private void onLostFocus() {
        this.hasFocus = false;
        if (this.errorMessage != null) {
            hideErrorMessage();
        }
    }

    public void setError(String errorMessage) {
        this.errorMessage = errorMessage;
        if (!this.hasFocus) {
            return;
        }
        if (errorMessage == null) {
            hideErrorMessage();
        }
        else {
            showErrorMessage();
        }
    }

    public void resetError() {
        setError(null);
    }

    public DateBox withMessagePopup(MessagePopup messagePopup) {
        this.messagePopup = messagePopup;
        return this;
    }

    private void showErrorMessage() {
        this.messagePopup.show(this.textBox, this.errorMessage);
    }

    private void hideErrorMessage() {
        this.messagePopup.hide();
    }

    private void showDatePicker() {
        if (!this.textBox.isEnabled()) {
            return;
        }
        final DatePicker datePicker;
        if (this.datePickerPopup == null) {
            datePicker = new DatePicker(this.intervalMode == null ? IntervalPicker.IntervalMode.PAST : this.intervalMode);
            datePicker.addListener(new DateListener() {
                public void setDate(MmJsDate date) {
                    DateBox.this.setDate(date);
                    validate();
                }
            });
            this.datePickerPopup = new DatePickerPopup(datePicker, this.textBox);
        }
        else {
            datePicker = this.datePickerPopup.getDatePicker();
        }
        final MmJsDate date = getDate();
        datePicker.setDate(date == null ? this.lastDate : date);
        datePicker.setMaxDate(this.maxDate);
        datePicker.setMinDate(this.minDate);
        this.datePickerPopup.show();
    }


    public void setIntervalMode(IntervalPicker.IntervalMode intervalMode) {
        this.intervalMode = intervalMode;
        this.datePickerPopup = null;
    }


    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<MmJsDate> ch) {
        return this.addHandler(ch, ValueChangeEvent.getType());
    }

    private void fireValueChangeEvent() {
        if (CompareUtil.equals(this.currentDate, this.lastDate)) {
            return;
        }
        this.lastDate = this.currentDate;
        ValueChangeEvent.fire(this, this.currentDate);
    }

    public void setEnabled(boolean enabled) {
        this.textBox.setEnabled(enabled);
        if (enabled) {
            this.removeStyleName("disabled");
        }
        else {
            this.addStyleName("disabled");
        }
    }

    public boolean isEnabled() {
        return this.textBox.isEnabled();
    }

    @Override
    public int getTabIndex() {
        return this.textBox.getTabIndex();
    }

    @Override
    public void setAccessKey(char key) {
        this.textBox.setAccessKey(key);
    }

    @Override
    public void setFocus(boolean focused) {
        this.textBox.setFocus(focused);
    }

    @Override
    public void setTabIndex(int tabIndex) {
        this.textBox.setTabIndex(tabIndex);
    }

    @Override
    public HandlerRegistration addFocusHandler(FocusHandler handler) {
        return this.textBox.addFocusHandler(handler);
    }

    @Override
    public HandlerRegistration addBlurHandler(BlurHandler handler) {
        return this.textBox.addBlurHandler(handler);
    }
}