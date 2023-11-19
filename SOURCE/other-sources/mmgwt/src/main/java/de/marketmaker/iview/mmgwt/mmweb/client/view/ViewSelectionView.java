package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.HTML;

/**
 * @author umaurer
 */
public abstract class ViewSelectionView {
    protected final IndexedViewSelectionModel model;

    protected final FloatingToolbar toolbar;

    private HTML toolbarTextRight = null;

    public ViewSelectionView(IndexedViewSelectionModel model, FloatingToolbar toolbar) {
        this.model = model;
        this.toolbar = (toolbar != null) ? toolbar : createToolbar();
    }

    /**
     * Subclasses should call this method from their constructor; don't move the call into this class's
     * constructor as subclasses might require initialization before buttons can be created.
     */
    protected void prepareButtons() {
        initButtons();
        updateButtons();
        addButtons();
    }

    public static FloatingToolbar createToolbar() {
        final FloatingToolbar toolbar = new FloatingToolbar();
        toolbar.addStyleName("mm-viewWidget");
        return toolbar;
    }

    public void setToolbarText(SafeHtml safeHtml) {
        if (safeHtml == null) {
            if (this.toolbarTextRight != null) {
                this.toolbarTextRight.setText("");
            }
        }
        else {
            if (this.toolbarTextRight == null) {
                this.toolbarTextRight = new HTML(safeHtml);
                this.toolbar.add(new FillToolItem());
                this.toolbar.add(this.toolbarTextRight);
            }
            else {
                this.toolbarTextRight.setHTML(safeHtml);
            }
        }
    }

    public FloatingToolbar getToolbar() {
        return this.toolbar;
    }

    abstract void initButtons();
    abstract void updateButtons();
    abstract void addButtons();
}
