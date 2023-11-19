/*
 * SpsDashboardWidget.java
 *
 * Created on 02.10.2015 08:24
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.ConfigDao;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.DashboardController;
import de.marketmaker.iview.mmgwt.mmweb.client.dashboard.IdHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.data.DashboardConfig;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets.ObjectIdSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author mdick
 */
public class SpsDashboardWidget extends SpsBoundWidget<Widget, SpsGroupProperty> implements SpsAfterPropertiesSetHandler, NoValidationPopup, RequiresRelease {
    private final SimplePanel layout = new SimpleLayoutPanel();
    private DashboardController dc;

    public SpsDashboardWidget() {
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);
    }

    @Override
    public void afterPropertiesSet() {
        final SpsGroupProperty group = getBindFeature().getSpsProperty();
        final String analysisId = SpsUtil.getLeafStringValue(group, SpsAnalysisWidget.ANALYSIS_ID_LEAF);
        final String objectId = SpsUtil.getLeafStringValue(group, SpsAnalysisWidget.OBJECT_ID_LEAF);
        evaluate(objectId, analysisId);
    }

    public static boolean isDashboard(SpsGroupProperty spsGroupProperty) {
        final String analysisId = SpsUtil.getLeafStringValue(spsGroupProperty, SpsAnalysisWidget.ANALYSIS_ID_LEAF);
        return StringUtil.hasText(analysisId) && ConfigDao.getInstance().getConfigById(analysisId) != null;
    }

    private void evaluate(final String id, String analysisId) {
        if (!StringUtil.hasText(analysisId)) {
            throw new IllegalStateException("no analysisId given!"); // $NON-NLS$
        }

        final IdHandler idHandler;
        if(StringUtil.hasText(id)) {
            idHandler = new IdHandler() {
                @Override
                public void applyId(Snippet snippet) {
                    if (snippet instanceof ObjectIdSnippet) {
                        ((ObjectIdSnippet) snippet).setObjectId(id);
                    }
                }
            };
        }
        else {
            idHandler = null;
        }

        final DashboardConfig configById = ConfigDao.getInstance().getConfigById(analysisId);
        if(configById != null) {
            final DmxmlContext context = new DmxmlContext();
            this.dc = DashboardController.create(context, configById);
            if(idHandler != null) {
                this.dc.applyId(idHandler);
            }
            this.dc.refresh();
        }
        else {
            this.dc = DashboardController.createEmpty();
        }

        this.layout.setWidget(dc.getDashboardView());
    }

    @Override
    public void release() {
        super.release();
        if(this.dc != null) {
            this.dc.destroy();
            this.dc = null;
        }
    }

    @Override
    public void onPropertyChange() {
        //nothing to do. one would implement it, if there's a list or group which can be changed(add/delete)
    }

    @Override
    protected HTML createCaptionWidget() {
        return null;
    }

    @Override
    protected SimplePanel createInfoIconPanel() {
        return null;
    }

    @Override
    protected Widget createWidget() {
        return this.layout;
    }
}
