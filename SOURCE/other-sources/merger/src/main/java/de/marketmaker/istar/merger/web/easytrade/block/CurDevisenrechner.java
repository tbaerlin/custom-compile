/*
 * CurDevisenrechner.java
 *
 * Created on 13.07.2006 07:06:22
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.web.easytrade.block;

import java.math.BigDecimal;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joda.time.LocalDate;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;

import de.marketmaker.istar.common.validator.NotNull;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProvider;
import de.marketmaker.istar.merger.provider.IsoCurrencyConversionProviderImpl;
import de.marketmaker.istar.common.dmxmldocu.MmInternal;

/**
 * Converts a given amount from one currency to another currency.
 * <p>
 * Also supports currency conversion at historical rates.
 * </p>
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CurDevisenrechner extends EasytradeCommandController {

    public static final List<Currency> CURRENCIES = Arrays.asList(
            new Currency("AUD", "Australischer Dollar [AUD]"),
            new Currency("BRL", "Brasilianischer Real [BRL]"),
            new Currency("GBP", "Britisches Pfund [GBP]"),
            new Currency("DKK", "Dänische Krone [DKK]"),
            new Currency("EUR", "Euro [EUR]"),
            new Currency("HKD", "Hong Kong-Dollar [HKD]"),
            new Currency("INR", "Indische Rupie [INR]"),
            new Currency("ILS", "Israelischer Schekel [ILS]"),
            new Currency("JPY", "Japanischer Yen [JPY]"),
            new Currency("CAD", "Kanadischer Dollar [CAD]"),
            new Currency("MXN", "Mexikanischer Peso [MXN]"),
            new Currency("NZD", "Neuseeländischer Dollar [NZD]"),
            new Currency("NOK", "Norwegische Krone  [NOK]"),
            new Currency("PLN", "Polnische Zloty [PLN]"),
            new Currency("SEK", "Schwedische Krone [SEK]"),
            new Currency("CHF", "Schweizer Franken [CHF]"),
            new Currency("SGD", "Singapur Dollar [SGD]"),
            new Currency("ZAR", "Südafrikanischer Rand [ZAR]"),
            new Currency("CZK", "Tschechische Krone [CZK]"),
            new Currency("TRY", "Türkische Lira [TRY]"),
            new Currency("HUF", "Ungarischer Forint [HUF]"),
            new Currency("USD", "US-Dollar [USD]"),
            new Currency("EGP", "Ägyptisches Pfund [EGP]")
    );

    static {
        final Collator GERMAN_COLLATOR = Collator.getInstance(Locale.GERMAN);
        CURRENCIES.sort(new Comparator<Currency>() {
            public int compare(Currency o1, Currency o2) {
                return GERMAN_COLLATOR.compare(o1.getName(), o2.getName());
            }
        });
    }

    public static class Currency {
        private final String isocode;

        private final String name;

        public Currency(String isocode, String name) {
            this.isocode = isocode;
            this.name = name;
        }

        public String getIsocode() {
            return isocode;
        }

        public String getName() {
            return name;
        }
    }

    public static class Command {
        private static final String DEFAULT_AMOUNT = "1";

        private String isocodestart;

        private String isocodeziel;

        private String betrag = DEFAULT_AMOUNT;

        private LocalDate date;

        /**
         * @return the amount to convert. Default is {@value #DEFAULT_AMOUNT}.
         */
        @NotNull
        // TODO: BigDecimal statt String o.ä.?
        public String getBetrag() {
            return betrag;
        }

        public void setBetrag(String betrag) {
            this.betrag = betrag;
        }

        @NotNull
        @MmInternal
        public String getIsocodestart() {
            return isocodestart;
        }

        public void setIsocodestart(String isocodestart) {
            this.isocodestart = isocodestart;
        }

        public void setIsocodeFrom(String code) {
            setIsocodestart(code);
        }

        /**
         * @return the source currency 3-character ISO symbol from which to convert.
         * @sample EUR
         */
        @NotNull
        public String getIsocodeFrom() {
            return isocodestart;
        }

        @NotNull
        @MmInternal
        public String getIsocodeziel() {
            return isocodeziel;
        }

        public void setIsocodeziel(String isocodeziel) {
            this.isocodeziel = isocodeziel;
        }

        /**
         * @return the target currency 3-character ISO symbol into which to convert.
         * @sample USD
         */

        @NotNull
        public String getIsocodeTo() {
            return isocodeziel;
        }

        public void setIsocodeTo(String code) {
            setIsocodeziel(code);
        }

        /**
         * @return a date. If set, the historical conversion rate for the given date is used.
         *         If not set, the up-to-date conversion rate is used.
         */
        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }
    }

    public CurDevisenrechner() {
        super(Command.class);
    }

    private IsoCurrencyConversionProvider isoCurrencyConversionProvider;

    public void setIsoCurrencyConversionProvider(
            IsoCurrencyConversionProvider isoCurrencyConversionProvider) {
        this.isoCurrencyConversionProvider = isoCurrencyConversionProvider;
    }

    protected ModelAndView doHandle(HttpServletRequest request, HttpServletResponse response,
            Object o, BindException errors) {
        final Command cmd = (Command) o;
        final IsoCurrencyConversionProviderImpl.ConversionResult cr = getConversion(cmd);

        final Map<String, Object> model = new HashMap<>();
        model.put("rate", cr.getRate().getValue());
        model.put("date", cr.getRate().getDate());
        model.put("sourceToTargetFactor", cr.getSourceToTargetFactor());
        model.put("result", cr.convert(parse(cmd.getBetrag())));
        model.put("currencies", CURRENCIES);
        model.put("quoteRef", cr.getQuoteRef());
        return new ModelAndView("curdevisenrechner", model);
    }

    private IsoCurrencyConversionProviderImpl.ConversionResult getConversion(Command cmd) {
        final LocalDate day = cmd.getDate() != null ? cmd.getDate() : new LocalDate();
        return this.isoCurrencyConversionProvider.getConversion(cmd.getIsocodestart(),
                cmd.getIsocodeziel(), day);
    }

    private BigDecimal parse(String s) {
        if (!StringUtils.hasText(s)) {
            return BigDecimal.ONE;
        }

        final String tmp = s.trim().replace(',', '.');
        final int dot = tmp.lastIndexOf('.');

        final StringBuilder sb = new StringBuilder(tmp.length());
        if (tmp.startsWith("-")) {
            sb.append('-');
        }
        for (int i = 0; i < tmp.length(); i++) {
            if (tmp.charAt(i) >= '0' && tmp.charAt(i) <= '9') {
                sb.append(tmp.charAt(i));
            }
            else if (i == dot) {
                sb.append('.');
            }
        }

        try {
            return new BigDecimal(sb.toString());
        } catch (NumberFormatException e) {
            this.logger.warn("<parse> failed for '" + s + "' => '" + sb.toString() + "'");
            return BigDecimal.ONE;
        }
    }
}
