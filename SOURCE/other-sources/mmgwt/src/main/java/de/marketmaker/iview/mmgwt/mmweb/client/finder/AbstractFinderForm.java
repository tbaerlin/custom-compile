/*
 * FinderForm.java
 *
 * Created on 10.06.2008 13:31:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 * @author Michael Lösch
 */
abstract class AbstractFinderForm extends ContentPanel {
    private final ListBox savedSearchesBox = new ListBox();

    private static final int MAX_NUM_SAVED_SEARCHES = 20;

    boolean formShowing = false;

    protected List<Command> onRenderCommands = new ArrayList<>();

    protected final CardLayout cardLayout = new CardLayout();

    static String getQuery(List<? extends FinderFormElement> elements) {
        final StringBuilder sb = new StringBuilder();
        for (FinderFormElement element : elements) {
            final String subQuery = element.getQuery();
            if (subQuery == null) {
                continue;
            }
            if (sb.length() > 0) {
                sb.append(andOp(true));
            }
            sb.append(subQuery);
        }
        return (sb.length() > 0) ? sb.toString() : null;
    }

    private static Widget getExplanationWidget(List<? extends FinderFormElement> elements) {
        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("mm-finder-query"); // $NON-NLS-0$
        addExplanation(panel, elements);
        return panel;
    }

    static void addExplanation(FlowPanel panel, List<? extends FinderFormElement> elements) {
        for (FinderFormElement element : elements) {
            element.addExplanation(panel);
        }
    }

    static String andOp(boolean query) {
        return query ? "&&" : wrapInSpaces(I18n.I.finderFormAndOp());  // $NON-NLS$
    }

    static String orOp(boolean query) {
        return query ? "||" : wrapInSpaces(I18n.I.finderFormOrOp());  // $NON-NLS$
    }

    static String wrapInSpaces(String s) {
        return " " + s + " ";  // $NON-NLS$
    }

    static String eqOp(boolean query) {
        return query ? "==" : " = "; // $NON-NLS$
    }

    protected FinderController fc;

    protected LayoutContainer formPanel;

    protected List<FinderSection> sections = new ArrayList<>();

    AbstractFinderForm(FinderController fc) {
        init(fc);
        initSubclass();
        showSettings();
    }

    protected abstract void initSubclass();

    private void init(FinderController fc) {
        this.fc = fc;
        setHeaderVisible(false);
        setLayout(this.cardLayout);
        addStyleName("mm-multiContent"); // $NON-NLS-0$

        this.formPanel = new LayoutContainer();
        this.formPanel.addStyleName("mm-finder-form"); // $NON-NLS-0$
        this.formPanel.setBorders(false);
        this.formPanel.setScrollMode(Style.Scroll.AUTO);

        this.formPanel.addListener(Events.Render, new Listener<ComponentEvent>() {
            public void handleEvent(ComponentEvent event) {
                AbstractFinderForm.this.onRender();
            }
        });
    }

    protected void addManagementPanel(LayoutContainer container) {
        final BorderLayoutData data = new BorderLayoutData(Style.LayoutRegion.WEST, 240);
        data.setCollapsible(true);
        data.setFloatable(true);
        data.setSplit(false);

        container.add(createManagementPanel(false), data);
    }

    protected void onRender() {
        for (Command command : this.onRenderCommands) {
            command.execute();
        }
        this.onRenderCommands.clear();
    }

    protected ContentPanel createManagementPanel(boolean loadBtnLeft) {
        final ContentPanel p = new ContentPanel();
        p.addStyleName("mm-finder-saveForm"); // $NON-NLS-0$
        p.addStyleName("mm-contentData-leftPanel"); // $NON-NLS-0$
        p.setHeading(I18n.I.savedSearches());
        p.setScrollMode(Style.Scroll.AUTO);

        this.savedSearchesBox.setVisibleItemCount(10);
        this.savedSearchesBox.setWidth("100%"); // $NON-NLS-0$
        this.savedSearchesBox.setHeight("100%"); // $NON-NLS-0$
        final List<FinderFormConfig> configs = SessionData.INSTANCE.getUser().getAppConfig().getSearches(this.fc.getId());
        for (FinderFormConfig config : configs) {
            this.savedSearchesBox.addItem(config.getName());
        }

        final Button loadButton = Button.icon("mm-list-move" + (loadBtnLeft ? "-left" : "-right")) // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        loadSelectedSearch();
                    }
                })
                .build();
        final Button upButton = Button.icon("mm-list-move-up") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        moveSearch(savedSearchesBox.getSelectedIndex(), true);
                    }
                })
                .build();
        final Button downButton = Button.icon("mm-list-move-down") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        moveSearch(savedSearchesBox.getSelectedIndex(), false);
                    }
                })
                .build();
        final Button deleteButton = Button.icon("mm-finder-btn-delete") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        deleteSelectedSearch();
                    }
                })
                .build();

        final TextBox saveAsText = new TextBox();
        saveAsText.setValue(""); // $NON-NLS-0$
