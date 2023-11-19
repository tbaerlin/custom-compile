/*
 * ShowAboutEvent.java
 *
 * Created on 18.09.2012 12:24
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.presenter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.ShowAboutEvent;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.event.ShowAboutHandler;

/**
 * @author Markus Dick
 */
public class AboutPresenter implements ShowAboutHandler {
    private HandlerManager eventBus;
    private Display display;

    @Override
    public void onShowAbout() {
        showDisplay();
    }

    public interface Display {
        Widget asWidget();
        PopupPanel asPopupPanel();
        HasClickHandlers getOkButton();
    }

    public AboutPresenter(HandlerManager eventBus, Display display) {
        this.eventBus = eventBus;
        this.display = display;
        bind();
    }

    public void bind() {
        display.getOkButton().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                hideDisplay();
            }
        });

        eventBus.addHandler(ShowAboutEvent.TYPE, this);
    }

    private void showDisplay() {
        final PopupPanel popup = display.asPopupPanel();
        popup.show();
        popup.setModal(true);
        popup.setAutoHideEnabled(true);
        popup.center();
    }

    private void hideDisplay() {
        display.asPopupPanel().hide();
    }

    public void go(HasWidgets container) {
        onShowAbout();
    }
}
