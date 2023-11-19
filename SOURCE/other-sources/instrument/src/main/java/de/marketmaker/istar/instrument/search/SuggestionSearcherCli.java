/*
 * SuggestionSearcherCli.java
 *
 * Created on 21.02.12 08:47
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.instrument.search;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import de.marketmaker.istar.domain.data.SuggestedInstrument;
import de.marketmaker.istar.domain.instrument.InstrumentTypeEnum;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.data.SuggestedInstrumentImpl;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.instrument.IndexConstants;

/**
 * Tool to query data in a suggest-index
 * @author oflege
 */
public class SuggestionSearcherCli {
    public static void usage() {
        System.err.println("Usage: SuggestionSearcherCli [options] baseDir");
        System.err.println("options:");
        System.err.println("-s strategy(,strategy)* : strategy/ies to use");
        System.err.println("-p profile : resource profile name or 'all' to allow everything");
        System.err.println("-l limit   : number of expected results for each query");
        System.err.println("-start term : start term for querying, default is 'a'");
        System.err.println("-end tem    : end term for querying, default is 'aa'");
        System.err.println("-q  : search results will not be printed, for performance tests");
        System.err.println("-wm : use WM_WP_NAME_KURZ for name matching");
        System.err.println();
        System.err.println("baseDir has to contain data/ and index-suggest/ subdirs");
        System.err.println();
        System.err.println("The program will query the suggest-index for all strategies");
        System.err.println("and for all terms between start and end");
    }

    public static void main(String[] args) throws Exception {
        String query = "a";
        String endQuery = "aa";
        String profileName = "lbbw-test";
        String[] strategy = new String[]{"de"};
        String nameField = IndexConstants.FIELDNAME_NAME;
        InstrumentTypeEnum type = null;
        int limit = 10;
        boolean quiet = false;

        int n = 0;
        while (n < args.length && args[n].startsWith("-")) {
            if ("-s".equals(args[n])) {
                strategy = args[++n].split(",");
            }
            else if ("-p".equals(args[n])) {
                profileName = args[++n];
            }
            else if ("-l".equals(args[n])) {
                limit = Integer.parseInt(args[++n]);
            }
            else if ("-start".equals(args[n])) {
                query = args[++n];
            }
            else if ("-end".equals(args[n])) {
                endQuery = args[++n];
            }
            else if ("-q".equals(args[n])) {
                quiet = true;
            }
            else if ("-wm".equals(args[n])) {
                nameField = IndexConstants.FIELDNAME_WM_WP_NAME_KURZ;
            }
            else if ("-nf".equals(args[n])) {
                nameField = IndexConstants.FIELDNAME_NAME_FREE;
            }
            else if ("-nc".equals(args[n])) {
                nameField = IndexConstants.FIELDNAME_NAME_COST;
            }
            else if ("-t".equals(args[n])) {
                type = InstrumentTypeEnum.valueOf(args[++n]);
            }
            n++;
        }


        Profile profile = ("all".equals(profileName)) ? ProfileFactory.valueOf(true)
                : ProfileFactory.createInstance(ResourcePermissionProvider.getInstance(profileName));

        int numQueries = 0;
        long then = System.nanoTime();

        String[][] results = new String[limit][strategy.length];
        StringBuilder sb = new StringBuilder("  %-30s");
        for (int i = 1; i < strategy.length; i++) sb.append(" %-30s");
        String fmt = sb.append("%n").toString();

        SuggestionSearcherImpl ss = SuggestionSearcherImpl.create(new File(args[n]), null);

        do {
            int maxResults = 0;
            for (int i = 0; i < strategy.length; i++) {
                numQueries++;
                SuggestRequest request = new SuggestRequest(profile, nameField);
                request.setQuery(query);
                request.setStrategy(strategy[i]);
                request.setLimit(limit);
                request.setType(type);
                List<SuggestedInstrument> suggestions = ss.query(request);
                if (!quiet) {
                    for (int k = 0; k < limit; k++) {
                        if (k < suggestions.size()) {
                            SuggestedInstrumentImpl si = (SuggestedInstrumentImpl) suggestions.get(k);
                            results[k][i] = String.format("%5d|%-30s|%s|%s|%s", si.getOrder(), si.getName(),
                                    si.getSymbolIsin(), si.getSymbolWkn(), si.getInstrumentType().name());
                        } else {
                            results[k][i] = "";
                        }
                    }
                }
                maxResults = Math.max(maxResults, suggestions.size());
            }
            if (maxResults > 0 && !quiet) {
                System.out.println(query);
                if (strategy.length > 1) {
                    System.out.printf(fmt, strategy);
                }
                for (int i = 0; i < maxResults; i++) {
                    System.out.printf(fmt, results[i]);
                }
            }
            query = next(query);
        } while (!endQuery.equals(query));

        long now = System.nanoTime();

        ss.close();

        System.out.println("numQueries = " + numQueries);
        System.out.println("took " + (now - then) + " ns, avg: " + ((now - then) / numQueries) + " ns/q");
    }

    static String next(String s) {
        char[] ca = s.toCharArray();
        for (int i = ca.length; i-- > 0; ) {
            if (ca[i] < 'z') {
                ca[i]++;
                for (int j = i + 1; j < ca.length; j++) {
                    ca[j] = 'a';
                }
                return new String(ca);
            }
        }
        final char[] as = new char[ca.length + 1];
        Arrays.fill(as, 'a');
        return new String(as);
    }
}
