/*
 * FTAMetaDataImpl.java
 *
 * Created on 5/29/13 12:49 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.FTAMetaData;

import java.io.Serializable;

/**
 * @author Stefan Willenbrock
 */
public class FTAMetaDataImpl implements FTAMetaData, Serializable {
    private final static long serialVersionUID = 1L;

    public static class Builder {
        private String trendBreveMeta;
        private String inAttoDaGiorniTBreveMeta;
        private String trendMedioMeta;
        private String inAttoDaGiorniTMedioMeta;
        private String trendLungoTermineMeta;
        private String inAttoDaGiorniTLungoMeta;
        private String resistenzaMeta;
        private String supportoMeta;
        private String resistenza1Meta;
        private String supporto1Meta;
        private String mm20GiorniMeta;
        private String mm40GiorniMeta;
        private String mm100GiorniMeta;
        private String rsi8SeduteMeta;
        private String beta3MesiMeta;
        private String indiceCorrelazione3MesiMeta;
        private String alfaMensileMeta;
        private String volatilitaStoricaBreveMeta;
        private String dinaRiskMeta;
        private String volatilityRatioMeta;
        private String distPercleMinUltimiMeta;
        private String distPercleMasUltimiMeta;
        private String pivotHMeta;
        private String pivotLMeta;
        private String pivot1HMeta;
        private String pivot1LMeta;
        private String segnaleUltimaSedutaMeta;
        private String ultimoSegnaleAttivoMeta;
        private String chiusuraSegnaleMeta;
        private String risultPercPosInMeta;
        private String stopLossMeta;
        private String targetMeta;
        private String duePercMeta;
        private String meno2PercMeta;
        private String quattroPercMeta;
        private String meno4PercMeta;
        private String strategiaPortafoglioMeta;
        private String topFlopMeta;
        private String andamentoVolumiMeta;
        private String forzaRelativaMeta;

        public FTAMetaDataImpl build() {
            return new FTAMetaDataImpl(this);
            // no invariant to check for
        }

        public void setTrendBreveMeta(String trendBreveMeta) {
            this.trendBreveMeta = trendBreveMeta;
        }

        public void setInAttoDaGiorniTBreveMeta(String inAttoDaGiorniTBreveMeta) {
            this.inAttoDaGiorniTBreveMeta = inAttoDaGiorniTBreveMeta;
        }

        public void setTrendMedioMeta(String trendMedioMeta) {
            this.trendMedioMeta = trendMedioMeta;
        }

        public void setInAttoDaGiorniTMedioMeta(String inAttoDaGiorniTMedioMeta) {
            this.inAttoDaGiorniTMedioMeta = inAttoDaGiorniTMedioMeta;
        }

        public void setTrendLungoTermineMeta(String trendLungoTermineMeta) {
            this.trendLungoTermineMeta = trendLungoTermineMeta;
        }

        public void setInAttoDaGiorniTLungoMeta(String inAttoDaGiorniTLungoMeta) {
            this.inAttoDaGiorniTLungoMeta = inAttoDaGiorniTLungoMeta;
        }

        public void setResistenzaMeta(String resistenzaMeta) {
            this.resistenzaMeta = resistenzaMeta;
        }

        public void setSupportoMeta(String supportoMeta) {
            this.supportoMeta = supportoMeta;
        }

        public void setResistenza1Meta(String resistenza1Meta) {
            this.resistenza1Meta = resistenza1Meta;
        }

        public void setSupporto1Meta(String supporto1Meta) {
            this.supporto1Meta = supporto1Meta;
        }

        public void setMm20GiorniMeta(String mm20GiorniMeta) {
            this.mm20GiorniMeta = mm20GiorniMeta;
        }

        public void setMm40GiorniMeta(String mm40GiorniMeta) {
            this.mm40GiorniMeta = mm40GiorniMeta;
        }

        public void setMm100GiorniMeta(String mm100GiorniMeta) {
            this.mm100GiorniMeta = mm100GiorniMeta;
        }

