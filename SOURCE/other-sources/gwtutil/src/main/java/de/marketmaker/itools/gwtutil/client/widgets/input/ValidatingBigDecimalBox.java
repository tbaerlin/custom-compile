package de.marketmaker.itools.gwtutil.client.widgets.input;

import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.DomEvent;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueBox;
import de.marketmaker.itools.gwtutil.client.i18n.LocalizedFormatter;
import de.marketmaker.itools.gwtutil.client.util.StringUtility;
import de.marketmaker.itools.gwtutil.client.widgets.MessagePopup;

import java.math.BigDecimal;
import java.text.ParseException;

/**
 * Author: umaurer
 * Created: 18.03.14
 */
public class ValidatingBigDecimalBox extends ValueBox<BigDecimal> {
    final ValidationFeature<BigDecimal> validationFeature;

    public ValidatingBigDecimalBox(ValidationFeature.Validator validator, ValidationFeature.FormatDescriptionProvider formatDescriptionProvider, Parser<BigDecimal> bigDecimalParser, Renderer<BigDecimal> bigDecimalRenderer, MessagePopup messagePopup) {
        super(Document.get().createTextInputElement(), bigDecimalRenderer, bigDecimalParser);
        final ValidationFeature.Validator validators = new ValidationFeature.Validators(validator, new ValidationFeature.ParserValidator<>(bigDecimalParser, formatDescriptionProvider));
        this.validationFeature = new ValidationFeature<>(this, validators, messagePopup);
    }

    public ValidatingBigDecimalBox(ValidationFeature.Validator validator, ValidationFeature.FormatDescriptionProvider formatDescriptionProvider, Parser<BigDecimal> bigDecimalParser, Renderer<BigDecimal> bigDecimalRenderer) {
        super(Document.get().createTextInputElement(), bigDecimalRenderer, bigDecimalParser);
        final ValidationFeature.Validator validators = new ValidationFeature.Validators(validator, new ValidationFeature.ParserValidator<>(bigDecimalParser, formatDescriptionProvider));
        this.validationFeature = new ValidationFeature<>(this, validators);
    }

    /*public ValidatingBigDecimalBox(ValidationFeature.Validator validator, String formatDescription) {
        super(Document.get().createTextInputElement(), BigDecimalRenderer.instance(), BigDecimalParser.instance());
        final ValidationFeature.Validators validators = new ValidationFeature.Validators(validator, new ValidationFeature.ParserValidator<>(BigDecimalParser.instance(), formatDescription));
        this.validationFeature = new ValidationFeature<>(this, validators);
    }*/

    public ValidatingBigDecimalBox(String formatDescription) {
        super(Document.get().createTextInputElement(), BigDecimalRenderer.instance(), BigDecimalParser.instance());
        this.validationFeature = new ValidationFeature<>(this, new ValidationFeature.ParserValidator<>(BigDecimalParser.instance(), formatDescription));
    }

    public static class BigDecimalRenderer extends AbstractRenderer<BigDecimal> {
        private static BigDecimalRenderer INSTANCE;
        private static BigDecimalRenderer INSTANCE_NO_TRAILING_ZEROS;

        private final boolean removeTrailingZeros;

        public static Renderer<BigDecimal> instance() {
            if (INSTANCE == null) {
                INSTANCE = new BigDecimalRenderer(true);
            }
            return INSTANCE;
        }

        public static Renderer<BigDecimal> instanceTrailingZeros() {
            if (INSTANCE_NO_TRAILING_ZEROS == null) {
                INSTANCE_NO_TRAILING_ZEROS = new BigDecimalRenderer(false);
            }
            return INSTANCE_NO_TRAILING_ZEROS;
        }

        protected BigDecimalRenderer(boolean removeTrailingZeros) {
            this.removeTrailingZeros = removeTrailingZeros;
        }

        public String render(BigDecimal bd) {
            return LocalizedFormatter.getInstance().formatDecimal(bd, this.removeTrailingZeros);
        }
    }

    public static class BigDecimalParser implements Parser<BigDecimal> {
        private static BigDecimalParser INSTANCE;

        public static Parser<BigDecimal> instance() {
            if (INSTANCE == null) {
                INSTANCE = new BigDecimalParser();
            }
            return INSTANCE;
        }

        protected BigDecimalParser() {
        }

        public BigDecimal parse(CharSequence object) throws ParseException {
            if (object == null) {
                return null;
            }
            final String s = object.toString();
            if (StringUtility.isEmpty(s)) {
                return null;
            }

            try {
                return new BigDecimal(LocalizedFormatter.getInstance().parseDecimal(s.trim()));
            } catch (NumberFormatException e) {
                throw new ParseException(e.getMessage(), 0);
            }
        }
    }

    public void setText(String text, boolean fireEvent) {
        super.setText(text);
        if (fireEvent) {
            DomEvent.fireNativeEvent(Document.get().createChangeEvent(), this);
        }
    }
}
