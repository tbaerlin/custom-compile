/*
 * SectionListPanelView.java
 *
 * Created on 08.12.2014 08:06
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.panel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SectionListDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SectionListEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SpsListSectionUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.List;

/**
* @author mdick
*/
public class SectionListPanelView implements SectionListDisplay {
    private SectionListPresenter presenter;
    private final FlowPanel panel = new FlowPanel();
    private final FlowPanel entriesPanel = new FlowPanel();
    private final FlowPanel footerPanel = new FlowPanel();
    private final FlowPanel bottomButtonPanel;

    private int animatedEntryIndex = -1;
    private String listEntryCaption;
    private boolean readonly;
    private String addButtonTooltip;
    private String deleteButtonTooltip;

    private FlowPanel footerEntryPanel;
    private FlowPanel footerContentPanel;

    private String footerCaption;

    public SectionListPanelView() {
        this.panel.setStyleName("sps-listSection");
        this.panel.add(this.entriesPanel);
        this.panel.add(this.footerPanel);
        this.footerPanel.setStyleName("sps-listSection-footer");

        this.bottomButtonPanel = new FlowPanel();
        this.bottomButtonPanel.setStyleName("sps-listSection-bottom");
        this.panel.add(this.bottomButtonPanel);
    }

    @Override
    public void onWidgetConfigured() {
        createFooterEntry();

        if (this.readonly) {
            return;
        }

        final IconImageIcon icon = SpsListSectionUtil.createAddButton(this.addButtonTooltip);
        this.bottomButtonPanel.add(icon
                .withClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        TaskViewPanel.requestScrollDown();
                        presenter.addProperty();
                    }
                }));
        this.bottomButtonPanel.add(new InlineHTML("&nbsp;")); // $NON-NLS$

        this.bottomButtonPanel.add(SpsListSectionUtil.createDeleteAllButton(null)
                        .withClickHandler(new ClickHandler() {
                            @Override
                            public void onClick(ClickEvent clickEvent) {
                                Dialog.confirm(I18n.I.deleteAllEntries(), I18n.I.deleteAllEntriesConfirmation(), new Command() {
                                    @Override
                                    public void execute() {
                                        presenter.deleteAllAndAddProperty();
                                    }
                                });
                            }
                        })
        );
        this.bottomButtonPanel.add(new InlineHTML("&nbsp;")); // $NON-NLS$
    }

    @Override
    public void focusCollapsedTrigger() {
        //do nothing
    }

    @Override
    public void setPresenter(SectionListPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public SectionListEntry createListEntry(SpsProperty p, int i) {
        return new SectionListPanelViewEntry(this, p, i == this.animatedEntryIndex, i + 1);
    }

    @Override
    public void addListEntry(SectionListEntry sectionListEntry) {
        if(sectionListEntry instanceof SectionListPanelViewEntry) {
            this.entriesPanel.add(((SectionListPanelViewEntry) sectionListEntry).getEntryPanel());
        }
    }

    @Override
    public void addFooter(List<SpsWidget> footerWidgets) {
        final boolean hasFooter = footerWidgets != null && !footerWidgets.isEmpty();
        this.footerPanel.setVisible(hasFooter);
        if(!hasFooter) {
            return;
        }

        this.footerContentPanel.clear();
        for (SpsWidget spsWidget : footerWidgets) {
            for (Widget widget : spsWidget.asWidgets()) {
                this.footerContentPanel.add(widget);
            }
        }

        //should trigger animation on attach, but does not work
        this.footerPanel.remove(this.footerEntryPanel);
        this.footerPanel.add(this.footerEntryPanel);
    }

    private void createFooterEntry() {
        this.footerEntryPanel = new FlowPanel();
        this.footerEntryPanel.setStyleName("sps-listSection-entry");

        if(StringUtil.hasText(this.footerCaption)) {
            final FlowPanel headerPanel = new FlowPanel();
            headerPanel.setStyleName("sps-section-header");
            Widget caption = new InlineHTML(this.footerCaption);
            headerPanel.add(caption);
            this.footerEntryPanel.add(headerPanel);
        }

        this.footerContentPanel = new FlowPanel();
        this.footerContentPanel.setStyleName("sps-listSection-entryContent");
        this.footerEntryPanel.add(this.footerContentPanel);
    }

    @Override
    public void removeListEntry(SectionListEntry sectionListEntry) {
        if(sectionListEntry instanceof SectionListPanelViewEntry) {
            this.entriesPanel.remove(((SectionListPanelViewEntry) sectionListEntry).getEntryPanel());
        }
    }

    @Override
    public void setAnimatedEntryIndex(int animatedEntryIndex) {
        this.animatedEntryIndex = animatedEntryIndex;
    }

    @Override
    public void setListEntryCaption(String caption) {
        this.listEntryCaption = caption;
    }

    @Override
    public void setFooterCaption(String caption) {
        this.footerCaption = caption;
    }

    @Override
    public void setAddButtonTooltip(String addButtonTooltip) {
        this.addButtonTooltip = addButtonTooltip;
    }

    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public void setVisibleForRendering(boolean visible) {
        this.panel.setVisible(visible);
    }

    @Override
    public void setCollapsible(boolean collapsible) {
        if(collapsible) {
            DebugUtil.showDeveloperNotification("Collapsible is not supported by SectionList in panel layout mode");
        }
    }

    @Override
    public void setCollapsed(boolean collapsed) {
        //nothing to do
    }

    boolean isReadonly() {
        return this.readonly;
    }

    public String getListEntryCaption() {
        return this.listEntryCaption;
    }

    private String getListEntryCaption(int index) {
        return getListEntryCaption(getListEntryCaption(), index);
    }

    private static String getListEntryCaption(String raw, int index) {
        if (StringUtil.hasText(raw)) {
            return raw.replace("$#", Integer.toString(index));
        }
        return null;
    }

    @Override
    public FlowPanel getView() {
        return this.panel;
    }

    void createAndAddWidgets(SpsProperty p, List<SpsWidget> spsWidgets, FlowPanel entryContentPanel) {
        final List<SpsWidget> widgets = this.presenter.createAndAddWidgets(p);
        for (SpsWidget spsWidget : widgets) {
            spsWidgets.add(spsWidget);
            for (Widget widget : spsWidget.asWidgets()) {
                entryContentPanel.add(widget);
            }
        }
    }

    public void fireAfterPropertiesSet() {
        this.presenter.fireAfterPropertiesSet();
    }

    public void fireChanged(SpsProperty p) {
        this.presenter.fireChanged(p);
    }

    public boolean hasListEntryCaption() {
        return StringUtil.hasText(this.listEntryCaption);
    }

    SafeHtml getCaptionText(int n) {
        return this.hasListEntryCaption()
                ? SafeHtmlUtils.fromString(this.getListEntryCaption(n))
                : SafeHtmlUtils.fromTrustedString(Integer.toString(n));
    }

    public void deleteProperty(SpsProperty p) {
        this.presenter.deleteProperty(p);
    }

    @Override
    public void setDeleteButtonTooltip(String deleteButtonTooltip) {
        this.deleteButtonTooltip = deleteButtonTooltip;
    }

    String getDeleteButtonTooltip() {
        return this.deleteButtonTooltip;
    }
}
