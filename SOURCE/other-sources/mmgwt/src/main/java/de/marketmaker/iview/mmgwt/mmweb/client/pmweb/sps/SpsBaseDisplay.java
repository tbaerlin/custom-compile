/*
 * SpsBaseDisplay.java
 *
 * Created on 27.03.2015 12:47
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.user.client.ui.Widget;

/**
 * @author mdick
 */
public interface SpsBaseDisplay {
    void setWidgets(Widget[] widgets);
    void setNorthWidget(Widget northWidget);
    void layoutNorthWidget();
}
