/*
 * IntervalUnit.java
 *
 * Created on 4/1/14 10:13 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

/**
 * This enum encapsules the four intervals MM-Talk supports for summing up
 * signed integers on date instances.
 *
 * @author Stefan Willenbrock
 */
public enum IntervalUnit {

    MONTHS("AddMonths[%s]"),
    DAYS("%s"),
    WORKING_DAYS("AddWorkingDays[%s]"),
    YEARS("AddYears[%s]");

    protected static final long serialVersionUID = 1L;

    private final String formatString;

    private IntervalUnit(String mmDateFunction) {
        this.formatString = mmDateFunction;
    }

    public String toMMFunction(String operand) {
        return String.format(this.formatString, operand);
    }
}
