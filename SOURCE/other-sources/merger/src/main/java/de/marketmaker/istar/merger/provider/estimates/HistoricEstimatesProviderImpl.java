/*
 * HistoricEstimatesProviderImpl.java
 *
 * Created on 09.08.2006 07:57:42
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.HistoricEstimates;
import de.marketmaker.istar.domainimpl.data.HistoricEstimatesImpl;
import de.marketmaker.istar.domainimpl.data.NullHistoricEstimates;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class HistoricEstimatesProviderImpl implements InitializingBean, HistoricEstimatesProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;
    private File historicEstimatesFile;

    private final AtomicReference<Map<Long, HistoricEstimates>> estimates = new AtomicReference<>();

    public void setHistoricEstimatesFile(File historicEstimatesFile) {
        this.historicEstimatesFile = historicEstimatesFile;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource historicEstimates = new FileResource(this.historicEstimatesFile);
        historicEstimates.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readHistoricEstimates();
            }
        });
        this.activeMonitor.addResource(historicEstimates);

        readHistoricEstimates();
    }

    private void readHistoricEstimates() {
        try {
            final HistoricEstimatesReader br = new HistoricEstimatesReader();
            br.read(this.historicEstimatesFile);

            this.estimates.set(br.getValues());
        }
        catch (Exception e) {
            this.logger.error("<readHistoricEstimates> failed", e);
        }
    }

    @Override
    public HistoricEstimates getHistoricEstimates(long instrumentid) {
        final Map<Long, HistoricEstimates> map = this.estimates.get();
        final HistoricEstimates result = map.get(instrumentid);
        return result != null ? result : NullHistoricEstimates.INSTANCE;
    }

    public class HistoricEstimatesReader extends AbstractSaxReader {
        final Map<Long, HistoricEstimates> values = new HashMap<>();

        private long instrumentid = -1;
        private BigDecimal earningPerShare1m;
        private BigDecimal earningPerShare3m;
        private BigDecimal earningPerShareGrowth1m;
        private BigDecimal earningPerShareGrowth3m;
        private BigDecimal cashflowPerShare1m;
        private BigDecimal cashflowPerShare3m;
        private BigDecimal priceEarningRatio1m;
        private BigDecimal priceEarningRatio3m;
        private BigDecimal preTaxProfit1m;
        private BigDecimal preTaxProfit3m;
        private BigDecimal recommendation1w;
        private BigDecimal recommendation2w;
        private BigDecimal recommendation3w;
        private BigDecimal recommendation1m;
        private BigDecimal recommendation2m;
        private BigDecimal recommendation3m;
        private BigDecimal recommendation4m;
        private Integer numBuy1w;
        private Integer numOverweight1w;
        private Integer numHold1w;
        private Integer numUnderweight1w;
        private Integer numSell1w;
        private Integer numTotal1w;
        private Integer numBuy1m;
        private Integer numOverweight1m;
        private Integer numHold1m;
        private Integer numUnderweight1m;
        private Integer numSell1m;
        private Integer numTotal1m;

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new quote => process
                    storeFields();
                    return;
                }

                if (!hasText()) {
                    return;
                }

                if (tagName.equals("IID")) {
                    this.instrumentid = getCurrentLong();
                }
                else if (tagName.equals("EPS_1M")) {
                    this.earningPerShare1m = getCurrentBigDecimal();
                }
                else if (tagName.equals("EPS_3M")) {
                    this.earningPerShare3m = getCurrentBigDecimal();
                }
                else if (tagName.equals("EPS_GROWTH_1M")) {
                    this.earningPerShareGrowth1m = getCurrentBigDecimal();
                }
                else if (tagName.equals("EPS_GROWTH_3M")) {
                    this.earningPerShareGrowth3m = getCurrentBigDecimal();
                }
                else if (tagName.equals("CFPS_1M")) {
                    this.cashflowPerShare1m = getCurrentBigDecimal();
                }
                else if (tagName.equals("CFPS_3M")) {
                    this.cashflowPerShare3m = getCurrentBigDecimal();
                }
                else if (tagName.equals("P_E_1M")) {
                    this.priceEarningRatio1m = getCurrentBigDecimal();
                }
                else if (tagName.equals("P_E_3M")) {
                    this.priceEarningRatio3m = getCurrentBigDecimal();
                }
                else if (tagName.equals("PRE_TAX_PROFIT_1M")) {
                    this.preTaxProfit1m = getCurrentBigDecimal();
                }
                else if (tagName.equals("PRE_TAX_PROFIT_3M")) {
                    this.preTaxProfit3m = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_1W")) {
                    this.recommendation1w = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_2W")) {
                    this.recommendation2w = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_3W")) {
                    this.recommendation3w = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_1M")) {
                    this.recommendation1m = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_2M")) {
                    this.recommendation2m = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_3M")) {
                    this.recommendation3m = getCurrentBigDecimal();
                }
                else if (tagName.equals("RECOMMENDATION_4M")) {
                    this.recommendation4m = getCurrentBigDecimal();
                }
                else if (tagName.equals("NUM_BUY_1W")) {
                    this.numBuy1w = getCurrentInt();
                }
                else if (tagName.equals("NUM_OVERWEIGHT_1W")) {
                    this.numOverweight1w = getCurrentInt();
                }
                else if (tagName.equals("NUM_HOLD_1W")) {
                    this.numHold1w = getCurrentInt();
                }
                else if (tagName.equals("NUM_UNDERWEIGHT_1W")) {
                    this.numUnderweight1w = getCurrentInt();
                }
                else if (tagName.equals("NUM_SELL_1W")) {
                    this.numSell1w = getCurrentInt();
                }
                else if (tagName.equals("NUM_TOTAL_1W")) {
                    this.numTotal1w = getCurrentInt();
                }
                else if (tagName.equals("NUM_BUY_1M")) {
                    this.numBuy1m = getCurrentInt();
                }
                else if (tagName.equals("NUM_OVERWEIGHT_1M")) {
                    this.numOverweight1m = getCurrentInt();
                }
                else if (tagName.equals("NUM_HOLD_1M")) {
                    this.numHold1m = getCurrentInt();
                }
                else if (tagName.equals("NUM_UNDERWEIGHT_1M")) {
                    this.numUnderweight1m = getCurrentInt();
                }
                else if (tagName.equals("NUM_SELL_1M")) {
                    this.numSell1m = getCurrentInt();
                }
                else if (tagName.equals("NUM_TOTAL_1M")) {
                    this.numTotal1m = getCurrentInt();
                }
                else if (tagName.equals("ROWS")) {
                    // ignored
                }
                else {
                    notParsed(tagName);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.instrumentid < 0) {
                reset();
                return;
            }

            this.values.put(this.instrumentid, new HistoricEstimatesImpl(
                    this.instrumentid, this.earningPerShare1m, this.earningPerShare3m, this.earningPerShareGrowth1m,
                    this.earningPerShareGrowth3m, this.cashflowPerShare1m, this.cashflowPerShare3m, this.priceEarningRatio1m,
                    this.priceEarningRatio3m, this.preTaxProfit1m, this.preTaxProfit3m, this.recommendation1w, this.recommendation2w,
                    this.recommendation3w, this.recommendation1m, this.recommendation2m, this.recommendation3m, this.recommendation4m,
                    this.numBuy1w, this.numOverweight1w, this.numHold1w, this.numUnderweight1w, this.numSell1w, this.numTotal1w, this.numBuy1m,
                    this.numOverweight1m, this.numHold1m, this.numUnderweight1m, this.numSell1m, this.numTotal1m
            ));

            reset();
        }

        protected void reset() {
            this.instrumentid = -1;
            this.earningPerShare1m = null;
            this.earningPerShare3m = null;
            this.earningPerShareGrowth1m = null;
            this.earningPerShareGrowth3m = null;
            this.cashflowPerShare1m = null;
            this.cashflowPerShare3m = null;
            this.priceEarningRatio1m = null;
            this.priceEarningRatio3m = null;
            this.preTaxProfit1m = null;
            this.preTaxProfit3m = null;
            this.recommendation1w = null;
            this.recommendation2w = null;
            this.recommendation3w = null;
            this.recommendation1m = null;
            this.recommendation2m = null;
            this.recommendation3m = null;
            this.recommendation4m = null;
            this.numBuy1w = null;
            this.numOverweight1w = null;
            this.numHold1w = null;
            this.numUnderweight1w = null;
            this.numSell1w = null;
            this.numTotal1w = null;
            this.numBuy1m = null;
            this.numOverweight1m = null;
            this.numHold1m = null;
            this.numUnderweight1m = null;
            this.numSell1m = null;
            this.numTotal1m = null;
            this.errorOccured = false;
        }

        public Map<Long, HistoricEstimates> getValues() {
            return values;
        }
    }

    public static void main(String[] args) throws Exception {
        final HistoricEstimatesProviderImpl ep = new HistoricEstimatesProviderImpl();
        ep.setHistoricEstimatesFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-factset-historic-estimates.xml.gz"));
        ep.setActiveMonitor(new ActiveMonitor());
        ep.afterPropertiesSet();

        final HistoricEstimates estimates = ep.getHistoricEstimates(20665L);
        System.out.println(estimates);
    }
}
