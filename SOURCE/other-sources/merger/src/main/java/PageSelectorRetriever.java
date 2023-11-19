/*
 * PageSelectorRetriever.java
 *
 * Created on 07.01.2009 10:41:23
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

import java.io.File;
import java.util.*;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class PageSelectorRetriever {
    public static void main(String[] args) throws Exception {
        new PageSelectorRetriever().process();
    }

    private void process() throws Exception {
//        final Set<String> pageids = getPageids();
//        final Map<String, String> vwdcodeToVendorkey = readKeys();
//
//        final BasicDataSource ds = new BasicDataSource();
//        ds.setDriverClassName("com.mysql.jdbc.Driver");
//        ds.setUrl("jdbc:mysql://inchi7/fusion");
//        ds.setUsername("chicago");
//        ds.setPassword("chicago");
//
//        final EntitlementProviderVwd ep = new EntitlementProviderVwd();
//        ep.setEntitlementFieldGroups(new File(LocalConfigProvider.getIstarSrcDir(), "feed/src/conf/EntitlementFieldGroups.txt"));
//        ep.setEntitlementRules(new File(LocalConfigProvider.getIstarSrcDir(), "feed/src/conf/EntitlementRules.XFeed.txt"));
//        ep.afterPropertiesSet();
//
//
//        final Set<String> selectors = new HashSet<String>();
//
//        final PageDbDao dao = new PageDbDao();
//        dao.setDataSource(ds);
//        dao.afterPropertiesSet();
//        for (final String pageid : pageids) {
//            if (!StringUtils.hasText(pageid)) {
//                continue;
//            }
//            final PageData data = dao.getPageData(Integer.parseInt(pageid));
//            if (data == null) {
//                continue;
//            }
//            final List<String> keys = data.getKeys();
//            if (keys == null) {
//                continue;
//            }
//            for (final String key : keys) {
//                final String vendorkey = vwdcodeToVendorkey.get(key);
//                if (vendorkey == null) {
//                    continue;
//                }
//                final int[] ints = ep.getEntitlements(vendorkey);
//                for (final int anInt : ints) {
//                    selectors.add(EntitlementsVwd.normalize(Integer.toString(anInt)));
//                }
//            }
//        }
//
//        System.out.println(selectors);
    }

    private Map<String, String> readKeys() throws Exception {
        final Map<String, String> keys = new HashMap<>(4 * 1000 * 1000);
        final Scanner s = new Scanner(new File("d:/temp/freischaltungen/vendorkeys.txt"));
        while (s.hasNextLine()) {
            final String vendorkey = s.nextLine();
            keys.put(vendorkey.substring(vendorkey.indexOf(".") + 1), vendorkey);
        }
        s.close();
        return keys;
    }

    private Set<String> getPageids() throws Exception {
        final Set<String> pageids = new HashSet<>();
        final Scanner s = new Scanner(new File("d:/temp/freischaltungen/baylaba-pages.txt"));
        while (s.hasNextLine()) {
            final String line = s.nextLine();
            if (line.startsWith("#")) {
                continue;
            }
            final String[] elements = line.split(",");
            for (final String tokens : elements) {
                final String[] rangeTokens = tokens.split("-");
                if (rangeTokens.length == 1) {
                    pageids.add(rangeTokens[0]);
                }
                else {
                    final int start = Integer.parseInt(rangeTokens[0]);
                    final int end = Integer.parseInt(rangeTokens[1]);
                    for (int i = start; i <= end; i++) {
                        pageids.add(Integer.toString(i));
                    }
                }
            }
        }
        s.close();
        return pageids;
    }
}
