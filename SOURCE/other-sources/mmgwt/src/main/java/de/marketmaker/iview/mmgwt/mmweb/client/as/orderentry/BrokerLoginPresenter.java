/*
 * BrokerLoginPresenter.java
 *
 * Created on 17.10.13 10:27
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.safehtml.shared.SafeHtml;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.BrokerLoginDisplay.Presenter;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.BrokerLoginDisplay.Callback;

/**
 * @author Markus Dick
 */
public class BrokerLoginPresenter implements Presenter {
    private Callback callback;

    private BrokerLoginDisplay display;

    public BrokerLoginPresenter(BrokerLoginDisplay display) {
        this.display = display;
        this.display.setPresenter(this);
    }

    @Override
    public void onPassword() {
        this.display.hide();
        this.callback.onPassword(this.display.getUser(), this.display.getPassword());
    }

    @Override
    public void onCancel() {
        this.display.hide();
        this.callback.onCancel();
    }

    @Override
    public void setCallback(BrokerLoginDisplay.Callback callback) {
        this.callback = callback;
    }

    public void show(final String user, final SafeHtml message) {
        final BrokerLoginDisplay d = this.display;

        d.setUser(user);

        final boolean hasMessage = message != null && StringUtil.hasText(message.asString());
        d.setMessageVisible(hasMessage);
        if(hasMessage) {
            d.setMessage(message);
        }

        d.setUser(user);
        d.show();
    }

    public BrokerLoginPresenter withHeading(String titleAddendum) {
        this.display.setHeading(titleAddendum);
        return this;
    }
}
