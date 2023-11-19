/*
 * WMDataImpl.java
 *
 * Created on 02.11.11 16:17
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.Serializable;

import org.joda.time.LocalDate;

import de.marketmaker.istar.domain.data.RatingData;

/**
 * @author tkiesgen
 */
class RatingDataImpl implements RatingData, Serializable {
    protected static final long serialVersionUID = 1L;

    private final long instrumentid;

    private final String ratingFitchST;

    private final LocalDate ratingFitchSTDate;

    private final String ratingFitchSTAction;

    private final String ratingFitchLT;

    private final LocalDate ratingFitchLTDate;

    private final String ratingFitchLTAction;

    private final String ratingFitchIssuerST;

    private final LocalDate ratingFitchIssuerSTDate;

    private final String ratingFitchIssuerSTAction;

    private final String ratingFitchIssuerLT;

    private final LocalDate ratingFitchIssuerLTDate;

    private final String ratingFitchIssuerLTAction;

    private final String ratingMoodysST;

    private final LocalDate ratingMoodysSTDate;

    private final String ratingMoodysSTAction;

    private final String ratingMoodysLT;

    private final LocalDate ratingMoodysLTDate;

    private final String ratingMoodysLTAction;

    private final String ratingSnPST;

    private final LocalDate ratingSnPSTDate;

    private final String ratingSnPSTAction;

    private final String ratingSnPSTSource;

    private final String ratingSnPLT;

    private final LocalDate ratingSnPLTDate;

    private final String ratingSnPLTAction;

    private final String ratingSnPLTSource;

    private final String ratingSnPLTRegulatoryId;

    private final String ratingSnPLTQualifier;

    private final String ratingSnPLocalLT;

    private final LocalDate ratingSnPLocalLTDate;

    private final String ratingSnPLocalLTAction;

    private final String ratingSnPLocalLTSource;

    private final String ratingSnPLocalST;

    private final LocalDate ratingSnPLocalSTDate;

    private final String ratingSnPLocalSTAction;

    private final String ratingSnPLocalSTSource;

    static RatingData create(long instrumentid,
                             String ratingFitchST, LocalDate ratingFitchSTDate, String ratingFitchSTAction,
                             String ratingFitchLT, LocalDate ratingFitchLTDate, String ratingFitchLTAction,
                             String ratingFitchIssuerST, LocalDate ratingFitchIssuerSTDate,
                             String ratingFitchIssuerSTAction,
                             String ratingFitchIssuerLT, LocalDate ratingFitchIssuerLTDate,
                             String ratingFitchIssuerLTAction,
                             String ratingMoodysST, LocalDate ratingMoodysSTDate, String ratingMoodysSTAction,
                             String ratingMoodysLT, LocalDate ratingMoodysLTDate, String ratingMoodysLTAction,
                             String ratingSnPST, LocalDate ratingSnPSTDate, String ratingSnPSTAction, String ratingSnPSTSource,
                             String ratingSnPLT, LocalDate ratingSnPLTDate, String ratingSnPLTAction, String ratingSnPLTSource,
                             String ratingSnPLTRegulatoryId, String ratingSnPLTQualifier,
                             String ratingSnPLocalLT, LocalDate ratingSnPLocalLTDate, String ratingSnPLocalLTAction, String ratingSnPLocalLTSource,
                             String ratingSnPLocalST, LocalDate ratingSnPLocalSTDate, String ratingSnPLocalSTAction, String ratingSnPLocalSTSource) {
        return new RatingDataImpl(instrumentid,
                ratingFitchST, ratingFitchSTDate, ratingFitchSTAction,
                ratingFitchLT, ratingFitchLTDate, ratingFitchLTAction,
                ratingFitchIssuerST, ratingFitchIssuerSTDate, ratingFitchIssuerSTAction,
                ratingFitchIssuerLT, ratingFitchIssuerLTDate, ratingFitchIssuerLTAction,
                ratingMoodysST, ratingMoodysSTDate, ratingMoodysSTAction,
                ratingMoodysLT, ratingMoodysLTDate, ratingMoodysLTAction,
                ratingSnPST, ratingSnPSTDate, ratingSnPSTAction, ratingSnPSTSource,
                ratingSnPLT, ratingSnPLTDate, ratingSnPLTAction, ratingSnPLTSource,
                ratingSnPLTRegulatoryId, ratingSnPLTQualifier,
                ratingSnPLocalLT, ratingSnPLocalLTDate, ratingSnPLocalLTAction, ratingSnPLocalLTSource,
                ratingSnPLocalST, ratingSnPLocalSTDate, ratingSnPLocalSTAction, ratingSnPLocalSTSource);
    }

