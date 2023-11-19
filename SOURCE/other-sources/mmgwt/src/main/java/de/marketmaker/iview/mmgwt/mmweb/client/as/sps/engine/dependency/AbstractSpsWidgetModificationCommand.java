/*
 * AbstractWidgetModificationCommand.java
 *
 * Created on 08.10.2014 14:45
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
* @author mdick
*/
public abstract class AbstractSpsWidgetModificationCommand implements SpsWidgetModificationCommand {
    private BindFeature<?> bindFeature;
    private SpsWidget targetWidget;

    public AbstractSpsWidgetModificationCommand() {
        this(true);
    }

    protected AbstractSpsWidgetModificationCommand(boolean initBindFeature) {
        if(initBindFeature) {
            initBindFeature(new BindFeature<>(this));
        }
    }

    protected void initBindFeature(BindFeature<? extends SpsProperty> bindFeature) {
        if(this.bindFeature != null) {
            throw new IllegalStateException("initBindFeature must be only called once");   // $NON-NLS$
        }
        this.bindFeature = bindFeature;
    }

    @Override
    public void setWidget(SpsWidget targetWidget) {
        this.targetWidget = targetWidget;
    }

    @Override
    public BindFeature getBindFeature() {
        return this.bindFeature;
    }

    @Override
    public void onPropertyChange() {
        if(this.targetWidget == null) {
            return;
        }
        execute();
    }

    @Override
    public void release() {
        if(this.bindFeature != null) {
            this.bindFeature.release();
        }
        this.targetWidget = null;
    }

    protected SpsWidget getTargetWidget() {
        return this.targetWidget;
    }
}
