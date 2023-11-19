/*
 * AbstractSpsListBase.java
 *
 * Created on 23.04.2015 14:10
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;

/**
 * @author mdick
 */
public abstract class AbstractSpsListBase<W extends Widget, P extends SpsProperty> extends SpsBoundWidget<W, P> {
    protected final String keyField;
    protected final Context context;
    protected final SpsListBindFeature spsListBindFeature;

    public AbstractSpsListBase(Context context, BindToken parentToken, BindToken itemsBindToken, String keyField) {
        this.context = context;
        this.keyField = keyField;
        this.spsListBindFeature = new SpsListBindFeature(context, parentToken, itemsBindToken) {
            @Override
            public void onChange() {
                onPropertyChange();
            }
        };
    }

    protected SpsGroupProperty getEntry(String key) {
        for (SpsProperty spsProperty : this.spsListBindFeature.getSpsProperty().getChildren()) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            if (key.equals(((SpsLeafProperty) gp.get(this.keyField)).getStringValue())) {
                return gp;
            }
        }
        return null;
    }

    @Override
    public void release() {
        super.release();
        this.spsListBindFeature.release();
    }
}
