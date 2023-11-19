package de.marketmaker.iview.mmgwt.mmweb.client.as.navigation;

import de.marketmaker.iview.mmgwt.mmweb.client.view.NavItemSpec;

/**
 * Created on 29.05.13 12:02
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface NavTreeItemIdentifier<I extends Item> {
    NavTreeItemIdentifier<NavItemSpec> NIS_IDENTIFIER = new NavTreeItemIdentifier<NavItemSpec>() {
        @Override
        public boolean hasId(NavItemSpec nis, String id) {
            return !(id == null || nis == null) && id.equals(nis.getId());
        }
    };

    boolean hasId(I i, String id);
}
