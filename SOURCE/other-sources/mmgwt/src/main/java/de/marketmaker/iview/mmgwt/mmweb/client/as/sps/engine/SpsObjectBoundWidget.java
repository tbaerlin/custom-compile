/*
 * SpsObjectBoundWidget.java
 *
 * Created on 28.07.2014 09:55
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author mdick
 */
public abstract class SpsObjectBoundWidget<W extends Widget, P extends SpsProperty, O extends SpsProperty> extends SpsBoundWidget<W, P>{
    private BindFeature<O> objectBindFeature;

    public void release() {
        super.release();
        this.objectBindFeature.release();
    }

    protected SpsObjectBoundWidget() {
        this.objectBindFeature = new BindFeature<>(new HasBindFeature() {
            @Override
            public BindFeature getBindFeature() {
                return objectBindFeature;
            }

            @Override
            public void onPropertyChange() {
                onObjectPropertyChange();
            }
        });
    }

    public BindFeature<O> getObjectBindFeature() {
        return this.objectBindFeature;
    }

    public abstract void onObjectPropertyChange();
}
