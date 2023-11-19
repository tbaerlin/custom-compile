/*
 * FundDataProviderImpl.java
 *
 * Created on 11.08.2006 18:34:41
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import java.io.File;

import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.EdgData;
import de.marketmaker.istar.domainimpl.data.NullEdgData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class EdgDataProviderImpl extends AbstractUpdatableDataProvider<EdgData> implements EdgDataProvider {

    @Override
    protected void read(File file) throws Exception {
        new EdgDataReader(this).read(file);
    }

    public EdgData getEdgData(long instrumentid) {
        final EdgData data = getData(instrumentid);
        return data != null ? data : NullEdgData.INSTANCE;
    }

    public static void main(String[] args) throws Exception {
        final EdgDataProviderImpl p = new EdgDataProviderImpl();
        p.setFile(new File(LocalConfigProvider.getProductionBaseDir(), "var/data/provider/istar-edg-ratings.xml.gz"));
        p.afterPropertiesSet();
        System.out.println(p.getEdgData(10922L));
    }
}