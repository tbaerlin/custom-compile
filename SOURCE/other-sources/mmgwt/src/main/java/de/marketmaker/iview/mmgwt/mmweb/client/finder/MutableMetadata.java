package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.Map;

import de.marketmaker.iview.dmxml.FinderMetaList;

/**
 * Created on 18.11.11 16:50
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface MutableMetadata extends FinderFormElement {
    public void updateMetadata(Map<String, FinderMetaList> map, boolean force);
}
