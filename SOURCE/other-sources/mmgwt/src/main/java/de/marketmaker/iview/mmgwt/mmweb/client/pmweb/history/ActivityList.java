package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.history;

import de.marketmaker.iview.mmgwt.mmweb.client.history.ContextList;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Account;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created on 18.01.13 08:04
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class ActivityList extends ContextList<ActivityInfoItem> {

    public static ActivityList create(List<ActivityInstanceInfo> list) {
        ArrayList<ActivityInfoItem> activityInfoItems = new ArrayList<ActivityInfoItem>(list.size());
        for (ActivityInstanceInfo activityInstanceInfo : list) {
            activityInfoItems.add(new ActivityInfoItem(activityInstanceInfo));
        }
        return new ActivityList(activityInfoItems);
    }

    private ActivityList(List<ActivityInfoItem> list) {
        super(list);
    }

    @Override
    protected String getIdOf(ActivityInfoItem item) {
        return item.getId();
    }
}
