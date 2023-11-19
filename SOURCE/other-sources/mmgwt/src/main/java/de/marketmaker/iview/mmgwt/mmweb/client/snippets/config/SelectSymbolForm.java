/*
 * SelectSymbolForm.java
 *
 * Created on 05.05.2008 14:15:52
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.config;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.util.WidgetUtil;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.itools.gwtutil.client.widgets.menu.Menu;
import de.marketmaker.itools.gwtutil.client.widgets.menu.MenuItem;
import de.marketmaker.itools.gwtutil.client.widgets.menu.SelectButton;
import de.marketmaker.itools.gwtutil.client.widgets.notification.NotificationMessage;
import de.marketmaker.itools.gwtutil.client.widgets.notification.Notifications;
import de.marketmaker.iview.mmgwt.mmweb.client.Activatable;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.paging.PagingWidgets;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetTableWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.config.SnippetConfigurationView.SymbolParameterType;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.DefaultTableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.SimpleVisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRendererAdapter;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableCellRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumn;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.VisibilityCheck;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;
import de.marketmaker.iview.mmgwt.mmweb.client.view.IndexedViewSelectionModel;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ViewSpec;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SimpleGlassableLayoutPanel;

import java.util.Map;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SelectSymbolForm extends ContentPanel implements Activatable, HasCancellablePendingRequests, HasFocusWidget {
    public static final String PRESET_SEARCH_STRING = "presetSearchString"; //$NON-NLS$

    private final Button buttonSearch;
    private final FloatingToolbar toolbar;

    private static class MyTableCellRenderer extends TableCellRendererAdapter {
        private int num;
        private int check;

        public void render(Object data, StringBuffer sb, Context context) {
            boolean disabled = data instanceof Boolean && Boolean.FALSE.equals(data);
            sb.append("<input type=\"radio\" name=\"symSelect\""); // $NON-NLS$
            if (disabled) {
                sb.append("disabled=\"disabled\" ");  // $NON-NLS$
                if (this.check >= 0) {
                    this.check++;
                }
            }

            if (this.num++ == this.check) {
                this.check = -1;
                sb.append(" checked>"); // $NON-NLS$
            }
            else {
                sb.append(">"); // $NON-NLS$
            }
        }

        void reset() {
            this.num = 0;
            this.check = 0;
        }
    }

    private final SelectSymbolFormControllerInterface c;

    private final Map<String, String> params;

    protected final MyTableCellRenderer renderer = new MyTableCellRenderer();

    private SnippetTableWidget tw;

    private SimpleGlassableLayoutPanel sglp;

    private final TextBox searchTextField;

    private final SelectButton typeMenu = new SelectButton();

    private MenuItem[] typeMenuItems;

    private int nextPageCounter = 0;

    private static final int MAX_NEXT_PAGE = 30;

    private TableColumnModel tableColumnModel = null;

    private final SnippetConfigurationView.SymbolParameterType symbolParameterType;

    public static SelectSymbolForm create(Map<String, String> params,
                                          String[] types,
                                          String filterForUnderlyingsForType,
                                          Boolean filterForUnderlyingsOfLeveragProducts,
                                          SymbolParameterType symbolParameterType) {
        return new SelectSymbolForm(params,
                types,
                filterForUnderlyingsForType,
                filterForUnderlyingsOfLeveragProducts,
                new SelectSymbolController(),
                symbolParameterType,
                true
        );
    }

    public static SelectSymbolForm create(Map<String, String> params,
                                          String[] types,
                                          String filterForUnderlyingsForType,
                                          Boolean filterForUnderlyingsOfLeveragProducts,
                                          SelectSymbolFormControllerInterface controller,
                                          SymbolParameterType symbolParameterType,
                                          boolean showQuoteDataColumns) {
        return new SelectSymbolForm(params,
                types,
                filterForUnderlyingsForType,
                filterForUnderlyingsOfLeveragProducts,
                controller,
                symbolParameterType,
                showQuoteDataColumns);
    }

    public static SelectSymbolForm create(Map<String, String> params, String[] types, SymbolParameterType symbolParameterType) {
        return new SelectSymbolForm(params, types, null, null, new SelectSymbolController(), symbolParameterType, true);
    }

    public static SelectSymbolForm create(Map<String, String> params, SymbolParameterType symbolParameterType) {
        return new SelectSymbolForm(params, null, null, null, new SelectSymbolController(), symbolParameterType, true);
    }

    public static SelectSymbolForm createCert(Map<String, String> params, String category, SymbolParameterType symbolParameterType) {
        return new SelectSymbolForm(params,
                category != null ? new String[]{category} : null,
                null, null, new SelectCertSymbolController(), symbolParameterType, true);
    }

    /**
     * in case of filterForUnderlyingsForType==CER: i) null: search for all underlyings,
     * ii) false: search for non-leverage product underlyings,
     * iii) true: only underlyings for leverage products of customer/issuer DZ BANK are relevant
     */
    protected SelectSymbolForm(Map<String, String> params,
                               String[] types,
                               final String filterForUnderlyingsForType,
                               final Boolean filterForUnderlyingsOfLeveragProducts,
                               final SelectSymbolFormControllerInterface c,
                               SymbolParameterType symbolParameterType,
                               boolean showQuoteDataColumns) {
        this.symbolParameterType = symbolParameterType;
        this.c = c;
        this.c.setView(this);
        setHeaderVisible(false);
        addStyleName("mm-contentData"); // $NON-NLS-0$

        setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);

        if (types != null) {
            this.c.setTypes(types);
            this.c.setWithMsc(false);
        }

        this.params = params;

        this.tableColumnModel = createTableColumnModel(showQuoteDataColumns);

        this.tw = new SnippetTableWidget(this.tableColumnModel)
                .surroundedBy("<form id=\"symSelectForm\">", "</form>"); // $NON-NLS$
        this.tw.setWidth("100%"); // $NON-NLS-0$
        this.sglp = new SimpleGlassableLayoutPanel();
        this.sglp.addGlassClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Notifications.add(I18n.I.hint(), I18n.I.pleaseWaitWhileLoadingData()).requestStateDelayed(NotificationMessage.State.DELETED, 5);
            }
        });
        this.sglp.setGlassStyleName("as-reloadGlassPanel"); // $NON-NLS$
        this.sglp.setWidget(this.tw);
        this.sglp.setHeight("100%"); // $NON-NLS$
        this.sglp.setWidth("100%"); // $NON-NLS$
        add(this.sglp);