        public void setRsi8SeduteMeta(String rsi8SeduteMeta) {
            this.rsi8SeduteMeta = rsi8SeduteMeta;
        }

        public void setBeta3MesiMeta(String beta3MesiMeta) {
            this.beta3MesiMeta = beta3MesiMeta;
        }

        public void setIndiceCorrelazione3MesiMeta(String indiceCorrelazione3MesiMeta) {
            this.indiceCorrelazione3MesiMeta = indiceCorrelazione3MesiMeta;
        }

        public void setAlfaMensileMeta(String alfaMensileMeta) {
            this.alfaMensileMeta = alfaMensileMeta;
        }

        public void setVolatilitaStoricaBreveMeta(String volatilitaStoricaBreveMeta) {
            this.volatilitaStoricaBreveMeta = volatilitaStoricaBreveMeta;
        }

        public void setDinaRiskMeta(String dinaRiskMeta) {
            this.dinaRiskMeta = dinaRiskMeta;
        }

        public void setVolatilityRatioMeta(String volatilityRatioMeta) {
            this.volatilityRatioMeta = volatilityRatioMeta;
        }

        public void setDistPercleMinUltimiMeta(String distPercleMinUltimiMeta) {
            this.distPercleMinUltimiMeta = distPercleMinUltimiMeta;
        }

        public void setDistPercleMasUltimiMeta(String distPercleMasUltimiMeta) {
            this.distPercleMasUltimiMeta = distPercleMasUltimiMeta;
        }

        public void setPivotHMeta(String pivotHMeta) {
            this.pivotHMeta = pivotHMeta;
        }

        public void setPivotLMeta(String pivotLMeta) {
            this.pivotLMeta = pivotLMeta;
        }

        public void setPivot1HMeta(String pivot1HMeta) {
            this.pivot1HMeta = pivot1HMeta;
        }

        public void setPivot1LMeta(String pivot1LMeta) {
            this.pivot1LMeta = pivot1LMeta;
        }

        public void setSegnaleUltimaSedutaMeta(String segnaleUltimaSedutaMeta) {
            this.segnaleUltimaSedutaMeta = segnaleUltimaSedutaMeta;
        }

        public void setUltimoSegnaleAttivoMeta(String ultimoSegnaleAttivoMeta) {
            this.ultimoSegnaleAttivoMeta = ultimoSegnaleAttivoMeta;
        }

        public void setChiusuraSegnaleMeta(String chiusuraSegnaleMeta) {
            this.chiusuraSegnaleMeta = chiusuraSegnaleMeta;
        }

        public void setRisultPercPosInMeta(String risultPercPosInMeta) {
            this.risultPercPosInMeta = risultPercPosInMeta;
        }

        public void setStopLossMeta(String stopLossMeta) {
            this.stopLossMeta = stopLossMeta;
        }

        public void setTargetMeta(String targetMeta) {
            this.targetMeta = targetMeta;
        }

        public void setDuePercMeta(String duePercMeta) {
            this.duePercMeta = duePercMeta;
        }

        public void setMeno2PercMeta(String meno2PercMeta) {
            this.meno2PercMeta = meno2PercMeta;
        }

        public void setQuattroPercMeta(String quattroPercMeta) {
            this.quattroPercMeta = quattroPercMeta;
        }

        public void setMeno4PercMeta(String meno4PercMeta) {
            this.meno4PercMeta = meno4PercMeta;
        }

        public void setStrategiaPortafoglioMeta(String strategiaPortafoglioMeta) {
            this.strategiaPortafoglioMeta = strategiaPortafoglioMeta;
        }

        public void setTopFlopMeta(String topFlopMeta) {
            this.topFlopMeta = topFlopMeta;
        }

        public void setAndamentoVolumiMeta(String andamentoVolumiMeta) {
            this.andamentoVolumiMeta = andamentoVolumiMeta;
        }

        public void setForzaRelativaMeta(String forzaRelativaMeta) {
            this.forzaRelativaMeta = forzaRelativaMeta;
        }
    }

