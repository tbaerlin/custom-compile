/*
 * HasCreateColumnFilter.java
 *
 * Created on 24.03.2015 09:53
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter;

import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;

/**
 * @author mdick
 */
public interface HasCreateColumnFilter {
    DTTableRenderer.ColumnFilter createColumnFilter();
    boolean isColumnFilterDefined();
}