    RatingDataImpl(long instrumentid,
                   String ratingFitchST, LocalDate ratingFitchSTDate, String ratingFitchSTAction,
                   String ratingFitchLT, LocalDate ratingFitchLTDate, String ratingFitchLTAction,
                   String ratingFitchIssuerST, LocalDate ratingFitchIssuerSTDate,
                   String ratingFitchIssuerSTAction,
                   String ratingFitchIssuerLT, LocalDate ratingFitchIssuerLTDate,
                   String ratingFitchIssuerLTAction,
                   String ratingMoodysST, LocalDate ratingMoodysSTDate, String ratingMoodysSTAction,
                   String ratingMoodysLT, LocalDate ratingMoodysLTDate, String ratingMoodysLTAction,
                   String ratingSnPST, LocalDate ratingSnPSTDate, String ratingSnPSTAction, String ratingSnPSTSource,
                   String ratingSnPLT, LocalDate ratingSnPLTDate, String ratingSnPLTAction, String ratingSnPLTSource,
                   String ratingSnPLTRegulatoryId, String ratingSnPLTQualifier,
                   String ratingSnPLocalLT, LocalDate ratingSnPLocalLTDate, String ratingSnPLocalLTAction, String ratingSnPLocalLTSource,
                   String ratingSnPLocalST, LocalDate ratingSnPLocalSTDate, String ratingSnPLocalSTAction, String ratingSnPLocalSTSource) {
        this.instrumentid = instrumentid;
        this.ratingFitchST = ratingFitchST;
        this.ratingFitchSTDate = ratingFitchSTDate;
        this.ratingFitchSTAction = ratingFitchSTAction;
        this.ratingFitchLT = ratingFitchLT;
        this.ratingFitchLTDate = ratingFitchLTDate;
        this.ratingFitchLTAction = ratingFitchLTAction;
        this.ratingFitchIssuerST = ratingFitchIssuerST;
        this.ratingFitchIssuerSTDate = ratingFitchIssuerSTDate;
        this.ratingFitchIssuerSTAction = ratingFitchIssuerSTAction;
        this.ratingFitchIssuerLT = ratingFitchIssuerLT;
        this.ratingFitchIssuerLTDate = ratingFitchIssuerLTDate;
        this.ratingFitchIssuerLTAction = ratingFitchIssuerLTAction;
        this.ratingMoodysST = ratingMoodysST;
        this.ratingMoodysSTDate = ratingMoodysSTDate;
        this.ratingMoodysSTAction = ratingMoodysSTAction;
        this.ratingMoodysLT = ratingMoodysLT;
        this.ratingMoodysLTDate = ratingMoodysLTDate;
        this.ratingMoodysLTAction = ratingMoodysLTAction;
        this.ratingSnPST = ratingSnPST;
        this.ratingSnPSTDate = ratingSnPSTDate;
        this.ratingSnPSTAction = ratingSnPSTAction;
        this.ratingSnPSTSource = ratingSnPSTSource;
        this.ratingSnPLT = ratingSnPLT;
        this.ratingSnPLTDate = ratingSnPLTDate;
        this.ratingSnPLTAction = ratingSnPLTAction;
        this.ratingSnPLTSource = ratingSnPLTSource;
        this.ratingSnPLTRegulatoryId = ratingSnPLTRegulatoryId;
        this.ratingSnPLTQualifier = ratingSnPLTQualifier;
        this.ratingSnPLocalLT = ratingSnPLocalLT;
        this.ratingSnPLocalLTDate = ratingSnPLocalLTDate;
        this.ratingSnPLocalLTAction = ratingSnPLocalLTAction;
        this.ratingSnPLocalLTSource = ratingSnPLocalLTSource;
        this.ratingSnPLocalST = ratingSnPLocalST;
        this.ratingSnPLocalSTDate = ratingSnPLocalSTDate;
        this.ratingSnPLocalSTAction = ratingSnPLocalSTAction;
        this.ratingSnPLocalSTSource = ratingSnPLocalSTSource;

    }

