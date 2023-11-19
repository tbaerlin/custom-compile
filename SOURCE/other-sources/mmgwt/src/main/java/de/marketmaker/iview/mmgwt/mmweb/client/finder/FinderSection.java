/*
 * FinderSection.java
 *
 * Created on 12.06.2008 12:50:34
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ConfigChangedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.FinderFormElementEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.FinderFormElementEventHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.finder.config.SectionConfigUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A section in a FinderForm. Sections can be en-/disabled using a CheckBox, the view of their
 * fields can be collapsed, so that multiple sections can be handled easily without too much
 * scrolling.
 */
public class FinderSection implements FinderFormElement, ClickHandler, ChangeHandler, FinderFormElementEventHandler {
    private static final String STYLE_SECTION_HEAD_VISIBLE = "mm-finder-sectionHead visible"; // $NON-NLS-0$
    private static final String STYLE_SECTION_HEAD_HIDDEN = "mm-finder-sectionHead hidden"; // $NON-NLS-0$
    private static final String STYLE_SECTION_ELEMENT_VISIBLE = "mm-finder-element visible"; // $NON-NLS-0$
    private static final String STYLE_SECTION_ELEMENT_HIDDEN = "mm-finder-element hidden"; // $NON-NLS-0$
    private static final int MORE_LESS_COUNT = 3;

    private final CheckBox cbEnabled = new CheckBox(""); // $NON-NLS-0$

    private final ArrayList<FinderFormElement> elements = new ArrayList<FinderFormElement>();

    protected final String id;

    private final String name;

    private final boolean alwaysEnabled;

    private boolean alwaysExpanded = false;

    private boolean optionsSwitched = false;

    private boolean silent = false;

    private FlexTable flexTable;

    private int rowSectionHead;

    private boolean active = true;

    private final DataLoader<List<FinderFormElement>> loader;
    private boolean configurable = false;

    private String defaultQuery = "";

    FinderSection(String id, String name, boolean alwaysEnabled) {
        this(id, name, alwaysEnabled, null);
    }

