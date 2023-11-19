package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import java.util.List;

import com.google.gwt.user.client.Command;

import de.marketmaker.iview.mmgwt.mmweb.client.finder.FinderSection;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserTouch;

/**
 * Created on 30.09.11 15:11
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class LiveFinderSectionConfigurator extends LiveFinderAbstractConfigurator {
    private final List<FinderSection> sections;

    public LiveFinderSectionConfigurator(String title, List<FinderSection> sections, final Command onOk) {
        super(title, onOk);
        this.sections = sections;
    }

    @Override
    protected void addItems(List<LiveFinderConfiguratorItem> items, ItemChooserTouch.Callback itemChooserCallback) {
        for (FinderSection section : this.sections) {
            if (!section.isConfigurable()) {
                continue;
            }
            final LiveFinderConfiguratorItem sectionItem = new SectionItem(section, itemChooserCallback);
            items.add(sectionItem);
        }
    }
}