/*
 * ProgressControlFactory.java
 *
 * Created on 24.08.2015 14:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.factory;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProgressControlWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.pmxml.ProgressControlDesc;

/**
 * @author mdick
 */
public class ProgressControlFactory extends Factory<ProgressControlDesc> {
    public ProgressControlFactory(String baseClass) {
        super(baseClass);
    }

    @Override
    SpsWidget doCreateSpsWidget(ProgressControlDesc widgetDesc, Context context, BindToken parentToken) {
        final SpsWidget widget = new SpsProgressControlWidget();
        widget.setCaption(widgetDesc.getCaption());
        return widget;
    }
}
