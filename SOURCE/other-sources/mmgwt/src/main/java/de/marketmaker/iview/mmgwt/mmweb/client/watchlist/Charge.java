package de.marketmaker.iview.mmgwt.mmweb.client.watchlist;

import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;

/**
 * Created on Oct 7, 2009 11:01:07 AM
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */
public class Charge {
    private static Charge instance;

    private double minCharge;

    private double percentageCharge;

    public static Charge getInstance() {
        if (instance == null) {
            instance = new Charge();
        }
        return instance;
    }

    private Charge() {
        updateFromConfig();
    }

    public void updateFromConfig() {
        this.minCharge = readConfig(AppConfig.PROP_KEY_MIN_CHARGE);
        this.percentageCharge = readConfig(AppConfig.PROP_KEY_PERCENTAGE_CHARGE);
    }

    private double readConfig(String parameter) {
        final AppConfig config = SessionData.INSTANCE.getUser().getAppConfig();
        return toDouble(config.getProperty(parameter));
    }

    private double toDouble(String s) {
        return (s == null || s.length() == 0) ? 0d : Double.valueOf(s);
    }

    public double computeCharge(String pricePerUnit, String amount, double exchangeRate) {
        final double price = Double.valueOf(pricePerUnit) * exchangeRate * Double.valueOf(amount);
        double charge = price / 100 * this.percentageCharge;
        return Math.max(charge, this.minCharge);
    }
}
