/*
 * IndicateStaleDataModificationCommand.java
 *
 * Created on 25.06.2015 14:19
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
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasStaleDataIndicator;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCompositeProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLabelWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
 * @author mdick
 */
public class IndicateStaleDataModificationCommand extends AbstractSpsWidgetModificationCommand {
    private HasStaleDataIndicator staleDataIndicator;
    private HandlerRegistration rootPropHandlerRegistration;

    public IndicateStaleDataModificationCommand() {
        super(false);
        initBindFeature(new BindFeature<SpsProperty>(this) {
            @Override
            public void setContextAndTokens(Context context, BindToken parentToken, BindToken bindToken) {
                super.setContextAndTokens(context, parentToken, bindToken);
                // listen for root prop changes if the bind is a list or a group
                // both do not fire change events in case one of the children changes, although their changed flag
                // is updated
                if(getSpsProperty() instanceof SpsCompositeProperty) {
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
    public void setWidget(SpsWidget targetWidget) {
        if(targetWidget instanceof HasStaleDataIndicator) {
            super.setWidget(targetWidget);
            this.staleDataIndicator = (HasStaleDataIndicator)targetWidget;
            return;
        }
        throw new IllegalArgumentException("expected a SPS widget that implements HasStaleDataIndicator. Given widget is of type " + (targetWidget != null ? targetWidget.getClass().getSimpleName() : "null" ));  // $NON-NLS$
    }

    @Override
    protected SpsLabelWidget getTargetWidget() {
        return (SpsLabelWidget)super.getTargetWidget();
    }

    @Override
    public void release() {
        super.release();
        if(this.rootPropHandlerRegistration != null) {
            this.rootPropHandlerRegistration.removeHandler();
        }
    }

    @Override
    public void execute() {
        this.staleDataIndicator.setStaleDataIndicator(getBindFeature().getSpsProperty().hasChanged());
    }
}