    FinderSection(String id, String name, boolean alwaysEnabled,
                  DataLoader<List<FinderFormElement>> loader) {
        this.loader = loader;
        this.id = id;
        this.name = name;
        this.alwaysEnabled = alwaysEnabled;
        if (this.alwaysEnabled) {
            this.cbEnabled.setValue(true);
            this.cbEnabled.setEnabled(false);
        }
        this.cbEnabled.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent event) {
                switchOptions();
                if (!FinderSection.this.alwaysEnabled && FinderSection.this.cbEnabled.getValue()) {
                    // user activated sections check box --> expand the section
                    FinderSection.this.setExpanded(true);
                }
            }
        });
        EventBusRegistry.get().addHandler(FinderFormElementEvent.getType(), this);
    }

    public void setSilent(boolean silent) {
        this.silent = silent;
    }

    public void setAlwaysExpanded() {
        this.alwaysExpanded = true;
    }

    public void setOptionsSwitched(boolean optionsSwitched) {
        this.optionsSwitched = optionsSwitched;
    }

    void addAll(List<FinderFormElement> finderFormElements) {
        for (FinderFormElement element : finderFormElements) {
            add(element);
        }
    }

    void add(FinderFormElement finderFormElement) {
        this.elements.add(finderFormElement);
        finderFormElement.addClickHandler(this);
        finderFormElement.addChangeHandler(this);
    }


    public void fireEvent(GwtEvent<?> event) {
        //nothing to fire
    }

    public void onClick(final ClickEvent event) {
        if (this.alwaysEnabled) {
            return;
        }
        final CheckBox cb = (CheckBox) event.getSource();
        if (cb.getValue() && !this.cbEnabled.getValue()) {
            enableAndFireEvent(cb, event);
        } else {
            setEnabledIfElementIsEnabled();
        }
    }

    public void onChange(ChangeEvent event) {
        if (this.alwaysEnabled) {
            return;
        }
        final FinderFormElement source = (FinderFormElement) event.getSource();
        if (source.getValue() && !this.cbEnabled.getValue()) {
            enableAndFireEvent(source, event);
        } else {
            setEnabledIfElementIsEnabled();
        }
    }


    private void enableAndFireEvent(HasHandlers source, GwtEvent event) {
        this.cbEnabled.setValue(true);
        // fire event again, because section was not enabled when event was fired first time
        source.fireEvent(event);
    }

    private void setEnabledIfElementIsEnabled() {
        for (FinderFormElement element : elements) {
            if (element.getValue()) {
                return;
            }
        }
        this.cbEnabled.setValue(false);
    }

    public void addChangeHandler(ChangeHandler ch) {
        //nothing to add
    }

    public boolean getValue() {
        return this.cbEnabled.getValue();
    }

    public void setValue(boolean checked) {
        this.cbEnabled.setValue(checked);
        switchOptions();
    }

    private void switchOptions() {
        if (this.alwaysEnabled) {
            return;
        }
        if (this.optionsSwitched) {
            final boolean checked = this.cbEnabled.getValue();
            for (FinderFormElement element : elements) {
                element.setValue(checked);
            }
        }
    }

    public void addConfigTo(FinderFormConfig config) {
        if (!isActive()) {
            return;
        }
        if (this.cbEnabled.getValue()) {
            config.put(this.id, "true"); // $NON-NLS-0$
        }
        if (this.isExpanded()) {
            config.put(this.id + "-visible", "true"); // $NON-NLS$
        }
        for (FinderFormElement element : elements) {
            element.addConfigTo(config);
        }
    }

    public void addTo(final FlexTable flexTable, int sectionId) {
        if (!isActive()) {
            return;
        }
        this.flexTable = flexTable;
        final FlexTable.FlexCellFormatter formatter = flexTable.getFlexCellFormatter();
        final HTMLTable.RowFormatter rowFormatter = flexTable.getRowFormatter();
        int row = flexTable.getRowCount();
        if (!this.alwaysExpanded && hasActiveElements()) {
            this.rowSectionHead = row;
            final Label labelSectionHead = new Label(this.name);
            labelSectionHead.addClickHandler(new ClickHandler() {
                public void onClick(ClickEvent event) {
                    setExpanded(!isExpanded());
                }
            });
            flexTable.setWidget(row, 0, this.cbEnabled);
            formatter.setStyleName(row, 0, "mm-finder-sectionHeadCb"); // $NON-NLS-0$
            flexTable.setWidget(row, 1, labelSectionHead);
            labelSectionHead.setStyleName("mm-finder-sectionHeadLabel"); // $NON-NLS-0$
            formatter.setStyleName(row, 1, STYLE_SECTION_HEAD_HIDDEN);
            formatter.setColSpan(row, 1, 3);
            row++;
        }

        // add the "mehr.../weniger..."-Label if more than 3 elements are active
        FinderFormElements.MoreLessLabel moreLessLabel = null;
        if (getActiveElementsCount() > MORE_LESS_COUNT) {
            moreLessLabel = new FinderFormElements.MoreLessLabel(this.elements, MORE_LESS_COUNT, this.id);
            this.elements.add(MORE_LESS_COUNT, moreLessLabel);
        }

        for (final FinderFormElement element : this.elements) {
            element.addTo(flexTable, row);
            rowFormatter.setStyleName(row, this.alwaysExpanded ? STYLE_SECTION_ELEMENT_VISIBLE : STYLE_SECTION_ELEMENT_HIDDEN);
            row++;
        }

        if (moreLessLabel != null) {
            moreLessLabel.setValue(false);
        }
    }

    private int getActiveElementsCount() {
        int count = 0;
        if (!isConfigurable()) {
            return count;
        }
        for (FinderFormElement element : elements) {
            if (element.isActive()) {
                count++;
            }
        }
        return count;
    }

    private boolean hasActiveElements() {
        for (FinderFormElement element : elements) {
            if (element.isActive()) {
                return true;
            }
        }
        return false;
    }

    public void apply(FinderFormConfig config) {
        this.cbEnabled.setValue(this.alwaysEnabled || "true".equals(config.get(this.id))); // $NON-NLS-0$
        for (FinderFormElement element : this.elements) {
            if (element.isActive()) {
                element.apply(config);
            }
        }
        if (hasActiveElements() && "true".equals(config.get(this.id + "-visible"))) { // $NON-NLS-0$ $NON-NLS-1$
            setExpanded(true);
        }
    }

    private boolean isExpanded() {
        return this.alwaysExpanded || ((this.flexTable.getRowCount() >= this.rowSectionHead) && (this.flexTable.getCellCount(this.rowSectionHead) >= 1) &&
                STYLE_SECTION_HEAD_VISIBLE.equals(this.flexTable.getFlexCellFormatter().getStyleName(this.rowSectionHead, 1)));
    }

    public void setExpanded(boolean expanded) {
        if (this.alwaysExpanded || isExpanded() == expanded) {
            return;
        }
        final String styleHead;
        final String styleElement;
        if (expanded) {
            styleHead = STYLE_SECTION_HEAD_VISIBLE;
            styleElement = STYLE_SECTION_ELEMENT_VISIBLE;
        } else {
            styleHead = STYLE_SECTION_HEAD_HIDDEN;
            styleElement = STYLE_SECTION_ELEMENT_HIDDEN;
        }
        final FlexTable.FlexCellFormatter formatter = flexTable.getFlexCellFormatter();
        final HTMLTable.RowFormatter rowFormatter = flexTable.getRowFormatter();
        formatter.setStyleName(this.rowSectionHead, 1, styleHead);
        for (int row = rowSectionHead + this.elements.size(); row > rowSectionHead; row--) {
            rowFormatter.setStyleName(row, styleElement);
        }
    }

    public String getQuery() {
        if (!this.cbEnabled.getValue() || this.silent) {
            return FinderFormUtils.handleDefaultQuery(null, this.defaultQuery);
        }
        final String query = AbstractFinderForm.getQuery(this.elements);
        return FinderFormUtils.handleDefaultQuery(query, this.defaultQuery);
    }

    public void addExplanation(FlowPanel panel) {
        if (this.cbEnabled.getValue() && !this.silent) {
            AbstractFinderForm.addExplanation(panel, this.elements);
        }
    }

    public void initialize(Map<String, FinderMetaList> map) {
        for (FinderFormElement element : elements) {
            element.initialize(map);
        }
    }

    public void addClickHandler(ClickHandler ch) {
        this.cbEnabled.addClickHandler(ch);
    }

    public void reset() {
        if (!isActive()) {
            return;
        }
        for (FinderFormElement element : elements) {
            element.reset();
        }
        this.cbEnabled.setValue(this.alwaysEnabled);
    }

    public ArrayList<FinderFormElement> getElements() {
        return elements;
    }

    public boolean isActive() {
        return this.active;
    }

    public Integer getDefaultOrder() {
        return null;
    }

    public String getLabel() {
        return this.name;
    }

    public String getId() {
        return this.id;
    }

    public FinderSection withConfigurable() {
        this.configurable = true;
        return this;
    }

    public FinderSection expand() {
        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                if (!isExpanded()) {
                    setExpanded(true);
                }
            }
        });
        return this;
    }

    public FinderSection loadElements() {
        if (this.loader != null) {
            addAll(this.loader.loadData());
        }
        return this;
    }

    public boolean isConfigurable() {
        return this.configurable;
    }

    private void clear() {
        this.elements.clear();
    }

    public void reloadElements() {
        if (this.loader != null && isActive()) {
            clear();
            loadElements();
        }
    }

    public void onElementCloneAction(FinderFormElementEvent event) {
        for (FinderFormElement element : elements) {
            if (event.getId().equals(element.getId())) {
                if (event.getAction() == FinderFormElementEvent.Action.CLONE) {
                    final String cloneId = FinderFormUtils.incId(findMaxInstanceId(event.getId()));
                    insertElementIdIntoConfig(cloneId, event.getId());
                } else if (event.getAction() == FinderFormElementEvent.Action.DELETE) {
                    deleteElementIdFromConfig(event.getId());
                }
            }
        }
    }

    private void insertElementIdIntoConfig(String newElementsId, String insertAfterId) {
        final String oldConf = SectionConfigUtil.getSectionConfigAsString(this.id);
        final List<String> elementIds = SectionConfigUtil.getElementIds(this.elements);
        elementIds.add(elementIds.indexOf(insertAfterId) + 1, newElementsId);
        final String newConf = SectionConfigUtil.saveSectionConfig(this.id, elementIds);
        EventBusRegistry.get().fireEvent(new ConfigChangedEvent(
                SectionConfigUtil.getPropertyName(this.id), oldConf, newConf));
    }

    private void deleteElementIdFromConfig(String id) {
        final List<String> elementIds = SectionConfigUtil.getElementIds(this.elements);
        if (!elementIds.contains(id)) {
            return;
        }
        final String oldConf = SectionConfigUtil.getSectionConfigAsString(this.id);
        elementIds.remove(id);
        final String newConf = SectionConfigUtil.saveSectionConfig(this.id, elementIds);
        EventBusRegistry.get().fireEvent(new ConfigChangedEvent(
                SectionConfigUtil.getPropertyName(this.id), oldConf, newConf));
    }

    private String findMaxInstanceId(String id) {
        final String origId = FinderFormUtils.getOriginalId(id);
        final List<Integer> ids = new ArrayList<Integer>();
        for (FinderFormElement element : this.elements) {
            if (FinderFormUtils.getOriginalId(element.getId()).equals(origId)) {
                final int instanceCounterOfId = FinderFormUtils.getInstanceCounterOfId(element.getId());
                ids.add(instanceCounterOfId);
            }
        }
        final int max = Collections.max(ids);
        return origId + "-" + String.valueOf(max);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void updateMetadata(Map<String, FinderMetaList> metaData, boolean force) {
        for (FinderFormElement element : this.elements) {
            if (element instanceof MutableMetadata) {
                ((MutableMetadata) element).updateMetadata(metaData, force);
            }
        }
    }


    public void addMissingElements(FinderFormConfig config) {
        final List<FinderFormElement> missingElements = new ArrayList<FinderFormElement>();
        for (FinderFormElement element : this.elements) {
            if (!element.isActive() && config.contains(element.getId())) {
                missingElements.add(element);
            } else if (element.isActive() && element instanceof CloneableFinderFormElement &&
                    !((CloneableFinderFormElement) element).isClone()) {
                final List<String> cloneIds = config.getCloneIds(element);
                if (cloneIds.isEmpty()) {
                    continue;
                }
                for (String cloneId : cloneIds) {
                    if (!elementExists(cloneId)) {
                        final FinderFormElement newClone = ((CloneableFinderFormElement) element)
                                .cloneElement(FinderFormUtils.concatId(element,
                                        FinderFormUtils.getInstanceCounterOfId(cloneId)));
                        missingElements.add(newClone);
                    }
                }
            }
        }
        if (missingElements.isEmpty()) {
            return;
        }
        final List<String> elementIds = SectionConfigUtil.getElementIds(missingElements, true);
        insertElementIdsIntoConfig(elementIds);
    }

    private boolean elementExists(String id) {
        for (FinderFormElement element : this.elements) {
            if (element.getId().equals(id)) {
                return true;
            }
        }
        return false;
    }

    private void insertElementIdsIntoConfig(List<String> elementIds) {
        final String oldConf = SectionConfigUtil.getSectionConfigAsString(this.id);
        final List<String> activeElements = SectionConfigUtil.getElementIds(this.elements);
        activeElements.addAll(elementIds);
        final String newConf = SectionConfigUtil.saveSectionConfig(this.id, activeElements);
        EventBusRegistry.get().fireEvent(new ConfigChangedEvent(
                SectionConfigUtil.getPropertyName(this.id), oldConf, newConf));
    }

    public FinderSection withDefaultQuery(String defaultQuery) {
        this.defaultQuery = defaultQuery == null ? "" : defaultQuery;
        return this;
    }

    public String getDefaultQuery() {
        return defaultQuery;
    }

    /**
     * Hides the section.
     * Schedules the operation after the containing table of the section is ready.
     */
    public FinderSection hideDeferred() {
        Scheduler.get().scheduleDeferred(new Command() {
            public void execute() {
                hide();
            }
        });
        return this;
    }

    public void hide() {
        final HTMLTable.RowFormatter rowFormatter = flexTable.getRowFormatter();
        for (int row = rowSectionHead; row <= rowSectionHead + elements.size(); row++) {
            rowFormatter.setStyleName(row, STYLE_SECTION_ELEMENT_HIDDEN);
        }
    }

    public void show() {
        final HTMLTable.RowFormatter rowFormatter = flexTable.getRowFormatter();
        for (int row = rowSectionHead; row <= rowSectionHead + elements.size(); row++) {
            rowFormatter.setStyleName(row, STYLE_SECTION_ELEMENT_VISIBLE);
        }
    }

    public static void enableBaseSection(FinderFormConfig ffc, final String type) {
        final String value = "true"; // $NON-NLS-0$
        switch (type) {
            case "CER": // $NON-NLS-0$
                ffc.put(LiveFinderCER.BASE_ID, value);
                return;
            case "OPT": // $NON-NLS-0$
                ffc.put(LiveFinderOPT.BASE_ID, value);
                return;
            case "WNT": // $NON-NLS-0$
                ffc.put(LiveFinderWNT.BASE_ID, value);
                return;
            case "FND": // $NON-NLS-0$
                ffc.put(LiveFinderFND.BASE_ID, value);
                return;
        }
    }

    public static void enableUnderlyingSection(FinderFormConfig ffc, final String type) {
        final String value = "true"; // $NON-NLS-0$
        switch (type) {
            case "CER": // $NON-NLS-0$
                ffc.put(LiveFinderCER.UNDERLYING_ID, value);
                return;
            case "OPT": // $NON-NLS-0$
                ffc.put(LiveFinderOPT.UNDERLYING_ID, value);
                return;
            case "WNT": // $NON-NLS-0$
                ffc.put(LiveFinderWNT.UNDERLYING_ID, value);
                return;
        }
    }
}