/*
 * SymbolValidator.java
 *
 * Created on 01.08.2006 10:05:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade;

import org.springframework.validation.Errors;

import de.marketmaker.istar.common.validator.AbstractValidator;
import de.marketmaker.istar.domain.util.IsinUtil;
import de.marketmaker.istar.merger.util.SymbolUtil;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SymbolValidator extends AbstractValidator<Symbol> {
    public void validateValue(Object value, Errors errors) {
        if (!isValidSymbol(value)) {
            errors.rejectValue(
                    getPropertyName(), "validator.easytradesymbol",
                    getPropertyName() + " invalid: " + value);
        }
    }

    private boolean isValidSymbol(Object value) {
        if (value == null) {
            return true;
        }
        if (!(value instanceof String)) {
            return false;
        }
        final String s = (String) value;
        return isValidReference(SymbolUtil.extractSymbol(s));
    }

    private boolean isValidReference(String s) {
        return s.endsWith(EasytradeInstrumentProvider.QID_SUFFIX)
                || s.endsWith(EasytradeInstrumentProvider.IID_SUFFIX)
                || IsinUtil.isIsin(s)
                || s.length() == 6;
    }
}
