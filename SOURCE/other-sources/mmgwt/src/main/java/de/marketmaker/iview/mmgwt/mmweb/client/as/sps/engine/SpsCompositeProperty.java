package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.Collection;

/**
 * Created on 26.03.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public interface SpsCompositeProperty {
    Collection<SpsProperty> getChildren();
    SpsProperty get(BindKey bindKey);
    BindToken getBindToken();
}
