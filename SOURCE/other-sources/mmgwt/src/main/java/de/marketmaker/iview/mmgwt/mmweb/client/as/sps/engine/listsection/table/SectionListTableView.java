/*
 * SectionListTableView.java
 *
 * Created on 08.12.2014 10:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.table;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.IconImageIcon;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SectionListDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SectionListEntry;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection.SpsListSectionUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;

import java.util.ArrayList;
import java.util.List;

/**
 * @author mdick
 */
public class SectionListTableView implements SectionListDisplay {
    public static final String COLLAPSED_STYLE = "sps-collapsed";  // $NON-NLS$
    public static final String EXPANDED_STYLE = "sps-expanded";  // $NON-NLS$

    private SectionListPresenter presenter;
    private final SimplePanel panel = new SimplePanel();
    private final FlowPanel collapsibleContainer = new FlowPanel();
    private final FlexTable flexTable = new FlexTable();
    private final FlowPanel bottomButtonPanel;

    private boolean readonly;
    private String addButtonTooltip;
    private String deleteButtonTooltip;
    private final ImageButton collapseButton;

    private ArrayList<SectionListTableViewEntry> entries = new ArrayList<>();

    public SectionListTableView() {
        this.collapseButton = new ImageButton(IconImage.get("expander-down").createImage(), null, IconImage.get("expander-right").createImage());  // $NON-NLS$
        this.collapseButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                presenter.onCollapseClicked();
            }
        });
        this.collapseButton.addStyleName("sps-listSection-collapseButton");

        this.collapsibleContainer.add(this.collapseButton);

        this.panel.setStyleName("sps-listSection");
        this.panel.setWidget(this.collapsibleContainer);
        this.collapsibleContainer.setStyleName("sps-listSection-collapsibleContainer");
        this.flexTable.addStyleName("sps-listSection-tableContent");

        this.collapsibleContainer.add(this.flexTable);

        this.bottomButtonPanel = new FlowPanel();
        this.bottomButtonPanel.setStyleName("sps-listSection-bottom");
        this.collapsibleContainer.add(this.bottomButtonPanel);
    }

    @Override
    public void setPresenter(SectionListPresenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public SimplePanel getView() {
        return this.panel;
    }

    @Override
    public SectionListEntry createListEntry(SpsProperty p, int i) {
        return new SectionListTableViewEntry(this, p);
    }

    @Override
    public void addListEntry(SectionListEntry sectionListEntry) {
        if(sectionListEntry instanceof SectionListTableViewEntry) {
            addSectionListTableViewEntry((SectionListTableViewEntry) sectionListEntry);
        }
    }

    @Override
    public void addFooter(List<SpsWidget> footerWidgets) {
        if(footerWidgets == null) {
            return;
        }

        for (SpsWidget spsWidget : footerWidgets) {
            spsWidget.asWidgets();
        }

        final int rowCount = this.entries.size() + 1;
        int i = 0;
        for (SpsWidget spsWidget : footerWidgets) {
            this.flexTable.setWidget(rowCount, i++, spsWidget.getInfoIconPanel());
            this.flexTable.setWidget(rowCount, i++, spsWidget.getWidget());
        }
    }

    private void addSectionListTableViewEntry(SectionListTableViewEntry entry) {
        final FlexTable.FlexCellFormatter flexCellFormatter = this.flexTable.getFlexCellFormatter();
        final int entriesSize = this.entries.size();
        if(entriesSize == 0) {
            int i = 0;
            for (SpsWidget spsWidget : entry.getSpsWidgetList()) {
                flexCellFormatter.setColSpan(0, i, 2);
                this.flexTable.setWidget(0, i, new HTML(spsWidget.getCaptionWidget().getHTML()));
                i += 1;
            }
        }

        int row = indexToRow(entriesSize);
        int i = 0;
        for (SpsWidget spsWidget : entry.getSpsWidgetList()) {
            this.flexTable.setWidget(row, i++, spsWidget.getInfoIconPanel());
            this.flexTable.setWidget(row, i++, spsWidget.getWidget());
        }
        final IconImageIcon deleteButton = entry.getDeleteButton();
        if(deleteButton != null) {
            this.flexTable.setWidget(row, i, deleteButton);
        }
        this.entries.add(entry);
    }

    private int indexToRow(int entriesSize) {
        return entriesSize + 1;
    }

    @Override
    public void removeListEntry(SectionListEntry sectionListEntry) {
        if(sectionListEntry instanceof SectionListTableViewEntry) {
            removeSectionListTableViewEntry((SectionListTableViewEntry) sectionListEntry);
        }
    }

    private void removeSectionListTableViewEntry(SectionListTableViewEntry entry) {
        final int index = this.entries.indexOf(entry);
        if(index < 0) {
            throw new IllegalStateException("<SectionListTableView> table row does not exist");  // $NON-NLS$
        }
        this.entries.remove(index);
        this.flexTable.removeRow(indexToRow(index));
    }

    @Override
    public void setAnimatedEntryIndex(int animatedEntryIndex) {
        //not required here
    }

    @Override
    public void setListEntryCaption(String caption) {
        //not required here
    }

    @Override
    public void setFooterCaption(String caption) {
        //not required here
    }

    @Override
    public void setAddButtonTooltip(String addButtonTooltip) {
        this.addButtonTooltip = addButtonTooltip;
    }

    @Override
    public void setDeleteButtonTooltip(String deleteButtonTooltip) {
        this.deleteButtonTooltip = deleteButtonTooltip;
    }

    @Override
    public void setReadonly(boolean readonly) {
        this.readonly = readonly;
    }

    @Override
    public void setVisibleForRendering(boolean visible) {
        this.collapsibleContainer.setVisible(visible);
    }

    @Override
    public void setCollapsible(boolean collapsible) {
        this.collapseButton.setVisible(collapsible);
    }


    @Override
    public void setCollapsed(boolean collapsed) {
        int height = 0;
        try {
            final Element element = this.flexTable.getRowFormatter().getElement(0); // the header of the list table should exist
            final int offsetTop = element.getOffsetTop();
            final int clientHeight = element.getOffsetHeight();

            height = offsetTop + clientHeight;

            Firebug.debug("<" + getClass().getSimpleName() + "> offsetTop:" + offsetTop + ", clientHeight: " + clientHeight + ", height: " + height);
        }
        catch(Exception e) {
            Firebug.error("<" + getClass().getSimpleName() + "> failed to determine height of header. Applying default " + height + " (possibly overridden by CSS rules)", e);
        }

        /*
         * Collapsible/collapsed require the following additional styles via CSS to work properly.
         * if collapsed, the panel should have:
         *   overflow-y: hidden;
         *   min-height: 36px; in case of an exception (see above); adjust the min-height to your actual style.
         *
         * generally, the panel should have:
         *   position:relative
         *
         * The collapsibleButton should be positioned via CSS.
         */

        if(collapsed) {
            this.collapsibleContainer.removeStyleName(EXPANDED_STYLE);
            this.collapsibleContainer.addStyleName(COLLAPSED_STYLE);
            this.collapsibleContainer.getElement().getStyle().setHeight(height, Style.Unit.PX);
        }
        else {
            this.collapsibleContainer.removeStyleName(COLLAPSED_STYLE);
            this.collapsibleContainer.addStyleName(EXPANDED_STYLE);
            this.collapsibleContainer.getElement().getStyle().clearHeight();
        }

        this.collapseButton.setActive(collapsed);
    }

    @Override
    public void onWidgetConfigured() {
        if (this.readonly) {
            this.bottomButtonPanel.setVisible(false);
            return;
        }

        this.bottomButtonPanel.add(SpsListSectionUtil.createAddButton(this.addButtonTooltip)
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
        this.collapseButton.setFocus(true);
    }

    String getDeleteButtonTooltip() {
        return deleteButtonTooltip;
    }

    boolean isReadonly() {
        return readonly;
    }

    void fireAfterPropertiesSet() {
        this.presenter.fireAfterPropertiesSet();
    }

    void fireChanged(SpsProperty p) {
        this.presenter.fireChanged(p);
    }

    void createAndAddWidgets(SpsProperty p, List<SpsWidget> spsWidgets) {
        final List<SpsWidget> widgets = this.presenter.createAndAddWidgets(p);

        for (SpsWidget spsWidget : widgets) {
            spsWidgets.add(spsWidget);
            spsWidget.asWidgets();
        }
    }

    void deleteProperty(SpsProperty property) {
        this.presenter.deleteProperty(property);
    }
}
