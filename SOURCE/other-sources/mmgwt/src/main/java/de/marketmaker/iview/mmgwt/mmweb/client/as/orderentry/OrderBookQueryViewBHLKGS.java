/*
 * OrderBookQueryViewBHLKGS.java
 *
 * Created on 14.10.13 17:31
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers.TextWithKeyItemMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
public class OrderBookQueryViewBHLKGS extends Composite implements OrderBookQueryDisplay {
    interface OrderBookQueryViewBHLKGSUiBinder extends UiBinder<HTMLPanel, OrderBookQueryViewBHLKGS> {}
    private static OrderBookQueryViewBHLKGSUiBinder uiBinder = GWT.create(OrderBookQueryViewBHLKGSUiBinder.class);

    @UiField(provided = true)
    protected final I18n i18n = I18n.I;

    @UiField
    protected MappedListBox<TextWithKey> stateChoice;
    @UiField
    protected TextBox wknField;
    @UiField
    protected TextBox orderNumberField;
    @UiField
    protected Button okButton;
    @UiField
    protected Button cancelButton;

    private Presenter presenter;

    public OrderBookQueryViewBHLKGS() {
        initWidget(uiBinder.createAndBindUi(this));

        this.stateChoice.setItemMapper(new TextWithKeyItemMapper());
    }

    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public Widget asWidget() {
        return this;
    }

    @Override
    public void setWkn(String wkn) {
        this.wknField.setText(wkn);
    }

    @Override
    public String getWkn() {
        return this.wknField.getText();
    }

    @Override
    public void setOrderExecutionStates(List<TextWithKey> orderExecutionStates) {
        this.stateChoice.setItems(orderExecutionStates);
    }

    @Override
    public void setSelectedOrderExecutionState(TextWithKey selectedOrderExecutionState) {
        this.stateChoice.setSelectedItem(selectedOrderExecutionState);
    }

    @Override
    public TextWithKey getSelectedOrderExecutionState() {
        return this.stateChoice.getSelectedItem();
    }

    @Override
    public void setOrderNumber(String orderNumber) {
        this.orderNumberField.setText(orderNumber);
    }

    @Override
    public String getOrderNumber() {
        return this.orderNumberField.getText();
    }

    @UiHandler("okButton")
    void onOkButtonClicked(ClickEvent event) {
        this.presenter.onOkClicked();
    }

    @UiHandler("cancelButton")
    void onCancelButtonClicked(ClickEvent event) {
        this.presenter.onCancelClicked();
    }
}