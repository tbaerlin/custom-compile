/*
 * AsChangePasswordView.java
 *
 * Created on 22.01.2015 12:42:23
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Document;

import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.iview.mmgwt.mmweb.client.AbstractMainController;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskDisplay;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.view.HasShowHide;

/**
 * @author Markus Dick
 */
public class AsChangePasswordWindowView extends AsChangePasswordView implements HasShowHide {
    private final DialogIfc dialog;

    private final DialogButton okButton;

    public AsChangePasswordWindowView() {
        super(true);

        Document.get().getBody().addClassName("asView"); // $NON-NLS$

        this.dialog = Dialog.getImpl().createDialog();
        this.dialog.withTitle(I18n.I.changePassword())
                .withWidget(this.asWidget())
                .withStyle(TaskDisplay.SPS_TASK_VIEW_STYLE)
                .withEscapeCommand(this.dialog::keepOpen);

        this.okButton = this.dialog.addDefaultButton(I18n.I.ok(), () -> {
            this.dialog.keepOpen();
            getPresenter().onOk();
        });
    }

    @Override
    public void setCancelEnabled(boolean enabled) {
        //do nothing
    }

    @Override
    public void setSubmitEnabled(boolean enabled) {
        this.okButton.setEnabled(enabled);
    }

    @Override
    public void showSuccessMessage(String message) {
        AbstractMainController.INSTANCE.showMessage(message);
    }

    @Override
    public void show() {
        this.dialog.show();
        Scheduler.get().scheduleDeferred(this.formSection::focusFirst);
        clearValidationError();
        this.changeHandlersEnabled = true;
    }

    @Override
    public void hide() {
        this.dialog.closePopup();
    }
}
