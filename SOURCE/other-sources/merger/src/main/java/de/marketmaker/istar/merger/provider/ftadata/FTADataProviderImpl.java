/*
 * FTAInstrumentProviderImpl.java
 *
 * Created on 5/17/13 1:57 PM
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.ftadata;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.FTACommentary;
import de.marketmaker.istar.domain.data.FTAMasterData;
import de.marketmaker.istar.domain.data.FTAMetaData;
import de.marketmaker.istar.domainimpl.data.FTACommentaryImpl;
import de.marketmaker.istar.domainimpl.data.FTAMasterDataImpl;
import de.marketmaker.istar.domainimpl.data.FTAMetaDataImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.InitializingBean;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.apache.commons.lang.StringEscapeUtils.unescapeXml;

/**
 * @author Stefan Willenbrock
 */
public final class FTADataProviderImpl implements InitializingBean, FTADataProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;

    private File masterData;
    
    private File commentary;
    
    private File metaData;

    private volatile Map<Long, FTAMasterData> masterDataMap;

    private volatile Map<Long, FTACommentary> commentaryMap;

    private volatile FTAMetaData metaDataRecord;

    public void setMasterDataFile(File masterData) {
        this.masterData = masterData;
    }

    public void setCommentaryFile(File commentary) {
        this.commentary = commentary;
    }

    public void setMetaDataFile(File metaData) {
        this.metaData = metaData;
    }

    private FTAMasterData getMasterData(long iid) {
        return masterDataMap.get(iid);
    }

    private FTACommentary getCommentary(long iid) {
        return commentaryMap.get(iid);
    }

    private FTAMetaData getMetaData() {
        return metaDataRecord;
    }

    @Override
    public FTADataResponse getFTAData(FTADataRequest request) {
        final long iid = request.getInstrumentid();
        return new FTADataResponse(getMasterData(iid), getCommentary(iid), getMetaData());
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception { 
        doReadMasterData();
        doReadMetaData();
        doReadCommentary();
        
        if (this.activeMonitor != null) {
            addActiveResource(this.masterData, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    readMasterData();
                }
            });
            addActiveResource(this.commentary, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    readCommentary();
                }
            });
            addActiveResource(this.metaData, new PropertyChangeListener() {
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    readMetaData();
                }
            });
        }
    }

    private void addActiveResource(final File file, final PropertyChangeListener listener) {
        final FileResource resource = new FileResource(file);
        resource.addPropertyChangeListener(listener);
        this.activeMonitor.addResource(resource);
    }

    private void readMasterData() {
        try {
            doReadMasterData();
        } catch (Exception e) {
            this.logger.error("<readMasterData> failed", e);
        }
    }

    private void doReadMasterData() throws Exception {
        this.masterDataMap = new MasterDataReader().read(this.masterData);        
    }

    private void readCommentary() {
        try {
            doReadCommentary();
        } catch (Exception e) {
            this.logger.error("<readCommentary> failed", e);
        }
    }

    private void doReadCommentary() throws Exception {
        this.commentaryMap = new CommentaryReader().read(this.commentary);        
    }

    private void readMetaData() {
        try {
            doReadMetaData();
        } catch (Exception e) {
            this.logger.error("<readMetaData> failed", e);
        }
    }

    private void doReadMetaData() throws Exception {
        this.metaDataRecord = new MetaDataReader().read(this.metaData);        
    }

    private static class MasterDataReader extends IstarMdpExportReader<Map<Long, FTAMasterData>> {
        private Map<Long, FTAMasterData> result = new HashMap<>();

        @Override
        protected void handleRow() {
            final FTAMasterDataImpl.Builder builder = new FTAMasterDataImpl.Builder();
            builder.setInstrumentid(getLong("IID"));
            builder.setIsin(get("A_ISIN"));
            builder.setLastUpdate(getLocalDate("A_LASTUPDATE"));
            builder.setTrendBreve(get("A_TREND_BREVE"));
            builder.setInAttoDaGiorniTBreve(getInt("A_IN_ATTO_DA_GIORNI_TBREVE"));
            builder.setTrendMedio(get("A_TREND_MEDIO"));
            builder.setInAttoDaGiorniTMedio(getInt("A_IN_ATTO_DA_GIORNI_TMEDIO"));
            builder.setTrendLungoTermine(get("A_TREND_LUNGO_TERMINE"));
            builder.setInAttoDaGiorniTLungo(getInt("A_IN_ATTO_DA_GIORNI_TLUNGO"));
            builder.setResistenza(getBigDecimal("A_RESISTENZA"));
            builder.setSupporto(getBigDecimal(("A_SUPPORTO")));
            builder.setResistenza1(getBigDecimal(("A_RESISTENZA_1")));
            builder.setSupporto1(getBigDecimal(("A_SUPPORTO_1")));
            builder.setMm20Giorni(getBigDecimal(("A_MM_20_GIORNI")));
            builder.setMm40Giorni(getBigDecimal(("A_MM_40_GIORNI")));
            builder.setMm100Giorni(getBigDecimal(("A_MM_100_GIORNI")));
            builder.setRsi8Sedute(getBigDecimal(("A_RSI_8_SEDUTE")));
            builder.setBeta3Mesi(getBigDecimal(("A_BETA_3_MESI")));
            builder.setIndiceCorrelazione3Mesi(getBigDecimal(("A_INDICE_CORRELAZIONE_3_MESI")));
            builder.setAlfaMensile(getBigDecimal(("A_ALFA_MENSILE")));
            builder.setVolatilitaStoricaBreve(getBigDecimal(("A_VOLATILITA_STORICA_BREVE")));
            builder.setDinaRisk(getBigDecimal(("A_DINA_RISK")));
            builder.setVolatilityRatio(getBigDecimal(("A_VOLATILITY_RATIO")));
            builder.setDistPercleMinUltimi(getBigDecimal(("A_DIST_PERCLE_MIN_ULTIMI_")));
            builder.setDistPercleMasUltimi(getBigDecimal(("A_DIST_PERCLE_MAS_ULTIMI")));
            builder.setPivotH(getBigDecimal(("A_PIVOT_H")));
            builder.setPivotL(getBigDecimal(("A_PIVOT_L")));
            builder.setPivot1H(getBigDecimal(("A_PIVOT_1_H")));
            builder.setPivot1L(getBigDecimal(("A_PIVOT_1_L")));
            builder.setSegnaleUltimaSeduta(get("A_SEGNALE_ULTIMA_SEDUTA"));
            builder.setUltimoSegnaleAttivo(get("A_ULTIMO_SEGNALE_ATTIVO"));
            builder.setChiusuraSegnale(getBigDecimal(("A_CHIUSURA_SEGNALE")));
            builder.setRisultPercPosIn(getBigDecimal(("A_RISULT_PERC_POS_IN_")));
            builder.setStopLoss(getBigDecimal(("A_STOP_LOSS")));
            builder.setTarget(getBigDecimal(("A_TARGET")));
            builder.setDuePerc(get("A_DUEPERC"));
            builder.setMeno2Perc(get("A_MENO2PERC"));
            builder.setQuattroPerc(get("A_QUATTROPERC"));
            builder.setMeno4Perc(get("A_MENO4PERC"));
            builder.setStrategiaPortafoglio(get("A_STRATEGIA_PORTAFOGLIO"));
            builder.setTopFlop(get("A_TOP_FLOP"));
            builder.setAndamentoVolumi(get("A_ANDAMENTO_VOLUMI"));
            builder.setForzaRelativa(get("A_FORZA_RELATIVA"));
            final FTAMasterData fmd = builder.build();
            this.result.put(fmd.getInstrumentid(), fmd);
        }

        private LocalDate getLocalDate(final String name) {
            final DateTime dt = getDateTime(name);
            return (dt != null) ? dt.toLocalDate() : null;
        }

        @Override
        protected Map<Long, FTAMasterData> getResult() {
            return this.result;
        }
    }

    private static class CommentaryReader extends IstarMdpExportReader<Map<Long, FTACommentary>> {
        private Map<Long, FTACommentary> result = new HashMap<>();

        @Override
        protected void handleRow() {
            final FTACommentaryImpl.Builder builder = new FTACommentaryImpl.Builder();
            builder.setInstrumentid(getLong("IID"));
            builder.setPrevisioni(unescapeXml(get("PREVISIONI")));
            builder.setSituazione(unescapeXml(get("SITUAZIONE")));
            builder.setAnalisiTecnica(unescapeXml(get("ANALISI_TECNICA")));
            builder.setSegnaliOperativi(unescapeXml(get("SEGNALI_OPERATIVI")));
            builder.setAnalisiStatistica(unescapeXml(get("ANALISI_STATISTICA")));
            final FTACommentaryImpl fc = builder.build();
            this.result.put(fc.getInstrumentid(), fc);
        }

        @Override
        protected Map<Long, FTACommentary> getResult() {
            return this.result;
        }
    }

    private static class MetaDataReader extends IstarMdpExportReader<FTAMetaData> {
        private FTAMetaDataImpl.Builder builder = new FTAMetaDataImpl.Builder();

        @Override
        protected void handleRow() {
            final String fieldIdStr = get("A_FIELDID");
            final String description = unescapeXml(get("A_DESCRIPTION"));
            if ("Trend_Breve".equals(fieldIdStr)) {
                this.builder.setTrendBreveMeta(description);
            } else if ("In_atto_da_giorni_tbreve".equals(fieldIdStr)) {
                this.builder.setInAttoDaGiorniTBreveMeta(description);
            } else if ("Trend_medio".equals(fieldIdStr)) {
                this.builder.setTrendMedioMeta(description);
            } else if ("In_atto_da_giorni_tmedio".equals(fieldIdStr)) {
                this.builder.setInAttoDaGiorniTMedioMeta(description);
            } else if ("Trend_lungo_Termine".equals(fieldIdStr)) {
                this.builder.setTrendLungoTermineMeta(description);
            } else if ("In_atto_da_giorni_tlungo".equals(fieldIdStr)) {
                this.builder.setInAttoDaGiorniTLungoMeta(description);
            } else if ("Resistenza".equals(fieldIdStr)) {
                this.builder.setResistenzaMeta(description);
            } else if ("Supporto".equals(fieldIdStr)) {
                this.builder.setSupportoMeta(description);
            } else if ("Resistenza_1".equals(fieldIdStr)) {
                this.builder.setResistenza1Meta(description);
            } else if ("Supporto_1".equals(fieldIdStr)) {
                this.builder.setSupporto1Meta(description);
            } else if ("MM_20_giorni".equals(fieldIdStr)) {
                this.builder.setMm20GiorniMeta(description);
            } else if ("MM_40_giorni".equals(fieldIdStr)) {
                this.builder.setMm40GiorniMeta(description);
            } else if ("MM_100_giorni".equals(fieldIdStr)) {
                this.builder.setMm100GiorniMeta(description);
            } else if ("Rsi_8_sedute".equals(fieldIdStr)) {
                this.builder.setRsi8SeduteMeta(description);
            } else if ("Beta_3_mesi".equals(fieldIdStr)) {
                this.builder.setBeta3MesiMeta(description);
            } else if ("Indice_correlazione_3_mesi".equals(fieldIdStr)) {
                this.builder.setIndiceCorrelazione3MesiMeta(description);
            } else if ("Alfa_mensile".equals(fieldIdStr)) {
                this.builder.setAlfaMensileMeta(description);
            } else if ("Volatilita_storica_breve".equals(fieldIdStr)) {
                this.builder.setVolatilitaStoricaBreveMeta(description);
            } else if ("Dina_Risk".equals(fieldIdStr)) {
                this.builder.setDinaRiskMeta(description);
            } else if ("Volatility_Ratio".equals(fieldIdStr)) {
                this.builder.setVolatilityRatioMeta(description);
            } else if ("Distanza_percle_minimi_ultimi_3_mesi".equals(fieldIdStr)) {
                this.builder.setDistPercleMinUltimiMeta(description);
            } else if ("Distanza_percle_massimi_ultimi_3_mesi".equals(fieldIdStr)) {
                this.builder.setDistPercleMasUltimiMeta(description);
            } else if ("Pivot_H".equals(fieldIdStr)) {
                this.builder.setPivotHMeta(description);
            } else if ("Pivot_L".equals(fieldIdStr)) {
                this.builder.setPivotLMeta(description);
            } else if ("Pivot_1_H".equals(fieldIdStr)) {
                this.builder.setPivot1HMeta(description);
            } else if ("Pivot_1_L".equals(fieldIdStr)) {
                this.builder.setPivot1LMeta(description);
            } else if ("Segnale_ultima_seduta".equals(fieldIdStr)) {
                this.builder.setSegnaleUltimaSedutaMeta(description);
            } else if ("Ultimo_segnale_attivo".equals(fieldIdStr)) {
                this.builder.setUltimoSegnaleAttivoMeta(description);
            } else if ("Chiusura_segnale".equals(fieldIdStr)) {
                this.builder.setChiusuraSegnaleMeta(description);
            } else if ("Risultato_percle_posizione_in_esser".equals(fieldIdStr)) {
                this.builder.setRisultPercPosInMeta(description);
            } else if ("Stop_loss".equals(fieldIdStr)) {
                this.builder.setStopLossMeta(description);
            } else if ("Target".equals(fieldIdStr)) {
                this.builder.setTargetMeta(description);
            } else if ("dueperc".equals(fieldIdStr)) {
                this.builder.setDuePercMeta(description);
            } else if ("meno2perc".equals(fieldIdStr)) {
                this.builder.setMeno2PercMeta(description);
            } else if ("quattroperc".equals(fieldIdStr)) {
                this.builder.setQuattroPercMeta(description);
            } else if ("meno4perc".equals(fieldIdStr)) {
                this.builder.setMeno4PercMeta(description);
            } else if ("Strategia_portafoglio".equals(fieldIdStr)) {
                this.builder.setStrategiaPortafoglioMeta(description);
            } else if ("Top_Flop".equals(fieldIdStr)) {
                this.builder.setTopFlopMeta(description);
            } else if ("Andamento_volumi".equals(fieldIdStr)) {
                this.builder.setAndamentoVolumiMeta(description);
            } else if ("Forza_relativa".equals(fieldIdStr)) {
                this.builder.setForzaRelativaMeta(description);
            } else {
                this.logger.error(String.format("Text '%1' of tag '%2' is an unknown type.", fieldIdStr, "A_FIELDID"));
            }
        }

        @Override
        protected FTAMetaData getResult() {
            return this.builder.build();
        }
    }
}
