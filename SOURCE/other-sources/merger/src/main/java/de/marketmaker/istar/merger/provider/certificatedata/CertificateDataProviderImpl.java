/*
 * CertificateDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.merger.provider.MasterDataProvider;
import de.marketmaker.istar.merger.provider.ProviderPreference;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CertificateDataProviderImpl implements CertificateDataProvider {
    private MasterDataProvider<MasterDataCertificate> vwdProvider, smfProvider, sedexProvider;

    private EdgDataProviderImpl edgDataProvider;

    public void setVwdProvider(MasterDataProvider<MasterDataCertificate> vwdProvider) {
        this.vwdProvider = vwdProvider;
    }

    public void setSmfProvider(MasterDataProvider<MasterDataCertificate> smfProvider) {
        this.smfProvider = smfProvider;
    }

    public void setSedexProvider(MasterDataProvider<MasterDataCertificate> sedexProvider) {
        this.sedexProvider = sedexProvider;
    }

    public void setEdgDataProvider(EdgDataProviderImpl edgDataProvider) {
        this.edgDataProvider = edgDataProvider;
    }

    public MasterDataCertificate getMasterData(long instrumentid, ProviderPreference preference) {
        switch(preference) {
            case VWD:
                return this.vwdProvider.getMasterData(instrumentid);
            case SMF:
                return this.smfProvider.getMasterData(instrumentid);
            case SEDEX:
                return this.sedexProvider.getMasterData(instrumentid);
            default:
                throw new IllegalArgumentException(preference + " is no valid provider preference");
        }
    }

    @Override
    public MasterDataCertificate getMasterData(long instrumentid, boolean smf) {
        return getMasterData(instrumentid, smf ? ProviderPreference.SMF : ProviderPreference.VWD);
    }

    @Override
    public MasterDataCertificate getMasterData(long instrumentid) {
        return getMasterData(instrumentid, ProviderPreference.VWD);
    }

    public EdgData getEdgData(long instrumentid) {
        return this.edgDataProvider.getEdgData(instrumentid);
    }

    public static void main(String[] args) throws Exception {
        final CertificateDataProviderImpl p = new CertificateDataProviderImpl();

        MasterDataCertificateProviderVwd vwd = new MasterDataCertificateProviderVwd();
        vwd.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-gatrixx-certificate-masterdata.buf"));
        vwd.afterPropertiesSet();

        final MasterDataCertificateProviderSmf smf = new MasterDataCertificateProviderSmf();
        smf.setActiveMonitor(new ActiveMonitor());
        smf.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-smf-cer-wnt-masterdata.xml.gz"));
        smf.afterPropertiesSet();

        p.setVwdProvider(vwd);
        p.setSmfProvider(smf);

        System.out.println(p.getMasterData(1672293L, ProviderPreference.VWD));
        System.out.println(p.getMasterData(581719L, ProviderPreference.SMF));
        Thread.sleep(Long.MAX_VALUE);
    }
}
