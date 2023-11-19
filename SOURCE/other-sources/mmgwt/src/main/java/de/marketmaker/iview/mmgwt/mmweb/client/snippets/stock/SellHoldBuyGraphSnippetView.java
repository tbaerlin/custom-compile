/*
 * SellHoldBuyGraphSnippetView.java
 *
 * Created on 31.03.2008 11:23:52
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.snippets.stock;

import com.google.gwt.user.client.ui.Grid;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Image;

import de.marketmaker.itools.gwtutil.client.widgets.IconImage;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SellHoldBuy;

import de.marketmaker.iview.mmgwt.mmweb.client.snippets.SnippetView;

import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class SellHoldBuyGraphSnippetView extends SnippetView<SellHoldBuyGraphSnippet> {
    private final Grid grid;
    private final Image imgSell;
    private final Image imgHold;
    private final Image imgBuy;

    public SellHoldBuyGraphSnippetView(SellHoldBuyGraphSnippet snippet) {
        super(snippet);
        setTitle(I18n.I.recommendations()); 

        this.grid = new Grid(5, 3);
        this.grid.setStyleName("mm-shbGraph"); // $NON-NLS-0$

        this.grid.setText(0, 0, I18n.I.sell()); 
        this.grid.setText(1, 0, I18n.I.hold()); 
        this.grid.setText(2, 0, I18n.I.buy()); 
        this.grid.setHTML(3, 0, "&nbsp;"); // $NON-NLS-0$
        this.grid.setText(4, 0, I18n.I.total()); 

        this.imgSell = new Image(IconImage.getUrl("negative-pixel")); // $NON-NLS$
        this.imgSell.setStyleName("mm-shb"); // $NON-NLS-0$
        this.grid.setWidget(0, 2, this.imgSell);

        this.imgHold = new Image(IconImage.getUrl("neutral-pixel")); // $NON-NLS$
        this.imgHold.setStyleName("mm-shb"); // $NON-NLS-0$
        this.grid.setWidget(1, 2, this.imgHold);

        this.imgBuy = new Image(IconImage.getUrl("positive-pixel")); // $NON-NLS$
        this.imgBuy.setStyleName("mm-shb"); // $NON-NLS-0$
        this.grid.setWidget(2, 2, this.imgBuy);

        final HTMLTable.CellFormatter formatter = this.grid.getCellFormatter();
        for (int row = 0; row < this.grid.getRowCount(); row++) {
            formatter.setStyleName(row, 0, "mm-snippetTable-label"); // $NON-NLS-0$
            formatter.setStyleName(row, 1, "mm-right"); // $NON-NLS-0$
            formatter.setStyleName(row, 2, "mm-shbGraph-bar"); // $NON-NLS-0$
        }
    }

    @Override
    protected void onContainerAvailable() {
        super.onContainerAvailable();
        this.container.setContentWidget(this.grid);
    }

    void update(SellHoldBuy shb) {
        if (shb == null) {
            this.grid.setText(0, 1, "--"); // $NON-NLS-0$
            this.grid.setText(1, 1, "--"); // $NON-NLS-0$
            this.grid.setText(2, 1, "--"); // $NON-NLS-0$
            this.grid.setText(4, 1, "--"); // $NON-NLS-0$
            this.imgSell.setWidth("0"); // $NON-NLS-0$
            this.imgHold.setWidth("0"); // $NON-NLS-0$
            this.imgBuy.setWidth("0"); // $NON-NLS-0$
            return;
        }
        final int sell = shb.getAllSell();
        final int hold = shb.getHold();
        final int buy = shb.getAllBuy();

        this.grid.setText(0, 1, String.valueOf(sell));
        this.grid.setText(1, 1, String.valueOf(hold));
        this.grid.setText(2, 1, String.valueOf(buy));
        this.grid.setText(4, 1, String.valueOf(shb.getAll()));

        int max = Math.max(sell, hold);
        max = Math.max(max, buy);
        final float factor = 100f / max;
        final float sellWidth = factor * sell;
        final float holdWidth = factor * hold;
        final float buyWidth = factor * buy;

        this.imgSell.setWidth(sellWidth + "%"); // $NON-NLS-0$
        this.imgHold.setWidth(holdWidth + "%"); // $NON-NLS-0$
        this.imgBuy.setWidth(buyWidth + "%"); // $NON-NLS-0$
    }
}
