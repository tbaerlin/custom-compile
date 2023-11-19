package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import java.util.List;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderFormElements;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserTouch;

/**
 * Created on 30.09.11 15:11
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class LiveFinderElementConfigurator extends LiveFinderAbstractConfigurator {
    private final FinderMetaList metaList;
    private final List<FinderFormElements.Item> items;
    private final String liveFinderId;
    private final String elementId;
    private final String label;
    private final String[] defaultConfig;

    public LiveFinderElementConfigurator(String title, String label, List<FinderFormElements.Item> items, String liveFinderId, String elementId,
                                         String[] defaultConfig, Command onOk) {
        super(title, onOk);
        this.metaList = null;
        this.label = label;
        this.defaultConfig = defaultConfig;
        this.items = items;
        this.elementId = elementId;
        this.liveFinderId = liveFinderId;
    }

    public LiveFinderElementConfigurator(String title, String label, FinderMetaList metaList, String liveFinderId, String elementId,
                                         String[] defaultConfig, Command onOk) {
        super(title, onOk);
        if (metaList.getElement() == null || metaList.getElement().isEmpty()) {
            throw new IllegalStateException("metalist is null or empty"); // $NON-NLS$
        }
        this.items = null;
        this.label = label;
        this.defaultConfig = defaultConfig;
        this.metaList = metaList;
        this.elementId = elementId;
        this.liveFinderId = liveFinderId;
    }

    @Override
    protected void addItems(List<LiveFinderConfiguratorItem> items, ItemChooserTouch.Callback itemChooserCallback) {
        final LiveFinderConfiguratorItem item = this.metaList == null
                ? new FinderElementItem(this.label, this.items, this.liveFinderId, this.elementId, this.defaultConfig, itemChooserCallback)
                : new FinderElementItem(this.label, this.metaList, this.liveFinderId, this.elementId, this.defaultConfig, itemChooserCallback);
        items.add(item);
    }
}