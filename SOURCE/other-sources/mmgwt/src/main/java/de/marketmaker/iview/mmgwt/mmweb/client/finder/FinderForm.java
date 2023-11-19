/*
 * FinderForm.java
 *
 * Created on 10.06.2008 13:31:45
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.finder;

import java.util.Map;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.ui.FlexTable;

import de.marketmaker.iview.dmxml.FinderMetaList;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.view.FloatingToolbar;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
class FinderForm extends AbstractFinderForm {
    private LayoutContainer view0;
    private AbstractFinderView view1;

    FinderForm(FinderController fc) {
        super(fc);
    }

    void showSettings() {
        this.cardLayout.setActiveItem(this.view0);
        this.formShowing = true;
    }

    void showResult() {
        this.cardLayout.setActiveItem(this.view1);
        this.formShowing = false;
    }

    void addResultPanel(AbstractFinderView view) {
        final FloatingToolbar t = new FloatingToolbar();
        setTopComponent(t);
        view.addViewSelectionTo(t);
        add(view);
        this.view1 = view;
    }

    @Override
    protected void initSubclass() {
        this.view0 = new LayoutContainer();
        this.view0.setBorders(false);
        this.view0.addStyleName("mm-contentData"); // $NON-NLS-0$
        this.view0.addStyleName("mm-finder-wrapper"); // $NON-NLS-0$
        this.view0.setLayout(new BorderLayout());
        add(this.view0);

        this.view0.add(this.formPanel, new BorderLayoutData(Style.LayoutRegion.CENTER));

        if (!SessionData.INSTANCE.isAnonymous()) {
            addManagementPanel(this.view0);
        }
    }

    @Override
    protected void showConfigurator() {

    }

    @Override
    protected void addToFormPanel(FlexTable flexTable, Map<String, FinderMetaList> map) {
        this.formPanel.add(flexTable);
    }

    @Override
    void setControlsEnabled(boolean value) {
    }
}