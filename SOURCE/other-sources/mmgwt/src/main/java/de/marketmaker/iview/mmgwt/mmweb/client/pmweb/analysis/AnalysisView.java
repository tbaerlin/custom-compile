package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimpleLayoutPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.CompareUtil;
import de.marketmaker.itools.gwtutil.client.util.DOMUtil;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.Transitions;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.Tooltip;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.MainController;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ColumnHeaderEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.ColumnHeaderHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableRenderer;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.DTTableUtils;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmReportTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.async.ArchiveData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsContextPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.dms.DmsMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.ParameterConfigPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.ColumnFilterUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterData;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.FilterMetadata;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.widgets.dttablefilter.SingleColumnFilterPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasIsPrintable;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ProgressPanel;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SortOrderChooserWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.ToolbarPopupPanel;
import de.marketmaker.iview.pmxml.ActivityGadgetResult;
import de.marketmaker.iview.pmxml.DTCell;
import de.marketmaker.iview.pmxml.DTColumnSpec;
import de.marketmaker.iview.pmxml.DTRowGroup;
import de.marketmaker.iview.pmxml.DTSortSpec;
import de.marketmaker.iview.pmxml.DTTable;
import de.marketmaker.iview.pmxml.Language;
import de.marketmaker.iview.pmxml.LayoutDesc;
import de.marketmaker.iview.pmxml.internaltypes.DMSSearchResult;

/**
 * Created on 29.08.13 08:59
 * Copyright (c) market maker Software AG. All Rights Reserved.
 * @author Michael Lösch
 */

