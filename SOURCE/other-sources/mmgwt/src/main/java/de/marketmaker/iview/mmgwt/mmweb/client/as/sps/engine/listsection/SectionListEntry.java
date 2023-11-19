/*
 * SectionListEntry.java
 *
 * Created on 05.12.2014 17:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection;

import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

import java.util.List;

/**
 * @author mdick
 */
public interface SectionListEntry {
    List<SpsWidget> getSpsWidgetList();
    void setDeleteVisible(boolean visible);
    void setCaptionNumber(int n);
    void clearWidgets();
    boolean focusFirst();
}
