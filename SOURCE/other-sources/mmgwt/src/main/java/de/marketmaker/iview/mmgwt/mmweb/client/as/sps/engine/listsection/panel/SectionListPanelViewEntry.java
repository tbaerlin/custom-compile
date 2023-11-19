/*
 * SectionListPanelViewEntry.java
 *
 * Created on 08.12.2014 08:07
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import de.marketmaker.itools.gwtutil.client.util.Transitions;
import de.marketmaker.itools.gwtutil.client.widgets.WipePanel;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SectionListEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SpsListSectionUtil;

import java.util.ArrayList;
import java.util.List;

import static de.marketmaker.itools.gwtutil.client.widgets.WipePanel.FixedPosition.TOP;

/**
* @author mdick
*/
class SectionListPanelViewEntry implements SectionListEntry {
    private final SectionListPanelView parent;
    private final List<SpsWidget> spsWidgetList = new ArrayList<>();
    private final WipePanel entryPanel;
    private final InlineHTML caption;
    private final IconImageIcon deleteButton;

    SectionListPanelViewEntry(SectionListPanelView parent, final SpsProperty p, boolean animatedEntry, int captionNumber) {
        this.parent = parent;
        this.entryPanel = new WipePanel(TOP, animatedEntry, 1000);
        this.entryPanel.setStyleName("sps-listSection-entry");

        final FlowPanel headerPanel = new FlowPanel();
        headerPanel.setStyleName("sps-section-header");

        this.caption = new InlineHTML(parent.getCaptionText(captionNumber));
        headerPanel.add(this.caption);

        if (!parent.isReadonly()) {
            this.deleteButton = createDeleteButton(p);
            headerPanel.add(this.deleteButton);
        }
        else {
            this.deleteButton = null;
        }

        this.entryPanel.add(headerPanel);

        final FlowPanel entryContentPanel = new FlowPanel();
        entryContentPanel.setStyleName("sps-listSection-entryContent");
        parent.createAndAddWidgets(p, this.spsWidgetList, entryContentPanel);
        this.entryPanel.add(entryContentPanel);

        parent.fireAfterPropertiesSet();
        parent.fireChanged(p);
    }

    private IconImageIcon createDeleteButton(final SpsProperty p) {
        return SpsListSectionUtil.createDeleteButton(this.parent.getDeleteButtonTooltip())
                .withClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        entryPanel.wipeOut(new Transitions.TransitionEndCallback() {
                            @Override
                            public void onTransitionEnd() {
                                parent.deleteProperty(p);
                            }
                        });
                    }
                });
    }

    @Override
    public List<SpsWidget> getSpsWidgetList() {
        return spsWidgetList;
    }

    @Override
    public void setDeleteVisible(boolean visible) {
        if (this.deleteButton == null) {
            return;
        }
        this.deleteButton.setVisible(visible);
    }

    @Override
    public void setCaptionNumber(int n) {
        this.caption.setHTML(parent.getCaptionText(n));
    }

    public WipePanel getEntryPanel() {
        return entryPanel;
    }

    @Override
    public void clearWidgets() {
        this.spsWidgetList.clear();
    }

    @Override
    public boolean focusFirst() {
        return SpsListSectionUtil.focusFirst(this.spsWidgetList);
    }
}
