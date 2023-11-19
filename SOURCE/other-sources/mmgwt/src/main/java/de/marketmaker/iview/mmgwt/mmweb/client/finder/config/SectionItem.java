package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.CloneableFinderFormElement;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElement;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderSection;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserItem;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserTouch;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserWidget;

/**
 * Created on 04.10.11 14:40
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class SectionItem implements LiveFinderConfiguratorItem {
    private final ItemChooserWidget icw;
    private final FinderSection section;
    private final Map<String, String> itemTitleExceptions = new HashMap<>();
    private final List<CloneableFinderFormElement> clones = new ArrayList<>();

    public SectionItem(FinderSection section, ItemChooserTouch.Callback itemChooserCallback) {
        this.section = section;

        this.itemTitleExceptions.put(I18n.I.averageVolume(), I18n.I.averageVolume2());

        final List<ItemChooserItem> allElements = new ArrayList<>();
        final List<ItemChooserItem> savedElements;
        if (SectionConfigUtil.getSectionConfigAsString(section.getId()) == null) {
            savedElements = null;
        }
        else {
            savedElements = new ArrayList<>();
        }

        final Map<Integer, List<ItemChooserItem>> defaultElements = new HashMap<>();

        final List<FinderFormElement> elements = section.getElements();
        for (FinderFormElement element : elements) {
            if (!element.isConfigurable()) {
                continue;
            }

            if (element instanceof CloneableFinderFormElement && (((CloneableFinderFormElement) element).isClone())) {
                this.clones.add((CloneableFinderFormElement) element);
                continue;
            }

            final ItemChooserItem itemChooserItem = new ItemChooserItem(
                    getException(element.getLabel()), element.getId(), false);
            allElements.add(itemChooserItem);
            if (element.isActive() && savedElements != null) {
                savedElements.add(itemChooserItem);
            }
            if (element.getDefaultOrder() != null) {
                if (!defaultElements.containsKey(element.getDefaultOrder())) {
                    defaultElements.put(element.getDefaultOrder(), new ArrayList<ItemChooserItem>());
                }
                defaultElements.get(element.getDefaultOrder()).add(itemChooserItem);
            }
        }

        this.icw = new ItemChooserTouch(allElements, savedElements, handleDefaultOrder(defaultElements), itemChooserCallback);
        this.icw.setLeftColHead(I18n.I.availableFinderParams());
        this.icw.setRightColHead(I18n.I.selectedFinderParams());
    }

    public ItemChooserWidget getWidget() {
        return this.icw;
    }

    public String getName() {
        return this.section.getLabel();
    }

    private List<ItemChooserItem> handleDefaultOrder(Map<Integer, List<ItemChooserItem>> defaultElements) {
        final List<ItemChooserItem> result = new ArrayList<>();
        final ArrayList<Integer> order = new ArrayList<>(defaultElements.keySet());
        Collections.sort(order);
        for (Integer integer : order) {
            final List<ItemChooserItem> itemChooserItems = defaultElements.get(integer);
            Collections.sort(itemChooserItems, new Comparator<ItemChooserItem>() {
                public int compare(ItemChooserItem o1, ItemChooserItem o2) {
                    return o1.getText().compareTo(o2.getText());
                }
            });
            result.addAll(itemChooserItems);
        }
        return result;
    }


    public void save() {
        List<String> elementIds = new ArrayList<>();
        final List<String> tmp = SectionConfigUtil.getElementIds(this.icw);
        for (String id : tmp) {
            elementIds.addAll(handleClones(id));
        }
        SectionConfigUtil.saveSectionConfig(this.section.getId(), elementIds);
    }

    private List<String> handleClones(String id) {
        final ArrayList<String> result = new ArrayList<>();
        result.add(id);
        for (CloneableFinderFormElement clone : this.clones) {
            if (FinderFormUtils.getOriginalId(clone.getId()).equals(id)) {
                result.add(clone.getId());
            }
        }
        return result;
    }

    private String getException(String value) {
        if (this.itemTitleExceptions.containsKey(value)) {
            return this.itemTitleExceptions.get(value);
        }
        else {
            return value;
        }
    }

}
