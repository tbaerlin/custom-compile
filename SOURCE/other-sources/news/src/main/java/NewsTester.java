/*
 * NewsTester.java
 *
 * Created on 23.08.2010 13:45:03
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.Version;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.domain.profile.PermissionType;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.DefaultProfile;
import de.marketmaker.istar.domainimpl.profile.SimplePermissionProvider;
import de.marketmaker.istar.news.backend.NewsQuerySupport;
import de.marketmaker.istar.news.frontend.NewsAttributeEnum;
import de.marketmaker.istar.news.frontend.NewsRecord;
import de.marketmaker.istar.news.frontend.NewsRequest;
import de.marketmaker.istar.news.frontend.NewsResponse;
import de.marketmaker.istar.news.frontend.NewsServer;

/**
 * @author oflege
 */
public class NewsTester implements InitializingBean, ApplicationContextAware {

    private NewsServer newsServer;

    private File profileDir;

    private File queryFile;

    private File outFile;

    private List<Profile> profiles = new ArrayList<>();

    private final int[] selectors = new int[] {
      1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27,
            28, 29, 30, 31, 32, 33, 34, 35, 36, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 50, 53,
            54, 55, 56, 57, 58, 59, 60, 62, 63, 66, 67, 68, 71, 72, 73, 76, 78, 81, 82, 83, 84, 85,
            86, 87, 88, 89, 90, 91, 92, 94, 98, 99, 100, 105, 106, 107, 108, 109, 110, 111, 112, 113,
            115, 116, 117, 118, 119, 124, 125, 126, 127, 130, 131, 132, 133, 134, 135, 136, 143, 144,
            145, 148, 153, 205, 222, 223, 368, 373, 374, 378, 382, 384, 386, 469, 470, 471, 472, 473,
            474, 477, 479, 483, 485, 486, 487, 488, 489, 491, 492, 493, 494, 502, 2158, 3002, 3003,
            3004, 3009, 3050, 3052, 3053, 3054, 3055, 3056, 3057, 3058, 3071, 3072, 3073, 3074, 3075,
            3076, 3078, 3079, 3080, 3081, 3082, 3098
    };

    private List<Query> queries = new ArrayList<>();

    private ApplicationContext applicationContext;

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public void setNewsServer(NewsServer newsServer) {
        this.newsServer = newsServer;
    }

    public void setProfileDir(File profileDir) {
        this.profileDir = profileDir;
    }

    public void setQueryFile(File queryFile) {
        this.queryFile = queryFile;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public void afterPropertiesSet() throws Exception {
        for (final int selector : selectors) {
            final String ent = Integer.toString(selector);
            this.profiles.add(new DefaultProfile(new SimplePermissionProvider(ent,
                    PermissionType.NEWS_REALTIME, Collections.singleton(ent))));
        }

        QueryParser qp = new QueryParser(Version.LUCENE_30, null, NewsQuerySupport.createAnalyzer());

        Scanner sc = new Scanner(this.queryFile, "utf8");
        while (sc.hasNextLine()) {
            queries.add(qp.parse(sc.nextLine()));
        }
        sc.close();

        doRun(null);

/*
        for (int i = 0; i < 100; i++) {
            final StringWriter sw = new StringWriter(65536);
            final PrintWriter pw = new PrintWriter(sw);
            doRun(pw);
            pw.close();
            FileCopyUtils.copy(sw.toString().getBytes("utf8"), this.outFile);
        }
*/

        ((ConfigurableApplicationContext)this.applicationContext).close();
    }

    private void doRun(PrintWriter pw) {
        final long then = System.currentTimeMillis();

        int n = 0;
        for (Profile profile : profiles) {
            for (Query query : queries) {
                final String s = profile.getName();
                final NewsResponse response = getNews(profile, query);
                for (NewsRecord record : response.getRecords()) {
                    final Set<String> tmp = record.getAttributes().get(NewsAttributeEnum.SELECTOR);
                    final Set<String> newsSelectors = new HashSet<>();
                    for (String s1 : tmp) {
                        newsSelectors.add(EntitlementsVwd.normalize(s1));
                    }
                    if (!newsSelectors.contains(s)) {
                        System.err.println(s + " NOT in " + newsSelectors + " for " + record.toString());
                    }
                }
                if (pw != null) {
                    pw.printf("%6d %6s %6d %10d %s%n", ++n, profile.getName(), response.getHitCount(),
                            (System.currentTimeMillis() - then), query.toString());
                }
            }
        }
    }

    private NewsResponse getNews(Profile profile, Query query) {
        NewsRequest request = new NewsRequest();
        request.setLuceneQuery(query);
        request.setWithHitCount(true);
        request.setCount(10);
        request.setOffset(0);
        request.setProfile(profile);
        request.setWithText(true);

        return this.newsServer.getNews(request);
    }
}
