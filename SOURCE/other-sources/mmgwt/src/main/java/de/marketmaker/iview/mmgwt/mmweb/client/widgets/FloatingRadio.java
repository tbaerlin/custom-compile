/*
 * FloatingRadio.java
 *
 * Created on 03.12.2015 11:41
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.widgets;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;

import de.marketmaker.itools.gwtutil.client.widgets.input.Radio;
import de.marketmaker.iview.mmgwt.mmweb.client.style.Styles;

/**
 * A simple lazily styled container for left floating radios.
 * @author mdick
 */
public class FloatingRadio<V> extends Composite {
    public FloatingRadio(Radio<V> radio, SafeHtml label) {
        final FlowPanel flowPanel = new FlowPanel();
        initWidget(flowPanel);

        Styles.trySetStyle(this, Styles.get().floatingRadio());
        flowPanel.add(radio);
        flowPanel.add(radio.createSpan(label));
    }
}
