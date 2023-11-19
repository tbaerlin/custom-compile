/*
 * RestrictedWMDataImpl.java
 *
 * Created on 10/31/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider;

import de.marketmaker.istar.domain.data.WMData;
import de.marketmaker.istar.domain.profile.Profile;
import de.marketmaker.istar.domain.profile.Selector;
import de.marketmaker.istar.merger.context.RequestContextHolder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Access restriction of wm data fields.
 *
 * @author Stefan Willenbrock
 */
public class RestrictedWMDataImpl implements WMData {

    private final WMData delegate;

    private final Profile profile;

    private final Collection<Field> fields;

    public RestrictedWMDataImpl(WMData wmData) {
        this.delegate = wmData;
        this.profile = RequestContextHolder.getRequestContext().getProfile();
        this.fields = createFields();
    }

    private Collection<Field> createFields() {
        if (profile == null) {
            return this.delegate.getFields();
        }
        final List<Field> result = new ArrayList<>();
        for (Field field : this.delegate.getFields()) {
            if (isAllowed(field.getName())) {
                result.add(field);
            }
        }
        return Collections.unmodifiableList(result);
    }

    private boolean isAllowed(String field) {
        return !"GD873F".equals(field) || profile.isAllowed(Selector.DZ_BANK_USER);
    }

    @Override
    public long getInstrumentid() {
        return this.delegate.getInstrumentid();
    }

    @Override
    public Collection<Field> getFields() {
        return this.fields;
    }

    @Override
    public Field getField(String name) {
        if (isAllowed(name)) {
            return this.delegate.getField(name);
        }
        return null;
    }
}
