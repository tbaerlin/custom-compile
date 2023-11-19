/*
 * TimeBox.java
 *
 * Created on 21.05.2014 07:28
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.dom.client.MouseWheelEvent;
import com.google.gwt.event.dom.client.MouseWheelHandler;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;
import de.marketmaker.itools.gwtutil.client.i18n.LocalizedFormatter;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.date.JsDateFormatter;
import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;
import de.marketmaker.itools.gwtutil.client.widgets.input.ValidationFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.io.IOException;
import java.text.ParseException;

/**
* @author Markus Dick
*/
public class TimeBox extends ValueBox<MmJsDate> implements HasMandatory {
    private static final char TIME_SEPARATOR = LocalizedFormatter.getInstance().getTimeSeparator();

    private enum SelectedBlock { HOURS(0, 2, 1, 1), MINUTES(3, 5, 1, 15), SECONDS(6, 8, 1, 15);
        private final int startIndex;
        private final int length;
        private final int smallIncFactor;
        private final int largeIncFactor;

        SelectedBlock(int startIndex, int endIndex, int smallIncFactor, int largeIncFactor) {
            this.startIndex = startIndex;
            this.length = endIndex - startIndex;
            this.smallIncFactor = smallIncFactor;
            this.largeIncFactor = largeIncFactor;
        }
    }

    private TimeParser parser;

    private final ValidationFeature<MmJsDate> validationFeature;

    private boolean controlKeyDown = false;

    @SuppressWarnings("unused")
    public TimeBox() {
        this(TimeFormat.HHMMSS);
    }

    public TimeBox(TimeFormat timeFormat) {
        this(timeFormat, new TimeRenderer(timeFormat), new TimeParser(timeFormat), null, new MessagePopup.DefaultMessagePopup());
    }

    @SuppressWarnings("unused")
    public TimeBox(TimeFormat timeFormat, MessagePopup messagePopup) {
        this(timeFormat, new TimeRenderer(timeFormat), new TimeParser(timeFormat), null, messagePopup);
    }

    public TimeBox(TimeFormat timeFormat, ValidationFeature.FormatDescriptionProvider formatDescriptionProvider, MessagePopup messagePopup) {
        this(timeFormat, new TimeRenderer(timeFormat), new TimeParser(timeFormat), formatDescriptionProvider, messagePopup);
    }

