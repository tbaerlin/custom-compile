/*
 * PermissionProvider.java
 *
 * Created on 06.03.2003 11:35:51
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.profile;

import java.util.Collection;

/**
 * Able to provide information about permissions of certain types.
 *
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface PermissionProvider {
    /**
     * Returns an identifier that allows to identify the origin of the permissions
     * @return identifier
     */
    String getName();

    /**
     * Returns a collection of Strings that represent permissions of the given type
     * @param type requested type
     * @return collection of permission-Strings, may be empty.
     */
    Collection<String> getPermissions(PermissionType type);
}