    private String trendBreveMeta;
    private String inAttoDaGiorniTBreveMeta;
    private String trendMedioMeta;
    private String inAttoDaGiorniTMedioMeta;
    private String trendLungoTermineMeta;
    private String inAttoDaGiorniTLungoMeta;
    private String resistenzaMeta;
    private String supportoMeta;
    private String resistenza1Meta;
    private String supporto1Meta;
    private String mm20GiorniMeta;
    private String mm40GiorniMeta;
    private String mm100GiorniMeta;
    private String rsi8SeduteMeta;
    private String beta3MesiMeta;
    private String indiceCorrelazione3MesiMeta;
    private String alfaMensileMeta;
    private String volatilitaStoricaBreveMeta;
    private String dinaRiskMeta;
    private String volatilityRatioMeta;
    private String distPercleMinUltimiMeta;
    private String distPercleMasUltimiMeta;
    private String pivotHMeta;
    private String pivotLMeta;
    private String pivot1HMeta;
    private String pivot1LMeta;
    private String segnaleUltimaSedutaMeta;
    private String ultimoSegnaleAttivoMeta;
    private String chiusuraSegnaleMeta;
    private String risultPercPosInMeta;
    private String stopLossMeta;
    private String targetMeta;
    private String duePercMeta;
    private String meno2PercMeta;
    private String quattroPercMeta;
    private String meno4PercMeta;
    private String strategiaPortafoglioMeta;
    private String topFlopMeta;
    private String andamentoVolumiMeta;
    private String forzaRelativaMeta;

    private FTAMetaDataImpl(Builder b) {
        this.trendBreveMeta = b.trendBreveMeta;
        this.inAttoDaGiorniTBreveMeta = b.inAttoDaGiorniTBreveMeta;
        this.trendMedioMeta = b.trendMedioMeta;
        this.inAttoDaGiorniTMedioMeta = b.inAttoDaGiorniTMedioMeta;
        this.trendLungoTermineMeta = b.trendLungoTermineMeta;
        this.inAttoDaGiorniTLungoMeta = b.inAttoDaGiorniTLungoMeta;
        this.resistenzaMeta = b.resistenzaMeta;
        this.supportoMeta = b.supportoMeta;
        this.resistenza1Meta = b.resistenza1Meta;
        this.supporto1Meta = b.supporto1Meta;
        this.mm20GiorniMeta = b.mm20GiorniMeta;
        this.mm40GiorniMeta = b.mm40GiorniMeta;
        this.mm100GiorniMeta = b.mm100GiorniMeta;
        this.rsi8SeduteMeta = b.rsi8SeduteMeta;
        this.beta3MesiMeta = b.beta3MesiMeta;
        this.indiceCorrelazione3MesiMeta = b.indiceCorrelazione3MesiMeta;
        this.alfaMensileMeta = b.alfaMensileMeta;
        this.volatilitaStoricaBreveMeta = b.volatilitaStoricaBreveMeta;
        this.dinaRiskMeta = b.dinaRiskMeta;
        this.volatilityRatioMeta = b.volatilityRatioMeta;
        this.distPercleMinUltimiMeta = b.distPercleMinUltimiMeta;
        this.distPercleMasUltimiMeta = b.distPercleMasUltimiMeta;
        this.pivotHMeta = b.pivotHMeta;
        this.pivotLMeta = b.pivotLMeta;
        this.pivot1HMeta = b.pivot1HMeta;
        this.pivot1LMeta = b.pivot1LMeta;
        this.segnaleUltimaSedutaMeta = b.segnaleUltimaSedutaMeta;
        this.ultimoSegnaleAttivoMeta = b.ultimoSegnaleAttivoMeta;
        this.chiusuraSegnaleMeta = b.chiusuraSegnaleMeta;
        this.risultPercPosInMeta = b.risultPercPosInMeta;
        this.stopLossMeta = b.stopLossMeta;
        this.targetMeta = b.targetMeta;
        this.duePercMeta = b.duePercMeta;
        this.meno2PercMeta = b.meno2PercMeta;
        this.quattroPercMeta = b.quattroPercMeta;
        this.meno4PercMeta = b.meno4PercMeta;
        this.strategiaPortafoglioMeta = b.strategiaPortafoglioMeta;
        this.topFlopMeta = b.topFlopMeta;
        this.andamentoVolumiMeta = b.andamentoVolumiMeta;
        this.forzaRelativaMeta = b.forzaRelativaMeta;
    }

