/*
 * NamedIdSet.java
 *
 * Created on 22.06.2007 13:09:18
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.domain.data;

import java.util.Set;
import java.util.List;
import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface NamedIdSet {
    String getId();

    String getName();

    /** @deprecated */
    Set<Long> getIds();

    Set<Long> getQids();

    Set<Long> getIids();

    String getName(long qid);

    String getMarketStrategy();

    String getConstituentsGroup();
}
