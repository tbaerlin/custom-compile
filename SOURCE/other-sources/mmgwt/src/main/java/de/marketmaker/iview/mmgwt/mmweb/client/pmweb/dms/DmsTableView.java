package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms;

import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.table.AbstractRowMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers.DelegateRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.util.LinkListener;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentView;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ToolbarPopupPanel;
import de.marketmaker.iview.pmxml.DocumentMetadata;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;

/**
 * Created on 21.04.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class DmsTableView implements DmsDisplay, ContentView {
    private final SnippetTableWidget stw;
    private final DockLayoutPanel panel;
    private final DmsTablePageController controller;

    public DmsTableView(DmsTablePageController controller, final DmsPresenter dms) {
        this.controller = controller;
        this.stw = new SnippetTableWidget(new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.date(), 0.1f, TableCellRenderers.DATE_AND_TIME, "date"), // $NON-NLS$
                new TableColumn(I18n.I.name(), 0.2f, new DmsLinkRenderer(dms), "documentName"), // $NON-NLS$
                new TableColumn(I18n.I.type(), 0.15f, TableCellRenderers.STRING_LEFT, "type"), // $NON-NLS$
                new TableColumn(I18n.I.postbox(), 0.05f, new DelegateRenderer<>(PmRenderers.DMS_POSTBOX_YES_NO_RENDERER), "postbox"), // $NON-NLS$
                new TableColumn(I18n.I.postboxStatus(), 0.1f, new DelegateRenderer<>(PmRenderers.DMS_POSTBOX_TIMESTAMP_RENDERER), "postboxStatus"), // $NON-NLS$
                new TableColumn(I18n.I.user(), 0.1f, TableCellRenderers.STRING_RIGHT, "login"), // $NON-NLS$
                new TableColumn(I18n.I.comment(), 0.3f, new TableCellRenderers.MultiLineMaxLengthStringRenderer(2, 80, ""))
        }));
        this.stw.getElement().getStyle().setOverflowY(Style.Overflow.AUTO);
        this.stw.getElement().getStyle().setWidth(100, Style.Unit.PCT);
        this.stw.getElement().getStyle().setHeight(100, Style.Unit.PCT);

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config());
        controller.getPagingFeature().setPagingWidgets(pagingWidgets);
        this.panel = new DockLayoutPanel(Style.Unit.PX);
        final FloatingToolbar toolbar = createToolbar(controller.getConfig(), controller.getDefaultConfig());
        this.panel.addNorth(toolbar, toolbar.getToolbarHeightPixel());
        this.panel.addSouth(pagingWidgets.getToolbar(), 24); //toolbar height FOR_ICON_SIZE_UNDEF, so calling toolbar.getToolbarHeightPixel() is not possible
        this.panel.add(this.stw);
    }

    private FloatingToolbar createToolbar(final Presenter.Config config, final Presenter.Config defaultConfig) {
        final FloatingToolbar toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);
        final Button configButton = Button.icon("as-tool-settings") // $NON-NLS$
                .tooltip(I18n.I.settings())
                .build();
        configButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                showSettings(configButton, config, defaultConfig);
            }
        });
        toolbar.add(configButton);
        return toolbar;
    }

    @Override
    public void updateData(DMSSearchResult result) {
        if (result == null || result.getMetaData() == null) {
            this.stw.updateData(null);
            return;
        }
        final DefaultTableDataModel tdm = DefaultTableDataModel.create(result.getMetaData(),
                new AbstractRowMapper<DocumentMetadata>() {
                    @Override
                    public Object[] mapRow(DocumentMetadata dm) {
                        return new Object[]{
                                dm.getDateCreated(),
                                dm,
                                dm.getDocumentType(),
                                dm,
                                dm,
                                dm.getCreatedBy(),
                                dm.getComment()
                        };
                    }
                }
        ).withSort(result.getSort());
        this.stw.updateData(tdm);
    }

    public void setSortLinkListener(LinkListener<String> sortLinkListener) {
        this.stw.setSortLinkListener(sortLinkListener);
    }

    @Override
    public void layout() {
        //nothing to do
    }

    @Override
    public Widget getWidget() {
        return this.panel;
    }

    @Override
    public void onBeforeHide() {
        //nothing to do
    }

    private void showSettings(UIObject relativeTo, Presenter.Config config, Presenter.Config defaultConfig) {
        final FlowPanel panel = new FlowPanel();
        final PopupPanel popupPanel = new ToolbarPopupPanel(true);
        final TableSettings tableSettings = new TableSettings(config, defaultConfig);
        popupPanel.setWidget(panel);
        panel.add(tableSettings);
        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(10);
        table.setWidget(0, 0, Button.text(I18n.I.accept()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
                controller.update();
            }
        }).build());
        table.setWidget(0, 1, Button.text(I18n.I.reset()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                tableSettings.reset();
            }
        }).build());
        table.setWidget(0, 2, Button.text(I18n.I.cancel()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                popupPanel.hide();
            }
        }).build());
        panel.add(table);
        popupPanel.showRelativeTo(relativeTo);
    }
}