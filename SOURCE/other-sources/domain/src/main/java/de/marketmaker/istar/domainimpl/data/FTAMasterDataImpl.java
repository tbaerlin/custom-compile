/*
 * FTAMasterDataImpl.java
 *
 * Created on 5/29/13 12:46 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.FTAMasterData;
import org.joda.time.LocalDate;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @author Stefan Willenbrock
 */
public class FTAMasterDataImpl implements Serializable, FTAMasterData {
    private final static long serialVersionUID = 1L;

    public static class Builder {
        private long instrumentid = -1;
        private String isin;
        private LocalDate lastUpdate;
        private String trendBreve;
        private Integer inAttoDaGiorniTBreve;
        private String trendMedio;
        private Integer inAttoDaGiorniTMedio;
        private String trendLungoTermine;
        private Integer inAttoDaGiorniTLungo;
        private BigDecimal resistenza;
        private BigDecimal supporto;
        private BigDecimal resistenza1;
        private BigDecimal supporto1;
        private BigDecimal mm20Giorni;
        private BigDecimal mm40Giorni;
        private BigDecimal mm100Giorni;
        private BigDecimal rsi8Sedute;
        private BigDecimal beta3Mesi;
        private BigDecimal indiceCorrelazione3Mesi;
        private BigDecimal alfaMensile;
        private BigDecimal volatilitaStoricaBreve;
        private BigDecimal dinaRisk;
        private BigDecimal volatilityRatio;
        private BigDecimal distPercleMinUltimi;
        private BigDecimal distPercleMasUltimi;
        private BigDecimal pivotH;
        private BigDecimal pivotL;
        private BigDecimal pivot1H;
        private BigDecimal pivot1L;
        private String segnaleUltimaSeduta;
        private String ultimoSegnaleAttivo;
        private BigDecimal chiusuraSegnale;
        private BigDecimal risultPercPosIn;
        private BigDecimal stopLoss;
        private BigDecimal target;
        private String duePerc;
        private String meno2Perc;
        private String quattroPerc;
        private String meno4Perc;
        private String strategiaPortafoglio;
        private String topFlop;
        private String andamentoVolumi;
        private String forzaRelativa;

        public FTAMasterDataImpl build() {
            FTAMasterDataImpl fmd = new FTAMasterDataImpl(this);
            if (!fmd.isValid()) {
                throw new IllegalStateException("instrumentid not set");
            }
            return fmd;
        }

        public void setInstrumentid(long instrumentid) {
            this.instrumentid = instrumentid;
        }

        public void setIsin(String isin) {
            this.isin = isin;
        }

        public void setLastUpdate(LocalDate lastUpdate) {
            this.lastUpdate = lastUpdate;
        }

        public void setTrendBreve(String trendBreve) {
            this.trendBreve = trendBreve;
        }

        public void setInAttoDaGiorniTBreve(Integer inAttoDaGiorniTBreve) {
            this.inAttoDaGiorniTBreve = inAttoDaGiorniTBreve;
        }

        public void setTrendMedio(String trendMedio) {
            this.trendMedio = trendMedio;
        }

        public void setInAttoDaGiorniTMedio(Integer inAttoDaGiorniTMedio) {
            this.inAttoDaGiorniTMedio = inAttoDaGiorniTMedio;
        }

        public void setTrendLungoTermine(String trendLungoTermine) {
            this.trendLungoTermine = trendLungoTermine;
        }

        public void setInAttoDaGiorniTLungo(Integer inAttoDaGiorniTLungo) {
            this.inAttoDaGiorniTLungo = inAttoDaGiorniTLungo;
        }

        public void setResistenza(BigDecimal resistenza) {
            this.resistenza = resistenza;
        }

        public void setSupporto(BigDecimal supporto) {
            this.supporto = supporto;
        }

        public void setResistenza1(BigDecimal resistenza1) {
            this.resistenza1 = resistenza1;
        }

        public void setSupporto1(BigDecimal supporto1) {
            this.supporto1 = supporto1;
        }

        public void setMm20Giorni(BigDecimal mm20Giorni) {
            this.mm20Giorni = mm20Giorni;
        }

        public void setMm40Giorni(BigDecimal mm40Giorni) {
            this.mm40Giorni = mm40Giorni;
        }

        public void setMm100Giorni(BigDecimal mm100Giorni) {
            this.mm100Giorni = mm100Giorni;
        }

        public void setRsi8Sedute(BigDecimal rsi8Sedute) {
            this.rsi8Sedute = rsi8Sedute;
        }

        public void setBeta3Mesi(BigDecimal beta3Mesi) {
            this.beta3Mesi = beta3Mesi;
        }

