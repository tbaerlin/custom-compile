/*
 * SpsSectionList.java
 *
 * Created on 14.04.2014
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.listsection;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.itools.gwtutil.client.util.Firebug;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.ChildrenFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.ExceptionUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasChildrenFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.HasViewStateFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.RequiresPropertyUpdateBeforeSave;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.RequiresRelease;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsBoundWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsCompositeProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListActions;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsListProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.ViewStateFeature;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.widget.NoValidationPopup;
import de.marketmaker.iview.mmgwt.mmweb.client.events.EventBusRegistry;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Ulrich Maurer
 * @author Markus Dick
 */
public class SpsSectionList extends SpsBoundWidget<Panel, SpsProperty> implements SpsAfterPropertiesSetHandler,
        HasChildrenFeature, HasViewStateFeature, NoValidationPopup, RequiresPropertyUpdateBeforeSave,
        SectionListDisplay.SectionListPresenter {
    private static final String COLLAPSED_VIEW_STATE_KEY = "collapsed";  // $NON-NLS$

    private final SpsListActions spsListActions;

    private final SectionListDisplay display;

    private final ChildrenFeature childrenFeature = new ChildrenFeature();
    private boolean collapsible = false;
    private boolean collapsibleInitialized = false;
    private boolean collapsed = false;
    private ViewStateFeature viewStateFeature = new ViewStateFeature();

    private boolean removeDefaultListEntries = false;
    private SpsProperty defaultCompareProperty = null;

    private List<String> groupOrder = null;

    final List<SectionListEntry> listListEntries = new ArrayList<>();
    final Map<SpsProperty, SectionListEntry> mapListEntries = new HashMap<>();
    private List<SpsProperty> orderedChildren;
    private List<SpsWidget> footerWidgets;

    public SpsSectionList(final SpsListActions spsListActions, boolean readonly, SectionListDisplay sectionListDisplay) {
        EventBusRegistry.get().addHandler(SpsAfterPropertiesSetEvent.getType(), this);

        this.spsListActions = spsListActions;
        setReadonly(readonly);

        this.display = sectionListDisplay;
        this.display.setPresenter(this);
        this.display.setReadonly(readonly);
    }

    public SpsSectionList withRemoveDefaultListEntries() {
        this.removeDefaultListEntries = true;
        return this;
    }

    private boolean getCollapsedStateOrDefault() {
        final String value = this.viewStateFeature.getValue(COLLAPSED_VIEW_STATE_KEY);
        if(!StringUtil.hasText(value)) {
            return true;
        }
        return Boolean.valueOf(value);
    }

    public SpsSectionList withListEntryCaption(String listEntryCaption) {
        setListEntryCaption(listEntryCaption);
        return this;
    }

    public void setCollapsible(boolean collapsible) {
        this.collapsible = collapsible;
        this.display.setCollapsible(collapsible);
    }

    public void setCollapsed(boolean collapsed) {
        if(!this.collapsible || this.collapsed == collapsed) {
            return;
        }
        this.collapsed = collapsed;
        this.display.setCollapsed(this.collapsed);
        this.viewStateFeature.putValue(COLLAPSED_VIEW_STATE_KEY, Boolean.toString(this.collapsed));
    }

    public void setListEntryCaption(String listEntryCaption) {
        this.display.setListEntryCaption(listEntryCaption);
    }

    public void setAddButtonTooltip(String addButtonTooltip) {
        this.display.setAddButtonTooltip(addButtonTooltip);
    }

    public SpsSectionList withAddButtonTooltip(String addButtonTooltip) {
        setAddButtonTooltip(addButtonTooltip);
        return this;
    }

    public void setDeleteButtonTooltip(String deleteButtonTooltip) {
        this.display.setDeleteButtonTooltip(deleteButtonTooltip);
    }

    public SpsSectionList withDeleteButtonTooltip(String deleteButtonTooltip) {
        setDeleteButtonTooltip(deleteButtonTooltip);
        return this;
    }

    @Override
    public void addProperty() {
        int childCount = getChildCount(getBindFeature().getSpsProperty());
        this.display.setAnimatedEntryIndex(childCount);
        this.spsListActions.addProperty(true);
    }

    private int getChildCount(SpsProperty spsProperty) {
        if(spsProperty instanceof SpsListProperty) {
            return ((SpsListProperty) spsProperty).getChildCount();
        }
        else if(spsProperty instanceof SpsGroupProperty){
            return ((SpsGroupProperty) spsProperty).getChildren().size();
        }
        throw new IllegalStateException("property is null or has no children");  // $NON-NLS$
    }

    private boolean isEqual(SpsProperty defaultProperty, SpsProperty entryProperty) {
        if(defaultProperty.getClass() != entryProperty.getClass()) {
            return false;
        }

        if(entryProperty instanceof SpsLeafProperty) {
            final SpsLeafProperty entryLeaf = (SpsLeafProperty) entryProperty;
            final SpsLeafProperty defaultLeaf = (SpsLeafProperty) defaultProperty;

            return MmTalkHelper.equals(defaultLeaf.getParsedTypeInfo().getTypeId(), defaultLeaf.getDataItem(), entryLeaf.getDataItem());
        }
        else if(entryProperty instanceof SpsListProperty) {
            final SpsListProperty entryList = (SpsListProperty) entryProperty;
            final SpsListProperty defaultList = (SpsListProperty) defaultProperty;

            if(defaultList.getChildCount() != entryList.getChildCount()) {
                return false;
            }
            boolean result = true;
            for (int i = 0; i < defaultList.getChildCount(); i++) {
                result &= isEqual(entryList.get(i), defaultList.get(i));
            }
            return result;
        }
        else if(entryProperty instanceof SpsGroupProperty) {
            final SpsGroupProperty entryGroup = (SpsGroupProperty) entryProperty;
            final SpsGroupProperty defaultGroup = (SpsGroupProperty) defaultProperty;

            if(!StringUtil.equals(defaultGroup.getNodeGUID(), entryGroup.getNodeGUID())) {
                return false;
            }

            boolean result = true;
            for (SpsProperty defaultChild : defaultGroup.getChildren()) {
                final SpsProperty entryChild = entryGroup.get(defaultChild.getBindKey());
                if(entryChild == null) {
                    return false;
                }
                result &= isEqual(defaultChild, entryChild);
            }
            return result;
        }
        else {
            throw new IllegalStateException("instance of property not handled by isEqual");  // $NON-NLS$
        }
    }

    @Override
    public void deleteProperty(SpsProperty p){
        this.spsListActions.deleteProperty(p);
    }

    @Override
    public void deleteAllAndAddProperty() {
        this.display.setAnimatedEntryIndex(0);
        this.spsListActions.deleteAllAndAddProperty();
    }

    @Override
    public void afterPropertiesSet() {
        // create an default list entry to delete all list entries that contain nothing more than default data
        if(this.defaultCompareProperty == null) {
            this.defaultCompareProperty = this.spsListActions.createDefaultCompareProperty();
        }

        //create the footer widgets
        this.footerWidgets = this.spsListActions.createFooterWidgets(getLevel() + 1);

        final SpsProperty spsProperty = getBindFeature().getSpsProperty();
        if(spsProperty instanceof SpsListProperty) {
            //add an initial list item as it will appear if pm delivers an empty item.
            //must be called at the end of the loop, because onPropertyChange fires also
            //afterPropertiesSet events
            if (getChildCount(spsProperty) == 0) {
                Scheduler.get().scheduleFinally(new Scheduler.ScheduledCommand() {
                    @Override
                    public void execute() {
                        onPropertyChange();
                    }
                });
            }
        }
    }

    private void tryInitializeCollapsedState() {
        if(!this.collapsible || this.collapsibleInitialized) {
            return;
        }
        setCollapsed(getCollapsedStateOrDefault());
        this.collapsibleInitialized = true;
    }

    @Override
    public void onPropertyChange() {
        this.orderedChildren = null;

        final SpsProperty spsProperty = getBindFeature().getSpsProperty();

        if (getChildCount(spsProperty) == 0) {
            this.display.setAnimatedEntryIndex(0);
            // If we should remove the default list entries added here, we do not want to mark the list as changed,
            // because otherwise adding the entry will trigger the refresh button to appear in cases of SA_REFRESH.
            // In those cases, we will never get the next button, because an empty list, causes to add a new entry
            // immediately which will then trigger the refresh button to appear and so on.
            // However, in cases where the default list entry should not be deleted, it may be valid to add this
            // default entry, whether the user entered data in the bound field or not. Hence, it is necessary
            // to mark the whole list as changed, so that it is guaranteed that a single default entry is transferred
            // to PM. Cf. AS-1297
            this.spsListActions.addProperty(!this.removeDefaultListEntries);
            return;
        }

        final List<SpsProperty> childProperties = getChildren(spsProperty);

        this.display.setVisibleForRendering(false);

        final Map<SpsProperty, SectionListEntry> map = new HashMap<>(this.mapListEntries);
        final boolean multipleEntries = childProperties.size() > 1;
        for (int i = 0; i < childProperties.size(); i++) {
            final SpsProperty p = childProperties.get(i);
            final SectionListEntry sectionListEntry = map.remove(p);
            if (sectionListEntry == null) {
                final SectionListEntry newEntry = this.display.createListEntry(p, i);
                newEntry.setDeleteVisible(multipleEntries);
                this.listListEntries.add(newEntry);
                this.mapListEntries.put(p, newEntry);
                this.display.addListEntry(newEntry);
                newEntry.focusFirst();
            }
            else {
                sectionListEntry.setCaptionNumber(i + 1);
                sectionListEntry.setDeleteVisible(multipleEntries);
            }
        }

        for (Map.Entry<SpsProperty, SectionListEntry> entry : map.entrySet()) {
            final SectionListEntry sectionListEntry = entry.getValue();
            this.listListEntries.remove(sectionListEntry);
            this.mapListEntries.remove(entry.getKey());
            this.display.removeListEntry(sectionListEntry);
            this.childrenFeature.getChildren().removeAll(sectionListEntry.getSpsWidgetList());

            releaseWidgets(sectionListEntry.getSpsWidgetList());
            sectionListEntry.clearWidgets();
        }

        this.display.addFooter(this.footerWidgets);

        this.display.setVisibleForRendering(true);
        this.display.setAnimatedEntryIndex(-1);

        tryInitializeCollapsedState();
    }

    private List<SpsProperty> getChildren(SpsProperty spsProperty) {
        if(spsProperty instanceof SpsListProperty) {
            return ((SpsListProperty) spsProperty).getChildren();
        }
        else if(spsProperty instanceof SpsGroupProperty) {
            if(this.orderedChildren != null) {
                return this.orderedChildren;
            }

            //output all children if order is not given by PM
            if(this.groupOrder == null || this.groupOrder.isEmpty()) {
                this.orderedChildren = new ArrayList<>(((SpsGroupProperty) spsProperty).getChildren());
                return this.orderedChildren;
            }

            //order children as indicated by PM; only output children that have been explicitly named in groupOrder
            this.orderedChildren = new ArrayList<>();
            final HashSet<String> processedChildren = new HashSet<>();
            for (String bindKey : this.groupOrder) {
                if(processedChildren.contains(bindKey)) {
                    DebugUtil.showDeveloperNotification("SpsSectionList - child group '" + bindKey + "' occurs more than once in groupOrder");
                    continue;
                }
                processedChildren.add(bindKey);

                final SpsProperty child = ((SpsGroupProperty) spsProperty).get(bindKey);
                if(child == null) {
                    DebugUtil.showDeveloperNotification("SpsSectionList - child group '" + bindKey + "' named in groupOrder was not found in bound group");
                    continue;
                }
                this.orderedChildren.add(child);
            }
            return this.orderedChildren;
        }
        throw new IllegalStateException("property is null or has no children");  // $NON-NLS$
    }

    @Override
    public List<SpsWidget> createAndAddWidgets(SpsProperty p) {
        try {
            final List<SpsWidget> widgets = this.spsListActions.createWidgets(p, getLevel() + 1);
            this.childrenFeature.addChildren(widgets);
            return widgets;
        }
        catch (Exception e) {
            ExceptionUtil.logErrorOrPropertyIsNull("<SpsListSection.createAndAddWidgets>", e);  // $NON-NLS$
        }
        return Collections.emptyList();
    }

    @Override
    public void fireAfterPropertiesSet() {
        try {
            SpsAfterPropertiesSetEvent.fireAndRemoveHandlers();
        }
        catch (Exception e) {
            ExceptionUtil.logErrorOrPropertyIsNull("<SpsListSection.fireAfterPropertiesSet>", e);  // $NON-NLS$
        }
    }

    @Override
    public void fireChanged(SpsProperty p) {
        if (p == null) {
            Firebug.warn("<SpsListSection.fireChanged> cannot fire change: property is null");
            return;
        }

        try {
            p.fireChanged();
        }
        catch (Exception e) {
            ExceptionUtil.logErrorOrPropertyIsNull("<SpsListSection.fireChanged>", e);  // $NON-NLS$
        }

        if (p instanceof SpsCompositeProperty) {
            for (SpsProperty child : ((SpsCompositeProperty) p).getChildren()) {
                fireChanged(child);
            }
        }
    }

    @Override
    public void onCollapseClicked() {
        setCollapsed(!this.collapsed);
    }

    private static void releaseWidgets(List<SpsWidget> spsWidgets) {
        for (SpsWidget spsWidget : spsWidgets) {
            if (spsWidget instanceof RequiresRelease) {
                ((RequiresRelease) spsWidget).release();
            }
        }
    }

    @Override
    protected void onWidgetConfigured() {
        this.display.onWidgetConfigured();
        super.onWidgetConfigured();
    }

    @Override
    protected HTML createCaptionWidget() {
        final HTML captionWidget = super.createCaptionWidget();
        if (captionWidget == null) {
            return null;
        }
        captionWidget.setStyleName("sps-section-header");
        return captionWidget;
    }

    @Override
    protected Panel createWidget() {
        return this.display.getView();
    }

    @Override
    public boolean focusFirst() {
        if(this.collapsible && this.collapsed) {
            this.display.focusCollapsedTrigger();
            return true;
        }

        for (SectionListEntry sectionListEntry : this.listListEntries) {
            if (sectionListEntry.focusFirst()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public ChildrenFeature getChildrenFeature() {
        return this.childrenFeature;
    }

    @Override
    public boolean isFormContainer() {
        return false;
    }

    @Override
    public ViewStateFeature getViewStateFeature() {
        return this.viewStateFeature;
    }

    @Override
    public boolean isReadonly() {
        final SpsProperty spsProperty = getBindFeature().getSpsProperty();
        // it is not possible to add list entries, if the bound property is a group because we do not have a single decl
        // that can act as a template for new entries and we cannot guess a name for the group.
        return spsProperty != null && !(spsProperty instanceof SpsListProperty) || super.isReadonly();
    }

    @Override
    public void updatePropertyBeforeSave() {
        final SpsProperty spsProperty = getBindFeature().getSpsProperty();
        if(this.isReadonly() || !this.removeDefaultListEntries || this.defaultCompareProperty == null) {
            return;
        }

        final SpsListProperty list = (SpsListProperty) spsProperty;
        final List<SpsProperty> children = list.getChildren();
        if(children.size() == 1) {
            for (SpsProperty child : children) {
                final boolean equal = isEqual(this.defaultCompareProperty, child);
                if (equal) {
                    list.remove(child, true, false);
                }
            }
        }
    }

    public SpsSectionList withGroupOrder(List<String> groupOrder) {
        this.groupOrder = groupOrder;
        return this;
    }

    public void setFooterCaption(String footerCaption) {
        this.display.setFooterCaption(footerCaption);
    }
}
