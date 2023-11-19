/*
 * EdgDataProvider.java
 *
 * Created on 19.04.2010 11:59:41
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */
package de.marketmaker.istar.merger.provider.certificatedata;

import de.marketmaker.istar.domain.data.EdgData;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public interface EdgDataProvider {
    EdgData getEdgData(long instrumentid);
}