        public void setIndiceCorrelazione3Mesi(BigDecimal indiceCorrelazione3Mesi) {
            this.indiceCorrelazione3Mesi = indiceCorrelazione3Mesi;
        }

        public void setAlfaMensile(BigDecimal alfaMensile) {
            this.alfaMensile = alfaMensile;
        }

        public void setVolatilitaStoricaBreve(BigDecimal volatilitaStoricaBreve) {
            this.volatilitaStoricaBreve = volatilitaStoricaBreve;
        }

        public void setDinaRisk(BigDecimal dinaRisk) {
            this.dinaRisk = dinaRisk;
        }

        public void setVolatilityRatio(BigDecimal volatilityRatio) {
            this.volatilityRatio = volatilityRatio;
        }

        public void setDistPercleMinUltimi(BigDecimal distPercleMinUltimi) {
            this.distPercleMinUltimi = distPercleMinUltimi;
        }

        public void setDistPercleMasUltimi(BigDecimal distPercleMasUltimi) {
            this.distPercleMasUltimi = distPercleMasUltimi;
        }

        public void setPivotH(BigDecimal pivotH) {
            this.pivotH = pivotH;
        }

        public void setPivotL(BigDecimal pivotL) {
            this.pivotL = pivotL;
        }

        public void setPivot1H(BigDecimal pivot1H) {
            this.pivot1H = pivot1H;
        }

        public void setPivot1L(BigDecimal pivot1L) {
            this.pivot1L = pivot1L;
        }

        public void setSegnaleUltimaSeduta(String segnaleUltimaSeduta) {
            this.segnaleUltimaSeduta = segnaleUltimaSeduta;
        }

        public void setUltimoSegnaleAttivo(String ultimoSegnaleAttivo) {
            this.ultimoSegnaleAttivo = ultimoSegnaleAttivo;
        }

        public void setChiusuraSegnale(BigDecimal chiusuraSegnale) {
            this.chiusuraSegnale = chiusuraSegnale;
        }

        public void setRisultPercPosIn(BigDecimal risultPercPosIn) {
            this.risultPercPosIn = risultPercPosIn;
        }

        public void setStopLoss(BigDecimal stopLoss) {
            this.stopLoss = stopLoss;
        }

        public void setTarget(BigDecimal target) {
            this.target = target;
        }

        public void setDuePerc(String duePerc) {
            this.duePerc = duePerc;
        }

        public void setMeno2Perc(String meno2Perc) {
            this.meno2Perc = meno2Perc;
        }

        public void setQuattroPerc(String quattroPerc) {
            this.quattroPerc = quattroPerc;
        }

        public void setMeno4Perc(String meno4Perc) {
            this.meno4Perc = meno4Perc;
        }

        public void setStrategiaPortafoglio(String strategiaPortafoglio) {
            this.strategiaPortafoglio = strategiaPortafoglio;
        }

        public void setTopFlop(String topFlop) {
            this.topFlop = topFlop;
        }

        public void setAndamentoVolumi(String andamentoVolumi) {
            this.andamentoVolumi = andamentoVolumi;
        }

        public void setForzaRelativa(String forzaRelativa) {
            this.forzaRelativa = forzaRelativa;
        }
    }

    private long instrumentid;
    private String isin;
    private LocalDate lastUpdate;
    private String trendBreve;
    private Integer inAttoDaGiorniTBreve;
    private String trendMedio;
    private Integer inAttoDaGiorniTMedio;
    private String trendLungoTermine;
    private Integer inAttoDaGiorniTLungo;
    private BigDecimal resistenza;
    private BigDecimal supporto;
    private BigDecimal resistenza1;
    private BigDecimal supporto1;
    private BigDecimal mm20Giorni;
    private BigDecimal mm40Giorni;
    private BigDecimal mm100Giorni;
    private BigDecimal rsi8Sedute;
    private BigDecimal beta3Mesi;
    private BigDecimal indiceCorrelazione3Mesi;
    private BigDecimal alfaMensile;
    private BigDecimal volatilitaStoricaBreve;
    private BigDecimal dinaRisk;
    private BigDecimal volatilityRatio;
    private BigDecimal distPercleMinUltimi;
    private BigDecimal distPercleMasUltimi;
    private BigDecimal pivotH;
    private BigDecimal pivotL;
    private BigDecimal pivot1H;
    private BigDecimal pivot1L;
    private String segnaleUltimaSeduta;
    private String ultimoSegnaleAttivo;
    private BigDecimal chiusuraSegnale;
    private BigDecimal risultPercPosIn;
    private BigDecimal stopLoss;
    private BigDecimal target;
    private String duePerc;
    private String meno2Perc;
    private String quattroPerc;
    private String meno4Perc;
    private String strategiaPortafoglio;
    private String topFlop;
    private String andamentoVolumi;
    private String forzaRelativa;

