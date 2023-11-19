/*
 * QuotationUnit.java
 *
 * Created on 27.01.12 12:30
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.export;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.regex.Pattern;

import de.marketmaker.istar.domain.instrument.MinimumQuotationSize;

import static de.marketmaker.istar.domain.instrument.MinimumQuotationSize.Unit;

/**
 * Based on definition by mail from AKlaiber@vwd.com to tkiesgen, 2012-01-23
 *
 * @author tkiesgen
 */
public class QuotationUnit {

    // this HashSet MUST contain the markets of all regexs in ITEMS
    static final HashSet<String> MARKETS_WITH_LOCAL_UNIT_SIZE
            = new HashSet<>(Arrays.asList("TFI", "BBA", "TPI", "FH", "MLIND", "TUBD", "BONDS"));

    private static final List<Item> ITEMS = Arrays.asList(
            new Item("[A-Z]{6}\\.TFI", Unit.UNIT),
            new Item("FN?[A-Z]{6}\\.TFI\\.\\d+[A-Z]", Unit.UNIT),
            new Item("O[A-Z]{6}\\.TFI\\.\\d+[A-Z]", Unit.PERCENT),
            new Item("B[A-Z]{6}.?\\.TFI\\.\\d+[A-Z]", Unit.POINT),
            new Item("CCS.[A-Z]{3}\\.TFI\\.\\d+[A-Z]", Unit.PERCENT),
            new Item("I.[A-Z]{3}\\.TFI\\.\\d+I?[A-Z]", Unit.PERCENT),
            new Item("(CC|CD|D)[A-Z]{3}\\.TFI\\.(\\d+[A-Z]|SN|TN|ON)", Unit.PERCENT),
            new Item("W.[A-Z]{3}\\d+[A-Z]\\.TFI\\.\\d+[A-Z]", Unit.PERCENT),
            new Item("P[A-Z]{3}\\d{2}R\\.TFI\\.\\d+[A-Z]", Unit.PERCENT),
            new Item("R[A-Z]{3}\\.TFI\\.\\d+[A-Z]X\\d+[A-Z]", Unit.PERCENT),
            new Item(".*\\.BBA\\..*", Unit.PERCENT),
            new Item(".*\\.TPI", Unit.PERCENT),
            new Item(".*\\.FH", Unit.POINT),
            new Item(".*\\.FH\\.\\d+[A-Z]", Unit.POINT),
            new Item(".*\\.MLIND", Unit.POINT),
            new Item(".*V\\.TUBD\\..*", Unit.PERCENT),
            new Item(".*\\.BONDS", Unit.PERCENT)
            );

    public static MinimumQuotationSize.Unit evaluate(String symbol) {
        for (final Item item : ITEMS) {
            if (item.matches(symbol)) {
                return item.unit;
            }
        }
        return MinimumQuotationSize.Unit.NOTHING;
    }

    private static class Item {
        private final Pattern pattern;

        private final Unit unit;

        private Item(String regex, Unit unit) {
            this.pattern = Pattern.compile(regex);
            this.unit = unit;
        }

        boolean matches(String symbol) {
            return this.pattern.matcher(symbol).matches();
        }
    }
}