    @Override
    public long getInstrumentid() {
        return instrumentid;
    }

    @Override
    public String getRatingFitchST() {
        return ratingFitchST;
    }

    @Override
    public LocalDate getRatingFitchSTDate() {
        return ratingFitchSTDate;
    }

    @Override
    public String getRatingFitchSTAction() {
        return ratingFitchSTAction;
    }

    @Override
    public String getRatingFitchLT() {
        return ratingFitchLT;
    }

    @Override
    public LocalDate getRatingFitchLTDate() {
        return ratingFitchLTDate;
    }

    @Override
    public String getRatingFitchLTAction() {
        return ratingFitchLTAction;
    }

    @Override
    public String getRatingFitchIssuerST() {
        return ratingFitchIssuerST;
    }

    @Override
    public LocalDate getRatingFitchIssuerSTDate() {
        return ratingFitchIssuerSTDate;
    }

    @Override
    public String getRatingFitchIssuerSTAction() {
        return ratingFitchIssuerSTAction;
    }

    @Override
    public String getRatingFitchIssuerLT() {
        return ratingFitchIssuerLT;
    }

    @Override
    public LocalDate getRatingFitchIssuerLTDate() {
        return ratingFitchIssuerLTDate;
    }

    @Override
    public String getRatingFitchIssuerLTAction() {
        return ratingFitchIssuerLTAction;
    }

    @Override
    public String getRatingMoodysST() {
        return ratingMoodysST;
    }

    @Override
    public LocalDate getRatingMoodysSTDate() {
        return ratingMoodysSTDate;
    }

    @Override
    public String getRatingMoodysSTAction() {
        return ratingMoodysSTAction;
    }

    @Override
    public String getRatingMoodysLT() {
        return ratingMoodysLT;
    }

    @Override
    public LocalDate getRatingMoodysLTDate() {
        return ratingMoodysLTDate;
    }

    @Override
    public String getRatingMoodysLTAction() {
        return ratingMoodysLTAction;
    }

    @Override
    public String getRatingSnPST() {
        return this.ratingSnPST;
    }

    @Override
    public LocalDate getRatingSnPSTDate() {
        return this.ratingSnPSTDate;
    }

    @Override
    public String getRatingSnPSTAction() {
        return this.ratingSnPSTAction;
    }

    @Override
    public String getRatingSnPSTSource() {
        return ratingSnPSTSource;
    }

    @Override
    public String getRatingSnPLT() {
        return this.ratingSnPLT;
    }

    @Override
    public LocalDate getRatingSnPLTDate() {
        return this.ratingSnPLTDate;
    }

    @Override
    public String getRatingSnPLTAction() {
        return this.ratingSnPLTAction;
    }

    @Override
    public String getRatingSnPLTSource() {
        return ratingSnPLTSource;
    }

    @Override
    public String getRatingSnPLTRegulatoryId() {
        return this.ratingSnPLTRegulatoryId;
    }

    @Override
    public String getRatingSnPLTQualifier() {
        return this.ratingSnPLTQualifier;
    }

    @Override
    public String getRatingSnPLocalLT() {
        return ratingSnPLocalLT;
    }

