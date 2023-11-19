/*
* ContactFormView.java
*
* Created on 05.08.2008 15:03:13
*
* Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
*/
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.extjs.gxt.ui.client.Style;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FormEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.FileUploadField;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.HiddenField;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.CardLayout;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import de.marketmaker.iview.mmgwt.mmweb.client.customer.Customer;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;

import java.util.Date;
import java.util.Iterator;

import static de.marketmaker.iview.mmgwt.mmweb.client.Selector.DZ_BANK_USER;

/**
 * @author Michael Lösch
 * @author Ulrich Maurer
 */
public class ContactFormView extends ContentPanel {

    private final ContactFormController controller;

    private final TextArea taResult = new TextArea();

    private final CardLayout cardLayout = new CardLayout();

    private final FormPanel mailDialog;

    private ContentPanel okPanel;

    private ContentPanel errPanel;

    public ContactFormView(ContactFormController controller) {
        this.controller = controller;
        setLayout(cardLayout);
        addStyleName("mm-contactForm"); // $NON-NLS-0$
        setHeaderVisible(false);

        this.mailDialog = createMailDialog();

        showMailDialog();
    }

    private FormPanel createMailDialog() {
        final FormPanel panel = new FormPanel();
        panel.setHeaderVisible(false);
        panel.setAction(((Settings) GWT.create(Settings.class)).contactFormUri());
        panel.setMethod(FormPanel.Method.POST);
        panel.setEncoding(FormPanel.Encoding.MULTIPART);
        panel.setScrollMode(Style.Scroll.AUTO);

        panel.addListener(Events.Submit, new Listener<FormEvent>() {
            public void handleEvent(FormEvent event) {
                controller.afterSubmit(event);
            }
        });

        final TextField<String> tfPhone = new TextField<>();
        tfPhone.setName("phone"); // $NON-NLS-0$
        final TextField<String> tfEMail = new TextField<>();
        tfEMail.setName("email"); // $NON-NLS-0$
        final TextField<String> tfGenoId = new TextField<>();
        tfGenoId.setName("genoId"); // $NON-NLS-0$
        tfGenoId.setValue(SessionData.INSTANCE.getUser().getGenoId());
        tfGenoId.setReadOnly(true);
        final TextField<String> tfLogin = new TextField<>();
        tfLogin.setName("loginId"); // $NON-NLS-0$
        tfLogin.setValue(SessionData.INSTANCE.getUser().getLogin());
        tfLogin.setReadOnly(true);
        final TextField<String> tfGisCustomerId = new TextField<>();
        tfGisCustomerId.setName("gisCustomerId"); // $NON-NLS-0$
        tfGisCustomerId.setValue(SessionData.INSTANCE.getUser().getGisCustomerId());
        tfGisCustomerId.setReadOnly(true);

        final CheckBox cbCallback = new CheckBox();
        cbCallback.setName("callback"); // $NON-NLS-0$
        cbCallback.setValueAttribute("true"); // $NON-NLS-0$

        final TextField<String> tfDateProposal = new TextField<>();
        tfDateProposal.setName("when"); // $NON-NLS-0$

        final FileUploadField tfAttachment = new FileUploadField();
        tfAttachment.setName("attachment"); // $NON-NLS-0$
        tfAttachment.addStyleName("mm-contactForm-fileupload"); // $NON-NLS-0$

        final TextArea taRemarks = new TextArea();
        taRemarks.setName("remarks"); // $NON-NLS-0$
        taRemarks.setWidth("100%"); // $NON-NLS-0$
        taRemarks.setHeight(200);

        Button btnSend = new Button(I18n.I.send(), new SelectionListener<ButtonEvent>() {
            @Override
            public void componentSelected(ButtonEvent buttonEvent) {
                boolean hasMissingPhone = tfPhone.getValue() == null || tfPhone.getValue().isEmpty();
                boolean hasMissingRemarks = taRemarks.getValue() == null || taRemarks.getValue().isEmpty();
                if (hasMissingPhone || hasMissingRemarks) {
                    Dialog.error(I18n.I.inputIncomplete(), I18n.I.pleaseFillRequiredFields());
                } else {
                    panel.submit();
                }
            }
        });

        FlexTable table = new FlexTable();

        table.setStyleName("mm-contactForm"); // $NON-NLS-0$
        FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        int row = 0;
        table.setHTML(row, 0, I18n.I.telephoneNumberAbbr() + ":<span class=\"mm-mandatory\" >*</span>"); // $NON-NLS-0$
        table.setWidget(row, 1, tfPhone);
        formatter.setStyleName(row, 2, "mm-space"); // $NON-NLS-0$
        table.setHTML(row, 3, I18n.I.userId() + ":");
        if (StringUtil.hasText(tfGenoId.getValue())) {
            table.setWidget(row, 4, tfGenoId);
        } else {
            table.setWidget(row, 4, tfLogin);
        }
        row++;
        table.setHTML(row, 0, I18n.I.email() + ":");
        table.setWidget(row, 1, tfEMail);
        formatter.setStyleName(row, 2, "mm-space"); // $NON-NLS-0$
        // only add the field if it contains data, customer might go bananas 'cause the field is readonly
        if (FeatureFlags.Feature.DZ_RELEASE_2015.isEnabled() && isDz()) {
            table.setHTML(row, 3, I18n.I.gisCustomerId() + ":");
            table.setWidget(row, 4, tfGisCustomerId);
        }
        row++;
        formatter.setStyleName(row, 0, "mm-space"); // $NON-NLS-0$
        row++;
        table.setHTML(row, 0, I18n.I.callbackRequest() + ":");
        table.setWidget(row, 1, cbCallback);
        table.setHTML(row, 3, I18n.I.suggestedDate() + ":");
        table.setWidget(row, 4, tfDateProposal);
        row++;
        formatter.setStyleName(row, 0, "mm-space"); // $NON-NLS-0$
        row++;
        table.setHTML(row, 0, I18n.I.attachment() + ":");
        formatter.setColSpan(row, 1, 4);
        table.setWidget(row, 1, tfAttachment);
        row++;
        formatter.setStyleName(row, 0, "mm-space"); // $NON-NLS-0$
        row++;
        table.setHTML(row, 0, I18n.I.description() + ":<span  class=\"mm-mandatory\" >*</span>"); // $NON-NLS-0$
        row++;
        formatter.setColSpan(row, 0, 5);
        table.setWidget(row, 0, taRemarks);
        row++;
        formatter.setColSpan(row, 0, 4);
        table.setHTML(row, 0, "<span class=\"mm-mandatory\" >*</span> = " + I18n.I.fillRequiredFields()); // $NON-NLS-0$
        formatter.setStyleName(row, 1, "mm-right"); // $NON-NLS-0$
        table.setWidget(row, 1, btnSend);
        row++;
        final HiddenField<String> vwdId = new HiddenField<>();
        vwdId.setName("vwdId"); // $NON-NLS-0$
        vwdId.setValue(SessionData.INSTANCE.getUser().getVwdId());
        table.setWidget(row, 0, vwdId);
        formatter.setColSpan(row, 0, 5);
        final HiddenField<String> login = new HiddenField<>();
        login.setName("login"); // $NON-NLS-0$
        login.setValue(SessionData.INSTANCE.getUser().getLogin());
        table.setWidget(row, 1, login);
        formatter.setColSpan(row, 0, 5);

        panel.add(table);

        add(panel);
        return panel;
    }