    @Override
    public String getTrendBreveMeta() {
        return trendBreveMeta;
    }

    @Override
    public String getInAttoDaGiorniTBreveMeta() {
        return inAttoDaGiorniTBreveMeta;
    }

    @Override
    public String getTrendMedioMeta() {
        return trendMedioMeta;
    }

    @Override
    public String getInAttoDaGiorniTMedioMeta() {
        return inAttoDaGiorniTMedioMeta;
    }

    @Override
    public String getTrendLungoTermineMeta() {
        return trendLungoTermineMeta;
    }

    @Override
    public String getInAttoDaGiorniTLungoMeta() {
        return inAttoDaGiorniTLungoMeta;
    }

    @Override
    public String getResistenzaMeta() {
        return resistenzaMeta;
    }

    @Override
    public String getSupportoMeta() {
        return supportoMeta;
    }

    @Override
    public String getResistenza1Meta() {
        return resistenza1Meta;
    }

    @Override
    public String getSupporto1Meta() {
        return supporto1Meta;
    }

    @Override
    public String getMm20GiorniMeta() {
        return mm20GiorniMeta;
    }

    @Override
    public String getMm40GiorniMeta() {
        return mm40GiorniMeta;
    }

    @Override
    public String getMm100GiorniMeta() {
        return mm100GiorniMeta;
    }

    @Override
    public String getRsi8SeduteMeta() {
        return rsi8SeduteMeta;
    }

    @Override
    public String getBeta3MesiMeta() {
        return beta3MesiMeta;
    }

    @Override
    public String getIndiceCorrelazione3MesiMeta() {
        return indiceCorrelazione3MesiMeta;
    }

    @Override
    public String getAlfaMensileMeta() {
        return alfaMensileMeta;
    }

    @Override
    public String getVolatilitaStoricaBreveMeta() {
        return volatilitaStoricaBreveMeta;
    }

    @Override
    public String getDinaRiskMeta() {
        return dinaRiskMeta;
    }

    @Override
    public String getVolatilityRatioMeta() {
        return volatilityRatioMeta;
    }

    @Override
    public String getDistPercleMinUltimiMeta() {
        return distPercleMinUltimiMeta;
    }

    @Override
    public String getDistPercleMasUltimiMeta() {
        return distPercleMasUltimiMeta;
    }

    @Override
    public String getPivotHMeta() {
        return pivotHMeta;
    }

    @Override
    public String getPivotLMeta() {
        return pivotLMeta;
    }

    @Override
    public String getPivot1HMeta() {
        return pivot1HMeta;
    }

    @Override
    public String getPivot1LMeta() {
        return pivot1LMeta;
    }

    @Override
    public String getSegnaleUltimaSedutaMeta() {
        return segnaleUltimaSedutaMeta;
    }

    @Override
    public String getUltimoSegnaleAttivoMeta() {
        return ultimoSegnaleAttivoMeta;
    }

    @Override
    public String getChiusuraSegnaleMeta() {
        return chiusuraSegnaleMeta;
    }

    @Override
    public String getRisultPercPosInMeta() {
        return risultPercPosInMeta;
    }

    @Override
    public String getStopLossMeta() {
        return stopLossMeta;
    }

    @Override
    public String getTargetMeta() {
        return targetMeta;
    }

    @Override
    public String getDuePercMeta() {
        return duePercMeta;
    }

    @Override
    public String getMeno2PercMeta() {
        return meno2PercMeta;
    }

    @Override
    public String getQuattroPercMeta() {
        return quattroPercMeta;
    }

