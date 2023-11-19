/*
 * FTACommentaryImpl.java
 *
 * Created on 5/29/13 12:48 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.data;

import de.marketmaker.istar.domain.data.FTACommentary;

import java.io.Serializable;

/**
 * @author Stefan Willenbrock
 */
public class FTACommentaryImpl implements FTACommentary, Serializable {
    private final static long serialVersionUID = 1L;

    public static class Builder {
        private long instrumentid = -1;
        private String previsioni;
        private String situazione;
        private String analisiTecnica;
        private String segnaliOperativi;
        private String analisiStatistica;

        public FTACommentaryImpl build() {
            FTACommentaryImpl fc = new FTACommentaryImpl(this);
            if (!fc.isValid()) {
                throw new IllegalStateException("instrumentid not set");
            }
            return fc;
        }

        public void setInstrumentid(long instrumentid) {
            this.instrumentid = instrumentid;
        }

        public void setPrevisioni(String previsioni) {
            this.previsioni = previsioni;
        }

        public void setSituazione(String situazione) {
            this.situazione = situazione;
        }

        public void setAnalisiTecnica(String analisiTecnica) {
            this.analisiTecnica = analisiTecnica;
        }

        public void setSegnaliOperativi(String segnaliOperativi) {
            this.segnaliOperativi = segnaliOperativi;
        }

        public void setAnalisiStatistica(String analisiStatistica) {
            this.analisiStatistica = analisiStatistica;
        }
    }

    private long instrumentid;
    private String previsioni;
    private String situazione;
    private String analisiTecnica;
    private String segnaliOperativi;
    private String analisiStatistica;

    private FTACommentaryImpl(Builder b) {
        this.instrumentid = b.instrumentid;
        this.previsioni = b.previsioni;
        this.situazione = b.situazione;
        this.analisiTecnica = b.analisiTecnica;
        this.segnaliOperativi = b.segnaliOperativi;
        this.analisiStatistica = b.analisiStatistica;
    }

    private boolean isValid() {
        return this.instrumentid != -1;
    }

    @Override
    public long getInstrumentid() {
        return instrumentid;
    }

    @Override
    public String getPrevisioni() {
        return previsioni;
    }

    @Override
    public String getSituazione() {
        return situazione;
    }

    @Override
    public String getAnalisiTecnica() {
        return analisiTecnica;
    }

    @Override
    public String getSegnaliOperativi() {
        return segnaliOperativi;
    }

    @Override
    public String getAnalisiStatistica() {
        return analisiStatistica;
    }

    @Override
    public String toString() {
        return "FTACommentaryImpl{" +
                "instrumentid=" + instrumentid +
                ", previsioni='" + previsioni + '\'' +
                ", situazione='" + situazione + '\'' +
                ", analisiTecnica='" + analisiTecnica + '\'' +
                ", segnaliOperativi='" + segnaliOperativi + '\'' +
                ", analisiStatistica='" + analisiStatistica + '\'' +
                '}';
    }
}
