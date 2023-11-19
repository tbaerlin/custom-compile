/*
 * MultiContentView.java
 *
 * Created on 23.04.2008 15:11:43
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.TopBorderPanel;

/**
 * @author Ulrich Maurer
 */
public class MultiContentView implements ContentContainer, ContentView {
    private ContentContainer c;
    private final TopBorderPanel topBorderPanel = new TopBorderPanel();

    private ViewSelectionView viewSelectionView;

    private ContentView lastContentView;

    public MultiContentView(ContentContainer c) {
        this.c = c;
    }

    public void init(IndexedViewSelectionModel viewSelectionModel) {
        this.viewSelectionView = new ViewSelectionViewButtons(viewSelectionModel);
        this.topBorderPanel.setStyleName("mm-multiContent"); // $NON-NLS-0$
        this.topBorderPanel.addNorth(this.viewSelectionView.getToolbar());
    }

    @Override
    public void setContent(ContentView contentView) {
        if (this.lastContentView != null && this.lastContentView != contentView) {
            this.lastContentView.onBeforeHide();
        }
        this.lastContentView = contentView;
        this.topBorderPanel.setWidget(contentView.getWidget());

        this.c.setContent(this);
        this.viewSelectionView.updateButtons();
    }

    @Override
    public void setContent(Widget content) {
        setContent(new ContentViewAdapter(content));
    }

    @Override
    public Widget getContent() {
        return this.lastContentView == null ? null : this.lastContentView.getWidget();
    }

    @Override
    public boolean isShowing(Widget w) {
        return this.lastContentView != null && this.lastContentView.getWidget() == w;
    }

    @Override
    public Widget getWidget() {
        return this.topBorderPanel.asWidget();
    }

    @Override
    public void onBeforeHide() {
        if (this.lastContentView != null) {
            this.lastContentView.onBeforeHide();
        }
    }

    public FloatingToolbar getToolbar() {
        return this.viewSelectionView.getToolbar();
    }
}
