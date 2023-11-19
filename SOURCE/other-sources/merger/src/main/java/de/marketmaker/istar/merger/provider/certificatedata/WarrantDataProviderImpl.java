/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import de.marketmaker.istar.merger.provider.MasterDataProvider;
import de.marketmaker.istar.merger.provider.ProviderPreference;

import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domain.data.MasterDataWarrant;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class WarrantDataProviderImpl implements WarrantDataProvider {

    private MasterDataProvider<MasterDataWarrant> vwdProvider, sedexProvider;

    private EdgDataProviderImpl edgDataProvider;

    public void setVwdProvider(MasterDataProvider<MasterDataWarrant> vwdProvider) {
        this.vwdProvider = vwdProvider;
    }

    public void setSedexProvider(MasterDataProvider<MasterDataWarrant> sedexProvider) {
        this.sedexProvider = sedexProvider;
    }

    public void setEdgDataProvider(EdgDataProviderImpl edgDataProvider) {
        this.edgDataProvider = edgDataProvider;
    }

    @Override
    public MasterDataWarrant getMasterData(long instrumentid, ProviderPreference preference) {
        switch(preference) {
            case VWD:
                return this.vwdProvider.getMasterData(instrumentid);
            case SEDEX:
                return this.sedexProvider.getMasterData(instrumentid);
            default:
                throw new IllegalArgumentException(preference + " is not available");
        }
    }

    @Override
    public MasterDataWarrant getMasterData(long instrumentid) {
        return getMasterData(instrumentid, ProviderPreference.VWD);
    }

    public EdgData getEdgData(long instrumentid) {
        return this.edgDataProvider.getEdgData(instrumentid);
    }
}