    private FTAMasterDataImpl(Builder b) {
        this.instrumentid = b.instrumentid;
        this.isin = b.isin;
        this.lastUpdate = b.lastUpdate;
        this.trendBreve = b.trendBreve;
        this.inAttoDaGiorniTBreve = b.inAttoDaGiorniTBreve;
        this.trendMedio = b.trendMedio;
        this.inAttoDaGiorniTMedio = b.inAttoDaGiorniTMedio;
        this.trendLungoTermine = b.trendLungoTermine;
        this.inAttoDaGiorniTLungo = b.inAttoDaGiorniTLungo;
        this.resistenza = b.resistenza;
        this.supporto = b.supporto;
        this.resistenza1 = b.resistenza1;
        this.supporto1 = b.supporto1;
        this.mm20Giorni = b.mm20Giorni;
        this.mm40Giorni = b.mm40Giorni;
        this.mm100Giorni = b.mm100Giorni;
        this.rsi8Sedute = b.rsi8Sedute;
        this.beta3Mesi = b.beta3Mesi;
        this.indiceCorrelazione3Mesi = b.indiceCorrelazione3Mesi;
        this.alfaMensile = b.alfaMensile;
        this.volatilitaStoricaBreve = b.volatilitaStoricaBreve;
        this.dinaRisk = b.dinaRisk;
        this.volatilityRatio = b.volatilityRatio;
        this.distPercleMinUltimi = b.distPercleMinUltimi;
        this.distPercleMasUltimi = b.distPercleMasUltimi;
        this.pivotH = b.pivotH;
        this.pivotL = b.pivotL;
        this.pivot1H = b.pivot1H;
        this.pivot1L= b.pivot1L;
        this.segnaleUltimaSeduta = b.segnaleUltimaSeduta;
        this.ultimoSegnaleAttivo = b.ultimoSegnaleAttivo;
        this.chiusuraSegnale = b.chiusuraSegnale;
        this.risultPercPosIn = b.risultPercPosIn;
        this.stopLoss = b.stopLoss;
        this.target = b.target;
        this.duePerc = b.duePerc;
        this.meno2Perc = b.meno2Perc;
        this.quattroPerc = b.quattroPerc;
        this.meno4Perc = b.meno4Perc;
        this.strategiaPortafoglio = b.strategiaPortafoglio;
        this.topFlop = b.topFlop;
        this.andamentoVolumi = b.andamentoVolumi;
        this.forzaRelativa = b.forzaRelativa;
    }

    private boolean isValid() {
        return this.instrumentid != -1;
    }

    @Override
    public long getInstrumentid() {
        return instrumentid;
    }

    @Override
    public String getIsin() {
        return isin;
    }

    @Override
    public LocalDate getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String getTrendBreve() {
        return trendBreve;
    }

    @Override
    public Integer getInAttoDaGiorniTBreve() {
        return inAttoDaGiorniTBreve;
    }

    @Override
    public String getTrendMedio() {
        return trendMedio;
    }

    @Override
    public Integer getInAttoDaGiorniTMedio() {
        return inAttoDaGiorniTMedio;
    }

    @Override
    public String getTrendLungoTermine() {
        return trendLungoTermine;
    }

    @Override
    public Integer getInAttoDaGiorniTLungo() {
        return inAttoDaGiorniTLungo;
    }

    @Override
    public BigDecimal getResistenza() {
        return resistenza;
    }

    @Override
    public BigDecimal getSupporto() {
        return supporto;
    }

    @Override
    public BigDecimal getResistenza1() {
        return resistenza1;
    }

    @Override
    public BigDecimal getSupporto1() {
        return supporto1;
    }

    @Override
    public BigDecimal getMm20Giorni() {
        return mm20Giorni;
    }

    @Override
    public BigDecimal getMm40Giorni() {
        return mm40Giorni;
    }

    @Override
    public BigDecimal getMm100Giorni() {
        return mm100Giorni;
    }

    @Override
    public BigDecimal getRsi8Sedute() {
        return rsi8Sedute;
    }

    @Override
    public BigDecimal getBeta3Mesi() {
        return beta3Mesi;
    }

    @Override
    public BigDecimal getIndiceCorrelazione3Mesi() {
        return indiceCorrelazione3Mesi;
    }

    @Override
    public BigDecimal getAlfaMensile() {
        return alfaMensile;
    }

    @Override
    public BigDecimal getVolatilitaStoricaBreve() {
        return volatilitaStoricaBreve;
    }

