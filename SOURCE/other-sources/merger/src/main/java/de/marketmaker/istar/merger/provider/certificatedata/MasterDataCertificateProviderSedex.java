/*
 * MasterDataCertificateProviderSedex.java
 *
 * Created on 6/26/14 9:57 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;
import java.math.BigDecimal;

import org.joda.time.LocalDate;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.MasterDataCertificate;
import de.marketmaker.istar.domain.data.NullMasterDataCertificate;
import de.marketmaker.istar.domainimpl.data.MasterDataCertificateImpl;
import de.marketmaker.istar.merger.provider.LocalDateCache;
import de.marketmaker.istar.merger.provider.MasterDataProvider;

/**
 * @author Stefan Willenbrock
 */
public class MasterDataCertificateProviderSedex extends AbstractUpdatableDataProvider<MasterDataCertificate>
        implements MasterDataProvider<MasterDataCertificate> {

    private final LocalDateCache localDateCache = new LocalDateCache();

    @Override
    public MasterDataCertificate getMasterData(long instrumentid) {
        final MasterDataCertificate data = getData(instrumentid);
        return data != null ? data : NullMasterDataCertificate.INSTANCE;
    }

    @Override
    protected void read(File file) throws Exception {
        new IstarMdpExportReader<Void>("ADF_PRODUKTART_HEBELPRODUKTE", "ADF_PRODUKTART_INVESTMENTPRODU") {

            @Override
            protected void handleRow() {
                final Long instrumentid = getLong("IID");
                if (instrumentid == null) {
                    return;
                }

                final String issuerName = get("ADF_EMITTENT");
                final String issuerCountryCode = get("ADF_LAND");
                final LocalDate issueDate = getDateMonthYear("ADF_EMISSIONS_TAG");
                final LocalDate firstTradingDay = getDateMonthYear("ADF_ERSTER_BOERSENHANDELSTAG");
                final LocalDate lastTradingDay = getDateMonthYear("ADF_LETZTER_HANDELSTAG");

                final BigDecimal strikePrice = getBigDecimal("ADF_BASISPREIS");
                final BigDecimal subscriptionRatio = getBigDecimal("ADF_BEZUGSVERHAELTNIS");
                final BigDecimal cap = getBigDecimal("ADF_CAP");
                final String leverageType = get("ADF_PRODUKTART_HEBELPRODUKTE");
                final String guaranteeType = get("ADF_PRODUKTART_INVESTMENTPRODU");
                final LocalDate settlementDay = getDateMonthYear("ADF_FAELLIGKEITS_DATUM");

                final MasterDataCertificate data = new MasterDataCertificateImpl(instrumentid,
                        null, null, null, null,
                        null, null, null, null,
                        null, null, null,
                        cap, null, null,
                        null, null, null, null,
                        null, null, null,
                        firstTradingDay, lastTradingDay,
                        null,
                        null, null, null,
                        issueDate, issuerName, issuerCountryCode, null, null,
                        null, null, null,
                        null, null,
                        null, null, null, null,
                        guaranteeType, leverageType, null,
                        strikePrice, null, null,
                        null, null, null, null,
                        null,
                        null, null, null, null,
                        null, null,
                        null, null, null,
                        null, null,
                        subscriptionRatio, null, null,
                        null,
                        settlementDay);

                addOrReplace(instrumentid, data);
            }

            private LocalDate getDateMonthYear(String name) {
                return localDateCache.getDate(get(name));
            }

            @Override
            protected Void getResult() {
                return null;
            }
        }.read(file);
    }
}
