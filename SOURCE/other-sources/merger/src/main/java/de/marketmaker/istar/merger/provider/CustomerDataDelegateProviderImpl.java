/*
 * CustomerDataDelegateProviderImpl.java
 *
 * Created on 07.10.2008 13:31:10
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.util.List;

import de.marketmaker.istar.domain.data.DerivativeIpoData;
import de.marketmaker.istar.domain.data.WGZCertificateData;
import de.marketmaker.istar.domain.special.DzBankRecord;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class CustomerDataDelegateProviderImpl implements CustomerDataDelegateProvider {

    private DerivativeIpoProviderDzbank derivativeIpoProviderDzbank;

    private WGZCertificateProvider wgzCertificateProvider;

    private DzBankRecordProvider dzBankRecordProvider;

    public void setDerivativeIpoProviderDzbank(
            DerivativeIpoProviderDzbank derivativeIpoProviderDzbank) {
        this.derivativeIpoProviderDzbank = derivativeIpoProviderDzbank;
    }

    public void setDzBankRecordProvider(DzBankRecordProvider dzBankRecordProvider) {
        this.dzBankRecordProvider = dzBankRecordProvider;
    }

    public void setWgzCertificateProvider(WGZCertificateProvider wgzCertificateProvider) {
        this.wgzCertificateProvider = wgzCertificateProvider;
    }

    @Override
    public List<DzBankRecord> getDzBankRecords(List<Long> iids) {
        return this.dzBankRecordProvider.getDzBankRecords(iids);
    }

    @Override
    public DzBankRecordSearchResponse searchDzBankRecords(IstarQueryListRequest req) {
        return this.dzBankRecordProvider.searchDzBankRecords(req);
    }

    @Override
    public DzBankRecordMetaDataResponse createDzBankRecordsMetadata() {
        return this.dzBankRecordProvider.createDzBankRecordsMetadata();
    }

    public List<DerivativeIpoData> getDerivateIposDzbank(String type) {
        return this.derivativeIpoProviderDzbank.getDerivateIposDzbank(type);
    }

    public List<WGZCertificateData> getWGZCertificateData() {
        return this.wgzCertificateProvider.getWGZCertificateData();
    }
}
