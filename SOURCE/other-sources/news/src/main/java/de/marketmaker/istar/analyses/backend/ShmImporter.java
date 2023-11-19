/*
 * DpaAfxImporter.java
 *
 * Created on 20.03.12 11:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.news.backend.ObjectAdapter;
import de.marketmaker.istar.news.data.NewsRecordImpl;

/**
 * @author oflege
 */
public class ShmImporter extends AnalysesNewsHandler {

    public ShmImporter(AnalysesProviderShm provider) {
        super(Selector.SHM_ANALYSES, provider);
    }

    @Override
    protected void addProviderSpecificFields(NewsRecordImpl nr, Protos.Analysis.Builder builder) {
        addField(nr, builder, VwdFieldDescription.NDB_Analyst, "source");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recommendation, "rating", ObjectAdapter.SHM_RATING_ADAPTER);
    }

    public static void main(String[] args) throws Exception {
        final SingleConnectionDataSource ds = new SingleConnectionDataSource(
                "jdbc:mysql://teprovider1/analyses", "analysesadm", "analysesadm", true);
        ds.setDriverClassName("com.mysql.jdbc.Driver");

        final AnalysesDaoDb dao = new AnalysesDaoDb();
        dao.setDataSource(ds);
        dao.afterPropertiesSet();

        final AnalysesProviderShm provider = new AnalysesProviderShm();
        provider.setDao(dao);

        final ShmImporter importer = new ShmImporter(provider);
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(new File("d:/temp/shm.obj")))) {
            for (int i = 0; i < 21858; i++) {
                NewsRecordImpl nr = (NewsRecordImpl) ois.readObject();

                Protos.Analysis.Builder builder = importer.buildAnalysis(nr);
                provider.addId(builder);
                dao.insertAnalysis(builder.build());
//                System.out.println(builder.getId());
            }
        }
    }

}
