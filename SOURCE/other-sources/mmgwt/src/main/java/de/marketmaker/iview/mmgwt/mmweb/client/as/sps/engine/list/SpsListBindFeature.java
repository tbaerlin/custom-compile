package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.list;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.BindToken;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasBindFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;

/**
 * Created on 09.05.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public abstract class SpsListBindFeature implements SpsAfterPropertiesSetHandler {
    private final Context context;
    private final BindToken parentToken;
    private final BindToken itemsBindToken;
    private BindFeature<SpsListProperty> bindFeature;

    private boolean released = false;

    public SpsListBindFeature(final Context context, final BindToken parentToken, final BindToken itemsBindToken) {
        this.context = context;
        this.parentToken = parentToken;
        this.itemsBindToken = itemsBindToken;
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);
    }

    public void release() {
        assert !this.released : "already released!";
        assert this.bindFeature != null : "bind feature not initialized!";

        this.released = true;
        this.bindFeature.release();
    }

    @Override
    public void afterPropertiesSet() {
        this.bindFeature = new BindFeature<>(new HasBindFeature() {
            @Override
            public BindFeature getBindFeature() {
                return bindFeature;
            }

            @Override
            public void onPropertyChange() {
                onChange();
            }
        });
        this.bindFeature.setContextAndTokens(this.context, this.parentToken, this.itemsBindToken);
        onChange();
    }

    public boolean isPropertySet() {
        return this.bindFeature != null;
    }

    public SpsListProperty getSpsProperty() {
        if (this.bindFeature == null) {
            throw new IllegalStateException("bindFeature has not been created yet. can't return property."); // $NON-NLS$
        }
        return this.bindFeature.getSpsProperty();
    }

    public abstract void onChange();

    public SpsGroupProperty getProperty(String keyField, String key) {
        if (!isPropertySet()) {
            return null;
        }
        for (SpsProperty spsProperty : getSpsProperty().getChildren()) {
            if (!(spsProperty instanceof SpsGroupProperty)) {
                throw new IllegalStateException("not group property: " + spsProperty.getBindToken()); // $NON-NLS$
            }
            final SpsGroupProperty gp = (SpsGroupProperty) spsProperty;
            if (CompareUtil.equals(key, ((SpsLeafProperty) gp.get(keyField)).getStringValue())) { // TODO: use key with type DataItem???
                return gp;
            }
        }
        return null;
    }
}