    @Override
    public BigDecimal getDinaRisk() {
        return dinaRisk;
    }

    @Override
    public BigDecimal getVolatilityRatio() {
        return volatilityRatio;
    }

    @Override
    public BigDecimal getDistPercleMinUltimi() {
        return distPercleMinUltimi;
    }

    @Override
    public BigDecimal getDistPercleMasUltimi() {
        return distPercleMasUltimi;
    }

    @Override
    public BigDecimal getPivotH() {
        return pivotH;
    }

    @Override
    public BigDecimal getPivotL() {
        return pivotL;
    }

    @Override
    public BigDecimal getPivot1H() {
        return pivot1H;
    }

    @Override
    public BigDecimal getPivot1L() {
        return pivot1L;
    }

    @Override
    public String getSegnaleUltimaSeduta() {
        return segnaleUltimaSeduta;
    }

    @Override
    public String getUltimoSegnaleAttivo() {
        return ultimoSegnaleAttivo;
    }

    @Override
    public BigDecimal getChiusuraSegnale() {
        return chiusuraSegnale;
    }

    @Override
    public BigDecimal getRisultPercPosIn() {
        return risultPercPosIn;
    }

    @Override
    public BigDecimal getStopLoss() {
        return stopLoss;
    }

    @Override
    public BigDecimal getTarget() {
        return target;
    }

    @Override
    public String getDuePerc() {
        return duePerc;
    }

    @Override
    public String getMeno2Perc() {
        return meno2Perc;
    }

    @Override
    public String getQuattroPerc() {
        return quattroPerc;
    }

    @Override
    public String getMeno4Perc() {
        return meno4Perc;
    }

    @Override
    public String getStrategiaPortafoglio() {
        return strategiaPortafoglio;
    }

    @Override
    public String getTopFlop() {
        return topFlop;
    }

    @Override
    public String getAndamentoVolumi() {
        return andamentoVolumi;
    }

    @Override
    public String getForzaRelativa() {
        return forzaRelativa;
    }

    @Override
    public String toString() {
        return "FTAMasterDataImpl{" +
                "instrumentid=" + instrumentid +
                ", isin='" + isin + '\'' +
                ", lastUpdate=" + lastUpdate +
                ", trendBreve='" + trendBreve + '\'' +
                ", inAttoDaGiorniTBreve=" + inAttoDaGiorniTBreve +
                ", trendMedio='" + trendMedio + '\'' +
                ", inAttoDaGiorniTMedio=" + inAttoDaGiorniTMedio +
                ", trendLungoTermine='" + trendLungoTermine + '\'' +
                ", inAttoDaGiorniTLungo=" + inAttoDaGiorniTLungo +
                ", resistenza=" + resistenza +
                ", supporto=" + supporto +
                ", resistenza1=" + resistenza1 +
                ", supporto1=" + supporto1 +
                ", mm20Giorni=" + mm20Giorni +
                ", mm40Giorni=" + mm40Giorni +
                ", mm100Giorni=" + mm100Giorni +
                ", rsi8Sedute=" + rsi8Sedute +
                ", beta3Mesi=" + beta3Mesi +
                ", indiceCorrelazione3Mesi=" + indiceCorrelazione3Mesi +
                ", alfaMensile=" + alfaMensile +
                ", volatilitaStoricaBreve=" + volatilitaStoricaBreve +
                ", dinaRisk=" + dinaRisk +
                ", volatilityRatio=" + volatilityRatio +
                ", distPercleMinUltimi=" + distPercleMinUltimi +
                ", distPercleMasUltimi=" + distPercleMasUltimi +
                ", pivotH=" + pivotH +
                ", pivotL=" + pivotL +
                ", pivot1H=" + pivot1H +
                ", pivot1L=" + pivot1L +
                ", segnaleUltimaSeduta='" + segnaleUltimaSeduta + '\'' +
                ", ultimoSegnaleAttivo='" + ultimoSegnaleAttivo + '\'' +
                ", chiusuraSegnale=" + chiusuraSegnale +
                ", risultPercPosIn=" + risultPercPosIn +
                ", stopLoss=" + stopLoss +
                ", target=" + target +
                ", duePerc='" + duePerc + '\'' +
                ", meno2Perc='" + meno2Perc + '\'' +
                ", quattroPerc='" + quattroPerc + '\'' +
                ", meno4Perc='" + meno4Perc + '\'' +
                ", strategiaPortafoglio='" + strategiaPortafoglio + '\'' +
                ", topFlop='" + topFlop + '\'' +
                ", andamentoVolumi='" + andamentoVolumi + '\'' +
                ", forzaRelativa='" + forzaRelativa + '\'' +
                '}';
    }
}
