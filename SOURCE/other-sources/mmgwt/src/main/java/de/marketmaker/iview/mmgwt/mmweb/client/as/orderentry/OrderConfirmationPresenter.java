/*
 * OrderConfirmationPresenter.java
 *
 * Created on 05.02.13 15:00
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import de.marketmaker.itools.gwtutil.client.util.date.MmJsDate;
import de.marketmaker.iview.mmgwt.mmweb.client.util.PrintWindow;

import java.util.List;

import static de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.OrderConfirmationDisplay.*;

/**
 * @author Markus Dick
 */
public class OrderConfirmationPresenter implements Presenter {
    public interface Callback {
        void onExecute();
        void onCancel();
        void onBack();
    }

    public static abstract class AbstractCallback implements Callback {
        @Override
        public void onExecute() {

        }

        @Override
        public void onCancel() {

        }

        @Override
        public void onBack() {

        }
    }

    private final OrderConfirmationDisplay<OrderConfirmationPresenter> display;
    private final Callback callback;
    private final String title;
    private int columns;

    public OrderConfirmationPresenter(String title, Callback callback) {
        this.display = new OrderConfirmationView<>();
        this.display.setPresenter(this);
        this.callback = callback;
        this.title = title;
        this.columns = 2;
    }

    public void show(List<Section> model) {
        this.display.setTitle(this.title);
        this.display.setSections(model);
        this.display.setColumns(this.columns);
        this.display.setPrintDate(new MmJsDate());

        this.display.show();
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public void setExecuteButtonText(String text) {
        this.display.setExecuteButtonText(text);
    }

    public void setExecuteButtonVisible(boolean visible) {
        this.display.setExecuteButtonVisible(visible);
    }

    public void setCancelButtonText(String text) {
        this.display.setCancelButtonText(text);
    }

    public void setCancelButtonVisible(boolean visible) {
        this.display.setCancelButtonVisible(visible);
    }

    public void setBackButtonText(String text) {
        this.display.setBackButtonText(text);
    }

    public void setBackButtonVisible(boolean visible) {
        this.display.setBackButtonVisible(visible);
    }

    public void setPrintDateVisible(boolean visible) {
        this.display.setPrintDateVisible(visible);
    }

    @Override
    public void onExecuteClicked() {
        this.display.hide();
        this.callback.onExecute();
    }

    @Override
    public void onCancelClicked() {
        this.display.hide();
        this.callback.onCancel();
    }

    @Override
    public void onBackClicked() {
        this.display.hide();
        this.callback.onBack();
    }

    @Override
    public void onPrintClicked() {
        PrintWindow.print(this.display.getPrintHtml(), null);
    }

    public OrderConfirmationDisplay getDisplay() {
        return display;
    }

    public Callback getCallback() {
        return callback;
    }

    public String getTitle() {
        return title;
    }
}
