/*
 * AbstractViewFactory.java
 *
 * Created on 26.03.2015 15:08
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.event.logical.shared.HasResizeHandlers;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.Context;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsGroupProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsLeafProperty;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SpsWidget;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.events.SpsAfterPropertiesSetEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.mock.MockUtil;
import de.marketmaker.iview.pmxml.DataContainerCompositeNode;
import de.marketmaker.iview.pmxml.DataContainerGroupNode;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDataItem;
import de.marketmaker.iview.pmxml.DataContainerLeafNodeDeclaration;
import de.marketmaker.iview.pmxml.MM;
import de.marketmaker.iview.pmxml.MMIndexedString;
import de.marketmaker.iview.pmxml.ParsedTypeInfo;
import de.marketmaker.iview.pmxml.SectionDesc;
import de.marketmaker.iview.pmxml.TiType;
import de.marketmaker.iview.pmxml.ZoneDesc;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.mmtalk.MmTalkHelper.asMMType;

/**
 * @author mdick
 */
public abstract class AbstractViewFactory {
    public static class TaskViewPanelView implements SpsBaseDisplay {
        final TaskViewPanel tvp;
        public TaskViewPanelView(TaskViewPanel tvp) {
            this.tvp = tvp;
        }

        @Override
        public void setWidgets(Widget[] widgets) {
            if (widgets.length == 1) {
                this.tvp.setContentWidget(widgets[0]);
                return;
            }

            final FlowPanel panel = new FlowPanel();
            for (Widget widget : widgets) {
                panel.add(widget);
            }
            final Label labelEnd = new Label();
            labelEnd.setStyleName("sps-taskViewEnd");  // $NON-NLS$
            panel.add(labelEnd);

            this.tvp.setContentWidget(panel);
        }

        @Override
        public void setNorthWidget(Widget widget) {
            this.tvp.setNorthWidget(widget);
        }

        @Override
        public void layoutNorthWidget() {
            this.tvp.layout();
        }
    }

    private SpsBaseDisplay view;
    private Context spsContext;
    private SpsWidget spsRootWidget;

    protected abstract SectionDesc createFormDesc();

    protected abstract void addDeclAndData(DataContainerCompositeNode declRoot, DataContainerCompositeNode dataRoot);

    protected void init(SpsBaseDisplay view) {
        if(this.view != null) {
            throw new IllegalStateException("View has already been initialized");  // $NON-NLS$
        }
        if(view == null) {
            throw new IllegalArgumentException("View parameter must not be null");  // $NON-NLS$
        }

        this.view = view;

        final SectionDesc formDescRoot = createFormDesc();
        final DataContainerCompositeNode declRoot = new DataContainerGroupNode();
        final DataContainerCompositeNode dataRoot = new DataContainerGroupNode();
        addDeclAndData(declRoot, dataRoot);

        this.spsContext = new Context(declRoot, null, null, null, null, null, null, null, null);
        this.spsContext.transferDataToProperties(dataRoot, declRoot, false);
        this.spsRootWidget = this.spsContext.getEngine().createSpsWidget(formDescRoot);
        final Widget northWidget = this.spsRootWidget.createNorthWidget();
        this.view.setNorthWidget(northWidget);
        final SpsBaseDisplay fView = this.view;
        // necessary for fixed headers that contain a pie chart. As long as the chart engine is loaded initially,
        // loading the size of the chart is zero. So we have to resize the north widget if the chart engine was
        // loaded and the chart was rendered. This is very tedious so we are just listening for a resize event.
        // See also AS-1281
        if(northWidget instanceof HasResizeHandlers) {
            ((HasResizeHandlers) northWidget).addResizeHandler(new ResizeHandler() {
                @Override
                public void onResize(ResizeEvent event) {
                    fView.layoutNorthWidget();
                }
            });
        }
        this.view.setWidgets(this.spsRootWidget.asWidgets());
        this.spsContext.replayChangeEvents();
        SpsAfterPropertiesSetEvent.fireAndRemoveHandlers();
        this.spsRootWidget.focusFirst();
    }

    protected ParsedTypeInfo addLeaf(DataContainerCompositeNode declRoot, DataContainerCompositeNode dataRoot, TiType tiType, String displayName, String bind, String value) {
        return addLeaf(declRoot, dataRoot, tiType, true, displayName, bind, value);
    }

    protected ParsedTypeInfo addLeaf(DataContainerCompositeNode declRoot, DataContainerCompositeNode dataRoot, TiType tiType, boolean mandatory, String displayName, String bind, String value) {
        final ParsedTypeInfo ptiZone = MockUtil.pti(tiType, displayName, false, mandatory, "0", "0", "0", "0", 0, ""); // $NON-NLS$
        final DataContainerLeafNodeDeclaration declZone = new DataContainerLeafNodeDeclaration();
        declZone.setNodeLevelName(bind);
        declZone.setDescription(ptiZone);

        final DataContainerLeafNodeDataItem dataZone = new DataContainerLeafNodeDataItem();
        dataZone.setNodeLevelName(bind);
        if (value != null) {
            dataZone.setDataItem(asMMType(value, tiType));
        }

        declRoot.getChildren().add(declZone);
        dataRoot.getChildren().add(dataZone);
        return ptiZone;
    }

    protected MMIndexedString createEnumElement(ZoneDesc zoneDesc) {
        final MMIndexedString e = new MMIndexedString();
        e.setCode(zoneDesc.getId());
        e.setValue(zoneDesc.getName());
        return e;
    }

    public void resetPropertyChanged() {
        this.getRootProp().resetChanged();
    }

    protected MM getPropertyDataItem(String bindKey) {
        return getProperty(bindKey).getDataItem();
    }

    protected SpsLeafProperty getProperty(String bindKey) {
        return (SpsLeafProperty) getRootProp().get(bindKey);
    }

    protected SpsBaseDisplay getView() {
        return this.view;
    }

    @SuppressWarnings("unused")
    protected Context getSpsContext() {
        return this.spsContext;
    }

    public SpsGroupProperty getRootProp() {
        return this.spsContext.getRootProp();
    }

    public SpsWidget getSpsRootWidget() {
        return this.spsRootWidget;
    }
}
