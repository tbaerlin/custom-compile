package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.snippets;

import com.google.gwt.dom.client.Style;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.PrivacyMode;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityOverviewController;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityOverviewView;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.ActivityOverviewViewImpl;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.AbstractSnippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.Snippet;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetClass;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetConfiguration;
import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.pmxml.ActivityDefinitionInfo;
import de.marketmaker.iview.pmxml.ActivityInstanceInfo;
import de.marketmaker.iview.pmxml.ActivityInstancesRequest;
import de.marketmaker.iview.pmxml.ActivityInstancesResponse;

import java.util.List;

/**
 * Created on 26.06.15
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */

public class PmActivitySnippet extends AbstractSnippet<PmActivitySnippet, PmActivitySnippet.View> implements ActivityOverviewView.Presenter, ObjectIdSnippet {

    public static class Class extends SnippetClass {
        public Class() {
            super("PmActivity", I18n.I.activities()); // $NON-NLS-0$
        }

        public Snippet newSnippet(DmxmlContext context, SnippetConfiguration config) {
            return new PmActivitySnippet(context, config);
        }
    }

    class View extends SnippetView<PmActivitySnippet> {
        private final SimplePanel panel = new SimplePanel();

        public View(PmActivitySnippet snippet) {
            super(snippet);
            this.panel.getElement().getStyle().setOverflowY(Style.Overflow.AUTO); //TODO use ulrich's scroll container
            setTitle(getConfiguration().getString("title", I18n.I.activities()));  // $NON-NLS$
        }

        @Override
        protected void onContainerAvailable() {
            super.onContainerAvailable();
            this.container.setContentWidget(this.panel);
        }

        public void setWidget(IsWidget widget) {
            this.panel.setWidget(widget);
        }
    }


    private final DmxmlContext.Block<ActivityInstancesResponse> blockActivityInsts;
    private final ActivityInstancesRequest activityInstancesRequest;

    public PmActivitySnippet(DmxmlContext context, SnippetConfiguration configuration) {
        super(context, configuration);
        this.activityInstancesRequest = new ActivityInstancesRequest();
        this.blockActivityInsts = this.context.addBlock("ACT_GetInstances"); // $NON-NLS$
        this.blockActivityInsts.setParameter(this.activityInstancesRequest);
        // if the snippet is added to a global dashboard (i.e. by accident or to design a investor dashboard)
        // this snippet will send its request, even if no object ID as been set at all, which causes some PM coders
        // irritating error log entries in PM. Hence, the block is disabled by default and only enabled, if an ID is
        // set (cf. AS-1382).
        this.blockActivityInsts.setEnabled(false);
        this.activityInstancesRequest.setCustomerDesktopActive(PrivacyMode.isActive());
        setView(new View(this));
    }

    @Override
    public void setObjectId(String objectId) {
        // if the snippet is added to a global dashboard (i.e. by accident or to design a investor dashboard)
        // this snippet will send its request, even if no object ID as been set at all, which causes some PM coders
        // irritating error log entries in PM. Hence, the block is disabled by default and only enabled, if an ID is
        // set (cf. AS-1382).
        this.blockActivityInsts.setEnabled(StringUtil.hasText(objectId) && !"0".equals(objectId));  // $NON-NLS$

        this.activityInstancesRequest.setObjectId(objectId);
        this.activityInstancesRequest.setCustomerDesktopActive(PrivacyMode.isActive());
        this.blockActivityInsts.setToBeRequested();
        ackParametersChanged();
    }

    @Override
    public void destroy() {
        destroyBlock(this.blockActivityInsts);
    }

    @Override
    public void updateView() {
        if (!this.blockActivityInsts.isResponseOk()) {
            return;
        }
        final List<ActivityInstanceInfo> insts = this.blockActivityInsts.getResult().getInstances();
        final FlexTable table = ActivityOverviewViewImpl.createTable(this, insts, false);
        getView().setWidget(table);
    }

    @Override
    public void createNewActivity(ActivityDefinitionInfo def) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void goToActivityInstance(ActivityInstanceInfo inst) {
        final List<ActivityInstanceInfo> insts = this.blockActivityInsts.getResult().getInstances();
        ActivityOverviewController.goToActivityInstance(inst, insts);
    }

    @Override
    public void deleteActivity(ActivityInstanceInfo inst) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void deleteAllActivities() {
        throw new UnsupportedOperationException();
    }
}
