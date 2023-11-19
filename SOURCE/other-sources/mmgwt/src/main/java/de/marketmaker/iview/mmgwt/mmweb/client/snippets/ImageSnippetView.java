/*
 * PriceListSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets;

import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class ImageSnippetView extends SnippetView<ImageSnippet> {
    private Image image = new Image();
    private String imageName;
    private String width;
    private String height;

    public ImageSnippetView(ImageSnippet imageSnippet) {
        super(imageSnippet);
        final SnippetConfiguration config = imageSnippet.getConfiguration();
        setTitle(config.getString("title", I18n.I.chart()));  // $NON-NLS-0$

        this.imageName = config.getString("imageName", "dummy.png"); // $NON-NLS-0$ $NON-NLS-1$
        this.width= config.getString("chartwidth", "260"); // $NON-NLS-0$ $NON-NLS-1$
        this.height = config.getString("chartheight", "280"); // $NON-NLS-0$ $NON-NLS-1$
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();

        final String caption = getConfiguration().getString("caption", null); // $NON-NLS-0$
        if (caption == null) {
            this.container.setContentWidget(this.image);
        }
        else {
            final Panel panel = new FlowPanel();
            panel.add(this.image);
            final Label labelCaption = new Label(caption);
            labelCaption.setStyleName("mm-center"); // $NON-NLS-0$
            panel.add(labelCaption);
            this.container.setContentWidget(panel);
        }
    }

    void update() {
        this.image.setUrl(ChartUrlFactory.getUrl(this.imageName+"?width="+this.width+"&height="+this.height)); // $NON-NLS-0$ $NON-NLS-1$
        this.image.setVisible(true);
    }
}
