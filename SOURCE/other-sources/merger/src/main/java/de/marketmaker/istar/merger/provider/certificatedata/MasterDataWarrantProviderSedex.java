/*
 * MasterDataWarrantProviderSedex.java
 *
 * Created on 6/26/14 10:16 AM
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.data.MasterDataWarrant;
import de.marketmaker.istar.domain.data.NullMasterDataWarrant;
import de.marketmaker.istar.domainimpl.data.MasterDataWarrantImpl;
import de.marketmaker.istar.merger.provider.LocalDateCache;
import de.marketmaker.istar.merger.provider.MasterDataProvider;
import org.joda.time.LocalDate;
import org.joda.time.YearMonthDay;

import java.io.File;

/**
 * @author Stefan Willenbrock
 */
public class MasterDataWarrantProviderSedex extends AbstractUpdatableDataProvider<MasterDataWarrant>
        implements MasterDataProvider<MasterDataWarrant> {

    private final LocalDateCache localDateCache = new LocalDateCache();

    @Override
    public MasterDataWarrant getMasterData(long instrumentid) {
        final MasterDataWarrant data = getData(instrumentid);
        return data != null ? data : NullMasterDataWarrant.INSTANCE;
    }

    @Override
    protected void read(File file) throws Exception {
        new IstarMdpExportReader<Void>() {

            @Override
            protected void handleRow() {
                final Long instrumentid = getLong("IID");
                if (instrumentid == null) {
                    return;
                }

                final String issuerName = get("ADF_EMITTENT");
                final LocalDate issueDate = getDateMonthYear("ADF_EMISSIONS_TAG");
                final LocalDate firstTradingDay = getDateMonthYear("ADF_ERSTER_BOERSENHANDELSTAG");
                final LocalDate lastTradingDay = getDateMonthYear("ADF_LETZTER_HANDELSTAG");

                final MasterDataWarrant data = new MasterDataWarrantImpl(instrumentid, null, issueDate == null ? null : new YearMonthDay(issueDate), issuerName,
                        null, null, firstTradingDay, lastTradingDay);

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
