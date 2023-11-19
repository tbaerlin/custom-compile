/*
 * MerDataProviderImpl.java
 *
 * Created on 26.09.2008 07:36:26
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.mer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.xml.sax.SAXException;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.TimeTaker;
import de.marketmaker.istar.common.xml.AbstractSaxReader;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.instrument.Quote;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.feed.vwd.EntitlementProviderVwd;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProvider;
import de.marketmaker.istar.merger.provider.EntitlementQuoteProviderImpl;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class MerDataProviderImpl implements InitializingBean, MerDataProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private ActiveMonitor activeMonitor;
    private EntitlementQuoteProvider entitlementQuoteProvider;
    private File file;

    private final AtomicReference<List<MerItem>> items = new AtomicReference<>();

    public void setFile(File file) {
        this.file = file;
    }

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setEntitlementQuoteProvider(EntitlementQuoteProvider entitlementQuoteProvider) {
        this.entitlementQuoteProvider = entitlementQuoteProvider;
    }

    public void afterPropertiesSet() throws Exception {
        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                readItems();
            }
        });
        this.activeMonitor.addResource(resource);

        readItems();
    }

    private void readItems() {
        try {
            final TimeTaker tt = new TimeTaker();

            final ItemsReader ur = new ItemsReader(this.entitlementQuoteProvider);
            ur.read(this.file);

            this.items.set(ur.getItems());
            this.logger.info("<readItems> read items, took " + tt);
        }
        catch (Exception e) {
            this.logger.error("<readItems> failed", e);
        }
    }

    public Map<String, List<String>> getMetadata(Profile profile) {
        final Set<String> countries = new HashSet<>();
        final Set<String> types = new HashSet<>();

        for (final MerItem item : this.items.get()) {
            if (profile.getPriceQuality(item.getQuote()) != PriceQuality.NONE) {
                countries.add(item.getCountry());
                types.add(item.getType());
            }
        }

        final Map<String, List<String>> result = new HashMap<>();
        final List<String> sc = new ArrayList<>(countries);
        final List<String> st = new ArrayList<>(types);
        sc.sort(null);
        st.sort(null);
        result.put("country", sc);
        result.put("type", st);
        return result;
    }

    public List<MerItem> getItems(Profile profile, List<String> type, List<String> country) {
        final List<MerItem> items = new ArrayList<>();

        final HashSet<String> ts = new HashSet<>(type);
        final HashSet<String> tc = new HashSet<>(country);

        for (final MerItem item : this.items.get()) {
            if (!ts.isEmpty() && !ts.contains(item.getType())) {
                continue;
            }
            if (!tc.isEmpty() && !tc.contains(item.getCountry())) {
                continue;
            }

            if (profile.getPriceQuality(item.getQuote()) != PriceQuality.NONE) {
                items.add(item);
            }
        }

        return items;
    }

    private static class ItemsReader extends AbstractSaxReader {
        private final EntitlementQuoteProvider entitlementQuoteProvider;
        private final List<MerItem> items = new ArrayList<>();

        private long qid = -1;
        private String vwdsymbol;
        private String type;
        private String country;

        private ItemsReader(EntitlementQuoteProvider entitlementQuoteProvider) {
            this.entitlementQuoteProvider = entitlementQuoteProvider;
        }

        public void endElement(String uri, String localName, String tagName) throws SAXException {
            try {
                if (tagName.equals("ROW")) {
                    // i have finished a new row => process
                    storeFields();
                }
                else if (tagName.equals("QID")) {
                    this.qid = getCurrentLong();
                }
                else if (tagName.equals("VWDSYMBOL")) {
                    this.vwdsymbol = getCurrentString();
                }
                else if (tagName.equals("COUNTRY")) {
                    this.country = getCurrentString();
                }
                else if (tagName.equals("TYPE")) {
                    this.type = getCurrentString();
                }
                else if (tagName.equals("ROWS")) {
                    // ignore
                }
                else {
                    notParsed(tagName);
                }
            }
            catch (Exception e) {
                this.logger.error("<endElement> error in " + tagName, e);
                this.errorOccured = true;
            }
        }

        private void storeFields() {
            this.limiter.ackAction();

            if (this.errorOccured) {
                reset();
                return;
            }

            if (this.qid < 0 || this.country == null || this.type == null) {
                reset();
                return;
            }

            final Quote quote = this.entitlementQuoteProvider.getQuotes(Arrays.asList(this.vwdsymbol)).get(0);

            this.items.add(new MerItem(qid, quote, country, type));

            reset();
        }

        protected void reset() {
            this.qid = -1;
            this.type = null;
            this.country = null;
            this.errorOccured = false;
        }

        public List<MerItem> getItems() {
            return items;
        }
    }

    public static void main(String[] args) throws Exception {
        final MerDataProviderImpl p = new MerDataProviderImpl();
        p.setActiveMonitor(new ActiveMonitor());
        final EntitlementQuoteProviderImpl eqp = new EntitlementQuoteProviderImpl();
        final EntitlementProviderVwd ep = new EntitlementProviderVwd();
        ep.setEntitlementFieldGroups(new File(LocalConfigProvider.getIstarSrcDir(), "feed/src/conf/EntitlementFieldGroups.txt"));
        ep.setEntitlementRules(new File(LocalConfigProvider.getIstarSrcDir(), "feed/src/conf/EntitlementRules.XFeed.txt"));
        ep.afterPropertiesSet();
        eqp.setEntitlementProvider(ep);
        p.setEntitlementQuoteProvider(eqp);
        p.setFile(new File("d:/produktion/var/data/provider/istar-mer-items.xml.gz"));
        p.afterPropertiesSet();

        final Profile profile = ProfileFactory.valueOf(true);

        final Map<String, List<String>> metadata = p.getMetadata(profile);
        System.out.println(metadata.get("country"));
        System.out.println(metadata.get("type"));

//        final List<MerItem> items = p.getItems(profile, Collections.<String>emptyList(), Collections.<String>emptyList());
        final List<MerItem> items = p.getItems(profile, Arrays.asList("BIP"), Arrays.asList("Deutschland"));
        for (final MerItem item : items) {
            System.out.println(item.getCountry() + "/" + item.getType() + "/" + item.getQid());
        }
    }
}
