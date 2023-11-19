/*
 * PageStatisticsController.java
 *
 * Created on 14.01.2010 17:59:04
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;

/**
 * @author oflege
 */
public class TopStatisticsController extends StatsController {

    private TopStatisticsView view;

    void loadData(TopStatsCommand cmd) {
        StatisticsServiceAsync.App.getInstance().getStats(cmd, new AsyncCallback<StatsResult>() {
            public void onFailure(Throwable throwable) {
                Firebug.error("Fehler", throwable); // $NON-NLS-0$
                onDataAvailable(null);
            }

            public void onSuccess(StatsResult stats) {
                onDataAvailable(stats);
            }
        });
    }

    private void onDataAvailable(StatsResult stats) {
        this.view.showStats(stats);
    }


    @Override
    protected Widget getView() {
        if (this.view == null) {
            this.view = new TopStatisticsView(this);
        }
        return this.view;
    }
}
