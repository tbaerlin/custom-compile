/*
 * DpaAfxImporter.java
 *
 * Created on 20.03.12 11:18
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.analyses.backend;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Collections;
import java.util.Set;

import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.feed.vwd.VwdFieldDescription;
import de.marketmaker.istar.instrument.export.InstrumentDirDao;
import de.marketmaker.istar.news.backend.ObjectAdapter;
import de.marketmaker.istar.news.data.NewsRecordImpl;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;

/**
 * converting incoming feed data into protobuf content
 *
 * @author oflege
 */
public class DpaAfxImporter extends AnalysesNewsHandler {

    private static final String HEADLINE_PREFIX = "dpa-AFX: ";


    public DpaAfxImporter(AnalysesProviderDpaAfx provider) {
        super(Selector.DPA_AFX_ANALYSES, provider);
    }

    // see: T-45092,  T-46665 for a list of fields
    // this maps the feed-fields to protobuf-fields
    @Override
    protected void addProviderSpecificFields(NewsRecordImpl nr, Protos.Analysis.Builder builder) {
        // fields values for symbol and iid are added before calling this method

        // previous
        addFields(nr, builder, VwdFieldDescription.NDB_Country, "country");
        addField(nr, builder, VwdFieldDescription.NDB_CompanyName, "company_name");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Study_Date, "study_date", ObjectAdapter.LONG_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Ticker, "ticker");
        addField(nr, builder, VwdFieldDescription.NDB_Industry, "industry");
        addFields(nr, builder, VwdFieldDescription.NDB_Analyst, "analyst_name");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Institute_Name, "source");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Institute_Symbol, "institute_symbol");
        // normalizing the ratings
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recommendation, "rating", ObjectAdapter.DPA_AFX_RATING_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Recommendation, "previous_rating", ObjectAdapter.DPA_AFX_RATING_ADAPTER);

        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Price_Target, "target", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Price_Target, "previous_target", ObjectAdapter.PRICE_ADAPTER);
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Price_Currency, "currency");
        addField(nr, builder, VwdFieldDescription.NDB_RatingID, "rating_id");

        addFields(nr, builder, VwdFieldDescription.NDB_Branch, "branch");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recom_Source, "source_type");

        addFields(nr, builder, VwdFieldDescription.NDB_Analyst_Institute_ISIN, "institute_isin");
        addField(nr, builder, VwdFieldDescription.NDB_Time_Frame, "timeframe", ObjectAdapter.TIMEFRAME_ADAPTER);

        // store the raw rating values
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Recommendation, "analyst_recomm");
        addField(nr, builder, VwdFieldDescription.NDB_Analyst_Prev_Recommendation, "analyst_prev_recomm");

        // fallback for company name
        if (!builder.hasCompanyName() && nr.getInstruments().size() == 1) {
            builder.setCompanyName(nr.getInstruments().iterator().next().getName());
        }
    }

    @Override
    protected String getHeadline(String headline) {
        return (headline.startsWith(HEADLINE_PREFIX))
                ? headline.substring(HEADLINE_PREFIX.length())
                : headline;
    }

    /**
     * Allows to import news dump files to re-index missed news.
     * <dl>
     *     <dt>Select news ids from news-index</dt>
     *     <dd><tt>java -cp 'lib/*' NewsCli ~/produktion/var/data/news/historicIndex/ query '+selector:3082 + timestamp:[2012-05-11 TO 2012-05-12]' -o ~/tmp/ids.txt 200000</tt></dd>
     *     <dt>Serialize news to file</dt>
     *     <dd><tt>java -cp 'lib/*' NewsViewer -i ids.txt -o analyses.obj</tt></dd>
     *     <dt>Import news into target db</dt>
     *     <dd><tt>java -cp 'lib/*' de.marketmaker.istar.analyses.backend.DpaAfxImporter analyses.obj</tt></dd>
     * </dl>
     * Once the news have been imported, istar-anayses needs to be restarted.
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        InstrumentDirDao iDao = null;
        String host = "localhost";
        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-i".equals(args[n])) {
                iDao = new InstrumentDirDao(new File(args[++n]));
            }
            if ("-h".equals(args[n])) {
                host = args[++n];
            }
            n++;
        }
        File inFile = new File(args[n]);

        AnalysesDaoDb dao = new AnalysesDaoDb();
        final SingleConnectionDataSource ds = new SingleConnectionDataSource(
                "jdbc:mysql://" + host + "/analyses", "analysesadm", "analysesadm", true);
        ds.setDriverClassName("com.mysql.jdbc.Driver");
        dao.setDataSource(ds);
        dao.afterPropertiesSet();
        AnalysesProviderDpaAfx provider = new AnalysesProviderDpaAfx();

        DpaAfxImporter importer = new DpaAfxImporter(provider);

        ObjectInputStream ois
                = new ObjectInputStream(new FileInputStream(inFile));
        int numRead = 0;
        int numInserted = 0;
        try {
            while (true) {
                NewsRecordImpl nr = (NewsRecordImpl) ois.readObject();
                numRead++;
                final Set<String> iids = nr.getAttributes(NewsAttributeEnum.IID);
                if (iids.size() != 1) {
                    continue;
                }

                if (iDao != null) {
                    Instrument instrument = iDao.getInstrument(Long.parseLong(iids.iterator().next()));
                    if (instrument == null) {
                        continue;
                    }
                    nr.setInstruments(Collections.singleton(instrument));
                }

                Protos.Analysis.Builder builder = importer.buildAnalysis(nr);
                provider.addId(builder);
                if (dao.insertAnalysis(builder.build())) {
                    numInserted++;
                }
            }
        } catch (EOFException e) {
            // ignore
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            ois.close();
        }
        System.out.printf("Read %d, inserted %d%n", numRead, numInserted);
    }
}
