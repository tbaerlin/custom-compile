/*
 * TeaserWidget.java
 *
 * Created on 10/1/14 8:00 PM
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.workspace;

import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.Image;

import de.marketmaker.iview.tools.i18n.NonNLS;

/**
 * @author Stefan Willenbrock
 */
@NonNLS
public class TeaserWidget extends Composite {

    private enum State {
        DISABLED, CONFIGURED_TEASER, STATIC_TEASER
    }

    private final Workspace workspace;

    private final FocusPanel panel;

    private Image image;

    private int naturalTeaserWidth;
    
    private int naturalTeaserHeight;

    private TeaserConfigData teaserConfigData;

    private State widgetState = State.DISABLED;

    private int teaserHeight;

    public TeaserWidget(final Workspace workspace) {
        this.workspace = workspace;
        panel = new FocusPanel();
        panel.setStyleName("mm-productTeaser");
        panel.addStyleName("mm-link");
        panel.addClickHandler(event -> {
            if (widgetState != State.CONFIGURED_TEASER)
                return;
            if (teaserConfigData.isLinkEnabled()) {
                com.google.gwt.user.client.Window.open(teaserConfigData.getLinkUrl(),
                        teaserConfigData.getLinkTarget(), "");
            }
        });
        insertNewImage();
        initWidget(panel);
    }

    private void insertNewImage() {
        image = new Image();
        image.addLoadHandler(event -> {
            workspace.layoutTeaser();
            naturalTeaserWidth = image.getWidth();
            naturalTeaserHeight = image.getHeight();
            ContentPanel cp = TeaserWidget.this.workspace.getContentPanel();
            scaleImage(cp.getWidth());
            workspace.layoutTeaser();
        });
        panel.setWidget(image);
    }

    public void setTeaserConfigData(TeaserConfigData teaserConfigData) {
        insertNewImage();
        this.image.setUrl(DzBankTeaserUtil.getCurrentImageUrl());
        this.teaserConfigData = teaserConfigData;
        this.widgetState = State.CONFIGURED_TEASER;
    }

    public void setImageUrl(String imageUrl) {
        this.image.setUrl(UriUtils.fromTrustedString(imageUrl));
        this.widgetState = State.STATIC_TEASER;
    }

    public void scaleImage(int x) {
        teaserHeight = Workspace.TEASER_SIZE - 1; // containing div has 1px top border
        if (naturalTeaserWidth == 0 || naturalTeaserHeight == 0) {
            image.setWidth("auto");
            image.setHeight("auto");
            return;
        }
        final int width = (int) (teaserHeight / (double) naturalTeaserHeight * naturalTeaserWidth);
        final int xMinusBorder = x >= 2 ? x - 2 : 0;
        if (width > xMinusBorder) {
            image.setWidth(xMinusBorder + "px");
            teaserHeight = (int) (xMinusBorder / (double) naturalTeaserWidth * naturalTeaserHeight);
            image.setHeight(teaserHeight + "px");
        } else {
            image.setWidth(width + "px");
            image.setHeight(teaserHeight + "px");
        }
    }

    public int getTeaserHeight() {
        return teaserHeight;
    }
}
