package de.marketmaker.itools.gwtutil.client.widgets.input;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.BlurEvent;
import com.google.gwt.event.dom.client.BlurHandler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.FocusEvent;
import com.google.gwt.event.dom.client.FocusHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ValueBoxBase;
import de.marketmaker.itools.gwtutil.client.event.PasteEvent;
import de.marketmaker.itools.gwtutil.client.event.PasteHandler;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Author: umaurer
 * Created: 18.03.14
 */
public class ValidationFeature<T> {
    public interface Validator {
        boolean isValid(String value, List<String> messages);
    }

    public interface FormatDescriptionProvider {
        String getFormatDescription();
    }

    public static class ParserValidator<T> implements Validator {
        private final Parser<T> parser;
        private final String formatDescription;
        private final FormatDescriptionProvider formatDescriptionProvider;

        public ParserValidator(Parser<T> parser, String formatDescription) {
            this.parser = parser;
            this.formatDescription = formatDescription;
            this.formatDescriptionProvider = null;
        }

        public ParserValidator(Parser<T> parser, FormatDescriptionProvider formatDescriptionProvider) {
            this.parser = parser;
            this.formatDescription = null;
            this.formatDescriptionProvider = formatDescriptionProvider;
        }

        @Override
        public boolean isValid(String value, List<String> messages) {
            try {
                parser.parse(value);
                return true;
            }
            catch (ParseException e) {
                if (this.formatDescription != null) {
                    messages.add(this.formatDescription);
                }
                else if(formatDescriptionProvider != null){
                    final String fd = this.formatDescriptionProvider.getFormatDescription();
                    if(fd != null) {
                        messages.add(fd);
                    }
                }
                return false;
            }
        }
    }

    public static class Validators implements Validator {
        private final Validator[] validators;

        public Validators(Validator... validators) {
            this.validators = validators;
        }

        @Override
        public boolean isValid(String value, List<String> messages) {
            boolean result = true;
            for (Validator validator : this.validators) {
                if (!validator.isValid(value, messages)) {
                    result = false;
                }
            }
            return result;
        }
    }

    public static class ValidatorDelegate implements Validator {
        private Validator delegate;

        public ValidatorDelegate() {
        }

        public ValidatorDelegate(Validator delegate) {
            this.delegate = delegate;
        }

        public void setDelegate(Validator delegate) {
            this.delegate = delegate;
        }

        @Override
        public boolean isValid(String value, List<String> messages) {
            return this.delegate == null || this.delegate.isValid(value, messages);
        }
    }

    private final ValueBoxBase<T> valueBox;
    private final Validator validator;
    private boolean valid = true;
    private String lastValidatedValue;
    private MessagePopup messagePopup;
    private final List<String> messages = new ArrayList<>();

    public ValidationFeature(final ValueBoxBase<T> valueBox, Validator validator) {
        this(valueBox, validator, new MessagePopup.DefaultMessagePopup());
    }

    public ValidationFeature(final ValueBoxBase<T> valueBox, Validator validator, MessagePopup messagePopup) {
        this.valueBox = valueBox;
        this.validator = validator;
        valueBox.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                validateDeferred();
            }
        });
        valueBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(ChangeEvent event) {
                validateDeferred();
            }
        });
        valueBox.sinkEvents(Event.ONPASTE);
        valueBox.addDomHandler(new PasteHandler() {
            @Override
            public void onPaste(PasteEvent e) {
                validateDeferred();
            }
        }, PasteEvent.getType());
        valueBox.setStyleName("mm-validatingBox");
        valueBox.addFocusHandler(new FocusHandler() {
            @Override
            public void onFocus(FocusEvent event) {
                showErrorMessages();
            }
        });
        valueBox.addBlurHandler(new BlurHandler() {
            @Override
            public void onBlur(BlurEvent event) {
                hideErrorMessages();
            }
        });
        valueBox.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent e) {
                if (!e.isAttached()) {
                    hideErrorMessages();
                }
            }
        });
        this.messagePopup = messagePopup;
    }

    private void validateDeferred() {
        Scheduler.get().scheduleDeferred(new Scheduler.ScheduledCommand() {
            @Override
            public void execute() {
                validate();
            }
        });
    }

    public void validate() {
        final String value = valueBox.getText();
        if (this.lastValidatedValue != null && this.lastValidatedValue.equals(value)) {
            return;
        }
        this.messages.clear();
        final boolean valid = this.validator.isValid(value, this.messages);
        if (this.valid != valid) {
            this.valid = valid;
            if (valid) {
                this.valueBox.removeStyleName("mm-form-invalid");
            }
            else {
                this.valueBox.addStyleName("mm-form-invalid");
            }
            this.valueBox.getElement().setPropertyBoolean("validity", valid);
        }
        showErrorMessages();
        this.lastValidatedValue = value;
    }

    private void showErrorMessages() {
        if (this.messages.isEmpty()) {
            this.messagePopup.hide();
            return;
        }
        this.messagePopup.show(this.valueBox, this.messages.toArray(new String[this.messages.size()]));
    }

    private void hideErrorMessages() {
        this.messagePopup.hide();
    }
}