    @Override
    public String getMeno4PercMeta() {
        return meno4PercMeta;
    }

    @Override
    public String getStrategiaPortafoglioMeta() {
        return strategiaPortafoglioMeta;
    }

    @Override
    public String getTopFlopMeta() {
        return topFlopMeta;
    }

    @Override
    public String getAndamentoVolumiMeta() {
        return andamentoVolumiMeta;
    }

    @Override
    public String getForzaRelativaMeta() {
        return forzaRelativaMeta;
    }

    @Override
    public String toString() {
        return "FTAMetaDataImpl{" +
                "trendBreveMeta='" + trendBreveMeta + '\'' +
                ", inAttoDaGiorniTBreveMeta='" + inAttoDaGiorniTBreveMeta + '\'' +
                ", trendMedioMeta='" + trendMedioMeta + '\'' +
                ", inAttoDaGiorniTMedioMeta='" + inAttoDaGiorniTMedioMeta + '\'' +
                ", trendLungoTermineMeta='" + trendLungoTermineMeta + '\'' +
                ", inAttoDaGiorniTLungoMeta='" + inAttoDaGiorniTLungoMeta + '\'' +
                ", resistenzaMeta='" + resistenzaMeta + '\'' +
                ", supportoMeta='" + supportoMeta + '\'' +
                ", resistenza1Meta='" + resistenza1Meta + '\'' +
                ", supporto1Meta='" + supporto1Meta + '\'' +
                ", mm20GiorniMeta='" + mm20GiorniMeta + '\'' +
                ", mm40GiorniMeta='" + mm40GiorniMeta + '\'' +
                ", mm100GiorniMeta='" + mm100GiorniMeta + '\'' +
                ", rsi8SeduteMeta='" + rsi8SeduteMeta + '\'' +
                ", beta3MesiMeta='" + beta3MesiMeta + '\'' +
                ", indiceCorrelazione3MesiMeta='" + indiceCorrelazione3MesiMeta + '\'' +
                ", alfaMensileMeta='" + alfaMensileMeta + '\'' +
                ", volatilitaStoricaBreveMeta='" + volatilitaStoricaBreveMeta + '\'' +
                ", dinaRiskMeta='" + dinaRiskMeta + '\'' +
                ", volatilityRatioMeta='" + volatilityRatioMeta + '\'' +
                ", distPercleMinUltimiMeta='" + distPercleMinUltimiMeta + '\'' +
                ", distPercleMasUltimiMeta='" + distPercleMasUltimiMeta + '\'' +
                ", pivotHMeta='" + pivotHMeta + '\'' +
                ", pivotLMeta='" + pivotLMeta + '\'' +
                ", pivot1HMeta='" + pivot1HMeta + '\'' +
                ", pivot1LMeta='" + pivot1LMeta + '\'' +
                ", segnaleUltimaSedutaMeta='" + segnaleUltimaSedutaMeta + '\'' +
                ", ultimoSegnaleAttivoMeta='" + ultimoSegnaleAttivoMeta + '\'' +
                ", chiusuraSegnaleMeta='" + chiusuraSegnaleMeta + '\'' +
                ", risultPercPosInMeta='" + risultPercPosInMeta + '\'' +
                ", stopLossMeta='" + stopLossMeta + '\'' +
                ", targetMeta='" + targetMeta + '\'' +
                ", duePercMeta='" + duePercMeta + '\'' +
                ", meno2PercMeta='" + meno2PercMeta + '\'' +
                ", quattroPercMeta='" + quattroPercMeta + '\'' +
                ", meno4PercMeta='" + meno4PercMeta + '\'' +
                ", strategiaPortafoglioMeta='" + strategiaPortafoglioMeta + '\'' +
                ", topFlopMeta='" + topFlopMeta + '\'' +
                ", andamentoVolumiMeta='" + andamentoVolumiMeta + '\'' +
                ", forzaRelativaMeta='" + forzaRelativaMeta + '\'' +
                '}';
    }
}
