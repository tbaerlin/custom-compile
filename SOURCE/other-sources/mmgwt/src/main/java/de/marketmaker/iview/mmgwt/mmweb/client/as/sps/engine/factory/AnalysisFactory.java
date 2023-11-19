/*
 * AnalysisFactory.java
 *
 * Created on 25.03.2014
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsAnalysisResultWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsAnalysisWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsDashboardWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.pmxml.AnalysisControlDesc;
import de.marketmaker.iview.pmxml.BoundWidgetDesc;

/**
 * @author mloesch
 */
public class AnalysisFactory extends Factory<AnalysisControlDesc> {

    public AnalysisFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(AnalysisControlDesc widgetDesc, Context context, BindToken parentToken) {
        Firebug.debug("<AnalysisFactory.doCreateSpsWidget> for desc " + widgetDesc.getClass());
        if(isDashboard(widgetDesc, context, parentToken)) {
            return new SpsDashboardWidget();
        }

        Firebug.debug("<AnalysisFactory.doCreateSpsWidget> analysisId not found in available dashboard layout IDs. Falling back to analysis widget");

        final SpsBoundWidget<Widget, SpsGroupProperty> result = widgetDesc.isUseAsyncHandle()
                ? new SpsAnalysisResultWidget()
                : new SpsAnalysisWidget();
        Firebug.debug("<AnalysisFactory.doCreateSpsWidget> created " + result.getClass());
        return result;
    }

    private boolean isDashboard(BoundWidgetDesc widgetDesc, Context context, BindToken parentToken) {
        final BindToken bindToken = BindToken.create(parentToken, widgetDesc.getBind());
        final SpsProperty spsProperty = context.getRootProp().get(bindToken.getHead());
        return spsProperty instanceof SpsGroupProperty && SpsDashboardWidget.isDashboard((SpsGroupProperty) spsProperty);
    }
}