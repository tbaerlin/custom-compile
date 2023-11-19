/*
 * FormulaAdaptor.java
 *
 * Created on 12.05.2010 14:02:11
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * Formula adaptor is able to modify a given formula according to a given quote category.
 * @author zzhao
 * @see de.marketmaker.istar.ratios.backend.QuoteCategory
 * @since 1.1
 */
public final class FormulaAdaptor {
    private static final Logger logger = LoggerFactory.getLogger(FormulaAdaptor.class);

    private FormulaAdaptor() {
        throw new AssertionError("not for instantiation or inheritance");
    }

    /**
     * Adapts the given list of formulas according to the given quote category.
     * @param qc a quote category
     * @param formulas a list of formulas
     * @return the original(given) list of formulas if the given quote category is not adaptable or
     *         the given list of formulas is empty. Otherwise a new list of formulas with each formula
     *         adapted from the given list of formulas.
     */
    public static List<String> adapt(QuoteCategory qc, List<String> formulas) {
        if (!isAdaptable(qc) || CollectionUtils.isEmpty(formulas)) {
            return formulas;
        }

        final List<String> result = new ArrayList<>(formulas.size());
        for (final String formula : formulas) {
            result.add(adaptInternal(qc, formula));
        }

        return result;
    }

    /**
     * Adapts the given array of formulas according to the given quote category.
     * @param qc a quote category
     * @param formulas an array of formulas
     * @return the original(given) array of formulas if the given quote category is not adaptable or
     *         the given array of formulas is empty. Otherwise a new array of formulas with each formula
     *         adapted from the given array of formulas.
     */
    public static String[] adapt(QuoteCategory qc, String[] formulas) {
        if (!isAdaptable(qc) || ArrayUtils.isEmpty(formulas)) {
            return formulas;
        }

        final String[] ret = Arrays.copyOf(formulas, formulas.length);
        for (int i = 0; i < ret.length; i++) {
            if (StringUtils.hasText(ret[i])) {
                ret[i] = adaptInternal(qc, ret[i]);
            }
        }

        return ret;
    }

    /**
     * Adapts the given formula according to the given quote category.
     * @param qc a quote category
     * @param formula a formula
     * @return the original formula if the given quote category is not adaptable or the given formula
     *         is empty. Otherwise an adapted new formula.
     */
    public static String adapt(QuoteCategory qc, String formula) {
        if (!isAdaptable(qc) || !StringUtils.hasText(formula)) {
            return formula;
        }
        else {
            return adaptInternal(qc, formula);
        }
    }

    private static String adaptInternal(QuoteCategory qc, String formula) {
        if (!StringUtils.hasText(formula)) {
            return formula;
        }

        switch (qc) {
            case FUND_MARKET:
                return replaceHighLowWithCloseForFunds(formula);
            default:
                return formula;
        }
    }

    private static String replaceHighLowWithCloseForFunds(String formula) {
        if (formula.toLowerCase().contains("basispapier.")) {
            return formula;
        }

        return replaceHighLowWithClose(formula);
    }

    private static String replaceHighLowWithClose(String formula) {
        String result = formula.toLowerCase();
        result = StringUtils.replace(result, "high", "close");
        result = StringUtils.replace(result, "low", "close");

        if (!result.equalsIgnoreCase(formula)) {
            logger.debug("<replaceHighLowWithClose> '" + formula + "' -> '" + result + "'");
        }

        return result;
    }

    private static boolean isAdaptable(QuoteCategory qc) {
        return QuoteCategory.FUND_MARKET == qc;
    }
}
