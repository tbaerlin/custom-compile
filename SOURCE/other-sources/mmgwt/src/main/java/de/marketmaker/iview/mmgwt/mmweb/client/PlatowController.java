/*
 * PlatowController.java
 *
 * Created on 12.09.2008 17:51:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.HTML;
import com.extjs.gxt.ui.client.widget.ContentPanel;

import de.marketmaker.iview.dmxml.StringResult;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DmxmlContext;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author Ulrich Maurer
 */
public class PlatowController extends AbstractPageController {
    private final DmxmlContext.Block<StringResult> block;
    private boolean inlineFrame = false;

    public PlatowController(ContentContainer contentContainer) {
        super(contentContainer);

        this.block = context.addBlock("MSC_VwdCms_KeyGenerator"); // $NON-NLS-0$
        this.block.setParameter("type", "platow"); // $NON-NLS-0$ $NON-NLS-1$
    }

    public PlatowController withInlineFrame(boolean inlineFrame) {
        this.inlineFrame = inlineFrame;
        return this;
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        refresh();
    }

    protected void onResult() {
        if (!this.block.isResponseOk()) {
            this.getContentContainer().setContent(new HTML(I18n.I.accessNotPossibleAtPresent())); 
            return;
        }

        final String platowUrl = Settings.INSTANCE.platowUrl() + "?" + this.block.getResult().getResult(); // $NON-NLS-0$

        if (this.inlineFrame) {
            final Frame frame = new Frame(platowUrl);
            frame.setSize("100%", "100%"); // $NON-NLS-0$ $NON-NLS-1$
            frame.getElement().setAttribute("id", "platow"); // $NON-NLS-0$ $NON-NLS-1$
            frame.getElement().setAttribute("name", "platow"); // $NON-NLS-0$ $NON-NLS-1$
            frame.getElement().setAttribute("frameborder", "0"); // $NON-NLS-0$ $NON-NLS-1$
            final ContentPanel panel = new ContentPanel();
            panel.setBorders(false);
            panel.add(frame);
            this.getContentContainer().setContent(panel);
        }
        else {
            SimpleHtmlController.displayPlatow(this.getContentContainer(), platowUrl);
//            Window.open(platowUrl, "_blank", "menubar=yes,location=no,status=no");
        }
    }
}
