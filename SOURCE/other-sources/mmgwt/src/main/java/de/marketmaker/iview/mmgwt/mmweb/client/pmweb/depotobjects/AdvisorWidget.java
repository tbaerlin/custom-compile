/*
 * AdvisorWidget.java
 *
 * Created on 27.03.2012 10:32
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects;

import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Panel;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.PmRenderers;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Address;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.data.depotobjects.Advisor;

import static de.marketmaker.iview.mmgwt.mmweb.client.pmweb.depotobjects.AbstractUserObjectView.*;

/**
 * @author Markus Dick
 */
public class AdvisorWidget extends Composite implements TakesValue<Advisor> {
    private final Panel layout;
    private Advisor advisor;

    public AdvisorWidget() {
        this.advisor = null;
        this.layout = new FlowPanel();
        initWidget(this.layout);
    }

    @Override
    public void setValue(Advisor value) {
        this.advisor = value;
        update();
    }

    @Override
    public Advisor getValue() {
        return this.advisor;
    }

    private void update() {
        this.layout.clear();

        if(this.advisor == null) {
            final HasWidgets hasWidgets = this.layout;
            addField(hasWidgets, I18n.I.advisor(), "--");
            return;
        }

        addDefaultPanel(this.advisor);
        addAddressWidget(this.advisor);
    }

    private void addDefaultPanel(Advisor a) {
        final HasWidgets hasWidgets = this.layout;
        addField(hasWidgets, I18n.I.advisor(), PmRenderers.ADDRESS_FULL_NAME_WITH_SALUTATION.render(a.getAddress()));
        addField(hasWidgets, I18n.I.advisorNumberAbbr(), a.getAdvisorNumber());
        addField(hasWidgets, I18n.I.advisorAreaName(), a.getAreaName());
        addField(hasWidgets, I18n.I.advisorAreaNumberAbbr(), a.getAreaNumber());
    }

    private void addAddressWidget(Advisor a) {
        final HasWidgets hasWidgets = this.layout;
        final Address address = a.getAddress();
        if(address == null || address.isEmpty()) {
            addField(hasWidgets, I18n.I.address(), I18n.I.noContactDataAvailable());
        }

        final AddressWidget addressView = new AddressWidget();
        hasWidgets.add(addressView.asWidget());
        addressView.setValue(address);
    }
}
