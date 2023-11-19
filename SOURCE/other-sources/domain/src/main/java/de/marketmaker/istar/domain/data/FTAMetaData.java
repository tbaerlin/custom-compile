/*
 * FTAMetaData.java
 *
 * Created on 6/11/13 5:09 PM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

/**
 * @author Stefan Willenbrock
 */
public interface FTAMetaData {
    public String getTrendBreveMeta();

    public String getInAttoDaGiorniTBreveMeta();

    public String getTrendMedioMeta();

    public String getInAttoDaGiorniTMedioMeta();

    public String getTrendLungoTermineMeta();

    public String getInAttoDaGiorniTLungoMeta();

    public String getResistenzaMeta();

    public String getSupportoMeta();

    public String getResistenza1Meta();

    public String getSupporto1Meta();

    public String getMm20GiorniMeta();

    public String getMm40GiorniMeta();

    public String getMm100GiorniMeta();

    public String getRsi8SeduteMeta();

    public String getBeta3MesiMeta();

    public String getIndiceCorrelazione3MesiMeta();

    public String getAlfaMensileMeta();

    public String getVolatilitaStoricaBreveMeta();

    public String getDinaRiskMeta();

    public String getVolatilityRatioMeta();

    public String getDistPercleMinUltimiMeta();

    public String getDistPercleMasUltimiMeta();

    public String getPivotHMeta();

    public String getPivotLMeta();

    public String getPivot1HMeta();

    public String getPivot1LMeta();

    public String getSegnaleUltimaSedutaMeta();

    public String getUltimoSegnaleAttivoMeta();

    public String getChiusuraSegnaleMeta();

    public String getRisultPercPosInMeta();

    public String getStopLossMeta();

    public String getTargetMeta();

    public String getDuePercMeta();

    public String getMeno2PercMeta();

    public String getQuattroPercMeta();

    public String getMeno4PercMeta();

    public String getStrategiaPortafoglioMeta();

    public String getTopFlopMeta();

    public String getAndamentoVolumiMeta();

    public String getForzaRelativaMeta();
}
