/*
 * IndexCompositionProviderImpl.java
 *
 * Created on 26.04.2005 06:58:04
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.xml.IstarMdpExportReader;
import de.marketmaker.istar.domain.Language;
import de.marketmaker.istar.domain.data.IndexPosition;
import de.marketmaker.istar.domain.data.LocalizedString;
import de.marketmaker.istar.domain.data.NamedIdSet;
import de.marketmaker.istar.domain.instrument.Instrument;
import de.marketmaker.istar.domainimpl.data.IndexComposition;
import de.marketmaker.istar.domainimpl.data.IndexPositionImpl;
import de.marketmaker.istar.domainimpl.data.NamedIdSetImpl;
import de.marketmaker.istar.instrument.IndexCompositionProvider;
import de.marketmaker.istar.instrument.IndexCompositionRequest;
import de.marketmaker.istar.instrument.IndexCompositionResponse;
import de.marketmaker.istar.instrument.IndexMembershipRequest;
import de.marketmaker.istar.instrument.IndexMembershipResponse;
import de.marketmaker.istar.merger.provider.listoverview.ListOverviewProvider;
import de.marketmaker.istar.merger.provider.listoverview.ListOverviewProviderImpl;
import de.marketmaker.istar.merger.web.easytrade.block.EasytradeInstrumentProvider;
import de.marketmaker.iview.dmxml.ListOverviewListItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class IndexCompositionProviderImpl implements IndexCompositionProvider, InitializingBean {

    private static class Reader extends IstarMdpExportReader<Map<String, IndexComposition>> {

        final Map<Long, Set<Long>> map = new HashMap<>();

        final Map<Long, Set<Long>> mapByQuoteid = new HashMap<>();

        final Map<Long, Set<Long>> iidmapByQuoteid = new HashMap<>();

        final Map<String, List<IndexPosition>> mapByName = new HashMap<>();

        final Map<String, String> displaynameByName = new HashMap<>();

        final Map<String, IndexComposition> indexCompositions = new HashMap<>();

        final Map<Long, long[]> qidToIndexQids = new HashMap<>();

        private IndexComposition.Builder builder = null;

        private Reader() {
            super("ICGROUP", "NAME", "DISPLAYNAME");
        }

        private boolean isIndexCompositionUnsorted = false;

        @Override
        protected void handleRow() {
            Long indexQid = getLong("INDEXQUOTEID");
            String name = get("NAME");

            String key = (name != null) ? name : Long.toString(indexQid);
            if (builder == null || !builder.id.equals(key)) {
                buildCurrent();
                builder = IndexComposition.createBuilder(key, get("DISPLAYNAME"), get("ICGROUP"));
            }

            Long positionQuoteid = getLong("POSITIONQUOTEID");

            builder.addItem(positionQuoteid, getLong("IID"), getLocalized("ITEMNAME"));

            addIndexQidForPosition(indexQid, positionQuoteid);

            // TODO: remove after clients switched to new
            Long iid = getLong("IID");
            if (StringUtils.hasText(get("DISPLAYNAME")) && !displaynameByName.containsKey(name)) {
                displaynameByName.put(name, get("DISPLAYNAME"));
            }
            if (StringUtils.hasText(name)) {
                List<IndexPosition> positions = mapByName.get(name);
                if (positions == null) {
                    mapByName.put(name, positions = new ArrayList<>());
                }
                positions.add(new IndexPositionImpl(positionQuoteid, get("ITEMNAME")));
            }
            else {
                final Long indexInstrumentid = getLong("INDEXINSTRUMENTID");
                add(map, indexInstrumentid, positionQuoteid);
                add(mapByQuoteid, indexQid, positionQuoteid);
                add(iidmapByQuoteid, indexQid, iid);
            }
        }

        private LocalizedString getLocalized(String name) {
            LocalizedString localizedString = LocalizedString.createDefault(get(name));
            if (localizedString != null) {
                for (final Language language : Language.values()) {
                    final String localizedValue = get(name + "_" + language.name().toUpperCase());
                    if (StringUtils.hasText(localizedValue)) {
                        localizedString = localizedString.add(language, localizedValue);
                    }
                }
            }
            return localizedString;
        }

        private void addIndexQidForPosition(Long indexQid, Long positionQuoteid) {
            if (indexQid == null) {
                return;
            }
            final long[] qids = this.qidToIndexQids.get(positionQuoteid);
            if (qids == null) {
                this.qidToIndexQids.put(positionQuoteid, new long[] { indexQid });
            }
            else {
                long[] copy = Arrays.copyOf(qids, qids.length + 1);
                copy[copy.length - 1] = indexQid;
                this.qidToIndexQids.put(positionQuoteid, copy);
            }
        }

        @Override
        protected void endDocument() {
            buildCurrent();
        }

        private void buildCurrent() {
            if (builder != null) {
                IndexComposition ic = builder.build();
                if (!this.indexCompositions.containsKey(ic.getId())) {
                    this.indexCompositions.put(ic.getId(), ic);
                } else if (!this.isIndexCompositionUnsorted){
                    logger.error("<buildCurrent> index composition IDs not sorted, index composition incomplete");
                    this.isIndexCompositionUnsorted = true;
                }
            }
        }

        private Map<String, NamedIdSet> getNamedMap() {
            final Map<String, NamedIdSet> result = new HashMap<>();
            for (final Map.Entry<String, List<IndexPosition>> entry : mapByName.entrySet()) {
                final String displayname = displaynameByName.get(entry.getKey());
                final Set<Long> ids = new LinkedHashSet<>(entry.getValue().size());
                final Map<Long, String> namesByQuoteid = new HashMap<>();
                for (final IndexPosition position : entry.getValue()) {
                    ids.add(position.getQuoteid());
                    namesByQuoteid.put(position.getQuoteid(), position.getItemname());
                }
                result.put(entry.getKey(), new NamedIdSetImpl(entry.getKey(),
                        StringUtils.hasText(displayname) ? displayname : entry.getKey(), ids, namesByQuoteid));
            }
            return result;
        }

        private void add(Map<Long, Set<Long>> map, Long indexid, Long positionid) {
            Set<Long> positions = map.get(indexid);
            if (positions == null) {
                positions = new HashSet<>();
                map.put(indexid, positions);
            }
            positions.add(positionid);
        }

        @Override
        protected Map<String, IndexComposition> getResult() {
            return this.indexCompositions;
        }
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<Map<Long, Set<Long>>> indexComposition = new AtomicReference<>();

    private final AtomicReference<Map<Long, Set<Long>>> indexCompositionByQuoteid = new AtomicReference<>();

    private final AtomicReference<Map<Long, Set<Long>>> indexCompositionIidsByQuoteid = new AtomicReference<>();

    private final AtomicReference<Map<String, NamedIdSet>> indexCompositionByName = new AtomicReference<>();

    private volatile Map<String, IndexComposition> indexCompositions = Collections.emptyMap();

    private volatile Map<Long, long[]> qidToIndexQids = Collections.emptyMap();

    private ActiveMonitor activeMonitor;

    private File file;

    private ListOverviewProvider listOverviewProvider;

    private InstrumentBenchmarkProvider benchmarkProvider;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setListOverviewProvider(ListOverviewProvider listOverviewProvider) {
        this.listOverviewProvider = listOverviewProvider;
    }

    public void setFile(File file) {
        this.file = file;
    }


    public void setNewStyle(boolean newStyle) {
        if (!newStyle) {
            throw new IllegalArgumentException("old style no longer supported");
        }
    }

    public void afterPropertiesSet() throws Exception {
        this.benchmarkProvider = new InstrumentBenchmarkProvider(this);

        readIndexCompositionData();

        if (this.activeMonitor == null) {
            return;
        }

        final FileResource resource = getFileResource();
        this.activeMonitor.addResource(resource);
    }

    private FileResource getFileResource() throws Exception {
        final FileResource resource = new FileResource(this.file);
        resource.addPropertyChangeListener(evt -> readIndexCompositionData());
        return resource;
    }

    private void readIndexCompositionData() {
        try {
            doReadIndexCompositionData();
            assertListAvailability();
        } catch (Exception e) {
            this.logger.error("<readIndexCompositionData> failed", e);
        }
    }

    private void assertListAvailability() {
        this.listOverviewProvider.getListIds()
                .stream()
                .filter(id -> getIndexCompositionByListId(id) == null)
                .forEach(id -> this.logger.error("<assertListAvailability> no index composition for list '" + id + "'"));
    }

    private void doReadIndexCompositionData() throws Exception {
        Reader r = new Reader();
        this.indexCompositions = r.read(this.file);
        this.qidToIndexQids = r.qidToIndexQids;
        this.logger.info("#index compositions = " + this.indexCompositions.size());
        this.logger.info("#qidToIndexQids     = " + this.qidToIndexQids.size());

        // TODO: remove after clients switched to new
        this.indexComposition.set(r.map);
        this.indexCompositionByQuoteid.set(r.mapByQuoteid);
        this.indexCompositionIidsByQuoteid.set(r.iidmapByQuoteid);
        this.indexCompositionByName.set(r.getNamedMap());

        this.logger.info("#index compositions by instrumentid: " + this.indexComposition.get().size());
        this.logger.info("#index compositions by quoteid: " + this.indexCompositionByQuoteid.get().size());
        this.logger.info("#index compositions (iid) by quoteid: " + this.indexCompositionIidsByQuoteid.get().size());
        this.logger.info("#index compositions by name: " + this.indexCompositionByName.get().size());
    }

    public Set<Long> getIndexPositionsByQuoteid(long quoteid) {
        final Map<Long, Set<Long>> map = this.indexCompositionByQuoteid.get();
        return map.get(quoteid);
    }

    public Set<Long> getIndexPositionIidsByQuoteid(long quoteid) {
        final Map<Long, Set<Long>> map = this.indexCompositionIidsByQuoteid.get();
        return map.get(quoteid);
    }

    public Set<Long> getIndexPositionsByName(String name) {
        final NamedIdSet set = getIndexDefintionByName(name);
        if (set == null) {
            return Collections.emptySet();
        }
        return set.getQids();
    }

    public NamedIdSet getIndexDefintionByName(String name) {
        if (name.startsWith("listoverview|")) {
            return getOldListOverview(name);
        }

        final Map<String, NamedIdSet> map = this.indexCompositionByName.get();
        return map.get(name);
    }

    @Override
    public IndexCompositionResponse getIndexComposition(IndexCompositionRequest request) {
        final String key = request.getKey();
        if (key.startsWith("listoverview|")) {
            return new IndexCompositionResponse(getListOverview(key));
        }

        return new IndexCompositionResponse(this.indexCompositions.get(key));
    }

    private NamedIdSet getOldListOverview(String name) {
        final String[] tokens = name.split("\\|");
        // 0: listoverview, 1: customer (currently, only DZ), 2: type of function (list or elements), 3: id
        final ListOverviewListItem item =
                tokens.length >= 5 ?
                        this.listOverviewProvider.getListDefinition(tokens[3], tokens[4]) :
                        this.listOverviewProvider.getListDefinition(tokens[3], null);

        if (item == null) {
            return null;
        }

        if ("list".equals(tokens[2])) {
            if (item.getList() == null) {
                return null;
            }
            final String listid = item.getList().getValue();
            final String market = item.getList().getMarket();
            if (listid.endsWith(".qid")) {
                return new NamedIdSetImpl(listid, tokens[3],
                        getIndexPositionsByQuoteid(EasytradeInstrumentProvider.id(listid)),
                        Collections.<Long, String>emptyMap())
                        .withMarketStrategy(StringUtils.hasText(market) ? "market:" + market : null);
            }

            final NamedIdSet nameSet = getIndexDefintionByName(listid);
            return nameSet == null ? null
                    : new NamedIdSetImpl(nameSet.getId(), nameSet.getName(),
                    nameSet.getIds(), Collections.<Long, String>emptyMap())
                    .withMarketStrategy(StringUtils.hasText(market) ? "market:" + market : null);
        }

        if ("elements".equals(tokens[2])) {
            final List<String> elements = item.getElement();

            final Set<Long> qids =
                    elements.stream()
                            .map(EasytradeInstrumentProvider::id)
                            .collect(Collectors.toCollection(HashSet::new));

            final String listid = name + "-elements";
            return new NamedIdSetImpl(listid, listid, qids, Collections.<Long, String>emptyMap());
        }

        return null;
    }

    private IndexComposition getListOverview(String name) {
        // 0: listoverview, 1: customer (currently, only DZ), 2: type of function (list or elements), 3: id
        final String[] tokens = name.split("\\|");
        final String variant = (tokens.length >= 5) ? tokens[4] : null;
        final ListOverviewListItem item =
                this.listOverviewProvider.getListDefinition(tokens[3], variant);

        if (item == null) {
            return null;
        }

        if ("list".equals(tokens[2])) {
            ListOverviewListItem.List list = item.getList();
            if (list == null) {
                return null;
            }

            final IndexComposition ic = getIndexCompositionByListId(list.getValue());
            if (ic == null) {
                return null;
            }
            return ic.with(list.getValue(), tokens[3], getMarketStrategy(list));
        }

        if ("elements".equals(tokens[2])) {
            final List<String> elements = item.getElement();

            final String listid = name + "-elements";
            IndexComposition.Builder builder = IndexComposition.createBuilder(listid, listid, null);

            for (final String element : elements) {
                builder.addItem(EasytradeInstrumentProvider.id(element), 0L, null);
            }
            return builder.build();
        }

        return null;
    }

    private String getMarketStrategy(ListOverviewListItem.List list) {
        final String market = list.getMarket();
        return StringUtils.hasText(market) ? ("market:" + market) : null;
    }

    private IndexComposition getIndexCompositionByListId(final String listId) {
        String key = listId.endsWith(".qid") ? listId.substring(0, listId.length() - 4) : listId;
        return this.indexCompositions.get(key);
    }

    @Override
    public IndexMembershipResponse getIndexMembership(IndexMembershipRequest request) {
        final Map<Long, long[]> map = this.qidToIndexQids;
        final Map<String, IndexComposition> ics = this.indexCompositions;

        final Map<Long, String> tmp = new HashMap<>();
        for (final Long qid : request.getQids()) {
            final long[] indexQids = map.get(qid);
            if (indexQids != null) {
                for (long indexQid : indexQids) {
                    IndexComposition ic = ics.get(Long.toString(indexQid));
                    if (ic != null) {
                        tmp.put(indexQid, ic.getConstituentsGroup());
                    }
                }
            }
        }
        return new IndexMembershipResponse(tmp);
    }

    public Set<Long> getIndexQuoteidsForQuoteids(List<Long> quoteid) {
        final Map<Long, long[]> map = this.qidToIndexQids;
        final Set<Long> result = new HashSet<>();
        for (final Long qid : quoteid) {
            final long[] indexQids = map.get(qid);
            if (indexQids != null) {
                for (long indexQid : indexQids) {
                    result.add(indexQid);
                }
            }
        }
        return result;
    }

    public Long getBenchmarkId(Instrument instrument) {
        return this.benchmarkProvider.getBenchmarkId(instrument);
    }

    public static void main(String[] args) throws Exception {
        File p = LocalConfigProvider.getProductionDir("var/data/provider/");

        final ListOverviewProviderImpl lop = new ListOverviewProviderImpl();
        lop.setFile(new File(p, "dz-structure-definition.xml"));
        lop.setEntitlementsFile(new File(p, "dz-structure-entitlements.txt"));
        lop.afterPropertiesSet();

        final IndexCompositionProviderImpl r = new IndexCompositionProviderImpl();
        r.setFile(new File(p, "istar-indexcomposition-new.xml.gz"));
        r.setListOverviewProvider(lop);
        r.setNewStyle(true);
        r.afterPropertiesSet();

        System.out.println(r.getIndexComposition(new IndexCompositionRequest(106547L)));
        System.out.println(r.getIndexComposition(new IndexCompositionRequest("listoverview|DZ|list|dax-parkett")));
        System.out.println(r.getOldListOverview("listoverview|DZ|list|dax-parkett"));


//        final Set<Long> cers = r.getAssociatedInstruments(20665L, InstrumentTypeEnum.CER);
//        System.out.println(cers);
//        System.out.println("qids (DAX): " + r.getIndexPositionsByQuoteid(106547L));
//        System.out.println("iids (DAX): " + r.getIndexPositionIidsByQuoteid(106547L));
//        System.out.println("indizes: " + r.getIndexQuoteidsForQuoteids(Collections.singletonList(312864L)));

//        System.out.println(r.getListOverview("listoverview|DZ|list|DEUTSCHLAND.DAX.Xetra"));
        //System.out.println(r.getListOverview("listoverview|DZ|elements|Deutschland.DAX.Xetra"));
//        System.out.println(r.getListOverview("listoverview|DZ|elements|Devisen.CrossRates (EUR)"));

    }

}
