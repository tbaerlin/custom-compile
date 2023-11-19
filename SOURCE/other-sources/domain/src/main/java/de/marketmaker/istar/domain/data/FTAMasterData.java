/*
 * FTAMasterData.java
 *
 * Created on 6/13/13 9:12 AM
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import org.joda.time.LocalDate;

import java.math.BigDecimal;

/**
 * @author Stefan Willenbrock
 */
public interface FTAMasterData {
    long getInstrumentid();

    String getIsin();

    LocalDate getLastUpdate();

    String getTrendBreve();

    Integer getInAttoDaGiorniTBreve();

    String getTrendMedio();

    Integer getInAttoDaGiorniTMedio();

    String getTrendLungoTermine();

    Integer getInAttoDaGiorniTLungo();

    BigDecimal getResistenza();

    BigDecimal getSupporto();

    BigDecimal getResistenza1();

    BigDecimal getSupporto1();

    BigDecimal getMm20Giorni();

    BigDecimal getMm40Giorni();

    BigDecimal getMm100Giorni();

    BigDecimal getRsi8Sedute();

    BigDecimal getBeta3Mesi();

    BigDecimal getIndiceCorrelazione3Mesi();

    BigDecimal getAlfaMensile();

    BigDecimal getVolatilitaStoricaBreve();

    BigDecimal getDinaRisk();

    BigDecimal getVolatilityRatio();

    BigDecimal getDistPercleMinUltimi();

    BigDecimal getDistPercleMasUltimi();

    BigDecimal getPivotH();

    BigDecimal getPivotL();

    BigDecimal getPivot1H();

    BigDecimal getPivot1L();

    String getSegnaleUltimaSeduta();

    String getUltimoSegnaleAttivo();

    BigDecimal getChiusuraSegnale();

    BigDecimal getRisultPercPosIn();

    BigDecimal getStopLoss();

    BigDecimal getTarget();

    String getDuePerc();

    String getMeno2Perc();

    String getQuattroPerc();

    String getMeno4Perc();

    String getStrategiaPortafoglio();

    String getTopFlop();

    String getAndamentoVolumi();

    String getForzaRelativa();
}
