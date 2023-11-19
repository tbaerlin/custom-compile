package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Layout;
import de.marketmaker.iview.mmgwt.mmweb.client.util.BrowserSpecific;

/**
 * @author umaurer
 */
public class CardContentPanel extends ContentPanel {
    private static final String HIDE_STYLE = "mm-cardContent-hidden"; // $NON-NLS$

    public CardContentPanel() {
        BrowserSpecific.INSTANCE.setCardContentHideMode(this);
    }

    public CardContentPanel(Layout layout) {
        super(layout);
        BrowserSpecific.INSTANCE.setCardContentHideMode(this);
    }

    @Override
    protected void onHide() {
        super.onHide();
        addStyleName(HIDE_STYLE);
    }

    @Override
    protected void onShow() {
        super.onShow();
        removeStyleName(HIDE_STYLE);
    }
}
