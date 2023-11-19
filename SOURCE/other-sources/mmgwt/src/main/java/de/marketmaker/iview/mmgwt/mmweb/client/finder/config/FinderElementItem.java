package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;
import java.util.Collection;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.AppConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserItem;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserTouch;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserWidget;

/**
 * Created on 04.10.11 14:40
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class FinderElementItem implements LiveFinderConfiguratorItem {
    private final ItemChooserWidget icw;
    private final String liveFinderId;
    private final String elementId;
    private final String label;

    public FinderElementItem(String label, List<FinderFormElements.Item> items, String liveFinderId,
                             String elementId, String[] defaultConfig, ItemChooserTouch.Callback itemChooserCallback) {
        this.liveFinderId = liveFinderId;
        this.elementId = elementId;
        this.label = label;
        final List<ItemChooserItem> allItems = new ArrayList<>();
        final TreeMap<Integer, ItemChooserItem> savedItemsDict = new TreeMap<Integer, ItemChooserItem>();
        final TreeMap<Integer, ItemChooserItem> defaultItemsDict = new TreeMap<Integer, ItemChooserItem>();


        final String tmp = SessionData.INSTANCE.getUser().getAppConfig().getProperty(AppConfig.LIVE_FINDER_ELEMENT_PREFIX
                + this.liveFinderId + this.elementId);

        final List<String> savedConf;
        if (SectionConfigUtil.EMPTY_CONF.equals(tmp)) {
            savedConf = Collections.emptyList();
        } else if (!StringUtil.hasText(tmp)) {
            savedConf = null;
        } else {
            savedConf = Arrays.asList(tmp.split(SectionConfigUtil.SEPARATOR));
        }

        final List<String> defaultConfigList = defaultConfig != null
                ? Arrays.asList(defaultConfig)
                : Collections.<String>emptyList();
        for (FinderFormElements.Item item : items) {
            final ItemChooserItem ici = new ItemChooserItem(item.getItem(), item.getValue(), false);
            allItems.add(ici);
            int confIdx = getElementIndexInConf(item, savedConf);
            if(confIdx > -1) {
                savedItemsDict.put(confIdx, ici);
            }

            int defaultIdx = getElementIndexInConf(item, defaultConfigList);
            if (defaultIdx > -1) {
                defaultItemsDict.put(defaultIdx, ici);
            }
        }

        final List<ItemChooserItem> savedItems = createSortedItemList(savedItemsDict);
        final List<ItemChooserItem> defaultItems = createSortedItemList(defaultItemsDict);

        this.icw = new ItemChooserTouch(allItems, savedConf == null ? null : savedItems, defaultItems, itemChooserCallback)
                .withStyleForSelectedItemsListBox("mm-3rd-item-bottom-border"); // $NON-NLS$
        this.icw.setLeftColHead(I18n.I.availableOptions());
        this.icw.setRightColHead(I18n.I.selectedOptions());
    }

    public FinderElementItem(String label, FinderMetaList metadata, String liveFinderId, String elementId, String[] defaultConfig, ItemChooserTouch.Callback itemChooserCallback) {
        this(label, getItems(metadata), liveFinderId, elementId, defaultConfig, itemChooserCallback);
    }

    private static List<FinderFormElements.Item> getItems(FinderMetaList metadata) {
        final ArrayList<FinderFormElements.Item> items = new ArrayList<>();
        final List<FinderMetaList.Element> elements = metadata.getElement();
        for (FinderMetaList.Element e : elements) {
            items.add(new FinderFormElements.Item(e.getName(), e.getKey()));
        }
        return items;
    }

    private int getElementIndexInConf(FinderFormElements.Item item, List<String> savedConf) {
        if(savedConf != null) {
            for(int i = 0; i < savedConf.size(); ++i) {
                String s = savedConf.get(i);
                if (s.equals(item.getItem()) || s.equals(item.getValue())) {
                    return i;
                }
            }
        }
        return -1;
    }

   private List<ItemChooserItem> createSortedItemList(TreeMap<Integer, ItemChooserItem> itemsDict) {
        final List<ItemChooserItem> resultItems = new ArrayList<>();
        final Collection<ItemChooserItem> items = itemsDict.values();
        for (ItemChooserItem entry : items) {
            resultItems.add(entry);
        }
        return resultItems;
   }

    public ItemChooserWidget getWidget() {
        return this.icw;
    }

    public String getName() {
        return this.label;
    }

    public void save() {
        final StringBuilder sb = new StringBuilder();
        for (int i = 0; i < this.icw.getSelectedRowsCount(); i++) {
            if (i != 0) {
                sb.append(SectionConfigUtil.SEPARATOR);
            }
            final String columnValue = this.icw.getColumnValue(i);
            if (columnValue.contains(SectionConfigUtil.SEPARATOR)) {
                throw new IllegalArgumentException("Cannot save configuration. Identifier contains separator char: " + columnValue); // $NON-NLS$
            }
            sb.append(columnValue);
        }
        final String conf = sb.toString();
        SessionData.INSTANCE.getUser().getAppConfig().addProperty(AppConfig.LIVE_FINDER_ELEMENT_PREFIX
                + this.liveFinderId + this.elementId,
                conf.isEmpty() ? SectionConfigUtil.EMPTY_CONF : conf);
    }
}