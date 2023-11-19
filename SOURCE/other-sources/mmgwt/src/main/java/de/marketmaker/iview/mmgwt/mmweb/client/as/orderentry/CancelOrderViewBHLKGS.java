/*
 * CancelOrderViewBHLKGS.java
 *
 * Created on 11.11.13 14:03
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.orderentry.input.mappers.TextWithKeyItemMapper;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.MappedListBox;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.SimpleGlassablePanel;
import de.marketmaker.iview.pmxml.TextWithKey;

import java.util.List;

/**
 * @author Markus Dick
 */
public class CancelOrderViewBHLKGS implements CancelOrderDisplayBHLKGS {
    interface CancelOrderViewBHLKGSBinder extends UiBinder<HTMLPanel, CancelOrderViewBHLKGS> {}

    private static CancelOrderViewBHLKGSBinder uiBinder = GWT.create(CancelOrderViewBHLKGSBinder.class);
    private Presenter presenter;

    @UiField(provided = true)
    protected final I18n i18n = I18n.I;

    @UiField protected Label orderNumberField;

    @UiField protected SimpleGlassablePanel loadingIndicator;
    @UiField protected MappedListBox<TextWithKey> cancellationReasonsChoice;
    @UiField protected CheckBox printCancellationConfirmationCheck;
    @UiField protected MappedListBox<TextWithKey> cannedTextForOrderConfirmationChoice;
    @UiField protected TextBox internalText1Field;
    @UiField protected TextBox internalText2Field;

    private final DialogButton proceedButton;
    private final DialogButton cancelButton;

    private final DialogIfc dialog;

    public CancelOrderViewBHLKGS() {
        this.dialog = Dialog.getImpl().createDialog()
                .withStyle("as-oe-dlg") // $NON-NLS$
                .withTitle(I18n.I.orderEntryBHLKGSCancelOrderWindowTitle(""))
                .withWidget(uiBinder.createAndBindUi(this));

        this.cancellationReasonsChoice.setItemMapper(new TextWithKeyItemMapper());
        this.cannedTextForOrderConfirmationChoice.setItemMapper(new TextWithKeyItemMapper());

        this.proceedButton = this.dialog.addButton(I18n.I.orderEntryCancelOrder(), new Command() {
            @Override
            public void execute() {
                CancelOrderViewBHLKGS.this.dialog.keepOpen();
                CancelOrderViewBHLKGS.this.presenter.onProceedClicked();
            }
        });

        final Command cancelCommand = new Command() {
            @Override
            public void execute() {
                CancelOrderViewBHLKGS.this.dialog.keepOpen();
                CancelOrderViewBHLKGS.this.presenter.onCancelClicked();
            }
        };
        this.cancelButton = this.dialog.addButton(I18n.I.cancel(), cancelCommand);

        this.dialog.withEscapeCommand(cancelCommand);
    }

    @Override
    public void show() {
        this.dialog.show();
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void setOrderNumber(String orderNumber) {
        this.dialog.withTitle(I18n.I.orderEntryBHLKGSCancelOrderWindowTitle(orderNumber));
        this.orderNumberField.setText(orderNumber);
    }

    @Override
    public void setCancellationReasons(List<TextWithKey> deleteOrderReasons) {
        this.cancellationReasonsChoice.setItems(deleteOrderReasons);
    }

    @Override
    public void setCancellationReasonsSelectedItem(TextWithKey selectedItem) {
        this.cancellationReasonsChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getCancellationReasonsSelectedItem() {
        return this.cancellationReasonsChoice.getSelectedItem();
    }

    @Override
    public void setPrintCancellationConfirmation(boolean print) {
        this.printCancellationConfirmationCheck.setValue(print);
    }

    @Override
    public boolean isPrintCancellationConfirmation() {
        return this.printCancellationConfirmationCheck.getValue();
    }

    @Override
    public void setCannedTextForOrderConfirmations(List<TextWithKey> cannedTexts) {
        this.cannedTextForOrderConfirmationChoice.setItems(cannedTexts);
    }

    @Override
    public void setCannedTextForOrderConfirmationsSelectedItem(TextWithKey selectedItem) {
        this.cannedTextForOrderConfirmationChoice.setSelectedItem(selectedItem);
    }

    @Override
    public TextWithKey getCannedTextForOrderConfirmationsSelectedItem() {
        return this.cannedTextForOrderConfirmationChoice.getSelectedItem();
    }

    @Override
    public void setInternalText1(String internalText1Field) {
        this.internalText1Field.setText(internalText1Field);
    }

    @Override
    public String getInternalText1() {
        return this.internalText1Field.getText();
    }

    @Override
    public void setInternalText2(String internalText2Field) {
        this.internalText2Field.setText(internalText2Field);

    }

    @Override
    public String getInternalText2() {
        return this.internalText2Field.getText();
    }

    @Override
    public void setLoadingIndicatorVisible(boolean visible) {
        if(visible) {
            this.loadingIndicator.showGlass();
        }
        else {
            this.loadingIndicator.hideGlass();
        }
    }

    @Override
    public void setProceedButtonEnabled(boolean enabled) {
        this.proceedButton.setEnabled(enabled);
    }

    @Override
    public void setCancelButtonEnabled(boolean enabled) {
        this.cancelButton.setEnabled(enabled);
    }
}
