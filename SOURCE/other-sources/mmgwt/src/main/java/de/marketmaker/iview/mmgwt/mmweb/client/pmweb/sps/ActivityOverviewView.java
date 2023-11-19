package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.user.client.ui.IsWidget;
import de.marketmaker.iview.pmxml.ActivityDefinitionInfo;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;

import java.util.List;

/**
 * Author: umaurer
 * Created: 24.02.14
 */
public interface ActivityOverviewView extends IsWidget {
    void clear();
    void setActivities(List<ActivityDefinitionInfo> defs, List<ActivityInstanceInfo> insts);

    public interface Presenter {
        void createNewActivity(ActivityDefinitionInfo def);
        void goToActivityInstance(ActivityInstanceInfo inst);
        void deleteActivity(ActivityInstanceInfo inst);
        void deleteAllActivities();
    }
}
