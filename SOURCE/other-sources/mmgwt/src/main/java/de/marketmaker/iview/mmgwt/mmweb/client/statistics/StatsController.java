/*
 * StatsController.java
 *
 * Created on 13.04.2010 13:45:59
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.statistics;

import com.extjs.gxt.ui.client.widget.Text;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractPageController;
import de.marketmaker.iview.mmgwt.mmweb.client.GuiDefsLoader;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author oflege
 */
abstract class StatsController extends AbstractPageController {
    protected static StatisticsMetaData metaData = null;


    protected void loadMetaData() {
        final String vwdId = SessionData.INSTANCE.getUser().getVwdId();
        StatisticsServiceAsync.App.getInstance().getMetaData(GuiDefsLoader.getModuleName(), vwdId,
                new AsyncCallback<StatisticsMetaData>() {
                    public void onFailure(Throwable throwable) {
                        onMetaDataAvailable();
                    }

                    public void onSuccess(StatisticsMetaData statisticsMetaData) {
                        metaData = statisticsMetaData;
                        onMetaDataAvailable();
                    }
                });
    }

    protected void onMetaDataAvailable() {
        if (metaData == null) {
            getContentContainer().setContent(new Text(I18n.I.error())); 
            return;
        }
        try {
            getContentContainer().setContent(getView());
        } catch (Exception e) {
            Firebug.error("Fehler", e); // $NON-NLS-0$
        }
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        if (metaData == null) {
            loadMetaData();
        }
        else {
            getContentContainer().setContent(getView());
        }
    }

    protected abstract Widget getView();
}
