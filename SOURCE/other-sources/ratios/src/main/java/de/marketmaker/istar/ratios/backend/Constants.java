/*
 * Constants.java
 *
 * Created on 19.10.2005 13:13:01
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.backend;

import java.util.GregorianCalendar;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class Constants {
    public final static long NOT_DEFINED_LONG = Long.MIN_VALUE;
    public final static int NOT_DEFINED_INT= Integer.MIN_VALUE;

    public final static String NOT_DEFINED_STRING = "";

    public static final long SCALE_FOR_DECIMAL = 100000L;

    public static final GregorianCalendar CALENDAR_MS_NULL = new GregorianCalendar(1899, 11, 30);
}
