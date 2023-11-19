/*
 * HasDependencyFeature.java
 *
 * Created on 08.10.2014 11:15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.dependency;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

/**
 * @author mdick
 */
public interface HasDependencyFeature {
    DependencyFeature getDependencyFeature();
    SpsWidget asSpsWidget();
}
