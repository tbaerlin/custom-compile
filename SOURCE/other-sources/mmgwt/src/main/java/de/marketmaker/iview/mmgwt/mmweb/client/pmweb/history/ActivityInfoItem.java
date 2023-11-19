package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextItem;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ShellMMInfo;
import de.marketmaker.iview.pmxml.ShellMMType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 22.02.13 15:36
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class ActivityInfoItem implements ContextItem {
    private final ActivityInstanceInfo activityInstanceInfo;

    public ActivityInfoItem(ActivityInstanceInfo activityInstanceInfo) {
        this.activityInstanceInfo = activityInstanceInfo;
    }

    @Override
    public String getName() {
        return this.activityInstanceInfo.getDefinition().getName();
    }

    public String getId() {
        return this.activityInstanceInfo.getId();
    }
}