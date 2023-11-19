/*
 * ExtendBarData.java
 *
 * Created on 05.09.2008 12:42:22
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.data;

/**
 * @author Ulrich Maurer
 */
public class ExtendBarData {
    private final double value;
    private double maxValue = 0d;
    private double percentValue = 0d;

    public ExtendBarData(double value) {
        this.value = value;
    }

    public void setMaxValue(double maxValue) {
        this.maxValue = maxValue;
        this.percentValue = this.value * 100d / maxValue;
    }

    public double getMaxValue() {
        return this.maxValue;
    }

    public double getPercentValue() {
        return this.percentValue;
    }

    public double getValue() {
        return this.value;
    }
}
