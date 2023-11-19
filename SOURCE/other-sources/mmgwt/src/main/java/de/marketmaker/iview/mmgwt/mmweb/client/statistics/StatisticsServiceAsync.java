/*
 * StatisticsService.java
 *
 * Created on 13.01.2010 15:21:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.ServiceDefTarget;

import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;

/**
 * @author oflege
 */
public interface StatisticsServiceAsync {

    void getMetaData(String module, String vwdId, AsyncCallback<StatisticsMetaData> callback);

    void getStats(PageStatsCommand command, AsyncCallback<StatsResult> callback);

    void getStats(TopStatsCommand command, AsyncCallback<StatsResult> callback);

    /**
     * Utility/Convenience class.
     * Use UserService.App.getInstance () to access static instance of UserAsync
     */
    public static class App {
        private static StatisticsServiceAsync app = null;

        public static StatisticsServiceAsync getInstance() {
            if (app == null) {
                final boolean hosted = GWT.getHostPageBaseURL().startsWith("http://localhost:8888/") || !GWT.isScript(); // $NON-NLS-0$
                final String entryPoint = hosted
                        ? "Statistics Service" // $NON-NLS-0$
                        : MainController.INSTANCE.contextPath + "/" + GuiDefsLoader.getModuleName() + "/stats.rpc"; // $NON-NLS$
                app = (StatisticsServiceAsync) GWT.create(StatisticsService.class);
                ((ServiceDefTarget) app).setServiceEntryPoint(entryPoint);
            }
            return app;
        }
    }

}
