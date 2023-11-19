/*
 * PlatowController.java
 *
 * Created on 12.09.2008 17:51:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HTML;

import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.dmxml.StringResult;

/**
 * @author Ulrich Maurer
 */
public class AttraxFondsadvisorController extends AbstractPageController {
    private final DmxmlContext.Block<StringResult> block;

    public AttraxFondsadvisorController(ContentContainer contentContainer) {
        super(contentContainer);
        this.block = context.addBlock("MSC_VwdCms_KeyGenerator"); // $NON-NLS-0$
        this.block.setParameter("type", "attrax"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    protected void onResult() {
        if (!this.block.isResponseOk()) {
            this.getContentContainer().setContent(new HTML(I18n.I.accessNotPossibleAtPresent()));
            return;
        }

        final String url = SessionData.INSTANCE.getGuiDef("attraxFondsadvisorUrl").stringValue() // $NON-NLS-0$
                + this.block.getResult().getResult()
                + "&genoid=" + SessionData.INSTANCE.getUser().getLogin(); // $NON-NLS-0$
        SimpleHtmlController.displayAttraxFondsadvisor(getContentContainer(), url);
        ActionPerformedEvent.fire("X_ATTRAX"); // $NON-NLS-0$
        Window.open(url, "attrax", ""); // $NON-NLS-0$ $NON-NLS-1$
    }
}
