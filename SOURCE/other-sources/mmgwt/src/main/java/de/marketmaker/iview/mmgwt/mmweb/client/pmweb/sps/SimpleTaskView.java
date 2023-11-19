package de.marketmaker.iview.mmgwt.mmweb.client.pmweb.sps;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.itools.gwtutil.client.widgets.Button;
import de.marketmaker.iview.mmgwt.mmweb.client.I18n;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.SimpleStandaloneEngine;
import de.marketmaker.iview.pmxml.SubmitAction;

/**
 * Created on 02.09.14
 * Copyright (c) market maker Software AG. All Rights Reserved.
 *
 * @author mloesch
 */
public class SimpleTaskView extends TaskView {
    private final Widget saveButton;
    private final Button cancelButton;

    public SimpleTaskView() {
        this.cancelButton = SimpleStandaloneEngine.createTaskViewPanelButtonFactory(I18n.I.cancel(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                getPresenter().onCancel();
            }
        }).build();
        this.cancelButton.setVisible(false);

        this.saveButton = SimpleStandaloneEngine.createTaskViewPanelSubmitButtonFactory(I18n.I.save(), new ClickHandler() {
            @Override
            public void onClick(ClickEvent clickEvent) {
                getPresenter().onCommit();
            }
        }).build();

        SimpleStandaloneEngine.createAndSetTaskToolbar(this.tvp, this.cancelButton, this.saveButton);
    }

    public SimpleTaskView withCancelButton() {
        this.cancelButton.setVisible(true);
        return this;
    }

    @Override
    public void setActionPreviousVisible(boolean visible) {
        //nothing
    }

    @Override
    public void setSubmitAction(SubmitAction action) {
        this.saveButton.setVisible(action == SubmitAction.SA_COMMIT);
    }
}