//        saveAsText.setId(fc.getId() + "-saveAs"); // $NON-NLS-0$
        saveAsText.setMaxLength(30);
        saveAsText.setWidth("100%"); // $NON-NLS-0$

        final Button saveButton = Button.icon("mm-save-icon") // $NON-NLS$
                .clickHandler(new ClickHandler() {
                    @Override
                    public void onClick(ClickEvent clickEvent) {
                        final String name = saveAsText.getValue().trim();
                        if (!"".equals(name)) { // $NON-NLS-0$
                            addOrReplaceSearch(name);
                        }
                    }
                })
                .build();

        final FlexTable ft = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = ft.getFlexCellFormatter();
        ft.setCellPadding(0);
        ft.setCellSpacing(3);
        ft.setStyleName("mm-finder-saveTable"); // $NON-NLS-0$

        ft.setWidget(0, 0, this.savedSearchesBox);
        formatter.setColSpan(0, 0, 4);
        formatter.setStyleName(0, 0, "mm-center"); // $NON-NLS-0$

        ft.setWidget(1, 0, loadBtnLeft ? loadButton : deleteButton);
        ft.setWidget(1, 1, upButton);
        ft.setWidget(1, 2, downButton);
        ft.setWidget(1, 3, loadBtnLeft ? deleteButton : loadButton);
        formatter.setWidth(1, 0, "25%"); // $NON-NLS-0$
        formatter.setWidth(1, 1, "25%"); // $NON-NLS-0$
        formatter.setWidth(1, 2, "25%"); // $NON-NLS-0$
        formatter.setWidth(1, 3, "25%"); // $NON-NLS-0$
        formatter.setHorizontalAlignment(1, 0, HasHorizontalAlignment.ALIGN_LEFT);
        formatter.setHorizontalAlignment(1, 1, HasHorizontalAlignment.ALIGN_RIGHT);
        formatter.setHorizontalAlignment(1, 2, HasHorizontalAlignment.ALIGN_LEFT);
        formatter.setHorizontalAlignment(1, 3, HasHorizontalAlignment.ALIGN_RIGHT);

        ft.setHTML(2, 0, I18n.I.saveSearchQueryAs());
        formatter.setColSpan(2, 0, 4);
        formatter.setStyleName(2, 0, "mm-topSpace"); // $NON-NLS-0$

        ft.setWidget(3, 0, saveAsText);
        ft.setWidget(3, 1, saveButton);
        formatter.setColSpan(3, 0, 3);
        formatter.setHorizontalAlignment(3, 1, HasHorizontalAlignment.ALIGN_RIGHT);

        p.add(ft);

        return p;
    }

    protected void loadSelectedSearch() {
        final int n = getSavedSeachIdx();
        if (n >= 0) {
            apply(SessionData.INSTANCE.getUser().getAppConfig().getSavedSearch(this.fc.getId(), n));
            this.fc.onSearchLoaded();
        }
    }

    protected int getSavedSeachIdx() {
        return this.savedSearchesBox.getSelectedIndex();
    }

    private void deleteSelectedSearch() {
        final int n = getSavedSeachIdx();
        if (n >= 0) {
            this.savedSearchesBox.removeItem(n);
            SessionData.INSTANCE.getUser().getAppConfig().removeSearch(this.fc.getId(), n);
        }
    }

    private void updateStatus(String text, final boolean wasError) {
        if (wasError) {
            AbstractMainController.INSTANCE.showError(text);
        }
        else {
            AbstractMainController.INSTANCE.showMessage(text);
        }
    }

    private void addOrReplaceSearch(String name) {
        boolean update = false;
        for (int i = 0; i < this.savedSearchesBox.getItemCount(); i++) {
            if (name.equals(this.savedSearchesBox.getItemText(i))) {
                update = true;
                break;
            }
        }
        if (!update && this.savedSearchesBox.getItemCount() == MAX_NUM_SAVED_SEARCHES) {
            updateStatus(I18n.I.messageMaxSearches(MAX_NUM_SAVED_SEARCHES), true);
            return;
        }
        FinderFormConfig config = getConfig(name);
        SessionData.INSTANCE.getUser().getAppConfig().saveSearch(config);
        if (!update) {
            this.savedSearchesBox.addItem(name);
            updateStatus(I18n.I.savedSearch(), false);
        }
        else {
            updateStatus(I18n.I.updatedSearch(), false);
        }
    }

    private void moveSearch(int n, boolean up) {
        if (n < 0) {
            return;
        }
        final int m = up ? n - 1 : n + 1;
        if (m < 0 || m == this.savedSearchesBox.getItemCount()) {
            return;
        }
        final String text = savedSearchesBox.getItemText(n);
        this.savedSearchesBox.removeItem(n);
        this.savedSearchesBox.insertItem(text, m);
        this.savedSearchesBox.setSelectedIndex(m);
        SessionData.INSTANCE.getUser().getAppConfig().moveSearch(this.fc.getId(), n, up);
    }

    FinderSection addSection(String id, String name, boolean alwaysEnabled,
            DataLoader<List<FinderFormElement>> loader, SearchHandler searchHandler) {
        final FinderSection result = new FinderSection(id, name, alwaysEnabled, loader);
        if (searchHandler != null) {
            result.addClickHandler(searchHandler.getClickHandler());
        }
        this.sections.add(result);
        return result;
    }

    void apply(FinderFormConfig config) {
        reset();
        for (FinderSection section : sections) {
            section.apply(config);
        }
    }

    FinderFormConfig getConfig(String name) {
        final FinderFormConfig config = new FinderFormConfig(name, fc.getId());
        for (FinderSection section : sections) {
            section.addConfigTo(config);
        }
        Firebug.log(config.toString());
        return config;
    }

    List<String> getDefaultQueries() {
        final ArrayList<String> result = new ArrayList<>();
        for (FinderSection section : sections) {
            final String defaultQuery = section.getDefaultQuery();
            if (StringUtil.hasText(defaultQuery)) {
                result.add(defaultQuery);
            }
        }
        return result;
    }

    String getQuery() {
        try {
            return getQuery(this.sections);
        } catch (IllegalArgumentException e) {
            AbstractMainController.INSTANCE.showError(e.getMessage());
            return null;
        }
    }

    Widget getExplanationWidget() {
        try {
            return getExplanationWidget(this.sections);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * to be called after all sections have been added to this finder form; sets up all the
     * inner widgets that constitute this form.
     * @param map ...
     */
    void initialize(Map<String, FinderMetaList> map) {
        final FlexTable flexTable = new FlexTable();
        flexTable.setStyleName("mm-formTable"); // $NON-NLS-0$
        final int sectionCount = this.sections.size();
        for (int sectionId = 0; sectionId < sectionCount; sectionId++) {
            final FinderSection section = this.sections.get(sectionId);
            section.initialize(map);
            section.addTo(flexTable, sectionId);
        }

        addConfigurationButton(flexTable);

        addToFormPanel(flexTable, map);
    }

    private void addConfigurationButton(FlexTable flexTable) {
        int row = flexTable.getRowCount();
        HorizontalPanel panel = new HorizontalPanel();
        panel.setVerticalAlignment(HorizontalPanel.ALIGN_MIDDLE);
        panel.add(new Label(I18n.I.searchParamsConfig()));
        panel.add(Button.icon("x-tool-gear").clickHandler(new ClickHandler() {  // $NON-NLS$
            @Override
            public void onClick(ClickEvent event) {
                showConfigurator();
            }
        }).build());
        flexTable.setWidget(row, 1, panel);
        flexTable.getFlexCellFormatter().setStyleName(row, 1, "mm-finder-config");  // $NON-NLS-0$
    }

    protected abstract void showConfigurator();

    abstract protected void addToFormPanel(FlexTable flexTable, Map<String, FinderMetaList> map);

    void reset() {
        for (FinderSection section : this.sections) {
            section.reset();
        }
    }

    abstract void showSettings();

    abstract void showResult();

    abstract void addResultPanel(AbstractFinderView view);

    void addOnRenderCommand(Command c) {
        this.onRenderCommands.add(c);
    }

    public void deactivateElementById(String id) {
        for (FinderSection section : sections) {
            final ArrayList<FinderFormElement> elements = section.getElements();
            for (FinderFormElement element : elements) {
                if (element.getId().equals(id)) {
                    element.setValue(false);
                }
            }
        }
    }

    public boolean deactivateFollowingElementsInSection(
            final FinderFormElements.AbstractOption fromElement) {
        boolean deactivate = false;
        boolean deactivatedElements = false;
        for (FinderSection section : sections) {
            if (section.isActive()) {
                final ArrayList<FinderFormElement> elements = section.getElements();
                for (FinderFormElement e : elements) {
                    if (e instanceof DynamicValueElement) {
                        if (!deactivate) {
                            if (fromElement.getId().equals(e.getId())) {
                                deactivate = true;
                            }
                        }
                        else {
                            if (e.getValue()) {
                                e.setValue(false);
                                if (!deactivatedElements) {
                                    deactivatedElements = true;
                                }
                            }
                        }
                    }
                }
            }
            if (deactivate) {
                break;
            }
        }
        return deactivatedElements;
    }

    public String getContentPanelHtml() {
        return getElement().getInnerHTML();
    }

    abstract void setControlsEnabled(boolean value);

}