package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.RequiresResize;
import com.google.gwt.user.client.ui.SimplePanel;

/**
 * @author Ulrich Maurer
 *         Date: 30.11.12
 */
public class ResizableContentPanelAdapter extends Composite implements RequiresResize {
    private final SimplePanel parent;
    private final ContentPanel contentPanel;

    public ResizableContentPanelAdapter(ContentPanel contentPanel) {
        this.contentPanel = contentPanel;
        this.parent = new SimplePanel();
        this.parent.setWidget(contentPanel);
        initWidget(this.parent);
    }

    @Override
    public void onResize() {
        this.contentPanel.setPixelSize(parent.getOffsetWidth(), parent.getOffsetHeight());
    }
}