    @Override
    public LocalDate getRatingSnPLocalLTDate() {
        return ratingSnPLocalLTDate;
    }

    @Override
    public String getRatingSnPLocalLTAction() {
        return ratingSnPLocalLTAction;
    }

    @Override
    public String getRatingSnPLocalLTSource() {
        return ratingSnPLocalLTSource;
    }

    @Override
    public String getRatingSnPLocalST() {
        return ratingSnPLocalST;
    }

    @Override
    public LocalDate getRatingSnPLocalSTDate() {
        return ratingSnPLocalSTDate;
    }

    @Override
    public String getRatingSnPLocalSTAction() {
        return ratingSnPLocalSTAction;
    }

    @Override
    public String getRatingSnPLocalSTSource() {
        return ratingSnPLocalSTSource;
    }

    @Override
    public String toString() {
        return "RatingDataImpl{" +
                "instrumentid=" + instrumentid +
                ", ratingFitchST='" + ratingFitchST + '\'' +
                ", ratingFitchSTDate=" + ratingFitchSTDate +
                ", ratingFitchSTAction='" + ratingFitchSTAction + '\'' +
                ", ratingFitchLT='" + ratingFitchLT + '\'' +
                ", ratingFitchLTDate=" + ratingFitchLTDate +
                ", ratingFitchLTAction='" + ratingFitchLTAction + '\'' +
                ", ratingFitchIssuerST='" + ratingFitchIssuerST + '\'' +
                ", ratingFitchIssuerSTDate=" + ratingFitchIssuerSTDate +
                ", ratingFitchIssuerSTAction='" + ratingFitchIssuerSTAction + '\'' +
                ", ratingFitchIssuerLT='" + ratingFitchIssuerLT + '\'' +
                ", ratingFitchIssuerLTDate=" + ratingFitchIssuerLTDate +
                ", ratingFitchIssuerLTAction='" + ratingFitchIssuerLTAction + '\'' +
                ", ratingMoodysST='" + ratingMoodysST + '\'' +
                ", ratingMoodysSTDate=" + ratingMoodysSTDate +
                ", ratingMoodysSTAction='" + ratingMoodysSTAction + '\'' +
                ", ratingMoodysLT='" + ratingMoodysLT + '\'' +
                ", ratingMoodysLTDate=" + ratingMoodysLTDate +
                ", ratingMoodysLTAction='" + ratingMoodysLTAction + '\'' +
                ", ratingSnPST='" + ratingSnPST + '\'' +
                ", ratingSnPSTDate=" + ratingSnPSTDate +
                ", ratingSnPSTAction='" + ratingSnPSTAction + '\'' +
                ", ratingSnPSTSource='" + ratingSnPSTSource + '\'' +
                ", ratingSnPLT='" + ratingSnPLT + '\'' +
                ", ratingSnPLTDate=" + ratingSnPLTDate +
                ", ratingSnPLTAction='" + ratingSnPLTAction + '\'' +
                ", ratingSnPLTSource='" + ratingSnPLTSource + '\'' +
                ", ratingSnPLTRegulatoryId='" + ratingSnPLTRegulatoryId + '\'' +
                ", ratingSnPLTQualifier='" + ratingSnPLTQualifier + '\'' +
                ", ratingSnPLocalLT='" + ratingSnPLocalLT + '\'' +
                ", ratingSnPLocalLTDate=" + ratingSnPLocalLTDate +
                ", ratingSnPLocalLTAction='" + ratingSnPLocalLTAction + '\'' +
                ", ratingSnPLocalLTSource='" + ratingSnPLocalLTSource + '\'' +
                ", ratingSnPLocalST='" + ratingSnPLocalST + '\'' +
                ", ratingSnPLocalSTDate=" + ratingSnPLocalSTDate +
                ", ratingSnPLocalSTAction='" + ratingSnPLocalSTAction + '\'' +
                ", ratingSnPLocalSTSource='" + ratingSnPLocalSTSource + '\'' +
                '}';
    }
}