public class AnalysisView<P extends AnalysisDisplay.Presenter> extends Composite implements AnalysisDisplay<P>,
        HasIsPrintable, EvaluationController.View, RequiresResize {

    private static final int FADE_IN_MILLIS = 250;

    private static final String LANGUAGE_MENU_LANGUAGE_OBJECT_KEY = "data";// $NON-NLS$

    private static final String LANGUAGE_MENU_LANGUAGE_SHORT_KEY = "short";// $NON-NLS$

    private final DockLayoutPanel dockLayoutPanel = new DockLayoutPanel(Style.Unit.PX);

    private final SimpleLayoutPanel panel = new SimpleLayoutPanel();

    private Button configButton;

    private Button aggregationButton;

    private Button sortOrderButton;

    private Button xlsExportButton;

    private Button archiveButton;

    private Button dmsButton;

    private PagingWidgets chartPagingWidgets;

    protected PmReportTableWidget tableWidget;

    private static PopupPanel currentToolbarPopupPanel;

    private P presenter;

    private DmsDisplay.Presenter dmsPresenter;

    private long millisPopupClosed = 0;

    private Image chartImage = null;

    private ProgressPanel progressPanel = null;

    private boolean showProgressPanelCancelButton = true;

    private boolean showProgressPanelBackgroundButton = true;

    private ArchiveData.ContentType contentType = null;

    private boolean updatePrintButtonEnabled = true;

    private LayoutDesc layoutDesc;

    private String handle;

    private DTRowGroup originalTopLevelGroup;

    private DmsContextPopup dmsContextPopup;

    private final ArrayList<SingleColumnFilterPanel> columnFilterPanels = new ArrayList<>();

    private final Map<Integer, FilterData> currentColumnFilterData = new HashMap<>();

    private final ToolbarPopupPanel columnFilterPopup = new ToolbarPopupPanel();

    private final Menu languagesMenu = new Menu();

    private SelectButton languagesSelectButton;

    private class ElementAsUIObject extends UIObject {
        public ElementAsUIObject(Element element) {
            setElement(element);
        }
    }

    public AnalysisView() {
        this.dockLayoutPanel.setStyleName("mm-contentData pm-analysisView");
        final FloatingToolbar toolbar = createToolbar();
        this.dockLayoutPanel.addNorth(toolbar, toolbar.getToolbarHeightPixel());
        this.dockLayoutPanel.add(this.panel);

        initWidget(this.dockLayoutPanel);
    }

    @Override
    public void onResize() {
        this.dockLayoutPanel.onResize();
    }

    public static List<SortOrderChooserWidget.SortOrderChooserItem> toSortOrderChooserItems(List<DTSortSpec> sortSpecs) {
        final List<SortOrderChooserWidget.SortOrderChooserItem> items = new ArrayList<>();
        for (DTSortSpec sortSpec : sortSpecs) {
            final SortOrderChooserWidget.SortOrder sortOrder = sortSpec.isAscending() ?
                    SortOrderChooserWidget.SortOrder.ASCENDING : SortOrderChooserWidget.SortOrder.DESCENDING;
            items.add(new SortOrderChooserWidget.SortOrderChooserItem(sortSpec.getColIndex(), sortOrder));
        }
        return items;
    }

    @Override
    public void setPresenter(P presenter) {
        this.presenter = presenter;
        initTableWidget();
    }

    private void initTableWidget() {
        this.tableWidget = new PmReportTableWidget(this.presenter);
        this.tableWidget.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                if (!event.isAttached()) {
                    hideCurrentToolbarPopupPanel();
                }
            }
        });

        this.tableWidget.addColumnHeaderHandler(new ColumnHeaderHandler() {
            @Override
            public void onColumnHeader(ColumnHeaderEvent event) {
                switch (event.getWhat()) {
                    case SORT_CLICKED:
                        presenter.saveLayoutAndTableParams();
                        break;
                    case MOUSE_OVER:
                        tableWidget.setPopupHookVisible(event.getColumnIndex(), true);
                        break;
                    case MOUSE_OUT:
                        for (SingleColumnFilterPanel columnFilterPanel : columnFilterPanels) {
                            if (columnFilterPanel.getColumnIndex() == event.getColumnIndex()) {
                                tableWidget.setPopupHookVisible(event.getColumnIndex(),
                                        columnFilterPanel.isColumnFilterDefined());
                            }
                        }
                        break;
                    case POPUP_HOOK_CLICKED:
                        showFilterChooserPopup(event.getColumnIndex(), new ElementAsUIObject(event.getElement()));
                        break;
                }
            }
        });

        this.columnFilterPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> closeEvent) {
                // Check if the filter has changed. This check is necessary, because
                // if the popup panel hides automatically, no change event and no
                // value change event will be fired (the dom element is detached before the
                // event fires, which prevents the event from being fired at all).
                final SingleColumnFilterPanel panel = (SingleColumnFilterPanel) columnFilterPopup.getWidget();
                final FilterData filterData = currentColumnFilterData.get(panel.getColumnIndex());
                if (!CompareUtil.equals(filterData, panel.getFilterData())) {
                    onColumnFilterChange();
                }
            }
        });
    }

    private void createDmsView() {
        this.dmsContextPopup = new DmsContextPopup(this.dmsPresenter);
        this.dmsPresenter.addSearchResultListener(new DmsDisplay.Presenter.SearchResultListener() {
            @Override
            public void onSearchResult(DMSSearchResult response) {
                final boolean enabled = response.getMetaData() != null && !response.getMetaData().isEmpty();
                dmsButton.setEnabled(enabled);
                Tooltip.addQtip(dmsButton, (enabled ? I18n.I.dmsTooltip2(response.getMetaData().size()) : I18n.I.dmsTooltip1()));
            }
        });
    }

    @Override
    public P getPresenter() {
        return this.presenter;
    }

    private FloatingToolbar createToolbar() {
        final FloatingToolbar toolbar = new FloatingToolbar(FloatingToolbar.ToolbarHeight.FOR_ICON_SIZE_S);

        this.aggregationButton = Button.icon("as-tool-table-aggregation") // $NON-NLS$
                .tooltip(I18n.I.grouping())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        toggleAggregation();
                    }
                })
                .build();
        toolbar.add(this.aggregationButton);

        this.sortOrderButton = Button.icon("as-tool-sort") // $NON-NLS$
                .tooltip(I18n.I.sort())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (isReopenTime()) {
                            showSortOrderChooser();
                        }
                    }
                })
                .build();
        toolbar.add(this.sortOrderButton);

        this.configButton = Button.icon("as-tool-settings") // $NON-NLS$
                .tooltip(I18n.I.settings())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        if (isReopenTime()) {
                            AnalysisView.this.presenter.onShowReportSettings();
                        }
                    }
                })
                .build();
        toolbar.add(this.configButton);

        this.languagesSelectButton = new SelectButton()
                .withMenu(this.languagesMenu)
                .withClickOpensMenu()
                .withSelectionHandler(selectionEvent -> {
                    final Language language = (Language) selectionEvent.getSelectedItem().getData(LANGUAGE_MENU_LANGUAGE_OBJECT_KEY);
                    if (language == null) {
                        Firebug.debug("<AnalysisView.languagesSelectButton..selectionHandler> language is null");
                        return;
                    }
                    this.presenter.onLanguageSelected(language);
                });
        toolbar.add(this.languagesSelectButton);

        this.xlsExportButton = Button.icon("as-tool-export-xls") // $NON-NLS$
                .tooltip(I18n.I.exportAsXlsFile())
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent event) {
                        xlsExport();
                    }
                })
                .build();

        toolbar.addFill();

        if (withExcelExport()) {
            toolbar.add(this.xlsExportButton);
        }

        this.archiveButton = Button.icon("as-saveToArchive") // $NON-NLS$
                .tooltip(I18n.I.archiving())
                .clickHandler(new ClickHandler() {
                    public void onClick(ClickEvent event) {
                        presenter.onArchiveButtonClicked();
                    }
                })
                .build();
        this.archiveButton.setVisible(false);
        toolbar.add(this.archiveButton);

        this.dmsButton = Button.icon("as-tool-dms").clickHandler(new ClickHandler() { // $NON-NLS$
            @Override
            public void onClick(ClickEvent clickEvent) {
                dmsContextPopup.showRelativeTo(dmsButton);
            }
        }).build();
        this.dmsButton.setVisible(false);
        toolbar.add(this.dmsButton);

        this.chartPagingWidgets = new PagingWidgets(new PagingWidgets.Config().withToolbar(toolbar));

        this.aggregationButton.setEnabled(true);
        this.sortOrderButton.setEnabled(true);
        this.configButton.setEnabled(false);
        this.xlsExportButton.setEnabled(true);

        return toolbar;
    }

    private void xlsExport() {
        if (this.handle != null) {
            this.presenter.xlsExport(this.handle);
        }
    }

    @Override
    public void showReportSettings(Map<String, String> analysisParametersOfMetadataForm, Widget triggerWidget) {
        if (hideCurrentToolbarPopupPanel()) {
            return;
        }
        final ParameterConfigPopup configPopup = new ParameterConfigPopup(this.layoutDesc, analysisParametersOfMetadataForm,
                new ParameterConfigPopup.Apply() {
                    @Override
                    public void onApply(HashMap<String, String> params) {
                        AnalysisView.this.presenter.setAnalysisParametersOfMetadataForm(params);
                    }
                });
        showToolbarPopupPanelRelativeTo(configPopup.getPopupPanel(), this.configButton);
    }

    private void showSortOrderChooser() {
        if (hideCurrentToolbarPopupPanel()) {
            return;
        }

        final DTTable dtTable = this.tableWidget.getDTTable();
        final DTTableRenderer.Options options = this.tableWidget.getDTTableRendererOptions();
        final PmReportTableWidget.RowCount rowCount = this.tableWidget.getRowCount();

        if (dtTable == null || options == null) {
            return;
        }

        final List<DTSortSpec> sortSpecs;
        if (options.isWithCustomSort()) {
            sortSpecs = options.getCustomSort();
        }
        else {
            sortSpecs = dtTable.getSortSpecs();
        }

        final List<SortOrderChooserWidget.SortOrderChooserItem> items = toSortOrderChooserItems(sortSpecs);

        final List<SortOrderChooserWidget.SortOrderChooserColumn> columns = new ArrayList<>();
        final List<DTColumnSpec> columnSpecs = dtTable.getColumnSpecs();
        for (int i = 0; i < columnSpecs.size(); i++) {
            final String columnCaption = DTTableUtils.stripOffBlanksAndLineBreaks(columnSpecs.get(i).getCaption());
            final String columnIndex = Integer.toString(i);
            columns.add(new SortOrderChooserWidget.SortOrderChooserColumn(columnCaption, columnIndex));
        }

        final FlowPanel panel = new FlowPanel();
        final PopupPanel popupPanel = new ToolbarPopupPanel(true);
        popupPanel.setWidget(panel);

        final SortOrderChooserWidget sortOrderChooserWidget = new SortOrderChooserWidget(columns, items);
        panel.add(sortOrderChooserWidget);

        final FlexTable table = new FlexTable();
        table.setCellPadding(0);
        table.setCellSpacing(10);
        table.setWidget(0, 0, Button.text(I18n.I.accept()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hideToolbarPopupPanel(popupPanel);
                updateTableWidget(sortOrderChooserWidget, dtTable, options, rowCount);
            }
        }).build());
        table.setWidget(0, 1, Button.text(I18n.I.reset()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                sortOrderChooserWidget.setItems(toSortOrderChooserItems(dtTable.getSortSpecs()));
            }
        }).build());
        table.setWidget(0, 2, Button.text(I18n.I.cancel()).clickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hideToolbarPopupPanel(popupPanel);
            }
        }).build());
        panel.add(table);
        showToolbarPopupPanelRelativeTo(popupPanel, this.sortOrderButton);
    }

    private void showFilterChooserPopup(int columnIndex, UIObject column) {
        if (hideCurrentToolbarPopupPanel()) {
            return;
        }

        for (SingleColumnFilterPanel columnFilterPanel : this.columnFilterPanels) {
            if (columnIndex == columnFilterPanel.getColumnIndex()) {
                this.columnFilterPopup.clear();
                this.columnFilterPopup.add(columnFilterPanel);
                showToolbarPopupPanelRelativeTo(this.columnFilterPopup, column);
                break;
            }
        }
    }

    public void onColumnFilterChange() {
        //TODO: move to controller?
        final DTTable dtTable = this.tableWidget.getDTTable();
        final DTTableRenderer.Options options = this.tableWidget.getDTTableRendererOptions();
        final boolean prefiltered = filterDtTable(dtTable);
        final PmReportTableWidget.RowCount rowCount = countRows(dtTable, prefiltered);

        updateTable(dtTable, options.withPrefiltered(prefiltered), rowCount);
    }

    private boolean filterDtTable(DTTable dtTable) {
        final DTTableRenderer.ColumnFilter filter = ColumnFilterUtil.createColumnFilter(this.columnFilterPanels);

        final Map<Integer, FilterData> data = new HashMap<>();
        for (SingleColumnFilterPanel columnFilterPanel : this.columnFilterPanels) {
            data.put(columnFilterPanel.getColumnIndex(), columnFilterPanel.getFilterData());
        }
        this.currentColumnFilterData.clear();
        this.currentColumnFilterData.putAll(data);
        this.presenter.setFilterData(data);
        this.presenter.saveLayoutAndTableParams();

        final long start = System.currentTimeMillis();
        dtTable.setToplevelGroup(DTTableUtils.getFilteredCopy(this.originalTopLevelGroup, filter));
        Firebug.debug("<AnalysisView filter changed> filtering took " + (System.currentTimeMillis() - start) + "ms");

        return filter != null;
    }

    private PmReportTableWidget.RowCount countRows(DTTable dtTable, boolean hasFilter) {
        final long start = System.currentTimeMillis();
        final int filteredRowCount;
        final int totalRowCount;

        if (hasFilter) {
            final DTTableUtils.RowCountCollector rowCountCollector = new DTTableUtils.RowCountCollector();
            DTTableUtils.collect(dtTable, rowCountCollector);
            filteredRowCount = rowCountCollector.getCount();
        }
        else {
            filteredRowCount = -1;
        }

        final DTTableUtils.RowCountCollector rowCountCollector = new DTTableUtils.RowCountCollector();
        DTTableUtils.collect(this.originalTopLevelGroup, rowCountCollector);
        totalRowCount = rowCountCollector.getCount();

        Firebug.debug("<AnalysisView.countRows> took " + (System.currentTimeMillis() - start) + "ms");

        return new PmReportTableWidget.RowCount(totalRowCount, filteredRowCount);
    }

    private void showToolbarPopupPanelRelativeTo(PopupPanel popupPanel, UIObject relativeTo) {
        currentToolbarPopupPanel = popupPanel;
        popupPanel.addCloseHandler(new CloseHandler<PopupPanel>() {
            @Override
            public void onClose(CloseEvent<PopupPanel> event) {
                currentToolbarPopupPanel = null;
                millisPopupClosed = System.currentTimeMillis();
            }
        });
        DOMUtil.setTopZIndex(popupPanel);
        popupPanel.showRelativeTo(relativeTo);
    }

    private boolean isReopenTime() {
        return System.currentTimeMillis() - this.millisPopupClosed > 500;
    }

    private void hideToolbarPopupPanel(PopupPanel popupPanel) {
        currentToolbarPopupPanel = null;
        popupPanel.hide();
    }

    private boolean hideCurrentToolbarPopupPanel() {
        if (currentToolbarPopupPanel != null) {
            currentToolbarPopupPanel.hide();
            currentToolbarPopupPanel = null;
            return true;
        }
        return false;
    }

    private void updateTableWidget(SortOrderChooserWidget w, DTTable dtTable, DTTableRenderer.Options options,
            PmReportTableWidget.RowCount rowCount) {
        final ArrayList<DTSortSpec> dtSortSpecs = new ArrayList<>(w.getResultItems().size());

        for (SortOrderChooserWidget.SortOrderChooserItem item : w.getResultItems()) {
            final DTSortSpec dtSortSpec = new DTSortSpec();
            dtSortSpec.setColIndex(item.getColumnValue());
            dtSortSpec.setAscending(SortOrderChooserWidget.SortOrder.ASCENDING.equals(item.getSortOrder()));
            dtSortSpecs.add(dtSortSpec);
        }
        options.withCustomSort(dtSortSpecs);

        updateTable(dtTable, options, rowCount);
    }

    private void updateTable(DTTable dtTable, DTTableRenderer.Options options, PmReportTableWidget.RowCount rowCount) {
        updateTable(dtTable, options, null, rowCount);
    }

    private void updateTable(DTTable dtTable, DTTableRenderer.Options options, Integer indexOfMaximizedDiagram,
            PmReportTableWidget.RowCount rowCount) {
        this.tableWidget.update(dtTable, options, indexOfMaximizedDiagram, getHistoryContextName(), rowCount);
        show(this.tableWidget);
        for (SingleColumnFilterPanel columnFilterPanel : this.columnFilterPanels) {
            this.tableWidget.setPopupHookVisible(columnFilterPanel.getColumnIndex(),
                    columnFilterPanel.isColumnFilterDefined());
        }
    }

    protected String getHistoryContextName() {
        return this.layoutDesc.getLayout().getLayoutName();
    }

    private void toggleAggregation() {
        hideCurrentToolbarPopupPanel();

        final DTTableRenderer.Options options = this.tableWidget.getDTTableRendererOptions();
        final DTTable dtTable = this.tableWidget.getDTTable();
        final PmReportTableWidget.RowCount rowCount = this.tableWidget.getRowCount();

        final boolean toggledValue = !options.isWithAggregations();
        options.withAggregations(toggledValue);
        this.aggregationButton.setActive(toggledValue);

        updateTable(dtTable, options, rowCount);
    }

    @Override
    public void clear() {
        show(new Label());
        setLanguageButtonVisible(false);
        hideDmsButtons();
    }

    private void show(Widget widget) {
        this.panel.setWidget(widget);
    }

    @Override
    public void showGadget(ActivityGadgetResult gadgetResult, LayoutDesc layoutDesc, String handle) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void showTable(DTTable table, DTTableRenderer.Options options, Map<Integer, FilterData> filterData, LayoutDesc layoutDescEx,
            Integer maxDiagramIdx, String handle) {
        this.layoutDesc = layoutDescEx;
        this.handle = handle;

        updatePrintButton(ArchiveData.ContentType.TABLE);

        hideCurrentToolbarPopupPanel();

        this.aggregationButton.setActive(options.isWithAggregations());

        applyFilters(table, options, filterData);
        assertCustomSort(table, options);
        final PmReportTableWidget.RowCount rowCount = countRows(table, options.isPrefiltered());
        updateTable(table, options, maxDiagramIdx != null
                ? maxDiagramIdx
                : DTTableUtils.getInitialDiagramIndex(table), rowCount);
    }

    private void assertCustomSort(DTTable table, DTTableRenderer.Options options) {
        if (!options.isWithCustomSort() || options.getCustomSort().isEmpty()) {
            return;
        }

        final List<DTSortSpec> sortSpecs = options.getCustomSort();
        final int columns = table.getColumnSpecs().size();

        final ArrayList<DTSortSpec> toDrop = new ArrayList<>(options.getCustomSort().size());

        for (DTSortSpec sortSpec : sortSpecs) {
            try {
                final int columnIndex = Integer.parseInt(sortSpec.getColIndex());
                if (columnIndex >= columns) {
                    toDrop.add(sortSpec);
                    Firebug.info("<AnalysisView.assertCustomSort> dropping sort spec with column index " + columnIndex);
                }
            } catch (Exception nfe) {
                final String colIndex = sortSpec != null ? sortSpec.getColIndex() : "";
                Firebug.warn("<AnalysisView.assertCustomSort> failed to parse colIndex '" + colIndex + "' of DTSortSpec");
            }
        }
        sortSpecs.removeAll(toDrop);
    }

    public void applyFilters(DTTable table, DTTableRenderer.Options options, Map<Integer, FilterData> filterData) {
        //todo: move building the filter metadata and initial filtering to controller?
        final List<FilterMetadata<DTCell>> filterMetadata = ColumnFilterUtil.buildFilterMetadataFromColumnSpecs(table.getColumnSpecs());

        this.originalTopLevelGroup = table.getToplevelGroup();

        this.columnFilterPanels.clear();

        if (filterMetadata != null) {
            final ChangeHandler filterChangeHandler = new ChangeHandler() {
                @Override
                public void onChange(ChangeEvent event) {
                    onColumnFilterChange();
                }
            };

            // TODO: should be checked by the controller by comparing layout GUIDs?
            // check if filterData fits for filterMetadata, if not, clear filter data
            boolean hasFilter = false;

            // create filter panels if necessary and apply filterData
            final List<DTColumnSpec> columnSpecs = table.getColumnSpecs();
            FilterMetadata<DTCell> dummy = new FilterMetadata<>();

            final Comparator<FilterMetadata<DTCell>> filterMetadataComparator = new Comparator<FilterMetadata<DTCell>>() {
                @Override
                public int compare(FilterMetadata<DTCell> o1, FilterMetadata<DTCell> o2) {
                    return Integer.compare(o1.getColumnIndex(), o2.getColumnIndex());
                }
            };

            for (int i = 0; i < columnSpecs.size(); i++) {
                dummy.setColumnIndex(i);

                final int foundFilterPosition = Collections.binarySearch(filterMetadata, dummy, filterMetadataComparator);

                if (foundFilterPosition >= 0) {
                    final SingleColumnFilterPanel columnFilterPanel = new SingleColumnFilterPanel(i, columnSpecs.get(i));
                    columnFilterPanel.setFilterChangeHandler(filterChangeHandler);
                    this.columnFilterPanels.add(columnFilterPanel);

                    final FilterMetadata<DTCell> metadata = filterMetadata.get(foundFilterPosition);
                    columnFilterPanel.setFilterMetadata(metadata);
                    if (filterData != null) {
                        columnFilterPanel.setFilterData(filterData.get(metadata.getColumnIndex()));
                        hasFilter = true;
                    }
                }
            }

            final boolean prefiltered = hasFilter && filterDtTable(table);
            options.withPrefiltered(prefiltered);
        }
        else {
            options.withPrefiltered(false);
        }
        //TODO: should we release columnFilterPanels?
    }

    @Override
    public void showError(LayoutDesc layoutDescEx, String... messages) {
        this.layoutDesc = layoutDescEx;
        this.handle = null;

        hideCurrentToolbarPopupPanel();

        final FlowPanel panel = new FlowPanel();
        for (final String message : messages) {
            panel.add(new Label(message));
        }
        show(new ScrollPanel(panel));

        updatePrintButton(null);
    }

    @Override
    public void showChart(String uri, LayoutDesc layoutDescEx, String handle) {
        this.layoutDesc = layoutDescEx;
        this.handle = handle;

        updatePrintButton(ArchiveData.ContentType.CHART);

        hideCurrentToolbarPopupPanel();

        ActionPerformedEvent.fire("X_PM_CHART"); // $NON-NLS-0$
        this.chartImage = new Image(uri);
        final ScrollPanel panel = new ScrollPanel(this.chartImage);
        panel.setStyleName("pm-layout-frame");
        show(panel);
    }

    private Widget createObject(String uri) {

// todo: JS - implement
//        if (PhoneGapUtil.isPhoneGap()) {
//            final HTML html = new HTML("<iframe src=\"" + PhoneGapUtil.getPdfViewerUrl(uri) +"\" width=\"100%\" height=\"100%\" />"); // $NON-NLS$
//            html.setWidth("100%"); // $NON-NLS$
//            html.setHeight("100%"); // $NON-NLS$
//            return html;
//        }

        final HTML html = new HTML("<object type=\"application/pdf\" data=\"" + uri + "\"></object>"); // $NON-NLS$
        html.setStyleName("pm-layout-frame");
        html.setWidth("100%"); // $NON-NLS$
        html.setHeight("100%"); // $NON-NLS$

        BrowserSpecific.INSTANCE.fixDivBehindPdfObjectBugPart2(html);

        return html;
    }

    @Override
    public void showPdf(String uri, LayoutDesc layoutDescEx, String handle) {
        this.layoutDesc = layoutDescEx;
        this.handle = handle;
        updatePrintButton(ArchiveData.ContentType.PDF);

        hideCurrentToolbarPopupPanel();

        ActionPerformedEvent.fire("X_PM_PDF"); // $NON-NLS-0$

        show(iOsDevice() ? createLinkAndObject(uri, layoutDescEx) : createObject(uri));
        this.archiveButton.setEnabled(true);
    }

    private static native boolean iOsDevice() /*-{
        return (navigator.userAgent.indexOf('iPad') > -1) || (navigator.userAgent.indexOf('iPhone') > -1);
    }-*/;

    private Widget createLinkAndObject(String uri, LayoutDesc layoutDescEx) {
        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<div class=\"external-tool-header\">"); // $NON-NLS$
        sb.appendEscaped(layoutDescEx.getLayout().getLayoutName());
        sb.appendHtmlConstant("</div><div class=\"external-tool-text\">"); // $NON-NLS$
        sb.appendEscaped(I18n.I.reportIOS());
        sb.appendHtmlConstant("<br/><br/>");  // $NON-NLS$
        sb.appendHtmlConstant("<a href=\"");  // $NON-NLS$
        sb.appendHtmlConstant(uri);
        sb.appendHtmlConstant("\" target=\"_blank\"");  // $NON-NLS$
        sb.appendHtmlConstant("\" class=\"mm-simpleLink\">");   // $NON-NLS$
        sb.appendHtmlConstant("<div class=\"").appendHtmlConstant("mm-desktopIcon-pdf").appendHtmlConstant("\"></div>"); // $NON-NLS$
        sb.appendEscaped(layoutDescEx.getLayout().getLayoutName());
        sb.appendHtmlConstant("</a>"); // $NON-NLS$
        sb.appendHtmlConstant("</div>");  // $NON-NLS$
        sb.appendHtmlConstant("<div class=\"pm-layout-frame-ios\">");  // $NON-NLS$
        sb.appendHtmlConstant("<object type=\"application/pdf\" data=\"" + uri + "\"></object>"); // $NON-NLS$
        sb.appendHtmlConstant("</div>"); // $NON-NLS$
        return new HTML(sb.toSafeHtml());
    }

    @Override
    public void setSortOrderButtonVisible(boolean visible) {
        this.sortOrderButton.setVisible(visible);
    }

    @Override
    public void setAggregationButtonVisible(boolean visible) {
        this.aggregationButton.setVisible(visible);
    }

    @Override
    public void setXlsExportButtonVisible(boolean visible) {
        this.xlsExportButton.setVisible(visible);
    }

    @Override
    public void hideDmsButtons() {
        this.archiveButton.setVisible(false);
        this.dmsButton.setVisible(false);
    }

    @Override
    public void setDmsArchivButtonVisible(boolean visible) {
        this.archiveButton.setVisible(visible);
    }

    @Override
    public void setDmsPopupButtonVisible(boolean visible) {
        this.dmsButton.setVisible(visible);
        Tooltip.addQtip(this.dmsButton, visible ? I18n.I.dmsTooltip1() : null);
    }

    @Override
    public void setConfigButtonEnabled(boolean enabled) {
        this.configButton.setEnabled(enabled);
    }

    @Override
    public void setConfigButtonVisible(boolean visible) {
        this.configButton.setVisible(visible);
    }

    @Override
    public void setUpdatePrintButtonEnabled(boolean enabled) {
        this.updatePrintButtonEnabled = enabled;
    }

    private void updatePrintButton(ArchiveData.ContentType contentType) {
        this.contentType = contentType;

        if (!this.updatePrintButtonEnabled) {
            return;
        }

        if (contentType == null) {
            AbstractMainController.INSTANCE.getView().getTopToolbar().setPrintButtonEnabled(false);
            return;
        }

        switch (contentType) {
            case TABLE:
            case CHART:
                AbstractMainController.INSTANCE.getView().getTopToolbar().setPrintButtonEnabled(true);
                break;
            default:
                AbstractMainController.INSTANCE.getView().getTopToolbar().setPrintButtonEnabled(false);
        }
    }

    @Override
    public boolean isPrintable() {
        return this.updatePrintButtonEnabled && (this.contentType == ArchiveData.ContentType.TABLE
                || this.contentType == ArchiveData.ContentType.CHART);
    }

    @Override
    public String getPrintHtml() {
        if (!this.updatePrintButtonEnabled || ArchiveData.ContentType.PDF == this.contentType) {
            return I18n.I.notPrintable();
        }

        final SafeHtmlBuilder sb = new SafeHtmlBuilder();
        sb.appendHtmlConstant("<div class=\"as-contentHeader\">") //$NON-NLS$
                .append(MainController.INSTANCE.getView().getContentHeader())
                .appendHtmlConstant("</div>"); //$NON-NLS$

        switch (this.contentType) {
            case TABLE:
                sb.appendHtmlConstant(this.tableWidget.getPrintHtml());
                break;

            case CHART:
                sb.appendHtmlConstant(new SimplePanel(new Image(this.chartImage.getUrl())).getElement().getInnerHTML());
                break;

            default:
                sb.appendEscaped(I18n.I.notPrintable());
        }
        return sb.toSafeHtml().asString();
    }

    @Override
    public void setDmsPresenter(DmsDisplay.Presenter dmsPresenter) {
        this.dmsPresenter = dmsPresenter;
        if (dmsPresenter != null) {
            createDmsView();
        }
    }

    @Override
    public void showArchiveDialog(final DmsMetadata metadata) {
        if (!StringUtil.hasText(metadata.getIdentifier())) {
            Dialog.error(I18n.I.dmsErrorNoIdentifier());
            return;
        }
        ArchiveDialog.show(new ArchiveDialog.Save() {
            @Override
            public void save(String title, String comment, String layoutType) {
                if (handle != null) {
                    presenter.archive(layoutDesc.getLayout().getLayoutName(), handle, layoutType, title, comment, metadata);
                }
            }
        }, metadata, this.layoutDesc);
    }

    @Override
    public void setLanguages(List<Language> languages) {
        this.languagesMenu.removeAll(false);
        for (Language language : languages) {
            this.languagesMenu.add(new MenuItem(language.getLanguageLong())
                    .withData(LANGUAGE_MENU_LANGUAGE_OBJECT_KEY, language)
                    .withData(LANGUAGE_MENU_LANGUAGE_SHORT_KEY, language.getLanguageShort()));
        }
    }

    @Override
    public void setSelectedLanguage(Language language) {
        Firebug.debug("<setSelectedLanguage> Language is "+(language == null ? "null":language.getLanguageLong()));
        if (language == null) {
            return;
        }
        this.languagesSelectButton.setSelectedData(LANGUAGE_MENU_LANGUAGE_SHORT_KEY, language.getLanguageShort());
    }

    @Override
    public void setChartPagingWidgetsVisible(boolean visible) {
        this.chartPagingWidgets.setVisible(visible);
    }

    @Override
    public PagingWidgets getChartPagingWidgets() {
        return this.chartPagingWidgets;
    }

    @Override
    public void setLanguageButtonVisible(boolean visible) {
      this.languagesSelectButton.setVisible(visible);
    }

    @Override
    public void updateAsyncHandleMarker(String asyncHandle) {
        if (asyncHandle == null) {
            getElement().removeAttribute(ASYNC_HANDLE_MARKER);
            return;
        }
        getElement().setAttribute(ASYNC_HANDLE_MARKER, asyncHandle);
    }

    @Override
    public void startProgress(String header, String progressText) {
        this.progressPanel = new ProgressPanel(this.showProgressPanelBackgroundButton, this.showProgressPanelCancelButton);
        this.progressPanel.addCancelClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.cancelAsyncAndCloseSession();
            }
        });
        this.progressPanel.addToBackgroundClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                presenter.sendAsyncToBackground();
                progressPanel.showSentToBackground();
            }
        });
        this.progressPanel.setProgress(progressText);
        Transitions.fadeInAfterAttach(this.progressPanel, FADE_IN_MILLIS, "ease", 1000); // $NON-NLS$
        show(progressPanel);
    }

    public void setProgress(String progressText) {
        this.progressPanel.setProgress(progressText);
    }

    @Override
    public void setProgress(String progressText, int progress) {
        if (progress == -1) {
            return;
        }
        this.progressPanel.setProgress(progressText, progress);
    }

    private static boolean withExcelExport() {
        return Customer.INSTANCE.isCustomerAS() && Customer.INSTANCE.asCustomerAS().isXlsExportForTableLayouts();
    }

    @Override
    public Widget getContentPanel() {
        return this.panel;
    }

    @Override
    public void hideProgressPanelCancelButton() {
        this.showProgressPanelCancelButton = false;
    }

    @Override
    public void setProgressPanelBackgroundButtonVisible(boolean visible) {
        this.showProgressPanelBackgroundButton = visible;
    }
}