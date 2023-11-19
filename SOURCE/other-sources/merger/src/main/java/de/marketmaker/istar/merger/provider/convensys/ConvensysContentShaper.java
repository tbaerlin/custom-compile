/*
* ConvensysContentShaper.java
*
* Created on 17.09.2008 09:10:43
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.istar.merger.provider.convensys;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.springframework.util.StringUtils;

/**
 * @author Michael Lösch
 * @author Markus Dick
 */
public class ConvensysContentShaper {
    private static final String EMPTY = "--";
    private static final String PARSE_ERROR = "-//-";
    private static final String FORMAT = "#,##0.00";
    private static final String FORMAT_0 = "#,##0";
    private static final String FORMAT_3 = "#,##0.000";
    private final NumberFormat dfParser;
    private final NumberFormat dfFormatter;
    private final NumberFormat df0Formatter;
    private final NumberFormat df3Formatter;

    public ConvensysContentShaper(Locale parserLanguageLocale, Locale formatterLanguageLocale) {
        if(parserLanguageLocale == null) {
            throw new IllegalArgumentException("Argument parserLanguageLocale must not be null!");
        }
        if(formatterLanguageLocale == null) {
            throw new IllegalArgumentException("Argument formatterLanguageLocale must not be null!");
        }

        dfParser = NumberFormat.getNumberInstance(parserLanguageLocale);
        dfFormatter = new DecimalFormat(FORMAT, DecimalFormatSymbols.getInstance(formatterLanguageLocale));
        df0Formatter = new DecimalFormat(FORMAT_0, DecimalFormatSymbols.getInstance(formatterLanguageLocale));
        df3Formatter = new DecimalFormat(FORMAT_3, DecimalFormatSymbols.getInstance(formatterLanguageLocale));
    }

    /**
     * Parses the given decimal and formats with the given formatter.
     * Dashes are formatted as {@see EMPTY}.
     * A parser error is indicated with {@see PARSE_ERROR}.
     */
    private String formatNumber(NumberFormat formatter, String value) {
        if (!StringUtils.hasText(value)) {
            return EMPTY;
        }
        try {
            return formatter.format(dfParser.parse(value));
        }
        catch(ParseException e) {
            //Convensys uses "–" this sometimes to indicate, that a value is not known
            //Attention:
            //The first is not a regular dash (char# 45), instead it is #8211
            //The second is a regular dash (do not know if convensys really uses it)
            if("–".equals(value.trim()) || "-".equals(value.trim())) {
                return EMPTY;
            }
            return PARSE_ERROR;
        }
    }

    /**
     * Parses the given decimal and formats it.
     * @see #FORMAT
     * @see #formatNumber(java.text.NumberFormat, String)
     */
    public String formatFloat(String value) throws Exception {
        return formatNumber(dfFormatter, value);
    }

    /**
     * Parses the float, formats the float and appends a percent sign,
     * if the given string ends with one.
     * @see #FORMAT
     * @see #formatNumber(java.text.NumberFormat, String)
     */
    public String formatP(String value) throws Exception {
        if(!StringUtils.hasText(value)) {
            return EMPTY;
        }

        String formatted = formatNumber(dfFormatter, value);

        if(value.trim().endsWith("%")) {
            formatted = formatted + "%";
        }

        return formatted;
    }

    /**
     * Parses the given decimal and formats it.
     * @see #FORMAT_0
     * @see #formatNumber(java.text.NumberFormat, String)
     */
    public String format0(String value) throws Exception {
        return formatNumber(df0Formatter, value);
    }

    /**
     * Parses the given decimal and formats it.
     * @see #FORMAT_3
     * @see #formatNumber(java.text.NumberFormat, String)
     */
    public String format3(String value) throws Exception {
        return formatNumber(df3Formatter, value);
    }

    /**
     * Parses the float, formats the float and appends a percent sign,
     * if the given string ends with one.
     * If parsing results in an error, the original value is returned.
     * @see #formatP(String)
     */
    public String formatPC(String value) throws Exception {
        String formatted = formatP(value);
        if(PARSE_ERROR.equals(formatted)) {
            return value;
        }
        return formatted;
    }

    /**
     * Formats the given string as {@see EMPTY} if the given String is null, empty or contains a dash.
     */
    public String formatE(String value) {
        if (!StringUtils.hasText(value) || "–".equals(value.trim()) || "-".equals(value.trim())) {
            return EMPTY;
        }
        return value;
    }
}