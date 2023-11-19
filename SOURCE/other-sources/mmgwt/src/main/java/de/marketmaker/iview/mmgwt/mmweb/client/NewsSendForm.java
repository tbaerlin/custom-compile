/*
 * AlertEditForm.java
 *
 * Created on 08.01.2009 10:03:48
 *
 * Copyright (c) MARKET MAKER Software AG. All Rights Reserved.
 */
package de.marketmaker.iview.mmgwt.mmweb.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.TextBoxBase;
import de.marketmaker.itools.gwtutil.client.widgets.DialogButton;
import de.marketmaker.itools.gwtutil.client.widgets.DialogIfc;
import de.marketmaker.itools.gwtutil.client.widgets.input.LimitedTextArea;
import de.marketmaker.iview.mmgwt.mmweb.client.as.sps.engine.TextUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.data.SessionData;
import de.marketmaker.iview.mmgwt.mmweb.client.data.User;
import de.marketmaker.iview.mmgwt.mmweb.client.events.ActionPerformedEvent;
import de.marketmaker.iview.mmgwt.mmweb.client.util.DebugUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.Dialog;
import de.marketmaker.iview.mmgwt.mmweb.client.util.FormValidationHandler;
import de.marketmaker.iview.mmgwt.mmweb.client.util.RequestBuilderUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.util.StringUtil;
import de.marketmaker.iview.mmgwt.mmweb.client.widgets.WidgetUtil;

import java.util.ArrayList;

import static com.google.gwt.dom.client.Style.Unit.PX;

/**
 * @author Oliver Flege
 * @author Thomas Kiesgen
 */
public class NewsSendForm implements RequestCallback {

    public static final NewsSendForm INSTANCE = new NewsSendForm();

    private CheckBox cc = new CheckBox(I18n.I.ccToSender()); 

    private TextArea infoText = new LimitedTextArea(2000);

    private TextBox recipientName = new TextBox();

    private TextBox recipientAddress = new TextBox();

    private TextBox senderAddress = new TextBox();

    private Label errorText = new Label(""); // $NON-NLS-0$

    private final DialogIfc dialog = Dialog.getImpl().createDialog();
    private final DialogButton btnSend;

    private String newsid;

    private RequestBuilder rb;

    private NewsSendForm() {
        final FlexTable g = new FlexTable();
        final FlexTable.FlexCellFormatter formatter = g.getFlexCellFormatter();
        g.setStyleName("mm-spaceTable");
        g.getElement().getStyle().setWidth(400, PX);
        g.setCellPadding(0);
        g.setCellSpacing(0);
        g.setText(0, 0, I18n.I.recipientName()); 
        g.setWidget(0, 1, recipientName);

        g.setText(1, 0, I18n.I.recipientAddress()); 
        g.setWidget(1, 1, this.recipientAddress);

        g.setText(2, 0, I18n.I.senderAddress()); 
        g.setWidget(2, 1, this.senderAddress);

        g.getRowFormatter().setStyleName(3, "mm-top"); // $NON-NLS-0$
        g.setText(3, 0, I18n.I.infoText()); 
        g.setWidget(3, 1, infoText);
        this.infoText.setHeight("120px"); // $NON-NLS-0$

        g.setWidget(4, 1, this.cc);

        this.dialog.withTitle(I18n.I.sendMessage());
        this.btnSend = this.dialog.addDefaultButton(I18n.I.send(), new Command() {
            @Override
            public void execute() {
                onSend();
            }
        });
        this.dialog.withButton(I18n.I.cancel());

        g.setWidget(5, 0, new Label(I18n.I.messageHowtoMultipleRecipients()));
        formatter.setColSpan(5, 0, 2);
        g.setWidget(6, 0, this.errorText);
        formatter.setColSpan(6, 0, 2);
        this.dialog.withWidget(g);

        this.recipientName.setWidth("230px"); // $NON-NLS-0$
        this.recipientAddress.setWidth("230px"); // $NON-NLS-0$
        this.senderAddress.setWidth("230px"); // $NON-NLS-0$
        this.infoText.setWidth("230px"); // $NON-NLS-0$

        WidgetUtil.applyFormStyling(this.recipientName);
        WidgetUtil.applyFormStyling(this.recipientAddress);
        WidgetUtil.applyFormStyling(this.senderAddress);
        WidgetUtil.applyFormStyling(this.infoText);

        final FormValidationHandler handler = new FormValidationHandler() {
            protected void validateForm() {
                NewsSendForm.this.validateForm();
            }
        };
        applyValidation(this.recipientName, handler);
        applyValidation(this.recipientAddress, handler);
        applyValidation(this.senderAddress, handler);
        applyValidation(this.infoText, handler);
    }

    private void applyValidation(TextBoxBase textBox, FormValidationHandler handler) {
        textBox.addFocusHandler(handler);
        textBox.addChangeHandler(handler);
        textBox.addKeyUpHandler(handler);
    }