    private boolean isDz() {
        return Customer.INSTANCE.isDzWgz() && DZ_BANK_USER.isAllowed();
    }

    private void createOKDialog() {
        this.okPanel = new ContentPanel();
        this.okPanel.setHeaderVisible(false);

        final Date curDate = new Date();
        String date = DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.DATE_MEDIUM).format(curDate) + "       " // $NON-NLS$
                + DateTimeFormat.getFormat(DateTimeFormat.PredefinedFormat.TIME_MEDIUM).format(curDate);

        String resultMessage = I18n.I.contactFormReplyThanks();

        okPanel.setBorders(false);

        taResult.setValue(date + "\n\n" + resultMessage); // $NON-NLS-0$
        taResult.setWidth("100%"); // $NON-NLS-0$
        taResult.setHeight(200);
        taResult.setReadOnly(true);

        FlexTable table = new FlexTable();
        table.setStyleName("mm-contactForm"); // $NON-NLS-0$
        table.setWidth("600px"); // $NON-NLS-0$
        FlexTable.FlexCellFormatter formatter = table.getFlexCellFormatter();
        int row = 0;
        formatter.setStyleName(row, 0, "mm-space"); // $NON-NLS-0$
        table.setHTML(row, 0, I18n.I.sentConfirmation()); 
        formatter.setStyleName(row, 0, "mm-label"); // $NON-NLS-0$
        formatter.setStyleName(row, 1, "mm-right"); // $NON-NLS-0$
        row++;
        formatter.setColSpan(row, 0, 2);
        table.setWidget(row, 0, taResult);

        this.okPanel.add(table);
        add(this.okPanel);
    }

    private void createErrorDialog() {
        this.errPanel = new ContentPanel();
        this.errPanel.setHeaderVisible(false);
        Label lbError = new Label(I18n.I.errorDuringSending()); 
        errPanel.add(lbError);
        add(errPanel);
    }

    public void showMailDialog() {
        this.cardLayout.setActiveItem(this.mailDialog);
    }

    public void showOKDialog() {
        if (this.okPanel == null) {
            createOKDialog();
        }
        this.cardLayout.setActiveItem(this.okPanel);
    }

    public void showErrorDialog() {
        if (this.errPanel == null) {
            createErrorDialog();
        }
        this.cardLayout.setActiveItem(this.errPanel);
    }

    public boolean remove(Widget child) {
        return false;
    }

    @Override
    public Iterator<Component> iterator() {
        return null;
    }

}
