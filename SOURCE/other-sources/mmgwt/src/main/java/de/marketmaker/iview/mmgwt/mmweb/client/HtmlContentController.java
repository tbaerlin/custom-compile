/*
 * HtmlContentController.java
 *
 * Created on 12.09.2008 17:51:53
 *
 * Copyright (c) market maker Software AG. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.user.client.ui.HTML;
import de.marketmaker.iview.mmgwt.mmweb.client.view.ContentContainer;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;

/**
 * @author Ulrich Maurer
 */
public class HtmlContentController extends AbstractPageController {
    private final String url;
    private final String height;
    private final boolean printable;

    public HtmlContentController(ContentContainer contentContainer, String url, String height, boolean printable) {
        super(contentContainer);
        this.url = url;
        this.height = height;
        this.printable = printable;
    }

    public HtmlContentController(ContentContainer contentContainer, String url) {
        // NOTE: width=100%, height=95% needed for Serviceteam pages to be displayed w/o scrollbar
        this(contentContainer, url, "95%", true);  // $NON-NLS$
    }

    public void onPlaceChange(PlaceChangeEvent event) {
        // NOTE: width=100%, height=95% needed for Serviceteam pages to be displayed w/o scrollbar
        getContentContainer().setContent(new HTML("<iframe id=\"external-content\" name=\"external-content\" src=\"" // $NON-NLS-0$
                + this.url + "\" width=\"100%\" height=\"" + this.height + "\" frameborder=\"0\"></iframe>")); // $NON-NLS$

//        final Frame frame = new Frame();
//        frame.setUrl();
//        frame.setStyleName("mm-iframe");
//        this.getContentContainer().setContent(frame);
    }

    @Override
    public boolean isPrintable() {
        return this.printable;
    }
}
