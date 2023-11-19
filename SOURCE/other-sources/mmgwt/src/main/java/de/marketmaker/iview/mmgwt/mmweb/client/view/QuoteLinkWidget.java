/*
 * QuoteLinkWidget.java
 *
 * Created on 23.05.2008 12:37:36
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */

package de.marketmaker.iview.mmgwt.mmweb.client.view;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Hyperlink;
import com.google.gwt.user.client.ui.Label;

import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteInstrumentItemsStore;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.FavouriteItemsStores;
import de.marketmaker.iview.mmgwt.mmweb.client.workspace.InstrumentWorkspace;

/**
 * @author Ulrich Maurer
 */
public class QuoteLinkWidget extends Composite implements HasHyperlink {
    private Hyperlink link;

    public QuoteLinkWidget(final String linkText, final QuoteWithInstrument qwi) {
        this(linkText, qwi, true);
    }

    public QuoteLinkWidget(final String linkText, final QuoteWithInstrument qwi, final boolean withIcon) {
        this.link = new Hyperlink(linkText, PlaceUtil.getPortraitPlace(qwi, null));
        if (!withIcon) {
            initWidget(link);
            return;
        }

        final Label labelIcon = new HTML("&nbsp;"); // $NON-NLS-0$
        labelIcon.setStyleName("mm-quoteLinkIcon"); // $NON-NLS-0$
        labelIcon.addClickHandler(clickEvent -> {
            if (SessionData.isAsDesign()) {
                FavouriteItemsStores.ifPresent(FavouriteInstrumentItemsStore.class,
                        c -> c.addItem(qwi));
            }
            else {
                InstrumentWorkspace.INSTANCE.add(qwi);
            }
        });

        final Grid grid = new Grid(1, 2);
        grid.setCellPadding(0);
        grid.setCellSpacing(0);
        grid.setWidget(0, 0, labelIcon);
        grid.setWidget(0, 1, link);

        initWidget(grid);
    }

    public Hyperlink getLink() {
        return link;
    }
}
