/*
 * SpsListActions.java
 *
 * Created on 14.04.2014 12:18
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine;

import java.util.List;

/**
 * @author Markus Dick
 */
public interface SpsListActions {
    void deleteProperty(SpsProperty property);
    void addProperty(boolean indicateChanged);
    void deleteAllAndAddProperty();
    List<SpsWidget> createWidgets(SpsProperty p, int level);
    List<SpsWidget> createFooterWidgets(int level);
    SpsProperty createDefaultCompareProperty();
}
