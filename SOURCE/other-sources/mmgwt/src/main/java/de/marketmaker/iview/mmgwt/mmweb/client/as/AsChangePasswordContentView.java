/*
 * AsChangePasswordContentView.java
 *
 * Created on 22.01.2015 12:42:23
 *
 * Copyright (c) vwd GmbH. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client.as;

import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps.TaskViewPanel;
import de.marketmaker.iview.pmxml.ErrorMM;
import de.marketmaker.iview.pmxml.ErrorSeverity;

/**
 * @author Markus Dick
 */
public class AsChangePasswordContentView extends AsChangePasswordView {
    private final TaskViewPanel tvp;
    private final Button submitButton;

    public AsChangePasswordContentView() {
        super(false);
        this.submitButton = SimpleStandaloneEngine.createTaskViewPanelSubmitButtonFactory(
                I18n.I.changePassword(), event -> getPresenter().onOk()).build();

        this.tvp = SimpleStandaloneEngine.createTaskViewPanel(super.asWidget(), this.submitButton);
    }

    @Override
    public void setCancelEnabled(boolean enabled) {
        //do nothing
    }

    @Override
    public void setSubmitEnabled(boolean enabled) {
        this.submitButton.setEnabled(enabled);
    }

    @Override
    public void showSuccessMessage(String message) {
        final ErrorMM diError = SimpleStandaloneEngine.toErrorMM(message);
        diError.setErrorSeverity(ErrorSeverity.ESV_HINT);
        this.formSection.visualizeError(diError, true);
    }

    @Override
    public TaskViewPanel asWidget() {
        return this.tvp;
    }
}
