/*
 * SimplePermissionProvider.java
 *
 * Created on 20.02.13 16:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.istar.domainimpl.profile;

import java.util.Collection;
import java.util.Collections;

import de.marketmaker.istar.domain.profile.PermissionProvider;
import de.marketmaker.istar.domain.profile.PermissionType;

/**
 * @author oflege
 */
public class SimplePermissionProvider implements PermissionProvider {

    private final String name;

    private final PermissionType type;

    private final Collection<String> permissions;

    public SimplePermissionProvider(String name, PermissionType type, Collection<String> permissions) {
        this.name = name;
        this.type = type;
        this.permissions = permissions;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public Collection<String> getPermissions(PermissionType type) {
        if (type == this.type) {
            return this.permissions;
        }
        return Collections.emptyList();
    }
}
