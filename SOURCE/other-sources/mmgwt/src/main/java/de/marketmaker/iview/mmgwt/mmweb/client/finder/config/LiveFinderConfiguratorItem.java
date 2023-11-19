package de.marketmaker.iview.mmgwt.mmweb.client.finder.config;

import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ItemChooserWidget;

/**
 * Created on 04.10.11 14:39
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface LiveFinderConfiguratorItem {
    public void save();
    public ItemChooserWidget getWidget();
    public String getName();
}
