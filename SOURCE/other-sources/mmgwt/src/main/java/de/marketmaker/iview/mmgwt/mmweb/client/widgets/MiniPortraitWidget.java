package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import de.marketmaker.itools.gwtutil.client.widgets.CompletionLabel;
import de.marketmaker.itools.gwtutil.client.widgets.NameValueTable;
import de.marketmaker.iview.dmxml.IMGResult;
import de.marketmaker.iview.mmgwt.mmweb.client.QuoteWithInstrument;
import de.marketmaker.iview.mmgwt.mmweb.client.Selector;
import de.marketmaker.iview.mmgwt.mmweb.client.data.ContentFlagsEnum;
import de.marketmaker.iview.mmgwt.mmweb.client.events.PlaceChangeEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.table.TableDataModel;
import de.marketmaker.iview.mmgwt.mmweb.client.util.ChartUrlFactory;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PlaceUtil;

/**
 * Created on 10.08.11 16:12
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author Michael Lösch
 */

public class MiniPortraitWidget extends Composite {

    private final CompletionLabel name;
    private final NameValueTable nvt;
    private final FlowPanel panel;

    public MiniPortraitWidget(String headText, String footText, IMGResult img) {
        this.nvt = new NameValueTable();

        panel = new FlowPanel();
        panel.setStyleName("mm-mini-portrait"); // $NON-NLS$
        final HTML head = new HTML();
        head.setStyleName("mm-mini-portrait-head"); // $NON-NLS$
        if (headText == null) {
            head.setVisible(false);
        }
        else {
            head.setHTML(headText);
        }

        this.name = new CompletionLabel("");
        this.name.setStyleName("mm-mini-portrait-name");

        final Label foot = new Label();
        foot.setStyleName("mm-mini-portrait-foot"); // $NON-NLS$
        foot.setText(footText);

        final QuoteWithInstrument qwi = new QuoteWithInstrument(img.getInstrumentdata(), img.getQuotedata());
        final PlaceChangeEvent linkToPortrait = new PlaceChangeEvent(PlaceUtil.getPortraitPlace(qwi, "U")); // $NON-NLS$

        final Image chart = new Image();
        chart.setStyleName("mm-mini-portrait-chart"); // $NON-NLS$
        chart.setUrl(ChartUrlFactory.getUrl(img.getRequest()));
        chart.addClickHandler(new ClickHandler() {
            public void onClick(ClickEvent clickEvent) {
                PlaceUtil.fire(linkToPortrait);
            }
        });

        if (Selector.DZ_BANK_USER.isAllowed() && Selector.PRODUCT_WITH_PIB.isAllowed()
                && ContentFlagsEnum.PibDz.isAvailableFor(img.getQuotedata())) {
            panel.add(DzPibMarginDialog.createTriggerWidget(img.getQuotedata().getVwdcode()));
        }
        panel.add(head);
        panel.add(chart);
        panel.add(this.name);
        panel.add(this.nvt);
        initWidget(panel);
    }

    public void update(final String name, TableDataModel tdm) {
        assert(tdm.getColumnCount() == 2);
        this.panel.setTitle(name);
        this.name.setText(name);
        this.nvt.clear();
        for (int row = 0, rowCount = tdm.getRowCount(); row < rowCount; row++) {
            this.nvt.addLine((String) tdm.getValueAt(row, 0), (String) tdm.getValueAt(row, 1), true);
        }
    }
}