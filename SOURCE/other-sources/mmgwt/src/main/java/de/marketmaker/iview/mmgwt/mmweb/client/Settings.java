/*
 * Settings.java
 *
 * Created on 10.06.2008 12:49:32
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.Constants;

/**
 * @author Ulrich Maurer
 */
public interface Settings extends Constants {
    public static final Settings INSTANCE = (Settings) GWT.create(Settings.class);

    String pdfBaseUri();
    String pngBaseUri();
    String csvBaseUri();
    String contactFormUri();

    String productSelectorGisInvestor();
    String productSelectorGisInformer();
    String productSelectorGisInvestorPush();

    String analyserUrlBnd();
    String analyserUrlBndGis();
    String analyserUrlWnt();
    String analyserUrlWntGis();
    String analyserUrlFlexChart();
    String analyserUrlInvestmentCalculator();
    String analyserUrlInvestmentCalculatorGis();

    String platowUrl();
    String aiGuideUrl();
    String kapitalmarktFavDefaultCustomersUrlCms();
    String kapitalmarktFavDefaultCustomersUrl();
    String aktienanleihenMatrixUrl();
    String akzentInvestBestsellerMatrixUrl();

    String technicalAnalysisDaily();
    String technicalAnalysisWeekly();

    String vwdPageHelp();

    String feriDescriptionUrl();
    String morningstarDescriptionUrl();
    String morningstarStarUrl();
    String edgDescriptionUrl();

    String helpUrl1();
    String helpUrl2();
    String helpUrl3();
    String helpUrl4();
    String helpUrl5();
    String helpUrl6();
    String helpUrl7();
    String helpUrl8();
    String helpUrl9();
    String helpUrlTotal();

    String helpUrlWgz1();
    String helpUrlWgz2();
    String helpUrlWgz3();
    String helpUrlWgz4();
    String helpUrlWgz5();
    String helpUrlWgz6();
    String helpUrlWgz7();
    String helpUrlWgz8();
    String helpUrlWgzTotal();

    String helpApobankUrl1();
    String helpApobankUrl2();
    String helpApobankUrl3();
    String helpApobankUrl4();
    String helpApobankUrl5();
    String helpApobankUrl6();
    String helpApobankUrlTotal();

    String helpKwtUrl1();
    String helpKwtUrl2();
    String helpKwtUrl3();
    String helpKwtUrl4();
    String helpKwtUrl5();
    String helpKwtUrl6();
    String helpKwtUrlTotal();

    String olbThemenPdfUrl();

    String mainWindowPushQuote();
    String mainWindowPushTitle();

    String analyserUrlInvestmentCalculatorIt();
    String analyserUrlInvestmentCalculatorEn();
    String analyserUrlInvestmentCalculatorNl();
    String analyserUrlInvestmentCalculatorFr();
}
