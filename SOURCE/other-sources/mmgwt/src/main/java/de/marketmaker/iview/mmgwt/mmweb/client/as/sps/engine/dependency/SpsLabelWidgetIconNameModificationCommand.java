/*
 * SpsLabelWidgetIconNameModificationCommand.java
 *
 * Created on 23.06.2015 11:13
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLabelWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
* @author mdick
*/
public class SpsLabelWidgetIconNameModificationCommand extends AbstractSpsWidgetModificationCommand {
    private final String iconName;

    public SpsLabelWidgetIconNameModificationCommand(String iconName) {
        this.iconName = iconName;
    }

    @Override
    public void setWidget(SpsWidget targetWidget) {
        if(targetWidget instanceof SpsLabelWidget && ((SpsLabelWidget) targetWidget).getIconNameBindFeature() != null) {
            super.setWidget(targetWidget);
            return;
        }
        throw new IllegalArgumentException("expected SpsLabelWidget but was " + (targetWidget != null ? targetWidget.getClass().getSimpleName() : "null" ));  // $NON-NLS$
    }

    @Override
    protected SpsLabelWidget getTargetWidget() {
        return (SpsLabelWidget)super.getTargetWidget();
    }

    @Override
    public void execute() {
        if(!getBindFeature().getSpsProperty().hasChanged()) {
            return;
        }
        getTargetWidget().setIconName(this.iconName);
    }
}
