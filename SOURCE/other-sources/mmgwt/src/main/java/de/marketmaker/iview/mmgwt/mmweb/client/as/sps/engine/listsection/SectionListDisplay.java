/*
 * SectionListDisplay.java
 *
 * Created on 08.12.2014 07:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection;

import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;

import java.util.List;

/**
 * @author mdick
 */
public interface SectionListDisplay {
    void setPresenter(SectionListPresenter presenter);
    Panel getView();

    SectionListEntry createListEntry(SpsProperty p, int i);

    void addListEntry(SectionListEntry sectionListEntry);
    void addFooter(List<SpsWidget> footer);

    void removeListEntry(SectionListEntry sectionListEntry);

    void setAnimatedEntryIndex(int animatedEntryIndex);

    void setListEntryCaption(String caption);
    void setFooterCaption(String caption);
    void setAddButtonTooltip(String addButtonTooltip);
    void setDeleteButtonTooltip(String deleteButtonTooltip);
    void setReadonly(boolean readonly);
    void setVisibleForRendering(boolean visible);
    void setCollapsible(boolean collapsible);
    void setCollapsed(boolean collapsed);

    void onWidgetConfigured();

    void focusCollapsedTrigger();

    interface SectionListPresenter {
        void addProperty();
        void deleteProperty(SpsProperty p);
        void deleteAllAndAddProperty();

        List<SpsWidget> createAndAddWidgets(SpsProperty p);

        void fireAfterPropertiesSet();
        void fireChanged(SpsProperty p);

        void onCollapseClicked();
    }
}
