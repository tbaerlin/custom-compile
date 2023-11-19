/*
 * PriceRenderer.java
 *
 * Created on 05.06.2008 17:04:40
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.util;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PercentRenderer implements Renderer<String> {
    private final int shiftRightDecimalPlaces;
    private final StringBasedNumberFormat numberFormat;

    private final String defaultValue;
    private final String suffix;

    public PercentRenderer() {
        this(StringBasedNumberFormat.ROUND_2, "--", true, 2); // $NON-NLS-0$
    }

    public PercentRenderer(String defaultValue) {
        this(StringBasedNumberFormat.ROUND_2, defaultValue, true, 2);
    }

    public PercentRenderer(String defaultValue, boolean withPercentSuffix) {
        this(StringBasedNumberFormat.ROUND_2, defaultValue, withPercentSuffix, 2);
    }

    public PercentRenderer(StringBasedNumberFormat numberFormat, String defaultValue, boolean withPercentSuffix) {
        this(numberFormat, defaultValue, withPercentSuffix, 2);
    }

    public PercentRenderer(StringBasedNumberFormat numberFormat, String defaultValue, boolean withPercentSuffix,
                           final int shiftRightDecimalPlaces) {
        this.numberFormat = numberFormat;
        this.defaultValue = defaultValue;
        this.suffix = withPercentSuffix ? "%" : null; // $NON-NLS-0$
        this.shiftRightDecimalPlaces = shiftRightDecimalPlaces;
    }

    public String render(String s) {
        if (s == null) {
            return this.defaultValue;
        }
        return this.numberFormat.format(s, shiftRightDecimalPlaces, this.suffix);
    }
}