    private TimeBox(TimeFormat timeFormat, final Renderer<MmJsDate> renderer, TimeParser parser, ValidationFeature.FormatDescriptionProvider formatDescriptionProvider, MessagePopup messagePopup) {
        super(Document.get().createTextInputElement(), renderer, parser);

        this.parser = parser;

        final ValidationFeature.ParserValidator<MmJsDate> validator = formatDescriptionProvider == null
                ? new ValidationFeature.ParserValidator<>(parser, timeFormat.getPlaceholder())
                : new ValidationFeature.ParserValidator<>(parser, formatDescriptionProvider);
        this.validationFeature = new ValidationFeature<>(this, validator, messagePopup);

        getElement().setAttribute("placeholder", timeFormat.getPlaceholder()); // $NON-NLS$

        addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                TimeBox.this.controlKeyDown = event.isControlKeyDown();
                if (handleKeyDown(event.getNativeKeyCode(), event.isControlKeyDown())) {
                    event.preventDefault();
                }
            }
        });

        addKeyUpHandler(new KeyUpHandler() {
            public void onKeyUp(KeyUpEvent event) {
                TimeBox.this.controlKeyDown = event.isControlKeyDown();
                TimeBox.this.validationFeature.validate();
            }
        });

        addMouseWheelHandler(new MouseWheelHandler() {
            public void onMouseWheel(MouseWheelEvent event) {
                if (!WidgetUtil.hasFocus(getElement())) {
                    return;
                }
                addToTime(-event.getDeltaY() / 3, TimeBox.this.controlKeyDown);
                event.preventDefault();
            }
        });

        addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                //render again so that valid alternative inputs, e.g. 1223 (=12:23)
                //or '122305' are also formatted by the formatter.
                try {
                    final MmJsDate v = getValueOrThrow();
                    setValue(v, false);
                }
                catch(ParseException pe) {
                    //do nothing
                }
            }
        });
    }

    public ValidationFeature<MmJsDate> getValidationFeature() {
        return this.validationFeature;
    }

    public void setMandatory(boolean mandatory) {
        this.parser.setAllowEmpty(!mandatory);
    }

    @Override
    public boolean isMandatory() {
        return !this.parser.isAllowEmpty();
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

        addToTime(direction, ctrlKeyDown);
        return true;
    }

    private void addToTime(int direction, boolean ctrlKeyDown) {
        final MmJsDate time = getValueOrNow();
        final String s = this.getText();
        doAddToTime(s, time, direction, ctrlKeyDown);
    }

    private MmJsDate getValueOrNow() {
        final MmJsDate time = getValue();
        if (time == null) {
            final MmJsDate now = new MmJsDate();
            setValue(now);
            return now;
        }
        return time;
    }

    private void doAddToTime(String s, MmJsDate time, int direction, boolean ctrlKeyDown) {
        final MmJsDate tempTime = new MmJsDate(time);
        final int cursorPos = getCursorPos();
        final int firstIndex = s.indexOf(TIME_SEPARATOR);
        final int secondIndexTmp = s.indexOf(TIME_SEPARATOR, firstIndex + 1);
        final int secondIndex = secondIndexTmp < 0 ? s.length() : secondIndexTmp;

        final SelectedBlock where =
                cursorPos <= firstIndex ? SelectedBlock.HOURS :
                        cursorPos <= secondIndex ? SelectedBlock.MINUTES : SelectedBlock.SECONDS;

        switch (where) {
            case HOURS:
                tempTime.addHours(direction);
                break;
            case MINUTES:
                tempTime.addMinutes(direction * getIncrement(ctrlKeyDown, where));
                break;
            case SECONDS:
                tempTime.addSeconds(direction * getIncrement(ctrlKeyDown, where));
                break;
            default:
        }

        setDateAndSelection(tempTime, where);
    }

    private int getIncrement(boolean ctrlKeyDown, SelectedBlock selectedBlock) {
        int increment = selectedBlock.smallIncFactor;
        if(ctrlKeyDown) {
            increment = selectedBlock.largeIncFactor;
        }
        return increment;
    }

    private void setDateAndSelection(MmJsDate time, SelectedBlock selectedBlock) {
        try {
            setValue(time, true);
            setSelectionRange(selectedBlock);
        }
        catch (Exception e) {
            // ignore
        }
    }

    private void setSelectionRange(SelectedBlock selectedBlock) {
        setSelectionRange(selectedBlock.startIndex, selectedBlock.length);
    }

    public enum TimeFormat {
        HHMM("HH:MM", LocalizedFormatter.getInstance().getPlaceholderHm()), // $NON-NLS$
        HHMMSS("HH:MM:SS", LocalizedFormatter.getInstance().getPlaceholderHms());  // $NON-NLS$

        private final String label;
        private final String placeholder;

        TimeFormat(String label, String placeholder) {
            this.label = label;
            this.placeholder = placeholder;
        }

        public String getLabel() {
            return this.label;
        }

        public String getPlaceholder() {
            return placeholder;
        }
    }

    private static class TimeRenderer implements Renderer<MmJsDate> {
        private final TimeFormat format;

        public TimeRenderer(TimeFormat format) {
            this.format = format;
        }

        @Override
        public String render(MmJsDate dateTime) {
            if(dateTime == null) {
                return null;
            }
            switch(this.format) {
                case HHMM:
                    return JsDateFormatter.formatHhmm(dateTime);
                case HHMMSS:
                default:
                    return JsDateFormatter.formatHhmmss(dateTime);
            }
        }

        @Override
        public void render(MmJsDate dateTime, Appendable appendable) throws IOException {
            if(dateTime == null) {
                return;
            }
            appendable.append(render(dateTime));
        }
    }

    private static class TimeParser implements Parser<MmJsDate> {
        private final static char TIME_SEPARATOR = LocalizedFormatter.getInstance().getTimeSeparator();

        private final TimeFormat format;
        private boolean allowEmpty;

        public TimeParser(TimeFormat format) {
            this.format = format;
            this.allowEmpty = true;
        }

        @Override
        public MmJsDate parse(CharSequence text) throws ParseException {
            if(text == null) {
                return null;
            }

            final String time = text.toString().trim();

            if(!StringUtil.hasText(time)) {
                if(this.allowEmpty) {
                    return null;
                }
                else {
                    throw new ParseException("empty TimeBox", 0);  // TODO I18n? $NON-NLS$
                }
            }

            try {
                final int length = time.length();
                int posMinSep = time.indexOf(TIME_SEPARATOR);
                int posSecSepTmp = time.indexOf(TIME_SEPARATOR, posMinSep + 1);
                int posSecSep = posSecSepTmp > -1 ? posSecSepTmp : length;
                int beginIndexMinutes = posMinSep + 1;
                int beginIndexSeconds = posSecSep + 1;

                if(posMinSep == -1 && length >= 3) {
                    //allow and parse input without time separators
                    beginIndexMinutes = posMinSep = 2 ;
                    posSecSep = 4;
                    if(this.format != TimeFormat.HHMM) {
                        posSecSepTmp = beginIndexSeconds = length > 4 ? 4 : -1;
                    }
                }
                else {
                    if (posMinSep < 0) {
                        throw new ParseException("no minutes separator found", posMinSep);  // TODO I18n? $NON-NLS$
                    }
                    if (posSecSep < 0 && this.format == TimeFormat.HHMMSS) {
                        throw new ParseException("no seconds separator found", posSecSep);  // TODO I18n? $NON-NLS$
                    }
                    if (posSecSepTmp > -1 && this.format == TimeFormat.HHMM) {
                        throw new ParseException("seconds are not allowed", posSecSepTmp);  // TODO I18n? $NON-NLS$
                    }
                }

                final String hourStr = time.substring(0, posMinSep);
                final int hours = parseInt(hourStr, 0);

                final String minutesStr = time.substring(beginIndexMinutes, posSecSep);
                final int minutes = parseInt(minutesStr, beginIndexMinutes);

                final int seconds;
                if (posSecSepTmp > 0) {
                    final String secondsStr = time.substring(beginIndexSeconds, length);
                    seconds = parseInt(secondsStr, beginIndexSeconds);
                }
                else {
                    seconds = 0;
                }

                rangeCheck(hours, minutes, seconds);
                return getDateTime(hours, minutes, seconds);
            }
            catch(ParseException e) {
                Firebug.warn("TimeParser " + this.format.getLabel() + " pos. " + e.getErrorOffset(), e);
                throw e;
            }
            catch(Exception e) {
                Firebug.warn("TimeParser " + this.format.getLabel(), e);
                throw new ParseException(time, 0);
            }
        }

        private MmJsDate getDateTime(int hours, int minutes, int seconds) {
            final MmJsDate date = new MmJsDate();
            date.setHours(hours);
            date.setMinutes(minutes);
            date.setSeconds(seconds);
            date.setMilliseconds(0);
            return date;
        }

        private int parseInt(String text, int offset) throws ParseException {
            try {
                return Integer.parseInt(text);
            }
            catch(NumberFormatException e) {
                throw new ParseException("Not a number: " + text, offset);  // TODO I18n? $NON-NLS$
            }
        }

        protected void rangeCheck(int hours, int minutes, int seconds) throws ParseException {
            StringBuilder sb = null;
            if (0 > hours || hours > 23) {
                sb = createAppendMsg(null, "Hours must be set between 0 and 23"); // TODO I18n? $NON-NLS$
            }
            if (0 > minutes || minutes > 59) {
                sb = createAppendMsg(sb, "Minutes must be set between 0 and 59");  // TODO I18n? $NON-NLS$
            }
            if (0 > seconds || seconds > 59) {
                sb = createAppendMsg(sb, "Seconds must be set between 0 and 59"); // TODO I18n? $NON-NLS$
            }

            if(sb != null) {
                throw new ParseException(sb.toString(), 0);
            }
        }

        private StringBuilder createAppendMsg(StringBuilder sb, String message) {
            if(sb == null) {
                return new StringBuilder(message);
            }

            return sb.append(", ").append(message);  // $NON-NLS$;
        }

        public void setAllowEmpty(boolean allowEmpty) {
            this.allowEmpty = allowEmpty;
        }

        public boolean isAllowEmpty() {
            return this.allowEmpty;
        }
    }
}
