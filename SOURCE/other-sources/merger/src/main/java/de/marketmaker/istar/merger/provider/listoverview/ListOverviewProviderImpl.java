/*
 * ListOverviewProviderImpl.java
 *
 * Created on 14.07.2008 15:29:13
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.listoverview;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import de.marketmaker.istar.common.monitor.ActiveMonitor;
import de.marketmaker.istar.common.monitor.DirectoryResource;
import de.marketmaker.istar.common.monitor.FileResource;
import de.marketmaker.istar.common.util.CollectionUtils;
import de.marketmaker.istar.common.util.EntitlementsVwd;
import de.marketmaker.istar.common.util.LocalConfigProvider;
import de.marketmaker.istar.common.util.PropertiesLoader;
import de.marketmaker.istar.domain.KeysystemEnum;
import de.marketmaker.istar.domain.data.PriceQuality;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domainimpl.profile.ProfileFactory;
import de.marketmaker.istar.domainimpl.profile.ResourcePermissionProvider;
import de.marketmaker.istar.merger.context.RequestContext;
import de.marketmaker.istar.merger.context.RequestContextHolder;
import de.marketmaker.istar.merger.util.JaxbContextCache;
import de.marketmaker.istar.merger.web.easytrade.block.LbbwMarketStrategy;
import de.marketmaker.iview.dmxml.ListOverviewColumn;
import de.marketmaker.iview.dmxml.ListOverviewFinderItem;
import de.marketmaker.iview.dmxml.ListOverviewItem;
import de.marketmaker.iview.dmxml.ListOverviewItemList;
import de.marketmaker.iview.dmxml.ListOverviewLinkItem;
import de.marketmaker.iview.dmxml.ListOverviewListItem;
import de.marketmaker.iview.dmxml.ListOverviewMultiListItem;
import de.marketmaker.iview.dmxml.ListOverviewPageItem;
import de.marketmaker.iview.dmxml.ListOverviewSection;
import de.marketmaker.iview.dmxml.ListOverviewTitleItem;
import de.marketmaker.iview.dmxml.ListOverviewType;
import de.marketmaker.iview.dmxml.ResponseType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ListOverviewProviderImpl implements InitializingBean, ListOverviewProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private File file;

    private File entitlementsFile;

    private final AtomicReference<Map<String, Set<Integer>>> entitlements = new AtomicReference<>();

    private final AtomicReference<Map<String, ListOverviewType>> structure = new AtomicReference<>();

    private final AtomicReference<Map<String, Map<String, ListOverviewListItem>>> lists = new AtomicReference<>();

    private ActiveMonitor activeMonitor;

    public void setActiveMonitor(ActiveMonitor activeMonitor) {
        this.activeMonitor = activeMonitor;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void setEntitlementsFile(File entitlementsFile) {
        this.entitlementsFile = entitlementsFile;
    }

    public void afterPropertiesSet() throws Exception {
        readEntitlements();

        final DirectoryResource sr = createStructureResource(this.file);
        sr.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                try {
                    //noinspection unchecked
                    readStructures(evt.getPropertyName(), (Set<File>) evt.getNewValue());
                } catch (Exception e) {
                    logger.error("<propertyChange> failed", e);
                }
            }
        });
        this.structure.set(new HashMap<String, ListOverviewType>());
        this.lists.set(new HashMap<String, Map<String, ListOverviewListItem>>());
        readStructures(DirectoryResource.ADDED, sr.getFiles());

        if (this.activeMonitor != null) {
            final FileResource er = new FileResource(this.entitlementsFile);
            er.addPropertyChangeListener(new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    try {
                        readEntitlements();
                    } catch (Exception e) {
                        logger.error("<propertyChange> failed", e);
                    }
                }
            });
            this.activeMonitor.addResource(er);

            this.activeMonitor.addResource(sr);
        }
    }

    private String getLanguageAndVariant(final File file) {
        if (file.equals(this.file)) {
            return "";
        }
        final String defaultName = this.file.getName();
        final String defaultNamePrefix = defaultName.substring(0, defaultName.length() - 4);
        final String filename = file.getName();
        return filename.substring(defaultNamePrefix.length() + 1, filename.length() - 4);
    }

    private DirectoryResource createStructureResource(final File file) throws Exception {
        final String filename = file.getName();
        final String filenamePrefix = filename.substring(0, filename.length() - 4);
        return new DirectoryResource(file.getParentFile().getAbsolutePath(), new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                final String name = pathname.getName();
                return name.startsWith(filenamePrefix + ".") && name.endsWith(".xml");
            }
        });
    }

    private void readStructures(String modificationKey, Set<File> files) throws Exception {
        final Map<String, ListOverviewType> mapStructures = new HashMap<>(this.structure.get());
        final HashMap<String, Map<String, ListOverviewListItem>> mapLists = new HashMap<>(this.lists.get());
        if (DirectoryResource.ADDED.equals(modificationKey) || DirectoryResource.MODIFIED.equals(modificationKey)) {
            for (File file : files) {
                readStructure(file, mapStructures, mapLists);
            }
        }
        else if (DirectoryResource.REMOVED.equals(modificationKey)) {
            for (File file : files) {
                removeStructure(file, mapStructures, mapLists);
            }
        }
        this.structure.set(mapStructures);
        this.lists.set(mapLists);
    }

    private void removeStructure(final File file, final Map<String, ListOverviewType> mapStructures,
            final HashMap<String, Map<String, ListOverviewListItem>> mapLists) throws Exception {
        final String languageAndVariant = getLanguageAndVariant(file);
        logger.info("removeStructure(" + languageAndVariant + ", " + file.getAbsolutePath() + ")");
        mapStructures.remove(languageAndVariant);
        mapLists.remove(languageAndVariant);
    }

    private void readStructure(final File file, final Map<String, ListOverviewType> mapStructures,
            final HashMap<String, Map<String, ListOverviewListItem>> mapLists) throws Exception {
        final String languageAndVariant = getLanguageAndVariant(file);
        logger.info("readStructure(" + languageAndVariant + ", " + file.getAbsolutePath() + ")");
        final JAXBContext jc = JaxbContextCache.INSTANCE.getContext("de.marketmaker.iview.dmxml");
        final Unmarshaller unmarshaller = jc.createUnmarshaller();
        final InputStream inputStream = new FileInputStream(file);
        final JAXBElement<ResponseType> jaxbElement = unmarshaller.unmarshal(new StreamSource(inputStream), ResponseType.class);
        inputStream.close();
        final ResponseType response = jaxbElement.getValue();
        final ListOverviewType overview = (ListOverviewType) response.getData().getBlockOrError().get(0);
        final Map<String, ListOverviewListItem> map = initialize(overview);
        mapStructures.put(languageAndVariant, overview);
        mapLists.put(languageAndVariant, map);
    }

    private void readEntitlements() throws Exception {
        logger.info("readEntitlements()  -->  " + this.entitlementsFile.getAbsolutePath());
        final Map<String, Set<Integer>> map = new HashMap<>();
        final Properties properties = PropertiesLoader.load(this.entitlementsFile);
        for (String name : properties.stringPropertyNames()) {
            map.put(name, toSelectorSet(properties.getProperty(name)));
        }
        this.entitlements.set(map);
    }

    private Set<Integer> toSelectorSet(String selectorStr) {
        if (StringUtils.hasText(selectorStr)) {
            final String[] selectors = StringUtils.tokenizeToStringArray(selectorStr, ",");
            if (selectors.length == 1) {
                return Collections.singleton(EntitlementsVwd.toValue(selectors[0]));
            }
            final HashSet<Integer> result = new HashSet<>();
            for (final String selector : selectors) {
                result.add(EntitlementsVwd.toValue(selector));
            }
            return result;
        }
        return Collections.emptySet();
    }

    private Map<String, ListOverviewListItem> initialize(ListOverviewType structure) {
        final Map<String, ListOverviewListItem> map = new HashMap<>();
        for (final ListOverviewColumn column : structure.getColumn()) {
            for (final ListOverviewSection section : column.getSection()) {
                map.putAll(initializeItems(section.getName(), section.getItem()));
            }
        }
        return map;
    }

    private Map<String, ListOverviewListItem> initializeItems(String name,
            List<ListOverviewItem> items) {
        final Map<String, ListOverviewListItem> map = new HashMap<>();

        for (final ListOverviewItem item : items) {
            final String namePath = name + "." + item.getName();
            String id = item.getId();
            if (id == null) {
                id = namePath;
                item.setId(id);
            }

            if (item instanceof ListOverviewListItem) {
                final ListOverviewListItem list = (ListOverviewListItem) item;
                map.put(id, list);
                map.put(namePath, list);
            }
            else if (item instanceof ListOverviewItemList) {
                map.putAll(initializeItems(namePath, ((ListOverviewItemList) item).getItem()));
            }
        }

        return map;
    }

    public ListOverviewType getStructure() {
        return getStructure(null, null);
    }

    @Override
    public ListOverviewType getStructure(String variant) {
        return getStructure(Arrays.asList(Locale.GERMAN), variant);
    }

    public ListOverviewType getStructure(final List<Locale> locales, String variant) {
        ListOverviewType overview = getEntry(this.structure, locales, variant);
        if (overview == null) {
            throw new NullPointerException("no structure found for locales " + locales);
        }

        final ListOverviewType result = new ListOverviewType();

        for (final ListOverviewColumn column : overview.getColumn()) {
            final ListOverviewColumn rcolumn = new ListOverviewColumn();

            for (final ListOverviewSection section : column.getSection()) {
                final List<ListOverviewItem> ritems = processItems(section.getName(), section.getItem());

                removeRatiosOnly(ritems);
                removeHeadlines(ritems);

                if (!isEmpty(ritems)) {
                    final ListOverviewSection rsection = new ListOverviewSection();
                    rsection.setName(section.getName());
                    rsection.getItem().addAll(ritems);
                    rcolumn.getSection().add(rsection);
                }
            }

            if (!rcolumn.getSection().isEmpty()) {
                result.getColumn().add(rcolumn);
            }
        }

        return result;
    }

    private void removeRatiosOnly(List<ListOverviewItem> ritems) {
        for (int i = 0; i < ritems.size(); i++) {
            final ListOverviewItem current = ritems.get(i);
            if (current instanceof ListOverviewItemList) {
                final ListOverviewItemList list = (ListOverviewItemList) current;
                if (list.getItem().size() == 1 && list.getItem().get(0) instanceof ListOverviewFinderItem) {
                    ritems.set(i, null);
                }
            }
        }
        CollectionUtils.removeNulls(ritems);
    }

    private void removeHeadlines(List<ListOverviewItem> ritems) {
        for (int i = 0; i < ritems.size() - 1; i++) {
            final ListOverviewItem current = ritems.get(i);
            final ListOverviewItem next = ritems.get(i + 1);
            if (current instanceof ListOverviewTitleItem && next instanceof ListOverviewTitleItem) {
                ritems.set(i, null);
            }
        }
        int i = ritems.size();
        while (i-- > 0) {
            final ListOverviewItem current = ritems.get(i);
            if (current instanceof ListOverviewTitleItem) {
                ritems.set(i, null);
            }
            else {
                break;
            }
        }
        CollectionUtils.removeNulls(ritems);
    }

    private boolean isEmpty(List<ListOverviewItem> items) {
        if (items.isEmpty()) {
            return true;
        }

        for (final ListOverviewItem item : items) {
            if (!(item instanceof ListOverviewTitleItem)) {
                return false;
            }
        }

        return true;
    }

    private <T> T getEntry(AtomicReference<Map<String, T>> mapReference, List<Locale> locales,
            String variant) {
        final Map<String, T> map = mapReference.get();
        if (locales == null || locales.isEmpty()) {
            return map.get("");
        }
        if (!StringUtils.hasText(variant)) {
            variant = "DE";
        }
        for (Locale locale : locales) {
            String loc = locale.toString();
            T t = map.get(loc + "." + variant);
            while (t == null && !loc.isEmpty()) {
                int pos = loc.lastIndexOf('_');
                loc = pos == -1 ? "" : loc.substring(0, pos);
                t = map.get(loc + "." + variant);
            }
            if (t != null) {
                return t;
            }
        }
        return null;
    }

    public ListOverviewListItem getListDefinition(String id) {
        return getListDefinition(id, null, null);
    }

    public ListOverviewListItem getListDefinition(String id, String variant) {
        return getListDefinition(id, Arrays.asList(Locale.GERMAN), variant);
    }

    public ListOverviewListItem getListDefinition(String id, final List<Locale> locales,
            String variant) {
        final Map<String, ListOverviewListItem> list = getEntry(this.lists, locales, variant);
        if (list == null) {
            throw new NullPointerException("no list definition found for locales " + locales);
        }
        return list.get(id);
    }

    @Override
    public Collection<String> getListIds() {
        HashSet<String> result = new HashSet<>();
        Map<String, Map<String, ListOverviewListItem>> map = this.lists.get();
        for (Map<String, ListOverviewListItem> m : map.values()) {
            for (ListOverviewListItem item : m.values()) {
                if (item.getList() != null && StringUtils.hasText(item.getList().getValue())) {
                    result.add(item.getList().getValue());
                }
            }
        }
        return result;
    }

    private List<ListOverviewItem> processItems(String name, List<ListOverviewItem> items) {
        final Profile profile = RequestContextHolder.getRequestContext().getProfile();

        final List<ListOverviewItem> result = new ArrayList<>();

        for (final ListOverviewItem item : items) {

            if (item instanceof ListOverviewTitleItem) {
                result.add(item);
            }
            else if (item instanceof ListOverviewPageItem
                    || item instanceof ListOverviewLinkItem
                    || item instanceof ListOverviewMultiListItem
                    || item instanceof ListOverviewListItem) {
                final String namePath = name + "." + item.getName();
                Set<Integer> selectors = getSelectors(item, namePath);
                if (checkItem(profile, selectors, item.isSelectorsWithOr(), namePath)) {
                    result.add(item);
                }
            }
            else if (item instanceof ListOverviewFinderItem) {
                // TODO: check? Or allow finder always?
                result.add(item);
            }
            else if (item instanceof ListOverviewItemList) {
                final List<ListOverviewItem> innerList
                        = processItems(name + "." + item.getName(), ((ListOverviewItemList) item).getItem());
                if (!innerList.isEmpty()) {
                    final ListOverviewItemList rlistitem = new ListOverviewItemList();
                    rlistitem.setName(item.getName());
                    rlistitem.getItem().addAll(innerList);
                    result.add(rlistitem);
                }
            }
        }

        return result;
    }

    private Set<Integer> getSelectors(ListOverviewItem item, String namePath) {
        final Map<String, Set<Integer>> entitlementsMap = this.entitlements.get();
        Set<Integer> selectors = entitlementsMap.get(item.getId());
        return (selectors != null) ? selectors : entitlementsMap.get(namePath.replace(" ", ""));
    }

    private boolean checkItem(Profile profile, Set<Integer> selectors, Boolean selectorsWithOr,
            String namePath) {
        return (selectors == null) || (Boolean.TRUE.equals(selectorsWithOr))
                ? checkItemWithOr(profile, selectors, namePath)
                : checkItemWithAnd(profile, selectors, namePath);
    }

    private boolean checkItemWithAnd(Profile profile, Set<Integer> selectors, String namePath) {
        final Integer missingSelector = isAllowedWithAnd(profile, selectors);
        if (missingSelector == null) {
            return true;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("<processItems> not allowed: " + namePath + ", missing: " + missingSelector + "/" + EntitlementsVwd.toEntitlement(missingSelector));
        }
        return false;
    }

    private Integer isAllowedWithAnd(Profile profile, Set<Integer> selectors) {
        if (selectors == null) {
            return null;
        }

        for (final Integer selector : selectors) {
            if (!isAllowed(profile, selector)) {
                return selector;
            }
        }
        return null;
    }

    private boolean checkItemWithOr(Profile profile, Set<Integer> selectors, String namePath) {
        final boolean allowed = isAllowedWithOr(profile, selectors);
        if (!allowed && this.logger.isDebugEnabled()) {
            String divider = "";
            final StringBuilder sb = new StringBuilder();
            for (Integer selector : selectors) {
                sb.append(divider).append(selector).append('(').append(EntitlementsVwd.toEntitlement(selector)).append(')');
                divider = ", ";
            }
            this.logger.debug("<processItems> not allowed: " + namePath + ", missing (or): " + sb.toString());
        }
        return allowed;
    }

    private boolean isAllowedWithOr(Profile profile, Set<Integer> selectors) {
        if (selectors == null) {
            return true;
        }

        for (final Integer selector : selectors) {
            if (isAllowed(profile, selector)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAllowed(Profile profile, Integer selector) {
        final PriceQuality pq
                = profile.getPriceQuality(Integer.toString(selector), KeysystemEnum.VWDFEED);
        return (pq != PriceQuality.NONE
                || profile.isAllowed(Profile.Aspect.FUNCTION, selector.toString())
                || profile.isAllowed(Profile.Aspect.PRODUCT, selector.toString()));
    }


    public static void main(String[] args) throws Exception {
        final Locale locale = Locale.GERMAN;
        final List<Locale> locales = Arrays.asList(locale);
        final ListOverviewProviderImpl p = new ListOverviewProviderImpl();
        final File dir = LocalConfigProvider.getProductionDir("var/data/web");
        p.setFile(new File(dir, "dz-structure-definition.xml"));
        p.setEntitlementsFile(new File(dir, "dz-structure-entitlements.txt"));
        p.setActiveMonitor(new ActiveMonitor());
        p.afterPropertiesSet();

        Profile profile = ProfileFactory.createInstance(ResourcePermissionProvider.getInstance("dzbank-research"));
        RequestContextHolder.setRequestContext(new RequestContext(profile, LbbwMarketStrategy.INSTANCE));
        final ListOverviewType structure;
        try {
            structure = p.getStructure(locales, "DE");
        } finally {
            RequestContextHolder.setRequestContext(null);
        }

        final JAXBContext jc = JaxbContextCache.INSTANCE.getContext("de.marketmaker.iview.dmxml");
        final Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, XMLConstants.NULL_NS_URI);
        final StringWriter writer = new StringWriter();
        for (final ListOverviewColumn column : structure.getColumn()) {
            marshaller.marshal(new JAXBElement<>(new QName(XMLConstants.NULL_NS_URI, "column"),
                    ListOverviewColumn.class, column), writer);
            writer.write("\n");
        }

        writer.close();
        final StringBuffer buffer = writer.getBuffer();
        System.out.println(buffer.toString());

        final ListOverviewListItem listItem = p.getListDefinition("devisen-ezbeuro", locales, "DE");
        System.out.println("elements: " + listItem.getElement());
        System.out.println("list: " + listItem.getList().getValue());
    }
}
