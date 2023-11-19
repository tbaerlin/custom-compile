/*
 * DZPublicationsController.java
 *
 * Created on 10/30/15
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.dmxml.StringResult;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class DZPublicationsController extends AbstractPageController {
    private final DmxmlContext.Block<StringResult> block;

    public DZPublicationsController(ContentContainer contentContainer) {
        super(contentContainer);
        this.block = context.addBlock("MSC_VwdCms_KeyGenerator");
        this.block.setParameter("type", "platow");
    }

    @Override
    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    @Override
    protected void onResult() {
        super.onResult();
        final String query = this.block.isResponseOk() ? this.block.getResult().getResult() : null;
        final HTML widget = new HTML(SimpleHtmlController.createPublicationPage(query));
        widget.setStyleName("mm-simpleHtmlView");
        this.getContentContainer().setContent(widget);
    }
}
