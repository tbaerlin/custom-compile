package de.marketmaker.iview.mmgwt.mmweb.client.snippets;


import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.ContentPanelIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableColumnModel;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;

public class SnippetToggleTableView<V extends Snippet<V>> extends SnippetTableView<V> {
    private static final String MINUS_STYLE = "x-tool-btn-minus"; // $NON-NLS$
    private static final String PLUS_STYLE = "x-tool-btn-plus"; // $NON-NLS$

    private static final String EXPANDER_DOWN_STYLE = "expander-down"; // $NON-NLS$
    private static final String EXPANDER_RIGHT_STYLE = "expander-right"; // $NON-NLS$

    private static final String TOGGLE_STATE_KEY = "toggleState";  // $NON-NLS$

    private Widget expandBtn;

    private boolean userConfVisibility = true;

    protected SnippetToggleTableView(V snippet, TableColumnModel tableColumnModel) {
        super(snippet, tableColumnModel);
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        addToggleButton();
        updatePanelVisibility();
    }

    @Override
    public void update(TableDataModel dtm) {
        super.update(dtm);
        updatePanelVisibility();
    }

    private void addToggleButton() {
        this.expandBtn = this.container.addHeaderTool(getCurrentHeaderToolIconStyle(), headerToolWidget -> {
            setContentVisible(!isContentVisible());
            snippet.updateView();
        });
    }

    private String getCurrentHeaderToolIconStyle() {
        if(!SessionData.isAsDesign()) {
            return isContentVisible() ? MINUS_STYLE : PLUS_STYLE;
        }
        return isContentVisible() ? EXPANDER_DOWN_STYLE : EXPANDER_RIGHT_STYLE;
    }

    public void setUserConfVisibility(boolean userConfVisibility) {
        this.userConfVisibility = userConfVisibility;
    }

    public boolean isContentVisible() {
        return this.panel.isVisible();
    }

    public void setContentVisible(boolean isVisible) {
        getConfiguration().put(TOGGLE_STATE_KEY, isVisible);
        updatePanelVisibility();
    }

    private void updatePanelVisibility() {
        boolean isVisible = getConfiguration().getBoolean(TOGGLE_STATE_KEY, userConfVisibility);
        this.panel.setVisible(isVisible);
        if (this.expandBtn != null) {
            this.container.setToolIcon(this.expandBtn, getCurrentHeaderToolIconStyle());
        }
    }
}
