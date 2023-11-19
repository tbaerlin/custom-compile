/*
 * OrderDetailsView.java
 *
 * Created on 13.11.13 11:16
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.ImageButton;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.util.GuiUtil;

/**
 * @author Markus Dick
 */
public class OrderDetailsView extends OrderConfirmationView<OrderDetailsDisplay.Presenter> implements OrderDetailsDisplay<OrderDetailsDisplay.Presenter> {
    private final ImageButton reloadButton;
    private DialogButton changeOrderButton;

    public OrderDetailsView() {
        super();

        this.reloadButton = GuiUtil.createImageButton("as-reload", null, "as-reload active", I18n.I.doUpdate());  // $NON-NLS$
        this.reloadButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                OrderDetailsView.this.getPresenter().onReloadClicked();
            }
        });
        getToolbar().add(this.reloadButton);
    }

    @Override
    protected void initButtons() {
        this.changeOrderButton = getDialogIfc().addButton(I18n.I.orderEntryChangeOrder(), new Command() {
            @Override
            public void execute() {
                getDialogIfc().keepOpen();
                OrderDetailsView.this.getPresenter().onChangeOrderClicked();
            }
        });
        super.initButtons();
    }

    @Override
    public void setReloadActive(boolean active) {
        this.reloadButton.setActive(active);
    }

    @Override
    public void setChangeOrderButtonVisible(boolean visible) {
        this.changeOrderButton.setVisible(visible);
    }
}
