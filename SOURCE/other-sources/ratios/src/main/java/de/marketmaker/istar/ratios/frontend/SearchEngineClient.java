/*
 * SearchEngineClient.java
 *
 * Created on 27.10.2005 17:35:07
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.ratios.frontend;

import de.marketmaker.istar.common.rmi.RmiProxyFactory;
import de.marketmaker.istar.common.rmi.RmiServiceDescriptor;
import de.marketmaker.istar.common.rmi.RmiServiceDescriptorEditor;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.domain.data.RatioDataRecord;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.ratios.RatioFieldDescription;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SearchEngineClient {
    public static void main(String[] args) throws Exception {
        final IstarSearchEngineConnector connector = new IstarSearchEngineConnector();

        final RmiServiceDescriptorEditor editor = new RmiServiceDescriptorEditor();

        editor.setAsText(args.length > 1 ? args[1] : "1,rmi://lap11:1499/searchengineserver@lap11");
        final RmiProxyFactory proxy = new RmiProxyFactory();
        proxy.setRmiServices((RmiServiceDescriptor[]) editor.getValue());
        proxy.setServiceInterface(RatioSearchEngine.class);
        proxy.afterPropertiesSet();

        final RatioSearchEngine searchEngine = (SearchEngine) proxy.getObject();
        connector.setRatioSearchEngine(searchEngine);

        final Profile profile = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance("easytrade"));

        doStandard(profile, args, connector);

        // doMeta(connector);

        doMinMaxAvg(profile, connector);
    }

    private static void doMinMaxAvg(Profile profile, IstarSearchEngineConnector connector) {
        final RatioSearchRequest request = new RatioSearchRequest(profile);
        request.setType(InstrumentTypeEnum.FND);
        request.setVisitorClass(MinMaxAvgVisitor.class);
        request.setParameters(getParameters(MinMaxAvgVisitor.KEY_GROUP_BY + "=sector&" + MinMaxAvgVisitor.KEY_SOURCE + "=bviperformance1y,lastprice"));

        final MinMaxAvgRatioSearchResponse response = (MinMaxAvgRatioSearchResponse) connector.search(request);
        for (final Map.Entry<Integer, Map<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>>> perFieldEntry : response.getResult().entrySet()) {
            System.out.println("##### MinMaxAvg for " + RatioFieldDescription.getFieldById(perFieldEntry.getKey()).name());
            for (final Map.Entry<String, Map<String, MinMaxAvgRatioSearchResponse.MinMaxAvg>> entry : perFieldEntry.getValue().entrySet()) {
                final MinMaxAvgRatioSearchResponse.MinMaxAvg mma = (MinMaxAvgRatioSearchResponse.MinMaxAvg) entry.getValue();
                System.out.println(mma);
            }
        }
    }

//    private static void doMeta(SearchEngine searchEngine) throws RemoteException {
//        final RatioSearchMetaRequest ratioSearchMetaRequest = new RatioSearchMetaRequest();
//        ratioSearchMetaRequest.setType(InstrumentTypeEnum.STK);
//        final RatioSearchMetaResponse metaData = searchEngine.getMetaData(ratioSearchMetaRequest);
//        final Map<String, Integer> map = metaData.getEnumFields().get(RatioFieldDescription.ratingMoodys);
//        for (final String s : map.keySet()) {
//            if (s.startsWith("NR")) {
//                System.out.println(s);
//                if (s.indexOf('\0') >= 0) {
//                    System.out.println("ALARM");
//                }
//                for (int i = 0; i < s.length(); i++) {
//                    final char c = s.charAt(i);
//                    System.out.println((int) c);
//                }
//                System.out.println("");
//            }
//        }
//    }

    private static void doStandard(Profile profile, String[] args,
            IstarSearchEngineConnector connector) {
        final RatioSearchRequest request = new RatioSearchRequest(profile);
        request.setType(InstrumentTypeEnum.FND);
//        request.setInstrumentIds(Arrays.asList(new Long[]{55169L}));
        request.setParameters(getParameters(args.length > 0
                ? args[0]
                : "isin@name=de0008474511"));

        final TimeTaker tt = new TimeTaker();
        final RatioSearchResponse searchResponse = connector.search(request);
        if (!searchResponse.isValid()) {
            System.out.println("invalid response");
            return;
        }
        final DefaultRatioSearchResponse response = (DefaultRatioSearchResponse) searchResponse;
        System.out.println("took: " + tt);

        System.out.println("#results: " + response.getElements().size());
        int count = 0;
        for (final RatioDataResult rdr : response.getElements()) {
            count++;
            if (count == 3) {
                System.out.println("...");
                continue;
            }
            if (count > 2 && count < response.getElements().size() - 2) {
                continue;
            }
            System.out.println("   instrument: " + rdr.getInstrumentRatios());
            System.out.println("   quote: " + rdr.getQuoteData());

            final RatioDataRecord record = new RatioDataRecordImpl(rdr, null);
            System.out.println("iid: " + record.getInstrumentId());
            System.out.println("qid: " + record.getQuoteId());
            System.out.println("isin: " + record.getIsin());
            System.out.println("name: " + record.getName());
            System.out.println("boersenplatz: " + record.getSymbolVwdfeedMarket());
            System.out.println("waehrung: " + record.getCurrencySymbolIso());
            System.out.println("datum: " + record.getPrice().getDate());
            System.out.println("kurs: " + record.getPrice());
            System.out.println("differenzAbsolut: " + record.getChangeNet());
            System.out.println("differenzRelativ: " + record.getChangePercent());
            System.out.println("differenzAbsolut1Jahr: " + record.getChangeNet1Year());
            System.out.println("differenzRelative1Jahr: " + record.getChangePercent1Year());
            System.out.println("bvi 1: " + record.getBVIPerformance1Year());
            System.out.println("bvi 3: " + record.getBVIPerformance3Years());

            for (final QuoteRatios qr : rdr.getQuotes()) {
                final RatioDataRecordImpl r = new RatioDataRecordImpl(rdr.getInstrumentRatios(), qr,
                        Collections.<RatioDataRecord.Field, RatioFieldDescription.Field>emptyMap(), null);
                System.out.println(r.getQuoteId() + ": " + r.getSymbolVwdfeedMarket());
            }
        }
    }

    private static Map<String, String> getParameters(String s) {
        final Map<String, String> result = new HashMap<>();
        final String[] constraints = s.split("&");
        for (final String constraint : constraints) {
            final String[] keyvalue = constraint.split("=");
            result.put(keyvalue[0], keyvalue[1]);
        }
        return result;
    }
}