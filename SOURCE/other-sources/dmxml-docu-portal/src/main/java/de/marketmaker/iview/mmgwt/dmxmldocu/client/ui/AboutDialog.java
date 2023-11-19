/*
 * AboutDialog.java
 *
 * Created on 18.09.2012 12:33
 *
 * Copyright (c) vwd AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.dmxmldocu.client.ui;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.PopupPanel;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.TextConstants;
import de.marketmaker.iview.mmgwt.dmxmldocu.client.presenter.AboutPresenter;

/**
 * @author Markus Dick
 */
public class AboutDialog extends DialogBox implements AboutPresenter.Display {

    private final Button okButton;

    public AboutDialog() {
        addStyleName("mm-aboutDialog");//$NON-NLS$
        final FlowPanel mainPanel = new FlowPanel();
        setWidget(mainPanel);
        final FlexTable flexTable = new FlexTable();
        final Image logo = new Image(TextConstants.I.companyLogo());
        logo.addStyleName("mm-company-logo");
        flexTable.setWidget(0, 0, logo);

        final InlineLabel companyName = new InlineLabel(TextConstants.I.companyLabel());
        companyName.addStyleName("mm-company-name");
        flexTable.setWidget(0, 1, companyName);

        final InlineLabel contactPerson = new InlineLabel(TextConstants.I.contactPerson());
        contactPerson.addStyleName("mm-contact-person");
        flexTable.setWidget(2, 1, contactPerson);

        final InlineLabel supportLabel = new InlineLabel(TextConstants.I.contactLabel());
        supportLabel.addStyleName("mm-contact-label");
        flexTable.setWidget(4, 1, supportLabel);

        final InlineLabel contactDetails = new InlineLabel(TextConstants.I.contactDetails());
        contactDetails.addStyleName("mm-contact-person");
        flexTable.setWidget(5, 1, contactDetails);

        final InlineLabel contactEmail = new InlineLabel(TextConstants.I.contactEmail());
        contactEmail.addStyleName("mm-contact-person");
        flexTable.setWidget(6, 1, contactEmail);

        final InlineLabel companyPhone = new InlineLabel(TextConstants.I.companyPhone());
        companyPhone.addStyleName("mm-contact-person");
        flexTable.setWidget(7, 1, companyPhone);
        mainPanel.add(flexTable);
        FlowPanel copyrightPanel = new FlowPanel();
        InlineLabel copyrightText = new InlineLabel(TextConstants.I.copyrightText());
        copyrightPanel.add(copyrightText);
        copyrightPanel.addStyleName("mm-aboutDialog-copyrights"); //$NON-NLS$
        mainPanel.add(copyrightPanel);

        FlowPanel buttonPanel = new FlowPanel();
        okButton = new Button();
        okButton.setText("Ok"); //$NON-NLS$

        buttonPanel.add(okButton);
        buttonPanel.addStyleName("mm-aboutDialog-button"); //$NON-NLS$

        mainPanel.add(buttonPanel);
    }

    @Override
    public PopupPanel asPopupPanel() {
        return this;
    }

    @Override
    public HasClickHandlers getOkButton() {
        return okButton;
    }
}
