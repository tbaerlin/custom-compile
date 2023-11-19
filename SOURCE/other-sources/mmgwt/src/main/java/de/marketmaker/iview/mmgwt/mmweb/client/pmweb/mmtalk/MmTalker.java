package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk;

import de.marketmaker.iview.pmxml.MMTalkResponse;
import de.marketmaker.iview.pmxml.ObjectQuery;
import de.marketmaker.iview.pmxml.TableTreeTable;

import java.util.List;

/**
 * Created on 18.12.12 08:58
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public interface MmTalker<T extends ObjectQuery, O> {
    T getQuery();
    O createResultObject(MMTalkResponse response);
}