    private void onSend() {
        this.rb = RequestBuilderUtil.forPost(MainController.INSTANCE.contextPath + "/newsemail.frm"); // $NON-NLS-0$
        try {
            final String postData = createPostData();
            this.rb.sendRequest(postData, this);
            ActionPerformedEvent.fire("X_NEWS_SEND"); // $NON-NLS-0$
        }
        catch (RequestException e) {
            onSendError(e.getMessage(), e);
        }
    }

    public void onResponseReceived(Request request, Response response) {
        if (response.getStatusCode() == 200) {
            AbstractMainController.INSTANCE.showMessage(I18n.I.messageSent());
        }
        else {
            onSendError(response.getStatusCode() + "\n" + response.getHeadersAsString(), null); // $NON-NLS-0$
        }
    }

    public void onError(Request request, Throwable throwable) {
    }

    private void onSendError(String s, Throwable t) {
        final String message = I18n.I.messageNotSent() + this.rb.getUrl() + "\n" + s;  // $NON-NLS-0$
        DebugUtil.logToServer(message, t);
        Dialog.warning(TextUtil.toSafeHtml(message));
    }


    private String createPostData() {
        final User user = SessionData.INSTANCE.getUser();
        final StringBuilder sb = new StringBuilder()
                .append("vwdId=").append(user.getVwdId()) // $NON-NLS-0$
                .append("&newsId=").append(URL.encodeQueryString(this.newsid)) // $NON-NLS-0$
                .append("&ccToSender=").append(this.cc.getValue()) // $NON-NLS-0$
                .append("&message=").append(URL.encodeQueryString(this.infoText.getText())); // $NON-NLS-0$

        final String from = SessionData.INSTANCE.getGuiDefValue("news-mail-from"); // $NON-NLS-0$
        if (StringUtil.hasText(from)) {
            sb.append("&from=").append(URL.encodeQueryString(from)); // $NON-NLS-0$
        }
        else {
            sb.append("&from=").append(URL.encodeQueryString(this.senderAddress.getText())); // $NON-NLS-0$
        }

        sb.append("&replyTo=").append(URL.encodeQueryString(this.senderAddress.getText())) // $NON-NLS-0$
                .append("&module=").append(URL.encodeQueryString(GuiDefsLoader.getModuleName())) // $NON-NLS$
                .append("&locale=").append(I18n.I.locale()); // $NON-NLS-0$
        final ArrayList<String> addresses = StringUtil.split(this.recipientAddress.getText(), ',');
        final ArrayList<String> names = StringUtil.split(this.recipientName.getText(), ',');
        for (int i = 0; i < addresses.size(); i++) {
            sb.append("&recipients=").append(URL.encodeQueryString(addresses.get(i))) // $NON-NLS-0$
                    .append("&recipientNames=").append(URL.encodeQueryString(names.get(i))); // $NON-NLS-0$
        }
        return sb.toString();
    }


    public void show(String newsid) {
        this.newsid = newsid;

        if (getTextOrNull(this.senderAddress) == null &&
                StringUtil.hasText(AlertController.INSTANCE.getEmailAddress())) {
            this.senderAddress.setText(AlertController.INSTANCE.getEmailAddress());
        }

        this.errorText.setText(""); // $NON-NLS-0$

        this.dialog.show();
        validateForm();
    }

    private String getTextOrNull(TextBoxBase tf) {
        final String result = tf.getText().trim();
        return result.length() > 0 ? result : null;
    }

    private void validateForm() {
        boolean valid = ensureHasText(this.senderAddress, I18n.I.senderAddressMissing()); 
        valid &= ensureHasText(this.recipientAddress, I18n.I.recipientAddressMissing()); 
        valid &= ensureHasText(this.recipientName, I18n.I.recipientNameMissing()); 
        if (valid) {
            valid = ensureRecipientsMatchNames();
        }

        if (valid) {
            this.errorText.setText(""); // $NON-NLS-0$
        }
        this.btnSend.setEnabled(valid);
    }

    private boolean ensureRecipientsMatchNames() {
        final ArrayList<String> addresses = StringUtil.split(this.recipientAddress.getText(), ',');
        final ArrayList<String> names = StringUtil.split(this.recipientName.getText(), ',');
        if (addresses.size() == names.size()) {
            return true;
        }
        this.errorText.setText(I18n.I.recipientCounts(addresses.size(), names.size()));
        return false;
    }

    private boolean ensureHasText(final TextBox textBox, final String msg) {
        if (getTextOrNull(textBox) != null) {
            textBox.removeStyleName("mm-form-invalid"); // $NON-NLS-0$
            return true;
        }
        textBox.addStyleName("mm-form-invalid"); // $NON-NLS-0$
        this.errorText.setText(msg);
        return false;
    }
}
