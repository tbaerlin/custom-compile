package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;


public class FlowPanelView<S extends Snippet<S>> extends SnippetView<S> {
    protected final FlowPanel flowPanel;

    public FlowPanelView(S snippet) {
        super(snippet);
        setTitle(snippet.getConfiguration().getString("title")); // $NON-NLS-0$

        this.flowPanel = new FlowPanel();
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setHeaderVisible(this.container.getHeading() != null && this.container.getHeading().length() > 0);
        this.container.setContentWidget(this.flowPanel);
    }

    public void setTitle(String title) {
        this.snippet.getConfiguration().put("title", title); // $NON-NLS-0$
        reloadTitle();
    }

    public void add(Widget w) {
        flowPanel.add(w);
    }

    public FlowPanel getWidget() {
        return this.flowPanel;
    }
}
