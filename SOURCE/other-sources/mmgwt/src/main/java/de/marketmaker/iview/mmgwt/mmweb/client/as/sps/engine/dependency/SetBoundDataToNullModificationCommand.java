/*
 * SetBoundDataToNullModificationCommand.java
 *
 * Created on 26.06.2015 15:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCompositeProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLabelWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;

/**
 * @author mdick
 */
public class SetBoundDataToNullModificationCommand extends AbstractSpsWidgetModificationCommand {
    private SpsBoundWidget boundWidget;
    private HandlerRegistration rootPropHandlerRegistration;

    public SetBoundDataToNullModificationCommand() {
        super(false);
        initBindFeature(new BindFeature<SpsProperty>(this) {
            @Override
            public void setContextAndTokens(Context context, BindToken parentToken, BindToken bindToken) {
                super.setContextAndTokens(context, parentToken, bindToken);
                // listen for root prop changes if the bind is a list or a group
                // both do not fire change events in case one of the children changes, although their changed flag
                // is updated
                if (getSpsProperty() instanceof SpsCompositeProperty) {
                    rootPropHandlerRegistration = context.getRootProp().addChangeHandler(new ChangeHandler() {
                        @Override
                        public void onChange(ChangeEvent event) {
                            execute();
                        }
                    });
                }
            }
        });
    }

    @Override
    public void release() {
        super.release();
        if(this.rootPropHandlerRegistration != null) {
            this.rootPropHandlerRegistration.removeHandler();
        }
    }

    @Override
    public void setWidget(SpsWidget targetWidget) {
        if(!(targetWidget instanceof SpsBoundWidget)) {
            throw new IllegalArgumentException("expected a SPS widget that is a bound widget. Given widget is of type " + (targetWidget != null ? targetWidget.getClass().getSimpleName() : "null" ));  // $NON-NLS$
        }
        this.boundWidget = (SpsBoundWidget)targetWidget;
        super.setWidget(this.boundWidget);
    }

    @Override
    protected SpsLabelWidget getTargetWidget() {
        return (SpsLabelWidget)super.getTargetWidget();
    }

    @Override
    public void execute() {
        if (getBindFeature().getSpsProperty().hasChanged()) {
            if (this.boundWidget != null && this.boundWidget.getBindFeature().getSpsProperty() != null) {
                final SpsProperty spsProperty = this.boundWidget.getBindFeature().getSpsProperty();
                if (spsProperty instanceof SpsLeafProperty) {
                    ((SpsLeafProperty) spsProperty).setNullValue(true, true);
                } else {
                    DebugUtil.showDeveloperNotification("SetBoundDataToNullModificationCommand - expected bound property '" + spsProperty.getBindToken() + "' to be of type SpsLeafProperty, but was " + spsProperty.getClass().getSimpleName());  // $NON-NLS$
                }
            } else {
                DebugUtil.showDeveloperNotification("SetBoundDataToNullModificationCommand - bound widget is null or bound property is null" + (this.boundWidget != null ? ", descId: " + boundWidget.getDescId() : ""));  // $NON-NLS$
            }
        }
    }
}
