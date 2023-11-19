/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;
import java.math.BigDecimal;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.data.NullMasterDataCertificate;
import de.marketmaker.istar.domainimpl.data.MasterDataCertificateImpl;
import de.marketmaker.istar.merger.provider.MasterDataProvider;

import static de.marketmaker.istar.common.util.LocalConfigProvider.getProductionDir;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MasterDataCertificateProviderSmf extends AbstractUpdatableDataProvider<MasterDataCertificate>
        implements MasterDataProvider<MasterDataCertificate> {

    @Override
    protected void read(File file) throws Exception {
        new IstarMdpExportReader<Void>("ADF_SVSP_NAME_PRODUKTTYP", "ADF_EMITTENTEN_PRODUKTNAME",
                "ADF_STRATEGIE", "ADF_BASISWERT") {

            @Override
            protected void handleRow() {
                final Long instrumentid = getLong("IID");
                if (instrumentid == null) {
                    return;
                }

                String type = get("ADF_SVSP_NAME_PRODUKTTYP");
                String leveragetype = get("ADF_STRATEGIE");
                String productNameIssuer = get("ADF_EMITTENTEN_PRODUKTNAME");
                String multiassetName = get("ADF_BASISWERT");

                boolean quanto = "Y".equals(get("ADF_QUANTO"));

                BigDecimal coupon = getBigDecimal("ADF_ZINSSATZ");
                BigDecimal participationLevel = getBigDecimal("ADF_PART_RATE");
                BigDecimal subscriptionRatio = getBigDecimal("ADF_BEZUGSVERHAELTNIS");


                final MasterDataCertificate data = new MasterDataCertificateImpl(instrumentid,
                        null, LocalizedString.createDefault(type), null, type,
                        null, null, null, null,
                        null, null, null,
                        null, null, null, null, null, null, null, coupon, null, null, null, null, null,
                        null, null, null, null, null, null, null,
                        null, null, null, null, null, null, null, quanto, null, null, null,
                        leveragetype, null, null, null, null, null, null,
                        productNameIssuer, type, null, null, null, null,
                        null, null, null, null, null, null, null,
                        participationLevel, subscriptionRatio, multiassetName, null, null, null);

                addOrReplace(instrumentid, data);
            }

            @Override
            protected Void getResult() {
                return null;
            }
        }.read(file);
    }

    public MasterDataCertificate getMasterData(long instrumentid) {
        final MasterDataCertificate data = getData(instrumentid);
        return data != null ? data : NullMasterDataCertificate.INSTANCE;
    }

    public static void main(String[] args) throws Exception {
        final MasterDataCertificateProviderSmf p = new MasterDataCertificateProviderSmf();
        p.setFile(new File(getProductionDir("var/data/provider"), "istar-smf-cer-wnt-masterdata.xml.gz"));
        p.afterPropertiesSet();
        System.out.println(p.getMasterData(581719L));
    }
}