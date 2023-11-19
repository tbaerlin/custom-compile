/*
 * ListOverviewProvider.java
 *
 * Created on 16.07.2008 13:07:09
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.listoverview;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import de.marketmaker.iview.dmxml.ListOverviewListItem;
import de.marketmaker.iview.dmxml.ListOverviewType;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface ListOverviewProvider {
    /**
     * @deprecated
     */
    ListOverviewListItem getListDefinition(String id);
    ListOverviewListItem getListDefinition(String id, String variant);
    ListOverviewListItem getListDefinition(String id, List<Locale> locales, String variant);

    Collection<String> getListIds();

    /**
     * @deprecated
     */
    ListOverviewType getStructure();
    ListOverviewType getStructure(String variant);
    ListOverviewType getStructure(List<Locale> locales, String variant);
}