//        add(this.tw);

        this.toolbar = new FloatingToolbar();
        this.toolbar.add(new Label(getSearchLabel()));
        this.toolbar.addEmpty("4px"); // $NON-NLS$
        this.searchTextField = new TextBox();
        this.searchTextField.addKeyDownHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent e) {
                if (e.getNativeEvent().getKeyCode() == KeyCodes.KEY_ENTER) {
                    search(SelectSymbolForm.this.searchTextField.getValue());
                }
            }
        });
        this.searchTextField.addAttachHandler(new AttachEvent.Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent e) {
                if (e.isAttached()) {
                    WidgetUtil.deferredSetFocus(searchTextField);
                }
            }
        });
        this.toolbar.add(this.searchTextField);

        this.buttonSearch = Button.icon("mm-icon-finder") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        search(SelectSymbolForm.this.searchTextField.getValue());
                    }
                })
                .build();
        this.toolbar.add(this.buttonSearch);
        if (StringUtil.hasText(filterForUnderlyingsForType)) {
            final CheckBox underlyingCb = new CheckBox();
            underlyingCb.addListener(Events.OnClick, new Listener<BaseEvent>() {
                public void handleEvent(BaseEvent be) {
                    if (underlyingCb.getValue()) {
                        if ("CER".equals(filterForUnderlyingsForType)) { // $NON-NLS$
                            c.setFilterForUnderlyingsOfLeverageProducts(filterForUnderlyingsOfLeveragProducts, false);
                        }
                        c.setFilterForUnderlyingsForType(filterForUnderlyingsForType, true);
                    }
                    else {
                        c.setFilterForUnderlyingsForType(null, true);
                    }
                }
            });
            underlyingCb.addStyleName("mm-topToolbar-underlyingCheckbox");
            underlyingCb.setBoxLabel(I18n.I.underlyingsOnly());
            underlyingCb.setValue(true);
            c.setFilterForUnderlyingsForType(filterForUnderlyingsForType);
            c.setFilterForUnderlyingsOfLeverageProducts(filterForUnderlyingsOfLeveragProducts, false);
            this.toolbar.add(underlyingCb);
        }

        this.toolbar.addFill();

        this.typeMenu.withMenu(fillTypeMenu(new Menu()))
                .withClickOpensMenu()
                .withSelectionHandler(new SelectionHandler<MenuItem>() {
                    @Override
                    public void onSelection(SelectionEvent<MenuItem> e) {
                        final MenuItem selectedItem = e.getSelectedItem();
                        if (selectedItem != null) {
                            c.getIndexedViewSelectionModel().selectView((Integer) selectedItem.getData("itemIndex")); // $NON-NLS$

                        }
                    }
                });
        this.typeMenu.setEnabled(false);
        this.toolbar.add(this.typeMenu);

        setTopComponent(this.toolbar);

        final PagingWidgets pagingWidgets = new PagingWidgets(new PagingWidgets.Config());
        setBottomComponent(pagingWidgets.getToolbar());

        this.c.getPagingFeature().setPagingWidgets(pagingWidgets);

        addListener(Events.Render, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent baseEvent) {
                WidgetUtil.deferredSetFocus(SelectSymbolForm.this.searchTextField);
            }
        });
        enableSearchControls();
    }

    public void setToolbarVisible(boolean visible) {
        this.toolbar.setVisible(visible);
    }

    protected String getSearchLabel() {
        return I18n.I.searchStringSymbols() + "  ";
    }

    protected DefaultTableColumnModel createTableColumnModel(boolean showQuoteDataColumns) {

        final VisibilityCheck showWknCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowWkn());
        final VisibilityCheck showIsinCheck = SimpleVisibilityCheck.valueOf(SessionData.INSTANCE.isShowIsin());
        final VisibilityCheck showQuoteDataCheck = SimpleVisibilityCheck.valueOf(showQuoteDataColumns);

        return new DefaultTableColumnModel(new TableColumn[]{
                new TableColumn(I18n.I.option(), 0.05f).withRenderer(this.renderer)
                , new TableColumn(I18n.I.name(), 0.6f).withRenderer(new TableCellRenderers.QuoteNameRenderer(50, "-"))  // $NON-NLS-0$
                , new TableColumn("ISIN", 0.1f).withRenderer(TableCellRenderers.STRING_CENTER).withVisibilityCheck(showIsinCheck) // $NON-NLS-0$
                , new TableColumn("WKN", 0.1f).withRenderer(TableCellRenderers.STRING_CENTER).withVisibilityCheck(showWknCheck) // $NON-NLS-0$
                , new TableColumn(I18n.I.currency(), 0.1f).withRenderer(TableCellRenderers.STRING_CENTER).withVisibilityCheck(showQuoteDataCheck)
                , new TableColumn(I18n.I.marketName(), 0.1f).withRenderer(TableCellRenderers.STRING_10).withVisibilityCheck(showQuoteDataCheck)
        });
    }

    class MyFormPanel extends FormPanel {
        @Override
        protected void onAttach() {
            super.onAttach();
        }
    }

    private void search(String q) {
        if (q == null || q.trim().length() == 0) {
            return;
        }
        this.nextPageCounter = 0;
        c.search(q.trim());
        disableSearchControls();
    }

    public void activate() {
        final String presetSearchString = this.params.get(PRESET_SEARCH_STRING);
        if (StringUtil.hasText(presetSearchString)) {
            this.params.remove(PRESET_SEARCH_STRING);
            this.searchTextField.setValue(presetSearchString);
            this.search(presetSearchString);
        }
        else {
            enableSearchControls();
        }
    }

    public void deactivate() {
        enableSearchControls();
        ackSelectionChanged();
    }

    @Override
    public Widget getFocusWidget() {
        return this.searchTextField;
    }

    public void show(TableDataModel dtm) {
        enableSearchControls();
        this.renderer.reset();
        this.tw.update(this.tableColumnModel, dtm);
        if (dtm.getRowCount() == 0 && this.nextPageCounter++ < MAX_NEXT_PAGE) {
            this.c.getPagingFeature().gotoNextPage();
        }
    }

    private void ackNoSelection() {
        this.params.remove("title"); // $NON-NLS-0$
        this.params.remove("symbol"); // $NON-NLS-0$
        QuoteWithInstrument.setLastSelected(null);
    }

    protected int ackSelectionChanged() {
        final int n = getSelected();
        if (n == -1) {
            ackNoSelection();
            return n;
        }

        final QuoteWithInstrument qwi = this.c.getResultQwi(n);
        this.params.put("title", qwi.getInstrumentData().getName()); // $NON-NLS$

        final String symbol;
        switch (this.symbolParameterType) {
            case ISIN:
                symbol = qwi.getInstrumentData().getIsin();
                break;
            default:
                symbol = qwi.getInstrumentData().getIid();
                break;
        }
        this.params.put("symbol", symbol); // $NON-NLS$

        QuoteWithInstrument.setLastSelected(qwi);
        return n;
    }

    private MenuItem createItem(ViewSpec viewSpec, int itemIndex) {
        final MenuItem menuItem = new MenuItem(viewSpec.getName()).withData("itemIndex", itemIndex); // $NON-NLS$
        if (viewSpec.getIconCls() != null) {
            IconImage.setIconStyle(menuItem, viewSpec.getIconCls());
        }
        menuItem.setEnabled(false);
        return menuItem;
    }

    private Menu fillTypeMenu(Menu menu) {
        menu.removeAll();
        final int n = this.c.getIndexedViewSelectionModel().getViewCount();
        this.typeMenuItems = new MenuItem[n];
        for (int i = 0; i < this.typeMenuItems.length; i++) {
            this.typeMenuItems[i] = createItem(this.c.getIndexedViewSelectionModel().getViewSpec(i), i);
            menu.add(this.typeMenuItems[i]);
        }
        return menu;
    }

    public static native int getSelectedJs(Element e) /*-{
        var radios = e.getElementsByTagName("input"); // $NON-NLS-0$
        for (var i = 0; i < radios.length; i++) {
            if (radios[i].checked == true) return i;
        }
        return -1;
    }-*/;

    public int getSelected() {
        if (this.renderer.num == 0) {
            return -1;
        }
        return getSelectedJs(this.tw.getElement());
    }

    public void updateViewNames() {
        final IndexedViewSelectionModel model = c.getIndexedViewSelectionModel();

        if (model.getViewCount() != this.typeMenuItems.length) {
            fillTypeMenu(this.typeMenu.getMenu());
        }

        for (int i = 0; i < this.typeMenuItems.length; i++) {
            Firebug.log("<SelectSymbolForm.updateViewNames>");

            final MenuItem item = this.typeMenuItems[i];
            item.setText(model.getViewSpec(i).getName());
            item.setEnabled(model.isSelectable(i));
        }
        this.typeMenu.setEnabled(c.hasData());
        this.typeMenu.setSelectedItem(this.typeMenuItems[model.getSelectedView()]);
        this.typeMenu.getMenu().hide();
    }

    protected TableColumnModel getTableColumnModel() {
        return this.tableColumnModel;
    }

    protected void setTableColumnModel(TableColumnModel tableColumnModel) {
        this.tableColumnModel = tableColumnModel;
    }

    public void enableSearchControls() {
        this.sglp.hideGlass();
        this.searchTextField.setEnabled(true);
        this.buttonSearch.setEnabled(true);
        this.typeMenu.setEnabled(true);
        getBottomComponent().setEnabled(true);
        layout(true);
    }

    public void disableSearchControls() {
        this.sglp.showGlass(300);
        this.searchTextField.setEnabled(false);
        this.buttonSearch.setEnabled(false);
        this.typeMenu.setEnabled(false);
        getBottomComponent().setEnabled(false);
        layout(true);
    }

    public void reset() {
        show(DefaultTableDataModel.NULL);
    }

    @Override
    public void cancelPendingRequests() {
        this.c.cancelPendingRequests();
    }
}
