/*
 * EstimatesProviderImpl.java
 *
 * Created on 10.09.2008 09:59:51
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.estimates;

import de.marketmaker.istar.domain.data.HistoricEstimates;
import de.marketmaker.istar.domain.data.StockRevenueSummary;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.domainimpl.data.NullHistoricEstimates;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EstimatesProviderImpl implements EstimatesProvider {

    private EstimatesProviderFactset providerFactset;

    private EstimatesProviderFactset providerBoerseGo;

    private EstimatesProviderFactset providerMux;

    private EstimatesProviderThomsonReuters providerThomsonReutersDzbank;

    private HistoricEstimatesProvider historicProviderFactset;

    private HistoricEstimatesProvider historicProviderThomsonReutersDzbank;

    public void setProviderBoerseGo(EstimatesProviderFactset providerBoerseGo) {
        this.providerBoerseGo = providerBoerseGo;
    }

    public void setProviderMux(EstimatesProviderFactset providerMux) {
        this.providerMux = providerMux;
    }

    public void setProviderFactset(EstimatesProviderFactset providerFactset) {
        this.providerFactset = providerFactset;
    }

    public void setProviderThomsonReutersDzbank(
            EstimatesProviderThomsonReuters providerThomsonReutersDzbank) {
        this.providerThomsonReutersDzbank = providerThomsonReutersDzbank;
    }

    public void setHistoricProviderFactset(
            HistoricEstimatesProvider historicProviderFactset) {
        this.historicProviderFactset = historicProviderFactset;
    }

    public void setHistoricProviderThomsonReutersDzbank(
            HistoricEstimatesProvider historicProviderThomsonReutersDzbank) {
        this.historicProviderThomsonReutersDzbank = historicProviderThomsonReutersDzbank;
    }

    public List<Long> getEstimatesDirectory(Profile profile, DateTime refDate) {
        if (profile.isAllowed(Selector.FACTSET)
            || profile.isAllowed(Selector.FACTSET_ESTIMATES_IEX_MEDIA)) {
            return this.providerFactset.getEstimatesDirectory(profile, refDate);
        }

        if (profile.isAllowed(Selector.FACTSET_ESTIMATES_FOR_BOERSE_GO)) {
            return this.providerBoerseGo.getEstimatesDirectory(profile, refDate);
        }

        if (profile.isAllowed(Selector.THOMSONREUTERS_ESTIMATES_DZBANK)) {
            return this.providerThomsonReutersDzbank.getEstimatesDirectory(refDate);
        }

        return Collections.emptyList();
    }

    public List<StockRevenueSummary> getEstimates(Profile profile, long instrumentid) {
        if (profile.isAllowed(Selector.FACTSET)) {
            return this.providerFactset.getEstimates(profile, instrumentid);
        }

        if (profile.isAllowed(Selector.FACTSET_ESTIMATES_IEX_MEDIA)) {
            // allow factset estimates, but limits to fiscal year 0 to 4
            // currently no fy5+ anyway
            final List<StockRevenueSummary> estimates =
                this.providerFactset.getEstimates(profile, instrumentid);
            return estimates.size() <= 5 ? estimates : estimates.subList(0, 5);
        }

        if (profile.isAllowed(Selector.FACTSET_ESTIMATES_FOR_BOERSE_GO)) {
            return this.providerBoerseGo.getEstimates(profile, instrumentid);
        }

        if (profile.isAllowed(Selector.FACTSET_ESTIMATES_INFRONT_ANALYTICS)
                || profile.isAllowed(Selector.FACTSET_ESTIMATES_ABM_ONLINE_SOLUTIONS)) {
            return this.providerMux.getEstimates(profile, instrumentid);
        }

        if (profile.isAllowed(Selector.THOMSONREUTERS_ESTIMATES_DZBANK)) {
            return this.providerThomsonReutersDzbank.getEstimates(instrumentid);
        }

        return Collections.emptyList();
    }

    public HistoricEstimates getHistoricEstimates(Profile profile, long instrumentid) {
        if (profile.isAllowed(Selector.THOMSONREUTERS_ESTIMATES_DZBANK)) {
            return this.historicProviderThomsonReutersDzbank.getHistoricEstimates(instrumentid);
        }

        if (profile.isAllowed(Selector.FACTSET)) {
            return this.historicProviderFactset.getHistoricEstimates(instrumentid);
        }

        return NullHistoricEstimates.INSTANCE;
    }
}
