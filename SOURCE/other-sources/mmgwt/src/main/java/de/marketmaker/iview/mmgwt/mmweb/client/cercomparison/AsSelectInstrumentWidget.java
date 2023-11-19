/*
 * AsSelectInstrumentWidget.java
 *
 * Created on 04.11.2014 12:47
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.cercomparison;

import com.google.gwt.user.client.ui.FlowPanel;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;

/**
 * @author mdick
 */
public class AsSelectInstrumentWidget extends AbstractSelectInstrumentWidget {
    public AsSelectInstrumentWidget(CerComparisonController controller) {
        super(controller);

        final FlowPanel panel = new FlowPanel();
        panel.setStyleName("mm-certcomparison-dnd");

        final Button button = Button.icon("as-plus-24")  // $NON-NLS$
                .tooltip(I18n.I.certificatesSearch())
                .clickHandler(event -> searchForQuote())
                .build();

        panel.add(button);

        initWidget(panel);
    }
}
