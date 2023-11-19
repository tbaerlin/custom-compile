package de.marketmaker.iview.mmgwt.mmweb.client.dashboard;

import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.util.NaturalComparator;

import java.util.Comparator;

/**
 * Author: umaurer
 * Created: 24.06.15
 */
public class ConfigComparator implements Comparator<DashboardConfig> {
    public static final ConfigComparator COMPARE_NAME = new ConfigComparator(false);
    public static final ConfigComparator COMPARE_ACCESS_THEN_NAME = new ConfigComparator(true);

    private static final NaturalComparator<String> NC = NaturalComparator.createDefault();

    private final boolean compareAccess;

    private ConfigComparator(boolean compareAccess) {
        this.compareAccess = compareAccess;
    }

    @Override
    public int compare(DashboardConfig dc1, DashboardConfig dc2) {
        if(!this.compareAccess) {
            return NC.compareIgnoreCase(dc1.getName(), dc2.getName());
        }

        final int result = compareAccess(dc2, dc1); //reverse the enum order so that private ones occur on top.
        if(result == 0) {
            return NC.compareIgnoreCase(dc1.getName(), dc2.getName());
        }
        return result;
    }

    public int compareAccess(DashboardConfig dc1, DashboardConfig dc2) {
        final DashboardConfig.Access a1 = dc1.getAccess();
        final DashboardConfig.Access a2 = dc2.getAccess();
        return a1 == null
                ? (a2 == null ? 0 : 1 )
                : (a2 == null ? -1 : a1.compareTo(a2));
    }
}
