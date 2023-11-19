/*
 * RatingData.java
 *
 * Created on 31.10.2011 15:55:46
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;

/**
 * @author tkiesgen
 */
public interface RatingData {
    long getInstrumentid();

    String getRatingFitchST();

    LocalDate getRatingFitchSTDate();

    String getRatingFitchSTAction();

    String getRatingFitchLT();

    LocalDate getRatingFitchLTDate();

    String getRatingFitchLTAction();

    String getRatingFitchIssuerST();

    LocalDate getRatingFitchIssuerSTDate();

    String getRatingFitchIssuerSTAction();

    String getRatingFitchIssuerLT();

    LocalDate getRatingFitchIssuerLTDate();

    String getRatingFitchIssuerLTAction();

    String getRatingMoodysST();

    LocalDate getRatingMoodysSTDate();

    String getRatingMoodysSTAction();

    String getRatingMoodysLT();

    LocalDate getRatingMoodysLTDate();

    String getRatingMoodysLTAction();

    String getRatingSnPST();

    LocalDate getRatingSnPSTDate();

    String getRatingSnPSTAction();

    String getRatingSnPSTSource();

    String getRatingSnPLT();

    LocalDate getRatingSnPLTDate();

    String getRatingSnPLTAction();

    String getRatingSnPLTSource();

    String getRatingSnPLTRegulatoryId();

    String getRatingSnPLTQualifier();

    String getRatingSnPLocalLT();

    LocalDate getRatingSnPLocalLTDate();

    String getRatingSnPLocalLTAction();

    String getRatingSnPLocalLTSource();

    String getRatingSnPLocalST();

    LocalDate getRatingSnPLocalSTDate();

    String getRatingSnPLocalSTAction();

    String getRatingSnPLocalSTSource();
}
