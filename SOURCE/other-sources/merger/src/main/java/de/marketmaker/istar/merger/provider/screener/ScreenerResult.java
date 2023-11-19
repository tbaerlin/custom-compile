/*
 * ScreenerResult.java
 *
 * Created on 04.04.2007 13:40:59
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.screener;

import de.marketmaker.istar.instrument.data.screener.ScreenerAlternative;

import java.util.Collections;
import java.util.List;
import java.io.Serializable;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ScreenerResult implements Serializable {
    static final long serialVersionUID = 1L;

    private final long instrumentid;
    private final String language;
    private List<ScreenerField> baseFields = Collections.emptyList();
    private List<ScreenerField> analysisFields = Collections.emptyList();
    private List<ScreenerField> riskFields = Collections.emptyList();
    private List<ScreenerAlternative> groupAlternatives= Collections.emptyList();
    private List<ScreenerAlternative> countryAlternatives= Collections.emptyList();

    public ScreenerResult(long instrumentid, String language) {
        this.instrumentid = instrumentid;
        this.language = language;
    }

    public long getInstrumentid() {
        return instrumentid;
    }

    public String getLanguage() {
        return language;
    }

    public List<ScreenerField> getBaseFields() {
        return baseFields;
    }

    public void setBaseFields(List<ScreenerField> baseFields) {
        this.baseFields = baseFields;
    }

    public List<ScreenerField> getAnalysisFields() {
        return analysisFields;
    }

    public void setAnalysisFields(List<ScreenerField> analysisFields) {
        this.analysisFields = analysisFields;
    }

    public List<ScreenerField> getRiskFields() {
        return riskFields;
    }

    public void setRiskFields(List<ScreenerField> riskFields) {
        this.riskFields = riskFields;
    }

    public List<ScreenerAlternative> getGroupAlternatives() {
        return groupAlternatives;
    }

    public void setGroupAlternatives(List<ScreenerAlternative> groupAlternatives) {
        this.groupAlternatives = groupAlternatives;
    }

    public List<ScreenerAlternative> getCountryAlternatives() {
        return countryAlternatives;
    }

    public void setCountryAlternatives(List<ScreenerAlternative> countryAlternatives) {
        this.countryAlternatives = countryAlternatives;
    }
}
